package com.example.gestionacademica.catalogos.controller;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.service.CarreraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad Carrera.
 * Devuelve la entidad Carrera directamente (sin DTOs).
 */
@RestController
@RequestMapping("/api/v1/carreras")
@RequiredArgsConstructor
@Tag(name = "Carreras", description = "Operaciones para obtener carreras")
public class CarreraController {

    private final CarreraService carreraService;

    /**
     * Lista todas las carreras registradas.
     *
     * @return respuesta HTTP con la lista de carreras
     */
    @GetMapping
    @Operation(summary = "Listar todas las carreras")
    public ResponseEntity<List<Carrera>> listarTodos() {
        return ResponseEntity.ok(carreraService.listarTodas());
    }

    /**
     * Obtiene una carrera por su identificador.
     *
     * @param id identificador de la carrera
     * @return respuesta HTTP con la carrera encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener carrera por ID")
    public ResponseEntity<Carrera> buscarPorId(
            @Parameter(description = "ID de la carrera", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.buscarPorId(id));
    }

    /**
     * Crea una nueva carrera.
     *
     * @param carrera entidad con los datos de la carrera
     * @return respuesta HTTP con la carrera creada
     */
    @PostMapping
    @Operation(summary = "Crear nueva carrera")
    public ResponseEntity<Carrera> crear(@RequestBody Carrera carrera) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.crear(carrera));
    }

    /**
     * Actualiza una carrera existente.
     *
     * @param id identificador de la carrera
     * @param carrera datos actualizados de la carrera
     * @return respuesta HTTP con la carrera actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar carrera")
    public ResponseEntity<Carrera> actualizar(
            @Parameter(description = "ID de la carrera", example = "1")
            @PathVariable Integer id,
            @RequestBody Carrera carrera) {
        return ResponseEntity.ok(carreraService.actualizar(id, carrera));
    }

    /**
     * Elimina una carrera por su identificador.
     *
     * @param id identificador de la carrera
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar carrera")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la carrera", example = "1")
            @PathVariable Integer id) {
        carreraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
