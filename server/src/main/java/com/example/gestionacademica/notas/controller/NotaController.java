package com.example.gestionacademica.notas.controller;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.notas.dto.MisNotasResponseDTO;
import com.example.gestionacademica.notas.dto.NotaResponseDTO;
import com.example.gestionacademica.notas.service.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de notas.
 */
@RestController
@RequestMapping("/api/v1/notas")
@RequiredArgsConstructor
@Tag(name = "Notas", description = "Operaciones CRUD de notas")
public class NotaController {

    private final NotaService notaService;

    /**
     * Lista todas las notas.
     *
     * @return lista de notas
     */
    @GetMapping
    @Operation(summary = "Listar todas las notas")
    public ResponseEntity<List<Nota>> listarTodas() {
        return ResponseEntity.ok(notaService.listarTodas());
    }

    /**
     * Busca una nota por ID.
     *
     * @param id identificador de nota
     * @return nota encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota por ID")
    public ResponseEntity<Nota> buscarPorId(
            @Parameter(description = "ID de la nota", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(notaService.buscarPorId(id));
    }

    /**
     * Lista notas por evaluacion.
     *
     * @param idEvaluacion identificador de evaluacion
     * @return notas de la evaluacion
     */
    @GetMapping("/evaluacion/{idEvaluacion}")
    @Operation(summary = "Listar notas por evaluacion")
    public ResponseEntity<List<NotaResponseDTO>> listarPorEvaluacion(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer idEvaluacion) {
        return ResponseEntity.ok(notaService.listarPorEvaluacion(idEvaluacion).stream()
                .map(this::aRespuesta)
                .toList());
    }

    private NotaResponseDTO aRespuesta(Nota nota) {
        return new NotaResponseDTO(nota.getIdNota(), nota.getNota(), nota.getEstudiante().getIdUsuario());
    }

    /**
     * Obtiene las notas del estudiante autenticado para todas las evaluaciones
     * de una sección.
     *
     * @param authentication Contexto de seguridad (inyectado automáticamente).
     * @param idSeccion      ID de la sección.
     * @return Lista de notas con ID de evaluación asociada.
     */
    @GetMapping("/mis-notas/seccion/{idSeccion}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Obtener notas del estudiante autenticado en una sección")
    public ResponseEntity<List<MisNotasResponseDTO>> misNotasPorSeccion(
            Authentication authentication,
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer idSeccion) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(
                notaService.listarMisNotasPorSeccion(usuario.getIdUsuario(), idSeccion));
    }

    /**
     * Lista notas por estudiante.
     *
     * @param idEstudiante identificador de estudiante
     * @return notas del estudiante
     */
    @GetMapping("/estudiante/{idEstudiante}")
    @Operation(summary = "Listar notas por estudiante")
    public ResponseEntity<List<Nota>> listarPorEstudiante(
            @Parameter(description = "ID del estudiante", example = "1")
            @PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(notaService.listarPorEstudiante(idEstudiante));
    }

    /**
     * Crea una nota.
     *
     * @param nota datos de nota
     * @param idEvaluacion identificador de evaluacion
     * @param idEstudiante identificador de estudiante
     * @return nota creada
     */
    @PostMapping
    @Operation(summary = "Crear nota", description = "Requiere idEvaluacion e idEstudiante como query params")
    public ResponseEntity<Nota> crear(
            @RequestBody Nota nota,
            @Parameter(description = "ID de la evaluacion", example = "1")
            @RequestParam Integer idEvaluacion,
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notaService.crear(nota, idEvaluacion, idEstudiante, authentication));
    }

    /**
     * Actualiza una nota.
     *
     * @param id identificador de nota
     * @param nota datos nuevos
     * @param idEvaluacion identificador de evaluacion
     * @param idEstudiante identificador de estudiante
     * @return nota actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nota")
    public ResponseEntity<Nota> actualizar(
            @Parameter(description = "ID de la nota", example = "1")
            @PathVariable Integer id,
            @RequestBody Nota nota,
            @Parameter(description = "ID de la evaluacion", example = "1")
            @RequestParam Integer idEvaluacion,
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            Authentication authentication) {
        return ResponseEntity.ok(notaService.actualizar(id, nota, idEvaluacion, idEstudiante, authentication));
    }

    /**
     * Elimina una nota.
     *
     * @param id identificador de nota
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar nota")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la nota", example = "1")
            @PathVariable Integer id,
            Authentication authentication) {
        notaService.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
