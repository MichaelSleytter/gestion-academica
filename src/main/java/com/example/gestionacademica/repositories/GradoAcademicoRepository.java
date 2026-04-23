// GradoAcademicoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.GradoAcademico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repositorio JPA para la entidad {@link GradoAcademico}.
 */
public interface GradoAcademicoRepository extends JpaRepository<GradoAcademico, Integer> {
	Optional<GradoAcademico> findByNombreIgnoreCase(String nombre);
	boolean existsByNombreIgnoreCase(String nombre);
}