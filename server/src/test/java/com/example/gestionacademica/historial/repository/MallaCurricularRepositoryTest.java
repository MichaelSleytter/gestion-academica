package com.example.gestionacademica.historial.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gestionacademica.administradores.services.AdministradorService;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.historial.domain.MallaCurricular;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("MallaCurricularRepository")
class MallaCurricularRepositoryTest {

    @MockBean
    private AdministradorService administradorService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MallaCurricularRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("rejects duplicate career-course curriculum entries")
    void rejectsDuplicateCareerCourseEntries() {
        Carrera carrera = carrera("Sistemas");
        Curso curso = curso("MAT101", "Matemática I", 4);
        entityManager.persistAndFlush(malla(carrera, curso, 1, 4));

        MallaCurricular duplicada = malla(carrera, curso, 2, 5);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicada))
            .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    @DisplayName("requires positive credits and recommended cycle")
    void requiresPositiveCreditsAndRecommendedCycle() {
        Carrera carrera = carrera("Industrial");
        Curso curso = curso("IND101", "Procesos I", 3);
        MallaCurricular invalida = malla(carrera, curso, 0, 0);

        assertThatThrownBy(() -> entityManager.persistAndFlush(invalida))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("cicloRecomendado")
            .hasMessageContaining("creditos");
    }

    @Test
    @DisplayName("fetches curriculum courses for a career without lazy access failures")
    void fetchesCurriculumCoursesForCareer() {
        Carrera sistemas = carrera("Software");
        Carrera derecho = carrera("Derecho");
        Curso algebra = curso("MAT102", "Álgebra", 4);
        Curso civil = curso("DER101", "Derecho Civil", 3);
        entityManager.persistAndFlush(malla(sistemas, algebra, 2, 4));
        entityManager.persistAndFlush(malla(derecho, civil, 1, 3));
        entityManager.clear();

        List<MallaCurricular> resultado = repository.findByCarreraIdWithCurso(sistemas.getIdCarrera());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getCurso().getNombre()).contains("Álgebra");
        assertThat(Hibernate.isInitialized(resultado.getFirst().getCurso())).isTrue();
    }

    private Carrera carrera(String nombre) {
        Carrera carrera = new Carrera();
        carrera.setNombre(nombre + "-" + System.nanoTime());
        return entityManager.persistFlushFind(carrera);
    }

    private Curso curso(String codigo, String nombre, int creditos) {
        Curso curso = new Curso();
        curso.setCodigo(codigo + "-" + System.nanoTime());
        curso.setNombre(nombre + "-" + System.nanoTime());
        curso.setCreditos(creditos);
        return entityManager.persistFlushFind(curso);
    }

    private MallaCurricular malla(Carrera carrera, Curso curso, int ciclo, int creditos) {
        MallaCurricular malla = new MallaCurricular();
        malla.setCarrera(carrera);
        malla.setCurso(curso);
        malla.setCicloRecomendado(ciclo);
        malla.setObligatorio(true);
        malla.setCreditos(creditos);
        return malla;
    }
}
