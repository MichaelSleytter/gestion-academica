package com.example.gestionacademica.estudiantes.dto;

import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

/**
 * DTO de salida para representar un Estudiante en las respuestas.
 * Evita exponer entidades JPA directamente y previene referencias circulares.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResponseDTO {

    // ── Identificación ───────────────────────────────────
    private Integer idUsuario;
    private String codigoEstudiante;

    // ── Datos personales (del Usuario) ───────────────────
    private String nombre;
    private String apellido;
    private String emailPersonal;
    private String numeroDocumento;
    private String tipoDocumento;
    private Boolean estado;

    // ── Datos academicos ─────────────────────────────────
    private Integer ciclo;

    @Enumerated(EnumType.STRING)
    private EstudianteEstadoAcademico estadoAcademico;

    private String nombreCarrera;
    private Integer idCarrera;
}
