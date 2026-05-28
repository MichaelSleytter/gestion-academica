package com.example.gestionacademica.catalogos.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa el grado academico de un docente.
 * Ejemplo: Bachiller, Magíster, Doctor.
 */
@Entity
@Table(name = "grado_academico")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class GradoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grado")
    private Integer idGrado;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}
