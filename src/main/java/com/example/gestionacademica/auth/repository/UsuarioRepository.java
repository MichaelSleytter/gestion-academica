package com.example.gestionacademica.auth.repository;

import com.example.gestionacademica.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
/**
 * Repositorio JPA para la entidad {@link Usuario}.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    boolean existsByEmail(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
