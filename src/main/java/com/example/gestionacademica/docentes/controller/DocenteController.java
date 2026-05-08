package com.example.gestionacademica.docentes.controller;

import com.example.gestionacademica.docentes.dto.DocenteRequestDTO;
import com.example.gestionacademica.docentes.dto.DocenteResponseDTO;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.mapper.DocenteMapper;
import com.example.gestionacademica.auth.mapper.UsuarioMapper;
import com.example.gestionacademica.docentes.service.DocenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de Docentes.
 * Base URL: /api/v1/docentes
 */
@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
@Tag(name = "Docentes", description = "Operaciones para la gestión de docentes")
public class DocenteController {

    private final DocenteService docenteService;
    private final UsuarioMapper usuarioMapper;
    private final DocenteMapper docenteMapper;

    /**
     * Lista todos los docentes registrados.
     *
     * @return respuesta HTTP con docentes
     */
    @GetMapping
    @Operation(summary = "Listar todos los docentes")
    public ResponseEntity<List<DocenteResponseDTO>> listarTodos() {
        List<DocenteResponseDTO> respuesta = docenteService
            .listarTodos()
            .stream()
            .map(docenteMapper::aDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Busca un docente por su identificador.
     *
     * @param id identificador del docente
     * @return respuesta HTTP con el docente encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar docente por ID")
    public ResponseEntity<DocenteResponseDTO> buscarPorId(
            @Parameter(description = "ID del docente", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(docenteMapper.aDto(docenteService.buscarPorId(id)));
    }



    @PostMapping
    @Operation(summary = "Crear nuevo docente")
    public ResponseEntity<DocenteResponseDTO> crear(
        @Valid @RequestBody DocenteRequestDTO requestDTO
    ) {
        Usuario usuario = usuarioMapper.desdeSolicitud(requestDTO);
        Docente docente = new Docente();
        docente.setEspecialidad(requestDTO.getEspecialidad());

        Docente nuevoDocente = docenteService.crear(
            usuario,
            docente,
            requestDTO.getIdGrado(),
            requestDTO.getIdTipoDocumento()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteMapper.aDto(nuevoDocente));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar docente")
    public ResponseEntity<DocenteResponseDTO> actualizar(
        @Parameter(description = "ID del docente a actualizar", example = "1")
        @PathVariable Integer id,
        @Valid @RequestBody DocenteRequestDTO requestDTO
    ) {
        Docente datos = new Docente();
        datos.setEspecialidad(requestDTO.getEspecialidad());

        Docente actualizado = docenteService.actualizar(
            id,
            datos,
            requestDTO.getIdGrado()
        );
        return ResponseEntity.ok(docenteMapper.aDto(actualizado));
    }
    /**
     * Busca docentes por especialidad.
     *
     * @param especialidad especialidad a filtrar
     * @return respuesta HTTP con docentes coincidentes
     */
    @GetMapping("/especialidad/{especialidad}")
    @Operation(summary = "Buscar docentes por especialidad")
    public ResponseEntity<List<DocenteResponseDTO>> buscarPorEspecialidad(
            @Parameter(description = "Especialidad a buscar", example = "Matemáticas")
            @PathVariable String especialidad) {
        List<DocenteResponseDTO> respuesta = docenteService
            .listarPorEspecialidad(especialidad)
            .stream()
            .map(docenteMapper::aDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Elimina un docente por su identificador.
     *
     * @param id identificador del docente
     * @return respuesta HTTP sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar docente por ID")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del docente a eliminar", example = "1")
            @PathVariable Integer id) {
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
