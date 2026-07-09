# Exploración — Historial Académico

## Resumen ejecutivo

Backend Spring Boot/Maven bajo `server/` (Java 21, Spring Boot 3.5.13, PostgreSQL, Spring Data JPA, Spring Security JWT). Ya existe un paquete `historial` con entidad, repositorio, servicio y controlador CRUD para `HistorialAcademico`, pero representa registros finales por estudiante y sección, no una vista agregada de progreso de carrera.

Para construir una vista de progreso académico sin crear modelos nuevos, se pueden reutilizar `Estudiante`, `Carrera`, `Curso`, `Seccion`, `Matricula`, `HistorialAcademico`, `Nota`, `Evaluacion`, `DocenteSeccion` y roles de `Usuario`. Sin embargo, hay **gaps importantes**: no existe una relación Carrera→Curso/plan de estudios, no existe modelo de correlativas/prerrequisitos, no hay total de créditos requeridos por carrera y no hay reglas de autorización por rol/propiedad a nivel de endpoints.

## Mapa de paquetes

Base Java: `server/src/main/java/com/example/gestionacademica/`

- `auth/` — `Usuario`, `Rol`, `RefreshToken`, `PasswordResetToken`, JWT security filter
- `catalogos/` — `Carrera`, `TipoDocumento`, `GradoAcademico`
- `cursos/` — `Curso`, `Seccion`, `CicloAcademico`, `Horario`
- `estudiantes/` — `Estudiante`, `EstudianteEstadoAcademico`
- `docentes/` — `Docente`, `DocenteSeccion`
- `matriculas/` — `Matricula`
- `notas/` — `Nota`
- `evaluaciones/` — `Evaluacion`
- `historial/` — `HistorialAcademico`
- `config/` — security, CORS, Swagger/Scalar, encoder
- `exceptions/` — `GlobalExceptionHandler`
- `administradores/services/` — seed/creación de admin

## Entidades clave y relaciones

### Estudiante → Carrera (N:1)
`Estudiante` tiene `@ManyToOne` con `Carrera`. Gap: `Carrera` no tiene plan de estudios ni relación con `Curso`.

### Curso → Seccion (1:N)
`Curso` tiene créditos pero no pertenece a una carrera ni tiene ciclo recomendado/correlativas.

### Matricula (Estudiante ↔ Seccion)
Estados: `ACTIVA`, `RETIRADA`, `APROBADA`, `DESAPROBADA`. Restricción única por `(estudiante, seccion)`.

### HistorialAcademico (Estudiante ↔ Seccion)
Campos: `notaFinal`, `estado`. Misma restricción única. CRUD simple — no calcula promedio ni progreso.

### Nota → Evaluacion → Seccion
Evaluaciones ponderadas por porcentaje. Notas por estudiante y evaluación (0-20).

### DocenteSeccion (Docente ↔ Seccion)
Tabla puente para asignación docente-sección.

### Seguridad
- Spring Security + JWT. `@EnableMethodSecurity` habilitado pero sin `@PreAuthorize` implementado.
- Roles: `ADMIN`, `DOCENTE`, `ESTUDIANTE`.
- Sin enforcement de ownership: cualquier usuario autenticado puede acceder a cualquier endpoint.

## Diagrama ER

```
USUARIO ||--o| ESTUDIANTE : "perfil"
USUARIO ||--o| DOCENTE : "perfil"
USUARIO }o--o{ ROL : "usuario_rol"
CARRERA ||--o{ ESTUDIANTE : "inscribe"
ESTUDIANTE ||--o{ MATRICULA : "tiene"
ESTUDIANTE ||--o{ HISTORIAL_ACADEMICO : "tiene"
ESTUDIANTE ||--o{ NOTA : "recibe"
CURSO ||--o{ SECCION : "oferta"
SECCION ||--o{ MATRICULA : "recibe"
SECCION ||--o{ HISTORIAL_ACADEMICO : "genera"
SECCION ||--o{ EVALUACION : "define"
SECCION ||--o{ DOCENTE_SECCION : "asigna"
EVALUACION ||--o{ NOTA : "califica"
DOCENTE ||--o{ DOCENTE_SECCION : "dicta"
```

## Endpoints REST existentes

| Controlador | Path | Métodos clave |
|---|---|---|
| HistorialAcademico | `/api/v1/historial-academico` | GET /, /{id}, /estudiante/{id}, /seccion/{id}, POST, PUT, DELETE |
| Estudiante | `/api/v1/estudiantes` | GET paginado, /{id}, /carrera/{id}, /ciclo/{ciclo} |
| Matricula | `/api/v1/matriculas` | GET /estudiante/{id}, /seccion/{id}, POST, PATCH estado |
| Nota | `/api/v1/notas` | GET /estudiante/{id}, /evaluacion/{id} |
| Curso | `/api/v1/cursos` | GET paginado, /{id}, /buscar |
| Seccion | `/api/v1/secciones` | GET /curso/{id}, /ciclo/{id} |
| Carrera | `/api/v1/carreras` | GET /, /{id} |

## Gaps detectados

1. **Mapa completo de carrera:** `Carrera` no tiene plan de estudios ni relación con `Curso`. No se puede saber qué cursos pertenecen a una carrera.
2. **Materias pendientes:** no hay fuente para "todas las materias requeridas por carrera".
3. **Correlativas:** no hay modelo, columna ni tabla de prerrequisitos.
4. **Créditos restantes:** `Curso.creditos` existe, pero falta total requerido por carrera.
5. **Promedio general:** no hay servicio; debe definirse fuente de verdad (HistorialAcademico.notaFinal vs Nota ponderada).
6. **Estados no tipados:** `Matricula.estado` e `HistorialAcademico.estado` son String.
7. **Autorización por rol/propiedad:** solo autenticación global; falta `@PreAuthorize`.
8. **Lazy/N+1:** repositorios no cargan explícitamente relaciones anidadas.
9. **Sin migrations:** `ddl-auto: update`, sin trazabilidad.

## Recomendaciones

- Vista read-only nueva con endpoint `/api/v1/historial-academico/progreso/estudiante/{id}` y `/me`.
- `HistorialProgresoService` + DTOs, sin nuevas entidades JPA.
- Para correlativas/pendientes: el modelo actual **no lo permite**. Opciones:
  1. Alcance reducido: mostrar solo aprobadas/cursadas y créditos, sin correlativas ni pendientes.
  2. Permitir modelos nuevos (plan de estudios, prerrequisitos) en fases futuras.
  3. Usar convención externa si existe fuera del código.
