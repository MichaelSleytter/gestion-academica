package com.example.gestionacademica.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un docente dentro del sistema académico.
 */
@Entity
@Table(name = "docente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "secciones")
public class Docente {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grado", nullable = false)
    @JsonIgnore
    private GradoAcademico gradoAcademico;

    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DocenteSeccion> secciones = new ArrayList<>();
}