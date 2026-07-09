# Tasks — Historial Académico

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 1,600–2,300 additions/deletions |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: curriculum data model + repositories → PR 2: progress service algorithm → PR 3: security + API contract → PR 4: seed rollout + integration verification |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

Decision needed: choose a chain strategy before apply (`stacked-to-main` or `feature-branch-chain`) unless the maintainer explicitly accepts a `size:exception` for a single PR.

## TDD and delivery notes

### Apply checkbox tracking

- [x] T-001 — RED: curriculum entity/repository tests
- [x] T-002 — GREEN: curriculum entities and nullable course code
- [x] T-003 — GREEN: repository fetch queries
- [x] T-004 — TRIANGULATE: existing repository integration coverage
- [x] T-005 — RED: progress service calculation tests
- [x] T-006 — GREEN: progress service and DTO contract
- [x] T-007 — TRIANGULATE: algorithm edge cases
- [x] T-008 — REFACTOR: service readability and deterministic helpers
- [x] T-009 — RED: authorization helper tests
- [x] T-010 — GREEN: SpEL security helper and docente query
- [x] T-011 — RED: progress controller and JSON contract tests
- [x] T-012 — GREEN: read-only controller and exception mappings
- [x] T-013 — TRIANGULATE: API/security matrix coverage
- [x] T-014 — TRIANGULATE: integration smoke for full progress flow
- [x] T-015 — REFACTOR: test maintainability pass
- [x] T-016 — Seed SQL for InsForge CLI
- [x] T-017 — Rollout checklist and rollback notes
- [ ] T-018 — Full backend verification
- [ ] T-019 — PR slicing and review readiness

- Strict TDD is active for this change. Each implementation slice starts with RED tests, then GREEN implementation, then TRIANGULATE coverage, then REFACTOR.
- Backend verification target: `cd server && ./mvnw test` with the project PostgreSQL test setup (`@DataJpaTest` uses `Replace.NONE`).
- Keep `Matricula.estado` and `HistorialAcademico.estado` as `String`.
- Do not persist `Carrera.creditosTotales`; derive it from `sum(MallaCurricular.creditos)`.
- Add `Curso.codigo` as nullable and backfill through seed SQL.

## Phase 1 — Modelos y datos

### T-001 — RED: curriculum entity/repository tests

- **ID**: T-001
- **Título**: RED tests for curriculum model persistence
- **Dependencias**: none
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/historial/repository/MallaCurricularRepositoryTest.java`
  - `server/src/test/java/com/example/gestionacademica/historial/repository/PrerrequisitoRepositoryTest.java`
- **Archivos a modificar**:
  - none
- **Descripción**: Add failing `@DataJpaTest` cases for unique curriculum entries, positive credits/cycle expectations, prerequisite uniqueness, self-prerequisite rejection, and fetch access to `Curso` without lazy failures.
- **Criterios de aceptación**:
  - Tests fail because `MallaCurricular`, `Prerrequisito`, enums, and repositories do not exist yet.
  - Tests use PostgreSQL-compatible JPQL/JPA behavior only.
- **Esfuerzo estimado**: M

### T-002 — GREEN: curriculum entities and nullable course code

- **ID**: T-002
- **Título**: Implement curriculum entities and enums
- **Dependencias**: T-001
- **Archivos a crear**:
  - `server/src/main/java/com/example/gestionacademica/historial/domain/MallaCurricular.java`
  - `server/src/main/java/com/example/gestionacademica/historial/domain/Prerrequisito.java`
  - `server/src/main/java/com/example/gestionacademica/historial/domain/EstadoCursoProgreso.java`
  - `server/src/main/java/com/example/gestionacademica/historial/domain/TipoReglaPrerrequisito.java`
- **Archivos a modificar**:
  - `server/src/main/java/com/example/gestionacademica/cursos/domain/Curso.java`
- **Descripción**: Implement JPA mappings, table/index/unique constraints, validation annotations, `TipoReglaPrerrequisito.HARD`, status enum values, self-prerequisite guard, and nullable unique `Curso.codigo`.
- **Criterios de aceptación**:
  - T-001 tests compile past missing classes.
  - `malla_curricular` and `prerrequisito` are additive tables under `ddl-auto:update`.
  - `curso.codigo` remains nullable for existing rows.
- **Esfuerzo estimado**: M

### T-003 — GREEN: repository fetch queries

- **ID**: T-003
- **Título**: Implement bounded data queries
- **Dependencias**: T-002
- **Archivos a crear**:
  - `server/src/main/java/com/example/gestionacademica/historial/repository/MallaCurricularRepository.java`
  - `server/src/main/java/com/example/gestionacademica/historial/repository/PrerrequisitoRepository.java`
- **Archivos a modificar**:
  - `server/src/main/java/com/example/gestionacademica/estudiantes/repository/EstudianteRepository.java`
  - `server/src/main/java/com/example/gestionacademica/historial/repository/HistorialAcademicoRepository.java`
  - `server/src/main/java/com/example/gestionacademica/matriculas/repository/MatriculaRepository.java`
  - `server/src/main/java/com/example/gestionacademica/notas/repository/NotaRepository.java`
- **Descripción**: Add fetch-join/entity-graph queries for student+career, curriculum+course, prerequisites+courses, history+section+course+cycle, active enrollments, and notes+evaluation weights.
- **Criterios de aceptación**:
  - T-001 repository tests pass for new repositories.
  - Existing repository methods remain backward compatible.
  - Queries load all relationships required by `HistorialProgresoService` without relying on open-session-in-view.
- **Esfuerzo estimado**: M

### T-004 — TRIANGULATE: existing repository integration coverage

- **ID**: T-004
- **Título**: Cover existing fetch query behavior
- **Dependencias**: T-003
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/estudiantes/repository/EstudianteRepositoryProgressTest.java`
  - `server/src/test/java/com/example/gestionacademica/historial/repository/HistorialAcademicoRepositoryProgressTest.java`
  - `server/src/test/java/com/example/gestionacademica/matriculas/repository/MatriculaRepositoryProgressTest.java`
  - `server/src/test/java/com/example/gestionacademica/notas/repository/NotaRepositoryProgressTest.java`
- **Archivos a modificar**:
  - existing repository test fixtures under `server/src/test/java/com/example/gestionacademica/**` if present
- **Descripción**: Add integration tests for the new fetch methods, including active `Matricula.estado = 'ACTIVA'`, attempted section IDs, and evaluation percentage loading.
- **Criterios de aceptación**:
  - `cd server && ./mvnw test -Dtest='*ProgressTest,*RepositoryTest'` passes with the real PostgreSQL test configuration.
  - Lazy initialization errors are not needed to read fetched relationships in assertions.
- **Esfuerzo estimado**: M

## Phase 2 — Lógica de negocio

### T-005 — RED: progress service calculation tests

- **ID**: T-005
- **Título**: RED tests for progress algorithm
- **Dependencias**: T-003
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/historial/service/HistorialProgresoServiceTest.java`
  - `server/src/test/java/com/example/gestionacademica/historial/support/HistorialProgresoTestData.java`
- **Archivos a modificar**:
  - none
- **Descripción**: Add failing Mockito/unit tests for weighted final grade, pass threshold `>= 12.00`, latest attempt wins, older pass/latest fail, active enrollment, prerequisites, weighted GPA, credits, completion percentage, electives, and missing student.
- **Criterios de aceptación**:
  - Tests fail because `HistorialProgresoService` and DTOs/exceptions are not implemented.
  - Test data includes `Nota` + `Evaluacion.porcentaje`; no fallback to `HistorialAcademico.notaFinal` is asserted.
- **Esfuerzo estimado**: L

### T-006 — GREEN: progress service and DTO contract

- **ID**: T-006
- **Título**: Implement progress calculation service
- **Dependencias**: T-005
- **Archivos a crear**:
  - `server/src/main/java/com/example/gestionacademica/historial/service/HistorialProgresoService.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/HistorialProgresoResponseDto.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/EstudianteResumenDto.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/CarreraResumenDto.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/ProgresoResumenDto.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/CursoProgresoDto.java`
  - `server/src/main/java/com/example/gestionacademica/historial/dto/PrerrequisitoProgresoDto.java`
  - `server/src/main/java/com/example/gestionacademica/exceptions/EstudianteNotFoundException.java`
- **Archivos a modificar**:
  - none
- **Descripción**: Implement `calcularProgreso(Integer estudianteId)` using bounded repositories, BigDecimal weighted grade formula, latest-attempt ordering, status priority `PASSED → IN_PROGRESS → PENDING_BLOCKED → FAILED → PENDING_AVAILABLE`, GPA, credits, and DTO mapping.
- **Criterios de aceptación**:
  - T-005 tests pass.
  - `creditosTotales` is derived from `MallaCurricular.creditos`.
  - Course states and numeric values round to 2 decimals with `HALF_UP` where returned.
- **Esfuerzo estimado**: L

### T-007 — TRIANGULATE: algorithm edge cases

- **ID**: T-007
- **Título**: Add edge-case progress scenarios
- **Dependencias**: T-006
- **Archivos a crear**:
  - none
- **Archivos a modificar**:
  - `server/src/test/java/com/example/gestionacademica/historial/service/HistorialProgresoServiceTest.java`
  - `server/src/test/java/com/example/gestionacademica/historial/support/HistorialProgresoTestData.java`
- **Descripción**: Add tests for empty curriculum, no completed attempts, failed but blocked course precedence, active retry overriding failed status, prerequisite DTO fulfillment, and elective non-blocking behavior.
- **Criterios de aceptación**:
  - Added tests pass without changing public API shape.
  - No course outside the student's `MallaCurricular` contributes to pending courses, GPA denominator, or credits.
- **Esfuerzo estimado**: M

### T-008 — REFACTOR: service readability and deterministic helpers

- **ID**: T-008
- **Título**: Refactor progress service internals
- **Dependencias**: T-007
- **Archivos a crear**:
  - optional package-private helper classes under `server/src/main/java/com/example/gestionacademica/historial/service/`
- **Archivos a modificar**:
  - `server/src/main/java/com/example/gestionacademica/historial/service/HistorialProgresoService.java`
  - `server/src/test/java/com/example/gestionacademica/historial/service/HistorialProgresoServiceTest.java`
- **Descripción**: Extract small private/package-private helpers for grade calculation, latest-attempt selection, prerequisite DTO creation, status classification, and summary calculation if the service becomes difficult to review.
- **Criterios de aceptación**:
  - All T-005/T-007 tests still pass.
  - No behavior changes; only reduced method size/duplication and clearer names.
- **Esfuerzo estimado**: S

## Phase 3 — Seguridad

### T-009 — RED: authorization helper tests

- **ID**: T-009
- **Título**: RED tests for progress authorization
- **Dependencias**: T-003
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/historial/service/HistorialProgresoSecurityTest.java`
  - `server/src/test/java/com/example/gestionacademica/docentes/repository/DocenteSeccionRepositoryProgressTest.java`
- **Archivos a modificar**:
  - `server/pom.xml` if `spring-security-test` is absent
- **Descripción**: Add tests for ADMIN all access, ESTUDIANTE own-only access, DOCENTE assigned-only access, unauthenticated denial, and repository true/false docente-student assignment checks.
- **Criterios de aceptación**:
  - Tests fail because `HistorialProgresoSecurity` and/or docente assignment query are not implemented.
  - Test setup uses Spring Security test utilities only in test scope.
- **Esfuerzo estimado**: M

### T-010 — GREEN: SpEL security helper and docente query

- **ID**: T-010
- **Título**: Implement progress authorization helper
- **Dependencias**: T-009
- **Archivos a crear**:
  - `server/src/main/java/com/example/gestionacademica/historial/service/HistorialProgresoSecurity.java`
- **Archivos a modificar**:
  - `server/src/main/java/com/example/gestionacademica/docentes/repository/DocenteSeccionRepository.java`
  - `server/pom.xml` if `spring-security-test` is absent
- **Descripción**: Add `@Component("historialProgresoSecurity")`, role checks against `Authentication`, principal ID extraction from `Usuario`, and `existsDocenteAssignedToEstudiante(Integer idDocente, Integer idEstudiante)`.
- **Criterios de aceptación**:
  - T-009 tests pass.
  - No authorization decision is delegated to the frontend.
  - Missing/anonymous authentication returns `false` from the helper.
- **Esfuerzo estimado**: M

## Phase 4 — API

### T-011 — RED: progress controller and JSON contract tests

- **ID**: T-011
- **Título**: RED tests for read-only progress API
- **Dependencias**: T-006, T-010
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/historial/controller/HistorialProgresoControllerTest.java`
- **Archivos a modificar**:
  - `server/pom.xml` if `spring-security-test` is absent
- **Descripción**: Add `@WebMvcTest`/MVC tests for `/api/v1/historial-academico/progreso/me`, `/estudiante/{estudianteId}`, 401/403 paths, JSON DTO fields, enum serialization, and absence of mutating routes under `/progreso`.
- **Criterios de aceptación**:
  - Tests fail because `HistorialProgresoController` and/or exception mappings are missing.
  - Tests assert DTO JSON fields, not JPA entity serialization.
- **Esfuerzo estimado**: M

### T-012 — GREEN: read-only controller and exception mappings

- **ID**: T-012
- **Título**: Implement progress endpoints
- **Dependencias**: T-011
- **Archivos a crear**:
  - `server/src/main/java/com/example/gestionacademica/historial/controller/HistorialProgresoController.java`
- **Archivos a modificar**:
  - `server/src/main/java/com/example/gestionacademica/exceptions/GlobalExceptionHandler.java`
- **Descripción**: Add read-only GET endpoints, `@PreAuthorize` annotations, principal-derived `/me` lookup, path-based authorized lookup, and specific handlers for `EstudianteNotFoundException`, `AccessDeniedException`, and `MethodArgumentTypeMismatchException` if not already covered.
- **Criterios de aceptación**:
  - T-011 tests pass.
  - No POST/PUT/PATCH/DELETE mappings exist under `/api/v1/historial-academico/progreso`.
  - Existing `HistorialAcademicoController` CRUD routes remain unchanged.
- **Esfuerzo estimado**: M

### T-013 — TRIANGULATE: API/security matrix coverage

- **ID**: T-013
- **Título**: Complete role matrix tests
- **Dependencias**: T-012
- **Archivos a crear**:
  - none
- **Archivos a modificar**:
  - `server/src/test/java/com/example/gestionacademica/historial/controller/HistorialProgresoControllerTest.java`
  - `server/src/test/java/com/example/gestionacademica/historial/service/HistorialProgresoSecurityTest.java`
- **Descripción**: Add missing cases from the authorization matrix: anonymous 401, ESTUDIANTE `/me` 200, ESTUDIANTE other 403, DOCENTE `/me` 403, DOCENTE assigned 200, DOCENTE unassigned 403, ADMIN any student 200, missing student 404 when authorized.
- **Criterios de aceptación**:
  - The full authorization matrix in `spec.md` is covered by tests.
  - Unauthorized responses do not invoke or expose progress data for forbidden students.
- **Esfuerzo estimado**: M

## Phase 5 — Testing

### T-014 — TRIANGULATE: integration smoke for full progress flow

- **ID**: T-014
- **Título**: Add end-to-end backend smoke test
- **Dependencias**: T-004, T-013
- **Archivos a crear**:
  - `server/src/test/java/com/example/gestionacademica/historial/HistorialProgresoIntegrationTest.java`
- **Archivos a modificar**:
  - `server/src/test/java/com/example/gestionacademica/historial/support/HistorialProgresoTestData.java`
- **Descripción**: Add one realistic backend integration test that persists career, courses, curriculum, prerequisites, enrollment/history/notes, then verifies the service/controller response shape and key calculations.
- **Criterios de aceptación**:
  - Test passes against the configured PostgreSQL test database.
  - The response includes passed, in-progress, blocked, failed, and pending-available statuses in a minimal fixture where practical.
- **Esfuerzo estimado**: L

### T-015 — REFACTOR: test maintainability pass

- **ID**: T-015
- **Título**: Consolidate test fixtures
- **Dependencias**: T-014
- **Archivos a crear**:
  - optional shared fixture classes under `server/src/test/java/com/example/gestionacademica/historial/support/`
- **Archivos a modificar**:
  - `server/src/test/java/com/example/gestionacademica/historial/**/*.java`
  - `server/src/test/java/com/example/gestionacademica/**/repository/*ProgressTest.java`
- **Descripción**: Remove duplicated setup while keeping tests readable and independent. Keep builders close to the historial progress tests unless a broader project test factory already exists.
- **Criterios de aceptación**:
  - `cd server && ./mvnw test` passes.
  - Test fixture changes do not alter production code behavior.
- **Esfuerzo estimado**: S

## Phase 6 — Seed data & rollout

### T-016 — Seed SQL for InsForge CLI

- **ID**: T-016
- **Título**: Prepare curriculum seed script
- **Dependencias**: T-002, T-003
- **Archivos a crear**:
  - `openspec/changes/historial-academico/seed-insforge.sql`
- **Archivos a modificar**:
  - none
- **Descripción**: Create SQL for backfilling nullable `curso.codigo`, inserting minimum `malla_curricular` rows, inserting `HARD` prerequisites, and validating total curriculum credits per career. Use PostgreSQL `on conflict` clauses matching the JPA unique constraints.
- **Criterios de aceptación**:
  - Script can be reviewed before running in InsForge.
  - Script is idempotent for repeated application.
  - Script does not mutate `Matricula.estado`, `HistorialAcademico.estado`, or existing final grades.
- **Esfuerzo estimado**: M

### T-017 — Rollout checklist and rollback notes

- **ID**: T-017
- **Título**: Document rollout verification
- **Dependencias**: T-016
- **Archivos a crear**:
  - `openspec/changes/historial-academico/rollout.md`
- **Archivos a modificar**:
  - none
- **Descripción**: Document InsForge CLI execution order, required environment cautions, validation SQL, smoke API calls, rollback by disabling frontend consumption/removing seed rows, and known limitation for legacy histories without `Nota` rows.
- **Criterios de aceptación**:
  - Rollout document includes pre-check, apply, verify, and rollback steps.
  - No credentials or secrets are committed.
- **Esfuerzo estimado**: S

## Phase 7 — Integración y verificación

### T-018 — Full backend verification

- **ID**: T-018
- **Título**: Run complete backend verification
- **Dependencias**: T-015, T-017
- **Archivos a crear**:
  - none
- **Archivos a modificar**:
  - `openspec/changes/historial-academico/tasks.md` only to mark completed tasks during apply
- **Descripción**: Run the full backend test suite and manually inspect the progress endpoint contract against `spec.md` using seeded or fixture data.
- **Criterios de aceptación**:
  - `cd server && ./mvnw test` passes.
  - Manual/API verification confirms GPA, credits, statuses, prerequisites, and authorization outcomes.
  - Any failing or skipped PostgreSQL-dependent test is documented with root cause and remediation.
- **Esfuerzo estimado**: M

### T-019 — PR slicing and review readiness

- **ID**: T-019
- **Título**: Prepare reviewable delivery slices
- **Dependencias**: T-018
- **Archivos a crear**:
  - none
- **Archivos a modificar**:
  - `openspec/changes/historial-academico/tasks.md` only to record final completion status if using OpenSpec task checkoff
- **Descripción**: Split completed work according to the selected chain strategy and keep tests with the work unit they verify. Suggested boundaries: data model/repositories, service algorithm, security/API, seed/rollout/integration.
- **Criterios de aceptación**:
  - Each PR/work unit has a clear start, finish, verification command, and rollback boundary.
  - No PR exceeds the 400-line review budget unless `size:exception` is explicitly accepted.
  - Existing CRUD behavior is not included in the diff except for required additive repository methods/exception handlers.
- **Esfuerzo estimado**: S

## Coverage map

| Spec requirement | Primary tasks |
|---|---|
| FR-001 Curriculum map | T-001, T-002, T-003, T-004 |
| FR-002 Prerequisites | T-001, T-002, T-003, T-005, T-006 |
| FR-003 Final grade formula | T-005, T-006, T-007 |
| FR-004 GPA and credits | T-005, T-006, T-014 |
| FR-005 Course statuses | T-005, T-006, T-007, T-014 |
| FR-006 Electives | T-005, T-006, T-007 |
| FR-007 Student access | T-009, T-010, T-011, T-013 |
| FR-008 Teacher access | T-009, T-010, T-013 |
| FR-009 Admin access | T-009, T-010, T-013 |
| FR-010 Read-only endpoints | T-011, T-012, T-013 |
| FR-011 CRUD compatibility | T-012, T-018 |

## Final workload summary

- Total estimated changed lines: 1,600–2,300.
- Chained PRs recommended: Yes, because the expected implementation is well over 400 changed lines and spans schema, repositories, service logic, security, API, tests, and rollout SQL.
- Decision needed before apply: Yes — choose chain strategy or approve `size:exception`.
- Reviewer burnout risk: High without chaining; Medium if split into the suggested PR sequence.
