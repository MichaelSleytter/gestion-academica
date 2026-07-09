import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import type { HttpErrorResponse } from '@angular/common/http';

import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import {
  TuiButton,
  TuiDialog,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiLoader,
} from '@taiga-ui/core';
import { TuiPlatform } from '@taiga-ui/cdk';
import type { MatriculaResponse } from '../../../models/matricula';
import { EstudianteService } from '../../../core/services/estudiante.service';
import {
  useMatriculasBySeccionQuery,
  useMatricularMutation,
  useRetirarMutation,
} from '../../../queries/matricula.query';

/**
 * Vista de gestión de matrículas de una sección.
 *
 * Permite al ADMIN visualizar estudiantes matriculados,
 * matricular nuevos estudiantes y retirar matrículas activas.
 *
 * Accesible para: ADMIN
 * Ruta: /app/secciones/:id/matriculas
 */
@Component({
  selector: 'app-matriculas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterModule, TuiButton, TuiDialog, TuiLoader, TuiPlatform, TuiInput, TuiTextfield],
  templateUrl: './matriculas.html',
  styles: ``,
})
export class Matriculas {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly notifications = inject(TuiNotificationService);
  private readonly estudianteService = inject(EstudianteService);

  /** ID de la sección desde la ruta. */
  readonly idSeccion = toSignal(
    this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
    { initialValue: 0 },
  );

  /** Columnas de la tabla de matriculados. */
  readonly columns = ['codigo', 'nombre', 'apellido', 'email', 'estado', 'acciones'] as const;

  /** Query de matrículas por sección. */
  readonly matriculasQuery = useMatriculasBySeccionQuery(this.idSeccion);

  /** Cantidad de matrículas activas (para el indicador de vacantes). */
  readonly ocupados = computed(() => {
    const data = this.matriculasQuery.data();
    if (!data) return 0;
    return data.filter((m) => m.estado === 'ACTIVA').length;
  });

  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.matriculasQuery.isPending());

  // ─── Modales ───────────────────────────────────────────────────────────

  /** Control de apertura del modal de matrícula. */
  readonly matricularModalAbierto = signal(false);

  /** Control de apertura del modal de retiro. */
  readonly retirarModalAbierto = signal(false);

  /** Matrícula seleccionada para retirar. */
  readonly matriculaSeleccionada = signal<MatriculaResponse | null>(null);

  // ─── Búsqueda de estudiantes ───────────────────────────────────────────

  /** Texto de búsqueda de estudiantes. */
  readonly busquedaEstudiante = signal('');
  /** Resultados de la búsqueda de estudiantes. */
  readonly estudiantesResultados = signal<EstudianteItem[]>([]);
  /** Estudiante seleccionado en el modal. */
  readonly estudianteSeleccionado = signal<number | null>(null);
  /** Indica si está cargando la búsqueda. */
  readonly buscandoEstudiantes = signal(false);
  /** Timer para debounce de búsqueda. */
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  // ─── Mutaciones ────────────────────────────────────────────────────────

  readonly matricularMutation = useMatricularMutation();
  readonly retirarMutation = useRetirarMutation();

  /**
   * Maneja el cambio en el input de búsqueda con debounce de 300ms.
   */
  onBusquedaEstudianteChange(texto: string): void {
    this.estudianteSeleccionado.set(null);
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => {
      const query = texto.trim();
      this.busquedaEstudiante.set(query);
      if (query.length < 2) {
        this.estudiantesResultados.set([]);
        return;
      }
      this.buscarEstudiantes(query);
    }, 300);
  }

  private buscarEstudiantes(query: string): void {
    this.buscandoEstudiantes.set(true);
    this.estudianteService
      .getEstudiantesPaginado(0, 20, query)
      .then((page) => {
        this.estudiantesResultados.set(
          page.content.map((e) => ({
            id: e.idUsuario,
            codigo: e.codigoEstudiante,
            nombre: e.nombre,
            apellido: e.apellido,
            email: e.email,
          })),
        );
      })
      .catch(() => {
        this.estudiantesResultados.set([]);
      })
      .finally(() => {
        this.buscandoEstudiantes.set(false);
      });
  }

  // ─── Acciones ──────────────────────────────────────────────────────────

  /** Abre el modal de matrícula. */
  openMatricularModal(): void {
    this.busquedaEstudiante.set('');
    this.estudiantesResultados.set([]);
    this.estudianteSeleccionado.set(null);
    this.matricularModalAbierto.set(true);
  }

  /** Cierra el modal de matrícula. */
  closeMatricularModal(): void {
    this.matricularModalAbierto.set(false);
    this.busquedaEstudiante.set('');
    this.estudiantesResultados.set([]);
    this.estudianteSeleccionado.set(null);
  }

  /** Confirma la matrícula del estudiante seleccionado. */
  confirmarMatricular(observer: { complete(): void }): void {
    const idEstudiante = this.estudianteSeleccionado();
    const idSeccion = this.idSeccion();
    if (!idEstudiante || !idSeccion) return;

    this.matricularMutation.mutate(
      { idEstudiante, idSeccion },
      {
        onSuccess: () => {
          this.notifications
            .open('Estudiante matriculado exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeMatricularModal();
        },
        onError: (error: unknown) => {
          const httpError = error as HttpErrorResponse;
          this.notifications
            .open(
              httpError.error?.message ?? httpError.message ?? 'Error al matricular estudiante',
              {
                label: 'Error',
                appearance: 'error',
                autoClose: 5000,
              },
            )
            .subscribe();
        },
      },
    );
  }

  /** Abre el modal de confirmación de retiro. */
  openRetirarModal(matricula: MatriculaResponse): void {
    this.matriculaSeleccionada.set(matricula);
    this.retirarModalAbierto.set(true);
  }

  /** Cierra el modal de retiro. */
  closeRetirarModal(): void {
    this.retirarModalAbierto.set(false);
    this.matriculaSeleccionada.set(null);
  }

  /** Confirma el retiro de la matrícula. */
  confirmarRetirar(observer: { complete(): void }): void {
    const matricula = this.matriculaSeleccionada();
    if (!matricula) return;

    this.retirarMutation.mutate(matricula.idMatricula, {
      onSuccess: () => {
        this.notifications
          .open('Matrícula retirada exitosamente', {
            label: 'Éxito',
            appearance: 'success',
            autoClose: 3000,
          })
          .subscribe();
        observer.complete();
        this.closeRetirarModal();
      },
      onError: (error: unknown) => {
        const httpError = error as HttpErrorResponse;
        this.notifications
          .open(httpError.error?.message ?? httpError.message ?? 'Error al retirar matrícula', {
            label: 'Error',
            appearance: 'error',
            autoClose: 5000,
          })
          .subscribe();
      },
    });
  }

  /** Indica si está procesando alguna operación. */
  isProcessing(): boolean {
    return this.matricularMutation.isPending() || this.retirarMutation.isPending();
  }

  /** Navega de vuelta a la lista de secciones. */
  goBack(): void {
    void this.router.navigate(['/app/secciones']);
  }

  /** Retorna la clase de badge según el estado. */
  estadoBadgeClass(estado: string): string {
    switch (estado) {
      case 'ACTIVA':
        return 'bg-success text-on-success';
      case 'RETIRADA':
        return 'bg-surface-hover text-text-secondary';
      case 'APROBADA':
        return 'bg-info text-on-info';
      case 'DESAPROBADA':
        return 'bg-error text-on-error';
      default:
        return 'bg-surface-hover text-text-secondary';
    }
  }
}

/** Item de estudiante para la lista de resultados. */
interface EstudianteItem {
  id: number;
  codigo: string;
  nombre: string;
  apellido: string;
  email: string;
}
