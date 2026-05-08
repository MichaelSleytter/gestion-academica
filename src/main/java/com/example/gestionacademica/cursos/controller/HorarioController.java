package com.example.gestionacademica.cursos.controller;

import com.example.gestionacademica.cursos.domain.Horario;
import com.example.gestionacademica.cursos.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de horarios.
 */
@RestController
@RequestMapping("/api/v1/horarios")
@RequiredArgsConstructor
@Tag(name = "Horarios", description = "Operaciones CRUD de horarios")
public class HorarioController {

    private final HorarioService horarioService;

    /**
     * Lista todos los horarios.
     *
     * @return lista de horarios
     */
    @GetMapping
    @Operation(summary = "Listar todos los horarios")
    public ResponseEntity<List<Horario>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    /**
     * Busca un horario por ID.
     *
     * @param id identificador de horario
     * @return horario encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar horario por ID")
    public ResponseEntity<Horario> buscarPorId(
            @Parameter(description = "ID del horario", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(horarioService.buscarPorId(id));
    }

    /**
     * Lista horarios por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return horarios de la seccion
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar horarios por seccion")
    public ResponseEntity<List<Horario>> listarPorSeccion(
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(horarioService.listarPorSeccion(idSeccion));
    }

    /**
     * Crea un horario asociado a una seccion.
     *
     * @param horario datos del horario
     * @param idSeccion identificador de seccion
     * @return horario creado
     */
    @PostMapping
    @Operation(summary = "Crear horario", description = "Requiere idSeccion como parametro de query")
    public ResponseEntity<Horario> crear(
            @RequestBody Horario horario,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(horarioService.crear(horario, idSeccion));
    }

    /**
     * Actualiza un horario.
     *
     * @param id identificador de horario
     * @param horario datos nuevos
     * @param idSeccion identificador de seccion
     * @return horario actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar horario")
    public ResponseEntity<Horario> actualizar(
            @Parameter(description = "ID del horario", example = "1")
            @PathVariable Integer id,
            @RequestBody Horario horario,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.ok(horarioService.actualizar(id, horario, idSeccion));
    }

    /**
     * Elimina un horario por ID.
     *
     * @param id identificador de horario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar horario")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del horario", example = "1")
            @PathVariable Integer id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
