package com.example.gestionacademica.catalogos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para CarreraService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - CarreraService")
class CarreraServiceTest {

    @Mock
    private CarreraRepository carreraRepository;

    @InjectMocks
    private CarreraService carreraService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de carreras")
    void listarTodas_debeRetornarLista() {
        Carrera carrera = new Carrera();
        when(carreraRepository.findAll()).thenReturn(List.of(carrera));

        List<Carrera> resultado = carreraService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(carrera);
        verify(carreraRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar carrera cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Carrera carrera = new Carrera();
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        Carrera resultado = carreraService.buscarPorId(1);

        assertThat(resultado).isSameAs(carrera);
        verify(carreraRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(carreraRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carreraService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar carrera cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(carreraRepository.existsById(1)).thenReturn(true);

        carreraService.eliminar(1);

        verify(carreraRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(carreraRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> carreraService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(carreraRepository, never()).deleteById(999);
    }
}
