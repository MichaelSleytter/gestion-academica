-- Historial Académico seed for InsForge CLI
-- PostgreSQL / idempotent / no secrets
--
-- Review before running:
-- 1. Adjust params.carrera_nombre to the target career in the environment.
-- 2. Adjust seed_courses and hard_prerequisites to match the existing course catalog.
-- 3. Run after the application has created/updated schema with ddl-auto.

BEGIN;

-- Backfill nullable course codes introduced for the progress API contract.
UPDATE curso
SET codigo = CONCAT('CUR-', id_curso)
WHERE codigo IS NULL;

-- Minimum curriculum map for the target career.
WITH params AS (
    SELECT 'Ingeniería de Sistemas'::text AS carrera_nombre
), seed_courses(codigo, nombre, ciclo_recomendado, obligatorio) AS (
    VALUES
        ('MAT101', 'Matemática I', 1, true),
        ('COM101', 'Comunicación', 1, true),
        ('INT100', 'Introducción a la Universidad', 1, true),
        ('MAT102', 'Matemática II', 2, true),
        ('ALG201', 'Algoritmos', 3, true)
), target_career AS (
    SELECT ca.id_carrera
    FROM carrera ca
    JOIN params p ON ca.nombre = p.carrera_nombre
)
INSERT INTO malla_curricular (id_carrera, id_curso, ciclo_recomendado, obligatorio, creditos)
SELECT tc.id_carrera,
       cu.id_curso,
       sc.ciclo_recomendado,
       sc.obligatorio,
       cu.creditos
FROM target_career tc
JOIN seed_courses sc ON true
JOIN curso cu ON cu.codigo = sc.codigo OR cu.nombre = sc.nombre
WHERE cu.creditos > 0
ON CONFLICT (id_carrera, id_curso) DO NOTHING;

-- HARD prerequisites for the same target career.
WITH params AS (
    SELECT 'Ingeniería de Sistemas'::text AS carrera_nombre
), hard_prerequisites(curso_codigo, curso_nombre, prerrequisito_codigo, prerrequisito_nombre) AS (
    VALUES
        ('MAT102', 'Matemática II', 'MAT101', 'Matemática I'),
        ('ALG201', 'Algoritmos', 'MAT102', 'Matemática II')
), target_career AS (
    SELECT ca.id_carrera
    FROM carrera ca
    JOIN params p ON ca.nombre = p.carrera_nombre
)
INSERT INTO prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
SELECT tc.id_carrera,
       curso_destino.id_curso,
       curso_pre.id_curso,
       'HARD'
FROM target_career tc
JOIN hard_prerequisites hp ON true
JOIN curso curso_destino
    ON curso_destino.codigo = hp.curso_codigo OR curso_destino.nombre = hp.curso_nombre
JOIN curso curso_pre
    ON curso_pre.codigo = hp.prerrequisito_codigo OR curso_pre.nombre = hp.prerrequisito_nombre
WHERE curso_destino.id_curso <> curso_pre.id_curso
ON CONFLICT (id_carrera, id_curso, id_curso_prerrequisito) DO NOTHING;

COMMIT;

-- Validation query: curriculum size and total credits by career.
SELECT ca.nombre AS carrera,
       COUNT(mc.id_malla_curricular) AS cursos,
       COALESCE(SUM(mc.creditos), 0) AS creditos_totales
FROM carrera ca
LEFT JOIN malla_curricular mc ON mc.id_carrera = ca.id_carrera
GROUP BY ca.nombre
ORDER BY ca.nombre;
