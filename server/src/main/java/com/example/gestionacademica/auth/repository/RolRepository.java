package com.example.gestionacademica.auth.repository;

import com.example.gestionacademica.auth.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para operaciones de la entidad {@link Rol}.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol por nombre sin sensibilidad a mayúsculas.
     *
     * @param nombre nombre del rol
     * @return rol encontrado, si existe
     */
    Optional<Rol> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica existencia de un rol por nombre sin sensibilidad a mayúsculas.
     *
     * @param nombre nombre del rol
     * @return true si existe
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
