package com.example.gestionacademica.cursos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to generate the two academic periods for a year.
 */
@Getter
@Setter
public class GenerarAnioAcademicoRequest {

    @NotNull(message = "El año es obligatorio")
    @Min(value = 2020, message = "El año debe ser mayor o igual a 2020")
    @Max(value = 2099, message = "El año debe ser menor o igual a 2099")
    private Integer anio;
}
