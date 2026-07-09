package com.example.gestionacademica.historial.dto;

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
public class HistorialProgresoResponseDto {
    private EstudianteResumenDto estudiante;
    private CarreraResumenDto carrera;
    private ProgresoResumenDto resumen;
    private List<CursoProgresoDto> cursos;
}
