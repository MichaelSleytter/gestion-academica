package com.example.gestionacademica.auth.dto;

/**
 * DTO de respuesta al crear un usuario y enviar sus credenciales por correo.
 *
 * @param idUsuario identificador del usuario recién creado
 * @param email     correo institucional asignado
 * @param message   mensaje informativo sobre el envío de credenciales
 */
public record CrearUsuarioConCredencialesResponse(
        Integer idUsuario,
        String email,
        String message
) {
}
