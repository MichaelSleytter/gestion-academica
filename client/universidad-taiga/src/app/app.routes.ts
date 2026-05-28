/**
 * =============================================================================
 * CONFIGURACIÓN DE RUTAS CON GUARDS DE AUTENTICACIÓN
 * =============================================================================
 *
 * Estructura de rutas basada en los componentes existentes del proyecto.
 * Los guards verifican autenticación y roles antes de permitir acceso.
 *
 * ROLES:
 *   - ADMIN:     Acceso completo al sistema
 *   - DOCENTE:   Gestión de cursos, estudiantes y notas
 *   - ESTUDIANTE: Visualización de cursos, notas e historial
 */

import { Routes } from '@angular/router';
import { requiresAuth, requiresRole, requiresNoAuth } from './guards/auth.guard';

/**
 * =============================================================================
 * DEFINICIÓN DE RUTAS
 * =============================================================================
 */
export const routes: Routes = [
  // =========================================================================
  // RUTAS PÚBLICAS
  // =========================================================================

  /**
   * Página de Login - Ruta por defecto
   * Solo accesible si NO está autenticado.
   */
  {
    path: '',
    loadComponent: () => import('./components/shared/login/login.component').then((m) => m.LoginComponent),
    canActivate: [requiresNoAuth],
  },

  {
    path: 'login',
    loadComponent: () => import('./components/shared/login/login.component').then((m) => m.LoginComponent),
    canActivate: [requiresNoAuth],
  },

  /**
   * Página Olvidé mi contraseña
   * Solo accesible si NO está autenticado.
   */
  {
    path: 'forgot-password',
    loadComponent: () => import('./components/shared/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent),
    canActivate: [requiresNoAuth],
  },

  /**
   * Página 404 - Not Found
   */
  {
    path: 'page404',
    loadComponent: () => import('./components/shared/page404/page404').then((m) => m.Page404),
  },

  // =========================================================================
  // RUTAS PROTEGIDAS (requieren autenticación) - CON LAYOUT
  // =========================================================================

  /**
   * Layout principal con sidebar y header.
   * Todas las rutas hijas requieren autenticación.
   * Cada ruta hija tiene su propio guard de roles donde corresponda.
   */
  {
    path: 'app',
    loadComponent: () => import('./components/shared/layout/layout').then((m) => m.AppLayout),
    canActivate: [requiresAuth],
    canActivateChild: [requiresAuth],
    children: [
      // ─── Dashboard (SOLO ADMIN) ───────────────────────────────────────
      {
        path: 'dashboard',
        loadComponent: () => import('./components/admin/dashboard/dashboard').then((m) => m.Dashboard),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── Estudiantes (ADMIN + DOCENTE) ────────────────────────────────
      {
        path: 'estudiantes',
        loadComponent: () => import('./components/admin/estudiantes/estudiantes').then((m) => m.Estudiantes),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },
      {
        path: 'estudiante/:id',
        loadComponent: () => import('./components/admin/estudiantes/estudiantes').then((m) => m.Estudiantes),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Docentes (SOLO ADMIN) ────────────────────────────────────────
      {
        path: 'docentes',
        loadComponent: () => import('./components/admin/docentes/docentes').then((m) => m.Docentes),
        canActivate: [requiresRole('ADMIN')],
      },
      {
        path: 'docente/:id',
        loadComponent: () => import('./components/admin/docentes/docentes').then((m) => m.Docentes),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── Cursos ───────────────────────────────────────────────────────
      {
        path: 'cursos',
        loadComponent: () => import('./components/admin/cursos/cursos').then((m) => m.Cursos),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },
      {
        path: 'cursos/:id',
        loadComponent: () => import('./components/admin/cursos/cursos').then((m) => m.Cursos),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Secciones (ADMIN + DOCENTE) ──────────────────────────────────
      {
        path: 'secciones',
        loadComponent: () => import('./components/admin/secciones/secciones').then((m) => m.Secciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },
      {
        path: 'secciones/:id',
        loadComponent: () => import('./components/admin/secciones/secciones').then((m) => m.Secciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Horarios (ADMIN + DOCENTE) ───────────────────────────────────
      {
        path: 'horarios',
        loadComponent: () => import('./components/admin/horarios/horarios').then((m) => m.Horarios),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Evaluaciones (ADMIN + DOCENTE) ───────────────────────────────
      {
        path: 'evaluaciones',
        loadComponent: () => import('./components/admin/evaluaciones/evaluaciones').then((m) => m.Evaluaciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── DOCENTE: Mis cursos (SOLO DOCENTE) ───────────────────────────
      {
        path: 'docente/mis-cursos',
        loadComponent: () => import('./components/role-docente/mis-cursos/mis-cursos').then((m) => m.MisCursos),
        canActivate: [requiresRole('DOCENTE')],
      },
      {
        path: 'docente/mis-cursos/:id/notas',
        loadComponent: () => import('./components/role-docente/carga-notas/carga-notas').then((m) => m.CargaNotas),
        canActivate: [requiresRole('DOCENTE')],
      },

      // ─── ESTUDIANTE: Mis cursos (SOLO ESTUDIANTE) ────────────────────
      {
        path: 'estudiante/mis-cursos',
        loadComponent: () => import('./components/role-estudiante/mis-notas/mis-notas').then((m) => m.MisNotas),
        canActivate: [requiresRole('ESTUDIANTE')],
      },
      {
        path: 'estudiante/mis-cursos/:id/notas',
        loadComponent: () => import('./components/role-estudiante/mis-notas/mis-notas').then((m) => m.MisNotas),
        canActivate: [requiresRole('ESTUDIANTE')],
      },

      // ─── ESTUDIANTE: Historial (SOLO ESTUDIANTE) ─────────────────────
      {
        path: 'estudiante/historial',
        loadComponent: () => import('./components/role-estudiante/historial/historial').then((m) => m.Historial),
        canActivate: [requiresRole('ESTUDIANTE')],
      },

      // ─── Perfil (TODOS los roles) ────────────────────────────────────
      {
        path: 'perfil',
        loadComponent: () => import('./components/shared/perfil/perfil').then((m) => m.Perfil),
      },

      // ─── Default: el layout redirige al home según el rol ────────────
    ]
  },

  // Redirects de rutas antiguas al layout
  {
    path: 'dashboard',
    redirectTo: 'app/dashboard',
  },
  {
    path: 'estudiantes',
    redirectTo: 'app/estudiantes',
  },
  {
    path: 'docentes',
    redirectTo: 'app/docentes',
  },
  {
    path: 'cursos',
    redirectTo: 'app/cursos',
  },
  {
    path: 'secciones',
    redirectTo: 'app/secciones',
  },

  // =========================================================================
  // RUTA WILDCARD (404)
  // =========================================================================

  {
    path: '**',
    loadComponent: () => import('./components/shared/page404/page404').then((m) => m.Page404),
  },
];

/**
 * =============================================================================
 * NOTAS
 * =============================================================================
 *
 * ESTRUCTURA DE RUTAS:
 *
 * /                          → Dashboard (autenticado)
 * /login                     → Login (solo NO autenticados)
 * /page404                   → Página 404
 *
 * /estudiantes               → Gestión de estudiantes
 * /estudiante/:id            → Detalle de estudiante
 *
 * /docentes                  → Gestión de docentes (ADMIN/DOCENTE)
 * /docente/:id               → Detalle de docente (ADMIN/DOCENTE)
 *
 * /cursos                    → Gestión de cursos
 * /cursos/:id                → Detalle de curso
 *
 * /admin                     → Panel admin (ADMIN only) - descomenta cuando exista
 *
 *
 * GUARDS DISPONIBLES:
 *
 * requiresAuth         - Verifica que esté autenticado
 * requiresRole(...)    - Verifica que tenga el rol específico
 * requiresNoAuth       - Solo para NO autenticados (login/register)
 *
 *
 * CÓMO AGREGAR NUEVAS RUTAS:
 *
 * {
 *   path: 'nombre-ruta',
 *   loadComponent: () => import('./components/mi-componente/mi-componente').then(m => m.MiComponente),
 *   canActivate: [requiresAuth]  // o el guard que necesites
 * }
 */
