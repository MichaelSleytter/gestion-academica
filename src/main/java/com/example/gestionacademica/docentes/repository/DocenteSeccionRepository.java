package com.example.gestionacademica.docentes.repository;

import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.example.gestionacademica.docentes.domain.DocenteSeccion.DocenteSeccionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para asignaciones entre docentes y secciones.
 */
@Repository
public interface DocenteSeccionRepository extends JpaRepository<DocenteSeccion, DocenteSeccionId> {

    /**
     * Obtiene asignaciones por docente.
     *
     * @param idDocente identificador del docente
     * @return asignaciones asociadas
     */
    List<DocenteSeccion> findByDocente_IdUsuario(Integer idDocente);

    /**
     * Obtiene asignaciones por sección.
     *
     * @param idSeccion identificador de la sección
     * @return asignaciones asociadas
     */
    List<DocenteSeccion> findBySeccion_IdSeccion(Integer idSeccion);

    /**
     * Verifica si una asignación ya existe.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la sección
     * @return true si existe
     */
    boolean existsByDocente_IdUsuarioAndSeccion_IdSeccion(Integer idDocente, Integer idSeccion);
}
