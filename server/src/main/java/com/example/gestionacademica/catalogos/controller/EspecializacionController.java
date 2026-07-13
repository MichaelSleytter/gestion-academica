package com.example.gestionacademica.catalogos.controller;

import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.catalogos.service.EspecializacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for teacher specializations.
 */
@RestController
@RequestMapping("/api/v1/especializaciones")
@RequiredArgsConstructor
@Tag(name = "Especializaciones", description = "Operaciones CRUD de especializaciones docentes")
public class EspecializacionController {

    private final EspecializacionService especializacionService;

    /**
     * Lists all specializations.
     *
     * @return specializations
     */
    @GetMapping
    @Operation(summary = "Listar todas las especializaciones")
    public ResponseEntity<List<Especializacion>> listarTodas() {
        return ResponseEntity.ok(especializacionService.listarTodas());
    }

    /**
     * Finds a specialization by ID.
     *
     * @param id specialization ID
     * @return found specialization
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar especializacion por ID")
    public ResponseEntity<Especializacion> buscarPorId(
            @Parameter(description = "ID de la especializacion", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(especializacionService.buscarPorId(id));
    }

    /**
     * Creates a specialization.
     *
     * @param especializacion specialization data
     * @return created specialization
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear especializacion")
    public ResponseEntity<Especializacion> crear(@RequestBody Especializacion especializacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especializacionService.crear(especializacion));
    }

    /**
     * Updates a specialization.
     *
     * @param id specialization ID
     * @param especializacion updated data
     * @return updated specialization
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar especializacion")
    public ResponseEntity<Especializacion> actualizar(
            @Parameter(description = "ID de la especializacion", example = "1")
            @PathVariable Integer id,
            @RequestBody Especializacion especializacion) {
        return ResponseEntity.ok(especializacionService.actualizar(id, especializacion));
    }

    /**
     * Deletes a specialization.
     *
     * @param id specialization ID
     * @return empty response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar especializacion")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la especializacion", example = "1")
            @PathVariable Integer id) {
        especializacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
