// GradoAcademicoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.GradoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradoAcademicoRepository extends JpaRepository<GradoAcademico, Integer> {
}