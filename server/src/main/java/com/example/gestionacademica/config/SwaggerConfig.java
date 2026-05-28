package com.example.gestionacademica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
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
