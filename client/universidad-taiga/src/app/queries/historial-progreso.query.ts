import { inject, type Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { HistorialProgresoService } from '../core/services/historial-progreso.service';
import type { HistorialProgresoResponse } from '../models/historial';
import { HISTORIAL_PROGRESO_ESTUDIANTE_KEY, HISTORIAL_PROGRESO_ME_KEY } from './query-keys';

/** Query for the authenticated student's academic progress. */
export function useMiHistorialProgresoQuery() {
  const service = inject(HistorialProgresoService);

  return injectQuery<HistorialProgresoResponse, Error>(() => ({
    queryKey: HISTORIAL_PROGRESO_ME_KEY,
    queryFn: () => service.getMiProgreso(),
    staleTime: 1000 * 60,
    retry: 1,
  }));
}

/** Query for an authorized student academic progress lookup. */
export function useHistorialProgresoEstudianteQuery(idEstudiante: Signal<number | null>) {
  const service = inject(HistorialProgresoService);

  return injectQuery<HistorialProgresoResponse, Error>(() => ({
    queryKey: HISTORIAL_PROGRESO_ESTUDIANTE_KEY(idEstudiante()),
    queryFn: () => service.getProgresoEstudiante(idEstudiante() ?? 0),
    enabled: (idEstudiante() ?? 0) > 0,
    staleTime: 1000 * 60,
    retry: 1,
  }));
}
