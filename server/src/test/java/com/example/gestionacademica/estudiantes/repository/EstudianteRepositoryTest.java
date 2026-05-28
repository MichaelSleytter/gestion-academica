package com.example.gestionacademica.estudiantes.repository;

import static org.assertj.core.api.Assertions.*;

import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.auth.domain.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas unitarias para EstudianteRepository.
 * Usa @Transactional para rollback automático tras cada test.
 * No contamina la BD real.
 *
 * @author TechNova Solutions
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("Pruebas - EstudianteRepository")
class EstudianteRepositoryTest {

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    // ── Helper: crea un estudiante fresco en cada llamada ────────────
    private Estudiante crearEstudianteFresco(String sufijo) {
        String token = generarTokenCorto(sufijo);

        TipoDocumento tipo = new TipoDocumento();
        tipo.setNombre("DNI-" + token);
        tipo = tipoDocumentoRepository.saveAndFlush(tipo);

        Carrera carrera = new Carrera();
        carrera.setNombre("Carrera-" + token);
        carrera = carreraRepository.saveAndFlush(carrera);

        Usuario usuario = new Usuario();
        usuario.setNombre("Nombre-" + token);
        usuario.setApellido("Apellido-" + token);
        usuario.setEmail("test-" + token + "@test.com");
        usuario.setPassword("pass123");
        usuario.setNumeroDocumento("DOC-" + token);
        usuario.setEstado(true);
        usuario.setTipoDocumento(tipo);
        usuario = usuarioRepository.saveAndFlush(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setCodigoEstudiante("COD-" + token);
        estudiante.setCiclo(3);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
        estudiante.setUsuario(usuario);
        estudiante.setCarrera(carrera);

        return estudianteRepository.saveAndFlush(estudiante);
    }

    private String generarTokenCorto(String base) {
        String limpio = base.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String tokenBase =
            limpio +
            UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return tokenBase.substring(0, 8);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 1: Guardar y recuperar por ID
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe guardar un estudiante y recuperarlo por ID")
    void debeGuardarYRecuperarPorId() {
        Estudiante guardado = crearEstudianteFresco("T1-" + System.nanoTime());

        Optional<Estudiante> resultado = estudianteRepository.findById(
            guardado.getIdUsuario()
        );

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCiclo()).isEqualTo(3);
        assertThat(resultado.get().getEstadoAcademico()).isEqualTo(
            EstudianteEstadoAcademico.ACTIVO
        );
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 2: Buscar por codigo de estudiante
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe encontrar un estudiante por su codigo unico")
    void debeEncontrarPorCodigoEstudiante() {
        Estudiante guardado = crearEstudianteFresco("T2-" + System.nanoTime());

        Optional<Estudiante> resultado =
            estudianteRepository.findByCodigoEstudiante(
                guardado.getCodigoEstudiante()
            );

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getIdUsuario()).isEqualTo(
            guardado.getIdUsuario()
        );
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 3: Buscar por carrera
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe listar estudiantes de una carrera específica")
    void debeListarEstudiantesPorCarrera() {
        Estudiante guardado = crearEstudianteFresco("T3-" + System.nanoTime());

        List<Estudiante> resultado =
            estudianteRepository.findByCarrera_IdCarrera(
                guardado.getCarrera().getIdCarrera()
            );

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getCarrera().getIdCarrera()).isEqualTo(
            guardado.getCarrera().getIdCarrera()
        );
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 4: Buscar por ciclo
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe listar estudiantes de un ciclo específico")
    void debeListarEstudiantesPorCiclo() {
        crearEstudianteFresco("T4-" + System.nanoTime());

        List<Estudiante> resultado = estudianteRepository.findByCiclo(3);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getCiclo()).isEqualTo(3);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 5: Verificar existencia por codigo — debe retornar true
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe retornar true si el codigo de estudiante ya existe")
    void debeVerificarExistenciaDeCodigoEstudiante() {
        Estudiante guardado = crearEstudianteFresco("T5-" + System.nanoTime());

        boolean existe = estudianteRepository.existsByCodigoEstudiante(
            guardado.getCodigoEstudiante()
        );

        assertThat(existe).isTrue();
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 6: codigo inexistente debe retornar false
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe retornar false si el codigo de estudiante NO existe")
    void debeRetornarFalseParaCodigoInexistente() {
        boolean existe = estudianteRepository.existsByCodigoEstudiante(
            "CODIGO-INEXISTENTE-99999"
        );

        assertThat(existe).isFalse();
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 7: Eliminar estudiante
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debe eliminar un estudiante correctamente")
    void debeEliminarEstudiante() {
        Estudiante guardado = crearEstudianteFresco("T7-" + System.nanoTime());
        Integer id = guardado.getIdUsuario();

        estudianteRepository.deleteById(id);
        estudianteRepository.flush();

        Optional<Estudiante> resultado = estudianteRepository.findById(id);
        assertThat(resultado).isEmpty();
    }
}
