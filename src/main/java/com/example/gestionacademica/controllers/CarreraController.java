package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Carrera;
import com.example.gestionacademica.repositories.CarreraRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    private final CarreraRepository carreraRepository;

    @GetMapping
    @Operation(summary = "Listar todas las carreras")
    public ResponseEntity<List<Carrera>> listarTodos() {
        return ResponseEntity.ok(carreraRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener carrera por ID")
    public ResponseEntity<Carrera> buscarPorId(
            @Parameter(description = "ID de la carrera", example = "1")
            @PathVariable Integer id) {
        Carrera c = carreraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada con ID: " + id));
        return ResponseEntity.ok(c);
    }
}
