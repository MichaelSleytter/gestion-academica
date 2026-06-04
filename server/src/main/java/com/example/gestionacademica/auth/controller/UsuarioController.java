package com.example.gestionacademica.auth.controller;

import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.auth.dto.CrearUsuarioConCredencialesRequest;
import com.example.gestionacademica.auth.dto.CrearUsuarioConCredencialesResponse;
import com.example.gestionacademica.auth.service.EmailService;
import com.example.gestionacademica.auth.service.EmailValidatorService;
import com.example.gestionacademica.auth.service.UsuarioService;
import com.example.gestionacademica.estudiantes.util.EstudianteUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestion de usuarios.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Operaciones CRUD de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final EmailValidatorService emailValidatorService;

    /**
     * Lista todos los usuarios.
     *
     * @return lista de usuarios
     */
    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador de usuario
     * @return usuario encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<Usuario> buscarPorId(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario datos del usuario
     * @param idTipoDocumento identificador del tipo de documento
     * @return usuario creado
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Requiere idTipoDocumento como parametro de query")
    public ResponseEntity<Usuario> crear(
            @RequestBody Usuario usuario,
            @Parameter(description = "ID del tipo de documento", example = "1")
            @RequestParam Integer idTipoDocumento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario, idTipoDocumento));
    }

    /**
     * Actualiza un usuario.
     *
     * @param id identificador de usuario
     * @param usuario datos nuevos
     * @param idTipoDocumento identificador del tipo de documento
     * @return usuario actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<Usuario> actualizar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id,
            @RequestBody Usuario usuario,
            @Parameter(description = "ID del tipo de documento", example = "1")
            @RequestParam Integer idTipoDocumento) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario, idTipoDocumento));
    }

    /**
     * Elimina un usuario por ID.
     *
     * @param id identificador de usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Crea un usuario generando una contraseña aleatoria y enviándola por correo.
     *
     * <p>El email personal, si se proporciona, es validado contra sus registros MX
     * antes de crear el usuario. La contraseña temporal se genera con
     * {@link EstudianteUtil#generarContrasenaSegura(int)} y queda almacenada
     * cifrada (BCrypt) por {@link UsuarioService}.</p>
     *
     * @param request datos del nuevo usuario
     * @return respuesta con el id del usuario creado y mensaje informativo
     */
    @PostMapping("/crear-con-credenciales")
    @Operation(
            summary = "Crear usuario y enviar credenciales por correo",
            description = "Genera una contraseña aleatoria, crea el usuario y la envía por email.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado y credenciales enviadas",
                    content = @Content(schema = @Schema(implementation = CrearUsuarioConCredencialesResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email personal no válido",
                    content = @Content(schema = @Schema(implementation = Object.class)))
    })
    public ResponseEntity<CrearUsuarioConCredencialesResponse> crearConCredenciales(
            @Valid @RequestBody CrearUsuarioConCredencialesRequest request) {

        if (request.emailPersonal() != null && !request.emailPersonal().isBlank()) {
            EmailValidatorService.ResultadoValidacion resultado =
                    emailValidatorService.validar(request.emailPersonal());
            if (!resultado.esValido()) {
                throw new RuntimeException(
                        "El email personal no es válido: " + resultado.getMensaje());
            }
        }

        String passwordTemporal = EstudianteUtil.generarContrasenaSegura(12);
        String codigo = EstudianteUtil.generarCodigoAleatorio(8);
        String emailInstitucional = EstudianteUtil.generarCorreoDesdeCodigo(
                codigo, "institution.edu.pe");

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(emailInstitucional);
        usuario.setEmailPersonal(request.emailPersonal());
        usuario.setNumeroDocumento(request.numeroDocumento());
        usuario.setPassword(passwordTemporal);
        usuario.setEstado(true);

        Usuario creado = usuarioService.crear(usuario, request.idTipoDocumento());

        try {
            emailService.enviarCredenciales(
                    creado.getEmail(),
                    creado.getNombre(),
                    creado.getEmail(),
                    passwordTemporal);
        } catch (RuntimeException ex) {
            log.error("Usuario {} creado pero falló el envío de credenciales: {}",
                    creado.getIdUsuario(), ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CrearUsuarioConCredencialesResponse(
                        creado.getIdUsuario(),
                        creado.getEmail(),
                        "Usuario creado. Las credenciales fueron enviadas al correo."));
    }
}
