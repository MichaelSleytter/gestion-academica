package com.example.gestionacademica.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Table(name = "docente_seccion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DocenteSeccion {

    @EmbeddedId
    private DocenteSeccionId id = new DocenteSeccionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idDocente")
    @JoinColumn(name = "id_docente")
    @JsonIgnore
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSeccion")
    @JoinColumn(name = "id_seccion")
    @JsonIgnore
    private Seccion seccion;

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class DocenteSeccionId implements Serializable {

        @Column(name = "id_docente")
        private Integer idDocente;

        @Column(name = "id_seccion")
        private Integer idSeccion;
    }
}