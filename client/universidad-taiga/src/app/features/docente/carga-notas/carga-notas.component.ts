import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TuiButton, TuiIcon, TuiInput, TuiLoader, TuiTextfield } from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import type { MatriculaResponse } from '../../../models/matricula';
import type { NotaResponse } from '../../../models/nota';
import {
  useEvaluacionesBySeccionQuery,
  useMatriculasBySeccionQuery,
  useNotasByEvaluacionQuery,
  useSaveNotaMutation,
} from '../../../queries/docente-role.query';

/**
 * Vista de carga de notas para un docente.
 *
 * Muestra los estudiantes de una sección específica y permite
 * al docente registrar o modificar sus notas de evaluación.
 *
 * Accesible para: DOCENTE
 */
@Component({
  selector: 'app-carga-notas',
  imports: [
    RouterLink,
    TuiButton,
    TuiCardLarge,
    TuiHeader,
    TuiIcon,
    TuiInput,
    TuiLoader,
    TuiTable,
    TuiTextfield,
  ],
  templateUrl: './carga-notas.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CargaNotas {
  private readonly route = inject(ActivatedRoute);

  /** Columnas de la tabla de carga de notas. */
  readonly columns = ['student', 'status', 'grade', 'actions'] as const;

  /** ID de sección recibido por ruta. */
  readonly sectionId = signal(this.getRouteSectionId());

  /** ID de evaluación seleccionada para cargar notas. */
  readonly selectedEvaluationId = signal<number | null>(null);

  /** Valores editados localmente antes de guardar. */
  readonly gradeDrafts = signal<Record<number, number | null>>({});

  /** Query de evaluaciones de la sección. */
  readonly evaluationsQuery = useEvaluacionesBySeccionQuery(this.sectionId);

  /** Query de matrículas de la sección. */
  readonly enrollmentsQuery = useMatriculasBySeccionQuery(this.sectionId);

  /** Query de notas de la evaluación seleccionada. */
  readonly notesQuery = useNotasByEvaluacionQuery(this.selectedEvaluationId);

  /** Mutación de guardado de nota. */
  readonly saveNotaMutation = useSaveNotaMutation();

  /** Evaluaciones disponibles para la sección. */
  readonly evaluations = computed(() => this.evaluationsQuery.data() ?? []);

  /** Matrículas disponibles para la sección. */
  readonly enrollments = computed(() => this.enrollmentsQuery.data() ?? []);

  /** Notas registradas para la evaluación seleccionada. */
  readonly notes = computed(() => this.notesQuery.data() ?? []);

  /** Evaluación seleccionada completa. */
  readonly selectedEvaluation = computed(() =>
    this.evaluations().find((evaluation) => evaluation.idEvaluacion === this.selectedEvaluationId()) ?? null,
  );

  /** Indica si alguna consulta principal está cargando. */
  readonly isLoading = computed(() => this.evaluationsQuery.isPending() || this.enrollmentsQuery.isPending());

  constructor() {
    effect(() => {
      if (!this.selectedEvaluationId() && this.evaluations().length > 0) {
        this.selectedEvaluationId.set(this.evaluations()[0].idEvaluacion);
      }
    });
  }

  /**
   * Actualiza la evaluación seleccionada desde el selector.
   *
   * @param value - Valor recibido desde el select nativo.
   */
  onEvaluationChange(value: string): void {
    this.selectedEvaluationId.set(Number(value));
    this.gradeDrafts.set({});
  }

  /**
   * Guarda el borrador de nota de un estudiante.
   *
   * @param idEstudiante - ID del estudiante.
   * @param value - Valor recibido desde el input numérico.
   */
  onGradeInput(idEstudiante: number, value: string): void {
    const grade = value === '' ? null : Number(value);
    this.gradeDrafts.update((drafts) => ({
      ...drafts,
      [idEstudiante]: Number.isFinite(grade) ? grade : null,
    }));
  }

  /**
   * Persiste la nota de un estudiante para la evaluación seleccionada.
   *
   * @param enrollment - Matrícula asociada al estudiante.
   */
  saveGrade(enrollment: MatriculaResponse): void {
    const idEstudiante = this.getStudentId(enrollment);
    const idEvaluacion = this.selectedEvaluationId();
    if (!idEstudiante || !idEvaluacion) return;

    const nota = this.getGradeValue(idEstudiante);
    if (nota === null || nota < 0 || nota > 20) {
      return;
    }

    const existing = this.findNotaByStudent(idEstudiante);
    this.saveNotaMutation.mutate(
      { idNota: existing?.idNota, idEvaluacion, idEstudiante, nota: { nota } },
      {},
    );
  }

  /**
   * Obtiene el ID del estudiante de una matrícula.
   *
   * @param enrollment - Matrícula de la sección.
   * @returns ID del estudiante o null si no está disponible.
   */
  getStudentId(enrollment: MatriculaResponse): number | null {
    return enrollment.idEstudiante ?? enrollment.estudiante?.idUsuario ?? null;
  }

  /**
   * Obtiene la etiqueta visible del estudiante.
   *
   * @param enrollment - Matrícula de la sección.
   * @returns Nombre o código disponible para mostrar.
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
   * Obtiene la nota editable actual de un estudiante.
   *
   * @param idEstudiante - ID del estudiante.
   * @returns Nota borrador, nota persistida o null.
   */
  getGradeValue(idEstudiante: number): number | null {
    const draft = this.gradeDrafts()[idEstudiante];
    return draft ?? this.findNotaByStudent(idEstudiante)?.nota ?? null;
  }

  /**
   * Busca una nota ya existente por estudiante.
   *
   * @param idEstudiante - ID del estudiante.
   * @returns Nota encontrada o undefined.
   */
  findNotaByStudent(idEstudiante: number): NotaResponse | undefined {
    return this.notes().find((nota) => (nota.idEstudiante ?? nota.estudiante?.idUsuario) === idEstudiante);
  }

  private getRouteSectionId(): number | null {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    return Number.isFinite(id) ? id : null;
  }
}
