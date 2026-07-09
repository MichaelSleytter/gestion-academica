import { inject } from '@angular/core';
import {
  Router,
  type UrlTree,
  type CanActivateFn,
  type ActivatedRouteSnapshot,
} from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take, timeout, catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { RoleService } from '../services/role.service';
import type { AuthStatus } from '../../models/auth.model';

/**
 * Guard de ruta que requiere que el usuario esté autenticado.
 *
 * @description
 * Evalúa el `authStatus` del `TokenService` para decidir:
 * - `'authenticated'` → permite el acceso inmediatamente.
 * - `'loading'` → espera hasta 8 segundos a que la verificación termine
 *   (útil en recarga de página cuando aún se está renovando el token).
 * - `'unauthenticated'` → redirige a `/login?returnUrl=<ruta>`.
 *
 * Resuelve el problema de recarga de página: cuando el access token expiró
 * pero la cookie HttpOnly del refresh token sigue viva, el guard espera
 * a que `TokenService` complete la verificación antes de decidir.
 *
 * @param route - Ruta a la que se intenta acceder (se usa para construir el returnUrl).
 * @returns `true` si está autenticado, un `UrlTree` de redirección a `/login` si no.
 */
export const requiresAuth: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  const status = tokenService.authStatus();

  // ─── Autenticado → permitir ──────────────────────────────────────────
  if (status === 'authenticated') {
    return true;
  }

  // ─── Cargando (refresh en progreso) → esperar ────────────────────────
  if (status === 'loading') {
    return toObservable(tokenService.authStatus).pipe(
      filter((s: AuthStatus) => s !== 'loading'),
      take(1),
      timeout(8000),
      map((s: AuthStatus) => {
        if (s === 'authenticated') return true;
        return redirectToLogin(router, route);
      }),
      catchError(() => of(redirectToLogin(router, route))),
    );
  }

  // ─── No autenticado → redirigir a login ──────────────────────────────
  return redirectToLogin(router, route);
};

/**
 * Guard de ruta que requiere uno o más roles específicos además de autenticación.
 *
 * @description
 * - Primero verifica autenticación (igual que `requiresAuth`).
 * - Luego verifica que el usuario tenga al menos uno de los roles indicados.
 * - Si no tiene el rol requerido, redirige a `/access-denied`.
 * - También soporta el estado `'loading'` esperando hasta 8 segundos.
 *
 * @param allowedRoles - Lista de nombres de rol permitidos (OR lógico).
 * @returns Una función `CanActivateFn` lista para usar en la configuración de rutas.
 */
export const requiresRole = (...allowedRoles: string[]): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const tokenService = inject(TokenService);
    const roleService = inject(RoleService);
    const router = inject(Router);

    const status = tokenService.authStatus();

    // ─── Esperar si está cargando ──────────────────────────────────
    if (status === 'loading') {
      return toObservable(tokenService.authStatus).pipe(
        filter((s: AuthStatus) => s !== 'loading'),
        take(1),
        timeout(8000),
        switchMap((s: AuthStatus) => {
          if (s !== 'authenticated') return of(redirectToLogin(router, route));
          return of(checkRoles(router, route, roleService, allowedRoles));
        }),
        catchError(() => of(redirectToLogin(router, route))),
      );
    }

    // ─── No autenticado → redirigir ────────────────────────────────
    if (status !== 'authenticated') {
      return redirectToLogin(router, route);
    }

    // ─── Autenticado → verificar roles ─────────────────────────────
    return checkRoles(router, route, roleService, allowedRoles);
  };
};

/**
 * Verifica si el usuario tiene al menos uno de los roles permitidos.
 * Si no tiene el rol necesario, redirige a `/access-denied`.
 *
 * @param router - Router de Angular para crear el UrlTree de redirección.
 * @param route - Ruta actual (se usa para logging de advertencia).
 * @param roleService - Servicio que expone los roles del usuario autenticado.
 * @param allowedRoles - Lista de roles que tienen acceso permitido.
 * @returns `true` si tiene acceso, o un `UrlTree` a `/access-denied`.
 */
function checkRoles(
  router: Router,
  route: ActivatedRouteSnapshot,
  roleService: RoleService,
  allowedRoles: string[],
): boolean | UrlTree {
  const userRoles = roleService.getRoles();
  const hasRequiredRole = allowedRoles.some((role) => userRoles.includes(role));

  if (hasRequiredRole) {
    return true;
  }

  console.warn(
    `RoleGuard: User lacks required roles. ` +
      `Has: ${userRoles.join(', ')}, Needs: ${allowedRoles.join(', ')}`,
  );

  return router.createUrlTree(['/access-denied']);
}

/**
 * Guard de ruta inverso: solo permite acceder si el usuario NO está autenticado.
 *
 * @description
 * Útil para rutas públicas como `/login`, `/register`, `/forgot-password`,
 * `/reset-password`. Si el usuario ya está autenticado, redirige a su home
 * según el rol usando `RoleService.getHomeRouteByRole()`.
 *
 * @returns `true` si no está autenticado, un `UrlTree` al home del rol si ya lo está.
 */
export const requiresNoAuth: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const roleService = inject(RoleService);
  const router = inject(Router);

  if (tokenService.isAuthenticated()) {
    return router.createUrlTree([roleService.getHomeRouteByRole()]);
  }

  return true;
};

// ─── Helpers ──────────────────────────────────────────────────────────────

/**
 * Construye una redirección a `/login` preservando la ruta original como `returnUrl`.
 *
 * @param router - Router de Angular para crear el UrlTree.
 * @param route - Ruta original que se intentaba acceder (opcional).
 * @returns UrlTree apuntando a `/login?returnUrl=<ruta>`.
 */
function redirectToLogin(router: Router, route?: ActivatedRouteSnapshot) {
  const returnUrl = route?.url.map((s) => s.path).join('/') || '/';
  return router.createUrlTree(['/login'], { queryParams: { returnUrl } });
}
