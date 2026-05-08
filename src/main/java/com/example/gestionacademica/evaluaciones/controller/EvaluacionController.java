package com.example.gestionacademica.evaluaciones.controller;

import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.evaluaciones.service.EvaluacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de evaluaciones.
 */
@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Tag(name = "Evaluaciones", description = "Operaciones CRUD de evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    /**
     * Lista todas las evaluaciones.
     *
     * @return lista de evaluaciones
     */
    @GetMapping
    @Operation(summary = "Listar todas las evaluaciones")
    public ResponseEntity<List<Evaluacion>> listarTodas() {
        return ResponseEntity.ok(evaluacionService.listarTodas());
    }

    /**
     * Obtiene una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     * @return evaluacion encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar evaluacion por ID")
    public ResponseEntity<Evaluacion> buscarPorId(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(evaluacionService.buscarPorId(id));
    }

    /**
     * Lista evaluaciones por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return evaluaciones de la seccion
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar evaluaciones por seccion")
    public ResponseEntity<List<Evaluacion>> listarPorSeccion(
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(evaluacionService.listarPorSeccion(idSeccion));
    }

    /**
     * Crea una evaluacion asociada a una seccion.
     *
     * @param evaluacion datos de evaluacion
     * @param idSeccion identificador de seccion
     * @return evaluacion creada
     */
    @PostMapping
    @Operation(summary = "Crear evaluacion", description = "Requiere idSeccion como parametro de query")
    public ResponseEntity<Evaluacion> crear(
            @RequestBody Evaluacion evaluacion,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.crear(evaluacion, idSeccion));
    }

    /**
     * Actualiza una evaluacion.
     *
     * @param id identificador de evaluacion
     * @param evaluacion datos nuevos
     * @param idSeccion identificador de seccion
     * @return evaluacion actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evaluacion")
    public ResponseEntity<Evaluacion> actualizar(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id,
            @RequestBody Evaluacion evaluacion,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.ok(evaluacionService.actualizar(id, evaluacion, idSeccion));
    }

    /**
     * Elimina una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar evaluacion")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id) {
        evaluacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
