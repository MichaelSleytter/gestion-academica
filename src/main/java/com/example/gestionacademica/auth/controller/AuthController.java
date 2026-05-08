package com.example.gestionacademica.auth.controller;

import com.example.gestionacademica.auth.domain.RefreshToken;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.dto.AuthRequest;
import com.example.gestionacademica.auth.dto.AuthResponse;
import com.example.gestionacademica.auth.service.JwtService;
import com.example.gestionacademica.auth.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * Controller para manejar autenticación.
 *
 * ENDPOINTS:
 * - POST /api/auth/login → Login con email y password
 * - POST /api/auth/refresh → Refrescar el access token
 * - POST /api/auth/logout → Invalidar el refresh token
 *
 *
 * FLUJO DE DATOS
 * --------------
 *
 * LOGIN:
 * Request:
 * {
 * "email": "usuario@ejemplo.com",
 * "password": "miPassword123"
 * }
 *
 * Response (200 OK):
 * {
 * "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 * "tokenType": "Bearer",
 * "expiresIn": 900
 * }
 * + Cookie: refreshToken=abc123...; HttpOnly; Secure; SameSite=Strict
 *
 *
 * REFRESH:
 * Request: (envía la cookie automáticamente)
 * Response (200 OK):
 * {
 * "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." (nuevo),
 * "tokenType": "Bearer",
 * "expiresIn": 900
 * }
 *
 *
 * LOGOUT:
 * Request: (envía la cookie automáticamente)
 * Response (200 OK): { "message": "Sesión cerrada" }
 * + Borra la cookie del refresh token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    /**
     * LOGIN - Autentica usuario y retorna tokens.
     *
     * ¿QUÉ HACE?
     * 1. Valida email + password contra la BD
     * 2. Si es válido, genera Access Token y Refresh Token
     * 3. Guarda el Refresh Token en BD (para poder revocarlo)
     * 4. Retorna el Access Token al frontend
     * 5. Guarda el Refresh Token en una Cookie HttpOnly
     *
     *
     * ¿POR QUÉ Cookie HttpOnly?
     * - HttpOnly: JavaScript NO puede leer la cookie (previene XSS)
     * - El frontend NO necesita leer el refresh token
     * - Solo lo reenvía automáticamente en cada request
     *
     *
     * ¿POR QUÉ retornar el access token en el body?
     * Porque Angular necesita leerlo para usarlo en el Authorization header.
     * Lo guarda en memoria (NO en localStorage) para seguridad.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request,
            HttpServletResponse response) {

        // ============================================================
        // PASO 1: Autenticar credenciales
        // ============================================================
        // AuthenticationManager intenta cargar el usuario y verificar password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        // ============================================================
        // PASO 2: Obtener el usuario completo con roles
        // ============================================================
        // Authentication.getPrincipal() nos da el UserDetails cargado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioService.obtenerPorEmail(request.getEmail());

        // ============================================================
        // PASO 3: Generar tokens
        // ============================================================
        String accessToken = jwtService.generateAccessToken(usuario);
        RefreshToken refreshToken = jwtService.generateRefreshToken(usuario);

        // ============================================================
        // PASO 4: Guardar refresh token en Cookie HttpOnly
        // ============================================================
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshCookie.setHttpOnly(true); // JavaScript no puede leerla
        refreshCookie.setSecure(true); // Solo HTTPS (en producción)
        refreshCookie.setPath("/api/auth/refresh"); // Solo se envía a este endpoint
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días en segundos
        refreshCookie.setAttribute("SameSite", "Strict");
        response.addCookie(refreshCookie);

        // ============================================================
        // PASO 5: Retornar respuesta
        // ============================================================
        // Solo retornamos el ACCESS token porque el refresh va en cookie
        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                "Bearer",
                900 // 15 minutos en segundos
        ));
    }

    /**
     * REFRESH - Genera un nuevo Access Token usando el Refresh Token.
     *
     * ¿QUÉ HACE?
     * 1. Lee el Refresh Token de la cookie
     * 2. Valida que el token exista y no esté revoked/expired
     * 3. Genera un NUEVO Access Token
     * 4. Marca el Refresh Token como "usado" (previene replay)
     * 5. Retorna el nuevo Access Token
     *
     *
     * ¿POR QUÉ marcar como "usado"?
     * Para detectar ataques de replay.
     * Si alguien intercepta el refresh token y lo usa, el legítimo no podrá.
     * Esto nos alerta de un posible compromiso.
     *
     *
     * ¿POR QUÉ no generar un nuevo Refresh Token aquí?
     * Es una decisión de diseño. Aquí usamos "refresh token rotation":
     * - Cada refresh genera UN NUEVO access token
     * - Solo cuando el refresh token expira, el usuario necesita hacer login
     *
     * Otra opción sería rotar también el refresh token en cada refresh.
     * Eso es más seguro pero más complejo (ver "refresh token rotation" en OWASP).
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        // ============================================================
        // PASO 1: Extraer refresh token de la cookie
        // ============================================================
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No se encontró refresh token"));
        }

        // ============================================================
        // PASO 2: Validar el refresh token
        // ============================================================
        RefreshToken storedToken;
        try {
            storedToken = jwtService.findAndValidateRefreshToken(refreshToken);
        } catch (RuntimeException e) {
            // Token inválido, expirado, o ya usado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }

        // ============================================================
        // PASO 3: Generar nuevo access token
        // ============================================================
        Usuario usuario = storedToken.getUsuario();
        String newAccessToken = jwtService.generateAccessToken(usuario);

        // ============================================================
        // PASO 4: Marcar refresh token como usado
        // ============================================================
        // Esto previene que el mismo token se use dos veces
        // (protección contra replay attacks)
        jwtService.markRefreshTokenAsUsed(storedToken);

        // ============================================================
        // PASO 5: Retornar nuevo access token
        // ============================================================
        return ResponseEntity.ok(new AuthResponse(
                newAccessToken,
                "Bearer",
                900 // 15 minutos
        ));
    }

    /**
     * LOGOUT - Invalida el refresh token actual.
     *
     * ¿QUÉ HACE?
     * 1. Lee el Refresh Token de la cookie
     * 2. Lo marca como "revocado" en la BD
     * 3. Borra la cookie del cliente
     * 4. Retorna confirmación
     *
     *
     * ¿QUÉ NO HACE?
     * - No invalidar el Access Token (seguirá funcionando hasta que expire)
     * - Esto es normal porque el Access Token es de corta duración
     * - Si quisieras invalidarlo inmediatamente, necesitarías una blacklist
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null) {
            // Revocamos el refresh token en la BD
            jwtService.revokeRefreshToken(refreshToken);
        }

        // Borramos la cookie
        Cookie emptyCookie = new Cookie("refreshToken", null);
        emptyCookie.setHttpOnly(true);
        emptyCookie.setSecure(true);
        emptyCookie.setPath("/api/auth/refresh");
        emptyCookie.setMaxAge(0); // Expira inmediatamente
        response.addCookie(emptyCookie);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    // ==========================================================================
    // MÉTODOS HELPER
    // ==========================================================================

    /**
     * Extrae el refresh token de las cookies.
     *
     * ¿POR QUÉ buscar en cookies?
     * Porque el refresh token se guarda ahí para que JavaScript no pueda
     * leerlo (protección XSS). Solo se envía automáticamente al servidor.
     */
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
