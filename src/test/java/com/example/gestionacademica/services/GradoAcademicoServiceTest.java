package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.GradoAcademico;
import com.example.gestionacademica.repositories.GradoAcademicoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para GradoAcademicoService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - GradoAcademicoService")
class GradoAcademicoServiceTest {

    @Mock
    private GradoAcademicoRepository gradoAcademicoRepository;

    @InjectMocks
    private GradoAcademicoService gradoAcademicoService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de grados academicos")
    void listarTodos_debeRetornarLista() {
        GradoAcademico grado = new GradoAcademico();
        when(gradoAcademicoRepository.findAll()).thenReturn(List.of(grado));

        List<GradoAcademico> resultado = gradoAcademicoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(grado);
        verify(gradoAcademicoRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar grado academico cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        GradoAcademico grado = new GradoAcademico();
        when(gradoAcademicoRepository.findById(1)).thenReturn(Optional.of(grado));

        GradoAcademico resultado = gradoAcademicoService.buscarPorId(1);

        assertThat(resultado).isSameAs(grado);
        verify(gradoAcademicoRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(gradoAcademicoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradoAcademicoService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar grado academico cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(gradoAcademicoRepository.existsById(1)).thenReturn(true);

        gradoAcademicoService.eliminar(1);

        verify(gradoAcademicoRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(gradoAcademicoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> gradoAcademicoService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(gradoAcademicoRepository, never()).deleteById(999);
    }
}
