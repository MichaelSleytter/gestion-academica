import { Injectable, signal, computed } from '@angular/core';
import type { AuthStatus } from '../../models/auth.model';

/**
 * TokenService: ciclo de vida del access token JWT en memoria.
 * Sin HttpClient para evitar dependencia circular con el interceptor.
 *
 * El access token se mantiene exclusivamente en memoria (nunca en localStorage)
 * para prevenir ataques XSS. La persistencia de sesión se logra mediante la
 * cookie httpOnly del refresh token, que el frontend no puede leer.
 *
 * @security
 * - Access token: solo en memoria, nunca se persiste.
 * - Refresh token: en cookie httpOnly (backend), JS no puede leerlo.
 * - Al recargar la página, se llama a /auth/refresh con la cookie.
 */
@Injectable({
  providedIn: 'root',
})
export class TokenService {
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
   * Limpia el token de memoria, cancela refresh pendiente,
   * marca authStatus como 'unauthenticated'.
   */
  clearToken(): void {
    this.accessToken.set(null);
    this.tokenExpiryMs.set(0);
    this.authStatus.set('unauthenticated');
    this.cancelRefresh();
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
   * Extrae el identificador de usuario desde el claim `sub` del JWT actual.
   *
   * @returns ID de usuario autenticado, o null si el token no contiene un subject numérico.
   */
  extractCurrentUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    const subject = this.decodeToken(token)?.['sub'];
    const userId = Number(subject);
    return Number.isFinite(userId) && userId > 0 ? userId : null;
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
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      // atob() es Latin-1, no UTF-8. decodeURIComponent fuerza UTF-8 correcto.
      const decoded = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join(''),
      );
      return JSON.parse(decoded);
    } catch {
      return null;
    }
  }
}
