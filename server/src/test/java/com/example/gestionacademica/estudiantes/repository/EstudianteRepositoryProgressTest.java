package com.example.gestionacademica.estudiantes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gestionacademica.GestionacademicaApplication;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.historial.support.RepositoryProgressTestData;
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
@DisplayName("EstudianteRepository progress queries")
class EstudianteRepositoryProgressTest extends RepositoryProgressTestData {

    @Autowired
    private EstudianteRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("fetches student with user and career")
    void fetchesStudentWithUserAndCareer() {
        Estudiante estudiante = estudiante(entityManager);
        entityManager.clear();

        Estudiante resultado = repository.findByIdWithUsuarioAndCarrera(estudiante.getIdUsuario()).orElseThrow();

        assertThat(resultado.getUsuario().getEmail()).contains("@test.com");
        assertThat(resultado.getCarrera().getNombre()).contains("Carrera-");
        assertThat(Hibernate.isInitialized(resultado.getUsuario())).isTrue();
        assertThat(Hibernate.isInitialized(resultado.getCarrera())).isTrue();
    }
}
