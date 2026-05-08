package com.example.gestionacademica.estudiantes.controller;

import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.estudiantes.dto.EstudianteRequestDTO;
import com.example.gestionacademica.estudiantes.dto.EstudianteResponseDTO;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.estudiantes.service.EstudianteService;
import com.example.gestionacademica.estudiantes.mapper.EstudianteMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de Estudiantes.
 * Base URL: /api/v1/estudiantes
 *
 * Endpoints:
 * GET /api/v1/estudiantes → listar todos
 * GET /api/v1/estudiantes/{id} → buscar por ID
 * POST /api/v1/estudiantes → crear
 * PUT /api/v1/estudiantes/{id} → actualizar
 * DELETE /api/v1/estudiantes/{id} → eliminar
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Operaciones CRUD para la gestión de estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final EstudianteMapper estudianteMapper;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/estudiantes
    // ─────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar todos los estudiantes", description = "Retorna la lista completa de estudiantes registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    /**
     * Obtiene todos los estudiantes registrados en el sistema.
     *
     * @return respuesta HTTP con la lista de estudiantes mapeados a DTO
     */
    public ResponseEntity<List<EstudianteResponseDTO>> listarTodos() {
        List<EstudianteResponseDTO> respuesta = estudianteService
                .listarTodos()
                .stream()
                .map(estudianteMapper::aDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estudiante por ID", description = "Retorna un estudiante específico según su ID (id_usuario)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudiante encontrado"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
    })
    /**
     * Busca un estudiante por su identificador de usuario.
     *
     * @param id identificador del estudiante
     * @return respuesta HTTP con el estudiante encontrado
     */
    public ResponseEntity<EstudianteResponseDTO> buscarPorId(
            @Parameter(description = "ID del estudiante (id_usuario)", example = "1") @PathVariable Integer id) {
        Estudiante estudiante = estudianteService.buscarPorId(id);
        return ResponseEntity.ok(estudianteMapper.aDto(estudiante));
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/estudiantes
    // ─────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Crear nuevo estudiante", description = "Registra un nuevo estudiante junto con su usuario base en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudiante creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados"),
    })
    /**
     * Crea un nuevo estudiante a partir de los datos de la solicitud.
     *
     * @param requestDTO datos de registro del estudiante
     * @return respuesta HTTP con el estudiante creado
     */
    public ResponseEntity<EstudianteResponseDTO> crear(
            @Valid @RequestBody EstudianteRequestDTO requestDTO) {
        // Construir el comando de creación a partir del DTO de solicitud
        EstudianteCrearDTO comando = EstudianteCrearDTO.builder()
                .nombre(requestDTO.getNombre())
                .apellido(requestDTO.getApellido())
                .numeroDocumento(requestDTO.getNumeroDocumento())
                .idTipoDocumento(requestDTO.getIdTipoDocumento())
                .emailPersonal(requestDTO.getEmailPersonal())
                .ciclo(requestDTO.getCiclo())
                .idCarrera(requestDTO.getIdCarrera())
                .build();

        EstudianteService.ResultadoCreacion resultado = estudianteService.crearConCredenciales(comando);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                estudianteMapper.aDto(resultado.estudianteCreado()));
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estudiante", description = "Actualiza los datos academicos de un estudiante existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudiante actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
    })
    /**
     * Actualiza los datos académicos de un estudiante existente.
     *
     * @param id         identificador del estudiante
     * @param requestDTO datos a actualizar
     * @return respuesta HTTP con el estudiante actualizado
     */
    public ResponseEntity<EstudianteResponseDTO> actualizar(
            @Parameter(description = "ID del estudiante a actualizar", example = "1") @PathVariable Integer id,
            @Valid @RequestBody EstudianteRequestDTO requestDTO) {
        // Actualizamos usuario y estudiante a partir del DTO (merge de campos no nulos)
        Estudiante actualizado = estudianteService.actualizarDesdeDto(
                id,
                requestDTO);
        return ResponseEntity.ok(estudianteMapper.aDto(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estudiante", description = "Elimina un estudiante del sistema por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estudiante eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
    })
    /**
     * Elimina un estudiante por su identificador.
     *
     * @param id identificador del estudiante
     * @return respuesta HTTP sin contenido
     */
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del estudiante a eliminar", example = "1") @PathVariable Integer id) {
        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────
    // ENDPOINTS ADICIONALES
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/carrera/{idCarrera}")
    @Operation(summary = "Listar estudiantes por carrera", description = "Retorna todos los estudiantes de una carrera específica")
    /**
     * Lista los estudiantes asociados a una carrera.
     *
     * @param idCarrera identificador de la carrera
     * @return respuesta HTTP con estudiantes de la carrera
     */
    public ResponseEntity<List<EstudianteResponseDTO>> listarPorCarrera(
            @Parameter(description = "ID de la carrera", example = "1") @PathVariable Integer idCarrera) {
        List<EstudianteResponseDTO> respuesta = estudianteService
                .listarPorCarrera(idCarrera)
                .stream()
                .map(estudianteMapper::aDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/ciclo/{ciclo}")
    @Operation(summary = "Listar estudiantes por ciclo", description = "Retorna todos los estudiantes de un ciclo academico específico")
    /**
     * Lista los estudiantes de un ciclo académico específico.
     *
     * @param ciclo número de ciclo académico
     * @return respuesta HTTP con estudiantes del ciclo
     */
    public ResponseEntity<List<EstudianteResponseDTO>> listarPorCiclo(
            @Parameter(description = "Número de ciclo", example = "3") @PathVariable Integer ciclo) {
        List<EstudianteResponseDTO> respuesta = estudianteService
                .listarPorCiclo(ciclo)
                .stream()
                .map(estudianteMapper::aDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }
}
