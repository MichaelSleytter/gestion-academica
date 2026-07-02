<p align="right">
  <a href="../../../README.md">⬆ README principal</a>
</p>

<div align="center">

# Gestión Académica — Frontend

  <p><strong>Aplicación web Angular para administración académica universitaria</strong></p>

[![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)](https://angular.dev/)
[![Taiga UI](https://img.shields.io/badge/Taiga%20UI-5-4CC3FF?style=flat-square&logo=taigaui&logoColor=white)](https://taiga-ui.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![TanStack Query](https://img.shields.io/badge/TanStack%20Query-FF4154?style=flat-square&logo=reactquery&logoColor=white)](https://tanstack.com/query/latest)

</div>

Frontend del sistema **Gestión Académica**. Construido con Angular 21, Taiga UI 5 para componentes complejos y Tailwind CSS 4 para layout y estilos.

---

## 📦 Stack Tecnológico

| Categoría          | Tecnología                                                       |
| ------------------ | ---------------------------------------------------------------- |
| **Framework**      | Angular 21 (standalone components, signals, inject pattern)      |
| **UI Components**  | Taiga UI 5 — tablas, formularios, diálogos, badges, loaders      |
| **Estilos**        | Tailwind CSS 4 (layout) + CSS plano + Taiga UI tokens            |
| **Formularios**    | Reactive Forms (Angular)                                         |
| **HTTP / Caching** | TanStack Query (`@tanstack/angular-query-experimental`)          |
| **Autenticación**  | JWT (access token en memoria + refresh token en cookie HttpOnly) |
| **Build**          | Angular CLI 21                                                   |
| **Testing**        | Vitest + Angular Testing Library                                 |

---

## 📂 Arquitectura

```
src/app/
├── core/                        # Singleton — servicios, guards, interceptors, tokens
│   ├── services/                 # AuthService, TokenService, RoleService, catalogo, cruds
│   ├── guards/                   # auth.guard (requiresAuth, requiresRole, requiresNoAuth)
│   ├── interceptors/             # auth.interceptor (JWT + 401 auto-refresh)
│   └── tokens/                   # APP_API_URL (InjectionToken)
├── shared/components/            # Componentes reutilizables sin lógica de negocio
│   ├── header/                   # Header con info del usuario, logout
│   ├── sidebar/                  # Navegación por rol (ADMIN / DOCENTE / ESTUDIANTE)
│   ├── layout/                   # Layout principal (header + sidebar + router-outlet)
│   ├── page404/                  # Página no encontrada
│   └── perfil/                   # Perfil de usuario
├── features/                     # Feature modules lazy-loaded por dominio
│   ├── auth/                     # login, forgot-password, reset-password
│   ├── admin/                    # dashboard, estudiantes, cursos, docentes, evaluaciones, horarios, secciones
│   ├── docente/                  # mis-cursos, carga-notas, estudiantes-seccion
│   └── estudiante/               # mis-notas, historial
├── models/                       # DTOs y tipos (auth, docente, estudiante, seccion, horario, nota, etc.)
└── queries/                      # TanStack Query adapters (injectQuery, injectMutation)
```

### Principios

- **SOLID**: servicios con responsabilidad única. AuthService → TokenService + RoleService + AuthService
- **Feature-based**: cada feature se carga lazy por ruta
- **inject()**: patrón de inyección funcional, sin constructor injection
- **kebab-case.type.ts**: naming de archivos consistente (`.component`, `.service`, `.guard`, etc.)
- **Barrel exports**: `index.ts` en cada directorio para imports limpios

---

## 🚀 Ejecución Local

### Requisitos

- Node.js 20+
- pnpm 11 (ver `packageManager` en package.json)
- Backend corriendo en `http://localhost:8080`

### Instalación

```bash
cd client/universidad-taiga
pnpm install
```

### Desarrollo

```bash
pnpm start        # ng serve → http://localhost:4200/
```

Hot reload activo. En desarrollo usa `environment.ts` con `apiBaseUrl: 'http://localhost:8080/api/v1'`.

---

## 🏗️ Build y Deploy

```bash
# Build de producción
pnpm build        # ng build → dist/universidad-taiga/browser/
```

### Deploy en Vercel

El frontend se despliega en **Vercel** con el backend en **Railway** usando el patrón **same-origin proxy**:

```
Browser → Vercel (same-origin) → Railway (proxy inverso)
                ↓
         /api/* → Railway
```

Esto asegura que las cookies de refresh token viajen bajo el mismo dominio, evitando problemas de CORS y SameSite.

**Deploy desde la raíz del proyecto frontend:**

```bash
cd client/universidad-taiga
vercel --prod
```

**Deploy desde el build ya generado:**

```bash
cd client/universidad-taiga
pnpm build
vercel --prod dist/universidad-taiga/browser
```

**Configuración:**

- `vercel.json` en la raíz del frontend define rewrites de `/api/*` → Railway.
- `public/vercel.json` se copia automáticamente a `dist/` para el flujo de deploy desde build.
- En producción se usa `environment.prod.ts` con `apiBaseUrl: '/api/v1'` (same-origin).
- En desarrollo se usa `environment.ts` con `apiBaseUrl: 'http://localhost:8080/api/v1'`.

---

## 🧪 Testing

```bash
# Ejecutar tests unitarios
ng test

# Ejecutar tests con coverage
ng test --code-coverage
```

---

## 🏗️ Build

```bash
# Build de producción
ng build

# El output se genera en dist/
```

---

## 🔐 Autenticación

El frontend implementa un flujo de autenticación JWT con **refresh token rotation**:

| Mecanismo                  | Descripción                                                                                                                |
| -------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **Access Token**           | Se guarda en memoria (signal de Angular). Se pierde al recargar.                                                           |
| **Refresh Token**          | Cookie HttpOnly con `SameSite=Lax`. El navegador la envía automáticamente.                                                 |
| **Refresh Token Rotation** | Cada refresh genera un nuevo access token y rota el refresh token. El viejo se marca como `used` (protección anti-replay). |
| **Auto-refresh**           | Se programa 1 minuto antes de expirar el access token.                                                                     |
| **Interceptor**            | Agrega `Authorization: Bearer` a todas las requests. En 401 intenta auto-refresh antes de redirigir al login.              |
| **Guards**                 | `requiresAuth`, `requiresRole(ADMIN, DOCENTE, ESTUDIANTE)`, `requiresNoAuth`                                               |

### Flujo de login

```
POST /api/v1/auth/login
  → Backend valida credenciales
  → Retorna { accessToken, tokenType, expiresIn }
  → Setea cookie HttpOnly con refreshToken
  → AuthService:
      1. TokenService.setToken(accessToken, expiresIn)
      2. TokenService.saveToStorage(accessToken)
      3. Extrae roles del JWT → RoleService.setRoles(roles)
      4. Programa auto-refresh vía TokenService.scheduleRefresh()
      5. Redirige al home según el rol
```

### Restauración de sesión

Al cargar la app, `AuthService` intenta restaurar la sesión:

```
POST /api/v1/auth/refresh  (con cookie HttpOnly)
  → Si la cookie existe y es válida → nuevo access token
  → Si no hay cookie o expiró → estado no autenticado
```

---

## 🎨 Diseño

| Capa                      | Herramienta                                                |
| ------------------------- | ---------------------------------------------------------- |
| **Layout**                | Tailwind CSS (grid, flexbox, spacing)                      |
| **Componentes complejos** | Taiga UI (tablas, inputs, selects, diálogos)               |
| **Tokens de diseño**      | `design-system/MASTER.md` — colores, tipografía, espaciado |
| **Iconos**                | Taiga UI icons                                             |

---

## 📄 Convenciones de Código

Todas las reglas detalladas están en [`AGENTS.md`](../../../AGENTS.md). Principales:

- **Archivos**: `kebab-case.type.ts` (ej: `estudiantes.component.ts`)
- **Inyección**: `inject()` siempre, nunca constructor injection
- **Formularios**: Reactive Forms (obligatorio)
- **Fetching**: TanStack Query desde `queries/`
- **Documentación**: JSDoc en español para métodos públicos
- **Commits**: Conventional Commits en español (`feat:`, `fix:`, `refactor:`)

---

<div align="center">
  <small>Angular 21 + Taiga UI 5 + Tailwind CSS 4</small>
</div>
