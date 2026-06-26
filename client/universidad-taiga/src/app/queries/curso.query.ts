import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { CursoCreateRequest } from '../models/curso/curso.request';
import { CursoResponse } from '../models/curso/curso.response';
import { PageResponse } from '../models/shared/page.response';
import { CursoService } from '../core/services/curso.service';
import {
  CURSO_ACTUALIZAR_MUTATION_KEY,
  CURSO_CREAR_MUTATION_KEY,
  CURSO_ELIMINAR_MUTATION_KEY,
  CURSO_KEY,
  CURSOS_KEY,
  CURSOS_PAGINADOS_KEY,
} from './query-keys';

interface ActualizarCursoVariables {
  id: number;
  curso: CursoCreateRequest;
}

/**
 * Hook para obtener cursos con paginación y búsqueda opcional.
 *
 * @param pagina   signal con el número de página (0-based)
 * @param tamaño   signal con elementos por página
 * @param busqueda signal con el texto de búsqueda
 * @returns query con PageResponse<CursoResponse>
 */
export function useCursosPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
) {
  const service = inject(CursoService);

  return injectQuery<PageResponse<CursoResponse>, Error>(() => ({
    queryKey: CURSOS_PAGINADOS_KEY(pagina(), tamaño(), busqueda()),
    queryFn: () => service.getCursosPaginado(pagina(), tamaño(), busqueda()),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Mutación para crear un curso.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useCrearCursoMutation() {
  const service = inject(CursoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: CURSO_CREAR_MUTATION_KEY,
    mutationFn: (curso: CursoCreateRequest) => service.crearCurso(curso),
    onSuccess: async (cursoCreado) => {
      queryClient.setQueryData(CURSO_KEY(cursoCreado.idCurso), cursoCreado);
      await queryClient.invalidateQueries({ queryKey: CURSOS_KEY });
    },
  }));
}

/**
 * Mutación para actualizar un curso existente.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useActualizarCursoMutation() {
  const service = inject(CursoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: CURSO_ACTUALIZAR_MUTATION_KEY,
    mutationFn: ({ id, curso }: ActualizarCursoVariables) =>
      service.actualizarCurso(id, curso),
    onSuccess: async (cursoActualizado, variables) => {
      queryClient.setQueryData(CURSO_KEY(variables.id), cursoActualizado);
      await queryClient.invalidateQueries({ queryKey: CURSOS_KEY });
    },
  }));
}

/**
 * Mutación para eliminar un curso por ID.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useEliminarCursoMutation() {
  const service = inject(CursoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: CURSO_ELIMINAR_MUTATION_KEY,
    mutationFn: (id: number) => service.eliminarCurso(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: CURSO_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: CURSOS_KEY });
    },
  }));
}
