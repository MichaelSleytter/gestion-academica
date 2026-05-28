/**
 * =============================================================================
 * APP CONFIG - Configuración de la aplicación Angular
 * =============================================================================
 *
 * Este archivo configura los providers globales de Angular.
 * Incluye:
 * - HTTP Client con interceptors
 * - Router con guards
 * - TanStack Query
 * - Cualquier otro provider global
 */

import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideTaiga } from '@taiga-ui/core';
import { provideIcons } from '@ng-icons/core';
import {
  tablerLayoutDashboard,
  tablerSchool,
  tablerUsers,
  tablerBook,
  tablerClipboardList,
  tablerLayoutGrid,
  tablerStackBack,
  tablerSettings,
  tablerLogout,
  tablerMenu,
  tablerSearch,
  tablerBell,
  tablerUser,
} from '@ng-icons/tabler-icons';
import { provideTanStackQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { APP_API_URL } from './tokens/api.tokens';
import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { authInterceptor } from './services/auth.interceptor';

/**
 * QueryClient con opciones por defecto para la app.
 * - staleTime: cuánto tiempo consideramos los datos "frescos"
 * - retry: cuántas veces reintentar una consulta fallida
 * - refetchOnWindowFocus: si queremos que se vuelvan a cargar los datos al volver a la pestaña
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60, // 1 minuto
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

/**
 * =============================================================================
 * CONFIGURACIÓN DE LA APLICACIÓN
 * =============================================================================
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),

    // =======================================================================
    // API URL
    // =======================================================================
    { provide: APP_API_URL, useValue: environment.apiBaseUrl },

    // =======================================================================
    // HTTP CLIENT CON INTERCEPTORS
    // =======================================================================
    provideHttpClient(
      withInterceptors([authInterceptor]),
    ),

    // =======================================================================
    // ZONE CHANGE DETECTION
    // =======================================================================
    provideZoneChangeDetection({ eventCoalescing: true }),

    // =======================================================================
    // TANSTACK QUERY
    // =======================================================================
    provideTanStackQuery(queryClient),

    // =======================================================================
    // ANIMATIONS
    // =======================================================================
    provideAnimations(),

    // =======================================================================
    // TAIGA UI
    // =======================================================================
    provideTaiga(),

    // =======================================================================
    // NG ICONS (Tabler Icons)
    // =======================================================================
    provideIcons({
      tablerLayoutDashboard,
      tablerSchool,
      tablerUsers,
      tablerBook,
      tablerClipboardList,
      tablerLayoutGrid,
      tablerStackBack,
      tablerSettings,
      tablerLogout,
      tablerMenu,
      tablerSearch,
      tablerBell,
      tablerUser,
    }),
  ],
};
