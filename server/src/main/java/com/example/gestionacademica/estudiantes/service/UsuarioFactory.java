package com.example.gestionacademica.estudiantes.service;

import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.auth.domain.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioFactory {

    public Usuario crearDesdeComando(EstudianteCrearDTO comando) {
        Usuario u = new Usuario();
        u.setNombre(comando.getNombre());
        u.setApellido(comando.getApellido());
        u.setNumeroDocumento(comando.getNumeroDocumento());
        u.setEmailPersonal(comando.getEmailPersonal());
        // password asignada más adelante por el servicio (codificada)
        return u;
    }
}
