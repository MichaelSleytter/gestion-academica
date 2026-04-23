package com.example.gestionacademica.entities;

import com.example.gestionacademica.enums.EstudianteEstadoAcademico;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * Entidad JPA que representa a un estudiante.
 * <p>
 * El {@code Estudiante} comparte la misma clave primaria que su {@code Usuario}
 * (mappeado con {@link jakarta.persistence.MapsId}). Contiene datos academicos
 * y relaciones a carrera, matriculas, notas e historial academico.
 *
 * @since 1.0
 */
@Entity
@Table(name = "estudiante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "matriculas", "notas", "historial" })
public class Estudiante {

    /**
     * Identificador del estudiante. Corresponde al {@code id_usuario} y es la
     * misma clave que la entidad {@link Usuario} asociada.
     */
    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    /**
     * Código único del estudiante, con formato legible (ej: "EST-00000123").
     * Este valor debe ser único en la base de datos.
     */
    @Column(
        name = "codigo_estudiante",
        nullable = false,
        unique = true,
        length = 30
    )
    private String codigoEstudiante;

    /**
     * Ciclo académico actual del estudiante (1..12).
     */
    @Column(name = "ciclo", nullable = false)
    private Integer ciclo;

    /**
     * Estado academico del estudiante. Se persiste como {@link String} en la
     * columna {@code estado_academico}.
     */
    @Column(name = "estado_academico", length = 50)
    @Enumerated(EnumType.STRING)
    private EstudianteEstadoAcademico estadoAcademico;

    /**
     * Usuario asociado al estudiante. Relacion one-to-one y comparte PK.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    /**
     * Carrera a la que pertenece el estudiante.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    @JsonIgnore
    private Carrera carrera;

    /**
     * Matriculas asociadas al estudiante.
     */
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Matricula> matriculas = new ArrayList<>();

    /**
     * Notas obtenidas por el estudiante.
     */
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Nota> notas = new ArrayList<>();

    /**
     * Historial academico del estudiante.
     */
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HistorialAcademico> historial = new ArrayList<>();
}
