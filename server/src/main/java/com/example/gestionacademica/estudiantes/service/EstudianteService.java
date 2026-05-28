package com.example.gestionacademica.estudiantes.service;

import com.example.gestionacademica.auth.domain.Rol;
import com.example.gestionacademica.auth.repository.RolRepository;
import com.example.gestionacademica.estudiantes.dto.EstudianteCrearDTO;
import com.example.gestionacademica.estudiantes.dto.EstudianteRequestDTO;
import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.estudiantes.domain.EstudianteEstadoAcademico;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.auth.repository.UsuarioRepository;
import com.example.gestionacademica.catalogos.service.CatalogoService;
import com.example.gestionacademica.estudiantes.util.EstudianteUtil;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Servicio de lógica de negocio para operaciones sobre {@link Estudiante}.
 *
 * <p>
 * Proporciona operaciones CRUD, validaciones de negocio y la generación de
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
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
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
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
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
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con codigo: " + codigoEstudiante));
    }

    /**
     * Lista estudiantes por carrera.
     *
     * @param idCarrera identificador de la carrera
     * @return lista de estudiantes pertenecientes a la carrera
     */
    public List<Estudiante> listarPorCarrera(Integer idCarrera) {
        return estudianteRepository.findByCarrera_IdCarreraAndUsuario_EstadoTrue(
                idCarrera);
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
     * Crea estudiante y retorna estudiante + contraseña en texto plano para uso
     * interno.
     * La contraseña en texto plano no debe exponerse en respuestas HTTP.
     */
    @Transactional
    public ResultadoCreacion crearConCredenciales(EstudianteCrearDTO comando) {
        // Validaciones de negocio
        estudianteValidator.validarDocumentoNoDuplicado(
                comando.getNumeroDocumento());

        // Mapear entidades desde el comando
        Usuario usuario = usuarioFactory.crearDesdeComando(comando);
        Estudiante estudiante = estudianteFactory.crearDesdeComando(comando);

        // Asegurar código y correo
        estudianteValidator.asegurarCodigoEstudiante(estudiante);
        String correoGenerado = EstudianteUtil.generarCorreoDesdeCodigo(
                estudiante.getCodigoEstudiante(),
                dominioCorreoInstitucional);
        estudianteValidator.validarCorreoNoDuplicado(correoGenerado);

        // GENERAR CONTRASEÑA TEMPORAL usando EstudianteUtil
        String contrasenaTemporal = EstudianteUtil.generarContrasenaSegura(8);
        String contrasenaHash = passwordEncoder.encode(contrasenaTemporal);

        // Resolver referencias mediante CatalogoService
        TipoDocumento tipoDocumento = catalogoService.buscarTipoDocumentoPorId(
                comando.getIdTipoDocumento());
        Carrera carrera = catalogoService.buscarCarreraPorId(
                comando.getIdCarrera());
        Rol rolEstudiante = rolRepository.findByNombreIgnoreCase("ESTUDIANTE")
                .orElseThrow(() -> new RuntimeException("El rol 'ESTUDIANTE' no existe."));

        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        usuario.setEmail(correoGenerado);
        usuario.setEmailPersonal(comando.getEmailPersonal());
        usuario.setPassword(contrasenaHash);
        usuario.setRoles(Collections.singletonList(rolEstudiante));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        estudiante.setUsuario(usuarioGuardado);
        estudiante.setCarrera(carrera);
        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.ACTIVO);

        Estudiante estudianteGuardado = estudianteRepository.save(estudiante);
        return new ResultadoCreacion(estudianteGuardado, contrasenaTemporal);
    }

    /**
     * Envía la contraseña temporal al correo personal del estudiante.
     *
     * POR IMPLEMENTAR: Requiere configurar JavaMail/Spring Mail en el proyecto.
     *
     * Pasos para implementar:
     * 1. Agregar dependencia spring-boot-starter-mail en pom.xml
     * 2. Configurar properties de mail en application.yaml
     * 3. Crear un EmailService con JavaMailSender
     * 4. Llamar a este método después de crear el estudiante
     *
     * @param emailPersonal      correo personal del estudiante
     * @param contrasenaTemporal contraseña temporal generada
     * @param nombre             nombre del estudiante para el saludo
     * @param apellido           apellido del estudiante
     */
    public void enviarContrasenaTemporal(String emailPersonal, String contrasenaTemporal,
            String nombre, String apellido) {
        // TODO: Implementar cuando se configure JavaMail
        // Ejemplo de implementación:
        // emailService.sendEmail(
        // emailPersonal,
        // "Credenciales de Acceso - Sistema Académico",
        // "Hola " + nombre + " " + apellido + ",\n\n" +
        // "Tu contraseña temporal es: " + contrasenaTemporal + "\n\n" +
        // "Por favor, cambia tu contraseña al iniciar sesión.\n\n" +
        // "Saludos,\nAdministración Académica"
        // );

        // Por ahora solo logueamos
        System.out.println("===== EMAIL POR ENVIAR =====");
        System.out.println("Para: " + emailPersonal);
        System.out.println("Asunto: Credenciales de Acceso");
        System.out.println("Cuerpo: Hola " + nombre + " " + apellido);
        System.out.println("Tu contraseña temporal es: " + contrasenaTemporal);
        System.out.println("===========================");
    }

    /**
     * Resultado interno de creación que incluye contraseña en texto plano para
     * envío seguro.
     */
    public static record ResultadoCreacion(
            Estudiante estudianteCreado,
            String contrasenaPlano) {
    }

    /**
     * Actualiza los datos academicos de un estudiante existente.
     *
     * @param id        ID del estudiante (id_usuario)
     * @param datos     objeto con los nuevos datos
     * @param idCarrera nueva carrera (puede ser la misma)
     * @return estudiante actualizado
     */
    @Transactional
    public Estudiante actualizar(
            Integer id,
            Estudiante datos,
            Integer idCarrera) {
        Estudiante existente = buscarPorId(id);

        String nuevoCodigo = datos.getCodigoEstudiante();
        if (nuevoCodigo != null && !nuevoCodigo.trim().isEmpty()) {
            if (!Objects.equals(existente.getCodigoEstudiante(), nuevoCodigo) &&
                    estudianteRepository.existsByCodigoEstudiante(nuevoCodigo)) {
                throw new RuntimeException(
                        "Ya existe un estudiante con el codigo: " + nuevoCodigo);
            }
            existente.setCodigoEstudiante(nuevoCodigo);
        }

        // Resolver nueva carrera
        Carrera carrera = catalogoService.buscarCarreraPorId(idCarrera);

        existente.setCiclo(datos.getCiclo());
        if (datos.getEstadoAcademico() != null) {
            existente.setEstadoAcademico(datos.getEstadoAcademico());
        }
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
            if (dto.getNombre() != null)
                usuario.setNombre(dto.getNombre());
            if (dto.getApellido() != null)
                usuario.setApellido(
                        dto.getApellido());

            if (dto.getNumeroDocumento() != null &&
                    !dto.getNumeroDocumento().equals(usuario.getNumeroDocumento())) {
                if (usuarioRepository.existsByNumeroDocumento(
                        dto.getNumeroDocumento())) {
                    throw new RuntimeException(
                            "Ya existe un usuario con el documento: " +
                                    dto.getNumeroDocumento());
                }
                usuario.setNumeroDocumento(dto.getNumeroDocumento());
            }

            if (dto.getEmailPersonal() != null) {
                usuario.setEmailPersonal(dto.getEmailPersonal());
            }

            if (dto.getIdTipoDocumento() != null) {
                TipoDocumento tipo = catalogoService.buscarTipoDocumentoPorId(
                        dto.getIdTipoDocumento());
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
                .orElseThrow(() -> new RuntimeException(
                        "No se puede eliminar. Estudiante no encontrado con ID: " +
                                id));

        Usuario usuario = estudiante.getUsuario();
        if (usuario == null) {
            throw new RuntimeException(
                    "Estudiante sin usuario asociado: " + id);
        }

        // Desactivar usuario y marcar estado académico como INACTIVO
        usuario.setEstado(false);
        usuario.setFechaBaja(LocalDateTime.now());
        usuarioRepository.save(usuario);

        estudiante.setEstadoAcademico(EstudianteEstadoAcademico.INACTIVO);
        estudianteRepository.save(estudiante);
    }
}
