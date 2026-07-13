package com.example.gestionacademica.catalogos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity that represents a teacher specialization.
 */
@Entity
@Table(name = "especializacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Especializacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especializacion")
    private Integer idEspecializacion;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}
