package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.DocenteSeccion;
import com.example.gestionacademica.services.DocenteSeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para asignaciones entre docentes y secciones.
 */
@RestController
@RequestMapping("/api/v1/docentes-secciones")
@RequiredArgsConstructor
@Tag(name = "DocentesSecciones", description = "Operaciones para gestionar asignaciones docente-seccion")
public class DocenteSeccionController {

    private final DocenteSeccionService docenteSeccionService;

    /**
     * Lista todas las asignaciones docente-seccion.
     *
     * @return asignaciones registradas
     */
    @GetMapping
    @Operation(summary = "Listar asignaciones docente-seccion")
    public ResponseEntity<List<DocenteSeccion>> listarTodas() {
        return ResponseEntity.ok(docenteSeccionService.listarTodas());
    }

    /**
     * Obtiene una asignacion por su llave compuesta.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     * @return asignacion encontrada
     */
    @GetMapping("/{idDocente}/{idSeccion}")
    @Operation(summary = "Buscar asignacion docente-seccion")
    public ResponseEntity<DocenteSeccion> buscarPorId(
            @Parameter(description = "ID del docente", example = "1")
            @PathVariable Integer idDocente,
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(docenteSeccionService.buscarPorId(idDocente, idSeccion));
    }

    /**
     * Lista asignaciones por docente.
     *
     * @param idDocente identificador del docente
     * @return asignaciones del docente
     */
    @GetMapping("/docente/{idDocente}")
    @Operation(summary = "Listar asignaciones por docente")
    public ResponseEntity<List<DocenteSeccion>> listarPorDocente(
            @Parameter(description = "ID del docente", example = "1")
            @PathVariable Integer idDocente) {
        return ResponseEntity.ok(docenteSeccionService.listarPorDocente(idDocente));
    }

    /**
     * Lista asignaciones por seccion.
     *
     * @param idSeccion identificador de la seccion
     * @return asignaciones de la seccion
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar asignaciones por seccion")
    public ResponseEntity<List<DocenteSeccion>> listarPorSeccion(
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(docenteSeccionService.listarPorSeccion(idSeccion));
    }

    /**
     * Crea una asignacion docente-seccion.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     * @return asignacion creada
     */
    @PostMapping
    @Operation(summary = "Crear asignacion docente-seccion")
    public ResponseEntity<DocenteSeccion> crear(
            @Parameter(description = "ID del docente", example = "1")
            @RequestParam Integer idDocente,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteSeccionService.crear(idDocente, idSeccion));
    }

    /**
     * Elimina una asignacion docente-seccion.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{idDocente}/{idSeccion}")
    @Operation(summary = "Eliminar asignacion docente-seccion")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del docente", example = "1")
            @PathVariable Integer idDocente,
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        docenteSeccionService.eliminar(idDocente, idSeccion);
        return ResponseEntity.noContent().build();
    }
}
