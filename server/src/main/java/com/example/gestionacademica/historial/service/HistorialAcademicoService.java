package com.example.gestionacademica.historial.service;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.historial.domain.HistorialAcademico;
import com.example.gestionacademica.historial.domain.HistorialAcademicoEstado;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.historial.repository.HistorialAcademicoRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link HistorialAcademico}.
 */
@Service
@RequiredArgsConstructor
public class HistorialAcademicoService {

    private final HistorialAcademicoRepository historialAcademicoRepository;
    private final EstudianteRepository estudianteRepository;
    private final SeccionRepository seccionRepository;

    /**
     * Lista todos los registros de historial academico.
     *
     * @return historial academico
     */
    public List<HistorialAcademico> listarTodos() {
        return historialAcademicoRepository.findAll();
    }

    /**
     * Busca un registro de historial academico por ID.
     *
     * @param id identificador del historial
     * @return registro encontrado
     */
    public HistorialAcademico buscarPorId(Integer id) {
        return historialAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historial academico no encontrado con ID: " + id));
    }

    /**
     * Lista historial por estudiante.
     *
     * @param idEstudiante identificador de estudiante
     * @return historial del estudiante
     */
    public List<HistorialAcademico> listarPorEstudiante(Integer idEstudiante) {
        return historialAcademicoRepository.findByEstudiante_IdUsuario(idEstudiante);
    }

    /**
     * Lista historial por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return historial de la seccion
     */
    public List<HistorialAcademico> listarPorSeccion(Integer idSeccion) {
        return historialAcademicoRepository.findBySeccion_IdSeccion(idSeccion);
    }

    /**
     * Crea un nuevo registro de historial academico.
     *
     * @param historial datos de historial
     * @param idEstudiante identificador de estudiante
     * @param idSeccion identificador de seccion
     * @return registro creado
     */
    @Transactional
    public HistorialAcademico crear(HistorialAcademico historial, Integer idEstudiante, Integer idSeccion) {
        validarEstado(historial.getEstado());

        if (historialAcademicoRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(idEstudiante, idSeccion)) {
            throw new RuntimeException("Ya existe historial para el estudiante en la seccion indicada.");
        }

        historial.setEstudiante(obtenerEstudiante(idEstudiante));
        historial.setSeccion(obtenerSeccion(idSeccion));

        return historialAcademicoRepository.save(historial);
    }

    /**
     * Actualiza un historial academico.
     *
     * @param id identificador del historial
     * @param datos nuevos datos
     * @param idEstudiante identificador de estudiante
     * @param idSeccion identificador de seccion
     * @return historial actualizado
     */
    @Transactional
    public HistorialAcademico actualizar(Integer id, HistorialAcademico datos, Integer idEstudiante, Integer idSeccion) {
        HistorialAcademico existente = buscarPorId(id);
        validarEstado(datos.getEstado());

        boolean cambioRelacion = !existente.getEstudiante().getIdUsuario().equals(idEstudiante)
                || !existente.getSeccion().getIdSeccion().equals(idSeccion);

        if (cambioRelacion
                && historialAcademicoRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(idEstudiante, idSeccion)) {
            throw new RuntimeException("Ya existe historial para el estudiante en la seccion indicada.");
        }

        existente.setNotaFinal(datos.getNotaFinal());
        existente.setEstado(datos.getEstado());
        existente.setEstudiante(obtenerEstudiante(idEstudiante));
        existente.setSeccion(obtenerSeccion(idSeccion));

        return historialAcademicoRepository.save(existente);
    }

    /**
     * Elimina un historial academico por ID.
     *
     * @param id identificador del historial
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!historialAcademicoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Historial academico no encontrado con ID: " + id);
        }
        historialAcademicoRepository.deleteById(id);
    }

    private Estudiante obtenerEstudiante(Integer idEstudiante) {
        return estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + idEstudiante));
    }

    private Seccion obtenerSeccion(Integer idSeccion) {
        return seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new RuntimeException("Seccion no encontrada con ID: " + idSeccion));
    }

    private void validarEstado(HistorialAcademicoEstado estado) {
        if (estado == null) {
            throw new RuntimeException("El estado del historial academico es obligatorio.");
        }
    }
}
