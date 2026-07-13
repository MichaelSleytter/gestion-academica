package com.example.gestionacademica.docentes.domain;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "secciones")
public class Docente {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "especialidad", length = 100)
    @Deprecated
    private String especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especializacion")
    @JsonIgnore
    private Especializacion especializacion;

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
