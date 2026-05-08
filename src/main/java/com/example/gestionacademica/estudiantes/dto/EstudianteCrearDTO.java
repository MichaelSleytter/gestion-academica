package com.example.gestionacademica.estudiantes.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO comando para crear un Estudiante junto con su Usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteCrearDTO {

    // Datos del usuario
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @NotBlank
    private String numeroDocumento;
    @NotNull
    private Integer idTipoDocumento;
    @Email
    @Size(max = 120)
    private String emailPersonal;
    // Datos académicos
    @NotNull
    private Integer ciclo;
    @NotNull
    private Integer idCarrera;
}
