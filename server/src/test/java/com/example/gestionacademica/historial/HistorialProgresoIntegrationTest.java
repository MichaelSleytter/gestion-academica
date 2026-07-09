package com.example.gestionacademica.historial;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.historial.domain.EstadoCursoProgreso;
import com.example.gestionacademica.historial.dto.CursoProgresoDto;
import com.example.gestionacademica.historial.dto.HistorialProgresoResponseDto;
import com.example.gestionacademica.historial.service.HistorialProgresoService;
import com.example.gestionacademica.historial.support.HistorialProgresoIntegrationData;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import(HistorialProgresoService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("HistorialProgreso integration smoke")
class HistorialProgresoIntegrationTest extends HistorialProgresoIntegrationData {

    @Autowired
    private HistorialProgresoService service;

    @Test
    @DisplayName("calcularProgreso persists the full academic flow and returns calculated progress")
    void calcularProgreso_conDatosPersistidos_debeRetornarProgresoAcademicoCompleto() {
        IntegrationFixture fixture = persistFullProgressFixture();

        HistorialProgresoResponseDto response = service.calcularProgreso(fixture.estudiante().getIdUsuario());

        assertThat(response.getEstudiante().getId()).isEqualTo(fixture.estudiante().getIdUsuario());
        assertThat(response.getEstudiante().getCodigo()).isEqualTo(fixture.estudiante().getCodigoEstudiante());
        assertThat(response.getEstudiante().getNombres()).isEqualTo("Ana");
        assertThat(response.getEstudiante().getApellidos()).isEqualTo("Torres");
        assertThat(response.getCarrera().getId()).isEqualTo(fixture.carrera().getIdCarrera());
        assertThat(response.getCarrera().getNombre()).startsWith("Ingeniería de Sistemas");
        assertThat(response.getCarrera().getCreditosTotales()).isEqualTo(19);
        assertThat(response.getCursos()).hasSize(5);

        assertThat(response.getResumen().getTotalCursos()).isEqualTo(5);
        assertThat(response.getResumen().getCursosAprobados()).isEqualTo(2);
        assertThat(response.getResumen().getCursosEnProgreso()).isEqualTo(1);
        assertThat(response.getResumen().getCursosPendientes()).isEqualTo(2);
        assertThat(response.getResumen().getCreditosAprobados()).isEqualTo(7);
        assertThat(response.getResumen().getCreditosRestantes()).isEqualTo(12);
        assertThat(response.getResumen().getPromedioPonderado()).isEqualByComparingTo(new BigDecimal("13.83"));
        assertThat(response.getResumen().getPorcentajeAvance()).isEqualByComparingTo(new BigDecimal("36.84"));

        Map<String, CursoProgresoDto> cursos = response.getCursos().stream()
            .collect(Collectors.toMap(CursoProgresoDto::getCodigo, Function.identity()));

        assertCourse(cursos, fixture.matematicaI(), EstadoCursoProgreso.PASSED, "15.20");
        assertCourse(cursos, fixture.comunicacion(), EstadoCursoProgreso.PASSED, "12.00");
        assertCourse(cursos, fixture.matematicaII(), EstadoCursoProgreso.IN_PROGRESS, null);
        assertCourse(cursos, fixture.algoritmos(), EstadoCursoProgreso.PENDING_BLOCKED, null);
        assertCourse(cursos, fixture.introduccion(), EstadoCursoProgreso.PENDING_AVAILABLE, null);

        assertThat(cursos.get(fixture.matematicaII().getCodigo()).getPrerrequisitos())
            .singleElement()
            .satisfies(prerrequisito -> {
                assertThat(prerrequisito.getCursoId()).isEqualTo(fixture.matematicaI().getIdCurso());
                assertThat(prerrequisito.getCodigo()).isEqualTo(fixture.matematicaI().getCodigo());
                assertThat(prerrequisito.getCumplido()).isTrue();
            });
        assertThat(cursos.get(fixture.algoritmos().getCodigo()).getPrerrequisitos())
            .singleElement()
            .satisfies(prerrequisito -> {
                assertThat(prerrequisito.getCursoId()).isEqualTo(fixture.matematicaII().getIdCurso());
                assertThat(prerrequisito.getCodigo()).isEqualTo(fixture.matematicaII().getCodigo());
                assertThat(prerrequisito.getCumplido()).isFalse();
            });
    }

    private void assertCourse(
            Map<String, CursoProgresoDto> cursos,
            Curso curso,
            EstadoCursoProgreso estado,
            String notaFinal) {
        assertThat(cursos).containsKey(curso.getCodigo());
        CursoProgresoDto dto = cursos.get(curso.getCodigo());
        assertThat(dto.getCursoId()).isEqualTo(curso.getIdCurso());
        assertThat(dto.getNombre()).isEqualTo(curso.getNombre());
        assertThat(dto.getEstado()).isEqualTo(estado);
        if (notaFinal == null) {
            assertThat(dto.getNotaFinal()).isNull();
        } else {
            assertThat(dto.getNotaFinal()).isEqualByComparingTo(new BigDecimal(notaFinal));
        }
    }
}
