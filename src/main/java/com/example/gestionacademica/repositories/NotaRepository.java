package com.example.gestionacademica.repositories;

import com.example.gestionacademica.entities.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    // Notas de un estudiante (navegación correcta según BD)
    List<Nota> findByEstudiante_IdUsuario(Integer idUsuario);

    // Notas de una evaluación
    List<Nota> findByEvaluacion_IdEvaluacion(Integer idEvaluacion);
}