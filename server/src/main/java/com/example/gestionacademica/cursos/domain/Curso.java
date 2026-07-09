package com.example.gestionacademica.cursos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un curso ofertado por la institución.
 */
@Entity
@Table(name = "curso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "secciones")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer idCurso;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "codigo", unique = true, length = 30)
    private String codigo;

    @Column(name = "creditos", nullable = false)
    private Integer creditos;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    public Curso(Integer idCurso, String nombre, Integer creditos, String descripcion, List<Seccion> secciones) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.creditos = creditos;
        this.descripcion = descripcion;
        this.secciones = secciones;
    }

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seccion> secciones = new ArrayList<>();
}
