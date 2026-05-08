package com.example.gestionacademica.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para UsuarioService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de usuarios")
    void listarTodos_debeRetornarLista() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(usuario);
        verify(usuarioRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar usuario cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1);

        assertThat(resultado).isSameAs(usuario);
        verify(usuarioRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar usuario cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(usuarioRepository.existsById(1)).thenReturn(true);

        usuarioService.eliminar(1);

        verify(usuarioRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(usuarioRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(usuarioRepository, never()).deleteById(999);
    }
}
