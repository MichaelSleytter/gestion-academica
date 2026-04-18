package com.example.gestionacademica.repositories;

import com.example.gestionacademica.entities.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    List<Docente> findByGradoAcademico_IdGrado(Integer idGrado);

    List<Docente> findByEspecialidadContainingIgnoreCase(String especialidad);
}