package com.example.gestionacademica.estudiantes.dto;

import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO de entrada para crear o actualizar un Estudiante.
 *
 * <p>Contiene los datos necesarios para crear o actualizar la parte academica
 * del estudiante y los campos básicos del usuario. No incluye campos que el
 * servidor genera (código, email institucional, contraseña).
 *
 * @since 1.0
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

    // El email, la contraseña y el código de estudiante se generan en el servidor.
    // No deben ser proporcionados por el frontend en la petición de creación.

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(
        max = 30,
        message = "El número de documento no puede superar 30 caracteres"
    )
    private String numeroDocumento;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Integer idTipoDocumento;

    // ── Datos academicos del Estudiante ─────────────────

    @NotNull(message = "El ciclo es obligatorio")
    @Min(value = 1, message = "El ciclo mínimo es 1")
    @Max(value = 12, message = "El ciclo máximo es 12")
    private Integer ciclo;


    @NotNull(message = "El estado académico es obligatorio")
    @Enumerated(EnumType.STRING)
    private EstudianteEstadoAcademico estadoAcademico;

    @NotNull(message = "La carrera es obligatoria")
    private Integer idCarrera;

    /**
     * Email personal opcional provisto por el estudiante.
     * Si se envía, se almacenará en el campo {@code emailPersonal} del Usuario.
     */
    @Email(message = "El email personal no tiene un formato válido")
    @Size(
        max = 120,
        message = "El email personal no puede superar 120 caracteres"
    )
    private String emailPersonal;
}
