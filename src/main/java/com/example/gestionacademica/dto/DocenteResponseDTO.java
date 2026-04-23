package com.example.gestionacademica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de salida para representar un Docente en las respuestas.
 * Evita exponer entidades JPA directamente y previene referencias circulares.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteResponseDTO {

    // ── Identificación ───────────────────────────────────
    private Integer idUsuario;

    // ── Datos personales (del Usuario) ───────────────────
    private String nombre;
    private String apellido;
    private String email;
    private String numeroDocumento;
    private String tipoDocumento;
    private Boolean estado;

    // ── Datos del Docente ────────────────────────────────
    private String especialidad;
    private Integer idGrado;
    private String nombreGrado;
}
