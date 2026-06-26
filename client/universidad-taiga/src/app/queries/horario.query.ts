import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { HorarioCreateRequest } from '../models/horario/horario.request';
import { HorarioResponse } from '../models/horario/horario.response';
import { PageResponse } from '../models/shared/page.response';
import { HorarioService } from '../core/services/horario.service';
import {
  HORARIO_ACTUALIZAR_MUTATION_KEY,
  HORARIO_KEY,
  HORARIO_CREAR_MUTATION_KEY,
  HORARIO_ELIMINAR_MUTATION_KEY,
  HORARIOS_KEY,
  HORARIOS_PAGINADOS_KEY,
} from './query-keys';

interface CrearHorarioVariables {
  horario: HorarioCreateRequest;
  idSeccion: number;
}

interface ActualizarHorarioVariables {
  id: number;
  horario: HorarioCreateRequest;
  idSeccion: number;
}

/**
 * Hook para obtener horarios con paginación y búsqueda opcional.
 *
 * @param pagina   signal con el número de página (0-based)
 * @param tamaño   signal con elementos por página
 * @param busqueda signal con el texto de búsqueda
 * @returns query con PageResponse<HorarioResponse>
 */
export function useHorariosPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
) {
  const service = inject(HorarioService);

  return injectQuery<PageResponse<HorarioResponse>, Error>(() => ({
    queryKey: HORARIOS_PAGINADOS_KEY(pagina(), tamaño(), busqueda()),
    queryFn: () => service.getHorariosPaginado(pagina(), tamaño(), busqueda()),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Mutación para crear un horario.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useCrearHorarioMutation() {
  const service = inject(HorarioService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: HORARIO_CREAR_MUTATION_KEY,
    mutationFn: ({ horario, idSeccion }: CrearHorarioVariables) =>
      service.crearHorario(horario, idSeccion),
    onSuccess: async (horarioCreado) => {
      queryClient.setQueryData(HORARIO_KEY(horarioCreado.idHorario), horarioCreado);
      await queryClient.invalidateQueries({ queryKey: HORARIOS_KEY });
    },
  }));
}

/**
 * Mutación para actualizar un horario existente.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useActualizarHorarioMutation() {
  const service = inject(HorarioService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: HORARIO_ACTUALIZAR_MUTATION_KEY,
    mutationFn: ({ id, horario, idSeccion }: ActualizarHorarioVariables) =>
      service.actualizarHorario(id, horario, idSeccion),
    onSuccess: async (horarioActualizado, variables) => {
      queryClient.setQueryData(HORARIO_KEY(variables.id), horarioActualizado);
      await queryClient.invalidateQueries({ queryKey: HORARIOS_KEY });
    },
  }));
}

/**
 * Mutación para eliminar un horario por ID.
 *
 * @returns mutación con `mutate` y `mutateAsync`
 */
export function useEliminarHorarioMutation() {
  const service = inject(HorarioService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: HORARIO_ELIMINAR_MUTATION_KEY,
    mutationFn: (id: number) => service.eliminarHorario(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: HORARIO_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: HORARIOS_KEY });
    },
  }));
}
