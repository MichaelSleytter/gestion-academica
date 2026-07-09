import { inject, type Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { MatriculaService } from '../core/services/matricula.service';
import type { MatriculaResponse } from '../models/matricula';
import {
  MATRICULAS_BY_SECCION_KEY,
  MATRICULAR_MUTATION_KEY,
  RETIRAR_MUTATION_KEY,
} from './query-keys';

interface MatricularVariables {
  idEstudiante: number;
  idSeccion: number;
}

/**
 * Query de matrículas por sección.
 *
 * @param idSeccion - Signal con el ID de la sección.
 * @returns Query con las matrículas de la sección.
 */
export function useMatriculasBySeccionQuery(idSeccion: Signal<number>) {
  const service = inject(MatriculaService);

  return injectQuery<MatriculaResponse[], Error>(() => ({
    queryKey: MATRICULAS_BY_SECCION_KEY(idSeccion()),
    queryFn: () => service.getMatriculasBySeccion(idSeccion()),
    enabled: idSeccion() > 0,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Mutación para matricular un estudiante en una sección.
 *
 * @returns Mutación con `mutate`
 */
export function useMatricularMutation() {
  const service = inject(MatriculaService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: MATRICULAR_MUTATION_KEY,
    mutationFn: ({ idEstudiante, idSeccion }: MatricularVariables) =>
      service.matricular(idEstudiante, idSeccion),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: MATRICULAS_BY_SECCION_KEY(variables.idSeccion),
      });
    },
  }));
}

/**
 * Mutación para retirar (cambiar estado a RETIRADA) una matrícula.
 *
 * @returns Mutación con `mutate`
 */
export function useRetirarMutation() {
  const service = inject(MatriculaService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: RETIRAR_MUTATION_KEY,
    mutationFn: (idMatricula: number) => service.cambiarEstado(idMatricula, 'RETIRADA'),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['secciones'] });
    },
  }));
}
