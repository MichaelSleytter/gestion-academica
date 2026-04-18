package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Curso;
import com.example.gestionacademica.services.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Cursos.
 * Base URL: /api/v1/cursos
 */
@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Operaciones para la gestión de cursos")
public class CursoController {

    private final CursoService cursoService;

    @GetMapping
    @Operation(summary = "Listar todos los cursos")
    public ResponseEntity<List<Curso>> listarTodos() {
        return ResponseEntity.ok(cursoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar curso por ID")
    public ResponseEntity<Curso> buscarPorId(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(cursoService.buscarPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar cursos por nombre")
    public ResponseEntity<List<Curso>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre", example = "Matemáticas")
            @RequestParam String nombre) {
        return ResponseEntity.ok(cursoService.buscarPorNombre(nombre));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo curso")
    public ResponseEntity<Curso> crear(@Valid @RequestBody Curso curso) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cursoService.crear(curso));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar curso")
    public ResponseEntity<Curso> actualizar(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody Curso curso) {
        return ResponseEntity.ok(cursoService.actualizar(id, curso));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar curso")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del curso a eliminar", example = "1")
            @PathVariable Integer id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}