package com.example.gestionacademica.administradores.services;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public void crearAdministradorSiNoExiste() {
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail("arley.ticse@gmail.com");

        if (adminExistente.isEmpty()) {

            TipoDocumento tipoDocumento = tipoDocumentoRepository.findByNombreIgnoreCase("DNI")
                    .orElseGet(() -> {
                        TipoDocumento nuevoTipo = new TipoDocumento();
                        nuevoTipo.setNombre("DNI");
                        return tipoDocumentoRepository.save(nuevoTipo);
                    });

            Rol adminRol = rolRepository.findByNombreIgnoreCase("ADMIN").orElseGet(() -> {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre("ADMIN");
                return rolRepository.save(nuevoRol);
            });

            Usuario admin = new Usuario();
            admin.setNombre("admin");
            admin.setApellido("admin");
            admin.setEmail("arley.ticse@gmail.com");
            admin.setPassword(passwordEncoder.encode("@dmin123!"));
            admin.setNumeroDocumento("71234334");
            admin.setTipoDocumento(tipoDocumento);
            admin.setRoles(Collections.singletonList(adminRol));

            usuarioRepository.save(admin);
        }
    }
}
