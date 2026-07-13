package com.example.gestionacademica.cursos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.CicloAcademicoRepository;
import com.example.gestionacademica.cursos.repository.CursoRepository;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
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

    /** Verifica que generarProximoCodigo use siglas, sufijo de ciclo y secuencia. */
    @Test
    @DisplayName("generarProximoCodigo: debe usar siglas de curso, ciclo y secuencia")
    void generarProximoCodigo_debeUsarSiglasCicloYSecuencia() {
        Curso curso = new Curso();
        curso.setIdCurso(1);
        curso.setNombre("Matemáticas I");

        CicloAcademico ciclo = new CicloAcademico();
        ciclo.setIdCiclo(2);
        ciclo.setNombre("2026-I");

        Seccion existente = new Seccion();
        existente.setCodigoSeccion("MAT-I-002");

        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));
        when(cicloAcademicoRepository.findById(2)).thenReturn(Optional.of(ciclo));
        when(seccionRepository.findTopByCurso_IdCursoAndCicloAcademico_IdCicloAndCodigoSeccionStartingWithOrderByCodigoSeccionDesc(
                1, 2, "MAT-I-"))
            .thenReturn(Optional.of(existente));

        String codigo = seccionService.generarProximoCodigo(1, 2);

        assertThat(codigo).isEqualTo("MAT-I-003");
    }

    /** Verifica que crear genere el codigo cuando no se envia manualmente. */
    @Test
    @DisplayName("crear: debe autogenerar codigo cuando viene vacio")
    void crear_conCodigoVacio_debeAutogenerarCodigo() {
        Curso curso = new Curso();
        curso.setIdCurso(1);
        curso.setNombre("Programación Web");

        CicloAcademico ciclo = new CicloAcademico();
        ciclo.setIdCiclo(2);
        ciclo.setNombre("2026-II");

        Seccion seccion = new Seccion();
        seccion.setCodigoSeccion(" ");
        seccion.setVacantes(30);

        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));
        when(cicloAcademicoRepository.findById(2)).thenReturn(Optional.of(ciclo));
        when(seccionRepository.findTopByCurso_IdCursoAndCicloAcademico_IdCicloAndCodigoSeccionStartingWithOrderByCodigoSeccionDesc(
                1, 2, "PRO-II-"))
            .thenReturn(Optional.empty());
        when(seccionRepository.existsByCodigoSeccion("PRO-II-001")).thenReturn(false);
        when(seccionRepository.save(seccion)).thenReturn(seccion);

        Seccion resultado = seccionService.crear(seccion, 1, 2);

        assertThat(resultado.getCodigoSeccion()).isEqualTo("PRO-II-001");
        assertThat(resultado.getColor()).isNotBlank();
    }
}

