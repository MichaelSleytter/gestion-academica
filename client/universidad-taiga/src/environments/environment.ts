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
   * Desarrollo: ruta relativa proxy por Angular dev server a `localhost:8080`.
   * Producción: ruta relativa proxy por Vercel a Railway.
   *
   * @see proxy.conf.json - Configuración del proxy de desarrollo.
   * @see vercel.json - Configuración del proxy de producción.
   */
  apiBaseUrl: '/api/v1',
};
