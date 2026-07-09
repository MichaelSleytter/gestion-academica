package com.example.gestionacademica.historial.controller;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.historial.dto.HistorialProgresoResponseDto;
import com.example.gestionacademica.historial.service.HistorialProgresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/historial-academico/progreso")
@RequiredArgsConstructor
@Tag(name = "Historial Progreso", description = "Read-only academic progress endpoints")
public class HistorialProgresoController {

    private final HistorialProgresoService historialProgresoService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @Operation(summary = "Obtener el progreso académico del estudiante autenticado")
    public ResponseEntity<HistorialProgresoResponseDto> obtenerMiProgreso(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(historialProgresoService.calcularProgreso(usuario.getIdUsuario()));
    }

    @GetMapping("/estudiante/{estudianteId}")
    @PreAuthorize("@historialProgresoSecurity.puedeVerProgreso(authentication, #estudianteId)")
    @Operation(summary = "Obtener el progreso académico de un estudiante autorizado")
    public ResponseEntity<HistorialProgresoResponseDto> obtenerProgresoEstudiante(
            @Parameter(description = "ID del estudiante", example = "10")
            @PathVariable Integer estudianteId) {
        return ResponseEntity.ok(historialProgresoService.calcularProgreso(estudianteId));
    }
}
