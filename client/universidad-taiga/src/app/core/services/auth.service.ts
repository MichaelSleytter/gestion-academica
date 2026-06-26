import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { APP_API_URL } from '../tokens/api.tokens';
import { TokenService } from './token.service';
import { RoleService } from './role.service';
import { ForgotPasswordRequest, ResetPasswordRequest, MessageResponse } from '../../models/auth.model';

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

  constructor() {
    const storedToken = this.tokenService.loadFromStorage();
    if (storedToken) {
      const roles = this.tokenService.extractRolesFromToken(storedToken);
      this.roleService.setRoles(roles);
      // El token del localStorage es válido — el refreshSession() es un intento
      // silencioso de renovar el token. Si falla, no destruimos la sesión,
      // el token actual sigue sirviendo hasta que expire naturalmente.
    }
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
          this.tokenService.saveToStorage(response.accessToken);

          const roles = this.tokenService.extractRolesFromToken(response.accessToken);
          this.roleService.setRoles(roles);

          const refreshInMs = Math.max((response.expiresIn - 60) * 1000, 0);
          this.tokenService.scheduleRefresh(refreshInMs, () => this.forceRefresh());
          this.tokenService.authStatus.set('authenticated');
        }
        this.isVerifying = false;
      },
      error: () => {
        // Solo limpiamos la sesión si no hay token vigente. Si el token
        // almacenado sigue siendo válido (no expiró), el refresh fallido
        // no debe destruir la sesión — el token se usará hasta que expire
        // naturalmente y el interceptor maneje el 401 en ese momento.
        if (!this.tokenService.getToken()) {
          this.tokenService.clearToken();
          this.roleService.clearRoles();
        }
        this.isVerifying = false;
      },
    });
  }

  /** Valida el token con el backend usando la cookie de refresh. */
  private validateToken(): Observable<{ accessToken?: string; tokenType?: string; expiresIn?: number }> {
    return this.http
      .post<{ accessToken?: string; tokenType?: string; expiresIn?: number }>(
        `${this.apiUrl}/refresh`,
        {},
        { withCredentials: true },
      )
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
  login(email: string, password: string): Observable<{ accessToken: string; tokenType: string; expiresIn: number }> {
    return this.http
      .post<{ accessToken: string; tokenType: string; expiresIn: number }>(
        `${this.apiUrl}/login`,
        { email, password },
        { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.accessToken, response.expiresIn);
          this.tokenService.saveToStorage(response.accessToken);
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
    return this.http
      .post<{ accessToken: string; tokenType: string; expiresIn: number }>(
        `${this.apiUrl}/refresh`,
        {},
        { withCredentials: true },
      )
      .pipe(
        tap((response) => {
          this.tokenService.setToken(response.accessToken, response.expiresIn);
          this.tokenService.saveToStorage(response.accessToken);

          const roles = this.tokenService.extractRolesFromToken(response.accessToken);
          this.roleService.setRoles(roles);

          const refreshInMs = Math.max((response.expiresIn - 60) * 1000, 0);
          this.tokenService.scheduleRefresh(refreshInMs, () => this.forceRefresh());
        }),
        catchError((error) => {
          this.tokenService.clearToken();
          this.roleService.clearRoles();
          this.router.navigate(['/login']);
          return throwError(() => error);
        }),
      );
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
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, { token, nuevaPassword });
  }

  /**
   * Cierra la sesión del usuario.
   * Notifica al backend, limpia estado local y redirige a login.
   */
  logout(): void {
    this.http
      .post(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .subscribe({
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
    this.refresh().subscribe();
  }
}
