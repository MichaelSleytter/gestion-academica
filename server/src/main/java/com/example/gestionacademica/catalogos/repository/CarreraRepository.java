package com.example.gestionacademica.catalogos.repository;

import com.example.gestionacademica.catalogos.domain.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
/**
 * Repositorio JPA para la entidad {@link Carrera}.
 */
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    Optional<Carrera> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
