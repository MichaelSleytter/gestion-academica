package com.example.gestionacademica.cursos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.cursos.domain.Horario;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.HorarioRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Pruebas unitarias para HorarioService con Mockito puro.
 *
 * @author TechNova Solutions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - HorarioService")
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private SeccionRepository seccionRepository;

    @Mock
    private DocenteSeccionRepository docenteSeccionRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @InjectMocks
    private HorarioService horarioService;

    @Test
    @DisplayName("listarPorSeccion: ADMIN puede consultar cualquier sección")
    void listarPorSeccion_conAdmin_debePermitir() {
        Horario horario = new Horario();
        when(horarioRepository.findBySeccion_IdSeccion(7)).thenReturn(List.of(horario));

        List<Horario> resultado = horarioService.listarPorSeccion(7, auth(1, "ROLE_ADMIN"));

        assertThat(resultado).containsExactly(horario);
    }

    @Test
    @DisplayName("listarPorSeccion: DOCENTE asignado puede consultar")
    void listarPorSeccion_conDocenteAsignado_debePermitir() {
        Horario horario = new Horario();
        when(docenteSeccionRepository.existsByDocente_IdUsuarioAndSeccion_IdSeccion(20, 7)).thenReturn(true);
        when(horarioRepository.findBySeccion_IdSeccion(7)).thenReturn(List.of(horario));

        List<Horario> resultado = horarioService.listarPorSeccion(7, auth(20, "ROLE_DOCENTE"));

        assertThat(resultado).containsExactly(horario);
    }

    @Test
    @DisplayName("listarPorSeccion: DOCENTE no asignado no puede consultar")
    void listarPorSeccion_conDocenteNoAsignado_debeDenegar() {
        assertThatThrownBy(() -> horarioService.listarPorSeccion(7, auth(20, "ROLE_DOCENTE")))
                .isInstanceOf(AccessDeniedException.class);

        verify(horarioRepository, never()).findBySeccion_IdSeccion(7);
    }

    @Test
    @DisplayName("listarPorSeccion: ESTUDIANTE con matrícula activa puede consultar")
    void listarPorSeccion_conEstudianteMatriculadoActivo_debePermitir() {
        Horario horario = new Horario();
        when(matriculaRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccionAndEstado(
                30, 7, MatriculaEstado.ACTIVA)).thenReturn(true);
        when(horarioRepository.findBySeccion_IdSeccion(7)).thenReturn(List.of(horario));

        List<Horario> resultado = horarioService.listarPorSeccion(7, auth(30, "ROLE_ESTUDIANTE"));

        assertThat(resultado).containsExactly(horario);
    }

    @Test
    @DisplayName("listarPorSeccion: ESTUDIANTE sin matrícula activa no puede consultar")
    void listarPorSeccion_conEstudianteSinMatriculaActiva_debeDenegar() {
        when(matriculaRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccionAndEstado(
                30, 7, MatriculaEstado.ACTIVA)).thenReturn(false);

        assertThatThrownBy(() -> horarioService.listarPorSeccion(7, auth(30, "ROLE_ESTUDIANTE")))
                .isInstanceOf(AccessDeniedException.class);

        verify(horarioRepository, never()).findBySeccion_IdSeccion(7);
    }

    /** Verifica que listarTodos retorna los registros del repositorio. */
    @Test
    @DisplayName("listarTodos: debe retornar lista de horarios")
    void listarTodos_debeRetornarLista() {
        Horario horario = new Horario();
        when(horarioRepository.findAll()).thenReturn(List.of(horario));

        List<Horario> resultado = horarioService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isSameAs(horario);
        verify(horarioRepository, times(1)).findAll();
    }

    /** Verifica que buscarPorId retorna la entidad cuando existe. */
    @Test
    @DisplayName("buscarPorId: debe retornar horario cuando existe")
    void buscarPorId_cuandoExiste_debeRetornarEntidad() {
        Horario horario = new Horario();
        when(horarioRepository.findById(1)).thenReturn(Optional.of(horario));

        Horario resultado = horarioService.buscarPorId(1);

        assertThat(resultado).isSameAs(horario);
        verify(horarioRepository, times(1)).findById(1);
    }

    /** Verifica que buscarPorId lance excepcion cuando no existe el ID. */
    @Test
    @DisplayName("buscarPorId: debe lanzar excepcion cuando no existe")
    void buscarPorId_cuandoNoExiste_debeLanzarExcepcion() {
        when(horarioRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.buscarPorId(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");
    }

    /** Verifica que eliminar borra el registro cuando existe. */
    @Test
    @DisplayName("eliminar: debe eliminar horario cuando existe")
    void eliminar_cuandoExiste_debeEliminar() {
        when(horarioRepository.existsById(1)).thenReturn(true);

        horarioService.eliminar(1);

        verify(horarioRepository, times(1)).deleteById(1);
    }

    /** Verifica que eliminar no permita borrar cuando no existe el ID. */
    @Test
    @DisplayName("eliminar: debe lanzar excepcion cuando no existe")
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        when(horarioRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> horarioService.eliminar(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("999");

        verify(horarioRepository, never()).deleteById(999);
    }

    @Test
    @DisplayName("crear: permite mismo rango si aula y docentes no chocan")
    void crear_mismoRangoSinConflictos_debeGuardar() {
        Seccion seccion = seccion(1, "SEC-1");
        Horario nuevo = horario(null, seccion, "Aula 1");
        Horario cruce = horario(2, seccion(2, "SEC-2"), "Aula 2");

        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));
        when(horarioRepository.findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
                "Lunes", LocalTime.of(11, 0), LocalTime.of(9, 0))).thenReturn(List.of(cruce));
        when(docenteSeccionRepository.findBySeccion_IdSeccion(1)).thenReturn(List.of(docenteSeccion(10, 1)));
        when(docenteSeccionRepository.findBySeccion_IdSeccion(2)).thenReturn(List.of(docenteSeccion(20, 2)));
        when(horarioRepository.save(nuevo)).thenReturn(nuevo);

        Horario resultado = horarioService.crear(nuevo, 1);

        assertThat(resultado).isSameAs(nuevo);
        verify(horarioRepository).save(nuevo);
    }

    @Test
    @DisplayName("crear: bloquea cruce en la misma aula")
    void crear_mismaAulaSolapada_debeLanzarExcepcion() {
        Seccion seccion = seccion(1, "SEC-1");
        Horario nuevo = horario(null, seccion, " aula 1 ");
        Horario cruce = horario(2, seccion(2, "SEC-2"), "AULA 1");

        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));
        when(horarioRepository.findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
                "Lunes", LocalTime.of(11, 0), LocalTime.of(9, 0))).thenReturn(List.of(cruce));

        assertThatThrownBy(() -> horarioService.crear(nuevo, 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Ya existe un horario en el aula aula 1 que se cruza con SEC-2.");

        verify(horarioRepository, never()).save(nuevo);
    }

    @Test
    @DisplayName("crear: bloquea cruce de la misma sección")
    void crear_mismaSeccionSolapada_debeLanzarExcepcion() {
        Seccion seccion = seccion(1, "SEC-1");
        Horario nuevo = horario(null, seccion, "Aula 1");
        Horario cruce = horario(2, seccion, "Aula 2");

        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));
        when(horarioRepository.findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
                "Lunes", LocalTime.of(11, 0), LocalTime.of(9, 0))).thenReturn(List.of(cruce));

        assertThatThrownBy(() -> horarioService.crear(nuevo, 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("La sección ya tiene un horario que se cruza en ese rango.");

        verify(horarioRepository, never()).save(nuevo);
    }

    @Test
    @DisplayName("crear: bloquea cruce por docente compartido")
    void crear_docenteCompartido_debeLanzarExcepcion() {
        Seccion seccion = seccion(1, "SEC-1");
        Horario nuevo = horario(null, seccion, "Aula 1");
        Horario cruce = horario(2, seccion(2, "SEC-2"), "Aula 2");

        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));
        when(horarioRepository.findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
                "Lunes", LocalTime.of(11, 0), LocalTime.of(9, 0))).thenReturn(List.of(cruce));
        when(docenteSeccionRepository.findBySeccion_IdSeccion(1)).thenReturn(List.of(docenteSeccion(10, 1)));
        when(docenteSeccionRepository.findBySeccion_IdSeccion(2)).thenReturn(List.of(docenteSeccion(10, 2)));

        assertThatThrownBy(() -> horarioService.crear(nuevo, 1))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("El docente ya tiene una sección asignada en ese horario: SEC-2.");

        verify(horarioRepository, never()).save(nuevo);
    }

    @Test
    @DisplayName("actualizar: excluye el horario actual al validar cruces")
    void actualizar_excluyeHorarioActual_debeGuardar() {
        Seccion seccion = seccion(1, "SEC-1");
        Horario existente = horario(1, seccion, "Aula 1");
        Horario datos = horario(null, seccion, "Aula 1");

        when(horarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(seccionRepository.findById(1)).thenReturn(Optional.of(seccion));
        when(horarioRepository.findByDiaSemanaAndHoraInicioBeforeAndHoraFinAfter(
                "Lunes", LocalTime.of(11, 0), LocalTime.of(9, 0))).thenReturn(List.of(existente));
        when(docenteSeccionRepository.findBySeccion_IdSeccion(1)).thenReturn(List.of(docenteSeccion(10, 1)));
        when(horarioRepository.save(existente)).thenReturn(existente);

        Horario resultado = horarioService.actualizar(1, datos, 1);

        assertThat(resultado).isSameAs(existente);
        verify(horarioRepository).save(existente);
    }

    private Horario horario(Integer id, Seccion seccion, String aula) {
        Horario horario = new Horario();
        ReflectionTestUtils.setField(horario, "idHorario", id);
        ReflectionTestUtils.setField(horario, "diaSemana", "Lunes");
        ReflectionTestUtils.setField(horario, "horaInicio", LocalTime.of(9, 0));
        ReflectionTestUtils.setField(horario, "horaFin", LocalTime.of(11, 0));
        ReflectionTestUtils.setField(horario, "aula", aula);
        ReflectionTestUtils.setField(horario, "seccion", seccion);
        return horario;
    }

    private Seccion seccion(Integer id, String codigo) {
        Seccion seccion = new Seccion();
        ReflectionTestUtils.setField(seccion, "idSeccion", id);
        ReflectionTestUtils.setField(seccion, "codigoSeccion", codigo);
        return seccion;
    }

    private DocenteSeccion docenteSeccion(Integer idDocente, Integer idSeccion) {
        DocenteSeccion docenteSeccion = new DocenteSeccion();
        DocenteSeccion.DocenteSeccionId id = new DocenteSeccion.DocenteSeccionId();
        ReflectionTestUtils.setField(id, "idDocente", idDocente);
        ReflectionTestUtils.setField(id, "idSeccion", idSeccion);
        ReflectionTestUtils.setField(docenteSeccion, "id", id);
        return docenteSeccion;
    }

    private Authentication auth(Integer userId, String role) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);
        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority(role)));
    }
}
