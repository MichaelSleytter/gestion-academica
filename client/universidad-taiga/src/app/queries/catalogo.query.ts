import { inject } from '@angular/core';
import { CatalogoService } from '../core/services/catalogo.service';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { CATALOGO_CARRERAS_KEY, CATALOGO_TIPOS_DOCUMENTO_KEY } from './query-keys';

export const useTiposDocumentoQuery = () => {
  const service = inject(CatalogoService);

  return injectQuery(() => ({
    queryKey: CATALOGO_TIPOS_DOCUMENTO_KEY,
    queryFn: () => service.getTipoDocumento(),
    staleTime: 1000 * 30, // 30 segundos
    cacheTime: 1000 * 60 * 5,
    retry: 1,
  }));
};

/**
 * Query para carreras del catálogo
 */
export const useCarrerasQuery = () => {
  const service = inject(CatalogoService);

  return injectQuery(() => ({
    queryKey: CATALOGO_CARRERAS_KEY,
    queryFn: () => service.getCarreras(),
    staleTime: 1000 * 60 * 5, // 5 minutos
    cacheTime: 1000 * 60 * 10,
    retry: 1,
  }));
};
