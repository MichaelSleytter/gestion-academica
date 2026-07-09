package com.example.gestionacademica.historial.service;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.exceptions.EstudianteNotFoundException;
import com.example.gestionacademica.historial.domain.EstadoCursoProgreso;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.domain.MallaCurricular;
import com.example.gestionacademica.historial.domain.Prerrequisito;
import com.example.gestionacademica.historial.domain.TipoReglaPrerrequisito;
import com.example.gestionacademica.historial.dto.CarreraResumenDto;
import com.example.gestionacademica.historial.dto.CursoProgresoDto;
import com.example.gestionacademica.historial.dto.EstudianteResumenDto;
import com.example.gestionacademica.historial.dto.HistorialProgresoResponseDto;
import com.example.gestionacademica.historial.dto.PrerrequisitoProgresoDto;
import com.example.gestionacademica.historial.dto.ProgresoResumenDto;
import com.example.gestionacademica.historial.repository.HistorialAcademicoRepository;
import com.example.gestionacademica.historial.repository.MallaCurricularRepository;
import com.example.gestionacademica.historial.repository.PrerrequisitoRepository;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.notas.repository.NotaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialProgresoService {

    private static final BigDecimal NOTA_APROBATORIA = new BigDecimal("12.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private static final int SCALE_INTERMEDIATE = 4;
    private static final int SCALE_RESPONSE = 2;

    private final EstudianteRepository estudianteRepository;
    private final MallaCurricularRepository mallaCurricularRepository;
    private final PrerrequisitoRepository prerrequisitoRepository;
    private final HistorialAcademicoRepository historialAcademicoRepository;
    private final MatriculaRepository matriculaRepository;
    private final NotaRepository notaRepository;

    public HistorialProgresoResponseDto calcularProgreso(Integer estudianteId) {
        Estudiante estudiante = estudianteRepository.findByIdWithUsuarioAndCarrera(estudianteId)
            .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        Carrera carrera = estudiante.getCarrera();
        Integer carreraId = carrera.getIdCarrera();
        List<MallaCurricular> malla = mallaCurricularRepository.findByCarreraIdWithCurso(carreraId);
        List<Prerrequisito> prerrequisitos = prerrequisitoRepository.findByCarreraIdWithCursos(carreraId);
        List<HistorialAcademico> historial = historialAcademicoRepository.findByEstudianteIdWithSeccionCursoCiclo(estudianteId);
        List<Matricula> matriculasActivas = matriculaRepository.findActiveByEstudianteIdWithSeccionCurso(estudianteId);

        Map<Integer, List<Nota>> notasPorSeccion = cargarNotasPorSeccion(estudianteId, historial);
        Map<Integer, Attempt> ultimoIntentoPorCurso = seleccionarUltimosIntentos(historial, notasPorSeccion);
        Set<Integer> cursosActivos = obtenerCursosActivos(matriculasActivas);
        Set<Integer> cursosAprobados = obtenerCursosAprobados(ultimoIntentoPorCurso);
        Map<Integer, List<Prerrequisito>> prerrequisitosPorCurso = prerrequisitos.stream()
            .collect(Collectors.groupingBy(prerrequisito -> prerrequisito.getCurso().getIdCurso()));

        List<CursoProgresoDto> cursos = malla.stream()
            .map(entry -> mapCurso(entry, ultimoIntentoPorCurso, cursosActivos, cursosAprobados, prerrequisitosPorCurso))
            .toList();
        Integer creditosTotales = malla.stream().mapToInt(this::creditos).sum();
        ProgresoResumenDto resumen = calcularResumen(cursos, malla, ultimoIntentoPorCurso, creditosTotales);

        return HistorialProgresoResponseDto.builder()
            .estudiante(mapEstudiante(estudiante))
            .carrera(mapCarrera(carrera, creditosTotales))
            .resumen(resumen)
            .cursos(cursos)
            .build();
    }

    private Set<Integer> obtenerCursosActivos(List<Matricula> matriculasActivas) {
        return matriculasActivas.stream()
            .map(Matricula::getSeccion)
            .filter(Objects::nonNull)
            .map(Seccion::getCurso)
            .filter(Objects::nonNull)
            .map(Curso::getIdCurso)
            .collect(Collectors.toSet());
    }

    private Set<Integer> obtenerCursosAprobados(Map<Integer, Attempt> ultimoIntentoPorCurso) {
        return ultimoIntentoPorCurso.entrySet().stream()
            .filter(entry -> esAprobado(entry.getValue().notaFinal()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private Map<Integer, List<Nota>> cargarNotasPorSeccion(Integer estudianteId, List<HistorialAcademico> historial) {
        Set<Integer> seccionIds = historial.stream()
            .map(HistorialAcademico::getSeccion)
            .filter(Objects::nonNull)
            .map(Seccion::getIdSeccion)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (seccionIds.isEmpty()) {
            return Map.of();
        }

        return notaRepository.findByEstudianteIdAndSeccionIdsWithEvaluacion(estudianteId, seccionIds).stream()
            .filter(nota -> nota.getEvaluacion() != null && nota.getEvaluacion().getSeccion() != null)
            .collect(Collectors.groupingBy(nota -> nota.getEvaluacion().getSeccion().getIdSeccion()));
    }

    private Map<Integer, Attempt> seleccionarUltimosIntentos(
            List<HistorialAcademico> historial,
            Map<Integer, List<Nota>> notasPorSeccion) {
        return historial.stream()
            .map(entry -> crearIntento(entry, notasPorSeccion))
            .flatMap(Optional::stream)
            .collect(Collectors.groupingBy(
                Attempt::cursoId,
                Collectors.collectingAndThen(
                    Collectors.maxBy(ATTEMPT_COMPARATOR),
                    Optional::orElseThrow
                )
            ));
    }

    private Optional<Attempt> crearIntento(HistorialAcademico historial, Map<Integer, List<Nota>> notasPorSeccion) {
        if (historial.getSeccion() == null || historial.getSeccion().getCurso() == null) {
            return Optional.empty();
        }
        Integer seccionId = historial.getSeccion().getIdSeccion();
        Optional<BigDecimal> notaFinal = calcularNotaFinal(notasPorSeccion.getOrDefault(seccionId, List.of()));
        return notaFinal.map(finalGrade -> new Attempt(historial, finalGrade));
    }

    private Optional<BigDecimal> calcularNotaFinal(List<Nota> notas) {
        if (notas == null || notas.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal suma = notas.stream()
            .map(this::calcularAporteNota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Optional.of(suma.setScale(SCALE_INTERMEDIATE, RoundingMode.HALF_UP));
    }

    private BigDecimal calcularAporteNota(Nota nota) {
        BigDecimal valor = nota.getNota() == null ? BigDecimal.ZERO : nota.getNota();
        BigDecimal porcentaje = nota.getEvaluacion().getPorcentaje() == null
            ? BigDecimal.ZERO
            : nota.getEvaluacion().getPorcentaje();
        return valor.multiply(porcentaje).divide(ONE_HUNDRED, SCALE_INTERMEDIATE, RoundingMode.HALF_UP);
    }

    private CursoProgresoDto mapCurso(
            MallaCurricular entry,
            Map<Integer, Attempt> ultimoIntentoPorCurso,
            Set<Integer> cursosActivos,
            Set<Integer> cursosAprobados,
            Map<Integer, List<Prerrequisito>> prerrequisitosPorCurso) {
        Curso curso = entry.getCurso();
        Integer cursoId = curso.getIdCurso();
        Attempt intento = ultimoIntentoPorCurso.get(cursoId);
        List<PrerrequisitoProgresoDto> prerrequisitos = mapPrerrequisitos(
            prerrequisitosPorCurso.getOrDefault(cursoId, List.of()),
            cursosAprobados
        );
        boolean bloqueado = tienePrerrequisitoHardNoCumplido(prerrequisitos);
        EstadoCursoProgreso estado = clasificarEstado(intento, cursosActivos.contains(cursoId), bloqueado);

        return CursoProgresoDto.builder()
            .cursoId(cursoId)
            .codigo(curso.getCodigo())
            .nombre(curso.getNombre())
            .cicloRecomendado(entry.getCicloRecomendado())
            .obligatorio(entry.getObligatorio())
            .creditos(creditos(entry))
            .estado(estado)
            .notaFinal(intento == null ? null : toResponseScale(intento.notaFinal()))
            .prerrequisitos(prerrequisitos)
            .build();
    }

    private List<PrerrequisitoProgresoDto> mapPrerrequisitos(
            Collection<Prerrequisito> prerrequisitos,
            Set<Integer> cursosAprobados) {
        return prerrequisitos.stream()
            .map(prerrequisito -> {
                Curso curso = prerrequisito.getCursoPrerrequisito();
                return PrerrequisitoProgresoDto.builder()
                    .cursoId(curso.getIdCurso())
                    .codigo(curso.getCodigo())
                    .nombre(curso.getNombre())
                    .tipoRegla(prerrequisito.getTipoRegla())
                    .cumplido(cursosAprobados.contains(curso.getIdCurso()))
                    .build();
            })
            .toList();
    }

    private boolean tienePrerrequisitoHardNoCumplido(List<PrerrequisitoProgresoDto> prerrequisitos) {
        return prerrequisitos.stream()
            .anyMatch(prerrequisito -> TipoReglaPrerrequisito.HARD.equals(prerrequisito.getTipoRegla())
                && Boolean.FALSE.equals(prerrequisito.getCumplido()));
    }

    private EstadoCursoProgreso clasificarEstado(Attempt intento, boolean activo, boolean bloqueado) {
        if (intento != null && esAprobado(intento.notaFinal())) {
            return EstadoCursoProgreso.PASSED;
        }
        if (activo) {
            return EstadoCursoProgreso.IN_PROGRESS;
        }
        if (bloqueado) {
            return EstadoCursoProgreso.PENDING_BLOCKED;
        }
        if (intento != null) {
            return EstadoCursoProgreso.FAILED;
        }
        return EstadoCursoProgreso.PENDING_AVAILABLE;
    }

    private ProgresoResumenDto calcularResumen(
            List<CursoProgresoDto> cursos,
            List<MallaCurricular> malla,
            Map<Integer, Attempt> ultimoIntentoPorCurso,
            Integer creditosTotales) {
        Map<Integer, MallaCurricular> mallaPorCurso = malla.stream()
            .collect(Collectors.toMap(entry -> entry.getCurso().getIdCurso(), Function.identity(), (left, right) -> left));
        int cursosAprobados = (int) cursos.stream().filter(curso -> curso.getEstado() == EstadoCursoProgreso.PASSED).count();
        int cursosEnProgreso = (int) cursos.stream().filter(curso -> curso.getEstado() == EstadoCursoProgreso.IN_PROGRESS).count();
        int creditosAprobados = cursos.stream()
            .filter(curso -> curso.getEstado() == EstadoCursoProgreso.PASSED)
            .mapToInt(CursoProgresoDto::getCreditos)
            .sum();
        int creditosRestantes = Math.max(creditosTotales - creditosAprobados, 0);

        BigDecimal promedioPonderado = calcularPromedioPonderado(mallaPorCurso, ultimoIntentoPorCurso);
        BigDecimal porcentajeAvance = creditosTotales == 0
            ? BigDecimal.ZERO.setScale(SCALE_RESPONSE, RoundingMode.HALF_UP)
            : BigDecimal.valueOf(creditosAprobados)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(creditosTotales), SCALE_RESPONSE, RoundingMode.HALF_UP);

        return ProgresoResumenDto.builder()
            .totalCursos(cursos.size())
            .cursosAprobados(cursosAprobados)
            .cursosEnProgreso(cursosEnProgreso)
            .cursosPendientes(cursos.size() - cursosAprobados - cursosEnProgreso)
            .creditosAprobados(creditosAprobados)
            .creditosRestantes(creditosRestantes)
            .promedioPonderado(promedioPonderado)
            .porcentajeAvance(porcentajeAvance)
            .build();
    }

    private BigDecimal calcularPromedioPonderado(
            Map<Integer, MallaCurricular> mallaPorCurso,
            Map<Integer, Attempt> ultimoIntentoPorCurso) {
        BigDecimal numerador = BigDecimal.ZERO;
        int denominadorCreditos = 0;

        for (Map.Entry<Integer, Attempt> entry : ultimoIntentoPorCurso.entrySet()) {
            MallaCurricular malla = mallaPorCurso.get(entry.getKey());
            if (malla == null) {
                continue;
            }
            int creditos = creditos(malla);
            numerador = numerador.add(entry.getValue().notaFinal().multiply(BigDecimal.valueOf(creditos)));
            denominadorCreditos += creditos;
        }

        if (denominadorCreditos == 0) {
            return BigDecimal.ZERO.setScale(SCALE_RESPONSE, RoundingMode.HALF_UP);
        }
        return numerador.divide(BigDecimal.valueOf(denominadorCreditos), SCALE_RESPONSE, RoundingMode.HALF_UP);
    }

    private EstudianteResumenDto mapEstudiante(Estudiante estudiante) {
        return EstudianteResumenDto.builder()
            .id(estudiante.getIdUsuario())
            .codigo(estudiante.getCodigoEstudiante())
            .nombres(estudiante.getUsuario().getNombre())
            .apellidos(estudiante.getUsuario().getApellido())
            .build();
    }

    private CarreraResumenDto mapCarrera(Carrera carrera, Integer creditosTotales) {
        return CarreraResumenDto.builder()
            .id(carrera.getIdCarrera())
            .nombre(carrera.getNombre())
            .creditosTotales(creditosTotales)
            .build();
    }

    private boolean esAprobado(BigDecimal notaFinal) {
        return notaFinal.compareTo(NOTA_APROBATORIA) >= 0;
    }

    private int creditos(MallaCurricular entry) {
        return entry.getCreditos() == null ? 0 : entry.getCreditos();
    }

    private BigDecimal toResponseScale(BigDecimal value) {
        return value.setScale(SCALE_RESPONSE, RoundingMode.HALF_UP);
    }

    private static final Comparator<Attempt> ATTEMPT_COMPARATOR = Comparator
        .comparing(Attempt::fechaFin, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Attempt::fechaInicio, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Attempt::idSeccion, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Attempt::idHistorial, Comparator.nullsFirst(Comparator.naturalOrder()));

    private record Attempt(HistorialAcademico historial, BigDecimal notaFinal) {
        private Integer cursoId() {
            return historial.getSeccion().getCurso().getIdCurso();
        }

        private LocalDate fechaFin() {
            CicloAcademico ciclo = historial.getSeccion().getCicloAcademico();
            return ciclo == null ? null : ciclo.getFechaFin();
        }

        private LocalDate fechaInicio() {
            CicloAcademico ciclo = historial.getSeccion().getCicloAcademico();
            return ciclo == null ? null : ciclo.getFechaInicio();
        }

        private Integer idSeccion() {
            return historial.getSeccion().getIdSeccion();
        }

        private Integer idHistorial() {
            return historial.getIdHistorial();
        }
    }
}
