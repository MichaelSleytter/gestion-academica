package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.Evaluacion;
import com.example.gestionacademica.repositories.EvaluacionRepository;
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
 * Pruebas unitarias para EvaluacionService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - EvaluacionService")
class EvaluacionServiceTest {

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private EvaluacionService evaluacionService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de evaluaciones")
    void listarTodas_debeRetornarLista() {
        Evaluacion evaluacion = new Evaluacion();
        when(evaluacionRepository.findAll()).thenReturn(List.of(evaluacion));

        List<Evaluacion> resultado = evaluacionService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(evaluacion);
        verify(evaluacionRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar evaluacion cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Evaluacion evaluacion = new Evaluacion();
        when(evaluacionRepository.findById(1)).thenReturn(Optional.of(evaluacion));

        Evaluacion resultado = evaluacionService.buscarPorId(1);

        assertThat(resultado).isSameAs(evaluacion);
        verify(evaluacionRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(evaluacionRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluacionService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar evaluacion cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(evaluacionRepository.existsById(1)).thenReturn(true);

        evaluacionService.eliminar(1);

        verify(evaluacionRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(evaluacionRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> evaluacionService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(evaluacionRepository, never()).deleteById(999);
    }
}
