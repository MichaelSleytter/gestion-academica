package com.example.gestionacademica.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para restablecer la contraseña usando un token.
 *
 * @param token          token UUID recibido por correo
 * @param nuevaPassword  nueva contraseña en texto plano (será codificada por el servicio)
 */
public record ResetPasswordRequest(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String nuevaPassword
) {
}
