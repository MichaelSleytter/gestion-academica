package com.example.gestionacademica.services.estudiante;

import com.example.gestionacademica.dto.EstudianteCrearDTO;
import com.example.gestionacademica.dto.EstudianteRequestDTO;
import com.example.gestionacademica.entities.Carrera;
import com.example.gestionacademica.entities.Estudiante;
import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.enums.EstudianteEstadoAcademico;
import com.example.gestionacademica.repositories.*;
import com.example.gestionacademica.services.CatalogoService;
import com.example.gestionacademica.utils.EstudianteUtil;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final PasswordEncoder codificadorContrasena;

    // New collaborators
    private final CatalogoService catalogoService;
    private final UsuarioFactory usuarioFactory;
    private final EstudianteFactory estudianteFactory;
    private final EstudianteValidator estudianteValidator;

    @Value("${app.student.email.domain:institution.edu.pe}")
    private String dominioCorreoInstitucional;

    /**
     * Retorna todos los estudiantes registrados.
     *
     * @return lista de estudiantes (puede estar vacía)
     */
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAllByUsuario_EstadoTrue();
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
            .findByIdUsuarioAndUsuario_EstadoTrue(id)
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
        return estudianteRepository.findByCarrera_IdCarreraAndUsuario_EstadoTrue(
            idCarrera
        );
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
     * Crea estudiante y retorna estudiante + contraseña en texto plano para uso interno.
     * La contraseña en texto plano no debe exponerse en respuestas HTTP.
     */
    @Transactional
    public ResultadoCreacion crearConCredenciales(EstudianteCrearDTO comando) {
        // Validaciones de negocio
        estudianteValidator.validarDocumentoNoDuplicado(
            comando.getNumeroDocumento()
        );

        // Mapear entidades desde el comando
        Usuario usuario = usuarioFactory.crearDesdeComando(comando);
        Estudiante estudiante = estudianteFactory.crearDesdeComando(comando);

        // Asegurar código y correo
        estudianteValidator.asegurarCodigoEstudiante(estudiante);
        String correoGenerado = EstudianteUtil.generarCorreoDesdeCodigo(
            estudiante.getCodigoEstudiante(),
            dominioCorreoInstitucional
        );
        estudianteValidator.validarCorreoNoDuplicado(correoGenerado);

        // Contraseña en texto plano y codificación
        String contrasenaPlano = obtenerContrasenaPlanoDesdeComando(comando);
        usuario.setPassword(codificadorContrasena.encode(contrasenaPlano));

        // Resolver referencias mediante CatalogoService
        TipoDocumento tipoDocumento = catalogoService.buscarTipoDocumentoPorId(
            comando.getIdTipoDocumento()
        );
        Carrera carrera = catalogoService.buscarCarreraPorId(
            comando.getIdCarrera()
        );

        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        usuario.setEmail(correoGenerado);
        usuario.setEmailPersonal(comando.getEmailPersonal());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        estudiante.setUsuario(usuarioGuardado);
        estudiante.setCarrera(carrera);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);

        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        return new ResultadoCreacion(estudianteGuardado, contrasenaPlano);
    }

    private String obtenerContrasenaPlanoDesdeComando(
        EstudianteCrearDTO comando
    ) {
        String p = comando.getPassword();
        if (p == null || p.trim().isEmpty()) {
            return EstudianteUtil.generarContrasenaSegura(12);
        }
        return p;
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

        // Validar y actualizar codigo solo si el cliente lo provee (evita sobreescritura con NULL)
        String nuevoCodigo = datos.getCodigoEstudiante();
        if (nuevoCodigo != null && !nuevoCodigo.trim().isEmpty()) {
            if (
                !Objects.equals(existente.getCodigoEstudiante(), nuevoCodigo) &&
                estudianteRepository.existsByCodigoEstudiante(nuevoCodigo)
            ) {
                throw new RuntimeException(
                    "Ya existe un estudiante con el codigo: " + nuevoCodigo
                );
            }
            existente.setCodigoEstudiante(nuevoCodigo);
        }

        // Resolver nueva carrera
        Carrera carrera = catalogoService.buscarCarreraPorId(idCarrera);

        // Actualizar campos (codigo ya fue manejado arriba si aplica)
        existente.setCiclo(datos.getCiclo());
        existente.setEstadoAcademico(datos.getEstadoAcademico());
        existente.setCarrera(carrera);

        return estudianteRepository.save(existente);
    }

    /**
     * Actualiza estudiante y su usuario a partir del DTO de solicitud.
     * Se mergean sólo los campos no nulos del DTO en la entidad existente.
     */
    @Transactional
    public Estudiante actualizarDesdeDto(Integer id, EstudianteRequestDTO dto) {
        Estudiante existente = buscarPorId(id);

        // Actualizar usuario asociado si existe
        Usuario usuario = existente.getUsuario();
        if (usuario != null) {
            if (dto.getNombre() != null) usuario.setNombre(dto.getNombre());
            if (dto.getApellido() != null) usuario.setApellido(
                dto.getApellido()
            );

            if (
                dto.getNumeroDocumento() != null &&
                !dto.getNumeroDocumento().equals(usuario.getNumeroDocumento())
            ) {
                if (
                    usuarioRepository.existsByNumeroDocumento(
                        dto.getNumeroDocumento()
                    )
                ) {
                    throw new RuntimeException(
                        "Ya existe un usuario con el documento: " +
                            dto.getNumeroDocumento()
                    );
                }
                usuario.setNumeroDocumento(dto.getNumeroDocumento());
            }

            if (dto.getEmailPersonal() != null) {
                usuario.setEmailPersonal(dto.getEmailPersonal());
            }

            if (dto.getIdTipoDocumento() != null) {
                TipoDocumento tipo = catalogoService.buscarTipoDocumentoPorId(
                    dto.getIdTipoDocumento()
                );
                usuario.setTipoDocumento(tipo);
            }

            usuarioRepository.save(usuario);
        }

        // Construir objeto con cambios academicos y delegar a la lógica existente
        Estudiante datos = new Estudiante();
        datos.setCiclo(dto.getCiclo());
        datos.setEstadoAcademico(dto.getEstadoAcademico());

        return actualizar(id, datos, dto.getIdCarrera());
    }

    /**
     * Elimina un estudiante por su ID.
     *
     * @throws RuntimeException si no existe
     */
    @Transactional
    public void eliminar(Integer id) {
        Estudiante estudiante = estudianteRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "No se puede eliminar. Estudiante no encontrado con ID: " +
                        id
                )
            );

        Usuario usuario = estudiante.getUsuario();
        if (usuario == null) {
            throw new RuntimeException(
                "Estudiante sin usuario asociado: " + id
            );
        }

        //Desactivar usuario y marcar estado académico como INACTIVO
        usuario.setEstado(false);
        usuario.setFechaBaja(LocalDateTime.now());
        usuarioRepository.save(usuario);

        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.INACTIVO);
        estudianteRepository.save(estudiante);
    }
}
