package com.example.gestionacademica.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudiante")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"matriculas", "notas", "historial"})
public class Estudiante {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "codigo_estudiante", nullable = false, unique = true, length = 30)
    private String codigoEstudiante;

    @Column(name = "ciclo", nullable = false)
    private Integer ciclo;

    @Column(name = "estado_academico", length = 50)
    private String estadoAcademico;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    @JsonIgnore
    private Carrera carrera;

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Matricula> matriculas = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Nota> notas = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HistorialAcademico> historial = new ArrayList<>();
}