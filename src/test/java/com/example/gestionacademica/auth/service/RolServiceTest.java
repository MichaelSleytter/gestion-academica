package com.example.gestionacademica.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.repository.RolRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para RolService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - RolService")
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de roles")
    void listarTodos_debeRetornarLista() {
        Rol rol = new Rol();
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        List<Rol> resultado = rolService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(rol);
        verify(rolRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar rol cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Rol rol = new Rol();
        when(rolRepository.findById(1)).thenReturn(Optional.of(rol));

        Rol resultado = rolService.buscarPorId(1);

        assertThat(resultado).isSameAs(rol);
        verify(rolRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(rolRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar rol cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(rolRepository.existsById(1)).thenReturn(true);

        rolService.eliminar(1);

        verify(rolRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(rolRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> rolService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(rolRepository, never()).deleteById(999);
    }
}
