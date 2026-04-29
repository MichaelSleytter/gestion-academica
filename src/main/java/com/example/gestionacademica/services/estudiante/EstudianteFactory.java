package com.example.gestionacademica.services.estudiante;

import com.example.gestionacademica.dto.EstudianteCrearDTO;
import com.example.gestionacademica.entities.Estudiante;
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
