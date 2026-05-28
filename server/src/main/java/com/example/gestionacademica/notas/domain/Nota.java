package com.example.gestionacademica.notas.domain;

import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "nota",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"id_evaluacion", "id_estudiante"}
    ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Integer idNota;

    @Column(name = "nota", nullable = false, precision = 5, scale = 2)
    private BigDecimal nota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    @JsonIgnore
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    @JsonIgnore
    private Estudiante estudiante;
}
