package com.example.gestionacademica.historial.repository;

import com.example.gestionacademica.historial.domain.MallaCurricular;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MallaCurricularRepository extends JpaRepository<MallaCurricular, Integer> {

    @Query("""
        select mc
        from MallaCurricular mc
        join fetch mc.carrera c
        join fetch mc.curso cu
        where c.idCarrera = :idCarrera
        order by mc.cicloRecomendado asc, cu.nombre asc
    """)
    List<MallaCurricular> findByCarreraIdWithCurso(@Param("idCarrera") Integer idCarrera);

    boolean existsByCarrera_IdCarreraAndCurso_IdCurso(Integer idCarrera, Integer idCurso);
}
