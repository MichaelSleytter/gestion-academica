package com.example.gestionacademica.historial.repository;

import com.example.gestionacademica.historial.domain.Prerrequisito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrerrequisitoRepository extends JpaRepository<Prerrequisito, Integer> {

    @Query("""
        select p
        from Prerrequisito p
        join fetch p.carrera c
        join fetch p.curso curso
        join fetch p.cursoPrerrequisito pre
        where c.idCarrera = :idCarrera
    """)
    List<Prerrequisito> findByCarreraIdWithCursos(@Param("idCarrera") Integer idCarrera);

    @Query("""
        select p
        from Prerrequisito p
        join fetch p.curso curso
        join fetch p.cursoPrerrequisito pre
        where p.carrera.idCarrera = :idCarrera
          and curso.idCurso = :idCurso
    """)
    List<Prerrequisito> findByCarreraIdAndCursoIdWithCursos(
        @Param("idCarrera") Integer idCarrera,
        @Param("idCurso") Integer idCurso
    );
}
