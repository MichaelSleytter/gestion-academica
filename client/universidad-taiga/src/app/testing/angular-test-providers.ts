import type { EnvironmentProviders, Provider } from '@angular/core';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter, type Routes } from '@angular/router';
import { provideTaiga } from '@taiga-ui/core';
import { provideTanStackQuery, QueryClient } from '@tanstack/angular-query-experimental';
import type { PageResponse } from '../models/shared/page.response';

export function provideAngularComponentTest(
  routes: Routes = [],
): Array<EnvironmentProviders | Provider> {
  installMatchMediaMock();

  return [provideRouter(routes), provideNoopAnimations(), provideTaiga()];
}

export function provideQueryTestClient(): Provider[] {
  return provideTanStackQuery(
    new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
        mutations: {
          retry: false,
        },
      },
    }),
  );
}

export function emptyPage<T>(): PageResponse<T> {
  return {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 10,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: true,
  };
}

function installMatchMediaMock(): void {
  if (!globalThis.window || typeof globalThis.window.matchMedia === 'function') {
    return;
  }

  Object.defineProperty(globalThis.window, 'matchMedia', {
    writable: true,
    value: (query: string): MediaQueryList => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: () => {},
      removeListener: () => {},
      addEventListener: () => {},
      removeEventListener: () => {},
      dispatchEvent: () => false,
    }),
  });
}
