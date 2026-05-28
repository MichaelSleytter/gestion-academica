package com.example.gestionacademica.historial.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.repository.HistorialAcademicoRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para HistorialAcademicoService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - HistorialAcademicoService")
class HistorialAcademicoServiceTest {

    @Mock
    private HistorialAcademicoRepository historialAcademicoRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private HistorialAcademicoService historialAcademicoService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de historiales")
    void listarTodos_debeRetornarLista() {
        HistorialAcademico historial = new HistorialAcademico();
        when(historialAcademicoRepository.findAll()).thenReturn(List.of(historial));

        List<HistorialAcademico> resultado = historialAcademicoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(historial);
        verify(historialAcademicoRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar historial academico cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        HistorialAcademico historial = new HistorialAcademico();
        when(historialAcademicoRepository.findById(1)).thenReturn(Optional.of(historial));

        HistorialAcademico resultado = historialAcademicoService.buscarPorId(1);

        assertThat(resultado).isSameAs(historial);
        verify(historialAcademicoRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(historialAcademicoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historialAcademicoService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar historial academico cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(historialAcademicoRepository.existsById(1)).thenReturn(true);

        historialAcademicoService.eliminar(1);

        verify(historialAcademicoRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(historialAcademicoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> historialAcademicoService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(historialAcademicoRepository, never()).deleteById(999);
    }
}
