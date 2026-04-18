package com.example.gestionacademica.repositories;

import com.example.gestionacademica.entities.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    Optional<Carrera> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}