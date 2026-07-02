package com.example.gestionacademica.matriculas.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para cargar estudiantes matriculados de una sección sin exponer entidades JPA recursivas.
 *
 * @param idMatricula identificador de la matrícula.
 * @param fechaMatricula fecha de registro de la matrícula.
 * @param estado estado actual de la matrícula.
 * @param idEstudiante identificador del estudiante matriculado.
 * @param codigoEstudiante código académico del estudiante.
 * @param nombre nombre del estudiante.
 * @param apellido apellido del estudiante.
 * @param email correo institucional del estudiante.
 * @since 1.0
 */
public record MatriculaSeccionResponseDTO(
        Integer idMatricula,
        LocalDateTime fechaMatricula,
        String estado,
        Integer idEstudiante,
        String codigoEstudiante,
        String nombre,
        String apellido,
        String email) {
}
