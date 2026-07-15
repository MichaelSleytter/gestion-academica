import { inject, type Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import type { SeccionCreateRequest } from '../models/seccion/seccion.request';
import type { SeccionResponse } from '../models/seccion/seccion.response';
import type { PageResponse } from '../models/shared/page.response';
import { SeccionService } from '../core/services/seccion.service';
import {
  SECCION_ACTUALIZAR_MUTATION_KEY,
  SECCION_CREAR_MUTATION_KEY,
  SECCION_ELIMINAR_MUTATION_KEY,
  SECCION_KEY,
  SECCIONES_KEY,
  SECCIONES_PAGINADOS_KEY,
} from './query-keys';

interface CrearSeccionVariables {
  seccion: SeccionCreateRequest;
  idCurso: number;
  idCiclo: number;
}

interface ActualizarSeccionVariables {
  id: number;
  seccion: SeccionCreateRequest;
  idCurso: number;
  idCiclo: number;
}

/**
 * Hook para obtener secciones con paginación, búsqueda y filtro por ciclo académico.
 *
 * @param pagina      signal con el número de página (0-based)
 * @param tamaño      signal con elementos por página
 * @param busqueda    signal con el texto de búsqueda
 * @param idCiclo     signal con el ID del ciclo académico (opcional)
 * @param enabled     signal que habilita la query (opcional, default true)
 * @returns query con PageResponse<SeccionResponse>
 */
export function useSeccionesPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
  idCiclo?: Signal<number | null>,
  enabled?: Signal<boolean>,
) {
  const service = inject(SeccionService);

  return injectQuery<PageResponse<SeccionResponse>, Error>(() => ({
    queryKey: SECCIONES_PAGINADOS_KEY(pagina(), tamaño(), busqueda(), idCiclo?.() ?? undefined),
    queryFn: () =>
      service.getSeccionesPaginado(pagina(), tamaño(), busqueda(), idCiclo?.() ?? undefined),
    enabled: enabled?.() ?? true,
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

export function useSeccionQuery(idSeccion: Signal<number>) {
  const service = inject(SeccionService);

  return injectQuery<SeccionResponse, Error>(() => ({
    queryKey: SECCION_KEY(idSeccion()),
    queryFn: () => service.getSeccionById(idSeccion()),
    enabled: idSeccion() > 0,
    staleTime: 1000 * 30,
    retry: 1,
  }));
}

/**
 * Mutación para crear una sección.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useCrearSeccionMutation() {
  const service = inject(SeccionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: SECCION_CREAR_MUTATION_KEY,
    mutationFn: ({ seccion, idCurso, idCiclo }: CrearSeccionVariables) =>
      service.crearSeccion(seccion, idCurso, idCiclo),
    onSuccess: async (seccionCreada) => {
      queryClient.setQueryData(SECCION_KEY(seccionCreada.idSeccion), seccionCreada);
      await queryClient.invalidateQueries({ queryKey: SECCIONES_KEY });
    },
  }));
}

/**
 * Mutación para actualizar una sección existente.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useActualizarSeccionMutation() {
  const service = inject(SeccionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: SECCION_ACTUALIZAR_MUTATION_KEY,
    mutationFn: ({ id, seccion, idCurso, idCiclo }: ActualizarSeccionVariables) =>
      service.actualizarSeccion(id, seccion, idCurso, idCiclo),
    onSuccess: async (seccionActualizada, variables) => {
      queryClient.setQueryData(SECCION_KEY(variables.id), seccionActualizada);
      await queryClient.invalidateQueries({ queryKey: SECCIONES_KEY });
    },
  }));
}

/**
 * Mutación para eliminar una sección por ID.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useEliminarSeccionMutation() {
  const service = inject(SeccionService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: SECCION_ELIMINAR_MUTATION_KEY,
    mutationFn: (id: number) => service.eliminarSeccion(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: SECCION_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: SECCIONES_KEY });
    },
  }));
}
