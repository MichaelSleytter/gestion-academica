package com.example.gestionacademica.auth.service;

import com.example.gestionacademica.auth.domain.PasswordResetToken;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.repository.PasswordResetTokenRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para gestionar el ciclo de vida de tokens de recuperación de contraseña.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int expirationMinutes;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    /**
     * Solicita un reset de contraseña para el email indicado.
     *
     * <p>Por seguridad, no revela si el email existe: si el usuario no se
     * encuentra, el método retorna silenciosamente. Si existe, elimina los
     * tokens previos, genera uno nuevo con fecha de expiración y envía el
     * correo con el enlace de recuperación.</p>
     *
     * @param email correo del usuario que solicita el reset
     */
    @Transactional
    public void solicitarReset(String email) {
        Usuario usuario;
        try {
            usuario = usuarioService.obtenerPorEmail(email);
        } catch (RuntimeException ex) {
            log.warn("Solicitud de reset para email no registrado: {}", email);
            return;
        }

        tokenRepository.deleteByUsuario(usuario);

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenValue)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(expirationMinutes))
                .usado(false)
                .build();
        tokenRepository.save(token);

        String resetUrl = this.frontendBaseUrl + "/reset-password";
        emailService.enviarRecuperacionPassword(
                usuario.getEmail(),
                usuario.getNombre(),
                tokenValue,
                resetUrl);
    }

    /**
     * Restablece la contraseña usando un token válido.
     *
     * <p>Valida que el token exista, no haya sido usado y no esté expirado.
     * Si todo es correcto, codifica la nueva contraseña con BCrypt, la
     * persiste, marca el token como consumido y envía la confirmación.</p>
     *
     * @param token         valor UUID del token recibido por correo
     * @param nuevaPassword nueva contraseña en texto plano (será codificada)
     * @throws RuntimeException si el token no existe, ya fue usado o está expirado
     */
    @Transactional
    public void resetearPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            throw new RuntimeException("El token ya fue utilizado");
        }
        if (resetToken.isExpirado()) {
            throw new RuntimeException("El token ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        emailService.enviarConfirmacionCambioPassword(
                usuario.getEmail(),
                usuario.getNombre());
    }
}
