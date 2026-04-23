package com.example.gestionacademica.mappers;

import com.example.gestionacademica.dto.DocenteResponseDTO;
import com.example.gestionacademica.entities.Docente;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades {@link Docente} a DTOs de respuesta.
 */
@Component
public class DocenteMapper {

    private final UsuarioMapper usuarioMapper;

    public DocenteMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Convierte una entidad docente a su representación de respuesta.
     *
     * @param docente entidad de origen
     * @return DTO de respuesta o {@code null} si la entrada es nula
     */
    public DocenteResponseDTO aDto(Docente docente) {
        if (docente == null) return null;

        DocenteResponseDTO.DocenteResponseDTOBuilder constructorRespuesta =
            DocenteResponseDTO.builder()
                .idUsuario(docente.getIdUsuario())
                .especialidad(docente.getEspecialidad())
                .idGrado(
                    docente.getGradoAcademico() != null
                        ? docente.getGradoAcademico().getIdGrado()
                        : null
                )
                .nombreGrado(
                    docente.getGradoAcademico() != null
                        ? docente.getGradoAcademico().getNombre()
                        : null
                );

        usuarioMapper.mapearUsuarioAConstructorRespuesta(
            docente.getUsuario(),
            constructorRespuesta
        );

        return constructorRespuesta.build();
    }
}
