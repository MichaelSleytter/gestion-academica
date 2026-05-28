package com.example.gestionacademica.cursos.repository;

import com.example.gestionacademica.cursos.domain.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repositorio JPA para la entidad {@link Seccion}.
 */
public interface SeccionRepository extends JpaRepository<Seccion, Integer> {

    Optional<Seccion> findByCodigoSeccion(String codigoSeccion);

    List<Seccion> findByCurso_IdCurso(Integer idCurso);

    List<Seccion> findByCicloAcademico_IdCiclo(Integer idCiclo);

    boolean existsByCodigoSeccion(String codigoSeccion);
}
