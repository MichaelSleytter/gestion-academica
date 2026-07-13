package com.example.gestionacademica.auth.repository;

import com.example.gestionacademica.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
/**
 * Repositorio JPA para la entidad {@link Usuario}.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuario por email Y carga los roles con fetch join.
     * Esto evita el problema de LAZY loading en la relación con Roles.
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRoles(String email);

    /**
     * Busca usuario por ID Y carga roles y tipoDocumento con fetch join.
     * Evita LazyInitializationException al acceder a estas relaciones fuera de sesión.
     */
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.tipoDocumento WHERE u.idUsuario = :id")
    Optional<Usuario> findByIdWithRolesAndTipoDocumento(Integer id);

    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    boolean existsByEmail(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
