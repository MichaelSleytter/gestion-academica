package com.example.gestionacademica.notas.dto;

import java.math.BigDecimal;

/**
 * DTO de respuesta para correlacionar una nota con su estudiante sin exponer entidades JPA recursivas.
 *
 * @param idNota identificador de la nota.
 * @param nota valor numérico registrado.
 * @param idEstudiante identificador del estudiante evaluado.
 * @since 1.0
 */
public record NotaResponseDTO(Integer idNota, BigDecimal nota, Integer idEstudiante) {
}
