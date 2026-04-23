package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Carrera;
import com.example.gestionacademica.entities.Estudiante;
import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.enums.EstudianteEstadoAcademico;
import com.example.gestionacademica.repositories.*;
import com.example.gestionacademica.utils.EstudianteUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de negocio para operaciones sobre {@link Estudiante}.
 *
 * <p>Proporciona operaciones CRUD, validaciones de negocio y la generación de
 * datos derivados (correo institucional, contraseña y código de estudiante)
 * cuando corresponde.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    private final PasswordEncoder codificadorContrasena;

    @org.springframework.beans.factory.annotation.Value(
        "${app.student.email.domain:institution.edu.pe}"
    )
    private String dominioCorreoInstitucional;

    /**
     * Retorna todos los estudiantes registrados.
     *
     * @return lista de estudiantes (puede estar vacía)
     */
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    /**
     * Busca un estudiante por su ID (id_usuario).
     *
     * @param id identificador del estudiante
     * @return el estudiante encontrado
     * @throws RuntimeException si no existe
     */
    public Estudiante buscarPorId(Integer id) {
        return estudianteRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Estudiante no encontrado con ID: " + id)
            );
    }

    /**
     * Busca un estudiante por su código único.
     *
     * @param codigoEstudiante código del estudiante
     * @return el estudiante encontrado
     * @throws RuntimeException si no existe
     */
    public Estudiante buscarPorCodigo(String codigoEstudiante) {
        return estudianteRepository
            .findByCodigoEstudiante(codigoEstudiante)
            .orElseThrow(() ->
                new RuntimeException(
                    "Estudiante no encontrado con codigo: " + codigoEstudiante
                )
            );
    }

    /**
     * Lista estudiantes por carrera.
     *
     * @param idCarrera identificador de la carrera
     * @return lista de estudiantes pertenecientes a la carrera
     */
    public List<Estudiante> listarPorCarrera(Integer idCarrera) {
        return estudianteRepository.findByCarrera_IdCarrera(idCarrera);
    }

    /**
     * Lista estudiantes por ciclo.
     *
     * @param ciclo ciclo academico (ej: 1..12)
     * @return lista de estudiantes en el ciclo
     */
    public List<Estudiante> listarPorCiclo(Integer ciclo) {
        return estudianteRepository.findByCiclo(ciclo);
    }

    /**
     * Crea un nuevo estudiante junto con su usuario base.
     *
     * <p>El método valida la unicidad de número de documento y del email
     * institucional generado. Si el {@code usuario} no contiene contraseña se
     * genera una contraeña segura. Si el {@code estudiante} no tiene código se
     * genera uno aleatorio.
     *
     * @param usuario datos del usuario base
     * @param estudiante datos académicos del estudiante
     * @param idCarrera id de la carrera asociada
     * @param idTipoDocumento id del tipo de documento del usuario
     * @return estudiante creado y persistido
     * @throws RuntimeException en caso de duplicados o referencias inexistentes
     */
    @Transactional
    public Estudiante crear(
        Usuario usuario,
        Estudiante estudiante,
        Integer idCarrera,
        Integer idTipoDocumento
    ) {
        return crearConCredenciales(
            usuario,
            estudiante,
            idCarrera,
            idTipoDocumento
        ).estudianteCreado;
    }

    /**
     * Crea estudiante y retorna estudiante + contraseña en texto plano para uso interno.
     * La contraseña en texto plano no debe exponerse en respuestas HTTP.
     */
    @Transactional
    public ResultadoCreacion crearConCredenciales(
        Usuario usuario,
        Estudiante estudiante,
        Integer idCarrera,
        Integer idTipoDocumento
    ) {
        validarDocumentoNoDuplicado(usuario.getNumeroDocumento());
        asegurarCodigoEstudiante(estudiante);

        String correoGenerado = EstudianteUtil.generarCorreoDesdeCodigo(
            estudiante.getCodigoEstudiante(),
            dominioCorreoInstitucional
        );
        validarCorreoNoDuplicado(correoGenerado);
        String contrasenaPlano = obtenerContrasenaPlano(usuario);

        usuario.setPassword(codificadorContrasena.encode(contrasenaPlano));

        TipoDocumento tipoDocumento = obtenerTipoDocumento(idTipoDocumento);
        Carrera carrera = obtenerCarrera(idCarrera);

        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        usuario.setEmail(correoGenerado);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        estudiante.setUsuario(usuarioGuardado);
        estudiante.setCarrera(carrera);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);

        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        return new ResultadoCreacion(estudianteGuardado, contrasenaPlano);
    }

    /**
     * Resultado interno de creación que incluye contraseña en texto plano para envío seguro.
     */
    public static record ResultadoCreacion(
        Estudiante estudianteCreado,
        String contrasenaPlano
    ) {}

    /**
     * Actualiza los datos academicos de un estudiante existente.
     *
     * @param id ID del estudiante (id_usuario)
     * @param datos objeto con los nuevos datos
     * @param idCarrera nueva carrera (puede ser la misma)
     * @return estudiante actualizado
     */
    @Transactional
    public Estudiante actualizar(
        Integer id,
        Estudiante datos,
        Integer idCarrera
    ) {
        Estudiante existente = buscarPorId(id);

        // Validar codigo unico si cambió
        if (
            !existente
                .getCodigoEstudiante()
                .equals(datos.getCodigoEstudiante()) &&
            estudianteRepository.existsByCodigoEstudiante(
                datos.getCodigoEstudiante()
            )
        ) {
            throw new RuntimeException(
                "Ya existe un estudiante con el codigo: " +
                    datos.getCodigoEstudiante()
            );
        }

        // Resolver nueva carrera
        Carrera carrera = carreraRepository
            .findById(idCarrera)
            .orElseThrow(() ->
                new RuntimeException(
                    "Carrera no encontrada con ID: " + idCarrera
                )
            );

        // Actualizar campos
        existente.setCodigoEstudiante(datos.getCodigoEstudiante());
        existente.setCiclo(datos.getCiclo());
        existente.setEstadoAcademico(datos.getEstadoAcademico());
        existente.setCarrera(carrera);

        return estudianteRepository.save(existente);
    }

    /**
     * Elimina un estudiante por su ID.
     *
     * @throws RuntimeException si no existe
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!estudianteRepository.existsById(id)) {
            throw new RuntimeException(
                "No se puede eliminar. Estudiante no encontrado con ID: " + id
            );
        }
        estudianteRepository.deleteById(id);
    }

    private void validarDocumentoNoDuplicado(String numeroDocumento) {
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento)) {
            throw new RuntimeException(
                "Ya existe un usuario con el documento: " + numeroDocumento
            );
        }
    }

    private void asegurarCodigoEstudiante(Estudiante estudiante) {
        if (
            estudiante.getCodigoEstudiante() == null ||
            estudiante.getCodigoEstudiante().trim().isEmpty()
        ) {
            estudiante.setCodigoEstudiante(
                EstudianteUtil.generarCodigoAleatorio(8)
            );
        }
    }

    private void validarCorreoNoDuplicado(String correo) {
        if (usuarioRepository.existsByEmail(correo)) {
            throw new RuntimeException(
                "Ya existe un usuario con el email generado: " + correo
            );
        }
    }

    private String obtenerContrasenaPlano(Usuario usuario) {
        String contrasena = usuario.getPassword();
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return EstudianteUtil.generarContrasenaSegura(12);
        }
        return contrasena;
    }

    private TipoDocumento obtenerTipoDocumento(Integer idTipoDocumento) {
        return tipoDocumentoRepository
            .findById(idTipoDocumento)
            .orElseThrow(() ->
                new RuntimeException(
                    "Tipo de documento no encontrado con ID: " + idTipoDocumento
                )
            );
    }

    private Carrera obtenerCarrera(Integer idCarrera) {
        return carreraRepository
            .findById(idCarrera)
            .orElseThrow(() ->
                new RuntimeException(
                    "Carrera no encontrada con ID: " + idCarrera
                )
            );
    }
}
