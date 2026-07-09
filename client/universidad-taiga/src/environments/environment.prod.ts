/**
 * Configuración del entorno de producción.
 *
 * @description
 * Reemplaza a `environment.ts` durante el build de producción.
 * Usa el proxy same-origin de Vercel para evitar problemas de cookies
 * cross-origin con el backend en Railway.
 *
 * ## Proxy same-origin
 *
 * El frontend se despliega en Vercel y el backend en Railway.
 * Para que las cookies HttpOnly del refresh token funcionen, el frontend
 * llama a `/api/v1/*` (mismo origen) y Vercel reescribe a Railway mediante
 * `vercel.json`.
 *
 * ## Variables
 *
 * - `production: true` — activa optimizaciones de prod.
 * - `apiBaseUrl` — ruta relativa `/api/v1` que Vercel proxy al backend.
 *
 * @see environment.ts - Configuración para desarrollo local.
 * @see vercel.json - Reglas de rewrite del proxy same-origin.
 */
export const environment = {
  production: true,

  /**
   * URL base de la API REST.
   * Producción: ruta relativa `/api/v1` que Vercel redirige a Railway.
   * Desarrollo: `http://localhost:8080/api/v1` (directo).
   */
  apiBaseUrl: '/api/v1',
};
