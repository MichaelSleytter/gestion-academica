package com.example.gestionacademica.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un token de recuperación de contraseña.
 *
 * <p>Se genera cuando un usuario solicita restablecer su contraseña y se
 * persiste hasta que se consume (al resetear) o expira.</p>
 */
@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    /**
     * Identificador autogenerado del token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cadena única del token (UUID) que el cliente envía para resetear la contraseña.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Usuario asociado al token. Relación muchos-a-uno con carga LAZY.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora a partir de la cual el token deja de ser válido.
     */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Indica si el token ya fue consumido.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean usado = false;

    /**
     * Fecha de creación del token, se asigna automáticamente al persistir.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Asigna la fecha de creación antes del primer insert si aún no fue seteada.
     */
    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Indica si el token ya expiró según la fecha actual del sistema.
     *
     * @return {@code true} si la fecha actual es posterior a {@link #fechaExpiracion}.
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }
}
