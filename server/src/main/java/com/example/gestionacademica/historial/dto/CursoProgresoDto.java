package com.example.gestionacademica.historial.dto;

import com.example.gestionacademica.historial.domain.EstadoCursoProgreso;
import java.math.BigDecimal;
import java.util.List;
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
public class CursoProgresoDto {
    private Integer cursoId;
    private String codigo;
    private String nombre;
    private Integer cicloRecomendado;
    private Boolean obligatorio;
    private Integer creditos;
    private EstadoCursoProgreso estado;
    private BigDecimal notaFinal;
    private List<PrerrequisitoProgresoDto> prerrequisitos;
}
