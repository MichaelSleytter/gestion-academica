package com.example.gestionacademica.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Servicio para validar emails sin enviar correos reales.
 * 
 * Utiliza dos estrategias:
 * 1. Validación de formato (regex)
 * 2. Verificación de registro MX del dominio (consulta DNS)
 */
@Slf4j
@Service
public class EmailValidatorService {

    // Patrón básico para validar formato de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Valida un email usando las estrategias disponibles.
     * 
     * @param email el email a validar
     * @return ResultadoValidacion con el resultado y detalles
     */
    public ResultadoValidacion validar(String email) {
        if (email == null || email.isBlank()) {
            return new ResultadoValidacion(false, "El email no puede estar vacío");
        }

        // Normalizar email
        String emailNormalizado = email.trim().toLowerCase();

        // 1. Validar formato
        if (!EMAIL_PATTERN.matcher(emailNormalizado).matches()) {
            return new ResultadoValidacion(false, "El formato del email es inválido");
        }

        // 2. Extraer dominio
        String dominio = emailNormalizado.substring(emailNormalizado.indexOf("@") + 1);

        // 3. Verificar si el dominio tiene registros MX
        boolean tieneMX = verificarDominioMX(dominio);

        if (!tieneMX) {
            log.warn("El dominio {} no tiene registros MX - email potencialmente inválido", dominio);
            return new ResultadoValidacion(false, 
                    "El dominio '" + dominio + "' no acepta correos electrónicos");
        }

        return new ResultadoValidacion(true, "Email válido");
    }

    /**
     * Verifica si un dominio tiene registros MX (indica que acepta emails).
     * 
     * @param dominio el dominio a verificar (ej: gmail.com)
     * @return true si tiene registros MX
     */
    private boolean verificarDominioMX(String dominio) {
        try {
            // Consultar registros MX del dominio
            // Nota: Esta es una consulta simple, no verifica si el邮箱 específico existe
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns:");
            
            DirContext ctx = new InitialDirContext(env);
            javax.naming.directory.Attributes attrs = ctx.getAttributes(dominio, new String[]{"MX"});
            
            javax.naming.directory.Attribute mxAttr = attrs.get("MX");
            boolean tieneMX = mxAttr != null && mxAttr.size() > 0;
            
            ctx.close();
            return tieneMX;
            
        } catch (Exception e) {
            // Si falla la consulta DNS, asumimos que podría ser válido
            log.debug("No se pudo verificar dominio MX para {}: {}", dominio, e.getMessage());
            return true; // En caso de duda, permitir
        }
    }

    /**
     * Resultado de la validación de email.
     */
    public record ResultadoValidacion(boolean esValido, String mensaje) {
        public boolean esValido() {
            return esValido;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
}