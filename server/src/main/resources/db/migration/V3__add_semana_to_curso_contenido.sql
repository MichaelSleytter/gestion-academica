CREATE TABLE IF NOT EXISTS curso_contenido (
    id_contenido BIGSERIAL PRIMARY KEY,
    id_seccion INTEGER NOT NULL,
    nombre_original VARCHAR(500) NOT NULL,
    key VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100),
    size_bytes BIGINT,
    subido_por INTEGER,
    fecha_subida TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

ALTER TABLE curso_contenido
    ADD COLUMN IF NOT EXISTS semana INTEGER NOT NULL DEFAULT 1;

ALTER TABLE curso_contenido
    DROP CONSTRAINT IF EXISTS chk_curso_contenido_semana;

ALTER TABLE curso_contenido
    ADD CONSTRAINT chk_curso_contenido_semana CHECK (semana BETWEEN 1 AND 18);
