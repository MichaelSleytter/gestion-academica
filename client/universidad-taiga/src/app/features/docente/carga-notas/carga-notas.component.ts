import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiLoader, TuiNotificationService } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import type { MatriculaResponse } from '../../../models/matricula';
import type { NotaResponse } from '../../../models/nota';
import type { EvaluacionResponse } from '../../../models/evaluacion/evaluacion.response';
import {
  useEvaluacionesBySeccionQuery,
  useMatriculasBySeccionQuery,
} from '../../../queries/docente-role.query';
import { DocenteRoleService } from '../../../core/services/docente-role.service';

/**
 * Tipo para el estado de guardado de cada celda.
 */
type CellStatus = 'idle' | 'dirty' | 'saving' | 'saved' | 'error';

/**
 * Vista de carga de notas tipo planilla (spreadsheet).
 *
 * Muestra todas las evaluaciones de la sección como columnas,
 * permitiendo editar todas las notas en una sola vista y guardarlas
 * en conjunto. Navegación por teclado con Tab y Enter.
 *
 * Accesible para: DOCENTE
 */
@Component({
  selector: 'app-carga-notas',
  imports: [RouterLink, TuiButton, TuiCardLarge, TuiHeader, TuiIcon, TuiLoader],
  templateUrl: './carga-notas.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CargaNotas {
  private readonly route = inject(ActivatedRoute);
  private readonly docenteRoleService = inject(DocenteRoleService);
  private readonly notifications = inject(TuiNotificationService);

  /** ID de sección recibido por ruta. */
  readonly sectionId = signal(this.getRouteSectionId());

  // ─── Queries ─────────────────────────────────────────────────────

  /** Query de evaluaciones de la sección. */
  readonly evaluationsQuery = useEvaluacionesBySeccionQuery(this.sectionId);

  /** Query de matrículas de la sección. */
  readonly enrollmentsQuery = useMatriculasBySeccionQuery(this.sectionId);

  /** Evaluaciones disponibles para la sección. */
  readonly evaluations = computed(() => this.evaluationsQuery.data() ?? []);

  /** Matrículas disponibles para la sección. */
  readonly enrollments = computed(() => this.enrollmentsQuery.data() ?? []);

  /** Indica si las queries principales están cargando. */
  readonly isLoading = computed(
    () => this.evaluationsQuery.isPending() || this.enrollmentsQuery.isPending(),
  );

  // ─── Notas ───────────────────────────────────────────────────────

  /** Mapa de notas: [idEvaluacion] → NotaResponse[]. Se carga en paralelo. */
  readonly allNotesMap = signal<Record<number, NotaResponse[]>>({});

  /** Indica si las notas se están cargando. */
  readonly isLoadingNotes = signal(false);

  /** Mapa de celdas editadas localmente: clave "${idEvaluacion}-${idEstudiante}" → valor. */
  readonly gradeDrafts = signal<Record<string, number | null>>({});

  /** Mapa de estados por celda: clave "${idEvaluacion}-${idEstudiante}" → CellStatus. */
  readonly cellStatus = signal<Record<string, CellStatus>>({});

  /** Indica si hay cambios sin guardar. */
  readonly hasUnsavedChanges = computed(() => {
    return Object.values(this.gradeDrafts()).some((v) => v !== null);
  });

  /** Cantidad de notas pendientes de guardar. */
  readonly pendingCount = computed(() => {
    return Object.entries(this.gradeDrafts()).filter(([_, v]) => v !== null).length;
  });

  /** Indica si se está guardando (cualquier celda en saving). */
  readonly isSaving = computed(() => {
    return Object.values(this.cellStatus()).some((s) => s === 'saving');
  });

  /** Progreso: fracción de estudiantes que tienen AL MENOS UNA nota cargada. */
  readonly progressFraction = computed(() => {
    const evals = this.evaluations();
    const enrollments = this.enrollments();
    if (evals.length === 0 || enrollments.length === 0) return 0;

    const totalCells = enrollments.length * evals.length;
    let filledCells = 0;

    for (const enrollment of enrollments) {
      const idEstudiante = this.getStudentId(enrollment);
      if (!idEstudiante) continue;

      for (const evaluation of evals) {
        const existing = this.findNotaByStudent(evaluation.idEvaluacion, idEstudiante);
        const draftKey = `${evaluation.idEvaluacion}-${idEstudiante}`;
        const draft = this.gradeDrafts()[draftKey];

        if (draft !== undefined || existing) {
          filledCells++;
        }
      }
    }

    return totalCells > 0 ? filledCells / totalCells : 0;
  });

  /** Etiqueta de progreso. */
  readonly progressLabel = computed(() => {
    const evals = this.evaluations();
    const enrollments = this.enrollments();
    const totalCells = enrollments.length * evals.length;
    const filled = Math.round(this.progressFraction() * totalCells);
    return `${filled}/${totalCells} notas`;
  });

  constructor() {
    effect(() => {
      const evals = this.evaluations();
      if (evals.length > 0) {
        void this.cargarNotasEnParalelo(evals);
      }
    });
  }

  /**
   * Carga las notas de todas las evaluaciones en paralelo.
   */
  private async cargarNotasEnParalelo(evals: EvaluacionResponse[]): Promise<void> {
    if (evals.length === 0) return;

    this.isLoadingNotes.set(true);
    try {
      const results = await Promise.all(
        evals.map((e) => this.docenteRoleService.getNotasByEvaluacion(e.idEvaluacion)),
      );
      const map: Record<number, NotaResponse[]> = {};
      evals.forEach((e, i) => {
        map[e.idEvaluacion] = results[i];
      });
      this.allNotesMap.set(map);
    } catch {
      this.notifications
        .open('Error al cargar las notas. Intentá de nuevo.', {
          label: 'Error',
          appearance: 'error',
          autoClose: 4000,
        })
        .subscribe();
    } finally {
      this.isLoadingNotes.set(false);
    }
  }

  // ─── Input methods ──────────────────────────────────────────────

  /**
   * Obtiene el valor actual de una celda (borrador o persistido o vacío).
   *
   * @param idEvaluacion - ID de la evaluación (columna).
   * @param idEstudiante - ID del estudiante (fila).
   * @returns Valor de nota o null.
   */
  getGradeValue(idEvaluacion: number, idEstudiante: number): number | null {
    const key = `${idEvaluacion}-${idEstudiante}`;

    // 1. Borrador (prioridad)
    if (Object.hasOwn(this.gradeDrafts(), key)) {
      return this.gradeDrafts()[key];
    }

    // 2. Valor persistido
    const existing = this.findNotaByStudent(idEvaluacion, idEstudiante);
    return existing?.nota ?? null;
  }

  /**
   * Maneja input del usuario en una celda.
   *
   * @param idEvaluacion - ID de la evaluación.
   * @param idEstudiante - ID del estudiante.
   * @param value - Valor del input (string del evento).
   */
  onGradeInput(idEvaluacion: number, idEstudiante: number, value: string): void {
    const key = `${idEvaluacion}-${idEstudiante}`;
    const grade = value === '' ? null : Number(value);
    const finalGrade = Number.isFinite(grade) ? grade : null;

    this.gradeDrafts.update((drafts) => ({
      ...drafts,
      [key]: finalGrade,
    }));

    this.cellStatus.update((s) => ({
      ...s,
      [key]: 'dirty' as CellStatus,
    }));
  }

  /**
   * Navega al siguiente input (siguiente celda o fila).
   *
   * @param idEvaluacion - ID de la evaluación actual.
   * @param idEstudiante - ID del estudiante actual.
   * @param event - Evento de teclado.
   */
  onCellKeydown(idEvaluacion: number, idEstudiante: number, event: KeyboardEvent): void {
    const evals = this.evaluations();
    const enrollments = this.enrollments();
    const evalIndex = evals.findIndex((e) => e.idEvaluacion === idEvaluacion);
    const studentIndex = enrollments.findIndex((e) => this.getStudentId(e) === idEstudiante);

    let nextEvalIndex = evalIndex;
    let nextStudentIndex = studentIndex;
    let moved = false;

    if (event.key === 'Tab' && !event.shiftKey) {
      event.preventDefault();
      nextEvalIndex = evalIndex + 1;
      if (nextEvalIndex >= evals.length) {
        nextEvalIndex = 0;
        nextStudentIndex = studentIndex + 1;
      }
      moved = true;
    } else if (event.key === 'Tab' && event.shiftKey) {
      event.preventDefault();
      nextEvalIndex = evalIndex - 1;
      if (nextEvalIndex < 0) {
        nextEvalIndex = evals.length - 1;
        nextStudentIndex = studentIndex - 1;
      }
      moved = true;
    } else if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      nextStudentIndex = studentIndex + 1;
      moved = true;
    } else if (event.key === 'Enter' && event.shiftKey) {
      event.preventDefault();
      nextStudentIndex = studentIndex - 1;
      moved = true;
    }

    if (
      moved &&
      nextStudentIndex >= 0 &&
      nextStudentIndex < enrollments.length &&
      nextEvalIndex >= 0 &&
      nextEvalIndex < evals.length
    ) {
      const nextEstudiante = this.getStudentId(enrollments[nextStudentIndex]);
      const nextEval = evals[nextEvalIndex];
      if (nextEstudiante !== null) {
        const inputId = `grade-${nextEval.idEvaluacion}-${nextEstudiante}`;
        setTimeout(() => {
          const el = document.getElementById(inputId) as HTMLInputElement | null;
          el?.focus();
          el?.select();
        }, 0);
      }
    }
  }

  // ─── Save ────────────────────────────────────────────────────────

  /**
   * Guarda una nota individual (create o update).
   */
  private async guardarUnaNota(key: string, nota: number): Promise<void> {
    const [idEvaluacionStr, idEstudianteStr] = key.split('-');
    const idEvaluacion = Number(idEvaluacionStr);
    const idEstudiante = Number(idEstudianteStr);

    const existing = this.findNotaByStudent(idEvaluacion, idEstudiante);

    this.cellStatus.update((s) => ({ ...s, [key]: 'saving' as CellStatus }));

    if (existing) {
      await this.docenteRoleService.updateNota(
        existing.idNota,
        { nota },
        idEvaluacion,
        idEstudiante,
      );
    } else {
      await this.docenteRoleService.createNota({ nota }, idEvaluacion, idEstudiante);
    }

    this.cellStatus.update((s) => ({ ...s, [key]: 'saved' as CellStatus }));
  }

  /**
   * Guarda TODAS las notas pendientes en lote.
   */
  async guardarTodas(): Promise<void> {
    const drafts = this.gradeDrafts();
    const entries = Object.entries(drafts).filter(([_, v]) => v !== null && v! >= 0 && v! <= 20);

    if (entries.length === 0) {
      // Marcar como vacío si solo hay borradores null
      const nullEntries = Object.entries(drafts).filter(([_, v]) => v === null);
      for (const [key] of nullEntries) {
        this.cellStatus.update((s) => ({ ...s, [key]: 'idle' as CellStatus }));
      }
      this.gradeDrafts.set({});
      return;
    }

    // Set all pending to 'saving'
    for (const [key] of entries) {
      this.cellStatus.update((s) => ({ ...s, [key]: 'saving' as CellStatus }));
    }

    let saved = 0;
    let errors = 0;
    let lastError: string | null = null;

    const savePromises: Promise<void>[] = [];
    for (const [key, nota] of entries) {
      const notaValor = nota!;
      const promise = this.guardarUnaNota(key, notaValor)
        .then(() => {
          saved++;
        })
        .catch((error: unknown) => {
          errors++;
          lastError = error instanceof Error ? error.message : 'Error al guardar';
          this.cellStatus.update((s) => ({ ...s, [key]: 'error' as CellStatus }));
        });
      savePromises.push(promise);
    }

    await Promise.all(savePromises);

    // Clear drafts
    this.gradeDrafts.set({});

    // Recargar notas para tener el estado persistido actualizado
    await this.cargarNotasEnParalelo(this.evaluations());

    // Limpiar estados de celda después de un breve momento
    setTimeout(() => {
      this.cellStatus.set({});
    }, 2000);

    if (errors === 0) {
      this.notifications
        .open(
          `${saved} nota${saved !== 1 ? 's' : ''} guardada${saved !== 1 ? 's' : ''} exitosamente`,
          {
            label: 'Éxito',
            appearance: 'success',
            autoClose: 2500,
          },
        )
        .subscribe();
    } else {
      this.notifications
        .open(
          `${saved} guardada${saved !== 1 ? 's' : ''}, ${errors} error${errors > 1 ? 'es' : ''}${lastError ? ': ' + lastError : ''}`,
          { label: 'Completado con errores', appearance: 'warning', autoClose: 5000 },
        )
        .subscribe();
    }
  }

  // ─── Helper methods ─────────────────────────────────────────────

  /**
   * Obtiene el ID del estudiante de una matrícula.
   */
  getStudentId(enrollment: MatriculaResponse): number | null {
    return enrollment.idEstudiante ?? enrollment.estudiante?.idUsuario ?? null;
  }

  /**
   * Obtiene la etiqueta visible del estudiante.
   */
  getStudentLabel(enrollment: MatriculaResponse): string {
    const student = enrollment.estudiante;
    if (!student) {
      const fullName = [enrollment.nombre, enrollment.apellido].filter(Boolean).join(' ');
      return fullName || enrollment.codigoEstudiante || `Estudiante #${enrollment.idEstudiante}`;
    }

    const fullName = [student.usuario?.nombre, student.usuario?.apellido].filter(Boolean).join(' ');
    return fullName || student.codigoEstudiante || `Estudiante #${student.idUsuario}`;
  }

  /**
   * Busca nota existente por estudiante dentro de una evaluación.
   */
  findNotaByStudent(idEvaluacion: number, idEstudiante: number): NotaResponse | undefined {
    const notes = this.allNotesMap()[idEvaluacion] ?? [];
    return notes.find((n) => (n.idEstudiante ?? n.estudiante?.idUsuario) === idEstudiante);
  }

  /**
   * Obtiene el estado de celda actual.
   */
  getCellStatus(idEvaluacion: number, idEstudiante: number): CellStatus {
    return this.cellStatus()[`${idEvaluacion}-${idEstudiante}`] ?? 'idle';
  }

  private getRouteSectionId(): number | null {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    return Number.isFinite(id) ? id : null;
  }
}
