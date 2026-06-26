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
import { HorarioResponse } from '../../../models/horario/horario.response';
import { HorarioCreateRequest } from '../../../models/horario/horario.request';
import {
  useHorariosPaginadosQuery,
  useCrearHorarioMutation,
  useActualizarHorarioMutation,
  useEliminarHorarioMutation,
} from '../../../queries/horario.query';
import { HorarioService } from '../../../core/services/horario.service';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-horarios',
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
  templateUrl: './horarios.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de horarios académicos.
 * Lista, crea, edita y elimina horarios con modal de formulario y confirmación.
 * Cada horario define día, horas y aula para una sección.
 *
 * Accesible para: ADMIN, DOCENTE
 */
export class Horarios {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);
  private readonly horarioService = inject(HorarioService);

  /** Columnas de la tabla de horarios. */
  readonly columns = ['diaSemana', 'horaInicio', 'horaFin', 'aula', 'seccion', 'acciones'] as const;

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
  readonly horariosQuery = useHorariosPaginadosQuery(this.pagina, this.tamaño, this.busqueda);

  /** Total de elementos (de la respuesta paginada). */
  readonly totalHorarios = computed(() => this.horariosQuery.data()?.totalElements ?? 0);
  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.horariosQuery.data()?.totalPages ?? 0);
  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.horariosQuery.isPending());

  // ─── Listas para dropdowns ───────────────────────────────────────────

  /** Lista de secciones para el select del formulario. */
  readonly seccionesList = signal<HorarioResponse['seccion'][]>([]);
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

    this.horarioService
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
  readonly horarioModalAbierto = signal(false);
  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);
  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');
  /** Horario seleccionado para edición o eliminación. */
  readonly horarioSeleccionado = signal<HorarioResponse | null>(null);

  // ─── Formulario ──────────────────────────────────────────────────────

  readonly horarioForm = this.formBuilder.group({
    diaSemana: this.formBuilder.nonNullable.control('', [Validators.required]),
    horaInicio: this.formBuilder.nonNullable.control('', [Validators.required]),
    horaFin: this.formBuilder.nonNullable.control('', [Validators.required]),
    aula: this.formBuilder.nonNullable.control(''),
    idSeccion: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
  });

  // ─── Mutaciones ──────────────────────────────────────────────────────

  readonly crearHorarioMutation = useCrearHorarioMutation();
  readonly actualizarHorarioMutation = useActualizarHorarioMutation();
  readonly eliminarHorarioMutation = useEliminarHorarioMutation();

  // ─── Paginación ──────────────────────────────────────────────────────

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.horariosQuery.data();
    if (!data || data.totalElements === 0) return '0 horarios';
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
  openNuevoHorarioModal(): void {
    this.modoFormulario.set('crear');
    this.horarioSeleccionado.set(null);
    this.resetFormulario();
    this.horarioModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo del horario. */
  openEditarHorarioModal(horario: HorarioResponse): void {
    this.modoFormulario.set('editar');
    this.horarioSeleccionado.set(horario);
    this.cargarFormulario(horario);
    this.horarioModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeHorarioModal(): void {
    this.horarioModalAbierto.set(false);
    this.horarioSeleccionado.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarHorarioModal(horario: HorarioResponse): void {
    this.horarioSeleccionado.set(horario);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.horarioSeleccionado.set(null);
  }

  // ─── Guardar (crear/editar) ──────────────────────────────────────────

  guardarHorario(observer: DialogObserver): void {
    if (this.horarioForm.invalid) {
      this.horarioForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();
    if (!payload) return;

    const raw = this.horarioForm.getRawValue();
    const idSeccion = raw.idSeccion!;

    if (this.modoFormulario() === 'crear') {
      this.crearHorarioMutation.mutate(
        { horario: payload, idSeccion },
        {
          onSuccess: () => {
            this.notifications
              .open('Horario creado exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
              .subscribe();
            observer.complete();
            this.closeHorarioModal();
          },
          onError: (error) => {
            this.notifications
              .open(error?.message ?? 'Error al crear horario', { label: 'Error', appearance: 'error', autoClose: 5000 })
              .subscribe();
          },
        },
      );
      return;
    }

    const seleccionado = this.horarioSeleccionado();
    if (!seleccionado) return;

    this.actualizarHorarioMutation.mutate(
      { id: seleccionado.idHorario, horario: payload, idSeccion },
      {
        onSuccess: () => {
          this.notifications
            .open('Horario actualizado exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
          observer.complete();
          this.closeHorarioModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar horario', { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  // ─── Eliminar ────────────────────────────────────────────────────────

  confirmarEliminar(observer: DialogObserver): void {
    const seleccionado = this.horarioSeleccionado();
    if (!seleccionado) return;

    this.eliminarHorarioMutation.mutate(seleccionado.idHorario, {
      onSuccess: () => {
        this.notifications
          .open('Horario eliminado exitosamente', { label: 'Eliminado', appearance: 'success', autoClose: 3000 })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar horario', { label: 'Error', appearance: 'error', autoClose: 5000 })
          .subscribe();
      },
    });
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  isGuardando(): boolean {
    return this.crearHorarioMutation.isPending() || this.actualizarHorarioMutation.isPending();
  }

  isEliminando(): boolean {
    return this.eliminarHorarioMutation.isPending();
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
    this.horarioForm.reset({
      diaSemana: '',
      horaInicio: '',
      horaFin: '',
      aula: '',
      idSeccion: null,
    });
    this.horarioForm.markAsPristine();
    this.horarioForm.markAsUntouched();
  }

  private cargarFormulario(horario: HorarioResponse): void {
    this.horarioForm.reset({
      diaSemana: horario.diaSemana,
      horaInicio: horario.horaInicio.substring(0, 5),
      horaFin: horario.horaFin.substring(0, 5),
      aula: horario.aula ?? '',
      idSeccion: horario.seccion.idSeccion,
    });
    this.horarioForm.markAsPristine();
    this.horarioForm.markAsUntouched();
  }

  private construirPayload(): HorarioCreateRequest | null {
    const value = this.horarioForm.getRawValue();
    return {
      diaSemana: value.diaSemana.trim(),
      horaInicio: value.horaInicio + ':00',
      horaFin: value.horaFin + ':00',
      aula: value.aula.trim() || null,
    };
  }
}
