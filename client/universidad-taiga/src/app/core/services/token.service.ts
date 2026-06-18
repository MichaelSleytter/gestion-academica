import { Injectable, signal, computed } from '@angular/core';
import type { AuthStatus } from '../../models/auth.model';

/**
 * TokenService: ciclo de vida del access token JWT en memoria y localStorage.
 * Sin HttpClient para evitar dependencia circular con el interceptor.
 */
@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly ROLES_KEY = 'auth_roles';

  /** Access token en memoria. */
  private accessToken = signal<string | null>(null);

  /** Timestamp absoluto (ms) de expiración del token actual. */
  private tokenExpiryMs = signal<number>(0);

  /**
   * Estado de autenticación para guards.
   * 'loading' → verificando sesión | 'authenticated' | 'unauthenticated'.
   */
  public authStatus = signal<AuthStatus>('loading');

  public isAuthenticated = computed(() => !!this.accessToken());
  public getToken = computed(() => this.accessToken());

  private refreshTimer: ReturnType<typeof setTimeout> | null = null;

  /**
   * Guarda el token en memoria y calcula el timestamp de expiración.
   *
   * @param token - JWT access token
   * @param expiresIn - Segundos hasta expiración (del backend)
   */
  setToken(token: string, expiresIn: number): void {
    this.accessToken.set(token);
    this.tokenExpiryMs.set(Date.now() + expiresIn * 1000);
  }

  /**
   * Limpia token de memoria y localStorage, cancela refresh pendiente,
   * marca authStatus como 'unauthenticated'.
   */
  clearToken(): void {
    this.accessToken.set(null);
    this.tokenExpiryMs.set(0);
    this.authStatus.set('unauthenticated');
    this.cancelRefresh();
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLES_KEY);
  }

  /**
   * Carga el token desde localStorage verificando expiración.
   * Si expiró, lo limpia y retorna null.
   *
   * @returns El token si es válido, null si no hay o expiró
   */
  loadFromStorage(): string | null {
    try {
      const token = localStorage.getItem(this.TOKEN_KEY);
      if (!token) return null;

      const expiryMs = this.extractExpiryFromToken(token);
      if (expiryMs && expiryMs < Date.now()) {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.ROLES_KEY);
        return null;
      }

      this.accessToken.set(token);
      this.authStatus.set('authenticated');
      return token;
    } catch {
      return null;
    }
  }

  /** Persiste el token en localStorage para sobrevivir recargas. */
  saveToStorage(token: string): void {
    try {
      localStorage.setItem(this.TOKEN_KEY, token);
    } catch {
      // localStorage puede fallar si está lleno o deshabilitado
    }
  }

  /**
   * Programa un setTimeout para refrescar el token.
   * Cancela cualquier timer previo.
   *
   * @param delayMs - Milisegundos hasta ejecutar el callback
   * @param callback - Función a ejecutar (ej: AuthService.forceRefresh)
   */
  scheduleRefresh(delayMs: number, callback: () => void): void {
    this.cancelRefresh();
    this.refreshTimer = setTimeout(callback, delayMs);
  }

  /** Cancela el timer de refresh pendiente si existe. */
  cancelRefresh(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  /**
   * Extrae los roles del payload del JWT.
   *
   * @param token - JWT completo
   * @returns Array de roles o [] si no se puede decodificar
   */
  extractRolesFromToken(token: string): string[] {
    return this.decodeToken(token)?.['roles'] || [];
  }

  /**
   * Extrae el timestamp de expiración del JWT.
   *
   * @param token - JWT completo
   * @returns Timestamp en ms, o null si no se puede decodificar
   */
  extractExpiryFromToken(token: string): number | null {
    const claims = this.decodeToken(token);
    return claims?.['exp'] ? claims['exp'] * 1000 : null;
  }

  /**
   * Extrae el nombre + apellido del payload del JWT.
   *
   * @param token - JWT completo o null
   * @returns Nombre completo o 'Usuario' si no se puede determinar
   */
  extractDisplayName(token: string | null): string {
    if (!token) return 'Usuario';
    const claims = this.decodeToken(token);
    if (!claims) return 'Usuario';
    if (claims['nombre'] && claims['apellido']) {
      return `${claims['nombre']} ${claims['apellido']}`;
    }
    return claims['nombre'] || claims['email'] || 'Usuario';
  }

  /**
   * Decodifica el payload de un JWT sin verificar la firma.
   * Útil para extraer claims como roles, exp, nombre, etc.
   */
  private decodeToken(token: string): Record<string, any> | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }
}
