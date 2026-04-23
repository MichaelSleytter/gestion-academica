package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.services.TipoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la entidad TipoDocumento.
 * Devuelve la entidad TipoDocumento directamente (sin DTOs).
 */
@RestController
@RequestMapping("/api/v1/tipos-documento")
@RequiredArgsConstructor
@Tag(name = "TiposDocumento", description = "Operaciones para obtener tipos de documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    /**
     * Lista todos los tipos de documento.
     *
     * @return respuesta HTTP con los tipos de documento
     */
    @GetMapping
    @Operation(summary = "Listar todos los tipos de documento")
    public ResponseEntity<List<TipoDocumento>> listarTodos() {
        return ResponseEntity.ok(tipoDocumentoService.listarTodos());
    }

    /**
     * Obtiene un tipo de documento por su identificador.
     *
     * @param id identificador del tipo de documento
     * @return respuesta HTTP con el tipo de documento encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de documento por ID")
    public ResponseEntity<TipoDocumento> buscarPorId(
            @Parameter(description = "ID del tipo de documento", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(tipoDocumentoService.buscarPorId(id));
    }

    /**
     * Crea un nuevo tipo de documento.
     *
     * @param tipoDocumento datos del tipo de documento
     * @return respuesta HTTP con el tipo de documento creado
     */
    @PostMapping
    @Operation(summary = "Crear tipo de documento")
    public ResponseEntity<TipoDocumento> crear(@RequestBody TipoDocumento tipoDocumento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoDocumentoService.crear(tipoDocumento));
    }

    /**
     * Actualiza un tipo de documento existente.
     *
     * @param id identificador del tipo de documento
     * @param tipoDocumento datos actualizados
     * @return respuesta HTTP con el tipo de documento actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de documento")
    public ResponseEntity<TipoDocumento> actualizar(
            @Parameter(description = "ID del tipo de documento", example = "1")
            @PathVariable Integer id,
            @RequestBody TipoDocumento tipoDocumento) {
        return ResponseEntity.ok(tipoDocumentoService.actualizar(id, tipoDocumento));
    }

    /**
     * Elimina un tipo de documento por su identificador.
     *
     * @param id identificador del tipo de documento
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de documento")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del tipo de documento", example = "1")
            @PathVariable Integer id) {
        tipoDocumentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
