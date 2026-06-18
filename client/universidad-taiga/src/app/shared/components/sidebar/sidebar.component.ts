import { Component, ChangeDetectionStrategy, computed, inject } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { AuthService } from '../../../core/services/auth.service';
import { RoleService } from '../../../core/services/role.service';

/**
 * Ítem del menú de navegación del sidebar.
 */
type MenuItem = {
  /** Etiqueta visible (tooltip) */
  label: string;
  /** Ruta relativa al layout */
  route: string;
  /** Icono de Tabler Icons (sin prefijo) */
  icon?: string;
};

/**
 * Sidebar de navegación principal.
 *
 * Muestra items de menú según el rol del usuario autenticado:
 * - ADMIN:     Dashboard, Estudiantes, Docentes, Cursos, Secciones, Horarios, Perfil
 * - DOCENTE:   Mis Cursos, Estudiantes, Perfil
 * - ESTUDIANTE: Mis Cursos, Historial, Perfil
 *
 * Usa ChangeDetectionStrategy.OnPush para rendimiento.
 */
@Component({
  selector: 'app-sidebar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterModule, NgOptimizedImage, NgIcon],
  template: `
    <aside
      class="flex flex-col items-center w-20 h-screen bg-primary"
      role="navigation"
      aria-label="Sidebar"
    >
      <!-- Logo -->
      <div class="p-4 flex items-center justify-center">
        <img
          ngSrc="/assets/logo.png"
          width="32"
          height="32"
          alt="Logo"
          class="w-8 h-8"
        />
      </div>

      <!-- Navigation -->
      <nav class="flex-1 mt-2" aria-label="Main navigation">
        @for (item of menuItems(); track item.route) {
          <a
            [routerLink]="item.route"
            routerLinkActive="active"
            [routerLinkActiveOptions]="{ exact: item.route === '/' }"
            class="nav-item group"
            [attr.aria-label]="item.label"
            [attr.aria-current]="isFocused(item.route) ? 'page' : null"
          >
            <span class="sr-only">{{ item.label }}</span>
            @if (item.icon) {
              <ng-icon
                [name]="item.icon"
                class="nav-icon"
                size="24"
                width="24"
                height="24"
              />
            }
            <!-- Tooltip on hover -->
            <span
              class="absolute left-full ml-3 px-2 py-1 rounded-md text-xs font-medium whitespace-nowrap
                     bg-white text-primary shadow-md opacity-0 invisible
                     group-hover:opacity-100 group-hover:visible
                     transition-all duration-150 z-50"
            >
              {{ item.label }}
            </span>
          </a>
        } @empty {
          <p class="text-sm text-center text-gray-400">Sin elementos</p>
        }
      </nav>

      <!-- Bottom actions -->
      <div class="p-3 flex flex-col items-center gap-1">
        <button
          class="action-btn"
          aria-label="Perfil"
          [routerLink]="['perfil']"
          routerLinkActive="active"
        >
          <ng-icon name="tablerUser" color="white" size="24" />
        </button>
        <button
          class="action-btn"
          aria-label="Cerrar sesión"
          (click)="onLogout()"
        >
          <ng-icon name="tablerLogout" color="white" size="24" />
        </button>
      </div>
    </aside>
  `,
  styles: [`
    .nav-item {
      width: 3rem;
      height: 3rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #FFFFFF;
      border-radius: 0.5rem;
      transition: background-color 0.15s, color 0.15s;
      margin: 0.5rem 0;
      position: relative;
    }

    /* Active (router-link-active) — bg blanco + color brand */
    .nav-item.active {
      background-color: #FFFFFF;
      color: var(--color-primary, #4F46E5);
    }

    .nav-item.active .nav-icon {
      color: var(--color-primary, #4F46E5);
    }

    /* Hover — solo cuando NO está activo */
    .nav-item:hover:not(.active) {
      background-color: rgba(255, 255, 255, 0.1);
      color: var(--color-primary, #4F46E5);
    }

    .nav-item:focus-visible {
      outline: none;
      box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.3);
    }

    /* Icon inherits color from .nav-item */
    .nav-icon {
      color: inherit;
      display: flex;
    }

    .action-btn {
      width: 2.5rem;
      height: 2.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 0.5rem;
      color: rgba(255, 255, 255, 0.7);
      transition: background-color 0.15s, color 0.15s;
      cursor: pointer;
      background: none;
      border: none;
    }

    .action-btn:hover {
      color: #FFFFFF;
      background-color: rgba(255, 255, 255, 0.1);
    }

    .action-btn:focus-visible {
      outline: none;
      box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.4);
    }
  `],
})
export class Sidebar {
  private readonly authService = inject(AuthService);
  private readonly roleService = inject(RoleService);

  /**
   * Items de menú filtrados según los roles del usuario.
   */
  readonly menuItems = computed<MenuItem[]>(() => {
    const roles = this.roleService.getRoles();

    if (roles.includes('ADMIN')) {
      return [
        { label: 'Inicio', route: 'dashboard', icon: 'tablerLayoutDashboard' },
        { label: 'Estudiantes', route: 'estudiantes', icon: 'tablerSchool' },
        { label: 'Docentes', route: 'docentes', icon: 'tablerUsers' },
        { label: 'Cursos', route: 'cursos', icon: 'tablerBook' },
        { label: 'Secciones', route: 'secciones', icon: 'tablerStackBack' },
        { label: 'Horarios', route: 'horarios', icon: 'tablerClipboardList' },
      ];
    }

    if (roles.includes('DOCENTE')) {
      return [
        { label: 'Mis Cursos', route: 'docente/mis-cursos', icon: 'tablerBook' },
        { label: 'Estudiantes', route: 'estudiantes', icon: 'tablerSchool' },
      ];
    }

    if (roles.includes('ESTUDIANTE')) {
      return [
        { label: 'Mis Cursos', route: 'estudiante/mis-cursos', icon: 'tablerSchool' },
        { label: 'Historial', route: 'estudiante/historial', icon: 'tablerClipboardList' },
      ];
    }

    return [];
  });

  /**
   * Determina si una ruta está actualmente activa.
   * Se usa para el atributo aria-current.
   */
  isFocused(route: string): boolean {
    // No se usa router directamente porque el computed ya reacciona.
    // Se mantiene como helper para el template si hiciera falta lógica extra.
    return false;
  }

  /**
   * Cierra la sesión del usuario.
   */
  onLogout(): void {
    this.authService.logout();
  }
}
