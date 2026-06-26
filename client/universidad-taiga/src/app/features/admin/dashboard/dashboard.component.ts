import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { TuiButton } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiSkeleton } from '@taiga-ui/kit';
import { DashboardService, DashboardStats } from '../../../core/services/dashboard.service';
import { RoleService } from '../../../core/services/role.service';

/**
 * Enlace rápido a un módulo de administración.
 */
interface QuickLink {
  /** Etiqueta visible */
  label: string;
  /** Ruta absoluta dentro de /app */
  route: string;
  /** Nombre del icono Tabler Icons */
  icon: string;
  /** Descripción breve */
  description: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterModule, NgIcon, TuiButton, TuiCardLarge, TuiHeader, TuiSkeleton],
  templateUrl: './dashboard.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página principal del panel de administración.
 *
 * Muestra:
 * - Saludo con el nombre del admin (desde el JWT)
 * - Tarjetas con totales de estudiantes, docentes, cursos y secciones
 * - Enlaces rápidos a cada módulo de gestión
 */
export class Dashboard {
  private readonly dashboardService = inject(DashboardService);
  private readonly roleService = inject(RoleService);

  /** Nombre completo del usuario autenticado */
  readonly userName = computed(() => this.roleService.getDisplayName());

  /** Estadísticas del sistema */
  readonly stats = signal<DashboardStats | null>(null);

  /** Indica si las estadísticas están cargando */
  readonly loading = signal(true);

  /** Mensaje de error si falla la carga */
  readonly error = signal<string | null>(null);

  /** Enlaces rápidos a los módulos de administración */
  readonly quickLinks: QuickLink[] = [
    { label: 'Estudiantes', route: '/app/estudiantes', icon: 'tablerSchool', description: 'Gestionar estudiantes' },
    { label: 'Docentes', route: '/app/docentes', icon: 'tablerUsers', description: 'Gestionar docentes' },
    { label: 'Cursos', route: '/app/cursos', icon: 'tablerBook', description: 'Gestionar cursos' },
    { label: 'Secciones', route: '/app/secciones', icon: 'tablerStackBack', description: 'Gestionar secciones' },
    { label: 'Horarios', route: '/app/horarios', icon: 'tablerClipboardList', description: 'Gestionar horarios' },
    { label: 'Evaluaciones', route: '/app/evaluaciones', icon: 'tablerClipboardCheck', description: 'Gestionar evaluaciones' },
  ];

  constructor() {
    this.loadStats();
  }

  /**
   * Carga las estadísticas del sistema en paralelo.
   * Maneja errores mostrando un mensaje con opción de reintentar.
   */
  async loadStats(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const data = await this.dashboardService.getStats();
      this.stats.set(data);
    } catch {
      this.error.set('Error al cargar las estadísticas');
    } finally {
      this.loading.set(false);
    }
  }
}
