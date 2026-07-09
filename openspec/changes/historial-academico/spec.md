# Historial Académico Specification

## Purpose

Historial Académico MUST provide a secure, read-only academic progress view for a student's degree path. The capability MUST combine the student's career, curriculum map, prerequisites, enrollments, evaluations, grades, and final course outcomes into DTO responses suitable for future frontend consumption without exposing JPA entities.

## Assumptions

- The proposal does not contain a `Capabilities` section; this spec treats `historial-academico` as a new domain specification inferred from the proposal and exploration.
- The first slice is backend-focused. Angular/Tailwind/Taiga UI consumption is out of scope except for the API contract defined here.
- Curriculum versioning by cohort is out of scope. Each career has one active curriculum plan for this slice.

## Functional Requirements

### Requirement: FR-001 — Curriculum map definition

The system MUST represent the courses that belong to a career through `MallaCurricular` entries. Each entry MUST identify the career, course, recommended cycle, whether the course is mandatory or elective, and the credits that count toward academic progress.

#### Acceptance Criteria

##### Scenario: Career has a curriculum course

- GIVEN a career has a `MallaCurricular` entry for a course
- WHEN academic progress is calculated for a student in that career
- THEN the course MUST appear in the progress course map
- AND the response MUST include its recommended cycle, mandatory/elective flag, and credits.

##### Scenario: Course is not in the student's curriculum

- GIVEN a course exists in the catalog
- AND the student's career has no `MallaCurricular` entry for that course
- WHEN academic progress is calculated
- THEN the course MUST NOT be counted as a pending curriculum course for that student.

### Requirement: FR-002 — Prerequisite definition

The system MUST represent prerequisite relationships through `Prerrequisito` entries that connect a target course to a prerequisite course within a career curriculum. The rule type MUST support `HARD` prerequisites for this slice.

#### Acceptance Criteria

##### Scenario: Course has an unmet hard prerequisite

- GIVEN a curriculum course has a `HARD` prerequisite
- AND the student has not passed the prerequisite course
- WHEN the progress course map is calculated
- THEN the target course MUST be marked as blocked
- AND the unmet prerequisite MUST be included in the course DTO.

##### Scenario: Course prerequisite is passed

- GIVEN a curriculum course has a `HARD` prerequisite
- AND the student has passed the prerequisite course
- WHEN the progress course map is calculated
- THEN the target course MUST NOT be blocked by that prerequisite.

### Requirement: FR-003 — Final grade calculation

The system MUST calculate a course final grade as the weighted sum of all evaluation grades for the student's section: `Σ(notaEvaluacion × pesoEvaluacion / 100)`. Grades MUST use the 0 to 20 scale, and a course MUST be considered passed only when the final grade is greater than or equal to 12.

#### Acceptance Criteria

##### Scenario: Weighted final grade passes the course

- GIVEN a student has evaluation grades 14 with weight 40 and 16 with weight 60 for a course section
- WHEN the final grade is calculated
- THEN the final grade MUST be 15.20
- AND the course MUST be marked as passed.

##### Scenario: Weighted final grade does not pass the course

- GIVEN a student has a calculated final grade of 11.99 for a course
- WHEN the course status is calculated
- THEN the course MUST NOT be marked as passed.

### Requirement: FR-004 — Credit-weighted GPA and credit totals

The system MUST calculate the student's general average as a credit-weighted GPA: `Σ(notaFinal × creditos) / Σ(creditos)` for courses with a completed final grade. The system MUST calculate approved credits, remaining credits, required credits, and completion percentage.

#### Acceptance Criteria

##### Scenario: Weighted GPA is calculated by credits

- GIVEN a student completed Course A with final grade 15 and 3 credits
- AND completed Course B with final grade 12 and 5 credits
- WHEN the weighted GPA is calculated
- THEN the GPA MUST be `(15 × 3 + 12 × 5) / 8 = 13.125`.

##### Scenario: Approved and remaining credits are calculated

- GIVEN a career requires 200 credits
- AND the student has passed curriculum courses totaling 80 credits
- WHEN progress is calculated
- THEN approved credits MUST be 80
- AND remaining credits MUST be 120
- AND completion percentage MUST be 40.00.

### Requirement: FR-005 — Course progress status map

The system MUST classify each curriculum course in the response as one of: `PASSED`, `IN_PROGRESS`, `PENDING_AVAILABLE`, `PENDING_BLOCKED`, or `FAILED`. Active enrollments MUST be shown as `IN_PROGRESS` unless the course is already passed.

#### Acceptance Criteria

##### Scenario: Active enrollment appears as in progress

- GIVEN a student has an active `Matricula` for a curriculum course
- AND the course is not passed
- WHEN progress is calculated
- THEN the course status MUST be `IN_PROGRESS`.

##### Scenario: Pending course is available

- GIVEN a curriculum course is not passed
- AND the student has no active enrollment for the course
- AND all hard prerequisites are passed
- WHEN progress is calculated
- THEN the course status MUST be `PENDING_AVAILABLE`.

##### Scenario: Pending course is blocked

- GIVEN a curriculum course is not passed
- AND at least one hard prerequisite is not passed
- WHEN progress is calculated
- THEN the course status MUST be `PENDING_BLOCKED`.

### Requirement: FR-006 — Elective course treatment

The system MUST support elective courses through the mandatory/elective flag in `MallaCurricular`. Passed elective courses MUST count toward approved credits and weighted GPA. Unpassed elective courses MUST NOT be treated as mandatory blockers for degree progress.

#### Acceptance Criteria

##### Scenario: Passed elective counts toward credits

- GIVEN a student passes an elective curriculum course worth 4 credits
- WHEN progress is calculated
- THEN approved credits MUST increase by 4
- AND the elective course MUST appear as `PASSED`.

##### Scenario: Unpassed elective does not block progress

- GIVEN an elective curriculum course is not passed
- WHEN progress is calculated
- THEN the course MUST NOT block completion of mandatory courses solely because it is elective.

### Requirement: FR-007 — Student self-service access

The system MUST allow an authenticated `ESTUDIANTE` to retrieve only their own academic progress. The system MUST deny access when a student attempts to retrieve another student's progress.

#### Acceptance Criteria

##### Scenario: Student retrieves own progress

- GIVEN an authenticated user with role `ESTUDIANTE` linked to student ID 10
- WHEN they request `GET /api/v1/historial-academico/progreso/me`
- THEN the system MUST return HTTP 200 with student ID 10 progress.

##### Scenario: Student retrieves another student's progress

- GIVEN an authenticated user with role `ESTUDIANTE` linked to student ID 10
- WHEN they request `GET /api/v1/historial-academico/progreso/estudiante/11`
- THEN the system MUST return HTTP 403.

### Requirement: FR-008 — Teacher scoped access

The system MUST allow an authenticated `DOCENTE` to retrieve academic progress only for students enrolled in sections assigned to that teacher through `DocenteSeccion`.

#### Acceptance Criteria

##### Scenario: Teacher retrieves assigned student progress

- GIVEN an authenticated user with role `DOCENTE`
- AND the teacher is assigned to a section through `DocenteSeccion`
- AND student ID 20 is enrolled in that section
- WHEN the teacher requests `GET /api/v1/historial-academico/progreso/estudiante/20`
- THEN the system MUST return HTTP 200.

##### Scenario: Teacher retrieves unassigned student progress

- GIVEN an authenticated user with role `DOCENTE`
- AND student ID 21 is not enrolled in any section assigned to that teacher
- WHEN the teacher requests `GET /api/v1/historial-academico/progreso/estudiante/21`
- THEN the system MUST return HTTP 403.

### Requirement: FR-009 — Admin access

The system MUST allow an authenticated `ADMIN` to retrieve academic progress for any existing student.

#### Acceptance Criteria

##### Scenario: Admin retrieves any student progress

- GIVEN an authenticated user with role `ADMIN`
- AND student ID 30 exists
- WHEN the admin requests `GET /api/v1/historial-academico/progreso/estudiante/30`
- THEN the system MUST return HTTP 200.

##### Scenario: Admin requests a missing student

- GIVEN an authenticated user with role `ADMIN`
- AND student ID 999 does not exist
- WHEN the admin requests `GET /api/v1/historial-academico/progreso/estudiante/999`
- THEN the system MUST return HTTP 404.

### Requirement: FR-010 — Read-only progress endpoints

The system MUST expose read-only academic progress endpoints under `/api/v1/historial-academico/progreso`. These endpoints MUST return DTOs and MUST NOT expose JPA entities.

#### Acceptance Criteria

##### Scenario: Progress endpoint returns DTO response

- GIVEN an authorized authenticated user
- WHEN they request an academic progress endpoint
- THEN the response body MUST match the DTO contract in this specification
- AND it MUST NOT serialize JPA entity graphs directly.

##### Scenario: Progress endpoints are read-only

- GIVEN the progress API path
- WHEN a client attempts to create, update, or delete progress through that path
- THEN the API MUST NOT provide mutating operations for academic progress.

### Requirement: FR-011 — Existing CRUD compatibility

The system MUST preserve the existing `HistorialAcademico` CRUD API behavior unless a future accepted spec explicitly changes it.

#### Acceptance Criteria

##### Scenario: Existing CRUD consumers remain compatible

- GIVEN a client uses existing `HistorialAcademico` CRUD endpoints
- WHEN the academic progress feature is added
- THEN those existing endpoints MUST remain available with their current purpose
- AND the new progress endpoints MUST be additive.

## Non-Functional Requirements

### Performance

- The system SHOULD avoid N+1 query behavior when loading curriculum courses, prerequisites, student enrollments, teacher assignments, evaluations, and grades.
- Repository reads used by the progress view SHOULD use fetch joins, entity graphs, projections, or equivalent bounded query strategies.
- The progress service SHOULD aggregate data in memory only after loading the necessary bounded data set for one student.

### Security

- The system MUST enforce authentication for all progress endpoints.
- The system MUST enforce server-side ownership checks with method-level authorization such as `@PreAuthorize` and SpEL-compatible helpers.
- Unauthorized authenticated access MUST return HTTP 403.
- Missing resources visible to the authorized caller MUST return HTTP 404.
- The response MUST NOT reveal academic progress data for unauthorized students.

### Compatibility

- The system MUST preserve existing CRUD paths for `HistorialAcademico`.
- The new API MUST be additive and versioned under `/api/v1`.
- The response MUST use DTOs so future frontend changes do not depend on JPA entity shape.

## API Contract

### GET `/api/v1/historial-academico/progreso/me`

Returns academic progress for the authenticated student.

| Field | Value |
|---|---|
| Method | `GET` |
| Authentication | Required JWT bearer token |
| Required role | `ESTUDIANTE` with linked student profile |
| Path params | None |
| Query params | None for this slice |
| Request body | None |

#### Headers

```http
Authorization: Bearer <jwt>
Accept: application/json
```

#### Status codes

| Status | Scenario |
|---|---|
| 200 | Authenticated student retrieves own progress |
| 401 | Missing, expired, or invalid JWT |
| 403 | Authenticated user is not allowed to use the self-student endpoint |
| 404 | Authenticated student profile cannot be found |

#### Example request

```http
GET /api/v1/historial-academico/progreso/me HTTP/1.1
Authorization: Bearer eyJ...
Accept: application/json
```

#### Example response

```json
{
  "estudiante": {
    "id": 10,
    "codigo": "20240010",
    "nombres": "Ana",
    "apellidos": "Torres"
  },
  "carrera": {
    "id": 3,
    "nombre": "Ingeniería de Sistemas",
    "creditosTotales": 200
  },
  "resumen": {
    "totalCursos": 48,
    "cursosAprobados": 20,
    "cursosEnProgreso": 3,
    "cursosPendientes": 25,
    "creditosAprobados": 80,
    "creditosRestantes": 120,
    "promedioPonderado": 13.13,
    "porcentajeAvance": 40.00
  },
  "cursos": [
    {
      "cursoId": 101,
      "codigo": "MAT101",
      "nombre": "Matemática I",
      "cicloRecomendado": 1,
      "obligatorio": true,
      "creditos": 4,
      "estado": "PASSED",
      "notaFinal": 15.20,
      "prerrequisitos": []
    }
  ]
}
```

### GET `/api/v1/historial-academico/progreso/estudiante/{estudianteId}`

Returns academic progress for the requested student when the authenticated user is authorized.

| Field | Value |
|---|---|
| Method | `GET` |
| Authentication | Required JWT bearer token |
| Required roles | `ADMIN`, `DOCENTE`, or owning `ESTUDIANTE` |
| Path params | `estudianteId: Long` |
| Query params | None for this slice |
| Request body | None |

#### Headers

```http
Authorization: Bearer <jwt>
Accept: application/json
```

#### Status codes

| Status | Scenario |
|---|---|
| 200 | Caller is authorized for the requested student |
| 400 | `estudianteId` is not a valid numeric identifier |
| 401 | Missing, expired, or invalid JWT |
| 403 | Caller is authenticated but not authorized for the requested student |
| 404 | Requested student does not exist and the caller is otherwise allowed to query students in that context |

#### Example request

```http
GET /api/v1/historial-academico/progreso/estudiante/20 HTTP/1.1
Authorization: Bearer eyJ...
Accept: application/json
```

#### Example blocked-course response excerpt

```json
{
  "estudiante": {
    "id": 20,
    "codigo": "20230020",
    "nombres": "Luis",
    "apellidos": "Ramos"
  },
  "carrera": {
    "id": 3,
    "nombre": "Ingeniería de Sistemas",
    "creditosTotales": 200
  },
  "resumen": {
    "totalCursos": 48,
    "cursosAprobados": 1,
    "cursosEnProgreso": 1,
    "cursosPendientes": 46,
    "creditosAprobados": 4,
    "creditosRestantes": 196,
    "promedioPonderado": 15.20,
    "porcentajeAvance": 2.00
  },
  "cursos": [
    {
      "cursoId": 202,
      "codigo": "MAT102",
      "nombre": "Matemática II",
      "cicloRecomendado": 2,
      "obligatorio": true,
      "creditos": 4,
      "estado": "PENDING_BLOCKED",
      "notaFinal": null,
      "prerrequisitos": [
        {
          "cursoId": 101,
          "codigo": "MAT101",
          "nombre": "Matemática I",
          "tipoRegla": "HARD",
          "cumplido": false
        }
      ]
    }
  ]
}
```

## Proposed DTOs

```java
public class HistorialProgresoResponseDto {
    private EstudianteResumenDto estudiante;
    private CarreraResumenDto carrera;
    private ProgresoResumenDto resumen;
    private List<CursoProgresoDto> cursos;
}

public class EstudianteResumenDto {
    private Long id;
    private String codigo;
    private String nombres;
    private String apellidos;
}

public class CarreraResumenDto {
    private Long id;
    private String nombre;
    private Integer creditosTotales;
}

public class ProgresoResumenDto {
    private Integer totalCursos;
    private Integer cursosAprobados;
    private Integer cursosEnProgreso;
    private Integer cursosPendientes;
    private Integer creditosAprobados;
    private Integer creditosRestantes;
    private BigDecimal promedioPonderado;
    private BigDecimal porcentajeAvance;
}

public class CursoProgresoDto {
    private Long cursoId;
    private String codigo;
    private String nombre;
    private Integer cicloRecomendado;
    private Boolean obligatorio;
    private Integer creditos;
    private EstadoCursoProgreso estado;
    private BigDecimal notaFinal;
    private List<PrerrequisitoProgresoDto> prerrequisitos;
}

public class PrerrequisitoProgresoDto {
    private Long cursoId;
    private String codigo;
    private String nombre;
    private TipoReglaPrerrequisito tipoRegla;
    private Boolean cumplido;
}

public enum EstadoCursoProgreso {
    PASSED,
    IN_PROGRESS,
    PENDING_AVAILABLE,
    PENDING_BLOCKED,
    FAILED
}

public enum TipoReglaPrerrequisito {
    HARD
}
```

## Authorization Matrix

| Actor | `/progreso/me` | `/progreso/estudiante/{ownId}` | `/progreso/estudiante/{otherId}` |
|---|---:|---:|---:|
| Anonymous | 401 | 401 | 401 |
| ESTUDIANTE | 200 | 200 | 403 |
| DOCENTE assigned through `DocenteSeccion` | 403 | N/A | 200 for assigned students |
| DOCENTE not assigned | 403 | N/A | 403 |
| ADMIN | 403 | 200 | 200 |

## Data Model

### `MallaCurricular`

`MallaCurricular` MUST define membership of courses in a career curriculum.

| Field | Type | Required | Rule |
|---|---|---:|---|
| `id` | `Long` | Yes | Primary key |
| `carrera` | `Carrera` many-to-one | Yes | Student career that owns the curriculum entry |
| `curso` | `Curso` many-to-one | Yes | Course included in the curriculum |
| `cicloRecomendado` | `Integer` | Yes | MUST be greater than or equal to 1 |
| `obligatorio` | `Boolean` | Yes | `true` for mandatory, `false` for elective |
| `creditos` | `Integer` | Yes | MUST be greater than 0; default value SHOULD match `Curso.creditos` |

Constraints:

- The system MUST prevent duplicate active curriculum entries for the same `(carrera, curso)` pair.
- The system MUST keep `creditos` positive.
- The system MUST keep `cicloRecomendado` positive.

### `Prerrequisito`

`Prerrequisito` MUST define hard prerequisite relationships for curriculum courses.

| Field | Type | Required | Rule |
|---|---|---:|---|
| `id` | `Long` | Yes | Primary key |
| `carrera` | `Carrera` many-to-one | Yes | Career context for the prerequisite rule |
| `curso` | `Curso` many-to-one | Yes | Target course that may be blocked |
| `cursoPrerrequisito` | `Curso` many-to-one | Yes | Course that must be passed first |
| `tipoRegla` | `TipoReglaPrerrequisito` | Yes | MUST support `HARD` for this slice |

Constraints:

- The system MUST prevent duplicate prerequisite rules for `(carrera, curso, cursoPrerrequisito)`.
- The system MUST reject rules where `curso` and `cursoPrerrequisito` are the same course.
- The system SHOULD only allow prerequisite rules for courses that belong to the same career curriculum.

### Existing entity changes

`Carrera` SHOULD expose `creditosTotales` so required credits can be calculated consistently for the degree. When `creditosTotales` is configured, remaining credits MUST be `max(creditosTotales - creditosAprobados, 0)`.

Existing `HistorialAcademico`, `Matricula`, `Nota`, `Evaluacion`, `Curso`, `Seccion`, `Estudiante`, and `DocenteSeccion` entities MUST remain compatible with their current CRUD responsibilities.

## Detailed Scenarios

### Scenario: Student with passed, pending, and blocked courses

- GIVEN a student belongs to a career with courses MAT101, MAT102, and MAT201 in `MallaCurricular`
- AND MAT102 requires MAT101
- AND MAT201 requires MAT102
- AND the student passed MAT101
- AND the student has not passed MAT102
- WHEN the student requests their progress
- THEN MAT101 MUST be `PASSED`
- AND MAT102 MUST be `PENDING_AVAILABLE`
- AND MAT201 MUST be `PENDING_BLOCKED`.

### Scenario: Teacher queries a student in their section

- GIVEN a teacher is assigned to a section through `DocenteSeccion`
- AND a student is enrolled in that section
- WHEN the teacher requests the student's progress
- THEN the system MUST return HTTP 200.

### Scenario: Teacher queries a student outside their sections

- GIVEN a teacher is not assigned to any section where the student is enrolled
- WHEN the teacher requests that student's progress
- THEN the system MUST return HTTP 403.

### Scenario: Student queries another student

- GIVEN student user A is authenticated
- WHEN student user A requests progress for student B
- THEN the system MUST return HTTP 403.

### Scenario: Admin queries any student

- GIVEN an admin user is authenticated
- WHEN the admin requests progress for an existing student
- THEN the system MUST return HTTP 200.

### Scenario: Weighted average calculation

- GIVEN a student completed three curriculum courses with final grades and credits
- WHEN progress is calculated
- THEN `promedioPonderado` MUST equal `Σ(notaFinal × creditos) / Σ(creditos)`
- AND pending courses MUST NOT contribute to the denominator.

### Scenario: Active enrollment appears in course map

- GIVEN a student has an active enrollment in a curriculum course
- AND the course is not passed
- WHEN progress is calculated
- THEN the course MUST appear as `IN_PROGRESS`.

### Scenario: Elective course counts but does not block

- GIVEN a student passed an elective curriculum course
- WHEN progress is calculated
- THEN its credits MUST count toward approved credits
- AND an unpassed elective MUST NOT block mandatory course progression unless an explicit hard prerequisite rule exists.

## Open Questions

1. If a student repeats the same course, which attempt MUST be used for GPA and pass/fail status: latest completed attempt, highest grade, or another institutional rule?
2. Should `creditosTotales` be mandatory in `Carrera`, or may it be derived from `MallaCurricular` when not configured?
3. Should prerequisite cycles be validated to prevent a later-cycle course from being a prerequisite of an earlier-cycle course?
4. What seed/import process will populate initial `MallaCurricular` and `Prerrequisito` data while the project still relies on `ddl-auto:update`?
