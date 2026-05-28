package com.example.gestionacademica.auth.repository;

import com.example.gestionacademica.auth.domain.RefreshToken;
import com.example.gestionacademica.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar Refresh Tokens en la BD.
 *
 * ¿POR QUÉ JpaRepository y no un service simple?
 * Porque necesitamos:
 * - Buscar por token para validarlo
 * - Revocar tokens por usuario
 * - Limpiar tokens expirados periódicamente
 * - Buscar todos los tokens activos de un usuario
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor.
     *
     * ¿POR QUÉ buscar por el token directamente?
     * Porque el cliente nos envía el token y necesitamos encontrarlo.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Encuentra el refresh token e incluye el usuario (evita N+1).
     *
     * ¿POR QUÉ @Query con JOIN FETCH?
     * Porque al hacer login/refresh necesitamos el usuario inmediatamente.
     * Sin JOIN FETCH tendríamos que hacer 2 queries (una para token, otra para usuario).
     */
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.usuario WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUsuario(@Param("token") String token);

    /**
     * Busca todos los refresh tokens activos de un usuario.
     *
     * ¿POR QUÉ "activos"?
     * Porque un usuario puede tener tokens en múltiples dispositivos.
     * Al hacer logout de un dispositivo, solo revocamos UN token, no todos.
     */
    List<RefreshToken> findByUsuarioAndRevokedFalse(Usuario usuario);

    /**
     * Elimina todos los refresh tokens de un usuario.
     *
     * ¿POR QUÉ necesitamos esto?
     * Para el caso "logout desde todos los dispositivos" o
     * cuando el usuario cambia su contraseña.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);

    /**
     * Elimina tokens expirados y revocados.
     *
     * ¿POR QUÉ limpiar estos?
     * - Tokens expirados ya no se pueden usar
     * - Tokens revocados fueron invalidados
     * Mantenerlos ocupa espacio innecesario en la BD.
     *
     * Esta query debería ejecutarse periódicamente (ver SecurityConfig).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.fechaExpiracion < :now OR rt.revoked = true")
    void deleteExpiradosORevocados(@Param("now") LocalDateTime now);

    /**
     * Revoca un token específico.
     *
     * ¿POR QUÉ actualizar en lugar de eliminar?
     * Porque queremos mantener el registro para auditoría.
     * Si un token fue comprometido, queremos saber que existió.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);

    /**
     * Verifica si existe un refresh token válido.
     * Útil para validaciones antes de hacer refresh.
     */
    boolean existsByTokenAndRevokedFalseAndUsedFalse(String token);
}