package com.example.gestionacademica.cursos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.repository.CicloAcademicoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para CicloAcademicoService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - CicloAcademicoService")
class CicloAcademicoServiceTest {

    @Mock
    private CicloAcademicoRepository cicloAcademicoRepository;

    @InjectMocks
    private CicloAcademicoService cicloAcademicoService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de ciclos academicos")
    void listarTodos_debeRetornarLista() {
        CicloAcademico ciclo = new CicloAcademico();
        when(cicloAcademicoRepository.findAll()).thenReturn(List.of(ciclo));

        List<CicloAcademico> resultado = cicloAcademicoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(ciclo);
        verify(cicloAcademicoRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar ciclo academico cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        CicloAcademico ciclo = new CicloAcademico();
        when(cicloAcademicoRepository.findById(1)).thenReturn(Optional.of(ciclo));

        CicloAcademico resultado = cicloAcademicoService.buscarPorId(1);

        assertThat(resultado).isSameAs(ciclo);
        verify(cicloAcademicoRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(cicloAcademicoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cicloAcademicoService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar ciclo academico cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(cicloAcademicoRepository.existsById(1)).thenReturn(true);

        cicloAcademicoService.eliminar(1);

        verify(cicloAcademicoRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(cicloAcademicoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> cicloAcademicoService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(cicloAcademicoRepository, never()).deleteById(999);
    }
}
