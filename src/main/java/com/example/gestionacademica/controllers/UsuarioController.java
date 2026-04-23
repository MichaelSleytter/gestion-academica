package com.example.gestionacademica.controllers;

import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de usuarios.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones CRUD de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Lista todos los usuarios.
     *
     * @return lista de usuarios
     */
    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador de usuario
     * @return usuario encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<Usuario> buscarPorId(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario datos del usuario
     * @param idTipoDocumento identificador del tipo de documento
     * @return usuario creado
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Requiere idTipoDocumento como parametro de query")
    public ResponseEntity<Usuario> crear(
            @RequestBody Usuario usuario,
            @Parameter(description = "ID del tipo de documento", example = "1")
            @RequestParam Integer idTipoDocumento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario, idTipoDocumento));
    }

    /**
     * Actualiza un usuario.
     *
     * @param id identificador de usuario
     * @param usuario datos nuevos
     * @param idTipoDocumento identificador del tipo de documento
     * @return usuario actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Usuario> actualizar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id,
            @RequestBody Usuario usuario,
            @Parameter(description = "ID del tipo de documento", example = "1")
            @RequestParam Integer idTipoDocumento) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario, idTipoDocumento));
    }

    /**
     * Elimina un usuario por ID.
     *
     * @param id identificador de usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
