package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.Horario;
import com.example.gestionacademica.repositories.HorarioRepository;
import com.example.gestionacademica.repositories.SeccionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para HorarioService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - HorarioService")
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private HorarioService horarioService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de horarios")
    void listarTodos_debeRetornarLista() {
        Horario horario = new Horario();
        when(horarioRepository.findAll()).thenReturn(List.of(horario));

        List<Horario> resultado = horarioService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(horario);
        verify(horarioRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar horario cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Horario horario = new Horario();
        when(horarioRepository.findById(1)).thenReturn(Optional.of(horario));

        Horario resultado = horarioService.buscarPorId(1);

        assertThat(resultado).isSameAs(horario);
        verify(horarioRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(horarioRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar horario cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(horarioRepository.existsById(1)).thenReturn(true);

        horarioService.eliminar(1);

        verify(horarioRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(horarioRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> horarioService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(horarioRepository, never()).deleteById(999);
    }
}
