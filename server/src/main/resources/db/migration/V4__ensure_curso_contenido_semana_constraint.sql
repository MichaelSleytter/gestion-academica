ALTER TABLE curso_contenido
    DROP CONSTRAINT IF EXISTS chk_curso_contenido_semana;

ALTER TABLE curso_contenido
    ADD CONSTRAINT chk_curso_contenido_semana CHECK (semana BETWEEN 1 AND 18);
