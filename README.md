<div align="center">

  # Gestión Académica

  <p><strong>Sistema de gestión académica universitaria — monorepo full-stack</strong></p>

  [![Java 21](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Angular](https://img.shields.io/badge/Angular-21-DD0031?style=flat-square&logo=angular&logoColor=white)](https://angular.dev/)
  [![Taiga UI](https://img.shields.io/badge/Taiga%20UI-5-4CC3FF?style=flat-square&logo=taigaui&logoColor=white)](https://taiga-ui.dev/)
  [![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

</div>

Repositorio full-stack compuesto por una **API REST en Java Spring Boot** (`server/`) y una **aplicación web Angular** (`client/universidad-taiga/`).

---

## 📦 Stack Tecnológico

<details open>
<summary><strong>Backend</strong> — Java 21 + Spring Boot 3.5</summary>
<br/>

| Categoría | Tecnología |
|-----------|------------|
| **Lenguaje** | ![Java](https://img.shields.io/badge/Java_21-007396?style=flat-square&logo=openjdk&logoColor=white) |
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white) *(Web MVC, Security, Data JPA, Validation)* |
| **Build** | ![Maven](https://img.shields.io/badge/Maven_Wrapper-C71A36?style=flat-square&logo=apachemaven&logoColor=white) |
| **Base de datos** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL_15+-4169E1?style=flat-square&logo=postgresql&logoColor=white) *(InsForge cloud / local)* |
| **Documentación API** | ![Swagger](https://img.shields.io/badge/Springdoc_OpenAPI-85EA2D?style=flat-square&logo=swagger&logoColor=black) |
| **Testing** | ![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white) |
| **Seguridad** | Spring Security + JWT (JJWT 0.12) |

</details>

<details open>
<summary><strong>Frontend</strong> — Angular 21 + Taiga UI 5</summary>
<br/>

| Categoría | Tecnología |
|-----------|------------|
| **Framework** | ![Angular](https://img.shields.io/badge/Angular_21-DD0031?style=flat-square&logo=angular&logoColor=white) ![TypeScript](https://img.shields.io/badge/TypeScript_5-3178C6?style=flat-square&logo=typescript&logoColor=white) |
| **UI Components** | ![Taiga UI](https://img.shields.io/badge/Taiga_UI_5-4CC3FF?style=flat-square&logo=taigaui&logoColor=white) |
| **Estilos** | ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white) |
| **Caching / Estado** | ![TanStack Query](https://img.shields.io/badge/TanStack_Query-FF4154?style=flat-square&logo=reactquery&logoColor=white) |
| **Formularios** | Reactive Forms *(Angular)* |
| **Build** | ![Angular CLI](https://img.shields.io/badge/Angular_CLI-DD0031?style=flat-square&logo=angular&logoColor=white) |

</details>

---

## 📂 Estructura del Repositorio

```
gestion-academica/
├── server/                       # Backend Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── README.md                 ← Documentación del backend
├── client/
│   └── universidad-taiga/        # Frontend Angular
│       ├── src/
│       ├── package.json
│       └── README.md             ← Documentación del frontend
└── README.md                     ← Este archivo
```

---

## 🚀 Enlaces Rápidos

| Proyecto | README | Ejecución |
|----------|--------|-----------|
| **Backend** | [server/README.md](./server/README.md) | `cd server && ./mvnw spring-boot:run` |
| **Frontend** | [client/universidad-taiga/README.md](./client/universidad-taiga/README.md) | `cd client/universidad-taiga && ng serve` |

---

## 🌿 Flujo de Ramas

| Rama | Descripción |
|------|-------------|
| `main` | Producción. Solo merges desde `develop` o `hotfix/*`. |
| `develop` | Integración de funcionalidades. |
| `feature/<nombre>` | Nuevas funcionalidades (derivan de `develop`). |
| `refactor/<nombre>` | Refactorizaciones de código. |
| `fix/<nombre>` | Correcciones de bugs. |

> **Commits:** usar [Conventional Commits](https://www.conventionalcommits.org/) con títulos en español: `feat:`, `fix:`, `refactor:`, `docs:`, `style:`, `test:`, `chore:`.

---

## ✅ Funcionalidades

### Backend

| Módulo | Estado |
|--------|--------|
| Autenticación JWT (login, refresh, logout, forgot/reset password) | ✅ Completo |
| CRUD Estudiantes | ✅ Completo |
| CRUD Docentes | ✅ Completo |
| CRUD Cursos | ✅ Completo |
| CRUD Secciones | ✅ Completo |
| CRUD Horarios | ✅ Completo |
| Evaluaciones y Notas | ✅ Completo |
| Historial Académico | ✅ Completo |
| Gestión de Carreras | ✅ Completo |
| Roles y Usuarios | ✅ Completo |
| Swagger UI (`/swagger-ui.html`) | ✅ Documentado |

### Frontend

| Módulo | Estado |
|--------|--------|
| Login / Forgot / Reset Password | ✅ Completo |
| Dashboard Admin | ✅ Completo |
| Gestión de Estudiantes | ✅ Completo |
| Gestión de Cursos | ✅ Completo |
| Gestión de Docentes | ✅ Completo |
| Gestión de Secciones | ✅ Completo |
| Gestión de Horarios | ✅ Completo |
| Gestión de Evaluaciones | ✅ Completo |
| Módulo Docente (mis cursos, carga notas) | ⏳ Pendiente |
| Módulo Estudiante (mis notas, historial) | ⏳ Pendiente |

---

<div align="center">
  <small>Desarrollado con Spring Boot 3.5 + Angular 21 + Taiga UI 5</small>
</div>
