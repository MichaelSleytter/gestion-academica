package com.example.gestionacademica.historial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.support.RepositoryProgressTestData;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("HistorialAcademicoRepository progress queries")
class HistorialAcademicoRepositoryProgressTest extends RepositoryProgressTestData {

    @Autowired
    private HistorialAcademicoRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("fetches history with section course and academic cycle")
    void fetchesHistoryWithSectionCourseAndCycle() {
        Estudiante estudiante = estudiante(entityManager);
        Curso curso = curso(entityManager);
        CicloAcademico ciclo = ciclo(entityManager);
        Seccion seccion = seccion(entityManager, curso, ciclo);
        historial(entityManager, estudiante, seccion);
        entityManager.clear();

        List<HistorialAcademico> resultado = repository.findByEstudianteIdWithSeccionCursoCiclo(estudiante.getIdUsuario());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getSeccion().getCurso().getNombre()).contains("Curso-");
        assertThat(resultado.getFirst().getSeccion().getCicloAcademico().getNombre()).contains("2026-");
        assertThat(Hibernate.isInitialized(resultado.getFirst().getSeccion().getCurso())).isTrue();
        assertThat(Hibernate.isInitialized(resultado.getFirst().getSeccion().getCicloAcademico())).isTrue();
    }
}
