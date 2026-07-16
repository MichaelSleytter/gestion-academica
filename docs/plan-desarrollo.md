# Plan de Desarrollo — Gestión Académica

> **Proyecto:** Sistema de Gestión Académica Universitaria  
> **Repositorio:** [github.com/MichaelSleytter/gestion-academica](https://github.com/MichaelSleytter/gestion-academica)  
> **Período:** 18 abril 2026 — 15 julio 2026

---

## 1. Cronograma de Trabajo

El siguiente cronograma se extrajo directamente del historial de commits del repositorio, reflejando el trabajo real realizado sobre backend y frontend.

| # | Fecha | Hora | Autor | Tipo | Descripción |
| --- | ------ | ------ | ------- | ------ | ------------- |
| | | | | **FASE 1 — Inicialización del Backend** | |
| 1 | 18-abr-2026 | 15:36 | Michael Sleytter | `init` | Primer avance del proyecto (APF1) |
| 2 | 19-abr-2026 | 11:24 | TicseTorresArley | `feat` | Migrar de MySQL a PostgreSQL y actualizar dependencias |
| 3 | 19-abr-2026 | 11:25 | TicseTorresArley | `chore` | Añadir `.env.example` y actualizar `.gitignore` |
| 4 | 19-abr-2026 | 19:22 | TicseTorresArley | `feat` | Permitir CORS desde Angular (`localhost:4200`) |
| 5 | 19-abr-2026 | 19:29 | TicseTorresArley | `feat` | Agregar `CarreraController` y `TipoDocumentoController` |
| | | | | **FASE 2 — Pruebas, Documentación y Docker** | |
| 6 | 23-abr-2026 | 00:13 | TicseTorresArley | `test` | Tests unitarios para servicios del backend |
| 7 | 23-abr-2026 | 00:33 | TicseTorresArley | `docs` | README del proyecto |
| 8 | 23-abr-2026 | 00:40 | TicseTorresArley | `build` | Dockerfile para contenedor Docker |
| 9 | 23-abr-2026 | 00:54 | TicseTorresArley | `chore` | Plantilla `example.env` |
| 10-21 | 23-abr-2026 | 01:09—15:16 | TicseTorresArley | `fix/feat` | Ajustes CORS, Swagger, Scalar, DTOs |
| 22 | 25-abr-2026 | 08:38 | TicseTorresArley | `chore` | Inicializar OpenSpec + skill registry (SDD) |
| | | | | **FASE 3 — Refactorización y JWT** | |
| 23 | 28-abr-2026 | 21:09 | TicseTorresArley | `feat` | Refactor servicio de estudiantes: Factory, Validator, CrearDTO |
| 24-26 | 28-29-abr | — | TicseTorresArley | `feat/docs` | Documentación de endpoints, ejemplos `.env` |
| 27 | 07-may-2026 | 21:33 | TicseTorresArley | `refactor` | Migrar a arquitectura por **features** (paquetes) |
| 28-32 | 08-may-2026 | 11:37—11:41 | TicseTorresArley | `feat/fix` | **Autenticación JWT** con refresh tokens, CRUD estudiantes, CORS, tests |
| 33 | 21-may-2026 | 14:51 | TicseTorresArley | `refactor` | Puerto dinámico con variable de entorno |
| | | | | **FASE 4 — Mono-repo + Frontend Inicial** | |
| 34-38 | 28-may-2026 | 08:04—14:58 | TicseTorresArley | `chore/feat` | Reestructurar a **arquitectura client/server**, Scalar, Swagger |
| 39-40 | 04-jun-2026 | 01:31 | TicseTorresArley | `feat` | Flujo **forgot/reset password**, `EncoderConfig` |
| | | | | **FASE 5 — Frontend Angular + Módulos Admin** | |
| 41 | 18-jun-2026 | 12:55 | TicseTorresArley | `feat` | Reestructurar frontend: `core/shared/features` |
| 42 | 20-jun-2026 | 11:41 | TicseTorresArley | `fix` | Rutas raíz y login faltantes |
| 43 | 26-jun-2026 | 15:57 | TicseTorresArley | `feat` | Gestión de **estudiantes**: paginación, búsqueda, modal |
| 44 | 26-jun-2026 | 15:57 | TicseTorresArley | `feat` | Gestión de **docentes**: CRUD completo |
| 45 | 26-jun-2026 | 15:57 | TicseTorresArley | `chore` | Configuración y estilos del frontend |
| 46 | 26-jun-2026 | 15:58 | TicseTorresArley | `docs` | Estado real de módulos frontend en README |
| 47 | 26-jun-2026 | 16:20 | TicseTorresArley | `feat` | Gestión de **cursos**: CRUD + paginación |
| 48 | 26-jun-2026 | 16:30 | TicseTorresArley | `feat` | Módulo de **secciones**: paginación + CRUD |
| 49 | 26-jun-2026 | 16:34 | TicseTorresArley | `fix` | Corrección carácter oculto en secciones |
| 50 | 26-jun-2026 | 16:44 | TicseTorresArley | `feat` | Módulo de **horarios**: paginación + CRUD |
| 51 | 26-jun-2026 | 16:52 | TicseTorresArley | `feat` | Módulo de **evaluaciones**: paginación + CRUD |
| 52 | 26-jun-2026 | 17:00 | TicseTorresArley | `feat` | Menú Evaluaciones en sidebar |
| 53-54 | 26-jun-2026 | 17:12 | TicseTorresArley | `feat/docs` | **Dashboard admin** con estadísticas |
| | | | | **FASE 6 — Seed Data, Módulo Docente, Matrículas** | |
| 55-60 | 01-jul-2026 | 19:27—20:00 | TicseTorresArley | `fix/feat` | Seed data, refresh token rotation, fixes |
| 61 | 01-jul-2026 | 21:15 | TicseTorresArley | `feat` | Backend: endpoints carga-notas, matrículas por sección |
| 62 | 01-jul-2026 | 21:15 | TicseTorresArley | `feat` | Frontend: **módulo docente** (cards, notas, estudiantes) |
| 63-68 | 02-jul-2026 | 08:10—18:18 | TicseTorresArley | `fix/docs` | Correcciones URL base producción, documentación |
| | | | | **FASE 7 — Historial Académico + Seguridad** | |
| 69 | 09-jul-2026 | 08:11 | TicseTorresArley | `docs` | Documentación JSDoc en auth, environments |
| 70 | 09-jul-2026 | 12:19 | TicseTorresArley | `feat` | Gestión de **matrículas** para admin |
| 71-76 | 09-jul-2026 | 13:20—14:35 | TicseTorresArley | `feat` | **Historial académico**: modelo, servicio, seguridad, endpoints, Flyway, frontend |
| 77-79 | 12-jul-2026 | 20:31 | TicseTorresArley | `feat/docs` | Flujos académicos seguros (backend + frontend + docs) |
| 80-83 | 12-jul-2026 | 21:00—21:49 | TicseTorresArley | `fix/refactor` | Railway port, healthcheck, variable de entorno |
| | | | | **FASE 8 — Contenido, Accesibilidad, Pulido** | |
| 84-86 | 13-jul-2026 | 13:45—14:00 | TicseTorresArley | `fix` | Upload de contenido, filtro periodos, seguridad datos académicos |
| 87-89 | 13-jul-2026 | 20:37—20:38 | TicseTorresArley | `fix` | Acceso a detalle de sección, feedback login, vistas responsivas |
| 90-94 | 13-jul-2026 | 22:13 | TicseTorresArley | `fix` | Logos externos, horarios accesibles, refresh resiliente, storage API key |
| 95-96 | 13-jul-2026 | 23:38—23:41 | TicseTorresArley | `fix/refactor` | Accesibilidad en docentes, estilos + aria labels |
| 97-99 | 14-jul-2026 | 14:31—15:08 | TicseTorresArley | `refactor/fix` | Componentes, generación código sección, fix codegen |
| 100-101 | 15-jul-2026 | 17:39—17:52 | TicseTorresArley | `feat/refactor` | Filtrado por ciclo académico, error matrícula duplicada |

---

## 2. Herramientas Utilizadas

### 2.1 Backend

| Herramienta | Versión | Propósito |
| ------------- | --------- | ----------- |
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.5.13 | Framework principal (Web MVC, Security, Data JPA, Validation) |
| **Maven** | 4.x | Gestor de dependencias y build |
| **PostgreSQL** | 15+ | Base de datos relacional (InsForge cloud) |
| **Flyway** | — | Migraciones de base de datos versionadas |
| **Spring Security** | — | Autenticación y autorización |
| **JJWT** | 0.12.5 | Generación y validación de tokens JWT |
| **Lombok** | 1.18.42 | Reducción de boilerplate (getters, setters, builders) |
| **Springdoc OpenAPI** | 2.8.6 | Documentación interactiva de API (Swagger UI) |
| **Scalar** | — | Documentación alternativa de API |
| **H2** | — | Base de datos embebida para pruebas |
| **JUnit 5 + Mockito** | — | Testing unitario |
| **Spring Boot Mail** | — | Envío de correos (Resend SMTP) |
| **Docker** | — | Contenedorización de la aplicación |
| **Railway** | — | Hosting del backend en producción |
| **InsForge** | — | BaaS: PostgreSQL cloud, Storage, API key |

### 2.2 Frontend

| Herramienta | Versión | Propósito |
| ------------- | --------- | ----------- |
| **Angular** | 21.2 | Framework SPA (standalone components, signals) |
| **TypeScript** | 5.9 | Lenguaje de programación |
| **Taiga UI** | 5.2 | UI Kit de componentes (tablas, formularios, diálogos) |
| **Tailwind CSS** | 4 | Framework de estilos utilitario |
| **TanStack Query** | 5.99 | Caching y estado de peticiones HTTP |
| **RxJS** | 7.8 | Programación reactiva |
| **SweetAlert2** | 11.26 | Notificaciones y diálogos |
| **Ng-icons** | 33.2 | Librería de iconos (Tabler Icons) |
| **Angular CLI** | 21.2 | Build y desarrollo |
| **Vite** | — | Bundler (Angular 21 us build) |
| **Vitest** | 4.0 | Testing unitario |
| **pnpm** | 11.1 | Gestor de paquetes |
| **Vercel** | — | Hosting del frontend en producción |
| **PostCSS** | 8.5 | Procesamiento de CSS |
| **Less** | 4.6 | Preprocesador de CSS |

### 2.3 Desarrollo y Colaboración

| Herramienta | Propósito |
| ------------- | ----------- |
| **Git** | Control de versiones |
| **GitHub** | Repositorio remoto y colaboración |
| **IntelliJ IDEA / VS Code / Zed** | IDEs de desarrollo |
| **Conventional Commits** | Convención de mensajes de commit |
| **OpenSpec / SDD** | Metodología de desarrollo espec-driven |
| **Pi (coding agent)** | Asistente de desarrollo automatizado |

---

## 3. Roles del Equipo

| Rol | Responsable | Responsabilidades |
| ----- | ------------- | ------------------- |
| **Desarrollador Backend** | TicseTorresArley | API REST, seguridad JWT, modelos de datos, migraciones Flyway, lógica de negocio, endpoints CRUD, historial académico, integración InsForge Storage |
| **Desarrollador Frontend** | TicseTorresArley | Componentes Angular, servicios HTTP, autenticación JWT (interceptor, refresh), TanStack Query, UI con Taiga UI + Tailwind, diseño responsive, accesibilidad |
| **Desarrollador Full-stack** | Michael Sleytter | Primer avance del proyecto, configuración inicial |
| **Arquitecto / DevOps** | TicseTorresArley | Estructura del proyecto (feature-based, client/server), Docker, Railway (backend), Vercel (frontend), variables de entorno, CORS, SDD/OpenSpec |

> **Nota:** El proyecto fue desarrollado principalmente por **TicseTorresArley** (100 commits) con contribución inicial de **Michael Sleytter** (1 commit).

---

## 4. Metodología de Trabajo

| Aspecto | Enfoque |
| --------- | --------- |
| **Metodología** | **SDD (Spec-Driven Development)** — los cambios se planifican con especificaciones antes de implementar |
| **Control de versiones** | Git + GitHub con Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`) |
| **Flujo de ramas** | `main` → producción; `feature/*` → nuevas funcionalidades; `fix/*` → correcciones; `refactor/*` → reestructuración |
| **Arquitectura backend** | Capas por feature (`controller → service → repository → domain`) |
| **Arquitectura frontend** | Feature-based (`core/shared/features`) con componentes standalone |
| **Testing** | Backend: JUnit 5 + Mockito; Frontend: Vitest + Angular Testing Library |
| **Calidad** | Validación con Jakarta Bean Validation, manejo global de excepciones, interceptors HTTP |
| **Despliegue** | Backend en Railway, Frontend en Vercel (same-origin proxy) |

---

<div align="center">
  <small>Documento generado a partir del historial de commits del repositorio — julio 2026</small>
</div>
