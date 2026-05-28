import { inject } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import {
  EstudianteCreateRequest,
  EstudianteUpdateRequest,
} from '../models/estudiante/estudiante.request';
import { EstudianteService } from '../services/estudiante-service';
import {
  ESTUDIANTE_ACTUALIZAR_MUTATION_KEY,
  ESTUDIANTE_CREAR_MUTATION_KEY,
  ESTUDIANTE_ELIMINAR_MUTATION_KEY,
  ESTUDIANTE_KEY,
  ESTUDIANTES_KEY,
} from './query-keys';

interface ActualizarEstudianteVariables {
  id: number;
  estudiante: EstudianteUpdateRequest;
}

/**
 * Hook reutilizable para obtener la lista de estudiantes usando TanStack Query.
 *
 * - Separa la lógica de red en EstudianteService.
 * - Devuelve el objeto de query con data(), isPending(), error(), etc.
 *
 * @returns Resultado de la query de estudiantes
 */
export function useEstudiantesQuery() {
  const service = inject(EstudianteService);

  /*
   * queryKey: Clave única para la query, usada para cache y invalidación.
   * queryFn: Función que realiza la petición al servicio.
   * staleTime: Tiempo en ms antes de considerar la data como "stale" (que no está actualizada) y refrescar.
   * cacheTime: Tiempo en ms que la data se mantiene en caché después de que no haya suscriptores.
   * retry: Número de veces que se reintenta la petición en caso de error.
   */
  return injectQuery(() => ({
    queryKey: ESTUDIANTES_KEY,
    queryFn: () => service.getEstudiantes(),
    staleTime: 1000 * 30, // 30 segundos
    cacheTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Mutación para crear estudiante.
 *
 * - Ejecuta POST contra API usando EstudianteService.
 * - Invalida lista de estudiantes para refrescar cache.
 * - Actualiza cache puntual del estudiante creado por id.
 *
 * @returns Resultado de mutación con `mutate` y `mutateAsync`
 */
export function useCrearEstudianteMutation() {
  const service = inject(EstudianteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ESTUDIANTE_CREAR_MUTATION_KEY,
    mutationFn: (estudiante: EstudianteCreateRequest) => service.crearEstudiante(estudiante),
    onSuccess: async (estudianteCreado) => {
      queryClient.setQueryData(ESTUDIANTE_KEY(estudianteCreado.idUsuario), estudianteCreado);
      await queryClient.invalidateQueries({ queryKey: ESTUDIANTES_KEY });
    },
  }));
}

/**
 * Mutación para actualizar estudiante existente.
 *
 * - Ejecuta PUT usando id + payload.
 * - Sincroniza cache de detalle por id.
 * - Invalida lista para reflejar cambios globalmente.
 *
 * @returns Resultado de mutación con `mutate` y `mutateAsync`
 */
export function useActualizarEstudianteMutation() {
  const service = inject(EstudianteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ESTUDIANTE_ACTUALIZAR_MUTATION_KEY,
    mutationFn: ({ id, estudiante }: ActualizarEstudianteVariables) =>
      service.actualizarEstudiante(id, estudiante),
    onSuccess: async (estudianteActualizado, variables) => {
      queryClient.setQueryData(ESTUDIANTE_KEY(variables.id), estudianteActualizado);
      await queryClient.invalidateQueries({ queryKey: ESTUDIANTES_KEY });
    },
  }));
}

/**
 * Mutación para eliminar estudiante por id.
 *
 * - Ejecuta DELETE contra API.
 * - Remueve cache de detalle eliminada.
 * - Invalida lista para actualizar tabla/tarjetas.
 *
 * @returns Resultado de mutación con `mutate` y `mutateAsync`
 */
export function useEliminarEstudianteMutation() {
  const service = inject(EstudianteService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationKey: ESTUDIANTE_ELIMINAR_MUTATION_KEY,
    mutationFn: (id: number) => service.eliminarEstudiante(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: ESTUDIANTE_KEY(id) });
      await queryClient.invalidateQueries({ queryKey: ESTUDIANTES_KEY });
    },
  }));
}
