package com.example.gestionacademica.historial.support;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.domain.HistorialAcademicoEstado;
import com.example.gestionacademica.historial.domain.MallaCurricular;
import com.example.gestionacademica.historial.domain.Prerrequisito;
import com.example.gestionacademica.historial.domain.TipoReglaPrerrequisito;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.notas.domain.Nota;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class HistorialProgresoTestData {

    private HistorialProgresoTestData() {
    }

    public static Carrera carrera(Integer id, String nombre) {
        Carrera carrera = new Carrera();
        carrera.setIdCarrera(id);
        carrera.setNombre(nombre);
        return carrera;
    }

    public static Usuario usuario(Integer id, String nombre, String apellido) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail("user" + id + "@test.com");
        usuario.setPassword("password");
        usuario.setNumeroDocumento("DOC" + id);
        usuario.setEstado(true);
        return usuario;
    }

    public static Estudiante estudiante(Integer id, String codigo, Carrera carrera) {
        Estudiante estudiante = new Estudiante();
        estudiante.setIdUsuario(id);
        estudiante.setCodigoEstudiante(codigo);
        estudiante.setCiclo(1);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
        estudiante.setCarrera(carrera);
        estudiante.setUsuario(usuario(id, "Ana", "Torres"));
        return estudiante;
    }

    public static Curso curso(Integer id, String codigo, String nombre, Integer creditos) {
        Curso curso = new Curso();
        curso.setIdCurso(id);
        curso.setCodigo(codigo);
        curso.setNombre(nombre);
        curso.setCreditos(creditos);
        curso.setDescripcion(nombre);
        return curso;
    }

    public static MallaCurricular malla(Carrera carrera, Curso curso, Integer ciclo, Boolean obligatorio, Integer creditos) {
        return MallaCurricular.builder()
            .idMallaCurricular(curso.getIdCurso())
            .carrera(carrera)
            .curso(curso)
            .cicloRecomendado(ciclo)
            .obligatorio(obligatorio)
            .creditos(creditos)
            .build();
    }

    public static Prerrequisito prerrequisito(Carrera carrera, Curso curso, Curso prerrequisito) {
        return Prerrequisito.builder()
            .idPrerrequisito(curso.getIdCurso() * 100 + prerrequisito.getIdCurso())
            .carrera(carrera)
            .curso(curso)
            .cursoPrerrequisito(prerrequisito)
            .tipoRegla(TipoReglaPrerrequisito.HARD)
            .build();
    }

    public static CicloAcademico ciclo(Integer id, LocalDate inicio, LocalDate fin) {
        CicloAcademico ciclo = new CicloAcademico();
        ciclo.setIdCiclo(id);
        ciclo.setNombre("Ciclo " + id);
        ciclo.setFechaInicio(inicio);
        ciclo.setFechaFin(fin);
        return ciclo;
    }

    public static Seccion seccion(Integer id, Curso curso, CicloAcademico ciclo) {
        Seccion seccion = new Seccion();
        seccion.setIdSeccion(id);
        seccion.setCodigoSeccion("SEC-" + id);
        seccion.setCicloAcademicoNombre(ciclo.getNombre());
        seccion.setVacantes(30);
        seccion.setCurso(curso);
        seccion.setCicloAcademico(ciclo);
        return seccion;
    }

    public static HistorialAcademico historial(Integer id, Estudiante estudiante, Seccion seccion) {
        HistorialAcademico historial = new HistorialAcademico();
        historial.setIdHistorial(id);
        historial.setEstudiante(estudiante);
        historial.setSeccion(seccion);
        historial.setEstado(HistorialAcademicoEstado.APROBADO);
        return historial;
    }

    public static Evaluacion evaluacion(Integer id, Seccion seccion, String porcentaje) {
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setIdEvaluacion(id);
        evaluacion.setNombre("Evaluacion " + id);
        evaluacion.setPorcentaje(new BigDecimal(porcentaje));
        evaluacion.setSeccion(seccion);
        return evaluacion;
    }

    public static Nota nota(Integer id, Estudiante estudiante, Evaluacion evaluacion, String valor) {
        Nota nota = new Nota();
        nota.setIdNota(id);
        nota.setEstudiante(estudiante);
        nota.setEvaluacion(evaluacion);
        nota.setNota(new BigDecimal(valor));
        return nota;
    }

    public static Matricula matricula(Integer id, Estudiante estudiante, Seccion seccion) {
        Matricula matricula = new Matricula();
        matricula.setIdMatricula(id);
        matricula.setEstudiante(estudiante);
        matricula.setSeccion(seccion);
        matricula.setEstado(MatriculaEstado.ACTIVA);
        matricula.setFechaMatricula(LocalDateTime.now());
        return matricula;
    }
}
