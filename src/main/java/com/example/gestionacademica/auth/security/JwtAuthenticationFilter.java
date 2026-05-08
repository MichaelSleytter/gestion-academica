package com.example.gestionacademica.auth.security;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.service.JwtService;
import com.example.gestionacademica.auth.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticación JWT.
 *
 * ¿QUÉ HACE?
 * -----------
 * Este filtro se ejecuta EN CADA REQUEST que llega al servidor.
 * Intercepta el request, extrae el JWT del header Authorization,
 * lo valida, y si es válido, configura el contexto de seguridad
 * de Spring para que el request "sepa" quién es el usuario.
 *
 *
 * ¿POR QUÉ es un filtro?
 * ----------------------
 * Los filtros son el nivel más bajo de la cadena de procesamiento
 * de HTTP en Java EE/Spring. Se ejecutan ANTES de los controllers.
 *
 * Request → Filter → Controller → Service → Repository
 *
 * Esto nos permite:
 * - Interceptar TODOS los requests
 * - Validar el token ANTES de que llegue al controller
 * - Rechazar requests no autenticados inmediatamente
 *
 *
 * ¿CÓMO SE INSERTA EN LA CADENA?
 * ------------------------------
 * Lo registramos en SecurityConfig como parte del filter chain.
 * Spring Security lo ejecutará en cada request que coincida con
 * la configuración (generalmente todas las requests).
 *
 *
 * FLUJO DE VALIDACIÓN
 * -------------------
 * 1. Extraer header "Authorization: Bearer <token>"
 * 2. Si no existe header → request anónimo, continuar sin auth
 * 3. Si existe → extraer el token
 * 4. Validar el token (firma, expiración)
 * 5. Si inválido → continuar como anónimo (o rechazar, según config)
 * 6. Si válido → extraer userId del token
 * 7. Cargar usuario de la BD (para verificar que sigue activo, etc.)
 * 8. Crear Authentication object con los authorities (roles)
 * 9. Configurar SecurityContext con la Authentication
 * 10. Continuar con el filter chain
 *
 *
 * SEGURIDAD
 * ----------
 * ¿Por qué cargar el usuario de la BD en cada request?
 * Para verificar que:
 * - El usuario sigue existiendo
 * - El usuario sigue activo (estado = true)
 * - Los roles no han cambiado desde que se emitió el token
 *
 * Aunque esto agrega una query a la BD por request, es importante
 * para la seguridad. El token JWT puede estar "vivo" pero el usuario
 * podría haber sido desactivado o sus roles cambiados.
 *
 * OPTIMIZACIÓN: En una app grande, podrías cachear el usuario
 * en Redis o simplemente verificar el estado en el token (menos seguro).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    /**
     * Método principal que se ejecuta en cada request.
     *
     * @param request     La petición HTTP
     * @param response    La respuesta HTTP
     * @param filterChain La cadena de filtros (para continuar)
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // ================================================================
        // PASO 1: Extraer el token del header Authorization
        // ================================================================
        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", continuar sin auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer solo el token (sin "Bearer ")
        final String jwt = authHeader.substring(7);

        // ================================================================
        // PASO 2: Validar el token
        // ================================================================
        if (!jwtService.validateToken(jwt)) {
            // Token inválido (expirado, malformado, firma incorrecta)
            // Opciones: rechazar con 401, o continuar como anónimo
            // Aquí continuamos como anónimo (el endpoint decidirá si rechazar)
            filterChain.doFilter(request, response);
            return;
        }

        // ================================================================
        // PASO 3: Extraer información del usuario del token
        // ================================================================
        Integer userId;
        try {
            userId = jwtService.extractUserId(jwt);
        } catch (Exception e) {
            // No pudimos extraer el userId del token
            filterChain.doFilter(request, response);
            return;
        }

        // ================================================================
        // PASO 4: Verificar que NO hay una autenticación previa
        // ================================================================
        // Evitamos sobreescribir una autenticación ya existente
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ================================================================
        // PASO 5: Cargar el usuario de la BD para verificación
        // ================================================================
        // Hacemos esto para asegurarnos que el usuario:
        // - Existe
        // - Está activo
        // - Tiene los roles correctos
        Usuario usuario;
        try {
            usuario = usuarioService.buscarPorId(userId);
        } catch (Exception e) {
            // Usuario no encontrado - probablemente fue eliminado
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar que el usuario esté activo
        if (!usuario.getEstado()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ================================================================
        // PASO 6: Extraer roles y crear authorities de Spring Security
        // ================================================================
        List<String> roles = jwtService.extractRoles(jwt);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        // ================================================================
        // PASO 7: Crear Authentication token y configurar contexto
        // ================================================================
        // UsernamePasswordAuthenticationToken es la implementación
        // estándar de Authentication en Spring Security.
        //
        // Parámetros:
        // - principal: el objeto del usuario (puede ser username, email, o el objeto
        // Usuario)
        // - credentials: la contraseña (null porque ya está en el token)
        // - authorities: los permisos/roles del usuario
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                usuario, // principal
                null, // credentials (no las necesitamos)
                authorities // roles/permisos
        );

        // Configurar detalles adicionales del request
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // IMPORTANTE: Establecer el token en el SecurityContext
        // Esto hace que el request actual "sepa" que está autenticado
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // ================================================================
        // PASO 8: Continuar con la cadena de filtros
        // ================================================================
        filterChain.doFilter(request, response);
    }

    /**
     * Controla en qué URLs se aplica este filtro.
     *
     * ¿POR QUÉ no aplicar a TODAS las URLs?
     * Porque有一些请求不需要认证:
     * - /api/auth/login (primero autenticarse)
     * - /api/auth/register (registro de nuevos usuarios)
     * - /api/public/* (endpoints públicos)
     * - /swagger-ui/* (documentación)
     *
     * Aquí retornamos true (SÍ aplicar) para la mayoría de URLs.
     * Configuramos las excepciones en SecurityConfig.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        // NO aplicar filtro a estos paths (porque aún no existe token)
        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register");
    }
}
