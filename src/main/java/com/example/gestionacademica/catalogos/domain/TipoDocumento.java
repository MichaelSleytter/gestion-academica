package com.example.gestionacademica.catalogos.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que representa el tipo de documento de identidad.
 * Ejemplo: DNI, Pasaporte.
 */
@Entity
@Table(name = "tipo_documento")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Integer idTipoDocumento;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
