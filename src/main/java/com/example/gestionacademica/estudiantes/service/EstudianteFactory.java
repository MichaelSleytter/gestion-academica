package com.example.gestionacademica.estudiantes.service;

import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import org.springframework.stereotype.Component;

@Component
public class EstudianteFactory {

    public Estudiante crearDesdeComando(EstudianteCrearDTO comando) {
        Estudiante e = new Estudiante();
        e.setCiclo(comando.getCiclo());
        e.setEstadoAcademico(comando.getEstadoAcademico());
        e.setCodigoEstudiante(comando.getCodigoEstudiante()); // puede ser null y se asegura en el servicio
        return e;
    }
}
