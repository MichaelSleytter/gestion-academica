package com.example.gestionacademica.matriculas.dto;

/**
 * DTO de respuesta para el endpoint mis-cursos del estudiante autenticado.
 * <p>
 * Incluye datos de la matrícula, sección, curso y ciclo académico en un solo
 * objeto plano, evitando la serialización de entidades JPA.
 *
 * @param idMatricula         ID de la matrícula.
 * @param estado              Estado de la matrícula (ACTIVA, RETIRADA, etc.).
 * @param idSeccion           ID de la sección.
 * @param codigoSeccion       Código de la sección.
 * @param cicloAcademicoNombre Nombre del ciclo académico.
 * @param idCurso             ID del curso.
 * @param nombreCurso         Nombre del curso.
 * @param creditos            Créditos del curso.
 */
public record MatriculaMisCursosDTO(
        Integer idMatricula,
        String estado,
        Integer idSeccion,
        String codigoSeccion,
        String cicloAcademicoNombre,
        Integer idCurso,
        String nombreCurso,
        Integer creditos) {
}
