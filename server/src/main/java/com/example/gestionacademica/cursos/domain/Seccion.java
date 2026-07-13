package com.example.gestionacademica.cursos.domain;

import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "matriculas", "horarios", "docentes", "evaluaciones", "historial" })
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seccion")
    private Integer idSeccion;

    @Column(name = "codigo_seccion", nullable = false, unique = true, length = 30)
    private String codigoSeccion;

    @Column(name = "ciclo_academico", length = 50)
    private String cicloAcademicoNombre;

    @Column(name = "vacantes", nullable = false)
    private Integer vacantes;

    @Column(name = "color", length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    @JsonIgnoreProperties({"secciones"})
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ciclo", nullable = false)
    @JsonIgnoreProperties({"secciones"})
    private CicloAcademico cicloAcademico;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Matricula> matriculas = new ArrayList<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Horario> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DocenteSeccion> docentes = new ArrayList<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Evaluacion> evaluaciones = new ArrayList<>();

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HistorialAcademico> historial = new ArrayList<>();
}
