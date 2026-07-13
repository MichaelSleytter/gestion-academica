package com.example.gestionacademica.catalogos.repository;

import com.example.gestionacademica.catalogos.domain.Especializacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for {@link Especializacion}.
 */
@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion, Integer> {

    Optional<Especializacion> findByNombreIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
