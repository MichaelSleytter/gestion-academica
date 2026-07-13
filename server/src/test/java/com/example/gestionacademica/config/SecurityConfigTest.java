package com.example.gestionacademica.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.security.JwtAuthenticationFilter;
import com.example.gestionacademica.contenido.controller.CursoContenidoController;
import com.example.gestionacademica.contenido.domain.CursoContenido;
import com.example.gestionacademica.contenido.service.CursoContenidoService;
import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CursoContenidoController.class)
@Import(SecurityConfig.class)
@DisplayName("Pruebas - SecurityConfig")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CursoContenidoService contenidoService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowMockedJwtFilterToContinue() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("POST /contenido/upload rechaza ESTUDIANTE antes del controlador")
    void upload_conEstudiante_debeRetornarForbiddenAntesDelControlador() throws Exception {
        mockMvc.perform(multipart("/api/v1/contenido/upload")
                        .with(authentication(auth(30, "ROLE_ESTUDIANTE"))))
                .andExpect(status().isForbidden());

        verify(contenidoService, never()).subir(any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /contenido/upload permite DOCENTE")
    void upload_conDocente_debeLlegarAlControlador() throws Exception {
        when(contenidoService.subir(any(), any(), any(), any())).thenReturn(new CursoContenido());

        mockMvc.perform(uploadRequest().with(authentication(auth(20, "ROLE_DOCENTE"))))
                .andExpect(status().isCreated());

        verify(contenidoService).subir(any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /contenido/seccion/{id} mantiene acceso autenticado")
    void listarContenido_conEstudiante_debeMantenerRutaDisponible() throws Exception {
        when(contenidoService.listarPorSeccion(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/contenido/seccion/7")
                        .with(authentication(auth(30, "ROLE_ESTUDIANTE"))))
                .andExpect(status().isOk());

        verify(contenidoService).listarPorSeccion(any(), any());
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder uploadRequest() {
        return multipart("/api/v1/contenido/upload")
                .file(new MockMultipartFile("file", "clase.pdf", "application/pdf", "contenido".getBytes()))
                .file(new MockMultipartFile("idSeccion", "", "application/json", "7".getBytes()))
                .file(new MockMultipartFile("semana", "", "application/json", "1".getBytes()));
    }

    private Authentication auth(Integer userId, String role) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(userId);
        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority(role)));
    }
}
