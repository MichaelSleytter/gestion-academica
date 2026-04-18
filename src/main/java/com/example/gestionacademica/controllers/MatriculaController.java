package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Matricula;
import com.example.gestionacademica.services.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Matrículas.
 * Base URL: /api/v1/matriculas
 */
@RestController
@RequestMapping("/api/v1/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matrículas", description = "Operaciones para la gestión de matrículas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    @Operation(summary = "Listar todas las matrículas")
    public ResponseEntity<List<Matricula>> listarTodas() {
        return ResponseEntity.ok(matriculaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matrícula por ID")
    public ResponseEntity<Matricula> buscarPorId(
            @Parameter(description = "ID de la matrícula", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(matriculaService.buscarPorId(id));
    }

    @GetMapping("/estudiante/{idEstudiante}")
    @Operation(summary = "Listar matrículas de un estudiante")
    public ResponseEntity<List<Matricula>> listarPorEstudiante(
            @Parameter(description = "ID del estudiante (id_usuario)", example = "1")
            @PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(matriculaService.listarPorEstudiante(idEstudiante));
    }

    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar matrículas de una sección")
    public ResponseEntity<List<Matricula>> listarPorSeccion(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(matriculaService.listarPorSeccion(idSeccion));
    }

    @PostMapping
    @Operation(
        summary = "Registrar nueva matrícula",
        description = "Matricula a un estudiante en una sección. Valida duplicados y vacantes."
    )
    public ResponseEntity<Matricula> matricular(
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            @Parameter(description = "ID de la sección", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(matriculaService.matricular(idEstudiante, idSeccion));
    }

    @PatchMapping("/{id}/estado")
    @Operation(
        summary = "Cambiar estado de matrícula",
        description = "Estados válidos: ACTIVA, RETIRADA, APROBADA, DESAPROBADA"
    )
    public ResponseEntity<Matricula> cambiarEstado(
            @Parameter(description = "ID de la matrícula", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Nuevo estado", example = "RETIRADA")
            @RequestParam String estado) {
        return ResponseEntity.ok(matriculaService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar matrícula")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la matrícula a eliminar", example = "1")
            @PathVariable Integer id) {
        matriculaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}