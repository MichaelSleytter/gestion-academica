package com.example.gestionacademica.mappers;

import com.example.gestionacademica.dto.EstudianteRequestDTO;
import com.example.gestionacademica.dto.EstudianteResponseDTO;
import com.example.gestionacademica.entities.Usuario;
import org.springframework.stereotype.Component;

/**
 * Mapper encargado de las conversiones relacionadas con Usuario.
 */
@Component
public class UsuarioMapper {

    /**
     * Convierte una solicitud de estudiante en una entidad usuario base.
     *
     * @param solicitud datos de entrada para crear el usuario
     * @return entidad usuario inicializada con los campos de la solicitud
     */
    public Usuario desdeSolicitud(EstudianteRequestDTO solicitud) {
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.getNombre());
        usuario.setApellido(solicitud.getApellido());
        usuario.setNumeroDocumento(solicitud.getNumeroDocumento());
        usuario.setEmailPersonal(solicitud.getEmailPersonal());
        return usuario;
    }

    /**
     * Copia datos del {@link Usuario} al builder del {@link EstudianteResponseDTO}.
     *
     * @param usuario usuario fuente
     * @param constructorRespuesta constructor del DTO destino
     */
    public void mapearUsuarioAConstructorRespuesta(
        Usuario usuario,
        EstudianteResponseDTO.EstudianteResponseDTOBuilder constructorRespuesta
    ) {
        if (usuario == null) return;
        constructorRespuesta
            .nombre(usuario.getNombre())
            .apellido(usuario.getApellido())
            .email(usuario.getEmail())
            .numeroDocumento(usuario.getNumeroDocumento())
            .estado(usuario.getEstado());

        if (usuario.getTipoDocumento() != null) {
            constructorRespuesta.tipoDocumento(
                usuario.getTipoDocumento().getNombre()
            );
        }
    }
}
