package com.example.gestionacademica.docentes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.catalogos.repository.GradoAcademicoRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias para DocenteService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - DocenteService")
class DocenteServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GradoAcademicoRepository gradoAcademicoRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @InjectMocks
    private DocenteService docenteService;

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de docentes")
    void listarTodos_debeRetornarLista() {
        Docente docente = new Docente();
        when(docenteRepository.findAll()).thenReturn(List.of(docente));

        List<Docente> resultado = docenteService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(docente);
        verify(docenteRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar docente cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Docente docente = new Docente();
        when(docenteRepository.findById(1)).thenReturn(Optional.of(docente));

        Docente resultado = docenteService.buscarPorId(1);

        assertThat(resultado).isSameAs(docente);
        verify(docenteRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(docenteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar docente cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(docenteRepository.existsById(1)).thenReturn(true);

        docenteService.eliminar(1);

        verify(docenteRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(docenteRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> docenteService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(docenteRepository, never()).deleteById(999);
    }
}
