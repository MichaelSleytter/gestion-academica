package com.example.gestionacademica.cursos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ciclo_academico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "secciones")
public class CicloAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciclo")
    private Integer idCiclo;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @OneToMany(mappedBy = "cicloAcademico", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seccion> secciones = new ArrayList<>();
}
