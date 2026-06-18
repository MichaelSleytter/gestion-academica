<p align="center">
  <a href="../README.md">⬆ Volver al README principal</a>
</p>

<h1 align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=java&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-green?logo=spring&logoColor=white" alt="Spring Boot 3.5.13">
  <img src="https://img.shields.io/badge/PostgreSQL-15+-blue?logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Maven-4.0-red?logo=apache-maven&logoColor=white" alt="Maven">
</h1>

<p align="center">
  <strong>Gestión Académica</strong> — API REST del Sistema de Gestión Académica Universitaria
</p>

<p align="center">
  <a href="https://swagger.io/tools/swagger-ui/">
    <img src="https://img.shields.io/badge/Swagger-UI-blue?logo=swagger" alt="Swagger UI">
  </a>
  <a href="https://spring.io/projects/spring-boot">
    <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
  </a>
</p>

---

## Descripción del Proyecto

**Gestión Académica** es un sistema de gestión académica universitaria desarrollado con **Spring Boot 3.5.x** y **Java 21**, diseñado para administrar procesos académicos como estudiantes, docentes, matriculas, evaluaciones, historial académico, gestión de carreras, cursos, secciones y más.

La API proporciona endpoints RESTful para la gestión completa de la información académica institucional, con soporte para documentación interactiva via **Swagger UI**.

---

## Tabla de Contenidos

1. [Tecnologías](#tecnologías)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Requisitos Previos](#requisitos-previos)
5. [Configuración e Instalación](#configuración-e-instalación)
6. [Ejecución](#ejecución)
7. [Documentación de la API](#documentación-de-la-api)
8. [Características Principales](#características-principales)
9. [Pruebas](#pruebas)
10. [Construcción](#construcción)
11. [Contribución](#contribución)
12. [Licencia](#licencia)

---

## Tecnologías

| Tecnología | Versión |
|------------|--------|
| Java | 21 |
| Spring Boot | 3.5.13 |
| Spring Data JPA | (incluido en Spring Boot) |
| Hibernate | (implementación JPA) |
| PostgreSQL (InsForge) | 15+ |
| Maven | 4.x |
| Lombok | (latest) |
| Springdoc OpenAPI | 2.8.6 |
| Spring Security Crypto | 6.x |
| Jakarta Validation | (incluido) |

---

## Herramientas

| Categoría | Herramienta |
|----------|------------|
| **IDE** | IntelliJ IDEA, Visual Studio Code, Zed |
| **Base de datos** | InsForge (cloud PostgreSQL), pgAdmin |
| **Documentación API** | Swagger UI, Scalar |
| **Control de versiones** | Git |
| **Repositorio remoto** | GitHub |

---

## Arquitectura del Proyecto

El proyecto sigue una **arquitectura hexagonal** o **patrón de capas**:

```
┌─────────────────────────────────────────────────────────────┐
│                    CONTROLADORES (Controllers)              │
│         HTTP REST API Endpoints (/api/estudiantes, etc.)    │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICIOS (Services)                     │
│              Lógica de negocio y transacciones              │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 REPOSITORIOS (Repositories)                 │
│              Acceso a datos con Spring Data JPA             │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    ENTIDADES (Entities)                     │
│              Modelos de dominio y mapeo O/R                 │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    BASE DE DATOS (PostgreSQL)               │
│                  Almacenamiento persistente                 │
└─────────────────────────────────────────────────────────────┘
```

### Capas del Proyecto

- **Controllers** — Controladores REST que exponen endpoints HTTP
- **Services** — Lógica de negocio e investigación de dependencias
- **Repositories** — Interfaces que extienden `JpaRepository` para acceso a datos
- **Entities** — Modelos de dominio con anotaciones JPA/Hibernate
- **DTOs** — Objetos de transferencia de datos (Request/Response)
- **Mappers** — Conversión entre entidades y DTOs
- **Exceptions** — Manejo global de excepciones
- **Config** — Configuraciones (Security, CORS, Swagger)
- **Enums** — Enumeraciones del dominio
- **Utils** — Utilidades auxiliares

---

## Estructura del Proyecto

```
gestion-academica/
├── src/
│   ├── main/
│   │   ├── java/com/example/gestionacademica/
│   │   │   ├── config/           # Configuraciones
│   │   │   ├── controllers/      # Controladores REST
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entities/         # Entidades JPA
│   │   │   ├── enums/            # Enumeraciones
│   │   │   ├── exceptions/       # Manejo de excepciones
│   │   │   ├── mappers/          # Mapeadores
│   │   │   ├── repositories/     # Repositorios JPA
│   │   │   ├── services/         # Servicios
│   │   │   ├── utils/            # Utilidades
│   │   │   └── GestionacademicaApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── .env (referencia)
│   └── test/
│       └── java/.../gestionacademica/
│           ├── repositories/  # Pruebas de repositorio
│           └── services/       # Pruebas de servicio
├── pom.xml
└── mvnw / mvnw.cmd            # Maven Wrapper
```

---

## Requisitos Previos

- **Java 21** (JDK)
- **PostgreSQL 15+** (local o InsForge cloud)
- **Maven 4.x** (opcional, usar wrapper incluido)
- **Git** (para clonar el repositorio)

---

## Configuración e Instalación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd gestion-academica
```

### 2. Configurar la base de datos (InsForge)

Este proyecto usa **InsForge** como backend de base de datos PostgreSQL en la nube. También puedes usar una instancia local de PostgreSQL.

#### Opción A: InsForge (Cloud)

1. Crea una cuenta en [insforge.dev](https://insforge.dev)
2. En el dashboard, obtén las credenciales de conexión:
   - **Connection URL**: similar a `postgresql://postgres:[password]@tu-proyecto.insforge.app:5432/postgres`
   - **API Base URL**: `https://tu-proyecto.insforge.app`

#### Opción B: PostgreSQL local

Crea una base de datos llamada `gestion` (o la que prefieras).

### 3. Configurar variables de entorno

Crea un archivo `.env` en la raíz del proyecto (o en `src/.env`) con las siguientes variables:

#### Para InsForge (Cloud)

```env
DB_URL=postgresql://postgres:[password]@tu-proyecto.insforge.app:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=tu_password
CORS_ALLOWED_ORIGIN=http://localhost:4200
```

#### Para PostgreSQL local

```env
DB_URL=jdbc:postgresql://localhost:5432/gestion
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña
CORS_ALLOWED_ORIGIN=http://localhost:4200
```

### 4. Instalar dependencias

El proyecto incluye el **Maven Wrapper**. No necesitas tener Maven instalado:

```bash
# En Linux/macOS
./mvnw clean install

# En Windows
mvnw.cmd clean install
```

---

## Ejecución

### Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

### Ejecutar el JAR compilado

```bash
java -jar target/gestion-academica-1.0.0.jar
```

---

## Documentación de la API

### Swagger UI

Accede a la documentación interactiva:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Características Principales

### Endpoints Principales

| Módulo | Descripción |
|--------|-------------|
| **Estudiantes** | Gestión de información de estudiantes |
| **Docentes** | Administración de docente |
| **Carreras** | Gestión de carreras/programs académicos |
| **Cursos** | Catálogo de cursos |
| **Secciones** | Secciones de cursos por período |
| **Matriculas** | Matriculación de estudiante |
| **Evaluaciones** | Registro de evaluaciones |
| **Notas** | Calificaciones y notas |
| **Horarios** | Gestión de horarios |
| **Historial Académico** | Historial de progression |
| **Grados Académicos** | Gestión de grados/títulos |
| **Ciclos Académicos** | Períodos académicos |
| **Roles y Usuarios** | Sistema de usuarios y autenticación |

### Características Técnicas

- API RESTful con estándar Spring Boot
- Validación de datos con **Jakarta Bean Validation**
- Documentación automática con **Springdoc OpenAPI**
- Configuración CORS habilitada
- Manejo global de excepciones
- Mapeo automático de entidades a DTOs
- Encriptación de contraseñas con **BCrypt**

---

## Pruebas

El proyecto incluye pruebas unitarias y de integración en `src/test/java/`.

### Ejecutar todas las pruebas

```bash
./mvnw test
```

### Ejecutar prueba de una clase específica

```bash
./mvnw -Dtest=EstudianteRepositoryTest test
```

### Ejecutar un método de prueba específico

```bash
./mvnw -Dtest=EstudianteRepositoryTest#debeGuardarYRecuperarPorId test
```

> **Nota**: Las pruebas requieren una conexión a la base de datos PostgreSQL configurada. Verifica que las variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` estén definidas.

---

## Construcción

### Compilar sin ejecutar pruebas

```bash
./mvnw -DskipTests clean package
```

### Compilar con pruebas

```bash
./mvnw clean package
```

### JAR generado

El archivo JAR se genera en: `target/gestion-academica-1.0.0.jar`

---

## Contribución

¡Las contribuciones son bienvenidas! Para contribuir:

1. **Fork** del repositorio
2. Crea una rama (`git checkout -b feature/nueva-caracteristica`)
3. Realiza tus cambios y **commits** (`git commit -m 'Añadir nueva característica'`)
4. **Push** a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un **Pull Request**

### Buenas Prácticas

- Sigue las convenciones de código existentes
- Añade pruebas unitarias para nuevas funcionalidades
- Actualiza la documentación si es necesario
- Asegúrate de que el código compila y las pruebas pasan

---

## Licencia

Este proyecto está bajo la licencia **MIT**. Consulta el archivo `LICENSE` para más detalles.

---

<p align="center">
  <a href="../README.md">⬆ Volver al README principal</a>
</p>

<p align="center">
  <small>Desarrollado con Spring Boot 3.5.x y Java 21</small>
</p>
