package com.example.gestionacademica.historial.service;

import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.carrera;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.ciclo;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.curso;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.estudiante;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.evaluacion;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.historial;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.malla;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.matricula;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.nota;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.prerrequisito;
import static com.example.gestionacademica.historial.support.HistorialProgresoTestData.seccion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.exceptions.EstudianteNotFoundException;
import com.example.gestionacademica.historial.domain.EstadoCursoProgreso;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.domain.MallaCurricular;
import com.example.gestionacademica.historial.domain.Prerrequisito;
import com.example.gestionacademica.historial.dto.CursoProgresoDto;
import com.example.gestionacademica.historial.dto.HistorialProgresoResponseDto;
import com.example.gestionacademica.historial.repository.HistorialAcademicoRepository;
import com.example.gestionacademica.historial.repository.MallaCurricularRepository;
import com.example.gestionacademica.historial.repository.PrerrequisitoRepository;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.notas.repository.NotaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - HistorialProgresoService")
class HistorialProgresoServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private MallaCurricularRepository mallaCurricularRepository;

    @Mock
    private PrerrequisitoRepository prerrequisitoRepository;

    @Mock
    private HistorialAcademicoRepository historialAcademicoRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private NotaRepository notaRepository;

    @InjectMocks
    private HistorialProgresoService service;

    @Test
    @DisplayName("calcularProgreso: calcula nota final ponderada 14*40% + 16*60% = 15.20")
    void calcularProgreso_conNotasPonderadas_debeRetornarNotaFinalConDosDecimales() {
        Fixture fixture = baseFixture();
        HistorialAcademico intento = historial(1, fixture.estudiante(), fixture.seccionA());
        Evaluacion parcial = evaluacion(1, fixture.seccionA(), "40.00");
        Evaluacion finalExam = evaluacion(2, fixture.seccionA(), "60.00");
        Nota notaParcial = nota(1, fixture.estudiante(), parcial, "14.00");
        Nota notaFinal = nota(2, fixture.estudiante(), finalExam, "16.00");
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(intento), List.of(), List.of(notaParcial, notaFinal));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.PASSED);
        assertThat(curso.getNotaFinal()).isEqualByComparingTo(new BigDecimal("15.20"));
        assertThat(response.getResumen().getPromedioPonderado()).isEqualByComparingTo(new BigDecimal("15.20"));
    }

    @Test
    @DisplayName("calcularProgreso: nota 11.99 no aprueba")
    void calcularProgreso_conNotaOnceNoventaYNueve_debeMarcarCursoDesaprobado() {
        Fixture fixture = baseFixture();
        HistorialAcademico intento = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota nota = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "11.99");
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(intento), List.of(), List.of(nota));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.FAILED);
        assertThat(curso.getNotaFinal()).isEqualByComparingTo(new BigDecimal("11.99"));
        assertThat(response.getResumen().getCreditosAprobados()).isZero();
    }

    @Test
    @DisplayName("calcularProgreso: el último intento gana sobre un intento anterior")
    void calcularProgreso_conIntentosMultiples_debeUsarUltimoIntentoPorCurso() {
        Fixture fixture = baseFixture();
        CicloAcademico cicloAnterior = ciclo(1, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 15));
        Seccion seccionAnterior = seccion(10, fixture.cursoA(), cicloAnterior);
        HistorialAcademico intentoAnterior = historial(1, fixture.estudiante(), seccionAnterior);
        HistorialAcademico intentoReciente = historial(2, fixture.estudiante(), fixture.seccionA());
        Nota notaAnterior = nota(1, fixture.estudiante(), evaluacion(1, seccionAnterior, "100.00"), "10.00");
        Nota notaReciente = nota(2, fixture.estudiante(), evaluacion(2, fixture.seccionA(), "100.00"), "15.00");
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(intentoAnterior, intentoReciente), List.of(), List.of(notaAnterior, notaReciente));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.PASSED);
        assertThat(curso.getNotaFinal()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("calcularProgreso: un aprobado anterior con último intento desaprobado no aprueba")
    void calcularProgreso_conAprobadoAnteriorYUltimoDesaprobado_debeNoAprobar() {
        Fixture fixture = baseFixture();
        CicloAcademico cicloAnterior = ciclo(1, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 15));
        Seccion seccionAnterior = seccion(10, fixture.cursoA(), cicloAnterior);
        HistorialAcademico intentoAnterior = historial(1, fixture.estudiante(), seccionAnterior);
        HistorialAcademico intentoReciente = historial(2, fixture.estudiante(), fixture.seccionA());
        Nota notaAnterior = nota(1, fixture.estudiante(), evaluacion(1, seccionAnterior, "100.00"), "15.00");
        Nota notaReciente = nota(2, fixture.estudiante(), evaluacion(2, fixture.seccionA(), "100.00"), "11.00");
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(intentoAnterior, intentoReciente), List.of(), List.of(notaAnterior, notaReciente));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.FAILED);
        assertThat(curso.getNotaFinal()).isEqualByComparingTo(new BigDecimal("11.00"));
        assertThat(response.getResumen().getCreditosAprobados()).isZero();
    }

    @Test
    @DisplayName("calcularProgreso: matrícula activa marca el curso como en progreso")
    void calcularProgreso_conMatriculaActiva_debeMarcarCursoEnProgreso() {
        Fixture fixture = baseFixture();
        Matricula activa = matricula(1, fixture.estudiante(), fixture.seccionA());
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(), List.of(activa), List.of());

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(course(response, fixture.cursoA().getIdCurso()).getEstado()).isEqualTo(EstadoCursoProgreso.IN_PROGRESS);
    }

    @Test
    @DisplayName("calcularProgreso: prerrequisito no cumplido bloquea el curso")
    void calcularProgreso_conPrerrequisitoNoCumplido_debeMarcarPendienteBloqueado() {
        Fixture fixture = baseFixture();
        Prerrequisito prerequisito = prerrequisito(fixture.carrera(), fixture.cursoB(), fixture.cursoA());
        stubProgress(fixture, List.of(fixture.mallaA(), fixture.mallaB()), List.of(prerequisito), List.of(), List.of(), List.of());

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto cursoB = course(response, fixture.cursoB().getIdCurso());
        assertThat(cursoB.getEstado()).isEqualTo(EstadoCursoProgreso.PENDING_BLOCKED);
        assertThat(cursoB.getPrerrequisitos()).singleElement().satisfies(pre -> {
            assertThat(pre.getCursoId()).isEqualTo(fixture.cursoA().getIdCurso());
            assertThat(pre.getCumplido()).isFalse();
        });
    }

    @Test
    @DisplayName("calcularProgreso: prerrequisito cumplido deja el curso pendiente disponible")
    void calcularProgreso_conPrerrequisitoCumplidoYSinIntentos_debeMarcarPendienteDisponible() {
        Fixture fixture = baseFixture();
        Prerrequisito prerequisito = prerrequisito(fixture.carrera(), fixture.cursoB(), fixture.cursoA());
        HistorialAcademico intentoA = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota notaA = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "13.00");
        stubProgress(fixture, List.of(fixture.mallaA(), fixture.mallaB()), List.of(prerequisito), List.of(intentoA), List.of(), List.of(notaA));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto cursoB = course(response, fixture.cursoB().getIdCurso());
        assertThat(cursoB.getEstado()).isEqualTo(EstadoCursoProgreso.PENDING_AVAILABLE);
        assertThat(cursoB.getPrerrequisitos()).singleElement().extracting("cumplido").isEqualTo(true);
    }

    @Test
    @DisplayName("calcularProgreso: promedio ponderado se calcula por créditos")
    void calcularProgreso_conCursosCompletados_debeCalcularPromedioPonderadoPorCreditos() {
        Fixture fixture = baseFixture();
        MallaCurricular mallaA = malla(fixture.carrera(), fixture.cursoA(), 1, true, 3);
        MallaCurricular mallaB = malla(fixture.carrera(), fixture.cursoB(), 2, true, 5);
        HistorialAcademico intentoA = historial(1, fixture.estudiante(), fixture.seccionA());
        HistorialAcademico intentoB = historial(2, fixture.estudiante(), fixture.seccionB());
        Nota notaA = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "15.00");
        Nota notaB = nota(2, fixture.estudiante(), evaluacion(2, fixture.seccionB(), "100.00"), "12.00");
        stubProgress(fixture, List.of(mallaA, mallaB), List.of(), List.of(intentoA, intentoB), List.of(), List.of(notaA, notaB));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(response.getResumen().getPromedioPonderado()).isEqualByComparingTo(new BigDecimal("13.13"));
    }

    @Test
    @DisplayName("calcularProgreso: calcula créditos aprobados, restantes y porcentaje de avance")
    void calcularProgreso_conCursoAprobado_debeCalcularCreditosYPorcentaje() {
        Fixture fixture = baseFixture();
        MallaCurricular mallaA = malla(fixture.carrera(), fixture.cursoA(), 1, true, 4);
        MallaCurricular mallaB = malla(fixture.carrera(), fixture.cursoB(), 2, true, 6);
        HistorialAcademico intentoA = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota notaA = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "14.00");
        stubProgress(fixture, List.of(mallaA, mallaB), List.of(), List.of(intentoA), List.of(), List.of(notaA));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(response.getResumen().getCreditosAprobados()).isEqualTo(4);
        assertThat(response.getResumen().getCreditosRestantes()).isEqualTo(6);
        assertThat(response.getResumen().getPorcentajeAvance()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    @DisplayName("calcularProgreso: electivo aprobado cuenta créditos")
    void calcularProgreso_conElectivoAprobado_debeContarCreditosAprobados() {
        Fixture fixture = baseFixture();
        MallaCurricular electivo = malla(fixture.carrera(), fixture.cursoA(), 1, false, 4);
        HistorialAcademico intento = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota nota = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "18.00");
        stubProgress(fixture, List.of(electivo), List.of(), List.of(intento), List.of(), List.of(nota));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getObligatorio()).isFalse();
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.PASSED);
        assertThat(response.getResumen().getCreditosAprobados()).isEqualTo(4);
    }

    @Test
    @DisplayName("calcularProgreso: estudiante inexistente lanza EstudianteNotFoundException")
    void calcularProgreso_conEstudianteInexistente_debeLanzarExcepcion() {
        when(estudianteRepository.findByIdWithUsuarioAndCarrera(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calcularProgreso(999))
            .isInstanceOf(EstudianteNotFoundException.class)
            .hasMessageContaining("999");

        verify(mallaCurricularRepository, never()).findByCarreraIdWithCurso(1);
    }

    @Test
    @DisplayName("calcularProgreso: malla vacía retorna resumen en cero")
    void calcularProgreso_conMallaVacia_debeRetornarResumenCero() {
        Fixture fixture = baseFixture();
        stubProgress(fixture, List.of(), List.of(), List.of(), List.of(), List.of());

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(response.getCursos()).isEmpty();
        assertThat(response.getCarrera().getCreditosTotales()).isZero();
        assertThat(response.getResumen().getTotalCursos()).isZero();
        assertThat(response.getResumen().getCreditosRestantes()).isZero();
        assertThat(response.getResumen().getPromedioPonderado()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(response.getResumen().getPorcentajeAvance()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("calcularProgreso: curso activo ya aprobado conserva estado PASSED")
    void calcularProgreso_conMatriculaActivaYCursoAprobado_debePriorizarAprobado() {
        Fixture fixture = baseFixture();
        HistorialAcademico intento = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota nota = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "12.00");
        Matricula activa = matricula(1, fixture.estudiante(), fixture.seccionA());
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(intento), List.of(activa), List.of(nota));

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(course(response, fixture.cursoA().getIdCurso()).getEstado()).isEqualTo(EstadoCursoProgreso.PASSED);
    }

    @Test
    @DisplayName("calcularProgreso: prerrequisitos DTO indican cumplido true y false")
    void calcularProgreso_conPrerrequisitosMixtos_debeMapearCumplidoTrueYFalse() {
        Fixture fixture = baseFixture();
        Curso cursoC = curso(103, "MAT201", "Matemática III", 4);
        MallaCurricular mallaC = malla(fixture.carrera(), cursoC, 3, true, 4);
        Prerrequisito prereqCumplido = prerrequisito(fixture.carrera(), cursoC, fixture.cursoA());
        Prerrequisito prereqNoCumplido = prerrequisito(fixture.carrera(), cursoC, fixture.cursoB());
        HistorialAcademico intentoA = historial(1, fixture.estudiante(), fixture.seccionA());
        Nota notaA = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "13.00");
        stubProgress(
            fixture,
            List.of(fixture.mallaA(), fixture.mallaB(), mallaC),
            List.of(prereqCumplido, prereqNoCumplido),
            List.of(intentoA),
            List.of(),
            List.of(notaA)
        );

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, cursoC.getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.PENDING_BLOCKED);
        assertThat(curso.getPrerrequisitos())
            .extracting("cursoId", "cumplido")
            .containsExactlyInAnyOrder(
                org.assertj.core.groups.Tuple.tuple(fixture.cursoA().getIdCurso(), true),
                org.assertj.core.groups.Tuple.tuple(fixture.cursoB().getIdCurso(), false)
            );
    }

    @Test
    @DisplayName("calcularProgreso: curso sin intentos previos queda pendiente disponible")
    void calcularProgreso_sinIntentosPrevios_debeMarcarPendienteDisponible() {
        Fixture fixture = baseFixture();
        stubProgress(fixture, List.of(fixture.mallaA()), List.of(), List.of(), List.of(), List.of());

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto curso = course(response, fixture.cursoA().getIdCurso());
        assertThat(curso.getEstado()).isEqualTo(EstadoCursoProgreso.PENDING_AVAILABLE);
        assertThat(curso.getNotaFinal()).isNull();
    }

    @Test
    @DisplayName("calcularProgreso: curso fallado con prerrequisitos cumplidos queda FAILED")
    void calcularProgreso_conCursoFalladoYPrerrequisitosOk_debeMarcarFailed() {
        Fixture fixture = baseFixture();
        Prerrequisito prerequisito = prerrequisito(fixture.carrera(), fixture.cursoB(), fixture.cursoA());
        HistorialAcademico intentoA = historial(1, fixture.estudiante(), fixture.seccionA());
        HistorialAcademico intentoB = historial(2, fixture.estudiante(), fixture.seccionB());
        Nota notaA = nota(1, fixture.estudiante(), evaluacion(1, fixture.seccionA(), "100.00"), "14.00");
        Nota notaB = nota(2, fixture.estudiante(), evaluacion(2, fixture.seccionB(), "100.00"), "11.50");
        stubProgress(
            fixture,
            List.of(fixture.mallaA(), fixture.mallaB()),
            List.of(prerequisito),
            List.of(intentoA, intentoB),
            List.of(),
            List.of(notaA, notaB)
        );

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        CursoProgresoDto cursoB = course(response, fixture.cursoB().getIdCurso());
        assertThat(cursoB.getEstado()).isEqualTo(EstadoCursoProgreso.FAILED);
        assertThat(cursoB.getPrerrequisitos()).singleElement().extracting("cumplido").isEqualTo(true);
    }

    private Fixture baseFixture() {
        Carrera carrera = carrera(1, "Ingeniería de Sistemas");
        Estudiante estudiante = estudiante(10, "20240010", carrera);
        Curso cursoA = curso(101, "MAT101", "Matemática I", 4);
        Curso cursoB = curso(102, "MAT102", "Matemática II", 4);
        CicloAcademico cicloActual = ciclo(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 7, 15));
        Seccion seccionA = seccion(20, cursoA, cicloActual);
        Seccion seccionB = seccion(21, cursoB, cicloActual);
        return new Fixture(
            carrera,
            estudiante,
            cursoA,
            cursoB,
            seccionA,
            seccionB,
            malla(carrera, cursoA, 1, true, 4),
            malla(carrera, cursoB, 2, true, 4)
        );
    }

    private void stubProgress(
            Fixture fixture,
            List<MallaCurricular> malla,
            List<Prerrequisito> prerequisitos,
            List<HistorialAcademico> historial,
            List<Matricula> matriculasActivas,
            List<Nota> notas) {
        Integer estudianteId = fixture.estudiante().getIdUsuario();
        Integer carreraId = fixture.carrera().getIdCarrera();
        when(estudianteRepository.findByIdWithUsuarioAndCarrera(estudianteId)).thenReturn(Optional.of(fixture.estudiante()));
        when(mallaCurricularRepository.findByCarreraIdWithCurso(carreraId)).thenReturn(malla);
        when(prerrequisitoRepository.findByCarreraIdWithCursos(carreraId)).thenReturn(prerequisitos);
        when(historialAcademicoRepository.findByEstudianteIdWithSeccionCursoCiclo(estudianteId)).thenReturn(historial);
        when(matriculaRepository.findActiveByEstudianteIdWithSeccionCurso(estudianteId)).thenReturn(matriculasActivas);
        if (!historial.isEmpty()) {
            when(notaRepository.findByEstudianteIdAndSeccionIdsWithEvaluacion(
                org.mockito.ArgumentMatchers.eq(estudianteId),
                org.mockito.ArgumentMatchers.anyCollection()
            )).thenReturn(notas);
        }
    }

    private CursoProgresoDto course(HistorialProgresoResponseDto response, Integer cursoId) {
        return response.getCursos().stream()
            .filter(curso -> cursoId.equals(curso.getCursoId()))
            .findFirst()
            .orElseThrow();
    }

    private record Fixture(
        Carrera carrera,
        Estudiante estudiante,
        Curso cursoA,
        Curso cursoB,
        Seccion seccionA,
        Seccion seccionB,
        MallaCurricular mallaA,
        MallaCurricular mallaB
    ) {
    }
}
