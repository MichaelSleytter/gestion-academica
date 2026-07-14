import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { type Observable, tap, catchError, throwError, finalize, shareReplay } from 'rxjs';
import { APP_API_URL } from '../tokens/api.tokens';
import { TokenService } from './token.service';
import { RoleService } from './role.service';
import type { MessageResponse } from '../../models/auth.model';

/**
 * AuthService: operaciones HTTP de autenticación.
 * Delega el estado del token a TokenService y los roles a RoleService.
 * No tiene state propio.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);
  private readonly roleService = inject(RoleService);

  private readonly apiBaseUrl = inject(APP_API_URL);
  private readonly apiUrl = `${this.apiBaseUrl}/auth`;

  private isVerifying = false;
  private refreshRequest$: Observable<{
    accessToken: string;
    tokenType: string;
    expiresIn: number;
  }> | null = null;

  constructor() {
    // Al iniciar, intentamos renovar la sesión mediante la cookie httpOnly.
    // No cargamos el access token de localStorage por seguridad (XSS).
    this.refreshSession();
  }

  /**
   * Intenta restaurar la sesión usando la cookie HttpOnly de refresh token.
   * Se ejecuta al iniciar la app. Si la cookie no existe o expiró,
   * el backend responde 401 y se limpia el estado.
   */
  private refreshSession(): void {
    if (this.isVerifying) return;
    this.isVerifying = true;

    this.validateToken().subscribe({
      next: (response) => {
        if (response.accessToken && response.expiresIn) {
          this.tokenService.setToken(response.accessToken, response.expiresIn);

          const roles = this.tokenService.extractRolesFromToken(response.accessToken);
          this.roleService.setRoles(roles);

          const refreshInMs = Math.max((response.expiresIn - 60) * 1000, 0);
          this.tokenService.scheduleRefresh(refreshInMs, () => this.forceRefresh());
          this.tokenService.authStatus.set('authenticated');
        }
        this.isVerifying = false;
      },
      error: () => {
        // Si el refresh falla, no hay sesión que restaurar.
        // El authStatus pasó de 'loading' a 'unauthenticated'.
        this.tokenService.clearToken();
        this.roleService.clearRoles();
        this.isVerifying = false;
      },
    });
  }

  /** Valida el token con el backend usando la cookie de refresh. */
  private validateToken(): Observable<{
    accessToken?: string;
    tokenType?: string;
    expiresIn?: number;
  }> {
    return this.http
      .post<{
        accessToken?: string;
        tokenType?: string;
        expiresIn?: number;
      }>(`${this.apiUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap(() => console.log('Sesión verificada exitosamente')),
        catchError((error) => {
          console.error('Error verificando sesión:', error);
          return throwError(() => error);
        }),
      );
  }

  /**
   * Inicia sesión con email y password.
   * Backend retorna access token + cookie con refresh token.
   *
   * @param email - Email del usuario
   * @param password - Contraseña
   * @returns Observable con access token del backend
   */
  login(
    email: string,
    password: string,
  ): Observable<{ accessToken: string; tokenType: string; expiresIn: number }> {
    return this.http
      .post<{
        accessToken: string;
        tokenType: string;
        expiresIn: number;
      }>(`${this.apiUrl}/login`, { email, password }, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.accessToken, response.expiresIn);
          this.tokenService.authStatus.set('authenticated');

          const roles = this.tokenService.extractRolesFromToken(response.accessToken);
          this.roleService.setRoles(roles);

          const refreshInMs = Math.max((response.expiresIn - 60) * 1000, 0);
          this.tokenService.scheduleRefresh(refreshInMs, () => this.forceRefresh());
        }),
        catchError((error) => {
          this.tokenService.clearToken();
          this.roleService.clearRoles();
          return throwError(() => error);
        }),
      );
  }

  /**
   * Renueva el access token usando el refresh token (cookie HttpOnly).
   *
   * @returns Observable con el nuevo access token
   */
  refresh(): Observable<{ accessToken: string; tokenType: string; expiresIn: number }> {
    if (this.refreshRequest$) return this.refreshRequest$;

    this.refreshRequest$ = this.http
      .post<{
        accessToken: string;
        tokenType: string;
        expiresIn: number;
      }>(`${this.apiUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.accessToken, response.expiresIn);

          const roles = this.tokenService.extractRolesFromToken(response.accessToken);
          this.roleService.setRoles(roles);

          const refreshInMs = Math.max((response.expiresIn - 60) * 1000, 0);
          this.tokenService.scheduleRefresh(refreshInMs, () => this.forceRefresh());
        }),
        catchError((error) => {
          this.tokenService.clearToken();
          this.roleService.clearRoles();
          // No navegar a login aquí — el interceptor HTTP ya maneja
          // el logout si una request real falla después del refresh.
          // Esto evita que un refresh fallido (por timeout de red,
          // por ejemplo) saque al usuario de la sesión abruptamente.
          return throwError(() => error);
        }),
        finalize(() => {
          this.refreshRequest$ = null;
        }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );

    return this.refreshRequest$;
  }

  /**
   * Solicita un enlace de restablecimiento de contraseña.
   *
   * @param email - Email del usuario
   * @returns Observable con mensaje de confirmación
   */
  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, { email });
  }

  /**
   * Restablece la contraseña usando el token recibido por email.
   *
   * @param token - Token UUID recibido en el email
   * @param nuevaPassword - Nueva contraseña
   * @returns Observable con mensaje de confirmación
   */
  resetPassword(token: string, nuevaPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, {
      token,
      nuevaPassword,
    });
  }

  /**
   * Cierra la sesión del usuario.
   * Notifica al backend, limpia estado local y redirige a login.
   */
  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => console.log('Logout exitoso en backend'),
      error: () => console.log('Logout parcial (backend no respondió)'),
    });

    this.tokenService.clearToken();
    this.roleService.clearRoles();
    this.router.navigate(['/login']);
  }

  /**
   * Fuerza un refresh manual. Útil cuando el usuario hace una acción
   * crítica y queremos asegurar un token fresco.
   */
  forceRefresh(): void {
    this.tokenService.cancelRefresh();
    this.refresh().subscribe({
      error: () => {
        // Refresh silencioso falló — el interceptor se encargará
        // de redirigir a login cuando una request real falle.
      },
    });
  }
}
