package com.example.gestionacademica.historial.controller;

import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.service.HistorialAcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de historial academico.
 */
@RestController
@RequestMapping("/api/v1/historial-academico")
@RequiredArgsConstructor
@Tag(name = "HistorialAcademico", description = "Operaciones CRUD de historial academico")
public class HistorialAcademicoController {

    private final HistorialAcademicoService historialAcademicoService;

    /**
     * Lista todo el historial academico.
     *
     * @return lista del historial
     */
    @GetMapping
    @Operation(summary = "Listar todo el historial academico")
    public ResponseEntity<List<HistorialAcademico>> listarTodos() {
        return ResponseEntity.ok(historialAcademicoService.listarTodos());
    }

    /**
     * Busca un registro de historial por ID.
     *
     * @param id identificador de historial
     * @return historial encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar historial academico por ID")
    public ResponseEntity<HistorialAcademico> buscarPorId(
            @Parameter(description = "ID del historial academico", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(historialAcademicoService.buscarPorId(id));
    }

    /**
     * Lista historial por estudiante.
     *
     * @param idEstudiante identificador de estudiante
     * @return historial del estudiante
     */
    @GetMapping("/estudiante/{idEstudiante}")
    @Operation(summary = "Listar historial por estudiante")
    public ResponseEntity<List<HistorialAcademico>> listarPorEstudiante(
            @Parameter(description = "ID del estudiante", example = "1")
            @PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(historialAcademicoService.listarPorEstudiante(idEstudiante));
    }

    /**
     * Lista historial por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return historial de la seccion
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar historial por seccion")
    public ResponseEntity<List<HistorialAcademico>> listarPorSeccion(
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(historialAcademicoService.listarPorSeccion(idSeccion));
    }

    /**
     * Crea un registro de historial academico.
     *
     * @param historial datos del historial
     * @param idEstudiante identificador de estudiante
     * @param idSeccion identificador de seccion
     * @return historial creado
     */
    @PostMapping
    @Operation(summary = "Crear historial academico", description = "Requiere idEstudiante e idSeccion como query params")
    public ResponseEntity<HistorialAcademico> crear(
            @RequestBody HistorialAcademico historial,
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historialAcademicoService.crear(historial, idEstudiante, idSeccion));
    }

    /**
     * Actualiza un registro de historial academico.
     *
     * @param id identificador de historial
     * @param historial datos nuevos
     * @param idEstudiante identificador de estudiante
     * @param idSeccion identificador de seccion
     * @return historial actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar historial academico")
    public ResponseEntity<HistorialAcademico> actualizar(
            @Parameter(description = "ID del historial academico", example = "1")
            @PathVariable Integer id,
            @RequestBody HistorialAcademico historial,
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.ok(historialAcademicoService.actualizar(id, historial, idEstudiante, idSeccion));
    }

    /**
     * Elimina un historial academico por ID.
     *
     * @param id identificador de historial
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar historial academico")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del historial academico", example = "1")
            @PathVariable Integer id) {
        historialAcademicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
