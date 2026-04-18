package com.example.gestionacademica.repositories;

import com.example.gestionacademica.entities.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Estudiante.
 * PK: Integer (id_usuario)
 */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    List<Estudiante> findByCarrera_IdCarrera(Integer idCarrera);

    List<Estudiante> findByEstadoAcademico(String estadoAcademico);

    List<Estudiante> findByCiclo(Integer ciclo);

    @Query("SELECT e FROM Estudiante e WHERE e.carrera.idCarrera = :idCarrera AND e.estadoAcademico = 'ACTIVO'")
    List<Estudiante> findActivosPorCarrera(@Param("idCarrera") Integer idCarrera);
}