package com.example.gestionacademica.estudiantes.service;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.estudiantes.util.EstudianteUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstudianteValidator {

    private final UsuarioRepository usuarioRepository;

    public void validarDocumentoNoDuplicado(String numeroDocumento) {
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new RuntimeException(
                "Ya existe un usuario con el documento: " + numeroDocumento
            );
        }
    }

    public void validarCorreoNoDuplicado(String correo) {
        if (usuarioRepository.existsByEmail(correo)) {
            throw new RuntimeException(
                "Ya existe un usuario con el email generado: " + correo
            );
        }
    }

    public void asegurarCodigoEstudiante(Estudiante estudiante) {
        if (
            estudiante.getCodigoEstudiante() == null ||
            estudiante.getCodigoEstudiante().trim().isEmpty()
        ) {
            estudiante.setCodigoEstudiante(
                EstudianteUtil.generarCodigoAleatorio(8)
            );
        }
    }
}
