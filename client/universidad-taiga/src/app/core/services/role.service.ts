import { Injectable, inject, signal, computed } from '@angular/core';
import { TokenService } from './token.service';

/**
 * RoleService: roles del usuario autenticado y navegación por rol.
 * Sin HttpClient. Depende de TokenService solo para decodificar el JWT.
 */
@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly tokenService = inject(TokenService);

  private userRoles = signal<string[]>([]);

  public getRoles = computed(() => this.userRoles());
  public isAdmin = computed(() => this.userRoles().includes('ADMIN'));

  /**
   * Establece los roles del usuario actual.
   * Se llama desde AuthService después de login/refresh.
   *
   * @param roles - Array de nombres de rol (ADMIN, DOCENTE, ESTUDIANTE)
   */
  setRoles(roles: string[]): void {
    this.userRoles.set(roles);
  }

  /** Limpia los roles (logout / error de autenticación). */
  clearRoles(): void {
    this.userRoles.set([]);
  }

  /**
   * Retorna la ruta de inicio según el rol del usuario.
   *
   * @returns Ruta base para el home del usuario autenticado
   */
  getHomeRouteByRole(): string {
    const roles = this.userRoles();
    if (roles.includes('ADMIN')) return '/app/dashboard';
    if (roles.includes('DOCENTE')) return '/app/docente/mis-cursos';
    if (roles.includes('ESTUDIANTE')) return '/app/estudiante/mis-cursos';
    return '/app/dashboard';
  }

  /**
   * Retorna el nombre completo del usuario extraído del JWT.
   *
   * @returns Nombre completo o 'Usuario' si no hay token
   */
  getDisplayName(): string {
    return this.tokenService.extractDisplayName(this.tokenService.getToken());
  }
}
