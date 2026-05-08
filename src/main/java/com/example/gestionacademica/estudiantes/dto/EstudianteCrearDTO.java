package com.example.gestionacademica.estudiantes.dto;

import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @NotBlank private String nombre;
    @NotBlank private String apellido;
    @NotBlank private String numeroDocumento;
    @NotNull private Integer idTipoDocumento;
    @Email @Size(max = 120) private String emailPersonal;

    // Contraseña opcional: si no se envía, se genera en el servidor
    private String password;

    // Datos académicos
    @NotNull private Integer ciclo;
    @NotNull private Integer idCarrera;
    @NotNull @Enumerated(EnumType.STRING) private EstudianteEstadoAcademico estadoAcademico;

    // Código opcional: si no se envía, se genera en el servidor
    private String codigoEstudiante;
}
