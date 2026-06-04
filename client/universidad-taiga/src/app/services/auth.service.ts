import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { APP_API_URL } from '../tokens/api.tokens';
import { AuthStatus, ForgotPasswordRequest, ResetPasswordRequest, MessageResponse } from '../models/auth.model';

/**
 * =============================================================================
 * AUTH SERVICE - Servicio de Autenticación JWT
 * =============================================================================
 * 
 * Responsabilidades:
 * 1. Login - Enviar credenciales y guardar tokens
 * 2. Refresh - Renovar access token cuando expire
 * 3. Logout - Invalidar sesión y limpiar datos
 * 4. Estado - Saber si el usuario está autenticado
 * 
 * 
 * ARQUITECTURA DE STORAGE
 * -----------------------
 * 
 * Access Token:
 *   - Se guarda en MEMORIA (signal) - NO en localStorage ni sessionStorage
 *   - ¿POR QUÉ? Porque localStorage es vulnerable a XSS attacks
 *   - Si un atacante inyecta JavaScript, puede leer localStorage
 *   - En memoria, solo el código de la app puede acceder
 *   - Contras: se pierde al recargar la página (por eso usamos refresh)
 * 
 * Refresh Token:
 *   - Se guarda en una COOKIE HttpOnly
 *   - ¿POR QUÉ HttpOnly? JavaScript NO puede leer esta cookie
 *   - Solo el navegador la envía automáticamente al backend
 *   - El atacante no puede robarla via XSS
 * 
 * 
 * ESTRATEGIA DE AUTO-REFRESH
 * ---------------------------
 * No esperamos a que el backend retorne 401 para hacer refresh.
 * Calculamos cuándo falta 1 minuto para que expire el token
 * y hacemos refresh proactivamente.
 * 
 * Ejemplo: Token expira en 15 min (900 seg)
 * Hacemos refresh a los 14 min (840 seg)
 * Así el usuario nunca ve un 401 por token expirado.
 */
@Injectable({
  providedIn: 'root'  // Singleton - una sola instancia en toda la app
})
export class AuthService {
  
  // ===========================================================================
  // DEPENDENCIAS
  // ===========================================================================
  
  private http = inject(HttpClient);
  private router = inject(Router);
  
  // ===========================================================================
  // URL DEL BACKEND
  // ===========================================================================
  
  private readonly apiBaseUrl = inject(APP_API_URL);
  private readonly apiUrl = `${this.apiBaseUrl}/auth`;
  
  // ===========================================================================
  // CLAVES PARA LOCALSTORAGE
  // ===========================================================================
  
  private readonly TOKEN_KEY = 'auth_token';
  private readonly ROLES_KEY = 'auth_roles';
  
  // ===========================================================================
  // SEÑALES REACTIVAS (Angular 17+)
  // ===========================================================================
  
  /**
   * Access Token en memoria.
   * 
   * Ahora también se persiste en localStorage para sobrevivir recargas.
   * En producción, considera usar cookies HttpOnly para mayor seguridad.
   */
  private accessToken = signal<string | null>(null);
  
  /**
   * Roles del usuario actual.
   * Se extraen del access token cuando se hace login/refresh.
   */
  private userRoles = signal<string[]>([]);
  
  /**
   * Tiempo en milisegundos hasta que expire el access token.
   * Se usa para programar el auto-refresh.
   */
  private tokenExpiryMs = signal<number>(0);

  /**
   * Bandera para evitar múltiples verificaciones simultáneas.
   */
  private isVerifying = false;

  /**
   * Estado de la autenticación para que los guards puedan esperar
   * mientras se verifica la sesión con el backend.
   *
   * Valores:
   * - 'loading': verificando sesión al iniciar (refresh en progreso)
   * - 'authenticated': sesión activa y válida
   * - 'unauthenticated': no hay sesión activa
   */
  public authStatus = signal<AuthStatus>('loading');

  // ===========================================================================
  // CONSTRUCTOR - Carga sesión DESDE localStorage DE FORMA SINCRÓNICA
  // ===========================================================================

  constructor() {
    // PASO 1: Cargar token desde localStorage de forma síncrona
    // Esto da una respuesta inmediata al guard si el token sigue vigente.
    const storedToken = this.loadTokenFromStorage();
    if (storedToken) {
      console.log('✅ Token cargado desde localStorage al iniciar');
      this.accessToken.set(storedToken);
      this.authStatus.set('authenticated');
    }

    // PASO 2: Intentar refrescar la sesión con la cookie HttpOnly
    // Cubre el caso donde el access token expiró (>15 min) pero
    // el refresh token cookie sigue vivo (7 días).
    //
    // SIEMPRE intenta el refresh para restaurar la sesión.
    // Si la cookie no existe o expiró, el backend responde 401 y
    // se limpia el estado.
    this.refreshSession();
  }

  /**
   * Intenta restaurar la sesión usando la cookie de refresh token.
   *
   * Se ejecuta al iniciar la app, haya o no token en localStorage.
   * Si la cookie de refresh es válida, obtiene un nuevo access token
   * y restablece la sesión completa.
   */
  private refreshSession(): void {
    if (this.isVerifying) return;
    this.isVerifying = true;

    this.validateToken().subscribe({
      next: (response) => {
        if (response.accessToken && response.expiresIn) {
          // Refrescar access token en memoria
          this.setAccessToken(response.accessToken, response.expiresIn);

          // Extraer y guardar roles
          const roles = this.extractRolesFromToken(response.accessToken);
          this.userRoles.set(roles);

          // Persistir en localStorage para sobrevivir recargas
          this.saveTokenToStorage(response.accessToken, roles);

          // Programar próximo auto-refresh
          this.scheduleTokenRefresh(response.expiresIn);

          this.authStatus.set('authenticated');
          console.log('✅ Sesión restaurada desde cookie de refresh');
        }
        this.isVerifying = false;
      },
      error: () => {
        // No hay cookie de refresh válida o expiró.
        // Si había un token expirado en localStorage ya se limpió en
        // loadTokenFromStorage(). Si no había, es un usuario nuevo.
        console.log('ℹ️ No hay sesión activa (refresh cookie no disponible)');
        this.clearAuthState();
        this.isVerifying = false;
      }
    });
  }

  /**
   * Valida el token con el backend.
   * Usa la cookie de refresh token automáticamente (withCredentials).
   */
  private validateToken(): Observable<{ accessToken?: string; tokenType?: string; expiresIn?: number }> {
    return this.http.post<{ accessToken?: string; tokenType?: string; expiresIn?: number }>(
      `${this.apiUrl}/refresh`,
      {},
      {
        withCredentials: true  // IMPORTANTE: Envía la cookie de refresh
      }
    ).pipe(
      tap(response => {
        console.log('Sesión verificada exitosamente');
      }),
      catchError(error => {
        console.error('Error verificando sesión:', error);
        return throwError(() => error);
      })
    );
  }
  
  // ===========================================================================
  // COMPUTED SIGNALS (valores derivados)
  // ===========================================================================
  
  /**
   * ¿El usuario está autenticado?
   * 
   * Computed recalcula automáticamente cuando accessToken cambia.
   * Si hay token → autenticado = true
   * Si no hay token → autenticado = false
   */
  public isAuthenticated = computed(() => !!this.accessToken());
  
  /**
   * ¿El usuario tiene rol de admin?
   * Útil para.directives ngIf de elementos admin-only.
   */
  public isAdmin = computed(() => this.userRoles().includes('ADMIN'));
  
  /**
   * Obtener el token actual.
   * Se usa en el interceptor para agregar el header.
   */
  public getToken = computed(() => this.accessToken());
  
  /**
   * Obtener los roles del usuario.
   */
  public getRoles = computed(() => this.userRoles());
  
  // ===========================================================================
  // TIMER PARA AUTO-REFRESH
  // ===========================================================================
  
  /**
   * Timer que programa el próximo refresh.
   * Se cancela cuando el usuario hace logout.
   */
  private refreshTimer: ReturnType<typeof setTimeout> | null = null;
  
  // ===========================================================================
  // LOGIN
  // ===========================================================================
  
  /**
   * Inicia sesión con email y password.
   * 
   * FLUJO:
   * 1. Envía credenciales al backend
   * 2. Backend valida y retorna Access Token
   * 3. Guardamos el token en memoria
   * 4. Extraemos roles del token
   * 5. Programamos auto-refresh
   * 
   * @param email    - Email del usuario
   * @param password - Contraseña (nunca se guarda)
   * @returns Observable con la respuesta del backend
   */
  login(email: string, password: string): Observable<{ accessToken: string; tokenType: string; expiresIn: number }> {
    const body = { email, password };
    
    return this.http.post<{ accessToken: string; tokenType: string; expiresIn: number }>(
      `${this.apiUrl}/login`,
      body,
      { 
        withCredentials: true  // IMPORTANTE: Recibir la cookie del refresh token
      }
    ).pipe(
      tap(response => {
        this.setAccessToken(response.accessToken, response.expiresIn);
        this.authStatus.set('authenticated');

        // Extraer roles del token y persistir todo
        const roles = this.extractRolesFromToken(response.accessToken);
        this.userRoles.set(roles);
        this.saveTokenToStorage(response.accessToken, roles);

        // Programar auto-refresh 1 minuto antes de que expire
        this.scheduleTokenRefresh(response.expiresIn);
      }),
      
      // =======================================================================
      // MANEJO DE ERRORES
      // =======================================================================
      catchError(error => {
        console.error('Login falló:', error);
        // Limpiamos cualquier estado previo
        this.clearAuthState();
        // Rethrow para que el componente maneje el error
        return throwError(() => error);
      })
    );
  }
  
  // ===========================================================================
  // REFRESH
  // ===========================================================================
  
  /**
   * Renueva el Access Token usando el Refresh Token.
   * 
   * ¿CUÁNDO SE USA?
   * 1. Automáticamente cuando faltan 1 minuto para expirar
   * 2. Manualmente si el backend retornó 401
   * 
   * FLUJO:
   * 1. Envía request a /auth/refresh (la cookie se envía automáticamente)
   * 2. Backend valida el refresh token en BD
   * 3. Backend retorna nuevo Access Token
   * 4. Actualizamos el token en memoria
   * 5. Re-programamos el timer de refresh
   * 
   * @returns Observable con el nuevo access token
   */
  refresh(): Observable<{ accessToken: string; tokenType: string; expiresIn: number }> {
    return this.http.post<{ accessToken: string; tokenType: string; expiresIn: number }>(
      `${this.apiUrl}/refresh`,
      {},  // Body vacío, solo la cookie se envía
      {
        // Esta request SÍ necesita withCredentials porque:
        // - Estamos enviando credenciales (la cookie)
        // - El backend tiene CORS configurado para permitir esto
        withCredentials: true
      }
    ).pipe(
      tap(response => {
        // Actualizar token en memoria Y localStorage
        this.setAccessToken(response.accessToken, response.expiresIn);
        
        // Actualizar roles
        const roles = this.extractRolesFromToken(response.accessToken);
        this.userRoles.set(roles);
        
        // GUARDAR EN LOCALSTORAGE TRAS REFRESH
        this.saveTokenToStorage(response.accessToken, roles);
        
        // Re-programar refresh
        this.scheduleTokenRefresh(response.expiresIn);
        
        console.log('Token renovado exitosamente');
      }),
      catchError(error => {
        console.error('Refresh falló:', error);
        // Si el refresh falla, la sesión expiró
        // Limpiamos todo y redirigimos a login
        this.clearAuthState();
        this.router.navigate(['/login']);
        return throwError(() => error);
      })
    );
  }
  
  // ===========================================================================
  // FORGOT PASSWORD
  // ===========================================================================

  /**
   * Solicita un enlace de restablecimiento de contraseña.
   *
   * @param email - Email del usuario que olvidó su contraseña
   * @returns Observable con mensaje de confirmación
   */
  forgotPassword(email: string): Observable<MessageResponse> {
    const body: ForgotPasswordRequest = { email };
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, body);
  }

  /**
   * Restablece la contraseña usando el token recibido por email.
   *
   * @param token - Token UUID recibido en el email
   * @param nuevaPassword - Nueva contraseña (mínimo 8 caracteres)
   * @returns Observable con mensaje de confirmación
   */
  resetPassword(token: string, nuevaPassword: string): Observable<MessageResponse> {
    const body: ResetPasswordRequest = { token, nuevaPassword };
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, body);
  }

  // ===========================================================================
  // LOGOUT
  // ===========================================================================
  
  /**
   * Cierra la sesión del usuario.
   * 
   * FLUJO:
   * 1. Envía request a /auth/logout (la cookie se envía automáticamente)
   * 2. Backend marca el refresh token como revocado
   * 3. Limpiamos el access token de memoria
   * 4. Cancelamos el timer de refresh
   * 5. Redirigimos a login
   */
  logout(): void {
    // =======================================================================
    // OPCIONAL: Notificar al backend primero
    // Si el backend no responde, igualmente limpiamos localmente
    // =======================================================================
    this.http.post(
      `${this.apiUrl}/logout`,
      {},
      { withCredentials: true }
    ).subscribe({
      next: () => console.log('Logout exitoso en backend'),
      error: () => console.log('Logout parcial (backend no respondió)')
    });
    
    // =======================================================================
    // Limpiar estado local SIEMPRE
    // =======================================================================
    this.clearAuthState();
    
    // Redirigir a login
    this.router.navigate(['/login']);
  }
  
  // ===========================================================================
  // MÉTODOS PRIVADOS DE SOPORTE
  // ===========================================================================
  
  /**
   * Guarda el access token y calcula cuándo expira.
   * 
   * @param token   - El JWT access token
   * @param expiresIn - Segundos hasta expiración (del backend)
   */
  private setAccessToken(token: string, expiresIn: number): void {
    this.accessToken.set(token);
    
    // Guardamos cuándo expira el token (timestamp absoluto)
    const expiryTime = Date.now() + (expiresIn * 1000);
    this.tokenExpiryMs.set(expiryTime);
  }
  
  /**
   * Programa el próximo refresh automáticamente.
   * 
   * @param expiresIn - Segundos hasta que expire el token actual
   */
  private scheduleTokenRefresh(expiresIn: number): void {
    // =======================================================================
    // Cancelar timer anterior si existe
    // (por si se llamó refresh manualmente antes de tiempo)
    // =======================================================================
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }
    
    // =======================================================================
    // Calcular cuándo hacer refresh
    // Refresh 1 minuto (60 seg) antes de que expire
    // Si expiresIn es 900 seg (15 min), refresh a los 840 seg (14 min)
    // =======================================================================
    const refreshInMs = Math.max((expiresIn - 60) * 1000, 0);
    
    console.log(`Próximo refresh programado en ${refreshInMs / 1000} segundos`);
    
    // Programar el timer
    this.refreshTimer = setTimeout(() => {
      console.log('Ejecutando auto-refresh...');
      this.refresh().subscribe();
    }, refreshInMs);
  }
  
  /**
   * Retorna la ruta de inicio según el rol del usuario.
   *
   * @returns ruta base para el home del usuario autenticado
   */
  public getHomeRouteByRole(): string {
    const roles = this.userRoles();
    if (roles.includes('ADMIN')) return '/app/dashboard';
    if (roles.includes('DOCENTE')) return '/app/docente/mis-cursos';
    if (roles.includes('ESTUDIANTE')) return '/app/estudiante/mis-cursos';
    return '/app/dashboard';
  }

  /**
   * Retorna el nombre completo del usuario (extraído del JWT).
   *
   * Combina los claims "nombre" y "apellido" incluidos en el payload.
   * Fallback a 'Usuario' si no se encuentra.
   */
  public getDisplayName(): string {
    const token = this.accessToken();
    if (!token) return 'Usuario';
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const claims = JSON.parse(decoded);
      if (claims.nombre && claims.apellido) {
        return `${claims.nombre} ${claims.apellido}`;
      }
      return claims.nombre || claims.email || 'Usuario';
    } catch {
      return 'Usuario';
    }
  }

  /**
   * Extrae los roles del JWT payload.
   * 
   * ¿POR QUÉ extraemos del token en lugar de guardar en señal?
   * - Los roles están en el JWT, no necesitamos otra llamada al backend
   * - Garantiza sincronización entre token y roles
   * - Evita bugs donde los roles cambian pero no se actualizan
   * 
   * @param token - El JWT (formato: header.payload.signature)
   * @returns Array de roles, ej: ['ADMIN', 'ESTUDIANTE']
   */
  private extractRolesFromToken(token: string): string[] {
    try {
      // El JWT tiene 3 partes separadas por '.'
      // [0] = header (metadata del algoritmo)
      // [1] = payload (los datos/claims)
      // [2] = signature (firma para verificar autenticidad)
      const payload = token.split('.')[1];
      
      // El payload está Base64-encoded, necesitamos decodificarlo
      // Usamos atob para decodificar Base64
      // Reemplazamos caracteres especiales de URL encoding
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      
      // Parsear el JSON para obtener los datos
      const claims = JSON.parse(decoded);
      
      // Retornar los roles (están en el claim 'roles')
      return claims.roles || [];
    } catch (error) {
      console.error('Error decodificando token:', error);
      return [];
    }
  }
  
  /**
   * Limpia todo el estado de autenticación.
   * Se llama en logout y cuando hay errores de auth.
   */
  private clearAuthState(): void {
    // Limpiar token
    this.accessToken.set(null);
    
    // Limpiar roles
    this.userRoles.set([]);
    
    // Limpiar expiry
    this.tokenExpiryMs.set(0);

    // Marcar como no autenticado
    this.authStatus.set('unauthenticated');
    
    // Cancelar timer
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }

    // Limpiar localStorage
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLES_KEY);
  }

  // ===========================================================================
  // MÉTODOS DE PERSISTENCIA (LOCALSTORAGE)
  // ===========================================================================

  /**
   * Carga el token desde localStorage al iniciar la app.
   */
  private loadTokenFromStorage(): string | null {
    try {
      const token = localStorage.getItem(this.TOKEN_KEY);
      const roles = localStorage.getItem(this.ROLES_KEY);
      
      if (token) {
        // Extraer expiry del token y verificar si no ha expirado
        const expiryMs = this.extractExpiryFromTokenAndGet(token);
        
        if (expiryMs && expiryMs < Date.now()) {
          // Token expirado, limpiar y retornar null
          console.log('Token expirado, limpiando...');
          localStorage.removeItem(this.TOKEN_KEY);
          localStorage.removeItem(this.ROLES_KEY);
          return null;
        }

        // Cargar roles también
        if (roles) {
          this.userRoles.set(JSON.parse(roles));
        }
        
        console.log('Token cargado desde localStorage');
        return token;
      }
    } catch (e) {
      console.error('Error cargando token desde localStorage:', e);
    }
    return null;
  }

  /**
   * Guarda el token en localStorage.
   */
  private saveTokenToStorage(token: string, roles: string[]): void {
    try {
      localStorage.setItem(this.TOKEN_KEY, token);
      localStorage.setItem(this.ROLES_KEY, JSON.stringify(roles));
      console.log('Token guardado en localStorage');
    } catch (e) {
      console.error('Error guardando token en localStorage:', e);
    }
  }

/**
    * Extrae el tiempo de expiry del token JWT y lo guarda.
    * @returns El timestamp de expiry o null si no existe
    */
  private extractExpiryFromTokenAndGet(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const claims = JSON.parse(decoded);
      
      if (claims.exp) {
        const expiryMs = claims.exp * 1000;
        this.tokenExpiryMs.set(expiryMs);
        return expiryMs;
      }
    } catch (e) {
      // Ignorar errores al decodificar
    }
    return null;
  }

  /**
    * Extrae el tiempo de expiry del token JWT (sin retorno, solo guarda).
    */
  private extractExpiryFromToken(token: string): void {
    this.extractExpiryFromTokenAndGet(token);
  }
  
  // ===========================================================================
  // MÉTODOS PÚBLICOS DE UTILIDAD
  // ===========================================================================
  
  /**
   * Fuerza un refresh manual.
   * Útil cuando el usuario hace una acción crítica y queremos
   * asegurar que tiene un token fresco.
   */
  public forceRefresh(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
    }
    this.refresh().subscribe();
  }
}