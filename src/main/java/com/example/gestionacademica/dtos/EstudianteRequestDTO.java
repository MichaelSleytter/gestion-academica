package com.example.gestionacademica.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO de entrada para crear o actualizar un Estudiante.
 * Contiene datos del usuario base + datos academicos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteRequestDTO {

    // ── Datos del Usuario base ──────────────────────────

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 120, message = "El email no puede superar 120 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres")
    private String password;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 30, message = "El número de documento no puede superar 30 caracteres")
    private String numeroDocumento;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Integer idTipoDocumento;

    // ── Datos academicos del Estudiante ─────────────────

    @NotBlank(message = "El codigo de estudiante es obligatorio")
    @Size(max = 30, message = "El codigo no puede superar 30 caracteres")
    private String codigoEstudiante;

    @NotNull(message = "El ciclo es obligatorio")
    @Min(value = 1, message = "El ciclo mínimo es 1")
    @Max(value = 12, message = "El ciclo máximo es 12")
    private Integer ciclo;

    @Size(max = 50, message = "El estado academico no puede superar 50 caracteres")
    private String estadoAcademico;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;
}