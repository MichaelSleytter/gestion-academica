package com.example.gestionacademica.auth.controller;

import com.example.gestionacademica.auth.domain.RefreshToken;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.dto.AuthRequest;
import com.example.gestionacademica.auth.dto.AuthResponse;
import com.example.gestionacademica.auth.dto.ForgotPasswordRequest;
import com.example.gestionacademica.auth.dto.ResetPasswordRequest;
import com.example.gestionacademica.auth.service.JwtService;
import com.example.gestionacademica.auth.service.PasswordResetService;
import com.example.gestionacademica.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Login, refresh, logout y recuperación de contraseña")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final PasswordResetService passwordResetService;

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
        refreshCookie.setSecure(false); // TODO: true en producción (HTTPS)
        refreshCookie.setPath("/api/v1/auth/refresh"); // Debe coincidir con @RequestMapping
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días en segundos
        refreshCookie.setAttribute("SameSite", "Lax");
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
        // PASO 4: Rotar refresh token (emitir nuevo, marcar viejo como usado)
        // ============================================================
        // Esto previene ataques de replay: el viejo token ya no sirve,
        // y el frontend recibe un nuevo token en la cookie.
        jwtService.markRefreshTokenAsUsed(storedToken);
        RefreshToken newRefreshToken = jwtService.generateRefreshToken(usuario);

        Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken.getToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // TODO: true en producción
        refreshCookie.setPath("/api/v1/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 días
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

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
        emptyCookie.setSecure(false); // TODO: true en producción
        emptyCookie.setPath("/api/v1/auth/refresh");
        emptyCookie.setMaxAge(0); // Expira inmediatamente
        response.addCookie(emptyCookie);

        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }

    /**
     * FORGOT PASSWORD - Inicia el proceso de recuperación de contraseña.
     *
     * <p>Por seguridad, SIEMPRE retorna el mismo mensaje de éxito
     * independientemente de si el email existe en el sistema. Esto evita
     * que un atacante pueda enumerar usuarios válidos.</p>
     *
     * <p>Si el email existe, se genera un token, se persiste y se envía un
     * correo con el enlace de recuperación. El token expira en 30 minutos.</p>
     */
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar recuperación de contraseña",
            description = "Envía un correo con un enlace para restablecer la contraseña. "
                    + "Por seguridad, siempre retorna el mismo mensaje.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje genérico de éxito"),
            @ApiResponse(responseCode = "400", description = "Email inválido",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.solicitarReset(
                    request.email(),
                    "http://localhost:4200/reset-password");
        } catch (RuntimeException ex) {
            log.warn("Fallo controlado en forgot-password para {}: {}", request.email(), ex.getMessage());
        }
        return ResponseEntity.ok(Map.of(
                "message",
                "Si el email existe, recibirá instrucciones para restablecer su contraseña"));
    }

    /**
     * RESET PASSWORD - Restablece la contraseña usando un token válido.
     *
     * <p>El token llega al usuario por correo. Si es válido, no está usado
     * y no está expirado, se actualiza la contraseña (codificada con BCrypt)
     * y se marca el token como consumido. Se envía un correo de confirmación.</p>
     */
    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña con token",
            description = "Consume un token de reset y actualiza la contraseña del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Token inválido, usado o expirado",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetearPassword(request.token(), request.nuevaPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
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
