package com.example.gestionacademica.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gestionacademica.auth.domain.RefreshToken;
import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.repository.RefreshTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Servicio para manejar la creación y validación de tokens JWT.
 * 
 * Proporciona métodos para:
 * - Generar Access Token (15 min) y Refresh Token (7 días)
 * - Validar tokens JWT
 * - Extraer información del token (userId, email, roles)
 * - Manejar refresh y revocación de tokens
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    // ==========================================================================
    // CONFIGURACIÓN - Inyectada desde application.yml
    // ==========================================================================

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-days}")
    private int refreshTokenExpirationDays;

    // ==========================================================================
    // DEPENDENCIAS
    // ==========================================================================

    private final RefreshTokenRepository refreshTokenRepository;

    // ==========================================================================
    // CLAVE PARA FIRMAR TOKENS
    // ==========================================================================

    /**
     * La clave secreta para firmar los JWT.
     *
     * ¿POR QUÉ usar Keys.secretKeyFor()?
     * JJWT necesita una clave de un tamaño mínimo para ser segura.
     * Usando HS256 (HMAC-SHA256), necesitamos al menos 256 bits (32 bytes).
     * Keys.secretKeyFor() genera una clave segura del tamaño correcto.
     *
     * IMPORTANTE: En producción, esta clave debe ser:
     * - Mínimo 256 bits
     * - Aleatoria
     * - Guardada en una variable de entorno, NUNCA en el código
     */
    private SecretKey getSigningKey() {
        // Aseguramos que la clave tenga al menos 256 bits para HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Si es muy corta, la padding hasta 32 bytes
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un Access Token JWT para un usuario.
     * El token contiene: userId, email, roles, fecha de creación y expiración.
     */
    public String generateAccessToken(Usuario usuario) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(usuario.getIdUsuario().toString()) // "sub" claim
                .claim("email", usuario.getEmail())
                .claim("roles", usuario.getRoles().stream()
                        .map(Rol::getNombre)
                        .collect(java.util.stream.Collectors.toList()))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey()) // Firma con la clave secreta
                .compact();
    }

    /**
     * Genera un Refresh Token y lo guarda en la BD.
     * El refresh token es un UUID aleatorio que permite obtener nuevos access tokens.
     */
    @Transactional
    public RefreshToken generateRefreshToken(Usuario usuario) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .used(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Valida un Access Token JWT.
     *
     * ¿QUÉ validamos?
     * 1. Que el token esté bien formado
     * 2. Que la firma sea válida (no fue alterado)
     * 3. Que no esté expirado
     *
     * ¿QUÉ NO validamos aquí?
     * - Que el usuario siga existiendo
     * - Que el usuario siga activo
     * - Que los roles no hayan cambiado
     *
     * Estas validaciones se hacen en el filtro de seguridad.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token inválido: puede estar expirado, malformado, o alterado
            return false;
        }
    }

    /**
     * Extrae el user ID del token JWT.
     *
     * ¿POR QUÉ necesitamos esto?
     * Porque después de validar el token, necesitamos saber
     * quién es el usuario para cargar sus datos de la BD.
     */
    public Integer extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extrae el email del token JWT.
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Extrae los roles del token JWT.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * Busca un refresh token y lo valida.
     * Verifica: existe en BD, no expirado, no revocado, no usado.
     */
    public RefreshToken findAndValidateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenWithUsuario(token)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        if (refreshToken.isExpirado()) {
            throw new RuntimeException("Token expirado");
        }

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Token revocado");
        }

        if (refreshToken.isUsed()) {
            // Token ya usado - posible ataque de replay!
            // En producción, deberías loguear esto como warning de seguridad
            throw new RuntimeException("Token ya utilizado");
        }

        return refreshToken;
    }

    /**
     * Marca un refresh token como usado para prevenir replay attacks.
     */
    @Transactional
    public void markRefreshTokenAsUsed(RefreshToken refreshToken) {
        refreshToken.setUsed(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoca un refresh token (para logout).
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.revokeByToken(token);
    }

    /**
     * Revoca TODOS los refresh tokens de un usuario.
     *
     * ¿CUÁNDO usar esto?
     * - Cuando el usuario cambia su contraseña
     * - Cuando el usuario reporta que su cuenta fue comprometida
     * - Como feature de "logout desde todos los dispositivos"
     */
    @Transactional
    public void revokeAllUserTokens(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }

    /**
     * Extrae todos los claims (datos) del token JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
