package com.example.gestionacademica.services.estudiante;

import com.example.gestionacademica.dto.EstudianteCrearDTO;
import com.example.gestionacademica.entities.Usuario;
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
