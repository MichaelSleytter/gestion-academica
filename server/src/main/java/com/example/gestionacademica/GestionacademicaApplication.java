package com.example.gestionacademica;

import com.example.gestionacademica.administradores.services.AdministradorService;
import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import com.example.gestionacademica.catalogos.repository.GradoAcademicoRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.CicloAcademicoRepository;
import com.example.gestionacademica.cursos.repository.CursoRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.evaluaciones.repository.EvaluacionRepository;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Clase principal del Sistema de Gestión Académica Universitaria.
 *
 * <p>
 * TechNova Solutions - Avance 1
 * </p>
 *
 * <p>
 * Inicia el contexto de Spring Boot y levanta el servidor embebido Tomcat
 * en el puerto configurado en application.yaml (default: 8080).
 * </p>
 *
 * @author TechNova Solutions
 * @version 1.0.0
 */
@SpringBootApplication
public class GestionacademicaApplication {

    /**
     * Punto de entrada de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(GestionacademicaApplication.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner commandLineRunner(
            AdministradorService administradorService,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            TipoDocumentoRepository tipoDocumentoRepository,
            GradoAcademicoRepository gradoAcademicoRepository,
            CarreraRepository carreraRepository,
            DocenteRepository docenteRepository,
            EstudianteRepository estudianteRepository,
            CicloAcademicoRepository cicloAcademicoRepository,
            CursoRepository cursoRepository,
            SeccionRepository seccionRepository,
            DocenteSeccionRepository docenteSeccionRepository,
            EvaluacionRepository evaluacionRepository,
            MatriculaRepository matriculaRepository,
            TransactionTemplate txTemplate) {
        return args -> {
            administradorService.crearAdministradorSiNoExiste();

            txTemplate.executeWithoutResult(status -> {
                // ── Docente de prueba ─────────────────────────────────────────────
                if (usuarioRepository.findByEmail("docente@test.com").isEmpty()) {
                    TipoDocumento dni = tipoDocumentoRepository.findByNombreIgnoreCase("DNI")
                            .orElseGet(() -> {
                                TipoDocumento t = new TipoDocumento();
                                t.setNombre("DNI");
                                return tipoDocumentoRepository.save(t);
                            });

                    Rol rolDocente = rolRepository.findByNombreIgnoreCase("DOCENTE")
                            .orElseGet(() -> {
                                Rol r = new Rol();
                                r.setNombre("DOCENTE");
                                return rolRepository.save(r);
                            });

                    GradoAcademico grado = gradoAcademicoRepository.findByNombreIgnoreCase("Magíster")
                            .orElseGet(() -> {
                                GradoAcademico g = new GradoAcademico();
                                g.setNombre("Magíster");
                                return gradoAcademicoRepository.save(g);
                            });

                    Usuario usuario = new Usuario();
                    usuario.setNombre("Carlos");
                    usuario.setApellido("López");
                    usuario.setEmail("docente@test.com");
                    usuario.setPassword(passwordEncoder.encode("Docente123!"));
                    usuario.setNumeroDocumento("83234234");
                    usuario.setTipoDocumento(dni);
                    usuario.setRoles(Collections.singletonList(rolDocente));
                    usuario.setEstado(true);
                    usuario = usuarioRepository.save(usuario);

                    Docente docente = new Docente();
                    docente.setUsuario(usuario);
                    docente.setEspecialidad("Matemáticas");
                    docente.setGradoAcademico(grado);
                    docenteRepository.save(docente);
                }

                // ── Estudiante de prueba ──────────────────────────────────────────
                if (usuarioRepository.findByEmail("estudiante@test.com").isEmpty()) {
                    TipoDocumento dni = tipoDocumentoRepository.findByNombreIgnoreCase("DNI")
                            .orElseGet(() -> {
                                TipoDocumento t = new TipoDocumento();
                                t.setNombre("DNI");
                                return tipoDocumentoRepository.save(t);
                            });

                    Rol rolEstudiante = rolRepository.findByNombreIgnoreCase("ESTUDIANTE")
                            .orElseGet(() -> {
                                Rol r = new Rol();
                                r.setNombre("ESTUDIANTE");
                                return rolRepository.save(r);
                            });

                    Carrera carrera = carreraRepository.findByNombre("Ingeniería de Sistemas")
                            .orElseGet(() -> {
                                Carrera c = new Carrera();
                                c.setNombre("Ingeniería de Sistemas");
                                return carreraRepository.save(c);
                            });

                    Usuario usuario = new Usuario();
                    usuario.setNombre("María");
                    usuario.setApellido("García");
                    usuario.setEmail("estudiante@test.com");
                    usuario.setPassword(passwordEncoder.encode("Estudiante123!"));
                    usuario.setNumeroDocumento("83453434");
                    usuario.setTipoDocumento(dni);
                    usuario.setRoles(Collections.singletonList(rolEstudiante));
                    usuario.setEstado(true);
                    usuario = usuarioRepository.save(usuario);

                    Estudiante estudiante = new Estudiante();
                    estudiante.setUsuario(usuario);
                    estudiante.setCodigoEstudiante("EST-00000001");
                    estudiante.setCiclo(3);
                    estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
                    estudiante.setCarrera(carrera);
                    estudianteRepository.save(estudiante);
                }

                // ── Seed: Ciclo Académico, Curso, Sección ─────────────────────────
                CicloAcademico ciclo = cicloAcademicoRepository.findByNombre("2024-I")
                        .orElseGet(() -> {
                            CicloAcademico c = new CicloAcademico();
                            c.setNombre("2024-I");
                            c.setFechaInicio(LocalDate.of(2024, 3, 1));
                            c.setFechaFin(LocalDate.of(2024, 7, 15));
                            return cicloAcademicoRepository.save(c);
                        });

                Curso curso = cursoRepository.existsByNombre("Matemática Básica")
                        ? cursoRepository.findByNombreContainingIgnoreCase("Matemática Básica").getFirst()
                        : cursoRepository.save(new Curso(null, "Matemática Básica", 4, "Curso introductorio de matemáticas", new ArrayList<>()));

                Seccion seccion = seccionRepository.findByCodigoSeccion("2024-I-MAT101-A")
                        .orElseGet(() -> {
                            Seccion s = new Seccion();
                            s.setCodigoSeccion("2024-I-MAT101-A");
                            s.setCicloAcademicoNombre("2024-I");
                            s.setVacantes(30);
                            s.setCurso(curso);
                            s.setCicloAcademico(ciclo);
                            return seccionRepository.save(s);
                        });

                // ── Asignar docente a la sección ───────────────────────────────────
                usuarioRepository.findByEmail("docente@test.com")
                        .flatMap(u -> docenteRepository.findById(u.getIdUsuario()))
                        .ifPresent(docente -> {
                            if (!docenteSeccionRepository.existsByDocente_IdUsuarioAndSeccion_IdSeccion(
                                    docente.getIdUsuario(), seccion.getIdSeccion())) {
                                DocenteSeccion ds = new DocenteSeccion();
                                ds.getId().setIdDocente(docente.getIdUsuario());
                                ds.getId().setIdSeccion(seccion.getIdSeccion());
                                ds.setDocente(docente);
                                ds.setSeccion(seccion);
                                docenteSeccionRepository.save(ds);
                            }
                        });

                // ── Evaluación de prueba ───────────────────────────────────────────
                if (!evaluacionRepository.existsByNombreAndSeccion_IdSeccion("Examen Parcial", seccion.getIdSeccion())) {
                    Evaluacion eval = new Evaluacion();
                    eval.setNombre("Examen Parcial");
                    eval.setPorcentaje(new BigDecimal("30.00"));
                    eval.setSeccion(seccion);
                    evaluacionRepository.save(eval);
                }

                // ── Matrícula del estudiante de prueba ─────────────────────────────
                usuarioRepository.findByEmail("estudiante@test.com")
                        .flatMap(u -> estudianteRepository.findById(u.getIdUsuario()))
                        .ifPresent(estudiante -> {
                            if (!matriculaRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(
                                    estudiante.getIdUsuario(), seccion.getIdSeccion())) {
                                Matricula mat = new Matricula();
                                mat.setEstado(MatriculaEstado.ACTIVA);
                                mat.setFechaMatricula(LocalDateTime.now());
                                mat.setEstudiante(estudiante);
                                mat.setSeccion(seccion);
                                matriculaRepository.save(mat);
                            }
                        });
            });
        };
    }
}
