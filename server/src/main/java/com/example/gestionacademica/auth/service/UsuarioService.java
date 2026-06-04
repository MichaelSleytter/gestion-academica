package com.example.gestionacademica.auth.service;

import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link Usuario}.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios.
     *
     * @return usuarios registrados
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador de usuario
     * @return usuario encontrado
     */
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Busca un usuario por email.
     * Alias de {@link #obtenerPorEmail(String)} para consistencia.
     */
    public Usuario findByEmail(String email) {
        return obtenerPorEmail(email);
    }

    /**
     * Busca un usuario por ID.
     * Alias de {@link #buscarPorId(Integer)} para compatibilidad con JwtAuthenticationFilter.
     */
    public Usuario findById(Integer id) {
        return buscarPorId(id);
    }

    /**
     * Crea un usuario asociado a un tipo de documento.
     *
     * @param usuario         datos del usuario
     * @param idTipoDocumento identificador de tipo de documento
     * @return usuario creado
     */
    @Transactional
    public Usuario crear(Usuario usuario, Integer idTipoDocumento) {
        validarCamposBasicos(usuario);
        validarUnicidad(usuario.getEmail(), usuario.getNumeroDocumento());

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        TipoDocumento tipoDocumento = obtenerTipoDocumento(idTipoDocumento);
        usuario.setTipoDocumento(tipoDocumento);
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param id              identificador de usuario
     * @param datos           nuevos datos
     * @param idTipoDocumento identificador de tipo de documento
     * @return usuario actualizado
     */
    @Transactional
    public Usuario actualizar(Integer id, Usuario datos, Integer idTipoDocumento) {
        Usuario existente = buscarPorId(id);
        validarCamposBasicos(datos);

        boolean cambioEmail = !existente.getEmail().equalsIgnoreCase(datos.getEmail());
        if (cambioEmail && usuarioRepository.existsByEmail(datos.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + datos.getEmail());
        }

        boolean cambioDocumento = !existente.getNumeroDocumento().equals(datos.getNumeroDocumento());
        if (cambioDocumento && usuarioRepository.existsByNumeroDocumento(datos.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un usuario con el documento: " + datos.getNumeroDocumento());
        }

        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setEmailPersonal(datos.getEmailPersonal());
        existente.setPassword(datos.getPassword());
        existente.setNumeroDocumento(datos.getNumeroDocumento());
        existente.setEstado(datos.getEstado());
        existente.setTipoDocumento(obtenerTipoDocumento(idTipoDocumento));

        return usuarioRepository.save(existente);
    }

    /**
     * Elimina un usuario por ID.
     *
     * @param id identificador de usuario
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private void validarCamposBasicos(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del usuario es obligatorio.");
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            throw new RuntimeException("El apellido del usuario es obligatorio.");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email del usuario es obligatorio.");
        }
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La password del usuario es obligatoria.");
        }
        if (usuario.getNumeroDocumento() == null || usuario.getNumeroDocumento().trim().isEmpty()) {
            throw new RuntimeException("El numero de documento del usuario es obligatorio.");
        }
    }

    private void validarUnicidad(String email, String numeroDocumento) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con el email: " + email);
        }
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new RuntimeException("Ya existe un usuario con el documento: " + numeroDocumento);
        }
    }

    private TipoDocumento obtenerTipoDocumento(Integer idTipoDocumento) {
        return tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con ID: " + idTipoDocumento));
    }

    public Usuario obtenerPorEmail(String email) {
        // Usar el método que carga los roles (fetch join)
        return usuarioRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }
}
