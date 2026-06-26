import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDialog,
  TuiIcon,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiPlatform } from '@taiga-ui/cdk';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiSkeleton } from '@taiga-ui/kit';
import { EvaluacionResponse } from '../../../models/evaluacion/evaluacion.response';
import { EvaluacionCreateRequest } from '../../../models/evaluacion/evaluacion.request';
import {
  useEvaluacionesPaginadosQuery,
  useCrearEvaluacionMutation,
  useActualizarEvaluacionMutation,
  useEliminarEvaluacionMutation,
} from '../../../queries/evaluacion.query';
import { EvaluacionService } from '../../../core/services/evaluacion.service';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-evaluaciones',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiCardLarge,
    TuiDialog,
    TuiHeader,
    TuiInput,
    TuiPlatform,
    TuiTextfield,
    TuiTable,
    TuiTitle,
    TuiIcon,
    TuiSkeleton,
  ],
  templateUrl: './evaluaciones.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de evaluaciones académicas.
 * Lista, crea, edita y elimina evaluaciones con modal de formulario y confirmación.
 * Cada evaluación define nombre y porcentaje para una sección.
 *
 * Accesible para: ADMIN, DOCENTE
 */
export class Evaluaciones {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);
  private readonly evaluacionService = inject(EvaluacionService);

  /** Columnas de la tabla de evaluaciones. */
  readonly columns = ['nombre', 'porcentaje', 'seccion', 'acciones'] as const;

  // ─── Paginación y búsqueda ───────────────────────────────────────────

  /** Página actual (0-based). */
  readonly pagina = signal(0);
  /** Elementos por página. */
  readonly tamaño = signal(10);
  /** Texto de búsqueda actual. */
  readonly busqueda = signal('');
  /** Timer para debounce del search. */
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  /** Query paginada que se refresca al cambiar página, tamaño o búsqueda. */
  readonly evaluacionesQuery = useEvaluacionesPaginadosQuery(this.pagina, this.tamaño, this.busqueda);

  /** Total de elementos (de la respuesta paginada). */
  readonly totalEvaluaciones = computed(() => this.evaluacionesQuery.data()?.totalElements ?? 0);
  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.evaluacionesQuery.data()?.totalPages ?? 0);
  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.evaluacionesQuery.isPending());

  // ─── Listas para dropdowns ───────────────────────────────────────────

  /** Lista de secciones para el select del formulario. */
  readonly seccionesList = signal<EvaluacionResponse['seccion'][]>([]);
  /** Indica si las listas de catálogo están cargando. */
  readonly catalogosLoading = signal(true);
  /** Error al cargar catálogos. */
  readonly catalogosError = signal<string | null>(null);

  constructor() {
    this.cargarCatalogos();
  }

  private cargarCatalogos(): void {
    this.catalogosLoading.set(true);
    this.catalogosError.set(null);

    this.evaluacionService
      .getSeccionesList()
      .then((page) => {
        this.seccionesList.set(page.content);
        this.catalogosLoading.set(false);
      })
      .catch(() => {
        this.catalogosError.set('Error al cargar datos del formulario');
        this.catalogosLoading.set(false);
      });
  }

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Controla la apertura/cierre del modal de formulario (crear/editar). */
  readonly evaluacionModalAbierto = signal(false);
  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);
  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');
  /** Evaluación seleccionada para edición o eliminación. */
  readonly evaluacionSeleccionada = signal<EvaluacionResponse | null>(null);

  // ─── Formulario ──────────────────────────────────────────────────────

  readonly evaluacionForm = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    porcentaje: this.formBuilder.nonNullable.control<number>(10, [Validators.required, Validators.min(0.1), Validators.max(100)]),
    idSeccion: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
  });

  // ─── Mutaciones ──────────────────────────────────────────────────────

  readonly crearEvaluacionMutation = useCrearEvaluacionMutation();
  readonly actualizarEvaluacionMutation = useActualizarEvaluacionMutation();
  readonly eliminarEvaluacionMutation = useEliminarEvaluacionMutation();

  // ─── Paginación ──────────────────────────────────────────────────────

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.evaluacionesQuery.data();
    if (!data || data.totalElements === 0) return '0 evaluaciones';
    const desde = data.number * data.size + 1;
    const hasta = Math.min((data.number + 1) * data.size, data.totalElements);
    return `${desde}–${hasta} de ${data.totalElements}`;
  });

  /** Array de números de página para iterar en el template. */
  readonly paginasArray = computed(() => Array.from({ length: this.totalPaginas() }, (_, i) => i));

  protected readonly String = String;

  // ─── Búsqueda con debounce ───────────────────────────────────────────

  /**
   * Maneja el cambio en el input de búsqueda con debounce de 300ms.
   * Al escribir, vuelve a la página 0 para mostrar resultados desde el inicio.
   */
  onBusquedaChange(texto: string): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => {
      this.busqueda.set(texto.trim());
      this.pagina.set(0);
    }, 300);
  }

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Abre modal de creación con formulario limpio. */
  openNuevaEvaluacionModal(): void {
    this.modoFormulario.set('crear');
    this.evaluacionSeleccionada.set(null);
    this.resetFormulario();
    this.evaluacionModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo de la evaluación. */
  openEditarEvaluacionModal(evaluacion: EvaluacionResponse): void {
    this.modoFormulario.set('editar');
    this.evaluacionSeleccionada.set(evaluacion);
    this.cargarFormulario(evaluacion);
    this.evaluacionModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeEvaluacionModal(): void {
    this.evaluacionModalAbierto.set(false);
    this.evaluacionSeleccionada.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarEvaluacionModal(evaluacion: EvaluacionResponse): void {
    this.evaluacionSeleccionada.set(evaluacion);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.evaluacionSeleccionada.set(null);
  }

  // ─── Guardar (crear/editar) ──────────────────────────────────────────

  guardarEvaluacion(observer: DialogObserver): void {
    if (this.evaluacionForm.invalid) {
      this.evaluacionForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();
    if (!payload) return;

    const raw = this.evaluacionForm.getRawValue();
    const idSeccion = raw.idSeccion!;

    if (this.modoFormulario() === 'crear') {
      this.crearEvaluacionMutation.mutate(
        { evaluacion: payload, idSeccion },
        {
          onSuccess: () => {
            this.notifications
              .open('Evaluación creada exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
              .subscribe();
            observer.complete();
            this.closeEvaluacionModal();
          },
          onError: (error) => {
            this.notifications
              .open(error?.message ?? 'Error al crear evaluación', { label: 'Error', appearance: 'error', autoClose: 5000 })
              .subscribe();
          },
        },
      );
      return;
    }

    const seleccionado = this.evaluacionSeleccionada();
    if (!seleccionado) return;

    this.actualizarEvaluacionMutation.mutate(
      { id: seleccionado.idEvaluacion, evaluacion: payload, idSeccion },
      {
        onSuccess: () => {
          this.notifications
            .open('Evaluación actualizada exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
          observer.complete();
          this.closeEvaluacionModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar evaluación', { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  // ─── Eliminar ────────────────────────────────────────────────────────

  confirmarEliminar(observer: DialogObserver): void {
    const seleccionado = this.evaluacionSeleccionada();
    if (!seleccionado) return;

    this.eliminarEvaluacionMutation.mutate(seleccionado.idEvaluacion, {
      onSuccess: () => {
        this.notifications
          .open('Evaluación eliminada exitosamente', { label: 'Eliminado', appearance: 'success', autoClose: 3000 })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar evaluación', { label: 'Error', appearance: 'error', autoClose: 5000 })
          .subscribe();
      },
    });
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  isGuardando(): boolean {
    return this.crearEvaluacionMutation.isPending() || this.actualizarEvaluacionMutation.isPending();
  }

  isEliminando(): boolean {
    return this.eliminarEvaluacionMutation.isPending();
  }

  // ─── Navegación de páginas ───────────────────────────────────────────

  paginaAnterior(): void {
    if (this.pagina() > 0) this.pagina.update(p => p - 1);
  }

  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) this.pagina.update(p => p + 1);
  }

  // ─── Métodos privados ────────────────────────────────────────────────

  private resetFormulario(): void {
    this.evaluacionForm.reset({
      nombre: '',
      porcentaje: 10,
      idSeccion: null,
    });
    this.evaluacionForm.markAsPristine();
    this.evaluacionForm.markAsUntouched();
  }

  private cargarFormulario(evaluacion: EvaluacionResponse): void {
    this.evaluacionForm.reset({
      nombre: evaluacion.nombre,
      porcentaje: evaluacion.porcentaje,
      idSeccion: evaluacion.seccion.idSeccion,
    });
    this.evaluacionForm.markAsPristine();
    this.evaluacionForm.markAsUntouched();
  }

  private construirPayload(): EvaluacionCreateRequest | null {
    const value = this.evaluacionForm.getRawValue();
    return {
      nombre: value.nombre.trim(),
      porcentaje: value.porcentaje,
    };
  }
}
