package com.example.gestionacademica.matriculas.controller;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.dto.MatriculaMisCursosDTO;
import com.example.gestionacademica.matriculas.dto.MatriculaSeccionResponseDTO;
import com.example.gestionacademica.matriculas.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
     * Obtiene los cursos activos del estudiante autenticado.
     *
     * @return Lista de DTOs con datos de matrícula, sección y curso.
     */
    @GetMapping("/mis-cursos")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Obtener cursos del estudiante autenticado")
    public ResponseEntity<List<MatriculaMisCursosDTO>> misCursos(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(matriculaService.listarMisCursos(usuario.getIdUsuario()));
    }

    /**
     * Lista todas las matrículas registradas.
     *
     * @return respuesta HTTP con matrículas
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<List<MatriculaSeccionResponseDTO>> listarPorSeccion(
            @Parameter(description = "ID de la sección", example = "1")
            @PathVariable Integer idSeccion) {
        return ResponseEntity.ok(matriculaService.listarPorSeccion(idSeccion).stream()
                .map(this::aRespuestaSeccion)
                .toList());
    }

    private MatriculaSeccionResponseDTO aRespuestaSeccion(Matricula matricula) {
        Usuario usuario = matricula.getEstudiante().getUsuario();
        return new MatriculaSeccionResponseDTO(
                matricula.getIdMatricula(),
                matricula.getFechaMatricula(),
                matricula.getEstado().name(),
                matricula.getEstudiante().getIdUsuario(),
                matricula.getEstudiante().getCodigoEstudiante(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail());
    }

    /**
     * Registra una nueva matrícula para un estudiante en una sección.
     *
     * @param idEstudiante identificador del estudiante
     * @param idSeccion identificador de la sección
     * @return respuesta HTTP con la matrícula creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cambiar estado de matrícula",
        description = "Estados válidos: ACTIVA, RETIRADA, APROBADA, DESAPROBADA"
    )
    public ResponseEntity<Matricula> cambiarEstado(
            @Parameter(description = "ID de la matrícula", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Nuevo estado", example = "RETIRADA")
            @RequestParam MatriculaEstado estado) {
        return ResponseEntity.ok(matriculaService.cambiarEstado(id, estado));
    }

    /**
     * Elimina una matrícula por su identificador.
     *
     * @param id identificador de la matrícula
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar matrícula")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la matrícula a eliminar", example = "1")
            @PathVariable Integer id) {
        matriculaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
