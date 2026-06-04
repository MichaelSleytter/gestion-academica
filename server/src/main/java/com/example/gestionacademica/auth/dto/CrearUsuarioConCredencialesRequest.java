package com.example.gestionacademica.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de solicitud para crear un usuario y enviarle sus credenciales por correo.
 *
 * @param nombre           nombre del usuario
 * @param apellido         apellido del usuario
 * @param emailPersonal    correo personal opcional (será validado contra MX)
 * @param numeroDocumento  número de documento del usuario
 * @param idTipoDocumento  identificador del tipo de documento
 */
public record CrearUsuarioConCredencialesRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Email(message = "El formato del email personal no es válido")
        String emailPersonal,

        @NotBlank(message = "El número de documento es obligatorio")
        String numeroDocumento,

        @NotNull(message = "El idTipoDocumento es obligatorio")
        Integer idTipoDocumento
) {
}
