# Proposal — Historial Académico

## Executive Summary

Historial Académico will evolve from a CRUD of final grades into a read-only academic progress view for each student's full degree path. The feature will show passed and pending courses, prerequisites, GPA, approved credits, remaining credits, and degree completion progress.

The first SDD slice focuses on the backend: new academic-plan models, secure read-only REST endpoints, DTOs, service-level calculations, and repository queries that can produce the progress view without exposing unauthorized student data. The frontend Angular/Tailwind view is acknowledged as the consumer of these APIs and should be implemented in a later phase or follow-up slice.

## Problem Statement

Today the system has the raw pieces for academic activity but not the domain model required to explain degree progress.

Existing capabilities:

- `HistorialAcademico` stores final grade records by student and section.
- `Curso` has credits.
- `Estudiante` belongs to a `Carrera`.
- `Matricula`, `Nota`, `Evaluacion`, and `DocenteSeccion` describe enrollment, grades, evaluations, and teacher-section assignment.
- Authentication exists through JWT and roles: `ADMIN`, `DOCENTE`, `ESTUDIANTE`.

Current gaps:

- `Carrera` has no study plan, so the system cannot know which courses belong to a degree.
- There is no prerequisite/correlative model between courses.
- Required credits per degree are not represented.
- `Matricula.estado` and `HistorialAcademico.estado` are untyped strings, which makes business rules fragile.
- Endpoints are authenticated but not protected by role ownership rules.
- Current repositories do not explicitly fetch nested relationships, increasing N+1 risk.
- Database evolution currently relies on `ddl-auto:update`, with limited migration traceability.

## Proposed Solution

Add a backend-centered academic progress capability that computes a student's degree progress from the existing academic records plus two new domain models:

- `MallaCurricular`: defines the courses that belong to a career, recommended order/cycle, credits, and whether the course is required.
- `Prerrequisito`: defines prerequisite relationships between courses in a study plan.

The service layer will assemble a read-only progress response for one student, applying role-based access rules and returning DTOs tailored for the future Angular view. Existing grade records remain the source for completed course outcomes, while the new plan models provide the missing curriculum map.

## Key Features

### Complete career progress view

Provide a single read-only response that summarizes a student's progress through their degree:

- Career and student identity metadata.
- Total courses in the plan.
- Passed courses.
- Pending courses.
- Courses blocked or enabled by prerequisites.
- Approved, remaining, and required credits.
- GPA metrics.

### New models: `MallaCurricular` and `Prerrequisito`

Introduce academic-plan models so progress can be computed against the full degree plan, not only against courses the student has already taken.

`MallaCurricular` should associate:

- Career.
- Course.
- Recommended cycle/order.
- Required/elective flag, if needed for the first slice.
- Credits to count toward the degree, using `Curso.creditos` as the default source unless a plan-specific override is later required.

`Prerrequisito` should associate:

- Study plan course.
- Required previous course.
- Rule type if the implementation needs to distinguish hard prerequisite vs advisory prerequisite in a later phase.

### GPA calculation

Calculate both:

- Simple GPA: arithmetic average of final approved/attempted grades according to the chosen business rule.
- Credit-weighted GPA: weighted by course credits.

The proposal assumes `HistorialAcademico.notaFinal` is the first source of truth for final course outcomes. If a final record is missing, the system may later derive final grades from `Nota` + `Evaluacion`, but that derivation should not be mixed silently into the first backend slice without a clear rule.

### Approved and remaining credits

Use the curriculum plan and final academic history to calculate:

- Required credits for the career.
- Approved credits.
- Remaining credits.
- Completion percentage.

### Course map: passed, pending, prerequisites

Return a course-level map where each course can be classified as:

- Passed.
- Failed or previously attempted.
- Currently enrolled, if enrollment data is considered in the first implementation.
- Pending and available.
- Pending and blocked by unmet prerequisites.

### Role-based access control

Protect progress endpoints with method-level authorization using `@PreAuthorize` and SpEL-backed ownership checks.

Access rules:

- `ESTUDIANTE`: can view only their own academic progress.
- `DOCENTE`: can view progress only for students in sections assigned to that teacher.
- `ADMIN`: can view all students.

### Read-only REST endpoints

Add read-only endpoints for the progress view. Suggested paths:

- `GET /api/v1/historial-academico/progreso/me` for the authenticated student.
- `GET /api/v1/historial-academico/progreso/estudiante/{estudianteId}` for authorized admins and docentes.

The existing CRUD endpoints for `HistorialAcademico` should remain compatible unless later specs explicitly redefine them.

## Scope

### In scope

- Backend models for curriculum plan and prerequisites.
- Repository queries needed to load plans, prerequisites, student history, and teaching assignments efficiently.
- Service that computes academic progress, GPA, credits, and course status.
- DTOs designed for a read-only progress view.
- REST endpoints for authenticated and authorized progress lookup.
- Method-level access control with `@PreAuthorize`.
- Backend tests for calculations, access rules, and query behavior where practical.
- A backend contract that the future Angular/Tailwind view can consume.

### Out of scope

- Export to PDF, Excel, or other downloadable formats.
- Editing grades or academic history from the progress view.
- Full frontend implementation as part of the first backend-focused slice.
- Complex curriculum versioning by student cohort unless required by later discovery.
- Automatic migration from all historical enrollment/grade edge cases without explicit data rules.
- Replacing the existing `HistorialAcademico` CRUD module.

## User Stories / Use Cases

1. As a student, I want to see my approved courses, pending courses, GPA, and remaining credits so I understand how far I am from completing my degree.
2. As a student, I want to know which pending courses are blocked by prerequisites so I can plan my next enrollment period.
3. As a teacher, I want to view the academic progress of students in my assigned sections so I can advise them with relevant context.
4. As an admin, I want to view any student's academic progress so I can support academic operations and resolve student inquiries.
5. As an academic operator, I want progress to be computed from a formal study plan so reports are consistent and explainable.

## Technical Approach

### Backend

- Add JPA entities for `MallaCurricular` and `Prerrequisito` under an appropriate academic-plan package or a focused subpackage near `historial`/`catalogos`.
- Add repositories with explicit fetch strategies for:
  - Study plan by career.
  - Prerequisites for courses in the plan.
  - Final academic history by student with section and course data.
  - Teacher-student access checks through `DocenteSeccion` and `Matricula`.
- Add DTOs for the progress response instead of exposing entities directly.
- Add a `HistorialProgresoService` or equivalent application service to centralize calculations.
- Prefer fetch joins or entity graphs for nested relationships to avoid N+1 query behavior.
- Keep endpoints read-only and separate from the existing CRUD operations.

### Frontend

The first SDD focus is backend. The frontend should later add an Angular view that consumes the progress endpoints and presents:

- Summary cards for GPA, credits, and completion percentage.
- A curriculum map grouped by cycle or recommended order.
- Course status indicators for passed, pending, and prerequisite-blocked courses.

The view should follow the existing Angular/Tailwind/Taiga UI architecture, but implementation details belong to the frontend phase or a later SDD slice.

### Security

- Use `@PreAuthorize` on progress endpoints.
- Implement SpEL-compatible authorization helpers where direct role checks are not enough.
- Enforce ownership server-side; the frontend must not be trusted to hide unauthorized data.
- Return forbidden/not-found behavior consistently with the project's global exception strategy.

### Data and migration

- Define database changes for `MallaCurricular`, `Prerrequisito`, and any `Carrera` required-credit fields.
- Seed or migrate initial curriculum data before the progress endpoint is considered complete.
- If migrations are introduced later, keep schema changes explicit and reviewable instead of relying only on `ddl-auto:update`.

## Affected Areas

- Backend domain model for careers, courses, and academic history.
- Historial Académico API surface.
- Security authorization rules.
- Repository query performance.
- Future Angular student/admin/teacher academic progress screens.
- Database schema and seed data for curriculum plans.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Existing data lacks curriculum plan records | Progress cannot classify pending courses | Introduce `MallaCurricular` and seed minimum plans before enabling the endpoint for real use |
| Prerequisite rules are incomplete or ambiguous | Courses may be shown as available/blocked incorrectly | Model prerequisites explicitly and expose open questions before implementation |
| GPA source of truth is unclear | Different screens may show different averages | Use `HistorialAcademico.notaFinal` as the first source of truth and document any future derivation from `Nota`/`Evaluacion` |
| Query changes create N+1 or slow responses | Progress view becomes expensive for admins/docentes | Use fetch joins/entity graphs and service-level aggregation; add focused tests or query review |
| Role/ownership checks are too broad | Unauthorized academic data exposure | Enforce `@PreAuthorize` and backend ownership checks for students and docentes |
| Migration changes affect existing CRUD behavior | Current historial workflows may break | Keep new progress endpoints read-only and preserve existing CRUD contracts |

## Rollback Plan

- Keep the new progress endpoints additive and read-only so they can be disabled or removed without changing existing CRUD behavior.
- If curriculum data is incomplete, hide or disable the progress view while keeping existing `HistorialAcademico` endpoints available.
- Roll back frontend consumption independently because the backend contract is additive.
- For schema changes, prefer reversible migrations or clearly isolated DDL so `MallaCurricular`/`Prerrequisito` tables can be removed without touching existing grade history tables.

## Success Criteria

- Authorized students can retrieve only their own academic progress.
- Authorized docentes can retrieve progress only for students they teach.
- Admins can retrieve any student's progress.
- The response includes GPA, credit totals, completion percentage, passed courses, pending courses, and prerequisite status.
- Pending courses are computed from the career study plan, not inferred only from existing enrollments.
- Existing `HistorialAcademico` CRUD behavior remains compatible.
- Backend tests cover calculation rules and the main authorization paths.

## Open Questions

1. What is the exact passing threshold for a course: is `notaFinal >= 11` always the approval rule, or can it vary by course/career?
2. Should GPA include only approved courses, all final attempts, or the latest attempt per course?
3. Can a career have multiple curriculum versions by admission year/cohort, or is one active plan per career enough for the first slice?
4. Are elective courses needed in the first slice, or can all plan courses be treated as required initially?
5. Should current active enrollments affect the course status map in the first backend slice, or should the first response only classify passed vs pending vs prerequisite-blocked?

## Next Steps

1. Write the SDD spec defining functional requirements, acceptance criteria, API contract, and authorization rules.
2. Write the technical design for entities, repositories, services, DTOs, endpoint paths, and security helpers.
3. Break the implementation into tasks with backend-first reviewable slices.
4. Implement and verify backend behavior before starting the Angular progress view.
