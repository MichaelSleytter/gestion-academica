package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Carrera;
import com.example.gestionacademica.entities.Estudiante;
import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.entities.Usuario;
import com.example.gestionacademica.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de logica de negocio para Estudiante.
 * Gestiona operaciones CRUD y validaciones de negocio.
 */
@Service
@RequiredArgsConstructor
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    /**
     * Retorna todos los estudiantes registrados.
     */
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    /**
     * Busca un estudiante por su ID (id_usuario).
     *
     * @throws RuntimeException si no existe
     */
    public Estudiante buscarPorId(Integer id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con ID: " + id));
    }

    /**
     * Busca un estudiante por su codigo unico.
     *
     * @throws RuntimeException si no existe
     */
    public Estudiante buscarPorCodigo(String codigoEstudiante) {
        return estudianteRepository.findByCodigoEstudiante(codigoEstudiante)
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con codigo: " + codigoEstudiante));
    }

    /**
     * Lista estudiantes por carrera.
     */
    public List<Estudiante> listarPorCarrera(Integer idCarrera) {
        return estudianteRepository.findByCarrera_IdCarrera(idCarrera);
    }

    /**
     * Lista estudiantes por ciclo.
     */
    public List<Estudiante> listarPorCiclo(Integer ciclo) {
        return estudianteRepository.findByCiclo(ciclo);
    }

    /**
     * Crea un nuevo estudiante junto con su usuario base.
     * Valida duplicados de email, documento y codigo de estudiante.
     *
     * @param usuario   datos del usuario base
     * @param estudiante datos academicos del estudiante
     * @param idCarrera carrera a la que pertenece
     * @param idTipoDocumento tipo de documento del usuario
     * @return estudiante creado
     */
    @Transactional
    public Estudiante crear(Usuario usuario, Estudiante estudiante,
                            Integer idCarrera, Integer idTipoDocumento) {

        // Validar email unico
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException(
                    "Ya existe un usuario con el email: " + usuario.getEmail());
        }

        // Validar documento unico
        if (usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new RuntimeException(
                    "Ya existe un usuario con el documento: " + usuario.getNumeroDocumento());
        }

        // Validar codigo de estudiante unico
        if (estudianteRepository.existsByCodigoEstudiante(estudiante.getCodigoEstudiante())) {
            throw new RuntimeException(
                    "Ya existe un estudiante con el codigo: " + estudiante.getCodigoEstudiante());
        }

        // Resolver tipo de documento
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(idTipoDocumento)
                .orElseThrow(() -> new RuntimeException(
                        "Tipo de documento no encontrado con ID: " + idTipoDocumento));

        // Resolver carrera
        Carrera carrera = carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new RuntimeException(
                        "Carrera no encontrada con ID: " + idCarrera));

        // Configurar usuario
        usuario.setTipoDocumento(tipoDocumento);
        usuario.setEstado(true);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Configurar estudiante
        estudiante.setUsuario(usuarioGuardado);
        estudiante.setCarrera(carrera);
        estudiante.setEstadoAcademico("ACTIVO");

        return estudianteRepository.save(estudiante);
    }

    /**
     * Actualiza los datos academicos de un estudiante existente.
     *
     * @param id ID del estudiante (id_usuario)
     * @param datos objeto con los nuevos datos
     * @param idCarrera nueva carrera (puede ser la misma)
     * @return estudiante actualizado
     */
    @Transactional
    public Estudiante actualizar(Integer id, Estudiante datos, Integer idCarrera) {

        Estudiante existente = buscarPorId(id);

        // Validar codigo unico si cambió
        if (!existente.getCodigoEstudiante().equals(datos.getCodigoEstudiante())
                && estudianteRepository.existsByCodigoEstudiante(datos.getCodigoEstudiante())) {
            throw new RuntimeException(
                    "Ya existe un estudiante con el codigo: " + datos.getCodigoEstudiante());
        }

        // Resolver nueva carrera
        Carrera carrera = carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new RuntimeException(
                        "Carrera no encontrada con ID: " + idCarrera));

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
                    "No se puede eliminar. Estudiante no encontrado con ID: " + id);
        }
        estudianteRepository.deleteById(id);
    }
}