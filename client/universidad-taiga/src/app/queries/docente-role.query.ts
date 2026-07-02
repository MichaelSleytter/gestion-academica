import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { DocenteRoleService } from '../core/services/docente-role.service';
import type { SeccionResponse } from '../models/seccion/seccion.response';
import type { EvaluacionResponse } from '../models/evaluacion/evaluacion.response';
import type { MatriculaResponse } from '../models/matricula';
import type { NotaRequest, NotaResponse } from '../models/nota';
import {
  DOCENTE_SECCIONES_KEY,
  EVALUACIONES_BY_SECCION_KEY,
  MATRICULAS_BY_SECCION_KEY,
  NOTAS_BY_EVALUACION_KEY,
} from './query-keys';

interface SaveNotaVariables {
  idNota?: number;
  idEvaluacion: number;
  idEstudiante: number;
  nota: NotaRequest;
}

/**
 * Query de secciones asignadas al docente autenticado.
 *
 * @param idDocente - Signal con el ID del docente.
 * @returns Query con las secciones del docente.
 */
export function useDocenteSeccionesQuery(idDocente: Signal<number | null>) {
  const service = inject(DocenteRoleService);

  return injectQuery<SeccionResponse[], Error>(() => ({
    queryKey: DOCENTE_SECCIONES_KEY(idDocente()),
    queryFn: () => service.getSeccionesByDocente(idDocente() ?? 0),
    enabled: idDocente() !== null,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Query de evaluaciones por sección.
 *
 * @param idSeccion - Signal con el ID de la sección.
 * @returns Query con evaluaciones de la sección.
 */
export function useEvaluacionesBySeccionQuery(idSeccion: Signal<number | null>) {
  const service = inject(DocenteRoleService);

  return injectQuery<EvaluacionResponse[], Error>(() => ({
    queryKey: EVALUACIONES_BY_SECCION_KEY(idSeccion()),
    queryFn: () => service.getEvaluacionesBySeccion(idSeccion() ?? 0),
    enabled: idSeccion() !== null,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Query de matrículas por sección.
 *
 * @param idSeccion - Signal con el ID de la sección.
 * @returns Query con matrículas de la sección.
 */
export function useMatriculasBySeccionQuery(idSeccion: Signal<number | null>) {
  const service = inject(DocenteRoleService);

  return injectQuery<MatriculaResponse[], Error>(() => ({
    queryKey: MATRICULAS_BY_SECCION_KEY(idSeccion()),
    queryFn: () => service.getMatriculasBySeccion(idSeccion() ?? 0),
    enabled: idSeccion() !== null,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Query de notas por evaluación.
 *
 * @param idEvaluacion - Signal con el ID de la evaluación seleccionada.
 * @returns Query con notas de la evaluación.
 */
export function useNotasByEvaluacionQuery(idEvaluacion: Signal<number | null>) {
  const service = inject(DocenteRoleService);

  return injectQuery<NotaResponse[], Error>(() => ({
    queryKey: NOTAS_BY_EVALUACION_KEY(idEvaluacion()),
    queryFn: () => service.getNotasByEvaluacion(idEvaluacion() ?? 0),
    enabled: idEvaluacion() !== null,
    staleTime: 1000 * 10,
    retry: 1,
  }));
}

/**
 * Mutación para crear o actualizar notas.
 *
 * @returns Mutación de guardado de nota.
 */
export function useSaveNotaMutation() {
  const service = inject(DocenteRoleService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationFn: ({ idNota, nota, idEvaluacion, idEstudiante }: SaveNotaVariables) =>
      idNota
        ? service.updateNota(idNota, nota, idEvaluacion, idEstudiante)
        : service.createNota(nota, idEvaluacion, idEstudiante),
    onSuccess: async (_nota, variables) => {
      await queryClient.invalidateQueries({ queryKey: NOTAS_BY_EVALUACION_KEY(variables.idEvaluacion) });
    },
  }));
}
