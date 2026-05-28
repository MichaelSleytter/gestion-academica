// GradoAcademicoRepository.java
package com.example.gestionacademica.catalogos.repository;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
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
