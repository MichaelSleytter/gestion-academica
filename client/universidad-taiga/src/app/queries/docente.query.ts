import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { DocenteResponse } from '../models/docente/docente.response';
import { DocenteCreateRequest, DocenteUpdateRequest } from '../models/docente/docente.request';
import { PageResponse } from '../models/shared/page.response';
import { DocenteService } from '../core/services/docente.service';
import {
  DOCENTE_KEY,
  DOCENTES_KEY,
  DOCENTES_PAGINADOS_KEY,
} from './query-keys';

/**
 * Hook para obtener docentes con paginación y búsqueda opcional.
 *
 * Recibe señales para que TanStack Query se reactive automáticamente
 * cuando cambien página, tamaño o búsqueda.
 *
 * @param pagina   signal con el número de página (0-based)
 * @param tamaño   signal con elementos por página
 * @param busqueda signal con el texto de búsqueda (opcional)
 * @returns query con PageResponse<DocenteResponse>
 */
export function useDocentesPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
) {
  const service = inject(DocenteService);

  return injectQuery<PageResponse<DocenteResponse>, Error>(() => ({
    queryKey: DOCENTES_PAGINADOS_KEY(pagina(), tamaño(), busqueda()),
    queryFn: () => service.getDocentesPaginado(pagina(), tamaño(), busqueda()),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Mutación para crear un docente.
 */
export function useCrearDocenteMutation() {
  const service = inject(DocenteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ['docentes', 'crear'] as const,
    mutationFn: (docente: DocenteCreateRequest) => service.crearDocente(docente),
    onSuccess: async (docenteCreado) => {
      queryClient.setQueryData(DOCENTE_KEY(docenteCreado.idUsuario), docenteCreado);
      await queryClient.invalidateQueries({ queryKey: DOCENTES_KEY });
    },
  }));
}

interface ActualizarDocenteVariables {
  id: number;
  docente: DocenteUpdateRequest;
}

/**
 * Mutación para actualizar un docente existente.
 */
export function useActualizarDocenteMutation() {
  const service = inject(DocenteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ['docentes', 'actualizar'] as const,
    mutationFn: ({ id, docente }: ActualizarDocenteVariables) =>
      service.actualizarDocente(id, docente),
    onSuccess: async (docenteActualizado, variables) => {
      queryClient.setQueryData(DOCENTE_KEY(variables.id), docenteActualizado);
      await queryClient.invalidateQueries({ queryKey: DOCENTES_KEY });
    },
  }));
}

/**
 * Mutación para eliminar un docente por ID.
 */
export function useEliminarDocenteMutation() {
  const service = inject(DocenteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ['docentes', 'eliminar'] as const,
    mutationFn: (id: number) => service.eliminarDocente(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: DOCENTE_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: DOCENTES_KEY });
    },
  }));
}
