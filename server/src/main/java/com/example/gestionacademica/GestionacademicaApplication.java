package com.example.gestionacademica;


import com.example.gestionacademica.administradores.services.AdministradorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal del Sistema de Gestión Académica Universitaria.
 *
 * <p>
 * TechNova Solutions - Avance 1
 * </p>
 *
 * <p>
 * Inicia el contexto de Spring Boot y levanta el servidor embebido Tomcat
 * en el puerto configurado en application.yaml (default: 8080).
 * </p>
 *
 * @author TechNova Solutions
 * @version 1.0.0
 */
@SpringBootApplication
public class GestionacademicaApplication {

    /**
     * Punto de entrada de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(GestionacademicaApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(AdministradorService administradorService) {
        return args -> {
            administradorService.crearAdministradorSiNoExiste();
        };
    }
}
