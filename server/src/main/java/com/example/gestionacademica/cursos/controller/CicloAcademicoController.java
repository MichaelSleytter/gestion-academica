package com.example.gestionacademica.cursos.controller;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.service.CicloAcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de ciclos academicos.
 */
@RestController
@RequestMapping("/api/v1/ciclos-academicos")
@RequiredArgsConstructor
@Tag(name = "CiclosAcademicos", description = "Operaciones CRUD de ciclos academicos")
public class CicloAcademicoController {

    private final CicloAcademicoService cicloAcademicoService;

    /**
     * Lista todos los ciclos academicos.
     *
     * @return lista de ciclos
     */
    @GetMapping
    @Operation(summary = "Listar todos los ciclos academicos")
    public ResponseEntity<List<CicloAcademico>> listarTodos() {
        return ResponseEntity.ok(cicloAcademicoService.listarTodos());
    }

    /**
     * Busca un ciclo academico por ID.
     *
     * @param id identificador del ciclo
     * @return ciclo academico encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar ciclo academico por ID")
    public ResponseEntity<CicloAcademico> buscarPorId(
            @Parameter(description = "ID del ciclo academico", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(cicloAcademicoService.buscarPorId(id));
    }

    /**
     * Crea un nuevo ciclo academico.
     *
     * @param ciclo datos del ciclo
     * @return ciclo creado
     */
    @PostMapping
    @Operation(summary = "Crear ciclo academico")
    public ResponseEntity<CicloAcademico> crear(@RequestBody CicloAcademico ciclo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cicloAcademicoService.crear(ciclo));
    }

    /**
     * Actualiza un ciclo academico.
     *
     * @param id identificador del ciclo
     * @param ciclo datos nuevos
     * @return ciclo actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ciclo academico")
    public ResponseEntity<CicloAcademico> actualizar(
            @Parameter(description = "ID del ciclo academico", example = "1")
            @PathVariable Integer id,
            @RequestBody CicloAcademico ciclo) {
        return ResponseEntity.ok(cicloAcademicoService.actualizar(id, ciclo));
    }

    /**
     * Elimina un ciclo academico por ID.
     *
     * @param id identificador del ciclo
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ciclo academico")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del ciclo academico", example = "1")
            @PathVariable Integer id) {
        cicloAcademicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
