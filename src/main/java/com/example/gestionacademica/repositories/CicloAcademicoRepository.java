// CicloAcademicoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.CicloAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CicloAcademicoRepository extends JpaRepository<CicloAcademico, Integer> {
    java.util.Optional<CicloAcademico> findByNombre(String nombre);
}