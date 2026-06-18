import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';

/**
 * =============================================================================
 * AUTH INTERCEPTOR - Agrega el token JWT a todas las requests
 * =============================================================================
 * 
 * ¿QUÉ HACE?
 * Intercepta TODAS las requests HTTP salientes y:
 * 1. Agrega el header Authorization: Bearer <token>
 * 2. Agrega withCredentials: true (para enviar cookies)
 * 
 * 
 * ¿POR QUÉ UN INTERCEPTOR?
 * Sin interceptor, tendríamos que agregar el header manualmente
 * en cada llamada HTTP:
 * 
 * ❌ ANTES (sin interceptor):
 * this.http.get('/api/usuarios', {
 *   headers: { Authorization: 'Bearer ' + this.token }
 * })
 * 
 * ✅ DESPUÉS (con interceptor):
 * this.http.get('/api/usuarios')  // El interceptor lo hace solo
 * 
 * 
 * MANEJO DE 401 (Unauthorized)
 * ----------------------------
 * Si el backend retorna 401, significa que el token expiró o es inválido.
 * Intentamos hacer refresh automáticamente:
 * 
 * 1. Request falla con 401
 * 2. Interceptor captura el error
 * 3. Intentamos refresh con el refresh token (en cookie)
 * 4. Si refresh succeeds → reintentamos el request original
 * 5. Si refresh fails → redirigimos a login
 * 
 * 
 * ESTRUCTURA DEL INTERCEPTOR
 * ---------------------------
 * Los interceptores en Angular son "functional interceptors" (desde v17+).
 * Son funciones puras que reciben request ynext, y retornan un observable.
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
  const publicUrlPatterns = [
    '/auth/login',
    '/auth/register',
    '/auth/refresh',
  ];

  // Verificar si la URL actual es pública
  const isPublicUrl = publicUrlPatterns.some(pattern => req.url.includes(pattern));

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
        Authorization: `Bearer ${token}`
      },
      // Importante: para requests que van a APIs con cookies
      // (como el refresh), necesitamos withCredentials
      withCredentials: true
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
          switchMap(newTokenResponse => {
            console.log('Interceptor: Token refreshed, retrying original request');
            
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newTokenResponse.accessToken}`
              },
              withCredentials: true
            });
            
            // Reintentar la request original con el nuevo token
            return next(retryReq);
          }),
          
          // ===================================================================
          // SI EL REFRESH FALLA
          // ===================================================================
          catchError(refreshError => {
            console.error('Interceptor: Token refresh failed:', refreshError);
            
            // Limpiar sesión y redirigir a login
            authService.logout();
            
            // Retornar el error para que el componente lo maneje si quiere
            return throwError(() => refreshError);
          })
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
    })
  );
};