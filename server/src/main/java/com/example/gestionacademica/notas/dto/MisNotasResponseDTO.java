package com.example.gestionacademica.notas.dto;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el endpoint mis-notas del estudiante autenticado.
 * <p>
 * Incluye el ID de la nota (si existe), el valor de la nota y el ID de la
 * evaluación asociada, permitiendo al frontend emparejar notas con evaluaciones.
 *
 * @param idNota       ID de la nota (null si aún no tiene nota registrada).
 * @param nota         Valor numérico de la nota (null si no tiene).
 * @param idEvaluacion ID de la evaluación a la que pertenece esta nota.
 */
public record MisNotasResponseDTO(
        Integer idNota,
        BigDecimal nota,
        Integer idEvaluacion) {
}
