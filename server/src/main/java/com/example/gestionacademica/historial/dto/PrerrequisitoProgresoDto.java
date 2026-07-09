package com.example.gestionacademica.historial.dto;

import com.example.gestionacademica.historial.domain.TipoReglaPrerrequisito;
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
public class PrerrequisitoProgresoDto {
    private Integer cursoId;
    private String codigo;
    private String nombre;
    private TipoReglaPrerrequisito tipoRegla;
    private Boolean cumplido;
}
