package com.example.gestionacademica.auth.domain;

import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "roles", "estudiante", "docente" })
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "email_personal", length = 120)
    private String emailPersonal;

    @Column(name = "password", nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Column(
        name = "numero_documento",
        nullable = false,
        unique = true,
        length = 30
    )
    private String numeroDocumento;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @JsonIgnore
    private TipoDocumento tipoDocumento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    @JsonIgnore
    private List<Rol> roles = new ArrayList<>();

    @OneToOne(mappedBy = "usuario")
    @JsonIgnore
    private Estudiante estudiante;

    @OneToOne(mappedBy = "usuario")
    @JsonIgnore
    private Docente docente;
}
