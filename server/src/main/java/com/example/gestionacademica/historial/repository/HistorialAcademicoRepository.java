// HistorialAcademicoRepository.java
package com.example.gestionacademica.historial.repository;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
        select h
        from HistorialAcademico h
        join fetch h.estudiante e
        join fetch h.seccion s
        join fetch s.curso c
        join fetch s.cicloAcademico ca
        where e.idUsuario = :idEstudiante
    """)
    List<HistorialAcademico> findByEstudianteIdWithSeccionCursoCiclo(
        @Param("idEstudiante") Integer idEstudiante
    );
}
