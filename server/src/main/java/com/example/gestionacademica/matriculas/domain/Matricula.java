package com.example.gestionacademica.matriculas.domain;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matricula",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"id_estudiante", "id_seccion"}
    ))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_matricula")
    private Integer idMatricula;

    @Column(name = "fecha_matricula")
    private LocalDateTime fechaMatricula;

    @Column(name = "estado", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private MatriculaEstado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    @JsonIgnore
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    @JsonIgnore
    private Seccion seccion;
}
