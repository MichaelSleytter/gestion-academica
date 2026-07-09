import type { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

/**
 * Interceptor HTTP que agrega el token JWT a todas las solicitudes salientes
 * y maneja automáticamente la renovación del token cuando expira.
 *
 * @description
 * - Agrega el header `Authorization: Bearer <token>` a cada request.
 * - Configura `withCredentials: true` para envío de cookies.
 * - Captura errores 401 e intenta renovar el token automáticamente mediante
 *   `AuthService.refresh()`.
 * - Omite URLs públicas (`/auth/login`, `/auth/register`, `/auth/refresh`)
 *   para evitar dependencias circulares con `AuthService`.
 * - Los errores 403 y otros códigos se propagan sin modificar.
 *
 * ## Flujo de renovación automática
 *
 * 1. El request original falla con 401 (token expirado o inválido).
 * 2. El interceptor captura el error y llama a `AuthService.refresh()`.
 * 3. Si el refresh es exitoso, se clona el request original con el nuevo token.
 * 4. Si el refresh falla, se limpia la sesión y se redirige a `/login`.
 * 5. Si el request original YA es de refresh, no se reintenta (evita loops).
 *
 * ## Dependencia circular evitada
 *
 * `AuthService` usa `HttpClient`, que pasa por este interceptor.
 * Si el interceptor inyectara `AuthService` en URLs públicas, se produciría
 * una dependencia circular (`NG0200`). Por eso las URLs públicas se dejan
 * pasar sin modificar.
 *
 * @param req - Solicitud HTTP saliente (inmutable).
 * @param next - Handler que envía la solicitud al siguiente nivel en la cadena.
 * @returns Observable con la respuesta HTTP, o un error propagado/transformado.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // ===========================================================================
  // PASO 1: Verificar si la URL es pública ANTES de inyectar AuthService
  // ===========================================================================
  //
  // Esto es CRÍTICO para evitar la dependencia circular:
  //
  //   AuthService (constructor)
  //     → this.http.post('/auth/refresh')    ← se llama en refreshSession()
  //       → authInterceptor se ejecuta
  //         → inject(AuthService)            ← AuthService todavía construyéndose
  //           → NG0200 💥
  //
  // Solución: para URLs públicas (/auth/login, /auth/refresh, /auth/register)
  // retornamos next(req) inmediatamente SIN inyectar AuthService.
  //
  // Para el resto de URLs, AuthService ya está completamente construido
  // cuando se hace cualquier llamada HTTP posterior.

  /** URLs públicas que no requieren token de autenticación. */
  const publicUrlPatterns = ['/auth/login', '/auth/register', '/auth/refresh'];

  // Verificar si la URL actual es pública
  const isPublicUrl = publicUrlPatterns.some((pattern) => req.url.includes(pattern));

  // Si es URL pública, continuar sin modificar (sin inyectar AuthService)
  if (isPublicUrl) {
    return next(req);
  }

  // ===========================================================================
  // PASO 2: Inyectar servicios (seguro porque AuthService ya existe)
  // ===========================================================================

  const authService = inject(AuthService);
  const tokenService = inject(TokenService);
  const router = inject(Router);

  // ===========================================================================
  // PASO 3: AGREGAR TOKEN A LA REQUEST
  // ===========================================================================

  const token = tokenService.getToken();

  // Clonar la request con los headers nuevos
  let authReq = req;

  if (token) {
    // Clonar request con header Authorization (las requests Angular son inmutables)
    authReq = req.clone({
      setHeaders: {
        // Formato estándar: "Bearer " + token
        // El backend busca este formato exacto
        Authorization: `Bearer ${token}`,
      },
      // Importante: para requests que van a APIs con cookies
      // (como el refresh), necesitamos withCredentials
      withCredentials: true,
    });
  }

  // ===========================================================================
  // CONTINUAR CON LA REQUEST Y MANEJAR ERRORES
  // ===========================================================================

  return next(authReq).pipe(
    // ===========================================================================
    // CAPTURAR ERRORES HTTP
    // ===========================================================================
    catchError((error: HttpErrorResponse) => {
      // =======================================================================
      // MANEJAR 401 - TOKEN EXPIRADO O INVÁLIDO
      // =======================================================================
      if (error.status === 401) {
        console.log('Interceptor: Received 401, attempting token refresh...');

        /**
         * IMPORTANTE: No reintentar si la request original era de refresh.
         * Si el endpoint de refresh retorna 401, significa que el refresh token
         * también expiró/revocado. No tiene sentido intentar refresh de nuevo.
         */
        if (req.url.includes('/auth/refresh')) {
          console.log('Interceptor: Refresh endpoint returned 401, redirecting to login');
          authService.logout();
          return throwError(() => error);
        }

        // =====================================================================
        // INTENTAR REFRESH AUTOMÁTICO
        // =====================================================================
        /**
         * switchMap cancela el observable anterior y cambia a uno nuevo.
         *
         * ¿POR QUÉ switchMap y no mergeMap/concatMap?
         * - mergeMap: ejecuta todos en paralelo (no queremos eso)
         * - concatMap: espera uno antes de empezar otro (puede causar deadlocks)
         * - switchMap: cancela el anterior si llega uno nuevo
         *
         * En este caso, switchMap es ideal porque:
         * - Si llega otra request mientras esperamos el refresh, la cancelamos
         * - Solo nos importa el resultado del refresh más reciente
         */
        return authService.refresh().pipe(
          /**
           * switchMap dentro del pipe:
           * Después de obtener el nuevo token, reintentamos la request original.
           *
           * authReq ya tiene el token viejo.
           * Creamos una nueva request con el token actualizado.
           */
          switchMap((newTokenResponse) => {
            console.log('Interceptor: Token refreshed, retrying original request');

            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newTokenResponse.accessToken}`,
              },
              withCredentials: true,
            });

            // Reintentar la request original con el nuevo token
            return next(retryReq);
          }),

          // ===================================================================
          // SI EL REFRESH FALLA
          // ===================================================================
          catchError((refreshError) => {
            console.error('Interceptor: Token refresh failed:', refreshError);

            // Limpiar sesión y redirigir a login
            authService.logout();

            // Retornar el error para que el componente lo maneje si quiere
            return throwError(() => refreshError);
          }),
        );
      }

      // =======================================================================
      // MANEJAR 403 - SIN PERMISOS
      // =======================================================================
      if (error.status === 403) {
        console.warn('Interceptor: Access forbidden (403)');
        // Podrías mostrar un modal de "no tienes permisos" aquí
        // Por ahora solo continuamos con el error
      }

      // =======================================================================
      // OTROS ERRORES - propagar sin modificar
      // =======================================================================
      return throwError(() => error);
    }),
  );
};
