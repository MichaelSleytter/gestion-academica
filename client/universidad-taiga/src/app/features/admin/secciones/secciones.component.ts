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
import { TuiSkeleton, TuiSelect } from '@taiga-ui/kit';
import { SeccionResponse } from '../../../models/seccion/seccion.response';
import { SeccionCreateRequest } from '../../../models/seccion/seccion.request';
import {
  useSeccionesPaginadosQuery,
  useCrearSeccionMutation,
  useActualizarSeccionMutation,
  useEliminarSeccionMutation,
} from '../../../queries/seccion.query';
import { SeccionService, CicloAcademicoResponse } from '../../../core/services/seccion.service';
import { CursoResponse } from '../../../models/curso/curso.response';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-secciones',
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
    TuiSelect,
  ],
  templateUrl: './secciones.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de secciones académicas.
 * Lista, crea, edita y elimina secciones con modal de formulario y confirmación.
 * Cada sección agrupa un curso dentro de un ciclo académico.
 *
 * Accesible para: ADMIN, DOCENTE
 */
export class Secciones {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);
  private readonly seccionService = inject(SeccionService);

  /** Columnas de la tabla de secciones. */
  readonly columns = ['codigo', 'curso', 'ciclo', 'vacantes', 'acciones'] as const;

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
  readonly seccionesQuery = useSeccionesPaginadosQuery(this.pagina, this.tamaño, this.busqueda);

  /** Total de elementos (de la respuesta paginada). */
  readonly totalSecciones = computed(() => this.seccionesQuery.data()?.totalElements ?? 0);
  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.seccionesQuery.data()?.totalPages ?? 0);
  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.seccionesQuery.isPending());

  // ─── Listas para dropdowns ───────────────────────────────────────────

  /** Lista de cursos para el select del formulario. */
  readonly cursosList = signal<CursoResponse[]>([]);
  /** Lista de ciclos académicos para el select del formulario. */
  readonly ciclosList = signal<CicloAcademicoResponse[]>([]);
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

    Promise.all([
      this.seccionService.getCursosList(),
      this.seccionService.getCiclosAcademicosList(),
    ])
      .then(([cursosPage, ciclos]) => {
        this.cursosList.set(cursosPage.content);
        this.ciclosList.set(ciclos);
        this.catalogosLoading.set(false);
      })
      .catch((err) => {
        this.catalogosError.set('Error al cargar datos del formulario');
        this.catalogosLoading.set(false);
      });
  }

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Controla la apertura/cierre del modal de formulario (crear/editar). */
  readonly seccionModalAbierto = signal(false);
  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);
  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');
  /** Sección seleccionada para edición o eliminación. */
  readonly seccionSeleccionada = signal<SeccionResponse | null>(null);

  // ─── Formulario ──────────────────────────────────────────────────────

  readonly seccionForm = this.formBuilder.group({
    codigoSeccion: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
    ]),
    vacantes: this.formBuilder.nonNullable.control(1, [
      Validators.required,
      Validators.min(1),
    ]),
    cicloAcademicoNombre: this.formBuilder.nonNullable.control('', [Validators.required]),
    idCurso: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
    idCiclo: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
  });

  // ─── Mutaciones ──────────────────────────────────────────────────────

  readonly crearSeccionMutation = useCrearSeccionMutation();
  readonly actualizarSeccionMutation = useActualizarSeccionMutation();
  readonly eliminarSeccionMutation = useEliminarSeccionMutation();

  // ─── Paginación ──────────────────────────────────────────────────────

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.seccionesQuery.data();
    if (!data || data.totalElements === 0) return '0 secciones';
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
  openNuevaSeccionModal(): void {
    this.modoFormulario.set('crear');
    this.seccionSeleccionada.set(null);
    this.resetFormulario();
    this.seccionModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo de la sección. */
  openEditarSeccionModal(seccion: SeccionResponse): void {
    this.modoFormulario.set('editar');
    this.seccionSeleccionada.set(seccion);
    this.cargarFormulario(seccion);
    this.seccionModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeSeccionModal(): void {
    this.seccionModalAbierto.set(false);
    this.seccionSeleccionada.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarSeccionModal(seccion: SeccionResponse): void {
    this.seccionSeleccionada.set(seccion);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.seccionSeleccionada.set(null);
  }

  // ─── Guardar (crear/editar) ──────────────────────────────────────────

  guardarSeccion(observer: DialogObserver): void {
    if (this.seccionForm.invalid) {
      this.seccionForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();
    if (!payload) return;

    const raw = this.seccionForm.getRawValue();
    const idCurso = raw.idCurso!;
    const idCiclo = raw.idCiclo!;

    if (this.modoFormulario() === 'crear') {
      this.crearSeccionMutation.mutate(
        { seccion: payload, idCurso, idCiclo },
        {
          onSuccess: () => {
            this.notifications
              .open('Sección creada exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
              .subscribe();
            observer.complete();
            this.closeSeccionModal();
          },
          onError: (error) => {
            this.notifications
              .open(error?.message ?? 'Error al crear sección', { label: 'Error', appearance: 'error', autoClose: 5000 })
              .subscribe();
          },
        },
      );
      return;
    }

    const seleccionada = this.seccionSeleccionada();
    if (!seleccionada) return;

    this.actualizarSeccionMutation.mutate(
      { id: seleccionada.idSeccion, seccion: payload, idCurso, idCiclo },
      {
        onSuccess: () => {
          this.notifications
            .open('Sección actualizada exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
          observer.complete();
          this.closeSeccionModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar sección', { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  // ─── Eliminar ────────────────────────────────────────────────────────

  confirmarEliminar(observer: DialogObserver): void {
    const seleccionada = this.seccionSeleccionada();
    if (!seleccionada) return;

    this.eliminarSeccionMutation.mutate(seleccionada.idSeccion, {
      onSuccess: () => {
        this.notifications
          .open('Sección eliminada exitosamente', { label: 'Eliminada', appearance: 'success', autoClose: 3000 })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar sección', { label: 'Error', appearance: 'error', autoClose: 5000 })
          .subscribe();
      },
    });
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  isGuardando(): boolean {
    return this.crearSeccionMutation.isPending() || this.actualizarSeccionMutation.isPending();
  }

  isEliminando(): boolean {
    return this.eliminarSeccionMutation.isPending();
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
    this.seccionForm.reset({
      codigoSeccion: '',
      vacantes: 1,
      cicloAcademicoNombre: '',
      idCurso: null,
      idCiclo: null,
    });
    this.seccionForm.markAsPristine();
    this.seccionForm.markAsUntouched();
  }

  private cargarFormulario(seccion: SeccionResponse): void {
    this.seccionForm.reset({
      codigoSeccion: seccion.codigoSeccion,
      vacantes: seccion.vacantes,
      cicloAcademicoNombre: seccion.cicloAcademicoNombre,
      idCurso: seccion.curso.idCurso,
      idCiclo: seccion.cicloAcademico.idCiclo,
    });
    this.seccionForm.markAsPristine();
    this.seccionForm.markAsUntouched();
  }

  private construirPayload(): SeccionCreateRequest | null {
    const value = this.seccionForm.getRawValue();
    return {
      codigoSeccion: value.codigoSeccion.trim(),
      vacantes: Number(value.vacantes),
      cicloAcademicoNombre: value.cicloAcademicoNombre.trim(),
    };
  }
}
