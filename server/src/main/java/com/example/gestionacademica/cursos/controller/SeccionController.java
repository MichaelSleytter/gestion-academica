package com.example.gestionacademica.cursos.controller;

import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.service.SeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de Secciones.
 * Base URL: /api/v1/secciones
 */
@RestController
@RequestMapping("/api/v1/secciones")
@RequiredArgsConstructor
@Tag(name = "Secciones", description = "Operaciones para la gestión de secciones")
public class SeccionController {

    private final SeccionService seccionService;

    /**
     * Lista las secciones con paginación y búsqueda opcional.
     * <p>
     * La búsqueda se aplica sobre: código de sección y nombre del ciclo académico.
     *
     * @param pagina   número de página (empieza en 0)
     * @param tamaño   elementos por página
     * @param busqueda texto para filtrar (opcional)
     * @return página de secciones
     */
    @GetMapping
    @Operation(summary = "Listar secciones con paginación")
    public ResponseEntity<Page<Seccion>> listarPaginado(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int tamaño,
            @Parameter(description = "Texto de búsqueda (opcional)", example = "2024-01")
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(seccionService.listarPaginado(busqueda, PageRequest.of(pagina, tamaño)));
    }

    /**
     * Busca una sección por identificador.
     *
     * @param id identificador de la sección
     * @return respuesta HTTP con la sección encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCENTE')")
    @Operation(summary = "Buscar sección por ID")
    public ResponseEntity<Seccion> buscarPorId(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(seccionService.buscarPorIdAutorizado(id, authentication));
    }

    /**
     * Lista las secciones asociadas a un curso.
     *
     * @param idCurso identificador del curso
     * @return respuesta HTTP con secciones del curso
     */
    @GetMapping("/curso/{idCurso}")
    @Operation(summary = "Listar secciones por curso")
    public ResponseEntity<List<Seccion>> listarPorCurso(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer idCurso) {
        return ResponseEntity.ok(seccionService.listarPorCurso(idCurso));
    }

    /**
     * Lista las secciones asociadas a un ciclo académico.
     *
     * @param idCiclo identificador del ciclo académico
     * @return respuesta HTTP con secciones del ciclo
     */
    @GetMapping("/proximo-codigo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generar proximo codigo de seccion")
    public ResponseEntity<Map<String, String>> generarProximoCodigo(
            @Parameter(description = "ID del curso", example = "1")
            @RequestParam Integer idCurso,
            @Parameter(description = "ID del ciclo academico", example = "1")
            @RequestParam Integer idCiclo) {
        return ResponseEntity.ok(Map.of("codigo", seccionService.generarProximoCodigo(idCurso, idCiclo)));
    }

    @GetMapping("/ciclo/{idCiclo}")
    @Operation(summary = "Listar secciones por ciclo academico")
    public ResponseEntity<List<Seccion>> listarPorCiclo(
            @Parameter(description = "ID del ciclo academico", example = "1")
            @PathVariable Integer idCiclo) {
        return ResponseEntity.ok(seccionService.listarPorCiclo(idCiclo));
    }

    /**
     * Crea una nueva sección asociada a curso y ciclo académico.
     *
     * @param seccion datos de la sección
     * @param idCurso identificador del curso
     * @param idCiclo identificador del ciclo académico
     * @return respuesta HTTP con la sección creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nueva sección",
               description = "Requiere idCurso e idCiclo como parámetros de query")
    public ResponseEntity<Seccion> crear(
            @RequestBody Seccion seccion,
            @Parameter(description = "ID del curso", example = "1")
            @RequestParam Integer idCurso,
            @Parameter(description = "ID del ciclo academico", example = "1")
            @RequestParam Integer idCiclo) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(seccionService.crear(seccion, idCurso, idCiclo));
    }

    /**
     * Actualiza una sección existente y sus relaciones principales.
     *
     * @param id identificador de la sección
     * @param seccion datos actualizados
     * @param idCurso identificador del curso
     * @param idCiclo identificador del ciclo académico
     * @return respuesta HTTP con la sección actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sección")
    public ResponseEntity<Seccion> actualizar(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer id,
            @RequestBody Seccion seccion,
            @Parameter(description = "ID del curso", example = "1")
            @RequestParam Integer idCurso,
            @Parameter(description = "ID del ciclo academico", example = "1")
            @RequestParam Integer idCiclo) {
        return ResponseEntity.ok(seccionService.actualizar(id, seccion, idCurso, idCiclo));
    }

    /**
     * Elimina una sección por su identificador.
     *
     * @param id identificador de la sección
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar sección")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la sección a eliminar", example = "1")
            @PathVariable Integer id) {
        seccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
