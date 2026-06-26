import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { EvaluacionCreateRequest } from '../models/evaluacion/evaluacion.request';
import { EvaluacionResponse } from '../models/evaluacion/evaluacion.response';
import { PageResponse } from '../models/shared/page.response';
import { EvaluacionService } from '../core/services/evaluacion.service';
import {
  EVALUACION_ACTUALIZAR_MUTATION_KEY,
  EVALUACION_KEY,
  EVALUACION_CREAR_MUTATION_KEY,
  EVALUACION_ELIMINAR_MUTATION_KEY,
  EVALUACIONES_KEY,
  EVALUACIONES_PAGINADOS_KEY,
} from './query-keys';

interface CrearEvaluacionVariables {
  evaluacion: EvaluacionCreateRequest;
  idSeccion: number;
}

interface ActualizarEvaluacionVariables {
  id: number;
  evaluacion: EvaluacionCreateRequest;
  idSeccion: number;
}

/**
 * Hook para obtener evaluaciones con paginación y búsqueda opcional.
 *
 * @param pagina   signal con el número de página (0-based)
 * @param tamaño   signal con elementos por página
 * @param busqueda signal con el texto de búsqueda
 * @returns query con PageResponse<EvaluacionResponse>
 */
export function useEvaluacionesPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
) {
  const service = inject(EvaluacionService);

  return injectQuery<PageResponse<EvaluacionResponse>, Error>(() => ({
    queryKey: EVALUACIONES_PAGINADOS_KEY(pagina(), tamaño(), busqueda()),
    queryFn: () => service.getEvaluacionesPaginado(pagina(), tamaño(), busqueda()),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Mutación para crear una evaluación.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useCrearEvaluacionMutation() {
  const service = inject(EvaluacionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: EVALUACION_CREAR_MUTATION_KEY,
    mutationFn: ({ evaluacion, idSeccion }: CrearEvaluacionVariables) =>
      service.crearEvaluacion(evaluacion, idSeccion),
    onSuccess: async (evaluacionCreada) => {
      queryClient.setQueryData(EVALUACION_KEY(evaluacionCreada.idEvaluacion), evaluacionCreada);
      await queryClient.invalidateQueries({ queryKey: EVALUACIONES_KEY });
    },
  }));
}

/**
 * Mutación para actualizar una evaluación existente.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useActualizarEvaluacionMutation() {
  const service = inject(EvaluacionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: EVALUACION_ACTUALIZAR_MUTATION_KEY,
    mutationFn: ({ id, evaluacion, idSeccion }: ActualizarEvaluacionVariables) =>
      service.actualizarEvaluacion(id, evaluacion, idSeccion),
    onSuccess: async (evaluacionActualizada, variables) => {
      queryClient.setQueryData(EVALUACION_KEY(variables.id), evaluacionActualizada);
      await queryClient.invalidateQueries({ queryKey: EVALUACIONES_KEY });
    },
  }));
}

/**
 * Mutación para eliminar una evaluación por ID.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useEliminarEvaluacionMutation() {
  const service = inject(EvaluacionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: EVALUACION_ELIMINAR_MUTATION_KEY,
    mutationFn: (id: number) => service.eliminarEvaluacion(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: EVALUACION_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: EVALUACIONES_KEY });
    },
  }));
}
