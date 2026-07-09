package com.example.gestionacademica.historial.dto;

import java.math.BigDecimal;
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
public class ProgresoResumenDto {
    private Integer totalCursos;
    private Integer cursosAprobados;
    private Integer cursosEnProgreso;
    private Integer cursosPendientes;
    private Integer creditosAprobados;
    private Integer creditosRestantes;
    private BigDecimal promedioPonderado;
    private BigDecimal porcentajeAvance;
}
