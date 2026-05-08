package com.example.gestionacademica.config;

import com.example.gestionacademica.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security.
 * Define: CORS, CSRF, endpoints públicos/protegidos, filtro JWT, sesiones stateless.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

/**
     * Configura la cadena de filtros de seguridad.
     * Define: CORS, CSRF deshabilitado, endpoints públicos, sesiones stateless.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh")
                        .permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"No autorizado\", \"message\": \"" +
                                    authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\": \"Prohibido\", \"message\": \"" +
                                    accessDeniedException.getMessage() + "\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // =========================================================================
                // 5. AÑADIR EL FILTRO JWT A LA CADENA
                // =========================================================================
                // Añadimos nuestro filtro JwtAuthenticationFilter antes del filtro
                // estándar de UsernamePasswordAuthenticationFilter.
                //
                // ¿POR QUÉ ANTES?
                // Porque queremos que el JWT se valide ANTES de que Spring Security
                // intente hacer su autenticación estándar.
                //
                // Orden de filtros en Spring Security:
                // 1. WebAsyncManagerIntegrationFilter
                // 2. SecurityContextPersistenceFilter
                // 3. HeaderWriterFilter
                // 4. CorsFilter ← CORS se maneja aquí
                // 5. CsrfFilter ← Deshabilitado
                // 6. LogoutFilter
                // 7. JwtAuthenticationFilter ← NUESTRO FILTRO
                // 8. UsernamePasswordAuthenticationFilter
                // 9. ... más filtros ...
                //
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // =========================================================================
                // 6. CONFIGURAR EL MANEJADOR DE EXCEPCIONES
                // =========================================================================
                .exceptionHandling(exception -> exception
                        // Qué retornar cuando hay error de autenticación (401)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"No autorizado\", \"message\": \"" +
                                    authException.getMessage() + "\"}");
                        })
                        // Qué retornar cuando no tienes permisos (403)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\": \"Prohibido\", \"message\": \"" +
                                    accessDeniedException.getMessage() + "\"}");
                        }));

        return http.build();
    }

    // ==========================================================================
    // BEANS DE CONFIGURACIÓN
    // ==========================================================================

    /**
     * Password Encoder - Para verificar passwords.
     *
     * ¿QUÉ HACE?
     * BCrypt es un algoritmo de hashing diseñado específicamente para passwords.
     * Características:
     * - Lento por diseño (resistente a brute force)
     * - Genera salt automáticamente (protege contra rainbow tables)
     * - Factor de trabajo configurable (10 rounds por defecto)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Authentication Manager - Expuesto para usar en AuthController.
     *
     * ¿POR QUÉ LO NECESITAMOS?
     * Porque en el controller de login, queremos autenticar
     * el usuario antes de generar el JWT.
     *
     * IMPORTANTE: Spring Security automáticamente usa CustomUserDetailsService
     * y PasswordEncoder para crear el DaoAuthenticationProvider internally.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
