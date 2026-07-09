package com.example.gestionacademica.notas.repository;

import com.example.gestionacademica.notas.domain.Nota;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repositorio JPA para la entidad {@link Nota}.
 */
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    // Notas de un estudiante (navegación correcta según BD)
    List<Nota> findByEstudiante_IdUsuario(Integer idUsuario);

    // Notas de una evaluación
    @EntityGraph(attributePaths = "estudiante")
    List<Nota> findByEvaluacion_IdEvaluacion(Integer idEvaluacion);

    // Valida duplicado por restricción única (evaluación + estudiante)
    boolean existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(
            Integer idEvaluacion,
            Integer idUsuario);

    @Query("""
        select n
        from Nota n
        join fetch n.evaluacion ev
        join fetch ev.seccion s
        join fetch s.curso c
        where n.estudiante.idUsuario = :idEstudiante
          and s.idSeccion in :seccionIds
    """)
    List<Nota> findByEstudianteIdAndSeccionIdsWithEvaluacion(
        @Param("idEstudiante") Integer idEstudiante,
        @Param("seccionIds") Collection<Integer> seccionIds
    );

    @Query("""
        select n
        from Nota n
        join fetch n.evaluacion ev
        where n.estudiante.idUsuario = :idEstudiante
          and ev.seccion.idSeccion = :idSeccion
    """)
    List<Nota> findByEstudianteIdAndSeccionIdWithEvaluacion(
        @Param("idEstudiante") Integer idEstudiante,
        @Param("idSeccion") Integer idSeccion
    );
}
