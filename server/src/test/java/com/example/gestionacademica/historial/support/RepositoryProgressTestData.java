package com.example.gestionacademica.historial.support;

import com.example.gestionacademica.administradores.services.AdministradorService;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.notas.domain.Nota;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class RepositoryProgressTestData {

    @MockBean
    protected AdministradorService administradorService;

    @MockBean
    protected PasswordEncoder passwordEncoder;

    protected Estudiante estudiante(TestEntityManager em) {
        String token = token();
        TipoDocumento tipo = new TipoDocumento();
        tipo.setNombre("DNI-" + token);
        em.persist(tipo);

        Carrera carrera = new Carrera();
        carrera.setNombre("Carrera-" + token);
        em.persist(carrera);

        Usuario usuario = new Usuario();
        usuario.setNombre("Nombre");
        usuario.setApellido("Apellido");
        usuario.setEmail("test-" + token + "@test.com");
        usuario.setPassword("pass");
        usuario.setNumeroDocumento(documentNumber());
        usuario.setEstado(true);
        usuario.setTipoDocumento(tipo);
        em.persist(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiante("EST-" + token());
        estudiante.setCiclo(1);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
        estudiante.setCarrera(carrera);
        return em.persistFlushFind(estudiante);
    }

    protected Curso curso(TestEntityManager em) {
        String token = token();
        Curso curso = new Curso();
curso.setCodigo("CUR-" + token());
        curso.setNombre("Curso-" + token());
        curso.setCreditos(4);
        return em.persistFlushFind(curso);
    }

    protected CicloAcademico ciclo(TestEntityManager em) {
        String token = token();
        CicloAcademico ciclo = new CicloAcademico();
        ciclo.setNombre("2026-" + token);
        ciclo.setFechaInicio(LocalDate.of(2026, 3, 1));
        ciclo.setFechaFin(LocalDate.of(2026, 7, 15));
        return em.persistFlushFind(ciclo);
    }

    protected Seccion seccion(TestEntityManager em, Curso curso, CicloAcademico ciclo) {
        Seccion seccion = new Seccion();
        seccion.setCodigoSeccion("SEC-" + token());
        seccion.setCicloAcademicoNombre(ciclo.getNombre());
        seccion.setVacantes(30);
        seccion.setCurso(curso);
        seccion.setCicloAcademico(ciclo);
        return em.persistFlushFind(seccion);
    }

    protected Matricula matricula(TestEntityManager em, Estudiante estudiante, Seccion seccion, String estado) {
        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setSeccion(seccion);
        matricula.setEstado(estado);
        matricula.setFechaMatricula(LocalDateTime.now());
        return em.persistFlushFind(matricula);
    }

    protected HistorialAcademico historial(TestEntityManager em, Estudiante estudiante, Seccion seccion) {
        HistorialAcademico historial = new HistorialAcademico();
        historial.setEstudiante(estudiante);
        historial.setSeccion(seccion);
        historial.setEstado("APROBADO");
        historial.setNotaFinal(new BigDecimal("15.00"));
        return em.persistFlushFind(historial);
    }

    protected Evaluacion evaluacion(TestEntityManager em, Seccion seccion) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setNombre("Parcial-" + token());
        evaluacion.setPorcentaje(new BigDecimal("40.00"));
        evaluacion.setSeccion(seccion);
        return em.persistFlushFind(evaluacion);
    }

    protected Nota nota(TestEntityManager em, Estudiante estudiante, Evaluacion evaluacion) {
        Nota nota = new Nota();
        nota.setEstudiante(estudiante);
        nota.setEvaluacion(evaluacion);
        nota.setNota(new BigDecimal("16.00"));
        return em.persistFlushFind(nota);
    }

    private String token() {
        return Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private String documentNumber() {
            long nano = System.nanoTime();
            if (nano < 0) nano = -nano;
            if (nano < 10000000) nano += 10000000;
            return Long.toString(nano).substring(0, 8);
        }
}
