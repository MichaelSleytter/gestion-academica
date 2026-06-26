import { inject } from '@angular/core';
import { Router, UrlTree, CanActivateFn, ActivatedRouteSnapshot } from '@angular/router';
import { toObservable } from '@angular/core/rxjs-interop';
import { filter, map, take, timeout, catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../services/token.service';
import { RoleService } from '../services/role.service';
import type { AuthStatus } from '../../models/auth.model';

/**
 * =============================================================================
 * AUTH GUARD - Protege rutas que requieren autenticación
 * =============================================================================
 *
 * Usa el `authStatus` signal del AuthService para decidir:
 *   - 'authenticated': permite el acceso inmediatamente
 *   - 'loading': espera a que termine la verificación con el backend
 *                 (refresh token cookie) antes de decidir
 *   - 'unauthenticated': redirige al login
 *
 * Esto resuelve el problema de recarga de página cuando el access token
 * expiró pero el refresh token cookie sigue vivo: el guard espera a que
 * AuthService intente el refresh antes de redirigir a login.
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
 * =============================================================================
 * GUARD 2: requiresRole
 * =============================================================================
 *
 * Protección por roles: requiere estar autenticado Y tener rol específico.
 *
 * @param allowedRoles - Roles que pueden acceder (OR logic: cualquiera de ellos)
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
 */
function checkRoles(
  router: Router,
  route: ActivatedRouteSnapshot,
  roleService: RoleService,
  allowedRoles: string[],
): boolean | UrlTree {
  const userRoles = roleService.getRoles();
  const hasRequiredRole = allowedRoles.some(role => userRoles.includes(role));

  if (hasRequiredRole) {
    return true;
  }

  console.warn(
    `RoleGuard: User lacks required roles. ` +
    `Has: ${userRoles.join(', ')}, Needs: ${allowedRoles.join(', ')}`
  );

  return router.createUrlTree(['/access-denied']);
}


/**
 * =============================================================================
 * GUARD 3: requiresNoAuth
 * =============================================================================
 *
 * Protección inversa: solo permite acceder si NO está autenticado.
 * Útil para rutas como /login y /register.
 */
export const requiresNoAuth: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isAuthenticated()) {
    return router.createUrlTree(['/app/dashboard']);
  }

  return true;
};


// ─── Helpers ──────────────────────────────────────────────────────────────

/**
 * Crea un UrlTree de redirección a /login preservando la URL original.
 */
function redirectToLogin(router: Router, route?: ActivatedRouteSnapshot) {
  const returnUrl = route?.url.map(s => s.path).join('/') || '/';
  return router.createUrlTree(['/login'], { queryParams: { returnUrl } });
}
