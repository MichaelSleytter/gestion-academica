package com.example.gestionacademica.controllers;

import com.example.gestionacademica.dtos.EstudianteRequestDTO;
import com.example.gestionacademica.dtos.EstudianteResponseDTO;
import com.example.gestionacademica.entities.Estudiante;
import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.services.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de Estudiantes.
 * Base URL: /api/v1/estudiantes
 *
 * Endpoints:
 *  GET    /api/v1/estudiantes          → listar todos
 *  GET    /api/v1/estudiantes/{id}     → buscar por ID
 *  POST   /api/v1/estudiantes          → crear
 *  PUT    /api/v1/estudiantes/{id}     → actualizar
 *  DELETE /api/v1/estudiantes/{id}     → eliminar
 */
@RestController
@RequestMapping("/api/v1/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Operaciones CRUD para la gestión de estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/estudiantes
    // ─────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
        summary = "Listar todos los estudiantes",
        description = "Retorna la lista completa de estudiantes registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    public ResponseEntity<List<EstudianteResponseDTO>> listarTodos() {
        List<EstudianteResponseDTO> respuesta = estudianteService.listarTodos()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar estudiante por ID",
        description = "Retorna un estudiante específico según su ID (id_usuario)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estudiante encontrado"),
        @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    public ResponseEntity<EstudianteResponseDTO> buscarPorId(
            @Parameter(description = "ID del estudiante (id_usuario)", example = "1")
            @PathVariable Integer id) {

        Estudiante estudiante = estudianteService.buscarPorId(id);
        return ResponseEntity.ok(mapToResponseDTO(estudiante));
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/estudiantes
    // ─────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
        summary = "Crear nuevo estudiante",
        description = "Registra un nuevo estudiante junto con su usuario base en el sistema"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Estudiante creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o duplicados")
    })
    public ResponseEntity<EstudianteResponseDTO> crear(
            @Valid @RequestBody EstudianteRequestDTO requestDTO) {

        // Construir entidad Usuario desde el DTO
        Usuario usuario = new Usuario();
        usuario.setNombre(requestDTO.getNombre());
        usuario.setApellido(requestDTO.getApellido());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setPassword(requestDTO.getPassword());
        usuario.setNumeroDocumento(requestDTO.getNumeroDocumento());

        // Construir entidad Estudiante desde el DTO
        Estudiante estudiante = new Estudiante();
        estudiante.setCodigoEstudiante(requestDTO.getCodigoEstudiante());
        estudiante.setCiclo(requestDTO.getCiclo());
        estudiante.setEstadoAcademico(
            requestDTO.getEstadoAcademico() != null
                ? requestDTO.getEstadoAcademico()
                : "ACTIVO"
        );

        Estudiante creado = estudianteService.crear(
                usuario,
                estudiante,
                requestDTO.getIdCarrera(),
                requestDTO.getIdTipoDocumento()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapToResponseDTO(creado));
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar estudiante",
        description = "Actualiza los datos academicos de un estudiante existente"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estudiante actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    public ResponseEntity<EstudianteResponseDTO> actualizar(
            @Parameter(description = "ID del estudiante a actualizar", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody EstudianteRequestDTO requestDTO) {

        // Solo actualizamos datos academicos del estudiante
        Estudiante datos = new Estudiante();
        datos.setCodigoEstudiante(requestDTO.getCodigoEstudiante());
        datos.setCiclo(requestDTO.getCiclo());
        datos.setEstadoAcademico(requestDTO.getEstadoAcademico());

        Estudiante actualizado = estudianteService.actualizar(
                id,
                datos,
                requestDTO.getIdCarrera()
        );

        return ResponseEntity.ok(mapToResponseDTO(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE /api/v1/estudiantes/{id}
    // ─────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar estudiante",
        description = "Elimina un estudiante del sistema por su ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Estudiante eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del estudiante a eliminar", example = "1")
            @PathVariable Integer id) {

        estudianteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────
    // ENDPOINTS ADICIONALES
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/carrera/{idCarrera}")
    @Operation(
        summary = "Listar estudiantes por carrera",
        description = "Retorna todos los estudiantes de una carrera específica"
    )
    public ResponseEntity<List<EstudianteResponseDTO>> listarPorCarrera(
            @Parameter(description = "ID de la carrera", example = "1")
            @PathVariable Integer idCarrera) {

        List<EstudianteResponseDTO> respuesta = estudianteService
                .listarPorCarrera(idCarrera)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/ciclo/{ciclo}")
    @Operation(
        summary = "Listar estudiantes por ciclo",
        description = "Retorna todos los estudiantes de un ciclo academico específico"
    )
    public ResponseEntity<List<EstudianteResponseDTO>> listarPorCiclo(
            @Parameter(description = "Número de ciclo", example = "3")
            @PathVariable Integer ciclo) {

        List<EstudianteResponseDTO> respuesta = estudianteService
                .listarPorCiclo(ciclo)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    // ─────────────────────────────────────────────────────────────────
    // MAPPER PRIVADO — Entidad → DTO
    // ─────────────────────────────────────────────────────────────────

    /**
     * Convierte una entidad Estudiante a EstudianteResponseDTO.
     * Maneja nulls defensivamente para evitar NullPointerException.
     */
    private EstudianteResponseDTO mapToResponseDTO(Estudiante e) {
        return EstudianteResponseDTO.builder()
                .idUsuario(e.getIdUsuario())
                .codigoEstudiante(e.getCodigoEstudiante())
                .ciclo(e.getCiclo())
                .estadoAcademico(e.getEstadoAcademico())
                // Datos del usuario base
                .nombre(e.getUsuario() != null ? e.getUsuario().getNombre() : null)
                .apellido(e.getUsuario() != null ? e.getUsuario().getApellido() : null)
                .email(e.getUsuario() != null ? e.getUsuario().getEmail() : null)
                .numeroDocumento(e.getUsuario() != null ? e.getUsuario().getNumeroDocumento() : null)
                .estado(e.getUsuario() != null ? e.getUsuario().getEstado() : null)
                // Tipo de documento
                .tipoDocumento(
                    e.getUsuario() != null && e.getUsuario().getTipoDocumento() != null
                        ? e.getUsuario().getTipoDocumento().getNombre()
                        : null
                )
                // Carrera
                .idCarrera(e.getCarrera() != null ? e.getCarrera().getIdCarrera() : null)
                .nombreCarrera(e.getCarrera() != null ? e.getCarrera().getNombre() : null)
                .build();
    }
}