package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Rol;
import com.example.gestionacademica.services.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de roles.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Operaciones CRUD de roles")
public class RolController {

    private final RolService rolService;

    /**
     * Lista todos los roles.
     *
     * @return lista de roles
     */
    @GetMapping
    @Operation(summary = "Listar todos los roles")
    public ResponseEntity<List<Rol>> listarTodos() {
        return ResponseEntity.ok(rolService.listarTodos());
    }

    /**
     * Busca un rol por ID.
     *
     * @param id identificador de rol
     * @return rol encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar rol por ID")
    public ResponseEntity<Rol> buscarPorId(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(rolService.buscarPorId(id));
    }

    /**
     * Crea un nuevo rol.
     *
     * @param rol datos del rol
     * @return rol creado
     */
    @PostMapping
    @Operation(summary = "Crear rol")
    public ResponseEntity<Rol> crear(@RequestBody Rol rol) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(rol));
    }

    /**
     * Actualiza un rol existente.
     *
     * @param id identificador de rol
     * @param rol datos nuevos
     * @return rol actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<Rol> actualizar(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Integer id,
            @RequestBody Rol rol) {
        return ResponseEntity.ok(rolService.actualizar(id, rol));
    }

    /**
     * Elimina un rol por ID.
     *
     * @param id identificador de rol
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Integer id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
