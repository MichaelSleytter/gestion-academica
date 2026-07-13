package com.example.gestionacademica.matriculas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.dto.MatriculaMisCursosDTO;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import java.time.LocalDate;
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

    @Test
    @DisplayName("listarMisCursos: debe incluir fechas del ciclo académico")
    void listarMisCursos_debeIncluirFechasDelCiclo() {
        Matricula matricula = mock(Matricula.class);
        Seccion seccion = mock(Seccion.class);
        Curso curso = mock(Curso.class);
        CicloAcademico ciclo = mock(CicloAcademico.class);
        LocalDate fechaInicio = LocalDate.of(2026, 3, 1);
        LocalDate fechaFin = LocalDate.of(2026, 7, 15);
        when(matricula.getSeccion()).thenReturn(seccion);
        when(matricula.getIdMatricula()).thenReturn(10);
        when(matricula.getEstado()).thenReturn(MatriculaEstado.ACTIVA);
        when(seccion.getCurso()).thenReturn(curso);
        when(seccion.getCicloAcademico()).thenReturn(ciclo);
        when(ciclo.getFechaInicio()).thenReturn(fechaInicio);
        when(ciclo.getFechaFin()).thenReturn(fechaFin);
        when(matriculaRepository.findActiveByEstudianteIdWithSeccionCurso(1)).thenReturn(List.of(matricula));

        MatriculaMisCursosDTO resultado = matriculaService.listarMisCursos(1).getFirst();

        assertThat(resultado.fechaInicio()).isEqualTo(fechaInicio);
        assertThat(resultado.fechaFin()).isEqualTo(fechaFin);
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
