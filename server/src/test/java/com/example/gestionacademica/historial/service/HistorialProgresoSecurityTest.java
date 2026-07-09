package com.example.gestionacademica.historial.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - HistorialProgresoSecurity")
class HistorialProgresoSecurityTest {

    @Mock
    private DocenteSeccionRepository docenteSeccionRepository;

    @InjectMocks
    private HistorialProgresoSecurity security;

    @Test
    @DisplayName("puedeVerProgreso: ADMIN puede ver cualquier estudiante")
    void puedeVerProgreso_conAdmin_debePermitirCualquierEstudiante() {
        Authentication authentication = auth(1, "ROLE_ADMIN");

        boolean permitido = security.puedeVerProgreso(authentication, 99);

        assertThat(permitido).isTrue();
        verify(docenteSeccionRepository, never()).existsDocenteAssignedToEstudiante(1, 99);
    }

    @Test
    @DisplayName("puedeVerProgreso: ESTUDIANTE puede ver su propio progreso")
    void puedeVerProgreso_conEstudiantePropio_debePermitir() {
        Authentication authentication = auth(10, "ROLE_ESTUDIANTE");

        boolean permitido = security.puedeVerProgreso(authentication, 10);

        assertThat(permitido).isTrue();
    }

    @Test
    @DisplayName("puedeVerProgreso: ESTUDIANTE no puede ver otro estudiante")
    void puedeVerProgreso_conEstudianteAjeno_debeDenegar() {
        Authentication authentication = auth(10, "ROLE_ESTUDIANTE");

        boolean permitido = security.puedeVerProgreso(authentication, 11);

        assertThat(permitido).isFalse();
    }

    @Test
    @DisplayName("puedeVerProgreso: DOCENTE asignado puede ver al estudiante")
    void puedeVerProgreso_conDocenteAsignado_debePermitir() {
        Authentication authentication = auth(20, "ROLE_DOCENTE");
        when(docenteSeccionRepository.existsDocenteAssignedToEstudiante(20, 30)).thenReturn(true);

        boolean permitido = security.puedeVerProgreso(authentication, 30);

        assertThat(permitido).isTrue();
    }

    @Test
    @DisplayName("puedeVerProgreso: DOCENTE no asignado no puede ver al estudiante")
    void puedeVerProgreso_conDocenteNoAsignado_debeDenegar() {
        Authentication authentication = auth(20, "ROLE_DOCENTE");
        when(docenteSeccionRepository.existsDocenteAssignedToEstudiante(20, 31)).thenReturn(false);

        boolean permitido = security.puedeVerProgreso(authentication, 31);

        assertThat(permitido).isFalse();
    }

    @Test
    @DisplayName("puedeVerProgreso: sin autenticación deniega acceso")
    void puedeVerProgreso_sinAutenticacion_debeDenegar() {
        boolean permitido = security.puedeVerProgreso(null, 10);

        assertThat(permitido).isFalse();
    }

    private Authentication auth(Integer usuarioId, String role) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioId);
        return new UsernamePasswordAuthenticationToken(
            usuario,
            null,
            List.of(new SimpleGrantedAuthority(role))
        );
    }
}
