import { inject } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { CatalogoService } from '../core/services/catalogo.service';
import type { Especializacion } from '../models/catalogos/catalogo.response';
import { CATALOGO_ESPECIALIZACIONES_KEY } from './query-keys';
import {
  useCatalogItemsQuery,
  useCreateCatalogItemMutation,
  useDeleteCatalogItemMutation,
  useUpdateCatalogItemMutation,
} from './catalog-crud.query';

export function useEspecializacionesQuery() {
  const service = inject(CatalogoService);

  return injectQuery<Especializacion[], Error>(() => ({
    queryKey: CATALOGO_ESPECIALIZACIONES_KEY,
    queryFn: () => service.getEspecializaciones(),
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    retry: 1,
  }));
}

export {
  useCatalogItemsQuery as useEspecializacionesCatalogQuery,
  useCreateCatalogItemMutation as useCreateEspecializacionMutation,
  useDeleteCatalogItemMutation as useDeleteEspecializacionMutation,
  useUpdateCatalogItemMutation as useUpdateEspecializacionMutation,
};
