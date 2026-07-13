package com.example.gestionacademica.notas.service;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.auth.domain.Usuario;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.evaluaciones.repository.EvaluacionRepository;
import com.example.gestionacademica.notas.dto.MisNotasResponseDTO;
import com.example.gestionacademica.notas.repository.NotaRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link Nota}.
 */
@Service
@RequiredArgsConstructor
public class NotaService {

    private static final BigDecimal NOTA_MINIMA = BigDecimal.ZERO;
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("20");

    private final NotaRepository notaRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final DocenteSeccionRepository docenteSeccionRepository;

    /**
     * Lista todas las notas.
     *
     * @return notas registradas
     */
    public List<Nota> listarTodas() {
        return notaRepository.findAll();
    }

    /**
     * Busca una nota por ID.
     *
     * @param id identificador de la nota
     * @return nota encontrada
     */
    public Nota buscarPorId(Integer id) {
        return notaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada con ID: " + id));
    }

    /**
     * Lista notas por evaluacion.
     *
     * @param idEvaluacion identificador de evaluacion
     * @return notas asociadas
     */
    public List<Nota> listarPorEvaluacion(Integer idEvaluacion) {
        return notaRepository.findByEvaluacion_IdEvaluacion(idEvaluacion);
    }

    /**
     * Lista notas por estudiante.
     *
     * @param idEstudiante identificador de estudiante
     * @return notas asociadas
     */
    public List<Nota> listarPorEstudiante(Integer idEstudiante) {
        return notaRepository.findByEstudiante_IdUsuario(idEstudiante);
    }

    /**
     * Obtiene las notas del estudiante autenticado para todas las evaluaciones
     * de una sección determinada.
     *
     * @param idEstudiante ID del estudiante.
     * @param idSeccion    ID de la sección.
     * @return Lista de DTOs con ID de nota, valor de nota e ID de evaluación.
     */
    public List<MisNotasResponseDTO> listarMisNotasPorSeccion(Integer idEstudiante, Integer idSeccion) {
        return notaRepository.findByEstudianteIdAndSeccionIdWithEvaluacion(idEstudiante, idSeccion)
                .stream()
                .map(this::aMisNotasDTO)
                .toList();
    }

    private MisNotasResponseDTO aMisNotasDTO(Nota nota) {
        return new MisNotasResponseDTO(
                nota.getIdNota(),
                nota.getNota(),
                nota.getEvaluacion().getIdEvaluacion()
        );
    }

    /**
     * Crea una nota asociada a evaluacion y estudiante.
     *
     * @param nota datos de nota
     * @param idEvaluacion identificador de evaluacion
     * @param idEstudiante identificador de estudiante
     * @return nota creada
     */
    @Transactional
    public Nota crear(Nota nota, Integer idEvaluacion, Integer idEstudiante, Authentication authentication) {
        validarRangoNota(nota.getNota());
        Evaluacion evaluacion = obtenerEvaluacion(idEvaluacion);
        requireCanMutate(evaluacion, authentication);

        if (notaRepository.existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(idEvaluacion, idEstudiante)) {
            throw new RuntimeException("Ya existe una nota para ese estudiante en la evaluacion indicada.");
        }

        nota.setEvaluacion(evaluacion);
        nota.setEstudiante(obtenerEstudiante(idEstudiante));

        return notaRepository.save(nota);
    }

    /**
     * Actualiza una nota existente.
     *
     * @param id identificador de nota
     * @param datos nuevos datos
     * @param idEvaluacion identificador de evaluacion
     * @param idEstudiante identificador de estudiante
     * @return nota actualizada
     */
    @Transactional
    public Nota actualizar(
            Integer id,
            Nota datos,
            Integer idEvaluacion,
            Integer idEstudiante,
            Authentication authentication) {
        Nota existente = buscarPorId(id);
        validarRangoNota(datos.getNota());
        Evaluacion evaluacion = obtenerEvaluacion(idEvaluacion);
        requireCanMutate(existente.getEvaluacion(), authentication);
        requireCanMutate(evaluacion, authentication);

        boolean cambioRelacion = !existente.getEvaluacion().getIdEvaluacion().equals(idEvaluacion)
                || !existente.getEstudiante().getIdUsuario().equals(idEstudiante);

        if (cambioRelacion
                && notaRepository.existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(idEvaluacion, idEstudiante)) {
            throw new RuntimeException("Ya existe una nota para ese estudiante en la evaluacion indicada.");
        }

        existente.setNota(datos.getNota());
        existente.setEvaluacion(evaluacion);
        existente.setEstudiante(obtenerEstudiante(idEstudiante));

        return notaRepository.save(existente);
    }

    /**
     * Elimina una nota por ID.
     *
     * @param id identificador de nota
     */
    @Transactional
    public void eliminar(Integer id, Authentication authentication) {
        Nota nota = buscarPorId(id);
        requireCanMutate(nota.getEvaluacion(), authentication);
        notaRepository.delete(nota);
    }

    private Evaluacion obtenerEvaluacion(Integer idEvaluacion) {
        return evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada con ID: " + idEvaluacion));
    }

    private Estudiante obtenerEstudiante(Integer idEstudiante) {
        return estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + idEstudiante));
    }

    private void validarRangoNota(BigDecimal nota) {
        if (nota == null) {
            throw new RuntimeException("La nota es obligatoria.");
        }
        if (nota.compareTo(NOTA_MINIMA) < 0 || nota.compareTo(NOTA_MAXIMA) > 0) {
            throw new RuntimeException("La nota debe estar en el rango de 0 a 20.");
        }
    }

    private void requireCanMutate(Evaluacion evaluacion, Authentication authentication) {
        if (hasRole(authentication, "ADMIN") || isAssignedDocente(evaluacion, currentUserId(authentication))) {
            return;
        }
        throw new AccessDeniedException("No tiene permiso para modificar notas de esta evaluación.");
    }

    private boolean isAssignedDocente(Evaluacion evaluacion, Integer userId) {
        return docenteSeccionRepository.existsByDocente_IdUsuarioAndSeccion_IdSeccion(
                userId,
                evaluacion.getSeccion().getIdSeccion());
    }

    private Integer currentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getIdUsuario();
        }
        throw new AccessDeniedException("Usuario autenticado inválido.");
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
