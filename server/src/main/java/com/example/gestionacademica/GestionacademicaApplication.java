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
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    CommandLineRunner commandLineRunner(
            AdministradorService administradorService,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            TipoDocumentoRepository tipoDocumentoRepository,
            GradoAcademicoRepository gradoAcademicoRepository,
            CarreraRepository carreraRepository,
            DocenteRepository docenteRepository,
            EstudianteRepository estudianteRepository) {
        return args -> {
            administradorService.crearAdministradorSiNoExiste();

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
                docente.setUsuario(usuario); // @MapsId usa el ID del usuario automáticamente
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
                usuario.setNumeroDocumento("12345678");
                usuario.setTipoDocumento(dni);
                usuario.setRoles(Collections.singletonList(rolEstudiante));
                usuario.setEstado(true);
                usuario = usuarioRepository.save(usuario);

                Estudiante estudiante = new Estudiante();
                estudiante.setUsuario(usuario); // @MapsId usa el ID del usuario automáticamente
                estudiante.setCodigoEstudiante("EST-00000001");
                estudiante.setCiclo(3);
                estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
                estudiante.setCarrera(carrera);
                estudianteRepository.save(estudiante);
            }
        };
    }
}
