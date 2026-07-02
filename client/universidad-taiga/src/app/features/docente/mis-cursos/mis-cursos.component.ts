import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TuiButton, TuiIcon, TuiLoader } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TokenService } from '../../../core/services/token.service';
import { useDocenteSeccionesQuery } from '../../../queries/docente-role.query';

/**
 * Vista principal del Docente.
 *
 * Muestra las secciones (cursos) asignadas al docente autenticado.
 * Desde aquí puede acceder a la carga de notas de cada sección.
 *
 * Es el home del rol DOCENTE (redirección post-login).
 *
 * Accesible para: DOCENTE
 */
@Component({
  selector: 'app-mis-cursos',
  imports: [TuiButton, TuiCardLarge, TuiHeader, TuiIcon, TuiLoader],
  templateUrl: './mis-cursos.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisCursos {
  private readonly router = inject(Router);
  private readonly tokenService = inject(TokenService);

  /** ID del docente autenticado, extraído del subject del JWT. */
  readonly docenteId = signal(this.tokenService.extractCurrentUserId());

  /** Query con las secciones asignadas al docente autenticado. */
  readonly seccionesQuery = useDocenteSeccionesQuery(this.docenteId);

  /** Secciones asignadas listas para mostrar. */
  readonly secciones = computed(() => this.seccionesQuery.data() ?? []);

  /** Indica si la vista está esperando datos del backend. */
  readonly isLoading = computed(() => this.seccionesQuery.isPending());

  /** Cantidad total de secciones asignadas. */
  readonly totalSecciones = computed(() => this.secciones().length);

  /**
   * Navega a la pantalla de carga de notas para una sección.
   *
   * @param idSeccion - ID de la sección seleccionada.
   */
  navigateToNotas(idSeccion: number): void {
    void this.router.navigate(['/app/docente/mis-cursos', idSeccion, 'notas']);
  }

  navigateToEstudiantes(idSeccion: number): void {
    void this.router.navigate(['/app/docente/mis-cursos', idSeccion, 'estudiantes']);
  }
}
