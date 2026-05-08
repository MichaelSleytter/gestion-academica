package com.example.gestionacademica.historial.domain;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "historial_academico",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"id_estudiante", "id_seccion"}
    ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class HistorialAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @Column(name = "nota_final", precision = 5, scale = 2)
    private BigDecimal notaFinal;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    @JsonIgnore
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    @JsonIgnore
    private Seccion seccion;
}
