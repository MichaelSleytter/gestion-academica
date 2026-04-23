package com.example.gestionacademica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI 3.
 *
 * Acceso en navegador:
 *  - Swagger UI:  http://localhost:8080/swagger-ui.html
 *  - JSON specs:  http://localhost:8080/api-docs
 */
@Configuration
public class SwaggerConfig {

        /**
         * Construye la configuración OpenAPI publicada por la aplicación.
         *
         * @return objeto OpenAPI con metadatos de la API
         */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestión Académica Universitaria")
                        .description("""
                                API REST desarrollada por TechNova Solutions.
                                
                                Permite gestionar:
                                - Estudiantes
                                - Docentes
                                - Cursos
                                - Secciones
                                - Matrículas
                                
                                Base de datos: gestion_academica_universitaria (PostgreSQL)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TechNova Solutions")
                                .email("contacto@technova.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}