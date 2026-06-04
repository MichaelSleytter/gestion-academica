package com.example.gestionacademica.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para iniciar el proceso de recuperación de contraseña.
 *
 * @param email correo electrónico del usuario que solicita el reset
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email
) {
}
