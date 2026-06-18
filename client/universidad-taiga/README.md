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

| Categoría | Tecnología |
|-----------|------------|
| **Framework** | Angular 21 (standalone components, signals, inject pattern) |
| **UI Components** | Taiga UI 5 — tablas, formularios, diálogos, badges, loaders |
| **Estilos** | Tailwind CSS 4 (layout) + CSS plano + Taiga UI tokens |
| **Formularios** | Reactive Forms (Angular) |
| **HTTP / Caching** | TanStack Query (`@tanstack/angular-query-experimental`) |
| **Autenticación** | JWT (access token en memoria + refresh token en cookie HttpOnly) |
| **Build** | Angular CLI 21 |
| **Testing** | Vitest + Angular Testing Library |

---

## 📂 Arquitectura

```
src/app/
├── core/                        # Singleton — servicios, guards, interceptors, tokens
│   ├── services/                 # AuthService, TokenService, RoleService, estudiante, catalogo
│   ├── guards/                   # auth.guard (requiresAuth, requiresRole, requiresNoAuth)
│   ├── interceptors/             # auth.interceptor (JWT + refresh automático en 401)
│   └── tokens/                   # APP_API_URL (InjectionToken)
├── shared/components/            # Componentes reutilizables sin lógica de negocio
│   ├── header/                   # Header con info del usuario, logout
│   ├── sidebar/                  # Navegación por rol
│   ├── layout/                   # Layout principal (header + sidebar + router-outlet)
│   ├── page404/                  # Página no encontrada
│   └── perfil/                   # Perfil de usuario
├── features/                     # Feature modules lazy-loaded por dominio
│   ├── auth/                     # login, forgot-password, reset-password
│   ├── admin/                    # dashboard, estudiantes, cursos, docentes, evaluaciones, horarios, secciones
│   ├── docente/                  # mis-cursos, carga-notas
│   └── estudiante/               # mis-notas, historial
├── models/                       # DTOs y tipos globales
└── queries/                      # TanStack Query hooks (injectQuery, injectMutation)
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
- npm (o pnpm)
- Backend corriendo en `http://localhost:8080`

### Instalación

```bash
cd client/universidad-taiga
npm install
```

### Desarrollo

```bash
ng serve
```

Navegar a `http://localhost:4200/`. Hot reload activo.

### Variables de entorno

```env
# Opcional: si el backend no corre en localhost:8080
# Definir en .env en la raíz del proyecto frontend
```

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

El frontend implementa un flujo de autenticación JWT con **refresh token**:

| Mecanismo | Descripción |
|-----------|-------------|
| **Access Token** | Se guarda en memoria (signal de Angular). Se pierde al recargar. |
| **Refresh Token** | Cookie HttpOnly. El navegador la envía automáticamente. |
| **Auto-refresh** | Se renueva 1 minuto antes de expirar. |
| **Interceptor** | Agrega `Authorization: Bearer` a todas las requests. Maneja 401 automáticamente. |
| **Guards** | `requiresAuth`, `requiresRole(ADMIN, DOCENTE, ESTUDIANTE)`, `requiresNoAuth` |

---

## 🎨 Diseño

| Capa | Herramienta |
|------|-------------|
| **Layout** | Tailwind CSS (grid, flexbox, spacing) |
| **Componentes complejos** | Taiga UI (tablas, inputs, selects, diálogos) |
| **Tokens de diseño** | `design-system/MASTER.md` — colores, tipografía, espaciado |
| **Iconos** | Taiga UI icons |

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
