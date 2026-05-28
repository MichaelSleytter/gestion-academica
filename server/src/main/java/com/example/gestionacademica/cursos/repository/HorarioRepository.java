package com.example.gestionacademica.cursos.repository;

import com.example.gestionacademica.cursos.domain.Horario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad {@link Horario}.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    /**
     * Obtiene horarios vinculados a una sección.
     *
     * @param idSeccion identificador de sección
     * @return horarios asociados
     */
    List<Horario> findBySeccion_IdSeccion(Integer idSeccion);
}
