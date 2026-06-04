package com.example.gestionacademica.auth.repository;

import com.example.gestionacademica.auth.domain.PasswordResetToken;
import com.example.gestionacademica.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repositorio JPA para {@link PasswordResetToken}.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Busca un token de recuperación por su valor.
     *
     * @param token valor UUID del token
     * @return token encontrado, si existe
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina todos los tokens de recuperación asociados a un usuario.
     * <p>Se usa típicamente al solicitar un nuevo reset para invalidar los previos.</p>
     *
     * @param usuario usuario cuyos tokens serán eliminados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
}
