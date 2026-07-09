package com.example.gestionacademica.matriculas.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gestionacademica.GestionacademicaApplication;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.historial.support.RepositoryProgressTestData;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.domain.Matricula;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ContextConfiguration(classes = GestionacademicaApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("MatriculaRepository progress queries")
class MatriculaRepositoryProgressTest extends RepositoryProgressTestData {

    @Autowired
    private MatriculaRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("fetches only active enrollments with section course and cycle")
    void fetchesOnlyActiveEnrollmentsWithSectionCourseAndCycle() {
        Estudiante estudiante = estudiante(entityManager);
        CicloAcademico ciclo = ciclo(entityManager);
        Seccion activa = seccion(entityManager, curso(entityManager), ciclo);
        Seccion retirada = seccion(entityManager, curso(entityManager), ciclo);
        matricula(entityManager, estudiante, activa, MatriculaEstado.ACTIVA);
        matricula(entityManager, estudiante, retirada, MatriculaEstado.RETIRADA);
        entityManager.clear();

        List<Matricula> resultado = repository.findActiveByEstudianteIdWithSeccionCurso(estudiante.getIdUsuario());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getEstado()).isEqualTo(MatriculaEstado.ACTIVA);
        assertThat(resultado.getFirst().getSeccion().getCurso().getNombre()).contains("Curso-");
        assertThat(Hibernate.isInitialized(resultado.getFirst().getSeccion().getCicloAcademico())).isTrue();
    }
}
