package com.example.gestionacademica.cursos.repository;

import com.example.gestionacademica.cursos.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositorio JPA para la entidad {@link Curso}.
 */
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByNombre(String nombre);
}
