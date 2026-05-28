package com.example.gestionacademica.notas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.notas.repository.NotaRepository;
import com.example.gestionacademica.evaluaciones.repository.EvaluacionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para NotaService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - NotaService")
class NotaServiceTest {

    @Mock
    private NotaRepository notaRepository;

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @InjectMocks
    private NotaService notaService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de notas")
    void listarTodas_debeRetornarLista() {
        Nota nota = new Nota();
        when(notaRepository.findAll()).thenReturn(List.of(nota));

        List<Nota> resultado = notaService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(nota);
        verify(notaRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar nota cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Nota nota = new Nota();
        when(notaRepository.findById(1)).thenReturn(Optional.of(nota));

        Nota resultado = notaService.buscarPorId(1);

        assertThat(resultado).isSameAs(nota);
        verify(notaRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(notaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notaService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar nota cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(notaRepository.existsById(1)).thenReturn(true);

        notaService.eliminar(1);

        verify(notaRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(notaRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> notaService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(notaRepository, never()).deleteById(999);
    }
}
