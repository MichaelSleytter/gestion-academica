-- Historial Académico seed for InsForge CLI
-- PostgreSQL / idempotent / no secrets
--
-- Basado en los datos reales de la BD de Gestion-Universidad.
-- Ejecutar después de que la app haya creado el schema con ddl-auto.

-- 1. Backfill de códigos de curso (columna nullable agregada)
UPDATE curso SET codigo = CONCAT('CUR-', id_curso) WHERE codigo IS NULL;

-- 2. Malla curricular para Ingeniería de Sistemas (id_carrera = 19)
INSERT INTO malla_curricular (id_carrera, id_curso, ciclo_recomendado, obligatorio, creditos)
SELECT 19, cu.id_curso, sc.ciclo, sc.obligatorio, cu.creditos
FROM (VALUES
    ('CUR-2', 1, true),   -- Matemáticas I
    ('CUR-6', 1, true),   -- Introducción a la Programación
    ('CUR-19', 1, true),  -- Matemática Básica
    ('CUR-14', 1, false), -- Educación Física (electivo)
    ('CUR-3', 2, true),   -- Matemáticas II
    ('CUR-7', 2, true),   -- Estructura de Datos
    ('CUR-4', 2, true),   -- Física General
    ('CUR-8', 3, true),   -- Base de Datos I
    ('CUR-1', 3, true),   -- Desarrollo web
    ('CUR-15', 3, false), -- Ética Profesional (electivo)
    ('CUR-16', 4, true),  -- Metodología de Investigación
    ('CUR-9', 4, true),   -- Base de Datos II
    ('CUR-18', 4, true)   -- Programación III
) AS sc(codigo, ciclo, obligatorio)
JOIN curso cu ON cu.codigo = sc.codigo
ON CONFLICT (id_carrera, id_curso) DO NOTHING;

-- 3. Prerrequisitos HARD
INSERT INTO prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
SELECT 19,
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-3'),  -- Matemáticas II
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-2'),  -- requiere Matemáticas I
    'HARD'
WHERE NOT EXISTS (SELECT 1 FROM prerrequisito WHERE id_carrera = 19 AND id_curso = (SELECT id_curso FROM curso WHERE codigo = 'CUR-3') AND id_curso_prerrequisito = (SELECT id_curso FROM curso WHERE codigo = 'CUR-2'));

INSERT INTO prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
SELECT 19,
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-7'),  -- Estructura de Datos
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-6'),  -- requiere Intro. a la Programación
    'HARD'
WHERE NOT EXISTS (SELECT 1 FROM prerrequisito WHERE id_carrera = 19 AND id_curso = (SELECT id_curso FROM curso WHERE codigo = 'CUR-7') AND id_curso_prerrequisito = (SELECT id_curso FROM curso WHERE codigo = 'CUR-6'));

INSERT INTO prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
SELECT 19,
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-9'),  -- Base de Datos II
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-8'),  -- requiere Base de Datos I
    'HARD'
WHERE NOT EXISTS (SELECT 1 FROM prerrequisito WHERE id_carrera = 19 AND id_curso = (SELECT id_curso FROM curso WHERE codigo = 'CUR-9') AND id_curso_prerrequisito = (SELECT id_curso FROM curso WHERE codigo = 'CUR-8'));

INSERT INTO prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
SELECT 19,
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-18'), -- Programación III
    (SELECT id_curso FROM curso WHERE codigo = 'CUR-7'),  -- requiere Estructura de Datos
    'HARD'
WHERE NOT EXISTS (SELECT 1 FROM prerrequisito WHERE id_carrera = 19 AND id_curso = (SELECT id_curso FROM curso WHERE codigo = 'CUR-18') AND id_curso_prerrequisito = (SELECT id_curso FROM curso WHERE codigo = 'CUR-7'));

-- 4. Validación
SELECT ca.nombre AS carrera,
       COUNT(mc.id_malla_curricular) AS cursos,
       COALESCE(SUM(mc.creditos), 0) AS creditos_totales
FROM carrera ca
LEFT JOIN malla_curricular mc ON mc.id_carrera = ca.id_carrera
GROUP BY ca.nombre
ORDER BY ca.nombre;
