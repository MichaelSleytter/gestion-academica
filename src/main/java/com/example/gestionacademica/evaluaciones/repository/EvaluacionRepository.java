package com.example.gestionacademica.evaluaciones.repository;

import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad {@link Evaluacion}.
 */
@Repository
public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    /**
     * Lista evaluaciones por sección.
     *
     * @param idSeccion identificador de sección
     * @return evaluaciones asociadas
     */
    List<Evaluacion> findBySeccion_IdSeccion(Integer idSeccion);

    /**
     * Verifica si ya existe una evaluación con el mismo nombre dentro de una sección.
     *
     * @param nombre nombre de la evaluación
     * @param idSeccion identificador de sección
     * @return true si existe
     */
    boolean existsByNombreAndSeccion_IdSeccion(String nombre, Integer idSeccion);
}
