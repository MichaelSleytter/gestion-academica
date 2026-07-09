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
import com.example.gestionacademica.historial.domain.MallaCurricular;
import com.example.gestionacademica.historial.domain.Prerrequisito;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.notas.domain.Nota;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class HistorialProgresoIntegrationData {

    @MockBean
    protected AdministradorService administradorService;

    @MockBean
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected TestEntityManager entityManager;

    protected IntegrationFixture persistFullProgressFixture() {
        Carrera carrera = carrera(entityManager, "Ingeniería de Sistemas");
        Estudiante estudiante = estudiante(entityManager, carrera, "Ana", "Torres");
        CicloAcademico ciclo = ciclo(entityManager);

        Curso matematicaI = curso(entityManager, "MAT101", "Matemática I", 4);
        Curso comunicacion = curso(entityManager, "COM101", "Comunicación", 3);
        Curso matematicaII = curso(entityManager, "MAT102", "Matemática II", 4);
        Curso algoritmos = curso(entityManager, "ALG201", "Algoritmos", 5);
        Curso introduccion = curso(entityManager, "INT100", "Introducción a la Universidad", 3);

        malla(entityManager, carrera, matematicaI, 1, true, 4);
        malla(entityManager, carrera, comunicacion, 1, true, 3);
        malla(entityManager, carrera, introduccion, 1, true, 3);
        malla(entityManager, carrera, matematicaII, 2, true, 4);
        malla(entityManager, carrera, algoritmos, 3, true, 5);
        prerrequisito(entityManager, carrera, matematicaII, matematicaI);
        prerrequisito(entityManager, carrera, algoritmos, matematicaII);

        Seccion seccionMatematicaI = seccion(entityManager, matematicaI, ciclo);
        Seccion seccionComunicacion = seccion(entityManager, comunicacion, ciclo);
        Seccion seccionMatematicaII = seccion(entityManager, matematicaII, ciclo);

        historial(entityManager, estudiante, seccionMatematicaI, "15.20");
        Evaluacion parcialMatematica = evaluacion(entityManager, seccionMatematicaI, "40.00");
        Evaluacion finalMatematica = evaluacion(entityManager, seccionMatematicaI, "60.00");
        nota(entityManager, estudiante, parcialMatematica, "14.00");
        nota(entityManager, estudiante, finalMatematica, "16.00");

        historial(entityManager, estudiante, seccionComunicacion, "12.00");
        Evaluacion finalComunicacion = evaluacion(entityManager, seccionComunicacion, "100.00");
        nota(entityManager, estudiante, finalComunicacion, "12.00");

        matricula(entityManager, estudiante, seccionMatematicaII, "ACTIVA");
        entityManager.flush();
        entityManager.clear();

        return new IntegrationFixture(
            carrera,
            estudiante,
            matematicaI,
            comunicacion,
            matematicaII,
            algoritmos,
            introduccion
        );
    }

    protected Estudiante estudiante(TestEntityManager em) {
        Carrera carrera = carrera(em, "Carrera");
        return estudiante(em, carrera, "Nombre", "Apellido");
    }

    protected Carrera carrera(TestEntityManager em, String nombre) {
        Carrera carrera = new Carrera();
        carrera.setNombre(nombre + "-" + token());
        return em.persistFlushFind(carrera);
    }

    protected Estudiante estudiante(TestEntityManager em, Carrera carrera, String nombre, String apellido) {
        Usuario usuario = usuario(em, nombre, apellido);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiante("EST-" + token());
        estudiante.setCiclo(1);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
        estudiante.setCarrera(carrera);
        return em.persistFlushFind(estudiante);
    }

    protected Usuario usuario(TestEntityManager em, String nombre, String apellido) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setNombre("DNI-" + token());
        em.persist(tipo);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail("test-" + token() + "@test.com");
        usuario.setPassword("pass");
        usuario.setNumeroDocumento(documentNumber());
        usuario.setEstado(true);
        usuario.setTipoDocumento(tipo);
        return em.persistFlushFind(usuario);
    }

    protected Curso curso(TestEntityManager em) {
        return curso(em, "CUR-" + token(), "Curso-" + token(), 4);
    }

    protected Curso curso(TestEntityManager em, String codigo, String nombre, Integer creditos) {
        Curso curso = new Curso();
        curso.setCodigo(codigo + "-" + token());
        curso.setNombre(nombre + "-" + token());
        curso.setCreditos(creditos);
        curso.setDescripcion(nombre);
        return em.persistFlushFind(curso);
    }

    protected CicloAcademico ciclo(TestEntityManager em) {
        CicloAcademico ciclo = HistorialProgresoTestData.ciclo(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 7, 15));
        ciclo.setNombre("2026-" + token());
        return em.persistFlushFind(ciclo);
    }

    protected Seccion seccion(TestEntityManager em, Curso curso, CicloAcademico ciclo) {
        Seccion seccion = HistorialProgresoTestData.seccion(null, curso, ciclo);
        seccion.setCodigoSeccion("SEC-" + token());
        return em.persistFlushFind(seccion);
    }

    protected MallaCurricular malla(
            TestEntityManager em,
            Carrera carrera,
            Curso curso,
            Integer ciclo,
            Boolean obligatorio,
            Integer creditos) {
        MallaCurricular entry = HistorialProgresoTestData.malla(carrera, curso, ciclo, obligatorio, creditos);
        entry.setIdMallaCurricular(null);
        return em.persistFlushFind(entry);
    }

    protected Prerrequisito prerrequisito(TestEntityManager em, Carrera carrera, Curso curso, Curso cursoPrerrequisito) {
        Prerrequisito entry = HistorialProgresoTestData.prerrequisito(carrera, curso, cursoPrerrequisito);
        entry.setIdPrerrequisito(null);
        return em.persistFlushFind(entry);
    }

    protected Matricula matricula(TestEntityManager em, Estudiante estudiante, Seccion seccion, String estado) {
        Matricula matricula = HistorialProgresoTestData.matricula(null, estudiante, seccion);
        matricula.setEstado(estado);
        matricula.setFechaMatricula(LocalDateTime.now());
        return em.persistFlushFind(matricula);
    }

    protected HistorialAcademico historial(TestEntityManager em, Estudiante estudiante, Seccion seccion) {
        return historial(em, estudiante, seccion, "15.00");
    }

    protected HistorialAcademico historial(
            TestEntityManager em,
            Estudiante estudiante,
            Seccion seccion,
            String notaFinal) {
        HistorialAcademico historial = HistorialProgresoTestData.historial(null, estudiante, seccion);
        historial.setEstado("APROBADO");
        historial.setNotaFinal(new BigDecimal(notaFinal));
        return em.persistFlushFind(historial);
    }

    protected Evaluacion evaluacion(TestEntityManager em, Seccion seccion) {
        return evaluacion(em, seccion, "40.00");
    }

    protected Evaluacion evaluacion(TestEntityManager em, Seccion seccion, String porcentaje) {
        Evaluacion evaluacion = HistorialProgresoTestData.evaluacion(null, seccion, porcentaje);
        evaluacion.setNombre("Parcial-" + token());
        return em.persistFlushFind(evaluacion);
    }

    protected Nota nota(TestEntityManager em, Estudiante estudiante, Evaluacion evaluacion) {
        return nota(em, estudiante, evaluacion, "16.00");
    }

    protected Nota nota(TestEntityManager em, Estudiante estudiante, Evaluacion evaluacion, String valor) {
        Nota nota = HistorialProgresoTestData.nota(null, estudiante, evaluacion, valor);
        return em.persistFlushFind(nota);
    }

    private String token() {
        return Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private String documentNumber() {
        long nano = System.nanoTime();
        if (nano < 0) {
            nano = -nano;
        }
        String digits = Long.toString(nano);
        return digits.length() >= 8 ? digits.substring(0, 8) : String.format("%08d", nano);
    }

    protected record IntegrationFixture(
        Carrera carrera,
        Estudiante estudiante,
        Curso matematicaI,
        Curso comunicacion,
        Curso matematicaII,
        Curso algoritmos,
        Curso introduccion
    ) {
    }
}
