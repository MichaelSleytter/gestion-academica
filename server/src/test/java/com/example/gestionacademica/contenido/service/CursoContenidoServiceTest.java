package com.example.gestionacademica.contenido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.contenido.domain.CursoContenido;
import com.example.gestionacademica.contenido.dto.CursoContenidoRequest;
import com.example.gestionacademica.contenido.repository.CursoContenidoRepository;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - CursoContenidoService")
class CursoContenidoServiceTest {

    @Mock
    private CursoContenidoRepository repository;

    @Mock
    private DocenteSeccionRepository docenteSeccionRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    private CursoContenidoService service;

    @BeforeEach
    void setUp() {
        service = new CursoContenidoService(
                repository,
                docenteSeccionRepository,
                matriculaRepository,
                new ObjectMapper(),
                "https://3auan78u.us-east.insforge.app",
                "");
    }

    @Test
    @DisplayName("guardar: debe rechazar semana fuera de rango")
    void guardar_conSemanaInvalida_debeRechazar() {
        assertThatThrownBy(() -> service.guardar(validRequest(19, validUrl(validKey())), adminAuth()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("semana");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("guardar: debe rechazar URL de otro host aunque contenga la ruta")
    void guardar_conUrlDeOtroHost_debeRechazar() {
        String spoofedUrl = "https://evil.test/api/storage/buckets/curso-contenido/objects/" + encodedValidKey();

        assertThatThrownBy(() -> service.guardar(validRequest(1, spoofedUrl), adminAuth()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("URL");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("guardar: debe rechazar key de otra sección")
    void guardar_conKeyDeOtraSeccion_debeRechazar() {
        String wrongKey = "seccion/8/123-clase.pdf";

        assertThatThrownBy(() -> service.guardar(validRequest(wrongKey, 1, validUrl(wrongKey)), adminAuth()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ruta");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("guardar: debe aceptar URL exacta del bucket y key")
    void guardar_conUrlExacta_debeGuardar() {
        when(repository.save(any(CursoContenido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CursoContenido saved = service.guardar(validRequest(18, validUrl(validKey())), adminAuth());

        assertThat(saved.getKey()).isEqualTo(validKey());
        assertThat(saved.getSemana()).isEqualTo(18);
    }

    @Test
    @DisplayName("upload: debe configurar timeout finito en la solicitud a Storage")
    void uploadRequest_debeTenerTimeoutFinito() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "clase.pdf", "application/pdf", "contenido".getBytes());

        HttpRequest request = service.buildUploadRequest(file, validKey(), "boundary");

        assertThat(request.timeout()).contains(Duration.ofSeconds(60));
    }

    private CursoContenidoRequest validRequest(Integer semana, String url) {
        return validRequest(validKey(), semana, url);
    }

    private CursoContenidoRequest validRequest(String key, Integer semana, String url) {
        return new CursoContenidoRequest(
                7,
                "clase.pdf",
                key,
                url,
                "application/pdf",
                100L,
                semana);
    }

    private String validKey() {
        return "seccion/7/123-clase.pdf";
    }

    private String encodedValidKey() {
        return "seccion%2F7%2F123-clase.pdf";
    }

    private String validUrl(String key) {
        return "https://3auan78u.us-east.insforge.app/api/storage/buckets/curso-contenido/objects/"
                + key.replace("/", "%2F");
    }

    private Authentication adminAuth() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
