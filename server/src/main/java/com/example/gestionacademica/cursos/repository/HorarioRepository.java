package com.example.gestionacademica.cursos.repository;

import com.example.gestionacademica.cursos.domain.Horario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad {@link Horario}.
 */
@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer>, JpaSpecificationExecutor<Horario> {

    /**
     * Obtiene horarios vinculados a una sección.
     *
     * @param idSeccion identificador de sección
     * @return horarios asociados
     */
    List<Horario> findBySeccion_IdSeccion(Integer idSeccion);

    List<Horario> findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
            String diaSemana,
            java.time.LocalTime horaFin,
            java.time.LocalTime horaInicio);
}
