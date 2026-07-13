/*
 * V2: Add teacher specialization catalog and schedule support fields.
 *
 * Adds:
 * - especializacion catalog table
 * - docente.id_especializacion nullable FK for gradual migration from free-text especialidad
 * - seccion.color for calendar UI identification
 */

CREATE TABLE especializacion (
    id_especializacion SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

ALTER TABLE docente
    ADD COLUMN id_especializacion INTEGER;

ALTER TABLE docente
    ADD CONSTRAINT fk_docente_especializacion
    FOREIGN KEY (id_especializacion)
    REFERENCES especializacion(id_especializacion);

ALTER TABLE seccion
    ADD COLUMN color VARCHAR(7);

INSERT INTO especializacion (nombre) VALUES
    ('Matemáticas'),
    ('Lenguaje y Literatura'),
    ('Ciencias Naturales'),
    ('Ciencias Sociales'),
    ('Historia'),
    ('Inglés'),
    ('Educación Física'),
    ('Arte y Cultura'),
    ('Tecnología e Informática'),
    ('Filosofía');
