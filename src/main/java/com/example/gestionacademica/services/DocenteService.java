package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Docente;
import com.example.gestionacademica.entities.GradoAcademico;
import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de logica de negocio para Docente.
 */
@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final GradoAcademicoRepository gradoAcademicoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PasswordEncoder codificadorContrasena;

    /**
     * Lista todos los docentes.
     */
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    /**
     * Busca un docente por ID.
     */
    public Docente buscarPorId(Integer id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Docente no encontrado con ID: " + id));
    }

    /**
     * Lista docentes por especialidad.
     */
    public List<Docente> listarPorEspecialidad(String especialidad) {
        return docenteRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    /**
     * Lista docentes por grado academico.
     */
    public List<Docente> listarPorGrado(Integer idGrado) {
        return docenteRepository.findByGradoAcademico_IdGrado(idGrado);
    }

    /**
     * Crea un nuevo docente con su usuario base.
     */
    @Transactional
        public Docente crear(
        Usuario usuario,
        Docente docente,
        Integer idGrado,
        Integer idTipoDocumento
        ) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException(
                "Ya existe un usuario con el email: " + usuario.getEmail());
        }

        if (usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new RuntimeException(
                "Ya existe un usuario con el documento: " + usuario.getNumeroDocumento());
        }

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(idTipoDocumento)
            .orElseThrow(() -> new RuntimeException(
                "Tipo de documento no encontrado con ID: " + idTipoDocumento));

        GradoAcademico grado = gradoAcademicoRepository.findById(idGrado)
            .orElseThrow(() -> new RuntimeException(
                "Grado academico no encontrado con ID: " + idGrado));

        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        usuario.setPassword(codificadorContrasena.encode(usuario.getPassword()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        docente.setUsuario(usuarioGuardado);
        docente.setGradoAcademico(grado);
        return docenteRepository.save(docente);
        }

    /**
     * Actualiza datos de un docente.
     */
    @Transactional
    public Docente actualizar(Integer id, Docente datos, Integer idGrado) {

        Docente existente = buscarPorId(id);

        GradoAcademico grado = gradoAcademicoRepository.findById(idGrado)
                .orElseThrow(() -> new RuntimeException(
                        "Grado academico no encontrado con ID: " + idGrado));

        existente.setEspecialidad(datos.getEspecialidad());
        existente.setGradoAcademico(grado);

        return docenteRepository.save(existente);
    }

    /**
     * Elimina un docente por ID.
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!docenteRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Docente no encontrado con ID: " + id);
        }
        docenteRepository.deleteById(id);
    }
}