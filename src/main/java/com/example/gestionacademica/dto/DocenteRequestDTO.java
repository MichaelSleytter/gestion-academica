package com.example.gestionacademica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear o actualizar un Docente.
 *
 * <p>Contiene los datos básicos del usuario asociado y los datos
 * académicos del docente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocenteRequestDTO {

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
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    private String password;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 30, message = "El número de documento no puede superar 30 caracteres")
    private String numeroDocumento;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Integer idTipoDocumento;

    /**
     * Email personal opcional provisto por el docente.
     */
    @Email(message = "El email personal no tiene un formato válido")
    @Size(max = 120, message = "El email personal no puede superar 120 caracteres")
    private String emailPersonal;

    // ── Datos del Docente ───────────────────────────────

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100, message = "La especialidad no puede superar 100 caracteres")
    private String especialidad;

    @NotNull(message = "El grado académico es obligatorio")
    private Integer idGrado;
}
