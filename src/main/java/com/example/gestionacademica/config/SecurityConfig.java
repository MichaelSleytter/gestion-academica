package com.example.gestionacademica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración mínima de PasswordEncoder. Usar delegating encoder para permitir
 * migraciones futuras ({bcrypt} por defecto).
 */
@Configuration
public class SecurityConfig {

    /**
     * Expone el codificador de contraseñas usado por la aplicación.
     *
     * @return implementación de {@link PasswordEncoder} basada en BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
