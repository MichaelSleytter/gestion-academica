package com.example.gestionacademica.historial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResumenDto {
    private Integer id;
    private String codigo;
    private String nombres;
    private String apellidos;
}
