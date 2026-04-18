package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Seccion;
import com.example.gestionacademica.services.SeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Secciones.
 * Base URL: /api/v1/secciones
 */
@RestController
@RequestMapping("/api/v1/secciones")
@RequiredArgsConstructor
@Tag(name = "Secciones", description = "Operaciones para la gestión de secciones")
public class SeccionController {

    private final SeccionService seccionService;

    @GetMapping
    @Operation(summary = "Listar todas las secciones")
    public ResponseEntity<List<Seccion>> listarTodas() {
        return ResponseEntity.ok(seccionService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sección por ID")
    public ResponseEntity<Seccion> buscarPorId(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(seccionService.buscarPorId(id));
    }

    @GetMapping("/curso/{idCurso}")
    @Operation(summary = "Listar secciones por curso")
    public ResponseEntity<List<Seccion>> listarPorCurso(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer idCurso) {
        return ResponseEntity.ok(seccionService.listarPorCurso(idCurso));
    }

    @GetMapping("/ciclo/{idCiclo}")
    @Operation(summary = "Listar secciones por ciclo academico")
    public ResponseEntity<List<Seccion>> listarPorCiclo(
            @Parameter(description = "ID del ciclo academico", example = "1")
            @PathVariable Integer idCiclo) {
        return ResponseEntity.ok(seccionService.listarPorCiclo(idCiclo));
    }

    @PostMapping
    @Operation(summary = "Crear nueva sección",
               description = "Requiere idCurso e idCiclo como parámetros de query")
    public ResponseEntity<Seccion> crear(
            @RequestBody Seccion seccion,
            @Parameter(description = "ID del curso", example = "1")
            @RequestParam Integer idCurso,
            @Parameter(description = "ID del ciclo academico", example = "1")
            @RequestParam Integer idCiclo) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(seccionService.crear(seccion, idCurso, idCiclo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sección")
    public ResponseEntity<Seccion> actualizar(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer id,
            @RequestBody Seccion seccion,
            @Parameter(description = "ID del curso", example = "1")
            @RequestParam Integer idCurso,
            @Parameter(description = "ID del ciclo academico", example = "1")
            @RequestParam Integer idCiclo) {
        return ResponseEntity.ok(seccionService.actualizar(id, seccion, idCurso, idCiclo));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sección")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la sección a eliminar", example = "1")
            @PathVariable Integer id) {
        seccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}