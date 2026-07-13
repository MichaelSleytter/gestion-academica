import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  TuiButton,
  TuiDialog,
  TuiDialogService,
  TuiHint,
  TuiIcon,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiPlatform, TuiTime } from '@taiga-ui/cdk';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TUI_CONFIRM, type TuiConfirmData, TuiInputTime, TuiSkeleton, tuiCreateTimePeriods, tuiInputTimeOptionsProvider } from '@taiga-ui/kit';
import { firstValueFrom } from 'rxjs';
import { HorarioResponse } from '../../../models/horario/horario.response';
import { HorarioCreateRequest } from '../../../models/horario/horario.request';
import {
  useHorariosPaginadosQuery,
  useCrearHorarioMutation,
  useActualizarHorarioMutation,
  useEliminarHorarioMutation,
} from '../../../queries/horario.query';
import { HorarioService } from '../../../core/services/horario.service';
import {
  CALENDAR_DAYS,
  END_HOUR,
  HOUR_HEIGHT,
  START_HOUR,
  CalendarHorarioEvent,
  addWeeks,
  filterCalendarEvents,
  getCurrentWeekStart,
  getWeekLabel,
  isCurrentWeek,
  mapHorarioToCalendarEvent,
  moveEventToSlot,
  uniqueEventSections,
} from './calendar.helpers';

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
    TuiHint,
    TuiInput,
    TuiPlatform,
    TuiTextfield,
    TuiTable,
    TuiTitle,
    TuiIcon,
    TuiInputTime,
    TuiSkeleton,
  ],
  templateUrl: './horarios.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    tuiInputTimeOptionsProvider({
      valueTransformer: {
        fromControlValue(controlValue: string): TuiTime | null {
          if (!controlValue) return null;
          const [hours, minutes] = controlValue.split(':').map(Number);
          return isNaN(hours) || isNaN(minutes) ? null : new TuiTime(hours, minutes);
        },
        toControlValue(time: TuiTime | null): string {
          return time ? time.toString('HH:MM') : '';
        },
      },
    }),
  ],
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
  private readonly dialogs = inject(TuiDialogService);

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
    if (typeof window !== 'undefined') {
      const mql = window.matchMedia('(max-width: 767px)');
      this.isMobile.set(mql.matches);
      mql.addEventListener('change', (e) => this.isMobile.set(e.matches));
    }
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

  // ─── Semana actual ──────────────────────────────────────────────────

  /** Fecha de inicio de la semana visible (siempre lunes). */
  readonly currentWeekStart = signal(getCurrentWeekStart());
  /** Etiqueta formateada de la semana visible. */
  readonly weekLabel = computed(() => getWeekLabel(this.currentWeekStart()));
  /** Indica si la semana visible es la actual. */
  readonly isCurrentWeek = computed(() => isCurrentWeek(this.currentWeekStart()));
  /** Indica si el viewport es menor a 768px. */
  readonly isMobile = signal(false);

  /** Navega a la semana anterior. */
  previousWeek(): void {
    this.currentWeekStart.update((d) => addWeeks(d, -1));
  }

  /** Navega a la semana siguiente. */
  nextWeek(): void {
    this.currentWeekStart.update((d) => addWeeks(d, 1));
  }

  /** Vuelve a la semana actual. */
  goToCurrentWeek(): void {
    this.currentWeekStart.set(getCurrentWeekStart());
  }

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
  protected readonly Number = Number;
  protected readonly calendarDays = CALENDAR_DAYS;
  protected readonly calendarHours = Array.from({ length: END_HOUR - START_HOUR + 1 }, (_, i) => START_HOUR + i);
  protected readonly hourHeight = HOUR_HEIGHT;

  /** 30-min interval times for the time picker suggestions. */
  protected readonly acceptableTimes: readonly TuiTime[] = (() => {
    const periods = tuiCreateTimePeriods(START_HOUR, END_HOUR, [0, 30]);
    return [...periods, new TuiTime(END_HOUR, 0)];
  })();

  readonly selectedSectionId = signal<number | null>(null);
  readonly selectedDayIndex = signal<number | null>(null);
  readonly aulaFilter = signal('');
  readonly focusedDayIndex = signal(0);
  readonly draggedEvent = signal<CalendarHorarioEvent | null>(null);

  readonly calendarEvents = computed(() =>
    (this.horariosQuery.data()?.content ?? []).map((horario) => this.toCalendarEvent(horario)),
  );
  readonly filteredCalendarEvents = computed(() =>
    filterCalendarEvents(this.calendarEvents(), {
      sectionId: this.selectedSectionId(),
      dayIndex: this.selectedDayIndex(),
      aula: this.aulaFilter(),
    }),
  );
  readonly calendarLegend = computed(() => uniqueEventSections(this.calendarEvents()));
  readonly visibleCalendarDays = computed(() => {
    if (this.isMobile()) {
      return [this.selectedDayIndex() ?? new Date().getDay() - 1]; // Mon=0
    }
    const selected = this.selectedDayIndex();
    return selected == null ? this.calendarDays.map((_day, index) => index) : [selected];
  });

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

  openCrearDesdeSlot(dayIndex: number, hour: number): void {
    this.openNuevoHorarioModal();
    this.horarioForm.patchValue({
      diaSemana: this.calendarDays[dayIndex] ?? this.calendarDays[0],
      horaInicio: this.formatHour(hour),
      horaFin: this.formatHour(hour + 1),
    });
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

  /** Abre diálogo de confirmación para eliminación usando TUI_CONFIRM. */
  async openEliminarHorarioModal(horario: HorarioResponse): Promise<void> {
    const confirmed = await firstValueFrom(
      this.dialogs.open<boolean>(TUI_CONFIRM, {
        label: 'Eliminar horario',
        size: 's',
        data: {
          content: `¿Eliminar el horario de ${horario.diaSemana} a las ${horario.horaInicio.substring(0, 5)}? Esta acción no se puede deshacer.`,
          yes: 'Eliminar',
          no: 'Cancelar',
          appearance: 'accent',
        } satisfies TuiConfirmData,
      }),
    );

    if (!confirmed) return;

    this.horarioSeleccionado.set(horario);
    this.ejecutarEliminacion();
  }

  /** Ejecuta la mutación de eliminación. */
  private ejecutarEliminacion(): void {
    const seleccionado = this.horarioSeleccionado();
    if (!seleccionado) return;

    this.eliminarHorarioMutation.mutate(seleccionado.idHorario, {
      onSuccess: () => {
        this.notifications
          .open('Horario eliminado exitosamente', { label: 'Eliminado', appearance: 'success', autoClose: 3000 })
          .subscribe();
        this.horarioSeleccionado.set(null);
      },
      onError: (error) => {
        this.notifications
          .open(this.errorMessage(error, 'Error al eliminar horario'), { label: 'Error', appearance: 'error', autoClose: 5000 })
          .subscribe();
      },
    });
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
              .open(this.errorMessage(error, 'Error al crear horario'), { label: 'Error', appearance: 'error', autoClose: 5000 })
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
            .open(this.errorMessage(error, 'Error al actualizar horario'), { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  isGuardando(): boolean {
    return this.crearHorarioMutation.isPending() || this.actualizarHorarioMutation.isPending();
  }

  isEliminando(): boolean {
    return this.eliminarHorarioMutation.isPending();
  }

  toCalendarEvent(horario: HorarioResponse): CalendarHorarioEvent {
    return mapHorarioToCalendarEvent(horario);
  }

  eventsForDay(dayIndex: number): CalendarHorarioEvent[] {
    return this.filteredCalendarEvents().filter((event) => event.dayIndex === dayIndex);
  }

  dayLabel(dayIndex: number): string {
    return this.calendarDays[dayIndex] ?? this.calendarDays[0];
  }

  formatHour(hour: number): string {
    return `${String(hour).padStart(2, '0')}:00`;
  }

  onDragStart(event: DragEvent, calendarEvent: CalendarHorarioEvent): void {
    this.draggedEvent.set(calendarEvent);
    event.dataTransfer?.setData('text/plain', String(calendarEvent.idHorario));
    if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move';
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move';
  }

  onDrop(event: DragEvent, dayIndex: number, hour: number): void {
    event.preventDefault();
    const dragged = this.draggedEvent();
    this.draggedEvent.set(null);
    if (!dragged) return;

    this.moveCalendarEvent(dragged, dayIndex, hour);
  }

  moveCalendarEvent(event: CalendarHorarioEvent, dayIndex: number, hour: number): void {
    const horario = moveEventToSlot(event, dayIndex, hour * 60);

    this.actualizarHorarioMutation.mutate(
      { id: event.idHorario, horario, idSeccion: event.idSeccion },
      {
        onSuccess: () => {
          this.notifications
            .open('Horario actualizado exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
        },
        onError: (error) => {
          this.notifications
            .open(this.errorMessage(error, 'Error al mover horario'), { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  resizeCalendarEvent(event: CalendarHorarioEvent, hours: number): void {
    const payload: HorarioCreateRequest = {
      diaSemana: event.diaSemana,
      horaInicio: event.horaInicio,
      horaFin: `${String(Math.min(END_HOUR, Number(event.horaFin.slice(0, 2)) + hours)).padStart(2, '0')}${event.horaFin.slice(2)}`,
      aula: event.aula,
    };

    this.actualizarHorarioMutation.mutate({ id: event.idHorario, horario: payload, idSeccion: event.idSeccion });
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

  private errorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string') return error.error;
      const body = error.error as { mensaje?: string; message?: string } | null;
      return body?.mensaje ?? body?.message ?? error.message ?? fallback;
    }

    return error instanceof Error ? error.message : fallback;
  }
}
