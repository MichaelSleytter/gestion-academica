import { inject, type Signal } from '@angular/core';
import { injectMutation, injectQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { CatalogoService } from '../core/services/catalogo.service';
import type {
  CatalogKind,
  CatalogNameItem,
  CatalogNameRequest,
  CicloAcademico,
  GenerarCiclosAnioRequest,
} from '../models/catalogos/catalogo.response';
import {
  CATALOGO_CARRERAS_KEY,
  CATALOGO_CICLOS_KEY,
  CATALOGO_ESPECIALIZACIONES_KEY,
  CATALOGO_GRADOS_KEY,
} from './query-keys';

export function catalogQueryKey(kind: CatalogKind) {
  switch (kind) {
    case 'carreras':
      return CATALOGO_CARRERAS_KEY;
    case 'grados-academicos':
      return CATALOGO_GRADOS_KEY;
    case 'especializaciones':
      return CATALOGO_ESPECIALIZACIONES_KEY;
    case 'ciclos':
      return CATALOGO_CICLOS_KEY;
  }
}

export function useCatalogItemsQuery(kind: Signal<CatalogKind>) {
  const service = inject(CatalogoService);

  return injectQuery<CatalogNameItem[] | CicloAcademico[], Error>(() => ({
    queryKey: catalogQueryKey(kind()),
    queryFn: () => service.getCatalogItems(kind()),
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
    retry: 1,
  }));
}

export function useCreateCatalogItemMutation(kind: Signal<Exclude<CatalogKind, 'ciclos'>>) {
  const service = inject(CatalogoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationFn: (request: CatalogNameRequest) => service.createCatalogItem(kind(), request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: catalogQueryKey(kind()) });
    },
  }));
}

export interface UpdateCatalogItemVariables {
  id: number;
  request: CatalogNameRequest;
}

export function useUpdateCatalogItemMutation(kind: Signal<Exclude<CatalogKind, 'ciclos'>>) {
  const service = inject(CatalogoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationFn: ({ id, request }: UpdateCatalogItemVariables) =>
      service.updateCatalogItem(kind(), id, request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: catalogQueryKey(kind()) });
    },
  }));
}

export function useDeleteCatalogItemMutation(kind: Signal<Exclude<CatalogKind, 'ciclos'>>) {
  const service = inject(CatalogoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationFn: (id: number) => service.deleteCatalogItem(kind(), id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: catalogQueryKey(kind()) });
    },
  }));
}

export function useGenerateAcademicYearMutation() {
  const service = inject(CatalogoService);
  const queryClient = inject(QueryClient);

  return injectMutation(() => ({
    mutationFn: (request: GenerarCiclosAnioRequest) => service.generarCiclosAcademicos(request),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: CATALOGO_CICLOS_KEY });
    },
  }));
}
