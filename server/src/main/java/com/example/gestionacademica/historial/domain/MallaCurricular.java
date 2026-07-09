package com.example.gestionacademica.historial.domain;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.Curso;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(
    name = "malla_curricular",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_malla_curricular_carrera_curso",
        columnNames = {"id_carrera", "id_curso"}
    ),
    indexes = {
        @Index(name = "idx_malla_carrera", columnList = "id_carrera"),
        @Index(name = "idx_malla_curso", columnList = "id_curso")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MallaCurricular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_malla_curricular")
    private Integer idMallaCurricular;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Min(1)
    @Column(name = "ciclo_recomendado", nullable = false)
    private Integer cicloRecomendado;

    @Builder.Default
    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio = true;

    @Min(1)
    @Column(name = "creditos", nullable = false)
    private Integer creditos;
}
