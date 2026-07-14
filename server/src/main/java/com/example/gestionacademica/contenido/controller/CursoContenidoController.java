package com.example.gestionacademica.contenido.controller;

import com.example.gestionacademica.contenido.domain.CursoContenido;
import com.example.gestionacademica.contenido.dto.CursoContenidoRequest;
import com.example.gestionacademica.contenido.service.CursoContenidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Controlador REST para la gestión de contenido de cursos (archivos subidos).
 */
@RestController
@RequestMapping("/api/v1/contenido")
@RequiredArgsConstructor
@Tag(name = "Contenido", description = "Gestión de contenido de cursos (archivos)")
public class CursoContenidoController {

    private final CursoContenidoService contenidoService;

    /**
     * Lista el contenido activo de una sección.
     *
     * @param idSeccion identificador de la sección
     * @return lista de contenido activo
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar contenido por sección")
    public ResponseEntity<List<CursoContenido>> listarPorSeccion(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer idSeccion,
            Authentication authentication) {
        return ResponseEntity.ok(contenidoService.listarPorSeccion(idSeccion, authentication));
    }

    /**
     * Guarda la metadata de un archivo subido a InsForge Storage.
     *
     * @param request metadata del contenido
     * @return contenido creado
     */
    @PostMapping
    @Operation(summary = "Guardar metadata de contenido subido")
    public ResponseEntity<CursoContenido> guardar(
            @Valid @RequestBody CursoContenidoRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contenidoService.guardar(request, authentication));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir contenido de curso")
    public ResponseEntity<CursoContenido> subir(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idSeccion") Integer idSeccion,
            @RequestParam("semana") Integer semana,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contenidoService.subir(file, idSeccion, semana, authentication));
    }

    /**
     * Eliminación lógica de un contenido.
     *
     * @param id identificador del contenido
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar contenido (soft delete)")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del contenido", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        contenidoService.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
