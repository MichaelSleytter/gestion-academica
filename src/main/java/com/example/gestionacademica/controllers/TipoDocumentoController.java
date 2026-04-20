package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.repositories.TipoDocumentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @GetMapping
    @Operation(summary = "Listar todos los tipos de documento")
    public ResponseEntity<List<TipoDocumento>> listarTodos() {
        return ResponseEntity.ok(tipoDocumentoRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de documento por ID")
    public ResponseEntity<TipoDocumento> buscarPorId(
            @Parameter(description = "ID del tipo de documento", example = "1")
            @PathVariable Integer id) {
        TipoDocumento t = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con ID: " + id));
        return ResponseEntity.ok(t);
    }
}
