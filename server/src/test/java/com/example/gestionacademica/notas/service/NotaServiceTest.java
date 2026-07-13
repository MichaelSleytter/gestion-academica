package com.example.gestionacademica.notas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    @Mock
    private DocenteSeccionRepository docenteSeccionRepository;

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
        Nota nota = new Nota();
        when(notaRepository.findById(1)).thenReturn(Optional.of(nota));

        notaService.eliminar(1, adminAuth());

        verify(notaRepository, times(1)).delete(nota);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(notaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notaService.eliminar(999, adminAuth()))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(notaRepository, never()).delete(any());
    }

    private Authentication adminAuth() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
