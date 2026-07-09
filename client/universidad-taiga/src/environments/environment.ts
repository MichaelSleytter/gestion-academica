/**
 * Configuración del entorno de desarrollo.
 *
 * @description
 * Angular reemplaza este archivo por `environment.prod.ts` durante el build
 * de producción (`ng build --configuration production`).
 *
 * ## Variables
 *
 * - `production: false` — desactiva optimizaciones de prod.
 * - `apiBaseUrl` — apunta al backend local en `localhost:8080`.
 *   En desarrollo las cookies se envían a origen cruzado (localhost:4200 → localhost:8080).
 *
 * @see environment.prod.ts - Configuración para producción (proxy same-origin).
 */
export const environment = {
  production: false,

  /**
   * URL base de la API REST.
   * Desarrollo: apunta directo al backend local.
   * Producción: se reemplaza por ruta relativa `/api/v1` (Vercel proxy).
   */
  apiBaseUrl: 'http://localhost:8080/api/v1',
};
