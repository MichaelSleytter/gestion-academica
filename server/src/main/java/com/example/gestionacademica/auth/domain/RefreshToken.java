package com.example.gestionacademica.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Refresh Token.
 *
 * ¿POR QUÉ esta entidad?
 * -----------------------
 * Los tokens JWT son stateless (sin estado en el servidor), lo que es bueno para escalabilidad.
 * PERO: ¿cómo revocamos un token si el usuario hace logout o sentimos que fue comprometido?
 *
 * Aquí es donde entra el Refresh Token como estado.
 * Guardamos cada refresh token en la BD y podemos:
 * - Revocarlo individualmente (logout)
 * - Revocarlos todos (cambiar contraseña)
 * - Revocar todos los dispositivos (sospecha de hack)
 * - Expirar tokens automáticamente
 *
 * El Access Token es de vida corta (15 min) y no se guarda en BD.
 * El Refresh Token es de vida larga (7 días) y SÍ se guarda en BD.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El token en sí mismo.
     * Lo generamos con un UUID seguro + hash para que sea impredecible.
     *
     * ¿POR QUÉ no guardar el JWT directamente?
     * Porque no necesitamos la info del JWT (ya está codificada).
     * Solo necesitamos UN identificador único que el cliente nos devuelva.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Fecha de expiración del refresh token.
     * típicamente 7 días desde la creación.
     */
    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * ¿El token ha sido revoked?
     * Cuando un usuario hace logout, marcamos esto como true.
     * El token sigue existiendo en BD pero no se puede usar.
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * ¿El token ha sido usado para generar un nuevo access token?
     *
     * ¿POR QUÉ trackear esto?
     * Para prevenir ataques de "token replay".
     * Un atacante que intercepta el refresh token solo puede usarlo UNA vez.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Relación con el usuario dueño del token.
     * Un usuario puede tener múltiples refresh tokens (multi-device).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * ¿El token está expirado?
     * Se considera expirado si la fecha de expiración es menor a ahora.
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * ¿El token es válido para uso?
     * Un token es válido si:
     * - No está expirado
     * - No ha sido revocado
     * - No ha sido usado (para prevenir replay attacks)
     */
    public boolean isValido() {
        return !isExpirado() && !revoked && !used;
    }
}