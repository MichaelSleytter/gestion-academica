package com.example.gestionacademica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración independiente para beans de codificación.
 * <p>
 * Separada de {@link SecurityConfig} para evitar dependencias circulares
 * entre {@code SecurityConfig → JwtAuthenticationFilter → UsuarioService → PasswordEncoder}.
 * </p>
 */
@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
