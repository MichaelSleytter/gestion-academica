package com.example.gestionacademica.historial.service;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("historialProgresoSecurity")
@RequiredArgsConstructor
public class HistorialProgresoSecurity {

    private final DocenteSeccionRepository docenteSeccionRepository;

    public boolean puedeVerProgreso(Authentication authentication, Integer estudianteId) {
        if (authentication == null || !authentication.isAuthenticated() || estudianteId == null) {
            return false;
        }
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }
        if (!(authentication.getPrincipal() instanceof Usuario usuario) || usuario.getIdUsuario() == null) {
            return false;
        }
        if (hasRole(authentication, "ROLE_ESTUDIANTE")) {
            return usuario.getIdUsuario().equals(estudianteId);
        }
        if (hasRole(authentication, "ROLE_DOCENTE")) {
            return docenteSeccionRepository.existsDocenteAssignedToEstudiante(usuario.getIdUsuario(), estudianteId);
        }
        return false;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role::equals);
    }
}
