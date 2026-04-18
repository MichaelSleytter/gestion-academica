package com.example.gestionacademica.repositories;

import com.example.gestionacademica.entities.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {

    List<Matricula> findByEstudiante_IdUsuario(Integer idUsuario);

    List<Matricula> findBySeccion_IdSeccion(Integer idSeccion);

    Optional<Matricula> findByEstudiante_IdUsuarioAndSeccion_IdSeccion(
            Integer idUsuario, Integer idSeccion);

    boolean existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(
            Integer idUsuario, Integer idSeccion);

    @Query("SELECT COUNT(m) FROM Matricula m WHERE m.seccion.idSeccion = :idSeccion AND m.estado = 'ACTIVA'")
    Long countMatriculadosActivos(@Param("idSeccion") Integer idSeccion);
}