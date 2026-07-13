import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { TuiButton, TuiIcon, TuiLoader } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiTable } from '@taiga-ui/addon-table';
import {
  useMisCursosQuery,
  useEvaluacionesBySeccionEstudianteQuery,
  useMisNotasBySeccionQuery,
} from '../../../queries/estudiante-role.query';
import { ContenidoService } from '../../../core/services/contenido.service';
import type { CursoContenidoResponse } from '../../../models/contenido/curso-contenido.response';
import { isCurrentAcademicPeriod } from '../../../shared/utils/academic-period';
/**
 * Evaluación combinada con su nota (si existe).
 */
interface EvaluacionConNota {
  idEvaluacion: number;
  nombre: string;
  porcentaje: number;
  nota: number | null;
}

/**
 * Vista principal del Estudiante.
 *
 * Maneja dos estados según la ruta:
 * - `/app/estudiante/mis-cursos`: lista de cursos matriculados
 * - `/app/estudiante/mis-cursos/:id/notas`: notas por evaluación de un curso
 *
 * Es el home del rol ESTUDIANTE (redirección post-login).
 *
 * Accesible para: ESTUDIANTE
 */
@Component({
  selector: 'app-mis-notas',
  imports: [RouterModule, TuiButton, TuiCardLarge, TuiHeader, TuiIcon, TuiLoader, TuiTable],
  templateUrl: './mis-notas.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisNotas {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly contenidoService = inject(ContenidoService);

  /** ID de sección desde la ruta (null si estamos en la lista). */
  readonly idSeccion = toSignal(
    this.route.paramMap.pipe(
      map((params) => {
        const id = Number(params.get('id'));
        return Number.isFinite(id) && id > 0 ? id : null;
      }),
    ),
    { initialValue: null },
  );

  readonly activeTab = toSignal(
    this.route.url.pipe(map((segments) => segments.at(-1)?.path === 'contenido' ? 'contenido' : 'notas')),
    { initialValue: 'notas' },
  );

  /** Indica si estamos en la vista de detalle (notas de un curso). */
  readonly isDetailView = computed(() => this.idSeccion() !== null);

  // ─── Lista de cursos ───────────────────────────────────────────

  /** Query de cursos matriculados. */
  readonly misCursosQuery = useMisCursosQuery();

  /** Cursos matriculados del estudiante. */
  readonly cursosMatriculados = computed(() => this.misCursosQuery.data() ?? []);
  private readonly cursosActuales = computed(() =>
    this.cursosMatriculados().filter(
      (curso) => curso.estado === 'ACTIVA' && isCurrentAcademicPeriod(curso),
    ),
  );

  readonly periodoSeleccionado = signal('actual');

  readonly periodos = computed(() =>
    [...new Set(this.cursosMatriculados().map((curso) => curso.cicloAcademicoNombre).filter(Boolean))],
  );

  readonly cursosFiltrados = computed(() => {
    const periodo = this.periodoSeleccionado();
    if (periodo === 'todos') return this.cursosMatriculados();
    if (periodo === 'actual') return this.cursosActuales();
    return this.cursosMatriculados().filter((curso) => curso.cicloAcademicoNombre === periodo);
  });

  /** Cantidad de cursos activos del periodo actual. */
  readonly cursosActivos = computed(() => this.cursosActuales().length);

  /** Columnas de la tabla de cursos. */
  readonly cursosColumns = ['curso', 'seccion', 'ciclo', 'estado', 'acciones'] as const;

  // ─── Detalle de notas ──────────────────────────────────────────

  /** Columnas de la tabla de evaluaciones. */
  readonly notasColumns = ['nombre', 'porcentaje', 'nota'] as const;

  /** Query de evaluaciones de la sección seleccionada. */
  readonly evaluacionesQuery = useEvaluacionesBySeccionEstudianteQuery(this.idSeccion);

  /** Query de notas del estudiante en la sección seleccionada. */
  readonly notasQuery = useMisNotasBySeccionQuery(this.idSeccion);

  /** Evaluaciones de la sección. */
  readonly evaluaciones = computed(() => this.evaluacionesQuery.data() ?? []);

  /** Notas del estudiante en la sección, indexadas por idEvaluacion. */
  readonly notasPorEvaluacion = computed(() => {
    const notas = this.notasQuery.data() ?? [];
    const map: Record<number, number | null> = {};
    for (const n of notas) {
      if (n.idEvaluacion != null) {
        map[n.idEvaluacion] = n.nota;
      }
    }
    return map;
  });

  /** Evaluaciones combinadas con su nota correspondiente. */
  readonly evaluacionesConNota = computed<EvaluacionConNota[]>(() =>
    this.evaluaciones().map((e) => ({
      idEvaluacion: e.idEvaluacion,
      nombre: e.nombre,
      porcentaje: e.porcentaje,
      nota: this.notasPorEvaluacion()[e.idEvaluacion] ?? null,
    })),
  );

  /** Curso seleccionado de la lista. */
  readonly cursoSeleccionado = computed(
    () => this.cursosMatriculados().find((c) => c.idSeccion === this.idSeccion()) ?? null,
  );

  readonly contenido = signal<CursoContenidoResponse[]>([]);
  readonly contenidoLoading = signal(false);
  readonly contenidoError = signal('');

  readonly contenidoPorSemana = computed(() =>
    Array.from({ length: 18 }, (_, index) => index + 1)
      .map((semana) => ({
        semana,
        archivos: this.contenido().filter((archivo) => archivo.semana === semana),
      }))
      .filter((grupo) => grupo.archivos.length > 0),
  );

  constructor() {
    effect(() => {
      if (this.activeTab() === 'contenido' && this.idSeccion()) {
        void this.cargarContenido(this.idSeccion()!);
      }
    });
  }

  // ─── Promedio ponderado ────────────────────────────────────────

  /** Promedio ponderado calculado en base a notas y porcentajes. */
  readonly promedio = computed(() => {
    const items = this.evaluacionesConNota();
    if (items.length === 0) return null;

    let sumaPonderada = 0;
    let totalPorcentaje = 0;

    for (const item of items) {
      if (item.nota !== null) {
        sumaPonderada += item.nota * (item.porcentaje / 100);
        totalPorcentaje += item.porcentaje;
      }
    }

    if (totalPorcentaje === 0) return null;
    return Math.round((sumaPonderada / (totalPorcentaje / 100)) * 100) / 100;
  });

  // ─── Estados compartidos ───────────────────────────────────────

  /** Indica si hay datos cargando. */
  readonly isLoading = computed(() => {
    if (this.isDetailView()) {
      if (this.activeTab() === 'contenido') return this.contenidoLoading();
      return this.evaluacionesQuery.isPending() || this.notasQuery.isPending();
    }
    return this.misCursosQuery.isPending();
  });

  /** Indica si hay error. */
  readonly isError = computed(() => {
    if (this.isDetailView()) {
      if (this.activeTab() === 'contenido') return !!this.contenidoError();
      return this.evaluacionesQuery.isError();
    }
    return this.misCursosQuery.isError();
  });

  // ─── Navegación ───────────────────────────────────────────────

  /** Navega a la vista de notas de un curso. */
  verNotas(idSeccion: number): void {
    void this.router.navigate(['/app/estudiante/mis-cursos', idSeccion, 'notas']);
  }

  verContenido(): void {
    void this.router.navigate(['/app/estudiante/mis-cursos', this.idSeccion(), 'contenido']);
  }

  onPeriodoChange(event: Event): void {
    this.periodoSeleccionado.set((event.target as HTMLSelectElement).value);
  }

  private async cargarContenido(idSeccion: number): Promise<void> {
    this.contenidoLoading.set(true);
    this.contenidoError.set('');
    try {
      this.contenido.set(await this.contenidoService.listarPorSeccion(idSeccion));
    } catch {
      this.contenidoError.set('No se pudo cargar el contenido del curso.');
    } finally {
      this.contenidoLoading.set(false);
    }
  }

  /** Vuelve a la lista de cursos. */
  volverALista(): void {
    void this.router.navigate(['/app/estudiante/mis-cursos']);
  }

  /** Estado badge según estado de matrícula. */
  estadoClass(estado: string): string {
    switch (estado) {
      case 'ACTIVA':
        return 'bg-success/10 text-success border-success/20';
      case 'RETIRADA':
        return 'bg-surface-hover text-text-secondary border-border';
      case 'APROBADA':
        return 'bg-info/10 text-info border-info/20';
      case 'DESAPROBADA':
        return 'bg-error/10 text-error border-error/20';
      default:
        return 'bg-surface-hover text-text-secondary border-border';
    }
  }

  /** Retorna la clase CSS según el valor de nota. */
  notaColorClass(nota: number | null): string {
    if (nota === null) return 'text-text-secondary';
    if (nota >= 13) return 'text-success font-bold';
    if (nota >= 10.5) return 'text-warning font-bold';
    return 'text-error font-bold';
  }

  /** Ancho de la barra de progreso del promedio (0-100%). */
  promedioBarWidth(promedio: number): number {
    return Math.min((promedio / 20) * 100, 100);
  }
}
