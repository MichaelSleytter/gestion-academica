package com.example.gestionacademica.docentes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.EspecializacionRepository;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.catalogos.repository.GradoAcademicoRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para DocenteService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - DocenteService")
class DocenteServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GradoAcademicoRepository gradoAcademicoRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private EspecializacionRepository especializacionRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DocenteService docenteService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de docentes")
    void listarTodos_debeRetornarLista() {
        Docente docente = new Docente();
        when(docenteRepository.findAll()).thenReturn(List.of(docente));

        List<Docente> resultado = docenteService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(docente);
        verify(docenteRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar docente cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Docente docente = new Docente();
        when(docenteRepository.findById(1)).thenReturn(Optional.of(docente));

        Docente resultado = docenteService.buscarPorId(1);

        assertThat(resultado).isSameAs(docente);
        verify(docenteRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(docenteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar docente cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(docenteRepository.existsById(1)).thenReturn(true);

        docenteService.eliminar(1);

        verify(docenteRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(docenteRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> docenteService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(docenteRepository, never()).deleteById(999);
    }

    /** Verifica que crear asigne la especializacion seleccionada. */
    @Test
    @DisplayName("crear: debe asignar especializacion por ID")
    void crear_debeAsignarEspecializacionPorId() {
        Usuario usuario = new Usuario();
        usuario.setEmail("docente@universidad.edu");
        usuario.setNumeroDocumento("12345678");
        usuario.setPassword("secret123");

        Docente docente = new Docente();
        GradoAcademico grado = new GradoAcademico();
        TipoDocumento tipoDocumento = new TipoDocumento();
        Especializacion especializacion = new Especializacion();
        Rol rol = new Rol();

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())).thenReturn(false);
        when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDocumento));
        when(gradoAcademicoRepository.findById(2)).thenReturn(Optional.of(grado));
        when(especializacionRepository.findById(3)).thenReturn(Optional.of(especializacion));
        when(rolRepository.findByNombreIgnoreCase("DOCENTE")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(docenteRepository.save(docente)).thenReturn(docente);

        Docente resultado = docenteService.crear(usuario, docente, 2, 1, 3);

        assertThat(resultado.getEspecializacion()).isSameAs(especializacion);
        verify(docenteRepository, times(1)).save(docente);
    }

    /** Verifica que el texto legacy siga funcionando durante la migracion. */
    @Test
    @DisplayName("crear: debe permitir especialidad legacy sin especializacion catalogada")
    void crear_conEspecializacionNula_debePreservarEspecialidadLegacy() {
        Usuario usuario = new Usuario();
        usuario.setEmail("legacy@universidad.edu");
        usuario.setNumeroDocumento("87654321");
        usuario.setPassword("secret123");

        Docente docente = new Docente();
        docente.setEspecialidad("Matemáticas");
        GradoAcademico grado = new GradoAcademico();
        TipoDocumento tipoDocumento = new TipoDocumento();
        Rol rol = new Rol();

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())).thenReturn(false);
        when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDocumento));
        when(gradoAcademicoRepository.findById(2)).thenReturn(Optional.of(grado));
        when(rolRepository.findByNombreIgnoreCase("DOCENTE")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(docenteRepository.save(docente)).thenReturn(docente);

        Docente resultado = docenteService.crear(usuario, docente, 2, 1, null);

        assertThat(resultado.getEspecializacion()).isNull();
        assertThat(resultado.getEspecialidad()).isEqualTo("Matemáticas");
        verify(especializacionRepository, never()).findById(any());
        verify(docenteRepository, times(1)).save(docente);
    }
}

