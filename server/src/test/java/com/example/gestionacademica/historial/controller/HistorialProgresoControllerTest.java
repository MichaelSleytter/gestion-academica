package com.example.gestionacademica.historial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.gestionacademica.administradores.services.AdministradorService;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.security.JwtAuthenticationFilter;
import com.example.gestionacademica.exceptions.EstudianteNotFoundException;
import com.example.gestionacademica.exceptions.GlobalExceptionHandler;
import com.example.gestionacademica.historial.domain.EstadoCursoProgreso;
import com.example.gestionacademica.historial.domain.TipoReglaPrerrequisito;
import com.example.gestionacademica.historial.dto.CarreraResumenDto;
import com.example.gestionacademica.historial.dto.CursoProgresoDto;
import com.example.gestionacademica.historial.dto.EstudianteResumenDto;
import com.example.gestionacademica.historial.dto.HistorialProgresoResponseDto;
import com.example.gestionacademica.historial.dto.PrerrequisitoProgresoDto;
import com.example.gestionacademica.historial.dto.ProgresoResumenDto;
import com.example.gestionacademica.historial.service.HistorialProgresoSecurity;
import com.example.gestionacademica.historial.service.HistorialProgresoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = HistorialProgresoController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@ContextConfiguration(classes = {
    HistorialProgresoController.class,
    GlobalExceptionHandler.class,
    HistorialProgresoControllerTest.TestSecurityConfig.class
})
@DisplayName("Pruebas - HistorialProgresoController")
class HistorialProgresoControllerTest {

    private static final String BASE = "/api/v1/historial-academico/progreso";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistorialProgresoService historialProgresoService;

    @MockBean(name = "historialProgresoSecurity")
    private HistorialProgresoSecurity historialProgresoSecurity;

    @MockBean
    private AdministradorService administradorService;

    @Test
    @DisplayName("GET /progreso/me retorna 200 para ESTUDIANTE autenticado")
    void obtenerMiProgreso_conEstudianteAutenticado_debeRetornarOk() throws Exception {
        when(historialProgresoService.calcularProgreso(10)).thenReturn(response(10));

        mockMvc.perform(get(BASE + "/me").with(authentication(auth(10, "ROLE_ESTUDIANTE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estudiante.id").value(10))
            .andExpect(jsonPath("$.resumen.promedioPonderado").value(13.13));
    }

    @Test
    @DisplayName("GET /progreso/me retorna 403 para DOCENTE")
    void obtenerMiProgreso_conDocente_debeRetornarForbidden() throws Exception {
        mockMvc.perform(get(BASE + "/me").with(authentication(auth(20, "ROLE_DOCENTE"))))
            .andExpect(status().isForbidden());

        verify(historialProgresoService, never()).calcularProgreso(any());
    }

    @Test
    @DisplayName("GET /progreso/me retorna 403 para ADMIN")
    void obtenerMiProgreso_conAdmin_debeRetornarForbidden() throws Exception {
        mockMvc.perform(get(BASE + "/me").with(authentication(auth(1, "ROLE_ADMIN"))))
            .andExpect(status().isForbidden());

        verify(historialProgresoService, never()).calcularProgreso(any());
    }

    @Test
    @DisplayName("GET /progreso/me sin autenticación retorna 401")
    void obtenerMiProgreso_sinAutenticacion_debeRetornarUnauthorized() throws Exception {
        mockMvc.perform(get(BASE + "/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: ESTUDIANTE puede ver su propio progreso")
    void obtenerProgresoEstudiante_conEstudiantePropio_debeRetornarOk() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(10))).thenReturn(true);
        when(historialProgresoService.calcularProgreso(10)).thenReturn(response(10));

        mockMvc.perform(get(BASE + "/estudiante/10").with(authentication(auth(10, "ROLE_ESTUDIANTE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estudiante.id").value(10));
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: ADMIN puede ver cualquier estudiante")
    void obtenerProgresoEstudiante_conAdmin_debeRetornarOk() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(30))).thenReturn(true);
        when(historialProgresoService.calcularProgreso(30)).thenReturn(response(30));

        mockMvc.perform(get(BASE + "/estudiante/30").with(authentication(auth(1, "ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estudiante.id").value(30));
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: ESTUDIANTE no puede ver otro estudiante")
    void obtenerProgresoEstudiante_conEstudianteAjeno_debeRetornarForbidden() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(11))).thenReturn(false);

        mockMvc.perform(get(BASE + "/estudiante/11").with(authentication(auth(10, "ROLE_ESTUDIANTE"))))
            .andExpect(status().isForbidden());

        verify(historialProgresoService, never()).calcularProgreso(11);
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: DOCENTE asignado puede ver al estudiante")
    void obtenerProgresoEstudiante_conDocenteAsignado_debeRetornarOk() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(20))).thenReturn(true);
        when(historialProgresoService.calcularProgreso(20)).thenReturn(response(20));

        mockMvc.perform(get(BASE + "/estudiante/20").with(authentication(auth(2, "ROLE_DOCENTE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estudiante.id").value(20));
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: DOCENTE no asignado recibe 403")
    void obtenerProgresoEstudiante_conDocenteNoAsignado_debeRetornarForbidden() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(21))).thenReturn(false);

        mockMvc.perform(get(BASE + "/estudiante/21").with(authentication(auth(2, "ROLE_DOCENTE"))))
            .andExpect(status().isForbidden());

        verify(historialProgresoService, never()).calcularProgreso(21);
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: sin token retorna 401")
    void obtenerProgresoEstudiante_sinAutenticacion_debeRetornarUnauthorized() throws Exception {
        mockMvc.perform(get(BASE + "/estudiante/20"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /progreso retorna JSON con contrato de campos esperado")
    void obtenerProgresoEstudiante_conRespuesta_debeRetornarContratoJson() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(10))).thenReturn(true);
        when(historialProgresoService.calcularProgreso(10)).thenReturn(response(10));

        mockMvc.perform(get(BASE + "/estudiante/10").with(authentication(auth(1, "ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estudiante.id").value(10))
            .andExpect(jsonPath("$.estudiante.codigo").value("20240010"))
            .andExpect(jsonPath("$.estudiante.nombres").value("Ana"))
            .andExpect(jsonPath("$.estudiante.apellidos").value("Torres"))
            .andExpect(jsonPath("$.carrera.id").value(3))
            .andExpect(jsonPath("$.carrera.nombre").value("Ingeniería de Sistemas"))
            .andExpect(jsonPath("$.carrera.creditosTotales").value(200))
            .andExpect(jsonPath("$.resumen.totalCursos").value(48))
            .andExpect(jsonPath("$.resumen.cursosAprobados").value(20))
            .andExpect(jsonPath("$.resumen.cursosEnProgreso").value(3))
            .andExpect(jsonPath("$.resumen.cursosPendientes").value(25))
            .andExpect(jsonPath("$.resumen.creditosAprobados").value(80))
            .andExpect(jsonPath("$.resumen.creditosRestantes").value(120))
            .andExpect(jsonPath("$.resumen.promedioPonderado").value(13.13))
            .andExpect(jsonPath("$.resumen.porcentajeAvance").value(40.00))
            .andExpect(jsonPath("$.cursos[0].cursoId").value(101))
            .andExpect(jsonPath("$.cursos[0].codigo").value("MAT101"))
            .andExpect(jsonPath("$.cursos[0].nombre").value("Matemática I"))
            .andExpect(jsonPath("$.cursos[0].cicloRecomendado").value(1))
            .andExpect(jsonPath("$.cursos[0].obligatorio").value(true))
            .andExpect(jsonPath("$.cursos[0].creditos").value(4))
            .andExpect(jsonPath("$.cursos[0].estado").value("PASSED"))
            .andExpect(jsonPath("$.cursos[0].notaFinal").value(15.20))
            .andExpect(jsonPath("$.cursos[0].prerrequisitos[0].cursoId").value(100))
            .andExpect(jsonPath("$.cursos[0].prerrequisitos[0].tipoRegla").value("HARD"))
            .andExpect(jsonPath("$.cursos[0].prerrequisitos[0].cumplido").value(true));
    }

    @Test
    @DisplayName("/progreso no expone endpoints POST, PUT ni DELETE")
    void progreso_noDebeExponerEndpointsMutables() throws Exception {
        Authentication admin = auth(1, "ROLE_ADMIN");

        mockMvc.perform(post(BASE + "/me").with(authentication(admin)))
            .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put(BASE + "/estudiante/10").with(authentication(admin)))
            .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete(BASE + "/estudiante/10").with(authentication(admin)))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: estudiante inexistente retorna 404 para ADMIN")
    void obtenerProgresoEstudiante_conAdminYEstudianteInexistente_debeRetornarNotFound() throws Exception {
        when(historialProgresoSecurity.puedeVerProgreso(any(Authentication.class), eq(999))).thenReturn(true);
        when(historialProgresoService.calcularProgreso(999)).thenThrow(new EstudianteNotFoundException(999));

        mockMvc.perform(get(BASE + "/estudiante/999").with(authentication(auth(1, "ROLE_ADMIN"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /progreso/estudiante/{id}: identificador inválido retorna 400")
    void obtenerProgresoEstudiante_conIdInvalido_debeRetornarBadRequest() throws Exception {
        mockMvc.perform(get(BASE + "/estudiante/abc").with(authentication(auth(1, "ROLE_ADMIN"))))
            .andExpect(status().isBadRequest());
    }

    private Authentication auth(Integer usuarioId, String role) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioId);
        usuario.setNombre("Usuario");
        usuario.setApellido("Prueba");
        usuario.setEmail("usuario" + usuarioId + "@test.com");
        usuario.setEstado(true);
        return new UsernamePasswordAuthenticationToken(
            usuario,
            null,
            List.of(new SimpleGrantedAuthority(role))
        );
    }

    private HistorialProgresoResponseDto response(Integer estudianteId) {
        return HistorialProgresoResponseDto.builder()
            .estudiante(EstudianteResumenDto.builder()
                .id(estudianteId)
                .codigo("20240010")
                .nombres("Ana")
                .apellidos("Torres")
                .build())
            .carrera(CarreraResumenDto.builder()
                .id(3)
                .nombre("Ingeniería de Sistemas")
                .creditosTotales(200)
                .build())
            .resumen(ProgresoResumenDto.builder()
                .totalCursos(48)
                .cursosAprobados(20)
                .cursosEnProgreso(3)
                .cursosPendientes(25)
                .creditosAprobados(80)
                .creditosRestantes(120)
                .promedioPonderado(new BigDecimal("13.13"))
                .porcentajeAvance(new BigDecimal("40.00"))
                .build())
            .cursos(List.of(CursoProgresoDto.builder()
                .cursoId(101)
                .codigo("MAT101")
                .nombre("Matemática I")
                .cicloRecomendado(1)
                .obligatorio(true)
                .creditos(4)
                .estado(EstadoCursoProgreso.PASSED)
                .notaFinal(new BigDecimal("15.20"))
                .prerrequisitos(List.of(PrerrequisitoProgresoDto.builder()
                    .cursoId(100)
                    .codigo("MAT100")
                    .nombre("Matemática Básica")
                    .tipoRegla(TipoReglaPrerrequisito.HARD)
                    .cumplido(true)
                    .build()))
                .build()))
            .build();
    }

    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
                    .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(403))
                );
            return http.build();
        }
    }
}
