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
import { requiresAuth, requiresRole, requiresNoAuth } from './core/guards/auth.guard';

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
   * Ruta raíz — redirige al login.
   * El guard requiresNoAuth redirige a /app/dashboard si ya está autenticado.
   */
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },

  /**
   * Página de Login
   * Solo accesible si NO está autenticado.
   */
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
    canActivate: [requiresNoAuth],
  },

  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent),
    canActivate: [requiresNoAuth],
  },

  /**
   * Página Restablecer contraseña
   * Accesible con o sin autenticación (el token decide).
   */
  {
    path: 'reset-password',
    loadComponent: () => import('./features/auth/reset-password/reset-password.component').then((m) => m.ResetPasswordComponent),
  },

  {
    path: 'page404',
    loadComponent: () => import('./shared/components/page404/page404.component').then((m) => m.Page404),
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
    loadComponent: () => import('./shared/components/layout/layout.component').then((m) => m.AppLayout),
    canActivate: [requiresAuth],
    canActivateChild: [requiresAuth],
    children: [
      // ─── Dashboard (SOLO ADMIN) ───────────────────────────────────────
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/dashboard/dashboard.component').then((m) => m.Dashboard),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── Estudiantes (SOLO ADMIN) ────────────────────────────────────
      {
        path: 'estudiantes',
        loadComponent: () => import('./features/admin/estudiantes/estudiantes.component').then((m) => m.Estudiantes),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── ESTUDIANTE: Mis cursos (SOLO ESTUDIANTE) ────────────────────
      // MUST GO BEFORE estudiante/:id para que Angular matchee primero la ruta fija
      {
        path: 'estudiante/mis-cursos',
        loadComponent: () => import('./features/estudiante/mis-notas/mis-notas.component').then((m) => m.MisNotas),
        canActivate: [requiresRole('ESTUDIANTE')],
      },
      {
        path: 'estudiante/mis-cursos/:id/notas',
        loadComponent: () => import('./features/estudiante/mis-notas/mis-notas.component').then((m) => m.MisNotas),
        canActivate: [requiresRole('ESTUDIANTE')],
      },

      {
        path: 'estudiante/:id',
        loadComponent: () => import('./features/admin/estudiantes/estudiantes.component').then((m) => m.Estudiantes),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── DOCENTE: Estudiantes de una sección (solo lectura) ──────────
      {
        path: 'docente/mis-cursos/:id/estudiantes',
        loadComponent: () => import('./features/docente/estudiantes-seccion/estudiantes-seccion.component').then((m) => m.EstudiantesSeccion),
        canActivate: [requiresRole('DOCENTE')],
      },

      // ─── DOCENTE: Mis cursos (SOLO DOCENTE) ───────────────────────────
      // MUST GO BEFORE docente/:id para que Angular matchee primero la ruta fija
      {
        path: 'docente/mis-cursos',
        loadComponent: () => import('./features/docente/mis-cursos/mis-cursos.component').then((m) => m.MisCursos),
        canActivate: [requiresRole('DOCENTE')],
      },
      {
        path: 'docente/mis-cursos/:id/notas',
        loadComponent: () => import('./features/docente/carga-notas/carga-notas.component').then((m) => m.CargaNotas),
        canActivate: [requiresRole('DOCENTE')],
      },

      // ─── Docentes (SOLO ADMIN) ────────────────────────────────────────
      {
        path: 'docentes',
        loadComponent: () => import('./features/admin/docentes/docentes.component').then((m) => m.Docentes),
        canActivate: [requiresRole('ADMIN')],
      },
      {
        path: 'docente/:id',
        loadComponent: () => import('./features/admin/docentes/docentes.component').then((m) => m.Docentes),
        canActivate: [requiresRole('ADMIN')],
      },

      // ─── Cursos ───────────────────────────────────────────────────────
      {
        path: 'cursos',
        loadComponent: () => import('./features/admin/cursos/cursos.component').then((m) => m.Cursos),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },
      {
        path: 'cursos/:id',
        loadComponent: () => import('./features/admin/cursos/cursos.component').then((m) => m.Cursos),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Secciones (ADMIN + DOCENTE) ──────────────────────────────────
      {
        path: 'secciones',
        loadComponent: () => import('./features/admin/secciones/secciones.component').then((m) => m.Secciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },
      {
        path: 'secciones/:id',
        loadComponent: () => import('./features/admin/secciones/secciones.component').then((m) => m.Secciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Horarios (ADMIN + DOCENTE) ───────────────────────────────────
      {
        path: 'horarios',
        loadComponent: () => import('./features/admin/horarios/horarios.component').then((m) => m.Horarios),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── Evaluaciones (ADMIN + DOCENTE) ───────────────────────────────
      {
        path: 'evaluaciones',
        loadComponent: () => import('./features/admin/evaluaciones/evaluaciones.component').then((m) => m.Evaluaciones),
        canActivate: [requiresRole('ADMIN', 'DOCENTE')],
      },

      // ─── ESTUDIANTE: Historial (SOLO ESTUDIANTE) ─────────────────────
      {
        path: 'estudiante/historial',
        loadComponent: () => import('./features/estudiante/historial/historial.component').then((m) => m.Historial),
        canActivate: [requiresRole('ESTUDIANTE')],
      },

      // ─── Perfil (TODOS los roles) ────────────────────────────────────
      {
        path: 'perfil',
        loadComponent: () => import('./shared/components/perfil/perfil.component').then((m) => m.Perfil),
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
    loadComponent: () => import('./shared/components/page404/page404.component').then((m) => m.Page404),
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
 *   loadComponent: () => import('./components/mi-componente/mi-componente.component').then(m => m.MiComponente),
 *   canActivate: [requiresAuth]  // o el guard que necesites
 * }
 */
