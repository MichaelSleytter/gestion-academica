// TipoDocumentoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.TipoDocumento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repositorio JPA para la entidad {@link TipoDocumento}.
 */
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {
	Optional<TipoDocumento> findByNombreIgnoreCase(String nombre);
	boolean existsByNombreIgnoreCase(String nombre);
}