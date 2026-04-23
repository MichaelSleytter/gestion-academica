package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.Seccion;
import com.example.gestionacademica.repositories.CicloAcademicoRepository;
import com.example.gestionacademica.repositories.CursoRepository;
import com.example.gestionacademica.repositories.MatriculaRepository;
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
 * Pruebas unitarias para SeccionService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - SeccionService")
class SeccionServiceTest {

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CicloAcademicoRepository cicloAcademicoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private SeccionService seccionService;

    /** Verifica que listarTodas retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de secciones")
    void listarTodas_debeRetornarLista() {
        Seccion seccion = new Seccion();
        when(seccionRepository.findAll()).thenReturn(List.of(seccion));

        List<Seccion> resultado = seccionService.listarTodas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(seccion);
        verify(seccionRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar seccion cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Seccion seccion = new Seccion();
        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));

        Seccion resultado = seccionService.buscarPorId(1);

        assertThat(resultado).isSameAs(seccion);
        verify(seccionRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(seccionRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seccionService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe y no tiene matriculas activas. */
    @Test
    @DisplayName("eliminar: debe eliminar seccion cuando existe y no tiene matriculados")
    void eliminar_cuandoExisteSinMatriculas_debeEliminar() {
        when(seccionRepository.existsById(1)).thenReturn(true);
        when(matriculaRepository.countMatriculadosActivos(1)).thenReturn(0L);

        seccionService.eliminar(1);

        verify(seccionRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(seccionRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> seccionService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(seccionRepository, never()).deleteById(999);
    }
}
