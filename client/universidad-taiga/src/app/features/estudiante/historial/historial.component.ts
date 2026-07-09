import { ChangeDetectionStrategy, Component, computed } from '@angular/core';
import { TuiIcon, TuiLoader } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import type {
  CursoProgreso,
  EstadoCursoProgreso,
  HistorialProgresoResponse,
} from '../../../models/historial';
import { useMiHistorialProgresoQuery } from '../../../queries/historial-progreso.query';

interface CicloCursos {
  ciclo: number;
  cursos: CursoProgreso[];
}

interface StatusView {
  label: string;
  className: string;
}

/**
 * Vista del historial académico del estudiante.
 *
 * Muestra el progreso completo de carrera calculado por backend:
 * créditos, promedio ponderado, avance, materias y correlativas.
 *
 * Accesible para: ESTUDIANTE.
 */
@Component({
  selector: 'app-historial',
  imports: [TuiCardLarge, TuiHeader, TuiIcon, TuiLoader],
  templateUrl: './historial.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Historial {
  /** Query with authenticated student's academic progress. */
  readonly historialQuery = useMiHistorialProgresoQuery();

  /** Academic progress response ready for the template. */
  readonly progreso = computed(() => this.historialQuery.data() ?? null);

  /** Summary metrics. */
  readonly resumen = computed(() => this.progreso()?.resumen ?? null);

  /** Courses grouped by recommended cycle. */
  readonly ciclos = computed<CicloCursos[]>(() =>
    this.groupCoursesByCycle(this.progreso()?.cursos ?? []),
  );

  /** True when the API returned no curriculum courses. */
  readonly hasNoCourses = computed(() => (this.progreso()?.cursos.length ?? 0) === 0);

  /** Human-readable API error. */
  readonly errorMessage = computed(
    () => this.historialQuery.error()?.message ?? 'No se pudo cargar tu historial académico.',
  );

  completionStyle(progreso: HistorialProgresoResponse): string {
    const value = Math.min(Math.max(progreso.resumen.porcentajeAvance ?? 0, 0), 100);
    return `width: ${value}%`;
  }

  formatNumber(value: number | null | undefined, digits = 2): string {
    if (value === null || value === undefined || Number.isNaN(value)) return '—';
    return new Intl.NumberFormat('es-PE', {
      minimumFractionDigits: digits,
      maximumFractionDigits: digits,
    }).format(value);
  }

  statusView(estado: EstadoCursoProgreso): StatusView {
    switch (estado) {
      case 'PASSED':
        return {
          label: 'Aprobado',
          className: 'bg-green-100 text-green-800 border-green-200',
        };
      case 'IN_PROGRESS':
        return {
          label: 'Cursando',
          className: 'bg-blue-100 text-blue-800 border-blue-200',
        };
      case 'PENDING_AVAILABLE':
        return {
          label: 'Disponible',
          className: 'bg-surface-hover text-text-secondary border-border',
        };
      case 'PENDING_BLOCKED':
        return {
          label: 'Bloqueado',
          className: 'bg-amber-100 text-amber-800 border-amber-200',
        };
      case 'FAILED':
        return {
          label: 'Desaprobado',
          className: 'bg-red-100 text-red-800 border-red-200',
        };
    }
  }

  trackCycle(_index: number, ciclo: CicloCursos): number {
    return ciclo.ciclo;
  }

  trackCourse(_index: number, curso: CursoProgreso): number {
    return curso.cursoId;
  }

  private groupCoursesByCycle(cursos: CursoProgreso[]): CicloCursos[] {
    const cycles = new Map<number, CursoProgreso[]>();

    for (const curso of cursos) {
      const ciclo = curso.cicloRecomendado ?? 0;
      const current = cycles.get(ciclo) ?? [];
      current.push(curso);
      cycles.set(ciclo, current);
    }

    return [...cycles.entries()]
      .sort(([a], [b]) => a - b)
      .map(([ciclo, cycleCourses]) => ({
        ciclo,
        cursos: [...cycleCourses].sort((a: CursoProgreso, b: CursoProgreso) =>
          a.nombre.localeCompare(b.nombre, 'es'),
        ),
      }));
  }
}
