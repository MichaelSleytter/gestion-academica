package com.example.gestionacademica.contenido.repository;

import com.example.gestionacademica.contenido.domain.CursoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link CursoContenido}.
 */
@Repository
public interface CursoContenidoRepository extends JpaRepository<CursoContenido, Long> {

    /**
     * Busca contenido activo de una sección, ordenado por fecha de subida descendente.
     *
     * @param idSeccion identificador de la sección
     * @return lista de contenido activo
     */
    List<CursoContenido> findByIdSeccionAndActivoTrueOrderBySemanaAscFechaSubidaDesc(Integer idSeccion);
}
