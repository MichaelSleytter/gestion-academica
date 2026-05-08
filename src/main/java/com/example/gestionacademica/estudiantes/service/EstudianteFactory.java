package com.example.gestionacademica.estudiantes.service;

import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import org.springframework.stereotype.Component;

@Component
public class EstudianteFactory {

    public Estudiante crearDesdeComando(EstudianteCrearDTO comando) {
        Estudiante e = new Estudiante();
        e.setCiclo(comando.getCiclo());
        // El código de estudiante se genera automáticamente en el servicio
        // El estado académico se define como ACTIVO al crear
        return e;
    }
}
