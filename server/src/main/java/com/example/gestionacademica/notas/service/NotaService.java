package com.example.gestionacademica.notas.service;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.notas.domain.Nota;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.evaluaciones.repository.EvaluacionRepository;
import com.example.gestionacademica.notas.repository.NotaRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
     * Crea una nota asociada a evaluacion y estudiante.
     *
     * @param nota datos de nota
     * @param idEvaluacion identificador de evaluacion
     * @param idEstudiante identificador de estudiante
     * @return nota creada
     */
    @Transactional
    public Nota crear(Nota nota, Integer idEvaluacion, Integer idEstudiante) {
        validarRangoNota(nota.getNota());

        if (notaRepository.existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(idEvaluacion, idEstudiante)) {
            throw new RuntimeException("Ya existe una nota para ese estudiante en la evaluacion indicada.");
        }

        nota.setEvaluacion(obtenerEvaluacion(idEvaluacion));
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
    public Nota actualizar(Integer id, Nota datos, Integer idEvaluacion, Integer idEstudiante) {
        Nota existente = buscarPorId(id);
        validarRangoNota(datos.getNota());

        boolean cambioRelacion = !existente.getEvaluacion().getIdEvaluacion().equals(idEvaluacion)
                || !existente.getEstudiante().getIdUsuario().equals(idEstudiante);

        if (cambioRelacion
                && notaRepository.existsByEvaluacion_IdEvaluacionAndEstudiante_IdUsuario(idEvaluacion, idEstudiante)) {
            throw new RuntimeException("Ya existe una nota para ese estudiante en la evaluacion indicada.");
        }

        existente.setNota(datos.getNota());
        existente.setEvaluacion(obtenerEvaluacion(idEvaluacion));
        existente.setEstudiante(obtenerEstudiante(idEstudiante));

        return notaRepository.save(existente);
    }

    /**
     * Elimina una nota por ID.
     *
     * @param id identificador de nota
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!notaRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Nota no encontrada con ID: " + id);
        }
        notaRepository.deleteById(id);
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
}
