package com.example.gestionacademica.exceptions;

public class EstudianteNotFoundException extends RuntimeException {

    public EstudianteNotFoundException(Integer id) {
        super("Estudiante no encontrado con ID: " + id);
    }
}
