// HistorialAcademicoRepository.java
package com.example.gestionacademica.historial.repository;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositorio JPA para la entidad {@link HistorialAcademico}.
 */
public interface HistorialAcademicoRepository extends JpaRepository<HistorialAcademico, Integer> {
    List<HistorialAcademico> findByEstudiante_IdUsuario(Integer idUsuario);
    List<HistorialAcademico> findBySeccion_IdSeccion(Integer idSeccion);
    boolean existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(Integer idUsuario, Integer idSeccion);
}
