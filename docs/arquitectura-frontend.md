# Arquitectura del Frontend

## Diagrama de Arquitectura

### Desarrollo (local)

```
┌──────────────────────────────────────────────────────────────┐
│                      Angular 21 SPA                          │
│                       localhost:4200                          │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │   Core   │  │   Auth   │  │ Features │  │  Shared  │    │
│  │  Module  │  │  Module  │  │ Modules  │  │  Module  │    │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘    │
│       │             │             │             │          │
│  ┌────┴─────────────┴─────────────┴─────────────┴───┐      │
│  │              Guards de Rutas                      │      │
│  │  requiresAuth  │  requiresRole  │  requiresNoAuth│      │
│  └───────────────────────────────────────────────────┘      │
│                         │                                    │
│  ┌──────────────────────┴────────────────────────────────┐  │
│  │              HTTP Interceptor (JWT)                    │  │
│  │  Request → Add Authorization: Bearer <token> → Send   │  │
│  └──────────────────────┬────────────────────────────────┘  │
│                         │                                    │
└─────────────────────────┼────────────────────────────────────┘
                          │
                    ┌─────┴──────┐
                    │  Backend   │
                    │ Spring Boot│
                    │ :8080/api  │
                    └────────────┘
```

### Producción (Vercel + Railway)

```
┌──────────────────────────────────────────────────────────────┐
│                  Navegador (cliente)                         │
│           https://gestion-academica.vercel.app               │
└─────────────────────────┬────────────────────────────────────┘
                          │
                    ┌─────┴──────────────────┐
                    │     Vercel (host)      │
                    │                        │
                    │  /api/*  → Railway     │
                    │  /*       → index.html │
                    │  (rewrite proxy)       │
                    └─────┬──────────────────┘
                          │
                    ┌─────┴──────────┐
                    │    Railway     │
                    │  Spring Boot   │
                    │  :8080/api/v1  │
                    └────────────────┘
```

**Clave:** El navegador solo conoce un dominio (Vercel). Las cookies viajan same-origin. Vercel proxea `/api/*` a Railway de forma transparente.

---

## 1.6.1.1 Arquitectura General

El frontend sigue una arquitectura basada en módulos funcionales, construida con **Angular 21** usando componentes standalone (sin NgModules) y signals para reactividad.

### Estructura de directorios

```
src/app/
├── core/                    # Singleton — servicios, guards, interceptors, tokens
│   ├── guards/              #  Auth guards (requiresAuth, requiresRole, requiresNoAuth)
│   ├── interceptors/        #  HTTP interceptor (JWT injection + 401 auto-refresh)
│   ├── services/            #  AuthService, TokenService, RoleService, cruds
│   └── tokens/              #  InjectionToken para API URL
│
├── features/                # Feature modules lazy-loaded por dominio
│   ├── admin/
│   │   ├── dashboard/       #  Panel de administración [ADMIN]
│   │   ├── docentes/        #  CRUD docentes [ADMIN]
│   │   │   ├── card-docente/       #  Card reutilizable
│   │   │   └── docente-form/       #  Formulario crear/editar
│   │   ├── estudiantes/     #  CRUD estudiantes [ADMIN]
│   │   │   ├── card-estudiante/          #  Card reutilizable
│   │   │   ├── estudiante-form/          #  Formulario crear/editar
│   │   │   └── estudiante-delete-dialog/ #  Confirmación de borrado
│   │   ├── cursos/          #  Gestión de cursos
│   │   ├── secciones/       #  Gestión de secciones
│   │   ├── horarios/        #  Gestión de horarios
│   │   └── evaluaciones/    #  Gestión de evaluaciones (notas)
│   │
│   ├── auth/                # Auth
│   │   ├── login/           #  Inicio de sesión
│   │   ├── forgot-password/ #  Recuperación de contraseña
│   │   └── reset-password/  #  Restablecer contraseña
│   │
│   ├── docente/             # Módulo del Rol Docente
│   │   ├── mis-cursos/      #  Vista de cursos asignados
│   │   ├── carga-notas/     #  Carga de notas por evaluación
│   │   └── estudiantes-seccion/  #  Listado de estudiantes (solo lectura)
│   │
│   └── estudiante/          # Módulo del Rol Estudiante
│       ├── mis-notas/       #  Visualización de notas
│       └── historial/       #  Historial académico
│
├── queries/                 # TanStack Query adapters
│   ├── catalogo.query.ts
│   ├── curso.query.ts
│   ├── docente.query.ts
│   ├── docente-role.query.ts
│   ├── estudiante.query.ts
│   ├── evaluacion.query.ts
│   ├── horario.query.ts
│   ├── seccion.query.ts
│   └── query-keys.ts
│
├── models/                  # DTOs y tipos (auth, docente, estudiante, seccion,
│   │                        #  horario, nota, evaluacion, matricula, shared...)
│   └── shared/
│       └── page.response.ts  # Paginación genérica
│
└── shared/                  # Componentes reutilizables
    ├── components/
    │   ├── layout/           #  Layout con sidebar + header + router-outlet
    │   ├── sidebar/          #  Sidebar de navegación que se adapta por rol
    │   ├── header/           #  Header con nombre, rol y logout
    │   ├── page404/          #  Página 404
    │   └── perfil/           #  Perfil de usuario
    └── utils/               #  Utilidades compartidas
```

### Core Module

Contiene los servicios y utilidades de alcance global:

| Componente | Descripción |
|---|---|
| `AuthService` | Manejo de autenticación: login, logout, refresh token |
| `TokenService` | Ciclo de vida del JWT en memoria + localStorage |
| `RoleService` | Gestión de roles del usuario y rutas por rol |
| `auth.guard` | Guards de ruta: `requiresAuth`, `requiresNoAuth`, `requiresRole(rol)` |
| `AuthInterceptor` | Interceptor HTTP que inyecta el JWT en cada petición |

Los services usan **Angular signals** para reactividad:

```typescript
// TokenService
authStatus = signal<AuthStatus>('loading');
isAuthenticated = computed(() => !!this.accessToken());

// RoleService
userRoles = signal<string[]>([]);
getRoles = computed(() => this.userRoles());
isAdmin = computed(() => this.userRoles().includes('ADMIN'));
```

### Feature Modules

Cada feature module es un componente standalone dentro de `features/`. Se cargan mediante **lazy loading** cuando el usuario navega a su ruta. No hay NgModules — Angular 21 funciona con standalone components por defecto.

### Shared Module

Componentes reutilizables y de layout que se comparten entre features:

- **Layout**: estructura principal con sidebar + header + router-outlet
- **Sidebar**: menú de navegación que se adapta según el rol del usuario
- **Header**: muestra nombre y rol del usuario autenticado
- **Page404**: página de error para rutas no encontradas

---

## 1.6.1.2 Comunicación con Backend

### Consumo de API REST

La comunicación se realiza mediante `HttpClient` de Angular contra el backend Spring Boot en `http://localhost:8080/api/v1`.

### Autenticación JWT

Cada petición HTTP incluye el token JWT en el encabezado `Authorization` mediante un **interceptor HTTP**:

```typescript
// Core Interceptor
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = this.tokenService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
```

### Almacenamiento del Token

- **Memoria**: señal `accessToken` en `TokenService` (reactiva, segura contra XSS)
- **localStorage**: persiste el token para sobrevivir recargas de página
- **Refresh**: refresco automático programado antes de la expiración (`scheduleRefresh()`)

### Refresh Token Rotation

El backend implementa **refresh token rotation** para prevenir ataques de replay:

1. Cada refresh exitoso genera un **nuevo** refresh token en cookie
2. El refresh token anterior se marca como `used` en la BD
3. Si un token robado se usa dos veces, el segundo intento falla
4. La cookie se setea con `HttpOnly`, `SameSite=Lax`, `Path=/api/v1/auth`

### Flujo de Autenticación

```
POST /api/v1/auth/login
  → Backend valida credenciales
  → Retorna { accessToken, expiresIn }
  → Setea cookie HttpOnly con refreshToken
  → AuthService:
      1. TokenService.setToken(accessToken, expiresIn) → memoria + localStorage
      2. Extrae roles del JWT → RoleService.setRoles()
      3. Programa auto-refresh vía TokenService.scheduleRefresh()
      4. Redirige al home según el rol
```

### Restauración de sesión al iniciar la app

```
constructor() de AuthService:
  → Si hay token en localStorage → restaura roles
  → refreshSession():
      POST /api/v1/auth/refresh (con cookie HttpOnly)
      → 200: nuevo accessToken → setToken + programa refresh
      → 401: limpia sesión si no hay token vigente
```

### Configuración de entornos

| Entorno | Archivo | apiBaseUrl |
|---------|---------|------------|
| Desarrollo | `environment.ts` | `http://localhost:8080/api/v1` |
| Producción | `environment.prod.ts` | `/api/v1` (same-origin proxy) |

En producción, Angular llama a `/api/v1` bajo el mismo dominio de Vercel.
Vercel proxea `/api/*` a Railway mediante `vercel.json` rewrites.

### TanStack Query (Reactividad)

Se utiliza `@tanstack/angular-query-experimental` para las consultas al backend. Los adapters están en `src/app/queries/`:

```
queries/
├── catalogo.query.ts        # Tipos de documento, carreras
├── curso.query.ts           # CRUD cursos
├── docente.query.ts         # CRUD docentes
├── docente-role.query.ts    # Queries específicas del rol docente
├── estudiante.query.ts      # CRUD estudiantes
├── evaluacion.query.ts      # CRUD evaluaciones
├── horario.query.ts         # CRUD horarios
├── seccion.query.ts         # CRUD secciones
└── query-keys.ts            # Constantes de query keys
```

Patrón de uso:

```typescript
export function useDocenteSeccionesQuery(idDocente: Signal<number | null>) {
  return injectQuery<SeccionResponse[], Error>(() => ({
    queryKey: ['docente', idDocente(), 'secciones'],
    queryFn: () => service.getSeccionesByDocente(idDocente() ?? 0),
    enabled: idDocente() !== null,
    staleTime: 1000 * 30,
  }));
}
```

Características:
- **Caching**: staleTime configurable por consulta
- **Invalidación**: actualización automática tras mutaciones (`invalidateQueries`)
- **Loading/Error states**: manejo reactivo en templates mediante `isPending`, `isError`

---

## 1.6.1.3 Sistema de Rutas

Las rutas se definen en `app.routes.ts` usando el `Router` de Angular con **lazy loading** (`loadComponent`) para todos los componentes. No hay NgModules — Angular 21 usa standalone components por defecto.

### Guards de Ruta

| Guard | Función |
|---|---|
| `requiresAuth` | Permite acceso solo si el usuario está autenticado |
| `requiresNoAuth` | Permite acceso solo si NO está autenticado (login) |
| `requiresRole(rol)` | Permite acceso solo si el usuario tiene el rol especificado |

### Estructura de Rutas

```
📁 RUTAS PÚBLICAS (sin autenticación)
├── /                          → Redirige a /login
├── /login                     → LoginComponent (requiresNoAuth)
├── /forgot-password           → ForgotPasswordComponent (requiresNoAuth)
├── /reset-password            → ResetPasswordComponent (público)
└── /page404                   → Page404

📁 RUTAS PRIVADAS (requieren autenticación) — bajo /app
│
├── /app/dashboard             → DashboardComponent      [ADMIN]
│
├── /app/estudiantes           → EstudiantesComponent    [ADMIN]
├── /app/estudiante/:id        → EstudiantesComponent    [ADMIN]
│
├── /app/estudiante/mis-cursos        → MisNotasComponent      [ESTUDIANTE]
├── /app/estudiante/mis-cursos/:id/notas  → MisNotasComponent  [ESTUDIANTE]
├── /app/estudiante/historial          → HistorialComponent     [ESTUDIANTE]
│
├── /app/docente/mis-cursos               → MisCursosComponent       [DOCENTE]
├── /app/docente/mis-cursos/:id/notas     → CargaNotasComponent      [DOCENTE]
├── /app/docente/mis-cursos/:id/estudiantes → EstudiantesSeccionComponent [DOCENTE]
│
├── /app/docentes              → DocentesComponent      [ADMIN]
├── /app/docente/:id           → DocentesComponent      [ADMIN]
│
├── /app/cursos                → CursosComponent        [ADMIN, DOCENTE]
├── /app/cursos/:id            → CursosComponent        [ADMIN, DOCENTE]
│
├── /app/secciones             → SeccionesComponent     [ADMIN, DOCENTE]
├── /app/secciones/:id         → SeccionesComponent     [ADMIN, DOCENTE]
│
├── /app/horarios              → HorariosComponent      [ADMIN, DOCENTE]
├── /app/evaluaciones          → EvaluacionesComponent  [ADMIN, DOCENTE]
│
└── /app/perfil                → PerfilComponent        [TODOS]

📁 RUTA WILDCARD
└── /**                        → Page404 (cualquier ruta no definida)
```

### Redirección por Rol

Al iniciar sesión, el usuario es redirigido según su rol:

| Rol | Ruta de inicio |
|---|---|
| ADMIN | `/app/dashboard` |
| DOCENTE | `/app/docente/mis-cursos` |
| ESTUDIANTE | `/app/estudiante/mis-cursos` |

### Sidebar Adaptativo

El sidebar muestra solo las opciones correspondientes al rol del usuario autenticado:

- **ADMIN**: Dashboard, Estudiantes, Docentes, Cursos, Secciones, Horarios, Evaluaciones, Perfil
- **DOCENTE**: Mis Cursos, Perfil
- **ESTUDIANTE**: Mis Cursos, Historial, Perfil

### Lazy Loading

Todos los componentes de rutas hijas se cargan mediante **lazy loading** (`loadComponent`), lo que significa que el código de cada feature solo se descarga cuando el usuario navega a esa ruta por primera vez. Esto optimiza el bundle inicial y mejora los tiempos de carga.

```typescript
// Ejemplo de lazy loading
{
  path: 'docente/mis-cursos',
  loadComponent: () => import('./features/docente/mis-cursos/mis-cursos.component')
    .then(m => m.MisCursos),
}
```
