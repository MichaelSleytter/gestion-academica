import { inject, Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import {
  EstudianteCreateRequest,
  EstudianteUpdateRequest,
} from '../models/estudiante/estudiante.request';
import { EstudianteResponse } from '../models/estudiante/estudiante.response';
import { PageResponse } from '../models/shared/page.response';
import { EstudianteService } from '../core/services/estudiante.service';
import {
  ESTUDIANTE_ACTUALIZAR_MUTATION_KEY,
  ESTUDIANTE_CREAR_MUTATION_KEY,
  ESTUDIANTE_ELIMINAR_MUTATION_KEY,
  ESTUDIANTE_KEY,
  ESTUDIANTES_KEY,
  ESTUDIANTES_PAGINADOS_KEY,
} from './query-keys';

interface ActualizarEstudianteVariables {
  id: number;
  estudiante: EstudianteUpdateRequest;
}

/**
 * Hook reutilizable para obtener la lista completa de estudiantes.
 *
 * @returns Resultado de la query de estudiantes
 */
export function useEstudiantesQuery() {
  const service = inject(EstudianteService);

  return injectQuery(() => ({
    queryKey: ESTUDIANTES_KEY,
    queryFn: () => service.getEstudiantes(),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
    retry: 1,
  }));
}

/**
 * Hook para obtener estudiantes con paginación y búsqueda opcional.
 *
 * Recibe señales (signal) en lugar de valores para que TanStack Query
 * se reactive automáticamente cuando cambien página, tamaño o búsqueda.
 *
 * @param pagina   signal con el número de página (0-based)
 * @param tamaño   signal con elementos por página
 * @param busqueda signal con el texto de búsqueda (opcional)
 * @returns query con PageResponse<EstudianteResponse>
 */
export function useEstudiantesPaginadosQuery(
  pagina: Signal<number>,
  tamaño: Signal<number>,
  busqueda: Signal<string>,
) {
  const service = inject(EstudianteService);

  return injectQuery<PageResponse<EstudianteResponse>, Error>(() => ({
    queryKey: ESTUDIANTES_PAGINADOS_KEY(pagina(), tamaño(), busqueda()),
    queryFn: () => service.getEstudiantesPaginado(pagina(), tamaño(), busqueda()),
    staleTime: 1000 * 30,
    gcTime: 1000 * 60 * 5,
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
