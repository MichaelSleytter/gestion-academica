package com.example.gestionacademica.catalogos.controller;

import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.service.GradoAcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de grados academicos.
 */
@RestController
@RequestMapping("/api/v1/grados-academicos")
@RequiredArgsConstructor
@Tag(name = "GradosAcademicos", description = "Operaciones CRUD de grados academicos")
public class GradoAcademicoController {

    private final GradoAcademicoService gradoAcademicoService;

    /**
     * Lista todos los grados academicos.
     *
     * @return lista de grados
     */
    @GetMapping
    @Operation(summary = "Listar todos los grados academicos")
    public ResponseEntity<List<GradoAcademico>> listarTodos() {
        return ResponseEntity.ok(gradoAcademicoService.listarTodos());
    }

    /**
     * Busca un grado academico por ID.
     *
     * @param id identificador
     * @return grado academico
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar grado academico por ID")
    public ResponseEntity<GradoAcademico> buscarPorId(
            @Parameter(description = "ID del grado academico", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(gradoAcademicoService.buscarPorId(id));
    }

    /**
     * Crea un grado academico.
     *
     * @param grado datos a crear
     * @return grado creado
     */
    @PostMapping
    @Operation(summary = "Crear grado academico")
    public ResponseEntity<GradoAcademico> crear(@RequestBody GradoAcademico grado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradoAcademicoService.crear(grado));
    }

    /**
     * Actualiza un grado academico existente.
     *
     * @param id identificador
     * @param grado datos nuevos
     * @return grado actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar grado academico")
    public ResponseEntity<GradoAcademico> actualizar(
            @Parameter(description = "ID del grado academico", example = "1")
            @PathVariable Integer id,
            @RequestBody GradoAcademico grado) {
        return ResponseEntity.ok(gradoAcademicoService.actualizar(id, grado));
    }

    /**
     * Elimina un grado academico.
     *
     * @param id identificador
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar grado academico")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del grado academico", example = "1")
            @PathVariable Integer id) {
        gradoAcademicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
