package com.example.gestionacademica.matriculas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para MatriculaService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - MatriculaService")
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @InjectMocks
    private MatriculaService matriculaService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de matriculas")
    void listarTodas_debeRetornarLista() {
        Matricula matricula = new Matricula();
        when(matriculaRepository.findAll()).thenReturn(List.of(matricula));

        List<Matricula> resultado = matriculaService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(matricula);
        verify(matriculaRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar matricula cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Matricula matricula = new Matricula();
        when(matriculaRepository.findById(1)).thenReturn(Optional.of(matricula));

        Matricula resultado = matriculaService.buscarPorId(1);

        assertThat(resultado).isSameAs(matricula);
        verify(matriculaRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(matriculaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matriculaService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar matricula cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(matriculaRepository.existsById(1)).thenReturn(true);

        matriculaService.eliminar(1);

        verify(matriculaRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(matriculaRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> matriculaService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(matriculaRepository, never()).deleteById(999);
    }
}
