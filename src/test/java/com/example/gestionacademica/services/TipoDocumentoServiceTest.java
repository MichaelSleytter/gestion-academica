package com.example.gestionacademica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.repositories.TipoDocumentoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para TipoDocumentoService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - TipoDocumentoService")
class TipoDocumentoServiceTest {

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private TipoDocumentoService tipoDocumentoService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de tipos de documento")
    void listarTodos_debeRetornarLista() {
        TipoDocumento tipo = new TipoDocumento();
        when(tipoDocumentoRepository.findAll()).thenReturn(List.of(tipo));

        List<TipoDocumento> resultado = tipoDocumentoService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(tipo);
        verify(tipoDocumentoRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar tipo de documento cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        TipoDocumento tipo = new TipoDocumento();
        when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(tipo));

        TipoDocumento resultado = tipoDocumentoService.buscarPorId(1);

        assertThat(resultado).isSameAs(tipo);
        verify(tipoDocumentoRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(tipoDocumentoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tipoDocumentoService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar tipo de documento cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(tipoDocumentoRepository.existsById(1)).thenReturn(true);

        tipoDocumentoService.eliminar(1);

        verify(tipoDocumentoRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(tipoDocumentoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> tipoDocumentoService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(tipoDocumentoRepository, never()).deleteById(999);
    }
}
