package com.example.gestionacademica.notas.repository;

import com.example.gestionacademica.notas.domain.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositorio JPA para la entidad {@link Nota}.
 */
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    // Notas de un estudiante (navegación correcta según BD)
    List<Nota> findByEstudiante_IdUsuario(Integer idUsuario);

    // Notas de una evaluación
    List<Nota> findByEvaluacion_IdEvaluacion(Integer idEvaluacion);

    // Valida duplicado por restricción única (evaluación + estudiante)
    boolean existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(
            Integer idEvaluacion,
            Integer idUsuario);
}
