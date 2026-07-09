package com.example.gestionacademica.historial.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.gestionacademica.administradores.services.AdministradorService;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.historial.domain.Prerrequisito;
import com.example.gestionacademica.historial.domain.TipoReglaPrerrequisito;
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
@DisplayName("PrerrequisitoRepository")
class PrerrequisitoRepositoryTest {

    @MockBean
    private AdministradorService administradorService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PrerrequisitoRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("rejects duplicate career-course-prerequisite rules")
    void rejectsDuplicatePrerequisiteRules() {
        Carrera carrera = carrera("Sistemas");
        Curso destino = curso("MAT102", "Matemática II");
        Curso previo = curso("MAT101", "Matemática I");
        entityManager.persistAndFlush(prerrequisito(carrera, destino, previo));

        Prerrequisito duplicado = prerrequisito(carrera, destino, previo);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicado))
            .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    @DisplayName("rejects a course as its own prerequisite")
    void rejectsSelfPrerequisite() {
        Carrera carrera = carrera("Industrial");
        Curso curso = curso("IND101", "Procesos I");
        Prerrequisito invalido = prerrequisito(carrera, curso, curso);

        assertThatThrownBy(() -> entityManager.persistAndFlush(invalido))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("own prerequisite");
    }

    @Test
    @DisplayName("fetches target and prerequisite courses without lazy access failures")
    void fetchesTargetAndPrerequisiteCourses() {
        Carrera carrera = carrera("Software");
        Curso destino = curso("PRO102", "Programación II");
        Curso previo = curso("PRO101", "Programación I");
        entityManager.persistAndFlush(prerrequisito(carrera, destino, previo));
        entityManager.clear();

        List<Prerrequisito> resultado = repository.findByCarreraIdWithCursos(carrera.getIdCarrera());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getCurso().getNombre()).contains("Programación II");
        assertThat(resultado.getFirst().getCursoPrerrequisito().getNombre()).contains("Programación I");
        assertThat(Hibernate.isInitialized(resultado.getFirst().getCurso())).isTrue();
        assertThat(Hibernate.isInitialized(resultado.getFirst().getCursoPrerrequisito())).isTrue();
    }

    private Carrera carrera(String nombre) {
        Carrera carrera = new Carrera();
        carrera.setNombre(nombre + "-" + System.nanoTime());
        return entityManager.persistFlushFind(carrera);
    }

    private Curso curso(String codigo, String nombre) {
        Curso curso = new Curso();
        curso.setCodigo(codigo + "-" + System.nanoTime());
        curso.setNombre(nombre + "-" + System.nanoTime());
        curso.setCreditos(4);
        return entityManager.persistFlushFind(curso);
    }

    private Prerrequisito prerrequisito(Carrera carrera, Curso curso, Curso previo) {
        Prerrequisito prerrequisito = new Prerrequisito();
        prerrequisito.setCarrera(carrera);
        prerrequisito.setCurso(curso);
        prerrequisito.setCursoPrerrequisito(previo);
        prerrequisito.setTipoRegla(TipoReglaPrerrequisito.HARD);
        return prerrequisito;
    }
}
