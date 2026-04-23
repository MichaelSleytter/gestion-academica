package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Docente;
import com.example.gestionacademica.services.DocenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Docentes.
 * Base URL: /api/v1/docentes
 */
@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
@Tag(name = "Docentes", description = "Operaciones para la gestión de docentes")
public class DocenteController {

    private final DocenteService docenteService;

    /**
     * Lista todos los docentes registrados.
     *
     * @return respuesta HTTP con docentes
     */
    @GetMapping
    @Operation(summary = "Listar todos los docentes")
    public ResponseEntity<List<Docente>> listarTodos() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }

    /**
     * Busca un docente por su identificador.
     *
     * @param id identificador del docente
     * @return respuesta HTTP con el docente encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar docente por ID")
    public ResponseEntity<Docente> buscarPorId(
            @Parameter(description = "ID del docente", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(docenteService.buscarPorId(id));
    }

    /**
     * Busca docentes por especialidad.
     *
     * @param especialidad especialidad a filtrar
     * @return respuesta HTTP con docentes coincidentes
     */
    @GetMapping("/especialidad/{especialidad}")
    @Operation(summary = "Buscar docentes por especialidad")
    public ResponseEntity<List<Docente>> buscarPorEspecialidad(
            @Parameter(description = "Especialidad a buscar", example = "Matemáticas")
            @PathVariable String especialidad) {
        return ResponseEntity.ok(docenteService.listarPorEspecialidad(especialidad));
    }

    /**
     * Elimina un docente por su identificador.
     *
     * @param id identificador del docente
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar docente por ID")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del docente a eliminar", example = "1")
            @PathVariable Integer id) {
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}