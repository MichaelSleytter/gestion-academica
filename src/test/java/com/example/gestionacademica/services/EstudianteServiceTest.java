package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.*;
import com.example.gestionacademica.repositories.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private CarreraRepository carreraRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

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
        estudianteBase.setEstadoAcademico("ACTIVO");
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
        when(estudianteRepository.findAll())
                .thenReturn(List.of(estudianteBase));

        // Act
        List<Estudiante> resultado = estudianteService.listarTodos();

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoEstudiante())
                .isEqualTo("2024-IS-001");

        verify(estudianteRepository, times(1)).findAll();
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 2: buscarPorId — estudiante existe
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: debe retornar el estudiante cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEstudiante() {
        // Arrange
        when(estudianteRepository.findById(1))
                .thenReturn(Optional.of(estudianteBase));

        // Act
        Estudiante resultado = estudianteService.buscarPorId(1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdUsuario()).isEqualTo(1);
        assertThat(resultado.getCiclo()).isEqualTo(4);

        verify(estudianteRepository, times(1)).findById(1);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 3: buscarPorId — estudiante NO existe → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: debe lanzar excepción cuando el ID no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(estudianteRepository.findById(999))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.buscarPorId(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(estudianteRepository, times(1)).findById(999);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 4: crear — flujo exitoso
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe crear estudiante correctamente cuando los datos son válidos")
    void crear_conDatosValidos_debeCrearEstudiante() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumento(anyString())).thenReturn(false);
        when(estudianteRepository.existsByCodigoEstudiante(anyString())).thenReturn(false);
        when(tipoDocumentoRepository.findById(1))
                .thenReturn(Optional.of(tipoDocumentoBase));
        when(carreraRepository.findById(1))
                .thenReturn(Optional.of(carreraBase));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioBase);
        when(estudianteRepository.save(any(Estudiante.class)))
                .thenReturn(estudianteBase);

        // Act
        Estudiante resultado = estudianteService.crear(
                usuarioBase, estudianteBase, 1, 1);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigoEstudiante()).isEqualTo("2024-IS-001");
        assertThat(resultado.getEstadoAcademico()).isEqualTo("ACTIVO");

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 5: crear — email duplicado → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe lanzar excepción si el email ya está registrado")
    void crear_conEmailDuplicado_debeLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                estudianteService.crear(usuarioBase, estudianteBase, 1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");

        // Verificar que nunca llegó a guardar
        verify(usuarioRepository, never()).save(any());
        verify(estudianteRepository, never()).save(any());
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 6: crear — codigo de estudiante duplicado → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("crear: debe lanzar excepción si el codigo de estudiante ya existe")
    void crear_conCodigoDuplicado_debeLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumento(anyString())).thenReturn(false);
        when(estudianteRepository.existsByCodigoEstudiante(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() ->
                estudianteService.crear(usuarioBase, estudianteBase, 1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("codigo");

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
        datosNuevos.setEstadoAcademico("ACTIVO");

        when(estudianteRepository.findById(1))
                .thenReturn(Optional.of(estudianteBase));
        when(carreraRepository.findById(1))
                .thenReturn(Optional.of(carreraBase));
        when(estudianteRepository.save(any(Estudiante.class)))
                .thenAnswer(inv -> inv.getArgument(0));

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
        when(estudianteRepository.existsById(1)).thenReturn(true);
        doNothing().when(estudianteRepository).deleteById(1);

        // Act
        estudianteService.eliminar(1);

        // Assert
        verify(estudianteRepository, times(1)).existsById(1);
        verify(estudianteRepository, times(1)).deleteById(1);
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 9: eliminar — ID no existe → excepción
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: debe lanzar excepción cuando el ID no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        // Arrange
        when(estudianteRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> estudianteService.eliminar(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");

        verify(estudianteRepository, never()).deleteById(any());
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 10: listarPorCarrera — retorna lista filtrada
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarPorCarrera: debe retornar estudiantes de la carrera indicada")
    void listarPorCarrera_debeRetornarListaFiltrada() {
        // Arrange
        when(estudianteRepository.findByCarrera_IdCarrera(1))
                .thenReturn(List.of(estudianteBase));

        // Act
        List<Estudiante> resultado = estudianteService.listarPorCarrera(1);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCarrera().getIdCarrera()).isEqualTo(1);

        verify(estudianteRepository, times(1)).findByCarrera_IdCarrera(1);
    }
}