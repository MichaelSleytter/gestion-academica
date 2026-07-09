# Design — Historial Académico

## 1. Design Goals

Implement a backend-only, read-only academic progress API that calculates a student's progress against their career curriculum. The design is additive: existing `HistorialAcademico` CRUD endpoints remain compatible, while the new progress endpoints return DTOs and enforce ownership/role authorization.

Primary decisions:

- Use the existing backend module under `server/src/main/java/com/example/gestionacademica`.
- Keep the progress feature close to the existing `historial` module.
- Use `Integer` identifiers in Java because the current domain model uses `Integer` primary keys (`Estudiante.idUsuario`, `Curso.idCurso`, `Carrera.idCarrera`, etc.). JSON clients are unaffected.
- Calculate `creditosTotales` from `MallaCurricular.creditos`; do not persist a derived total on `Carrera` in this slice.
- Calculate final course grade from `Nota` + `Evaluacion.porcentaje` according to the accepted rule: `Σ(nota × porcentaje / 100)`.
- Use the latest completed attempt per course for pass/fail, credits, prerequisites, and GPA.
- Keep `Matricula.estado` and `HistorialAcademico.estado` as `String` for compatibility in this slice; add typed enums only for new progress/prerequisite concepts.

## 2. Package Structure

Use the existing screaming-by-feature style and extend `historial` for the progress read model.

```text
server/src/main/java/com/example/gestionacademica/
  historial/
    controller/
      HistorialAcademicoController.java          # existing CRUD, unchanged
      HistorialProgresoController.java           # new read-only progress endpoints
    domain/
      HistorialAcademico.java                    # existing
      MallaCurricular.java                       # new curriculum entry
      Prerrequisito.java                         # new prerequisite rule
      EstadoCursoProgreso.java                   # new enum
      TipoReglaPrerrequisito.java                # new enum
    dto/
      HistorialProgresoResponseDto.java
      EstudianteResumenDto.java
      CarreraResumenDto.java
      ProgresoResumenDto.java
      CursoProgresoDto.java
      PrerrequisitoProgresoDto.java
    repository/
      HistorialAcademicoRepository.java          # add fetch query
      MallaCurricularRepository.java             # new
      PrerrequisitoRepository.java               # new
    service/
      HistorialAcademicoService.java             # existing CRUD, unchanged
      HistorialProgresoService.java              # new progress calculator
      HistorialProgresoSecurity.java             # SpEL authorization helper
  catalogos/domain/
    Carrera.java                                 # no persistent creditosTotales column
  cursos/domain/
    Curso.java                                   # add optional codigo field for API contract
```

Repository additions are also required in existing modules:

```text
matriculas/repository/MatriculaRepository.java
notas/repository/NotaRepository.java
docentes/repository/DocenteSeccionRepository.java
estudiantes/repository/EstudianteRepository.java
exceptions/GlobalExceptionHandler.java
exceptions/EstudianteNotFoundException.java
```

## 3. JPA Entity Design

### 3.1 `MallaCurricular`

```java
@Entity
@Table(
    name = "malla_curricular",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_malla_curricular_carrera_curso",
        columnNames = {"id_carrera", "id_curso"}
    ),
    indexes = {
        @Index(name = "idx_malla_carrera", columnList = "id_carrera"),
        @Index(name = "idx_malla_curso", columnList = "id_curso")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MallaCurricular {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_malla_curricular")
    private Integer idMallaCurricular;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Column(name = "ciclo_recomendado", nullable = false)
    @Min(1)
    private Integer cicloRecomendado;

    @Column(name = "obligatorio", nullable = false)
    private Boolean obligatorio = true;

    @Column(name = "creditos", nullable = false)
    @Min(1)
    private Integer creditos;
}
```

Rules:

- `(id_carrera, id_curso)` is unique.
- `creditos` is the progress-counting value and defaults, during seed/import, to `Curso.creditos`.
- `cicloRecomendado >= 1`.
- `obligatorio=false` marks electives from the beginning.
- `creditosTotales` for a career is derived as `sum(malla.creditos)` for that career.

### 3.2 `Prerrequisito`

```java
@Entity
@Table(
    name = "prerrequisito",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_prerrequisito_carrera_curso_pre",
        columnNames = {"id_carrera", "id_curso", "id_curso_prerrequisito"}
    ),
    indexes = {
        @Index(name = "idx_prerrequisito_carrera", columnList = "id_carrera"),
        @Index(name = "idx_prerrequisito_curso", columnList = "id_curso"),
        @Index(name = "idx_prerrequisito_pre", columnList = "id_curso_prerrequisito")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Prerrequisito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prerrequisito")
    private Integer idPrerrequisito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso_prerrequisito", nullable = false)
    private Curso cursoPrerrequisito;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_regla", nullable = false, length = 20)
    private TipoReglaPrerrequisito tipoRegla = TipoReglaPrerrequisito.HARD;

    @PrePersist
    @PreUpdate
    private void validarRegla() {
        if (curso != null && cursoPrerrequisito != null
                && curso.getIdCurso() != null
                && curso.getIdCurso().equals(cursoPrerrequisito.getIdCurso())) {
            throw new IllegalArgumentException("A course cannot be its own prerequisite");
        }
    }
}
```

Rules:

- `(id_carrera, id_curso, id_curso_prerrequisito)` is unique.
- `curso` and `cursoPrerrequisito` must be different.
- Both courses should exist in `MallaCurricular` for the same career; enforce this in service/import validation because cross-table assertions are not portable as JPA annotations.
- A prerequisite course must not be in a later recommended cycle than the target course. Validate in seed/import and in any future write service: `prerrequisito.cicloRecomendado <= cursoDestino.cicloRecomendado`.

### 3.3 `Carrera.creditosTotales`

Do **not** add a persisted `creditos_totales` column in this slice.

Reason: the accepted decision says `creditosTotales` is derived automatically from `Σ(creditos)` in the curriculum plan. Persisting it would create stale data risk, especially with `ddl-auto:update` and no migrations.

Implementation:

- `CarreraResumenDto.creditosTotales` is calculated in `HistorialProgresoService`.
- Optional only if existing catalog responses need it later: add an `@Transient` Java field/getter to `Carrera`, but the progress API should not depend on mutable transient entity state.

### 3.4 `Curso.codigo` Compatibility Gap

The API/spec requires `codigo` in `CursoProgresoDto`, but the current `Curso` entity has only `idCurso`, `nombre`, `creditos`, and `descripcion`.

Add a backwards-compatible nullable column:

```java
@Column(name = "codigo", unique = true, length = 30)
private String codigo;
```

Do not make it `nullable=false` while using `ddl-auto:update`, because existing rows may not have a code. Seed data should populate it. A future migration can enforce `NOT NULL` after backfill.

## 4. Enums

```java
public enum EstadoCursoProgreso {
    PASSED,
    IN_PROGRESS,
    PENDING_AVAILABLE,
    PENDING_BLOCKED,
    FAILED
}

public enum TipoReglaPrerrequisito {
    HARD
}
```

For existing string states:

- Keep `Matricula.estado` as `String` for this slice. Use a constant such as `private static final String ESTADO_MATRICULA_ACTIVA = "ACTIVA";` in repositories/services.
- Keep `HistorialAcademico.estado` as `String` for CRUD compatibility.
- A future migration can introduce `EstadoMatricula` and `EstadoHistorialAcademico`, but doing it now risks changing existing request/response behavior and database contents outside the progress feature.

## 5. Repository Design

### 5.1 `MallaCurricularRepository`

```java
@Repository
public interface MallaCurricularRepository extends JpaRepository<MallaCurricular, Integer> {
    @Query("""
        select mc
        from MallaCurricular mc
        join fetch mc.carrera c
        join fetch mc.curso cu
        where c.idCarrera = :idCarrera
        order by mc.cicloRecomendado asc, cu.nombre asc
    """)
    List<MallaCurricular> findByCarreraIdWithCurso(@Param("idCarrera") Integer idCarrera);

    boolean existsByCarrera_IdCarreraAndCurso_IdCurso(Integer idCarrera, Integer idCurso);
}
```

### 5.2 `PrerrequisitoRepository`

```java
@Repository
public interface PrerrequisitoRepository extends JpaRepository<Prerrequisito, Integer> {
    @Query("""
        select p
        from Prerrequisito p
        join fetch p.carrera c
        join fetch p.curso curso
        join fetch p.cursoPrerrequisito pre
        where c.idCarrera = :idCarrera
    """)
    List<Prerrequisito> findByCarreraIdWithCursos(@Param("idCarrera") Integer idCarrera);

    @Query("""
        select p
        from Prerrequisito p
        join fetch p.curso curso
        join fetch p.cursoPrerrequisito pre
        where p.carrera.idCarrera = :idCarrera
          and curso.idCurso = :idCurso
    """)
    List<Prerrequisito> findByCarreraIdAndCursoIdWithCursos(
        @Param("idCarrera") Integer idCarrera,
        @Param("idCurso") Integer idCurso
    );
}
```

### 5.3 Existing repository additions

`EstudianteRepository`:

```java
@EntityGraph(attributePaths = {"usuario", "carrera"})
@Query("select e from Estudiante e where e.idUsuario = :id")
Optional<Estudiante> findByIdWithUsuarioAndCarrera(@Param("id") Integer id);
```

`HistorialAcademicoRepository`:

```java
@Query("""
    select h
    from HistorialAcademico h
    join fetch h.estudiante e
    join fetch h.seccion s
    join fetch s.curso c
    join fetch s.cicloAcademico ca
    where e.idUsuario = :idEstudiante
""")
List<HistorialAcademico> findByEstudianteIdWithSeccionCursoCiclo(
    @Param("idEstudiante") Integer idEstudiante
);
```

`MatriculaRepository`:

```java
@Query("""
    select m
    from Matricula m
    join fetch m.seccion s
    join fetch s.curso c
    join fetch s.cicloAcademico ca
    where m.estudiante.idUsuario = :idEstudiante
      and m.estado = 'ACTIVA'
""")
List<Matricula> findActiveByEstudianteIdWithSeccionCurso(
    @Param("idEstudiante") Integer idEstudiante
);
```

`NotaRepository`:

```java
@Query("""
    select n
    from Nota n
    join fetch n.evaluacion ev
    join fetch ev.seccion s
    join fetch s.curso c
    where n.estudiante.idUsuario = :idEstudiante
      and s.idSeccion in :seccionIds
""")
List<Nota> findByEstudianteIdAndSeccionIdsWithEvaluacion(
    @Param("idEstudiante") Integer idEstudiante,
    @Param("seccionIds") Collection<Integer> seccionIds
);
```

If a single-section query is useful for tests or future services:

```java
@Query("""
    select n
    from Nota n
    join fetch n.evaluacion ev
    where n.estudiante.idUsuario = :idEstudiante
      and ev.seccion.idSeccion = :idSeccion
""")
List<Nota> findByEstudianteIdAndSeccionIdWithEvaluacion(
    @Param("idEstudiante") Integer idEstudiante,
    @Param("idSeccion") Integer idSeccion
);
```

`DocenteSeccionRepository`:

```java
@Query("""
    select case when count(ds) > 0 then true else false end
    from DocenteSeccion ds
    join ds.seccion s
    join s.matriculas m
    where ds.docente.idUsuario = :idDocente
      and m.estudiante.idUsuario = :idEstudiante
""")
boolean existsDocenteAssignedToEstudiante(
    @Param("idDocente") Integer idDocente,
    @Param("idEstudiante") Integer idEstudiante
);
```

## 6. Service Layer

### 6.1 `HistorialProgresoService`

Main method:

```java
@Transactional(readOnly = true)
public HistorialProgresoResponseDto calcularProgreso(Integer estudianteId)
```

Dependencies:

- `EstudianteRepository`
- `MallaCurricularRepository`
- `PrerrequisitoRepository`
- `HistorialAcademicoRepository`
- `MatriculaRepository`
- `NotaRepository`

Constants:

```java
private static final BigDecimal NOTA_APROBATORIA = new BigDecimal("12.00");
private static final int SCALE_RESPONSE = 2;
```

Responsibilities:

- Load student with `Usuario` and `Carrera`.
- Load curriculum entries for the student's career.
- Load prerequisite rules for that career.
- Load completed attempts from `HistorialAcademico` with section/course/cycle.
- Load active enrollments from `Matricula`.
- Load notes with evaluations for all attempted section IDs.
- Calculate final grade per attempt using `Nota` and `Evaluacion.porcentaje`.
- Pick the latest completed attempt per course.
- Classify each curriculum course.
- Calculate weighted GPA, approved credits, remaining credits, and completion percentage.
- Return DTOs only.

### 6.2 Final grade calculation

For each section attempt:

```text
finalGrade = Σ(nota.nota × nota.evaluacion.porcentaje / 100)
```

Use `BigDecimal` for arithmetic:

- Multiply with sufficient intermediate scale, e.g. `scale 4`.
- Round response values to `2` decimals with `RoundingMode.HALF_UP`.
- Validate note values are in the `0..20` range in service tests; existing DB constraints can be added later if needed.

`HistorialAcademico.notaFinal` remains for existing CRUD compatibility. The progress calculation should use `Nota` + `Evaluacion` as the primary source because FR-003 requires the weighted formula. If legacy rows exist without notes, treat them as incomplete for progress unless the implementation explicitly introduces a documented fallback in a later spec.

### 6.3 Latest attempt rule

Group attempts by `Curso.idCurso` and select the latest attempt using this ordering:

1. `seccion.cicloAcademico.fechaFin` descending.
2. `seccion.cicloAcademico.fechaInicio` descending.
3. `seccion.idSeccion` descending as deterministic tie-breaker.
4. `historial.idHistorial` descending as final tie-breaker.

Only the selected latest attempt contributes to:

- Course `estado`.
- `notaFinal` shown in the course DTO.
- Approved credits.
- Weighted GPA.
- Prerequisite fulfillment.

If an older attempt passed but the latest completed attempt failed, the course is not considered passed because the accepted business rule says retries use the latest attempt.

## 7. Security Design

Use method-level security because `SecurityConfig` already enables `@EnableMethodSecurity`.

### 7.1 Controller annotations

`GET /api/v1/historial-academico/progreso/me`:

```java
@PreAuthorize("hasRole('ESTUDIANTE')")
```

The controller derives `estudianteId` from the authenticated `Usuario` principal, so ownership cannot be spoofed through a path parameter.

`GET /api/v1/historial-academico/progreso/estudiante/{estudianteId}`:

```java
@PreAuthorize("@historialProgresoSecurity.puedeVerProgreso(authentication, #estudianteId)")
```

### 7.2 SpEL helper bean

```java
@Component("historialProgresoSecurity")
@RequiredArgsConstructor
public class HistorialProgresoSecurity {
    private final DocenteSeccionRepository docenteSeccionRepository;

    public boolean puedeVerProgreso(Authentication authentication, Integer estudianteId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        if (hasRole(authentication, "ROLE_ADMIN")) return true;

        Integer principalId = ((Usuario) authentication.getPrincipal()).getIdUsuario();

        if (hasRole(authentication, "ROLE_ESTUDIANTE")) {
            return principalId.equals(estudianteId);
        }

        if (hasRole(authentication, "ROLE_DOCENTE")) {
            return docenteSeccionRepository.existsDocenteAssignedToEstudiante(principalId, estudianteId);
        }

        return false;
    }
}
```

Prefer this bean over a custom `PermissionEvaluator` for this slice because:

- The rule is local to academic progress.
- The project does not currently use a permission-evaluator pattern.
- It is simpler to test with `@WebMvcTest`/method-security tests.

## 8. Controller Design

`HistorialProgresoController`:

```java
@RestController
@RequestMapping("/api/v1/historial-academico/progreso")
@RequiredArgsConstructor
@Tag(name = "Historial Progreso", description = "Read-only academic progress endpoints")
public class HistorialProgresoController {
    private final HistorialProgresoService historialProgresoService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<HistorialProgresoResponseDto> obtenerMiProgreso(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(historialProgresoService.calcularProgreso(usuario.getIdUsuario()));
    }

    @GetMapping("/estudiante/{estudianteId}")
    @PreAuthorize("@historialProgresoSecurity.puedeVerProgreso(authentication, #estudianteId)")
    public ResponseEntity<HistorialProgresoResponseDto> obtenerProgresoEstudiante(
            @PathVariable Integer estudianteId) {
        return ResponseEntity.ok(historialProgresoService.calcularProgreso(estudianteId));
    }
}
```

No POST, PUT, PATCH, or DELETE endpoints are added under `/progreso`.

## 9. DTO Contract

Use Lombok DTO classes, matching existing project conventions.

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialProgresoResponseDto {
    private EstudianteResumenDto estudiante;
    private CarreraResumenDto carrera;
    private ProgresoResumenDto resumen;
    private List<CursoProgresoDto> cursos;
}
```

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstudianteResumenDto {
    private Integer id;
    private String codigo;
    private String nombres;
    private String apellidos;
}
```

Mapping:

- `id` ← `Estudiante.idUsuario`
- `codigo` ← `Estudiante.codigoEstudiante`
- `nombres` ← `Estudiante.usuario.nombre`
- `apellidos` ← `Estudiante.usuario.apellido`

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarreraResumenDto {
    private Integer id;
    private String nombre;
    private Integer creditosTotales;
}
```

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgresoResumenDto {
    private Integer totalCursos;
    private Integer cursosAprobados;
    private Integer cursosEnProgreso;
    private Integer cursosPendientes;
    private Integer creditosAprobados;
    private Integer creditosRestantes;
    private BigDecimal promedioPonderado;
    private BigDecimal porcentajeAvance;
}
```

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CursoProgresoDto {
    private Integer cursoId;
    private String codigo;
    private String nombre;
    private Integer cicloRecomendado;
    private Boolean obligatorio;
    private Integer creditos;
    private EstadoCursoProgreso estado;
    private BigDecimal notaFinal;
    private List<PrerrequisitoProgresoDto> prerrequisitos;
}
```

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrerrequisitoProgresoDto {
    private Integer cursoId;
    private String codigo;
    private String nombre;
    private TipoReglaPrerrequisito tipoRegla;
    private Boolean cumplido;
}
```

No `@JsonProperty` is required because Java field names already match the JSON contract. Existing Jackson config excludes null values, so `notaFinal` may be omitted if null unless clients require explicit nulls. If explicit `null` is required for `notaFinal`, override with `@JsonInclude(JsonInclude.Include.ALWAYS)` on that field.

## 10. Query Strategy and Data Flow

For one progress request, use bounded reads:

1. `EstudianteRepository.findByIdWithUsuarioAndCarrera(estudianteId)`
2. `MallaCurricularRepository.findByCarreraIdWithCurso(carreraId)`
3. `PrerrequisitoRepository.findByCarreraIdWithCursos(carreraId)`
4. `HistorialAcademicoRepository.findByEstudianteIdWithSeccionCursoCiclo(estudianteId)`
5. `MatriculaRepository.findActiveByEstudianteIdWithSeccionCurso(estudianteId)`
6. `NotaRepository.findByEstudianteIdAndSeccionIdsWithEvaluacion(estudianteId, attemptedSectionIds)`

Avoid N+1 by using fetch joins/entity graphs on every relationship needed by the progress service:

- Curriculum → course.
- Prerequisite → target course + prerequisite course.
- History → section → course + academic cycle.
- Enrollment → section → course + academic cycle.
- Note → evaluation → section → course.
- Student → user + career.

Aggregation happens in memory after these bounded query results are loaded for one student.

## 11. Algorithm Design

### 11.1 Pseudocode

```text
calcularProgreso(estudianteId):
  estudiante = load student with usuario and carrera
  if not found: throw EstudianteNotFoundException

  carreraId = estudiante.carrera.idCarrera
  malla = load curriculum entries by carreraId with curso
  prerequisitos = load prerequisites by carreraId with courses
  historial = load history attempts by estudianteId with seccion, curso, ciclo
  matriculasActivas = load active enrollments by estudianteId with seccion, curso

  attemptedSectionIds = historial.map(seccion.idSeccion)
  notas = attemptedSectionIds.empty ? [] : load notes by estudianteId and section ids with evaluacion
  notasPorSeccion = group notas by evaluacion.seccion.idSeccion

  attempts = []
  for historialEntry in historial:
    sectionId = historialEntry.seccion.idSeccion
    finalGrade = calcularNotaFinal(notasPorSeccion[sectionId])
    if finalGrade exists:
      attempts.add(courseId, section, historialEntry, finalGrade)

  latestAttemptByCourse = group attempts by courseId, choose latest by ciclo.fechaFin/fechaInicio/idSeccion/idHistorial
  activeCourseIds = matriculasActivas.map(seccion.curso.idCurso)
  passedCourseIds = latestAttemptByCourse where finalGrade >= 12

  prerequisitosByTargetCourse = group prerequisitos by curso.idCurso

  cursosDto = []
  for mallaEntry in malla ordered by cicloRecomendado, curso.nombre:
    course = mallaEntry.curso
    latestAttempt = latestAttemptByCourse[course.idCurso]
    prereqDtos = build prereq DTOs using passedCourseIds
    unmetHardPrereqs = prereqDtos where tipoRegla == HARD and cumplido == false

    if latestAttempt.finalGrade >= 12:
       estado = PASSED
    else if activeCourseIds contains course.idCurso:
       estado = IN_PROGRESS
    else if unmetHardPrereqs not empty:
       estado = PENDING_BLOCKED
    else if latestAttempt exists and latestAttempt.finalGrade < 12:
       estado = FAILED
    else:
       estado = PENDING_AVAILABLE

    cursosDto.add(course progress dto)

  creditosTotales = sum malla.creditos
  creditosAprobados = sum credits for DTOs with estado PASSED
  creditosRestantes = max(creditosTotales - creditosAprobados, 0)
  promedioPonderado = sum(latestAttempt.finalGrade * malla.creditos) / sum(malla.creditos for courses with latestAttempt)
  porcentajeAvance = creditosTotales == 0 ? 0 : creditosAprobados * 100 / creditosTotales

  resumen = counts and calculated totals
  return response DTO
```

### 11.2 Status classification

Priority order:

1. `PASSED`: latest completed attempt grade is `>= 12.00`.
2. `IN_PROGRESS`: student has active `Matricula` for the course and it is not already passed.
3. `PENDING_BLOCKED`: course is not passed, not active, and has at least one unmet hard prerequisite.
4. `FAILED`: course has a latest completed attempt `< 12.00`, is not active, and is not currently blocked.
5. `PENDING_AVAILABLE`: course is not passed, not active, has no latest failed attempt, and all hard prerequisites are fulfilled.

This preserves the explicit `FAILED` state while still showing active retries as `IN_PROGRESS`.

### 11.3 GPA and credits

- `promedioPonderado`: use the latest completed attempt for each curriculum course with a final grade, passed or failed.
- Numerator: `Σ(notaFinalUltimoIntento × malla.creditos)`.
- Denominator: `Σ(malla.creditos)` for courses included in the numerator.
- `creditosAprobados`: sum credits only for `PASSED` courses.
- `creditosTotales`: sum credits for all `MallaCurricular` entries in the student's career.
- `creditosRestantes`: `max(creditosTotales - creditosAprobados, 0)`.
- `porcentajeAvance`: `(creditosAprobados × 100) / creditosTotales`, rounded to 2 decimals.

### 11.4 Electives

For this slice, electives are represented by `MallaCurricular.obligatorio=false` and are returned in the map.

- Passed electives count toward approved credits and GPA.
- Unpassed electives do not block mandatory courses unless there is an explicit hard prerequisite rule.
- No elective-pool/minimum-selection model is introduced yet; therefore `creditosTotales` is still the sum of all curriculum entries.

## 12. Exception Handling

Add explicit exceptions:

```java
public class EstudianteNotFoundException extends RuntimeException {
    public EstudianteNotFoundException(Integer id) {
        super("Estudiante no encontrado con ID: " + id);
    }
}
```

Recommended `GlobalExceptionHandler` additions:

```java
@ExceptionHandler(EstudianteNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleEstudianteNotFound(EstudianteNotFoundException ex) {
    return buildError(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
}

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    return buildError(HttpStatus.FORBIDDEN, "Prohibido", ex.getMessage());
}

@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return buildError(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Identificador inválido");
}
```

Notes:

- `@PreAuthorize` failures are normally handled by Spring Security's access denied handler and return 403.
- Keep the existing generic `RuntimeException` handler, but ensure specific handlers exist so missing students do not become HTTP 400.

## 13. Testing Strategy

### 13.1 Unit tests — `HistorialProgresoService`

Use Mockito, matching existing service test style.

Scenarios:

- Weighted final grade: `14×40% + 16×60% = 15.20`.
- Grade `11.99` is not passed.
- Latest attempt wins over older attempt.
- Older passed + latest failed results in not passed.
- Active enrollment returns `IN_PROGRESS` unless already passed.
- Unmet hard prerequisite returns `PENDING_BLOCKED` and includes unmet prerequisite DTO.
- Passed prerequisite returns `PENDING_AVAILABLE` or `FAILED` depending on attempts.
- Weighted GPA by credits.
- Approved, remaining, and completion percentage calculations.
- Elective passed counts credits; unpassed elective does not block mandatory courses.
- Missing student throws `EstudianteNotFoundException`.

### 13.2 Repository integration tests

Use `@DataJpaTest` for:

- `MallaCurricularRepository.findByCarreraIdWithCurso` loads courses without lazy failures.
- `PrerrequisitoRepository.findByCarreraIdWithCursos` loads both target and prerequisite course.
- `HistorialAcademicoRepository.findByEstudianteIdWithSeccionCursoCiclo` fetches section, course, and cycle.
- `MatriculaRepository.findActiveByEstudianteIdWithSeccionCurso` filters only `ACTIVA`.
- `NotaRepository.findByEstudianteIdAndSeccionIdsWithEvaluacion` fetches evaluation weights.
- `DocenteSeccionRepository.existsDocenteAssignedToEstudiante` returns true/false correctly.

Because the production database is PostgreSQL and hosted outside the test process, prefer Testcontainers PostgreSQL for repository tests. If that is too heavy for this iteration, add H2 only for repository smoke tests and keep PostgreSQL query syntax JPQL-only.

### 13.3 Security tests

Add `spring-security-test` as a test dependency if absent.

Cover:

- `ESTUDIANTE` can call `/progreso/me` and receives own ID.
- `ESTUDIANTE` cannot call `/progreso/estudiante/{otherId}`.
- `DOCENTE` can call assigned student progress.
- `DOCENTE` cannot call unassigned student progress.
- `ADMIN` can call any student progress.
- Anonymous calls return 401.

Use `@WebMvcTest(HistorialProgresoController.class)` with mocked service and mocked `HistorialProgresoSecurity`, plus at least one method-security test that exercises the real security helper.

### 13.4 API contract tests

Verify JSON fields:

- `estudiante.id`, `estudiante.codigo`, `estudiante.nombres`, `estudiante.apellidos`.
- `carrera.creditosTotales` derived from malla.
- `resumen.promedioPonderado` and `resumen.porcentajeAvance` rounded to 2 decimals.
- Course `estado` serializes as enum string.
- JPA entities are not serialized directly.

## 14. Seed Data

Production seed/import is out of this implementation slice and will be done with InsForge CLI + SQL. The schema must still make seed scripts straightforward.

### 14.1 Test data builders

For tests, create helpers/factories for:

- `Carrera` with `idCarrera` and `nombre`.
- `Curso` with `idCurso`, `codigo`, `nombre`, `creditos`.
- `MallaCurricular` entries with cycle/mandatory/credits.
- `Prerrequisito` rules.
- `CicloAcademico`, `Seccion`, `HistorialAcademico`, `Evaluacion`, `Nota`, `Matricula`.

### 14.2 Example SQL for InsForge/manual seed

```sql
-- Backfill course codes if the new nullable curso.codigo column exists.
update curso
set codigo = concat('CUR-', id_curso)
where codigo is null;

-- Curriculum entries. Adjust names to existing data.
insert into malla_curricular (id_carrera, id_curso, ciclo_recomendado, obligatorio, creditos)
select ca.id_carrera, cu.id_curso, 1, true, cu.creditos
from carrera ca
join curso cu on cu.nombre = 'Matemática I'
where ca.nombre = 'Ingeniería de Sistemas'
on conflict (id_carrera, id_curso) do nothing;

insert into malla_curricular (id_carrera, id_curso, ciclo_recomendado, obligatorio, creditos)
select ca.id_carrera, cu.id_curso, 2, true, cu.creditos
from carrera ca
join curso cu on cu.nombre = 'Matemática II'
where ca.nombre = 'Ingeniería de Sistemas'
on conflict (id_carrera, id_curso) do nothing;

-- Hard prerequisite: Matemática II requires Matemática I.
insert into prerrequisito (id_carrera, id_curso, id_curso_prerrequisito, tipo_regla)
select ca.id_carrera, curso_destino.id_curso, curso_pre.id_curso, 'HARD'
from carrera ca
join curso curso_destino on curso_destino.nombre = 'Matemática II'
join curso curso_pre on curso_pre.nombre = 'Matemática I'
where ca.nombre = 'Ingeniería de Sistemas'
on conflict (id_carrera, id_curso, id_curso_prerrequisito) do nothing;
```

Validation query before enabling the feature:

```sql
select ca.nombre as carrera, count(*) as cursos, sum(mc.creditos) as creditos_totales
from malla_curricular mc
join carrera ca on ca.id_carrera = mc.id_carrera
group by ca.nombre
order by ca.nombre;
```

## 15. Rollout Plan

1. Add enums, entities, repositories, and repository queries.
2. Add `Curso.codigo` as nullable for DTO compatibility.
3. Add `HistorialProgresoService` and unit tests for calculation rules.
4. Add `HistorialProgresoSecurity` and method/security tests.
5. Add `HistorialProgresoController` endpoints.
6. Add exception handler mappings for 404/403/400.
7. Seed minimum `MallaCurricular` and `Prerrequisito` data through InsForge CLI + SQL outside the application deployment.
8. Verify against the API contract before frontend integration.

Rollback is low-risk because endpoints and tables are additive. If seed data is incomplete, disable/hide frontend consumption or block endpoint exposure at the route level while existing CRUD remains available.

## 16. File Change Summary

New files:

- `server/src/main/java/com/example/gestionacademica/historial/domain/MallaCurricular.java`
- `server/src/main/java/com/example/gestionacademica/historial/domain/Prerrequisito.java`
- `server/src/main/java/com/example/gestionacademica/historial/domain/EstadoCursoProgreso.java`
- `server/src/main/java/com/example/gestionacademica/historial/domain/TipoReglaPrerrequisito.java`
- `server/src/main/java/com/example/gestionacademica/historial/repository/MallaCurricularRepository.java`
- `server/src/main/java/com/example/gestionacademica/historial/repository/PrerrequisitoRepository.java`
- `server/src/main/java/com/example/gestionacademica/historial/service/HistorialProgresoService.java`
- `server/src/main/java/com/example/gestionacademica/historial/service/HistorialProgresoSecurity.java`
- `server/src/main/java/com/example/gestionacademica/historial/controller/HistorialProgresoController.java`
- `server/src/main/java/com/example/gestionacademica/historial/dto/*.java`
- `server/src/main/java/com/example/gestionacademica/exceptions/EstudianteNotFoundException.java`

Modified files:

- `server/src/main/java/com/example/gestionacademica/cursos/domain/Curso.java` — add nullable `codigo`.
- `server/src/main/java/com/example/gestionacademica/estudiantes/repository/EstudianteRepository.java` — add fetch query.
- `server/src/main/java/com/example/gestionacademica/historial/repository/HistorialAcademicoRepository.java` — add fetch query.
- `server/src/main/java/com/example/gestionacademica/matriculas/repository/MatriculaRepository.java` — add active enrollment fetch query.
- `server/src/main/java/com/example/gestionacademica/notas/repository/NotaRepository.java` — add note/evaluation fetch query.
- `server/src/main/java/com/example/gestionacademica/docentes/repository/DocenteSeccionRepository.java` — add teacher-student authorization query.
- `server/src/main/java/com/example/gestionacademica/exceptions/GlobalExceptionHandler.java` — add specific handlers.
- `server/pom.xml` — optional test dependencies for `spring-security-test` and PostgreSQL Testcontainers if security/repository integration tests are implemented in this slice.

## 17. Risks and Mitigations

| Risk | Mitigation |
| --- | --- |
| Existing `Curso` rows have no `codigo` | Add nullable field first; seed/backfill codes before frontend depends on it. |
| Existing historical data has `HistorialAcademico.notaFinal` but no `Nota` rows | Progress calculation follows the accepted weighted formula; legacy fallback requires a future explicit decision. |
| `ddl-auto:update` creates schema without reviewable migrations | Keep changes additive and document seed SQL; consider migrations in a later hardening slice. |
| Elective requirements are more complex than a boolean | First slice supports elective flag only; elective pools/minimum credits are out of scope. |
| `String` states differ from expected `ACTIVA` | Keep a single constant and add tests; later migrate to enums if data is normalized. |
| Unauthorized academic data exposure | Enforce `@PreAuthorize` with SpEL helper and repository-backed docente-student check. |
