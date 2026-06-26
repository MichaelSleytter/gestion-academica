package com.example.gestionacademica.cursos.repository;

import com.example.gestionacademica.cursos.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Curso}.
 */
@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer>, JpaSpecificationExecutor<Curso> {

    List<Curso> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);
}
