package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.Curso;
import com.example.gestionacademica.repositories.CursoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para CursoService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - CursoService")
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private CursoService cursoService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de cursos")
    void listarTodos_debeRetornarLista() {
        Curso curso = new Curso();
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        List<Curso> resultado = cursoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(curso);
        verify(cursoRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar curso cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Curso curso = new Curso();
        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));

        Curso resultado = cursoService.buscarPorId(1);

        assertThat(resultado).isSameAs(curso);
        verify(cursoRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(cursoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cursoService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar curso cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(cursoRepository.existsById(1)).thenReturn(true);

        cursoService.eliminar(1);

        verify(cursoRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(cursoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> cursoService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(cursoRepository, never()).deleteById(999);
    }
}
