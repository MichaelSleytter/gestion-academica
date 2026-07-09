# Historial Académico Rollout

Roll out the read-only academic progress feature after schema creation and seed review. The API is additive; existing `HistorialAcademico` CRUD remains unchanged.

## Pre-requisites

- Application can start with `spring.jpa.hibernate.ddl-auto=update` against the target database.
- Tables/columns exist after startup: `malla_curricular`, `prerrequisito`, and nullable `curso.codigo`.
- Course catalog is loaded for the target career.
- `curso.codigo` is backfilled for existing rows.
- `openspec/changes/historial-academico/seed-insforge.sql` has been reviewed and adjusted to the target career/course names.
- No credentials or secrets are stored in rollout files.

## Deploy order

1. Deploy backend containing the additive entities, repositories, service, security, and read-only progress endpoints.
2. Start backend once so `ddl-auto:update` creates/updates the schema.
3. Run the reviewed seed script through InsForge CLI against the target database:

   ```bash
   insforge sql < openspec/changes/historial-academico/seed-insforge.sql
   ```

4. Confirm the validation query at the end of the seed script reports the expected course count and total credits for the target career.
5. Verify the API with authenticated users.
6. Enable frontend consumption only after API smoke checks pass.

## Smoke test calls

Replace `<token>` and IDs with real authorized users from the environment.

```bash
curl -sS \
  -H "Authorization: Bearer <student-token>" \
  -H "Accept: application/json" \
  http://localhost:8080/api/v1/historial-academico/progreso/me
```

```bash
curl -sS \
  -H "Authorization: Bearer <admin-token>" \
  -H "Accept: application/json" \
  http://localhost:8080/api/v1/historial-academico/progreso/estudiante/<estudianteId>
```

Expected checks:

- `estudiante.id`, `carrera.creditosTotales`, `resumen`, and `cursos[]` are present.
- Course states include expected `PASSED`, `IN_PROGRESS`, `PENDING_AVAILABLE`, and `PENDING_BLOCKED` values for seeded/fixture data.
- `promedioPonderado`, `creditosAprobados`, `creditosRestantes`, and `porcentajeAvance` match the student's notes and curriculum credits.
- Unauthorized student-to-student access returns `403`.
- Missing authorized student lookup returns `404`.

## Rollback plan

1. Disable or hide frontend entry points that call `/api/v1/historial-academico/progreso`.
2. If seed data is wrong, remove only seed rows for the affected career after review:

   ```sql
   BEGIN;

   DELETE FROM prerrequisito p
   USING carrera ca
   WHERE p.id_carrera = ca.id_carrera
     AND ca.nombre = 'Ingeniería de Sistemas';

   DELETE FROM malla_curricular mc
   USING carrera ca
   WHERE mc.id_carrera = ca.id_carrera
     AND ca.nombre = 'Ingeniería de Sistemas';

   COMMIT;
   ```

3. Keep the additive schema in place unless a separate migration rollback is explicitly approved.
4. Do not mutate `matricula.estado`, `historial_academico.estado`, or historical grades during rollback.

## Known limitation

Legacy `HistorialAcademico` rows without matching `Nota` + `Evaluacion.porcentaje` rows do not contribute to progress calculation in this slice. The accepted rule calculates final grades from weighted evaluations.
