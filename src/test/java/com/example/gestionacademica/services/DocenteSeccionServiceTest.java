package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.DocenteSeccion;
import com.example.gestionacademica.entities.DocenteSeccion.DocenteSeccionId;
import com.example.gestionacademica.repositories.DocenteRepository;
import com.example.gestionacademica.repositories.DocenteSeccionRepository;
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
 * Pruebas unitarias para DocenteSeccionService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - DocenteSeccionService")
class DocenteSeccionServiceTest {

    @Mock
    private DocenteSeccionRepository docenteSeccionRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private DocenteSeccionService docenteSeccionService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de asignaciones")
    void listarTodas_debeRetornarLista() {
        DocenteSeccion asignacion = new DocenteSeccion();
        when(docenteSeccionRepository.findAll()).thenReturn(List.of(asignacion));

        List<DocenteSeccion> resultado = docenteSeccionService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(asignacion);
        verify(docenteSeccionRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar asignacion cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        DocenteSeccion asignacion = new DocenteSeccion();
        DocenteSeccionId id = new DocenteSeccionId(1, 2);
        when(docenteSeccionRepository.findById(id)).thenReturn(Optional.of(asignacion));

        DocenteSeccion resultado = docenteSeccionService.buscarPorId(1, 2);

        assertThat(resultado).isSameAs(asignacion);
        verify(docenteSeccionRepository, times(1)).findById(id);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe la asignacion. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        DocenteSeccionId id = new DocenteSeccionId(1, 2);
        when(docenteSeccionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteSeccionService.buscarPorId(1, 2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("1")
            .hasMessageContaining("2");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar asignacion cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        DocenteSeccionId id = new DocenteSeccionId(1, 2);
        when(docenteSeccionRepository.existsById(id)).thenReturn(true);

        docenteSeccionService.eliminar(1, 2);

        verify(docenteSeccionRepository, times(1)).deleteById(id);
    }

    /** Verifica que eliminar no permita borrar cuando no existe la asignacion. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        DocenteSeccionId id = new DocenteSeccionId(9, 9);
        when(docenteSeccionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> docenteSeccionService.eliminar(9, 9))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("no encontrada");

        verify(docenteSeccionRepository, never()).deleteById(id);
    }
}
