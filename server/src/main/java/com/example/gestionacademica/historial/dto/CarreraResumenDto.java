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
public class CarreraResumenDto {
    private Integer id;
    private String nombre;
    private Integer creditosTotales;
}
