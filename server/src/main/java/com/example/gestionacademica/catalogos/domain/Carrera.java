package com.example.gestionacademica.catalogos.domain;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una carrera académica.
 */
@Entity
@Table(name = "carrera")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "estudiantes")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "carrera")
    @JsonIgnore
    private List<Estudiante> estudiantes = new ArrayList<>();
}
