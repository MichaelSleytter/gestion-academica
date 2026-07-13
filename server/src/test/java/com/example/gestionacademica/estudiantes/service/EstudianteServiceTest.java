package com.example.gestionacademica.estudiantes.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.catalogos.service.CatalogoService;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.estudiantes.util.EstudianteUtil;
import com.example.gestionacademica.auth.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pruebas unitarias para EstudianteService.
 *
 * Estrategia: Mockito puro — sin contexto Spring, sin BD real.
 * Todos los repositorios son mocks controlados.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - EstudianteService")
class EstudianteServiceTest {

    // ── Mocks de dependencias ────────────────────────────────────────
    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private CarreraRepository carreraRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CatalogoService catalogoService;

    @Mock
    private EstudianteUtil estudianteUtil;

    @Mock
    private UsuarioFactory usuarioFactory;

    @Mock
    private EstudianteFactory estudianteFactory;

    @Mock
    private EstudianteValidator estudianteValidator;

    // ── Sistema bajo prueba ──────────────────────────────────────────
    @InjectMocks
    private EstudianteService estudianteService;

    // ── Datos de prueba reutilizables ────────────────────────────────
    private Estudiante estudianteBase;
    private Usuario usuarioBase;
    private Carrera carreraBase;
    private TipoDocumento tipoDocumentoBase;

    @BeforeEach
    void setUp() {
        // Los métodos estáticos de EstudianteUtil se ejecutan directamente
        // Solo mockeamos el PasswordEncoder (si se usa en el servicio)

        // Preparar tipo de documento
        tipoDocumentoBase = new TipoDocumento();
        tipoDocumentoBase.setIdTipoDocumento(1);
        tipoDocumentoBase.setNombre("DNI");

        // Preparar carrera
        carreraBase = new Carrera();
        carreraBase.setIdCarrera(1);
        carreraBase.setNombre("Ingeniería de Sistemas");

        // Preparar usuario base
        usuarioBase = new Usuario();
        usuarioBase.setIdUsuario(1);
        usuarioBase.setNombre("Ana");
        usuarioBase.setApellido("García");
        usuarioBase.setEmail("ana.garcia@test.com");
        usuarioBase.setPassword("pass123");
        usuarioBase.setNumeroDocumento("12345678");
        usuarioBase.setEstado(true);
        usuarioBase.setTipoDocumento(tipoDocumentoBase);

        // Preparar estudiante base
        estudianteBase = new Estudiante();
        estudianteBase.setIdUsuario(1);
        estudianteBase.setCodigoEstudiante("2024-IS-001");
        estudianteBase.setCiclo(4);
        estudianteBase.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);
        estudianteBase.setUsuario(usuarioBase);
        estudianteBase.setCarrera(carreraBase);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 1: listarTodos — retorna lista correctamente
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarTodos: debe retornar lista de todos los estudiantes")
    void listarTodos_debeRetornarLista() {
        // Arrange
        when(estudianteRepository.findAllByUsuario_EstadoTrue()).thenReturn(
                List.of(estudianteBase));

        // Act
        List<Estudiante> resultado = estudianteService.listarTodos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoEstudiante()).isEqualTo(
                "2024-IS-001");

        verify(estudianteRepository, times(1)).findAllByUsuario_EstadoTrue();
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 2: buscarPorId — estudiante existe
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: debe retornar el estudiante cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEstudiante() {
        // Arrange
        when(estudianteRepository.findByIdUsuarioAndUsuario_EstadoTrue(1)).thenReturn(
                Optional.of(estudianteBase));

        // Act
        Estudiante resultado = estudianteService.buscarPorId(1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1);
        assertThat(resultado.getCiclo()).isEqualTo(4);

        verify(estudianteRepository, times(1)).findByIdUsuarioAndUsuario_EstadoTrue(1);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 3: buscarPorId — estudiante NO existe → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: debe lanzar excepción cuando el ID no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(estudianteRepository.findByIdUsuarioAndUsuario_EstadoTrue(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.buscarPorId(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(estudianteRepository, times(1)).findByIdUsuarioAndUsuario_EstadoTrue(999);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 4: crear — flujo exitoso
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe crear estudiante correctamente cuando los datos son válidos")
    void crear_conDatosValidos_debeCrearEstudiante() {
        // Arrange
        EstudianteCrearDTO comando = crearComandoValido();
        Rol rolEstudiante = new Rol(1, "ESTUDIANTE", List.of());

        when(usuarioFactory.crearDesdeComando(comando)).thenReturn(usuarioBase);
        when(estudianteFactory.crearDesdeComando(comando)).thenReturn(estudianteBase);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(catalogoService.buscarTipoDocumentoPorId(1)).thenReturn(tipoDocumentoBase);
        when(catalogoService.buscarCarreraPorId(1)).thenReturn(carreraBase);
        when(rolRepository.findByNombreIgnoreCase("ESTUDIANTE")).thenReturn(Optional.of(rolEstudiante));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteBase);

        // Act
        EstudianteService.ResultadoCreacion resultado = estudianteService.crearConCredenciales(comando);

        // Assert
        assertThat(resultado.estudianteCreado()).isSameAs(estudianteBase);
        assertThat(resultado.contrasenaPlano()).isNotBlank();
        assertThat(usuarioBase.getEmail()).isEqualTo("2024-is-001@institution.edu.pe");
        assertThat(usuarioBase.getPassword()).isEqualTo("encoded-password");
        assertThat(usuarioBase.getRoles()).containsExactly(rolEstudiante);

        verify(estudianteValidator).validarDocumentoNoDuplicado("12345678");
        verify(estudianteValidator).asegurarCodigoEstudiante(estudianteBase);
        verify(estudianteValidator).validarCorreoNoDuplicado("2024-is-001@institution.edu.pe");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 5: crear — email duplicado → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe lanzar excepción si el email generado ya está registrado")
    void crear_conEmailDuplicado_debeLanzarExcepcion() {
        // Arrange
        EstudianteCrearDTO comando = crearComandoValido();
        when(usuarioFactory.crearDesdeComando(comando)).thenReturn(usuarioBase);
        when(estudianteFactory.crearDesdeComando(comando)).thenReturn(estudianteBase);
        doThrow(new RuntimeException("Ya existe un usuario con el email generado"))
                .when(estudianteValidator)
                .validarCorreoNoDuplicado("2024-is-001@institution.edu.pe");

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.crearConCredenciales(comando))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email generado");

        verify(usuarioRepository, never()).save(any());
        verify(estudianteRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 6: crear — documento duplicado → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe lanzar excepción si el documento ya existe")
    void crear_conDocumentoDuplicado_debeLanzarExcepcion() {
        // Arrange
        EstudianteCrearDTO comando = crearComandoValido();
        doThrow(new RuntimeException("Ya existe un usuario con el documento"))
                .when(estudianteValidator)
                .validarDocumentoNoDuplicado("12345678");

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.crearConCredenciales(comando))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("documento");

        verify(usuarioFactory, never()).crearDesdeComando(any());
        verify(usuarioRepository, never()).save(any());
        verify(estudianteRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 7: actualizar — flujo exitoso
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizar: debe actualizar correctamente los datos del estudiante")
    void actualizar_conDatosValidos_debeActualizarEstudiante() {
        // Arrange
        Estudiante datosNuevos = new Estudiante();
        datosNuevos.setCodigoEstudiante("2024-IS-001");
        datosNuevos.setCiclo(5);
        datosNuevos.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);

        when(estudianteRepository.findByIdUsuarioAndUsuario_EstadoTrue(1)).thenReturn(
                Optional.of(estudianteBase));
        when(catalogoService.buscarCarreraPorId(1)).thenReturn(carreraBase);
        when(estudianteRepository.save(any(Estudiante.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Estudiante resultado = estudianteService.actualizar(1, datosNuevos, 1);

        // Assert
        assertThat(resultado.getCiclo()).isEqualTo(5);
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 8: eliminar — estudiante existe → elimina correctamente
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: debe eliminar el estudiante cuando el ID existe")
    void eliminar_cuandoExiste_debeEliminarCorrectamente() {
        // Arrange
        when(estudianteRepository.findById(1)).thenReturn(
                Optional.of(estudianteBase));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioBase);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteBase);

        // Act
        estudianteService.eliminar(1);

        // Assert
        verify(estudianteRepository, times(1)).findById(1);
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    // ───────────────────────────────────────────────────────────────
    // TEST 10: listarPorCarrera — retorna lista filtrada
    // ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorCarrera: debe retornar estudiantes de la carrera indicada")
    void listarPorCarrera_debeRetornarListaFiltrada() {
        // Arrange
        when(estudianteRepository.findByCarrera_IdCarreraAndUsuario_EstadoTrue(1)).thenReturn(
                List.of(estudianteBase));

        // Act
        List<Estudiante> resultado = estudianteService.listarPorCarrera(1);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCarrera().getIdCarrera()).isEqualTo(1);

        verify(estudianteRepository, times(1)).findByCarrera_IdCarreraAndUsuario_EstadoTrue(1);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 9: eliminar — ID no existe → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: debe lanzar excepción cuando el ID no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(estudianteRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.eliminar(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(estudianteRepository, times(1)).findById(999);
        verify(estudianteRepository, never()).deleteById(any());
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 10: listarPorCarrera — retorna lista filtrada
    // ────────────────────────────────────────────────────────────────

    private EstudianteCrearDTO crearComandoValido() {
        return EstudianteCrearDTO.builder()
                .nombre("Ana")
                .apellido("García")
                .numeroDocumento("12345678")
                .idTipoDocumento(1)
                .emailPersonal("ana.garcia@test.com")
                .ciclo(4)
                .idCarrera(1)
                .build();
    }
}

