import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { TuiAvatar } from '@taiga-ui/kit';

/**
 * Header de la aplicación.
 *
 * Muestra:
 * - Título dinámico según la ruta activa
 * - Avatar con iniciales del usuario
 * - Nombre, email y rol del usuario autenticado
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [TuiAvatar],
  templateUrl: './header.html',
  styleUrl: './header.less',
})
export class Header {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  /** Título de la página actual */
  readonly title = computed(() => {
    const url = this.router.url.split('?')[0];
    return this.getTitle(url);
  });

  /** Nombre del usuario autenticado */
  readonly userName = computed(() => this.authService.getDisplayName());

  /** Email del usuario (del token) */
  readonly userEmail = computed(() => {
    const token = this.authService.getToken();
    if (!token) return '';
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const claims = JSON.parse(decoded);
      return claims.email || claims.sub || '';
    } catch {
      return '';
    }
  });

  /** Iniciales del usuario para el avatar */
  readonly userInitials = computed(() => {
    const name = this.userName();
    if (!name || name === 'Usuario') return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name[0].toUpperCase();
  });

  /** Rol formateado para mostrar */
  readonly roleLabel = computed(() => {
    const roles = this.authService.getRoles();
    if (roles.includes('ADMIN')) return 'Administrador';
    if (roles.includes('DOCENTE')) return 'Docente';
    if (roles.includes('ESTUDIANTE')) return 'Estudiante';
    return '';
  });

  /**
   * Retorna el título según la ruta activa.
   *
   * @param url - URL actual
   * @returns título descriptivo de la página
   */
  private getTitle(url: string): string {
    const path = url.replace(/^\/(app\/)?/, '').split('/')[0];
    switch (path) {
      case '':
      case 'dashboard':
        return 'Dashboard';
      case 'estudiantes':
        return 'Estudiantes';
      case 'docentes':
        return 'Docentes';
      case 'cursos':
        return 'Cursos';
      case 'secciones':
        return 'Secciones';
      case 'horarios':
        return 'Horarios';
      case 'evaluaciones':
        return 'Evaluaciones';
      case 'perfil':
        return 'Mi Perfil';
      case 'docente':
        return 'Mis Cursos';
      case 'estudiante':
        return 'Mis Cursos';
      default:
        return '';
    }
  }
}
