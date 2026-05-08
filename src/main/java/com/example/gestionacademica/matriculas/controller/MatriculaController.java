package com.example.gestionacademica.matriculas.controller;

import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Matrículas.
 * Base URL: /api/v1/matriculas
 */
@RestController
@RequestMapping("/api/v1/matriculas")
@RequiredArgsConstructor
@Tag(name = "Matrículas", description = "Operaciones para la gestión de matrículas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    /**
     * Lista todas las matrículas registradas.
     *
     * @return respuesta HTTP con matrículas
     */
    @GetMapping
    @Operation(summary = "Listar todas las matrículas")
    public ResponseEntity<List<Matricula>> listarTodas() {
        return ResponseEntity.ok(matriculaService.listarTodas());
    }

    /**
     * Busca una matrícula por identificador.
     *
     * @param id identificador de la matrícula
     * @return respuesta HTTP con la matrícula encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar matrícula por ID")
    public ResponseEntity<Matricula> buscarPorId(
            @Parameter(description = "ID de la matrícula", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(matriculaService.buscarPorId(id));
    }

    /**
     * Lista matrículas asociadas a un estudiante.
     *
     * @param idEstudiante identificador del estudiante
     * @return respuesta HTTP con matrículas del estudiante
     */
    @GetMapping("/estudiante/{idEstudiante}")
    @Operation(summary = "Listar matrículas de un estudiante")
    public ResponseEntity<List<Matricula>> listarPorEstudiante(
            @Parameter(description = "ID del estudiante (id_usuario)", example = "1")
            @PathVariable Integer idEstudiante) {
        return ResponseEntity.ok(matriculaService.listarPorEstudiante(idEstudiante));
    }

    /**
     * Lista matrículas asociadas a una sección.
     *
     * @param idSeccion identificador de la sección
     * @return respuesta HTTP con matrículas de la sección
     */
    @GetMapping("/seccion/{idSeccion}")
    @Operation(summary = "Listar matrículas de una sección")
    public ResponseEntity<List<Matricula>> listarPorSeccion(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(matriculaService.listarPorSeccion(idSeccion));
    }

    /**
     * Registra una nueva matrícula para un estudiante en una sección.
     *
     * @param idEstudiante identificador del estudiante
     * @param idSeccion identificador de la sección
     * @return respuesta HTTP con la matrícula creada
     */
    @PostMapping
    @Operation(
        summary = "Registrar nueva matrícula",
        description = "Matricula a un estudiante en una sección. Valida duplicados y vacantes."
    )
    public ResponseEntity<Matricula> matricular(
            @Parameter(description = "ID del estudiante", example = "1")
            @RequestParam Integer idEstudiante,
            @Parameter(description = "ID de la sección", example = "1")
            @RequestParam Integer idSeccion) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(matriculaService.matricular(idEstudiante, idSeccion));
    }

    /**
     * Cambia el estado de una matrícula existente.
     *
     * @param id identificador de la matrícula
     * @param estado nuevo estado de la matrícula
     * @return respuesta HTTP con la matrícula actualizada
     */
    @PatchMapping("/{id}/estado")
    @Operation(
        summary = "Cambiar estado de matrícula",
        description = "Estados válidos: ACTIVA, RETIRADA, APROBADA, DESAPROBADA"
    )
    public ResponseEntity<Matricula> cambiarEstado(
            @Parameter(description = "ID de la matrícula", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Nuevo estado", example = "RETIRADA")
            @RequestParam String estado) {
        return ResponseEntity.ok(matriculaService.cambiarEstado(id, estado));
    }

    /**
     * Elimina una matrícula por su identificador.
     *
     * @param id identificador de la matrícula
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar matrícula")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la matrícula a eliminar", example = "1")
            @PathVariable Integer id) {
        matriculaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}