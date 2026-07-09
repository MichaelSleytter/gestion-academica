/*
 * V1: Add enum-compatible CHECK constraints for estado columns.
 *
 * Both Matricula.estado and HistorialAcademico.estado were previously
 * untyped String columns managed by Hibernate ddl-auto. Now they
 * use @Enumerated(EnumType.STRING) in Java, so we add CHECK constraints
 * to enforce valid values at the database level.
 *
 * This migration also fixes existing data that doesn't match the enums.
 */

-- Fix existing bad data: "ACTIVO" should be "ACTIVA"
UPDATE matricula SET estado = 'ACTIVA' WHERE estado = 'ACTIVO';

-- Add CHECK constraint to matricula.estado
-- Values: ACTIVA, RETIRADA, APROBADA, DESAPROBADA
ALTER TABLE matricula
    ADD CONSTRAINT chk_matricula_estado
    CHECK (estado IN ('ACTIVA', 'RETIRADA', 'APROBADA', 'DESAPROBADA'));

-- Add CHECK constraint to historial_academico.estado
-- Values: APROBADO, DESAPROBADO
ALTER TABLE historial_academico
    ADD CONSTRAINT chk_historial_estado
    CHECK (estado IN ('APROBADO', 'DESAPROBADO'));
