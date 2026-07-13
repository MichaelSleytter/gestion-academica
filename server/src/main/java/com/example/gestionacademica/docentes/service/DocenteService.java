package com.example.gestionacademica.docentes.service;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.repository.EspecializacionRepository;
import com.example.gestionacademica.catalogos.repository.GradoAcademicoRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final EspecializacionRepository especializacionRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder codificadorContrasena;

    /**
     * Lista todos los docentes.
     */
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    /**
     * Busca docentes con paginación y filtro de búsqueda opcional.
     * <p>
     * La búsqueda se aplica sobre: nombre, apellido, número de documento,
     * correo electrónico y especialidad.
     *
     * @param busqueda   texto para filtrar (opcional)
     * @param paginacion objeto con página, tamaño y ordenamiento
     * @return página de docentes que coinciden con el filtro
     */
    public Page<Docente> listarPaginado(String busqueda, Pageable paginacion) {
        Specification<Docente> especificacion = (root, query, cb) -> {
            if (busqueda == null || busqueda.isBlank()) {
                return cb.conjunction();
            }

            String patron = "%" + busqueda.toLowerCase() + "%";
            Join<Docente, com.example.gestionacademica.auth.domain.Usuario> usuario =
                    root.join("usuario");

            return cb.or(
                    cb.like(cb.lower(usuario.get("nombre")), patron),
                    cb.like(cb.lower(usuario.get("apellido")), patron),
                    cb.like(cb.lower(usuario.get("numeroDocumento")), patron),
                    cb.like(cb.lower(usuario.get("email")), patron),
                    cb.like(cb.lower(root.get("especialidad")), patron)
            );
        };

        return docenteRepository.findAll(especificacion, paginacion);
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
            Integer idTipoDocumento) {
        return crear(usuario, docente, idGrado, idTipoDocumento, null);
    }

    /**
     * Crea un nuevo docente con su usuario base y especializacion.
     */
    @Transactional
    public Docente crear(
            Usuario usuario,
            Docente docente,
            Integer idGrado,
            Integer idTipoDocumento,
            Integer idEspecializacion) {

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

        Especializacion especializacion = buscarEspecializacion(idEspecializacion);

        Rol rolDocente = rolRepository.findByNombreIgnoreCase("DOCENTE")
                .orElseThrow(() -> new RuntimeException("El rol 'DOCENTE' no existe."));


        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        usuario.setPassword(codificadorContrasena.encode(usuario.getPassword()));
        usuario.setRoles(Collections.singletonList(rolDocente));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        docente.setUsuario(usuarioGuardado);
        docente.setGradoAcademico(grado);
        docente.setEspecializacion(especializacion);
        return docenteRepository.save(docente);
    }

    /**
     * Actualiza datos de un docente.
     */
    @Transactional
    public Docente actualizar(Integer id, Docente datos, Integer idGrado) {
        return actualizar(id, datos, idGrado, null);
    }

    /**
     * Actualiza datos de un docente y su especializacion.
     */
    @Transactional
    public Docente actualizar(Integer id, Docente datos, Integer idGrado, Integer idEspecializacion) {

        Docente existente = buscarPorId(id);

        GradoAcademico grado = gradoAcademicoRepository.findById(idGrado)
                .orElseThrow(() -> new RuntimeException(
                        "Grado academico no encontrado con ID: " + idGrado));

        existente.setEspecialidad(datos.getEspecialidad());
        existente.setGradoAcademico(grado);
        existente.setEspecializacion(buscarEspecializacion(idEspecializacion));

        return docenteRepository.save(existente);
    }

    private Especializacion buscarEspecializacion(Integer idEspecializacion) {
        if (idEspecializacion == null) {
            return null;
        }
        return especializacionRepository.findById(idEspecializacion)
                .orElseThrow(() -> new RuntimeException(
                        "Especializacion no encontrada con ID: " + idEspecializacion));
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
