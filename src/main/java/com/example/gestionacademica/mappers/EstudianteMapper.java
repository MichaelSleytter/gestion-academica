package com.example.gestionacademica.mappers;

import com.example.gestionacademica.dto.EstudianteResponseDTO;
import com.example.gestionacademica.entities.Estudiante;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades {@link Estudiante} a DTOs de respuesta.
 */
@Component
public class EstudianteMapper {

    private final UsuarioMapper usuarioMapper;

    /**
     * Crea una instancia del mapper con sus dependencias.
     *
     * @param usuarioMapper mapper usado para trasladar datos del usuario asociado
     */
    public EstudianteMapper(UsuarioMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Convierte una entidad estudiante a su representación de respuesta.
     *
     * @param estudiante entidad de origen
     * @return DTO de respuesta o {@code null} si la entrada es nula
     */
    public EstudianteResponseDTO aDto(Estudiante estudiante) {
        if (estudiante == null) return null;

        EstudianteResponseDTO.EstudianteResponseDTOBuilder constructorRespuesta =
            EstudianteResponseDTO.builder()
                .idUsuario(estudiante.getIdUsuario())
                .codigoEstudiante(estudiante.getCodigoEstudiante())
                .ciclo(estudiante.getCiclo())
                .estadoAcademico(estudiante.getEstadoAcademico())
                .idCarrera(
                    estudiante.getCarrera() != null
                        ? estudiante.getCarrera().getIdCarrera()
                        : null
                )
                .nombreCarrera(
                    estudiante.getCarrera() != null
                        ? estudiante.getCarrera().getNombre()
                        : null
                );

        usuarioMapper.mapearUsuarioAConstructorRespuesta(
            estudiante.getUsuario(),
            constructorRespuesta
        );

        return constructorRespuesta.build();
    }
}
