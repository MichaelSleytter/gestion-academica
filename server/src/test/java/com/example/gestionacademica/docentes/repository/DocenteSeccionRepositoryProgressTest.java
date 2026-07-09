package com.example.gestionacademica.docentes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gestionacademica.GestionacademicaApplication;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.historial.support.RepositoryProgressTestData;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
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
@DisplayName("DocenteSeccionRepository progress queries")
class DocenteSeccionRepositoryProgressTest extends RepositoryProgressTestData {

    @Autowired
    private DocenteSeccionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("existsDocenteAssignedToEstudiante: retorna true si el estudiante está en una sección asignada")
    void existsDocenteAssignedToEstudiante_conEstudianteEnSeccionAsignada_debeRetornarTrue() {
        Estudiante estudiante = estudiante(entityManager);
        Docente docente = docente(entityManager);
        Curso curso = curso(entityManager);
        CicloAcademico ciclo = ciclo(entityManager);
        Seccion seccion = seccion(entityManager, curso, ciclo);
        matricula(entityManager, estudiante, seccion, MatriculaEstado.ACTIVA);
        docenteSeccion(entityManager, docente, seccion);
        entityManager.clear();

        boolean exists = repository.existsDocenteAssignedToEstudiante(
            docente.getIdUsuario(),
            estudiante.getIdUsuario()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsDocenteAssignedToEstudiante: retorna false si el docente no tiene al estudiante asignado")
    void existsDocenteAssignedToEstudiante_conEstudianteNoAsignado_debeRetornarFalse() {
        Estudiante estudiante = estudiante(entityManager);
        Docente docente = docente(entityManager);
        Curso cursoAsignado = curso(entityManager);
        Curso cursoNoAsignado = curso(entityManager);
        CicloAcademico ciclo = ciclo(entityManager);
        Seccion seccionAsignada = seccion(entityManager, cursoAsignado, ciclo);
        Seccion seccionNoAsignada = seccion(entityManager, cursoNoAsignado, ciclo);
        docenteSeccion(entityManager, docente, seccionAsignada);
        matricula(entityManager, estudiante, seccionNoAsignada, MatriculaEstado.ACTIVA);
        entityManager.clear();

        boolean exists = repository.existsDocenteAssignedToEstudiante(
            docente.getIdUsuario(),
            estudiante.getIdUsuario()
        );

        assertThat(exists).isFalse();
    }

    private Docente docente(TestEntityManager em) {
        Usuario usuario = usuario(em);
        GradoAcademico grado = new GradoAcademico();
        grado.setNombre("Grado-" + token());
        em.persist(grado);

        Docente docente = new Docente();
        docente.setUsuario(usuario);
        docente.setEspecialidad("Especialidad");
        docente.setGradoAcademico(grado);
        return em.persistFlushFind(docente);
    }

    private Usuario usuario(TestEntityManager em) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setNombre("DOC-DNI-" + token());
        em.persist(tipo);

        Usuario usuario = new Usuario();
        usuario.setNombre("Docente");
        usuario.setApellido("Asignado");
        usuario.setEmail("docente-" + token() + "@test.com");
        usuario.setPassword("pass");
        usuario.setNumeroDocumento(documentNumber());
        usuario.setEstado(true);
        usuario.setTipoDocumento(tipo);
        return em.persistFlushFind(usuario);
    }

    private DocenteSeccion docenteSeccion(TestEntityManager em, Docente docente, Seccion seccion) {
        DocenteSeccion asignacion = new DocenteSeccion();
        asignacion.setDocente(docente);
        asignacion.setSeccion(seccion);
        asignacion.setId(new DocenteSeccion.DocenteSeccionId(docente.getIdUsuario(), seccion.getIdSeccion()));
        return em.persistFlushFind(asignacion);
    }

    private String token() {
        return Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private String documentNumber() {
        long nano = System.nanoTime();
        if (nano < 0) {
            nano = -nano;
        }
        if (nano < 10000000) {
            nano += 10000000;
        }
        return Long.toString(nano).substring(0, 8);
    }
}
