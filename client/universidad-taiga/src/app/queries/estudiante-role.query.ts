import { inject, type Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import {
  EstudianteRoleService,
  type CursoMatriculadoResponse,
} from '../core/services/estudiante-role.service';
import type { EvaluacionResponse } from '../models/evaluacion/evaluacion.response';
import type { HorarioResponse } from '../models/horario/horario.response';
import type { NotaConEvaluacionResponse } from '../core/services/estudiante-role.service';
import { isCurrentAcademicPeriod } from '../shared/utils/academic-period';
import { EVALUACIONES_BY_SECCION_KEY } from './query-keys';

/**
 * Query de cursos matriculados del estudiante autenticado.
 *
 * @returns Query con los cursos en los que está matriculado.
 */
export function useMisCursosQuery() {
  const service = inject(EstudianteRoleService);

  return injectQuery<CursoMatriculadoResponse[], Error>(() => ({
    queryKey: ['estudiante', 'mis-cursos'],
    queryFn: () => service.getMisCursos(),
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Query de evaluaciones por sección (reutilizable para estudiante).
 *
 * @param idSeccion - Signal con el ID de la sección.
 * @returns Query con evaluaciones de la sección.
 */
export function useEvaluacionesBySeccionEstudianteQuery(idSeccion: Signal<number | null>) {
  const service = inject(EstudianteRoleService);

  return injectQuery<EvaluacionResponse[], Error>(() => ({
    queryKey: EVALUACIONES_BY_SECCION_KEY(idSeccion()),
    queryFn: () => service.getEvaluacionesBySeccion(idSeccion() ?? 0),
    enabled: idSeccion() !== null && (idSeccion() ?? 0) > 0,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Query de notas del estudiante autenticado para todas las evaluaciones
 * de una sección.
 *
 * @param idSeccion - Signal con el ID de la sección.
 * @returns Query con las notas del estudiante en la sección.
 */
export function useMisNotasBySeccionQuery(idSeccion: Signal<number | null>) {
  const service = inject(EstudianteRoleService);

  return injectQuery<NotaConEvaluacionResponse[], Error>(() => ({
    queryKey: ['notas', 'mis-notas', 'seccion', idSeccion()],
    queryFn: () => service.getMisNotasBySeccion(idSeccion() ?? 0),
    enabled: idSeccion() !== null && (idSeccion() ?? 0) > 0,
    staleTime: 1000 * 10,
    retry: 1,
  }));
}

export function useMiHorarioQuery() {
  const service = inject(EstudianteRoleService);

  return injectQuery<HorarioResponse[], Error>(() => ({
    queryKey: ['estudiante', 'mi-horario'],
    queryFn: async () => {
      const cursos = await service.getMisCursos();
      const seccionesActivas = cursos.filter(
        (curso) => curso.estado === 'ACTIVA' && isCurrentAcademicPeriod(curso),
      );
      const horarios = await Promise.all(
        seccionesActivas.map((curso) => service.getHorariosBySeccion(curso.idSeccion)),
      );

      return horarios.flat();
    },
    staleTime: 1000 * 30,
    retry: 1,
  }));
}
