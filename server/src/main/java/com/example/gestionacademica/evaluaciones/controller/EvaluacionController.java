package com.example.gestionacademica.evaluaciones.controller;

import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.evaluaciones.service.EvaluacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de evaluaciones.
 */
@RestController
@RequestMapping("/api/v1/evaluaciones")
@RequiredArgsConstructor
@Tag(name = "Evaluaciones", description = "Operaciones CRUD de evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    /**
     * Lista evaluaciones con paginación y búsqueda opcional por nombre.
     *
     * @param pagina   número de página (0-based)
     * @param tamaño   elementos por página
     * @param busqueda texto de búsqueda (opcional)
     * @return página de evaluaciones
     */
    @GetMapping
    @Operation(summary = "Listar evaluaciones paginadas")
    public ResponseEntity<Page<Evaluacion>> listarTodas(
            @Parameter(description = "Número de página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int tamano,
            @Parameter(description = "Búsqueda por nombre", example = "parcial")
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(evaluacionService.listarPaginado(busqueda, PageRequest.of(pagina, tamano)));
    }

    /**
     * Obtiene una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     * @return evaluacion encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar evaluacion por ID")
    public ResponseEntity<Evaluacion> buscarPorId(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(evaluacionService.buscarPorId(id));
    }

    /**
     * Lista evaluaciones por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return evaluaciones de la seccion
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar evaluaciones por seccion")
    public ResponseEntity<List<Evaluacion>> listarPorSeccion(
            @Parameter(description = "ID de la seccion", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(evaluacionService.listarPorSeccion(idSeccion));
    }

    /**
     * Crea una evaluacion asociada a una seccion.
     *
     * @param evaluacion datos de evaluacion
     * @param idSeccion identificador de seccion
     * @return evaluacion creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear evaluacion", description = "Requiere idSeccion como parametro de query")
    public ResponseEntity<Evaluacion> crear(
            @RequestBody Evaluacion evaluacion,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluacionService.crear(evaluacion, idSeccion));
    }

    /**
     * Actualiza una evaluacion.
     *
     * @param id identificador de evaluacion
     * @param evaluacion datos nuevos
     * @param idSeccion identificador de seccion
     * @return evaluacion actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar evaluacion")
    public ResponseEntity<Evaluacion> actualizar(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id,
            @RequestBody Evaluacion evaluacion,
            @Parameter(description = "ID de la seccion", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity.ok(evaluacionService.actualizar(id, evaluacion, idSeccion));
    }

    /**
     * Elimina una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar evaluacion")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la evaluacion", example = "1")
            @PathVariable Integer id) {
        evaluacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
