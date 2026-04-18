// HistorialAcademicoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.HistorialAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialAcademicoRepository extends JpaRepository<HistorialAcademico, Integer> {
    List<HistorialAcademico> findByEstudiante_IdUsuario(Integer idUsuario);
    List<HistorialAcademico> findBySeccion_IdSeccion(Integer idSeccion);
}