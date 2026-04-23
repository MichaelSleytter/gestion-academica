package com.example.gestionacademica.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una evaluación aplicada dentro de una sección.
 */
@Entity
@Table(name = "evaluacion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "notas")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Integer idEvaluacion;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    @JsonIgnore
    private Seccion seccion;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Nota> notas = new ArrayList<>();
}