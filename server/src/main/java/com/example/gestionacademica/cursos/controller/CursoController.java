package com.example.gestionacademica.cursos.controller;

import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Cursos.
 * Base URL: /api/v1/cursos
 */
@RestController
@RequestMapping("/api/v1/cursos")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Operaciones para la gestión de cursos")
public class CursoController {

    private final CursoService cursoService;

    /**
     * Lista todos los cursos con paginación y búsqueda opcional.
     * <p>
     * La búsqueda se aplica sobre: nombre y descripción del curso.
     *
     * @param pagina   número de página (empieza en 0)
     * @param tamaño   elementos por página
     * @param busqueda texto para filtrar (opcional)
     * @return página de cursos
     */
    @GetMapping
    @Operation(summary = "Listar cursos con paginación")
    public ResponseEntity<Page<Curso>> listarPaginado(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Elementos por página", example = "10")
            @RequestParam(defaultValue = "10") int tamaño,
            @Parameter(description = "Texto de búsqueda (opcional)", example = "Matemáticas")
            @RequestParam(required = false) String busqueda) {
        return ResponseEntity.ok(cursoService.listarPaginado(busqueda, PageRequest.of(pagina, tamaño)));
    }

    /**
     * Obtiene un curso por su identificador.
     *
     * @param id identificador del curso
     * @return respuesta HTTP con el curso encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar curso por ID")
    public ResponseEntity<Curso> buscarPorId(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(cursoService.buscarPorId(id));
    }

    /**
     * Busca cursos por coincidencia parcial de nombre.
     *
     * @param nombre texto a buscar en el nombre del curso
     * @return respuesta HTTP con cursos coincidentes
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar cursos por nombre")
    public ResponseEntity<List<Curso>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre", example = "Matemáticas")
            @RequestParam String nombre) {
        return ResponseEntity.ok(cursoService.buscarPorNombre(nombre));
    }

    /**
     * Crea un nuevo curso.
     *
     * @param curso datos del curso
     * @return respuesta HTTP con el curso creado
     */
    @PostMapping
    @Operation(summary = "Crear nuevo curso")
    public ResponseEntity<Curso> crear(@Valid @RequestBody Curso curso) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cursoService.crear(curso));
    }

    /**
     * Actualiza un curso existente.
     *
     * @param id identificador del curso
     * @param curso datos actualizados
     * @return respuesta HTTP con el curso actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar curso")
    public ResponseEntity<Curso> actualizar(
            @Parameter(description = "ID del curso", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody Curso curso) {
        return ResponseEntity.ok(cursoService.actualizar(id, curso));
    }

    /**
     * Elimina un curso por su identificador.
     *
     * @param id identificador del curso
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar curso")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del curso a eliminar", example = "1")
            @PathVariable Integer id) {
        cursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}