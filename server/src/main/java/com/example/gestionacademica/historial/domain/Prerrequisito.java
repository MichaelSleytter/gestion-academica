package com.example.gestionacademica.historial.domain;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.Curso;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "prerrequisito",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_prerrequisito_carrera_curso_pre",
        columnNames = {"id_carrera", "id_curso", "id_curso_prerrequisito"}
    ),
    indexes = {
        @Index(name = "idx_prerrequisito_carrera", columnList = "id_carrera"),
        @Index(name = "idx_prerrequisito_curso", columnList = "id_curso"),
        @Index(name = "idx_prerrequisito_pre", columnList = "id_curso_prerrequisito")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prerrequisito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prerrequisito")
    private Integer idPrerrequisito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso_prerrequisito", nullable = false)
    private Curso cursoPrerrequisito;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_regla", nullable = false, length = 20)
    private TipoReglaPrerrequisito tipoRegla = TipoReglaPrerrequisito.HARD;

    @PrePersist
    @PreUpdate
    private void validarRegla() {
        if (curso != null && cursoPrerrequisito != null
                && curso.getIdCurso() != null
                && curso.getIdCurso().equals(cursoPrerrequisito.getIdCurso())) {
            throw new IllegalArgumentException("A course cannot be its own prerequisite");
        }
    }
}
