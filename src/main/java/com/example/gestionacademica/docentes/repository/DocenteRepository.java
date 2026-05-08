package com.example.gestionacademica.docentes.repository;

import com.example.gestionacademica.docentes.domain.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositorio JPA para la entidad {@link Docente}.
 */
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    List<Docente> findByGradoAcademico_IdGrado(Integer idGrado);

    List<Docente> findByEspecialidadContainingIgnoreCase(String especialidad);
}
