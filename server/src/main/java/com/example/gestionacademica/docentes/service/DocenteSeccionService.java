package com.example.gestionacademica.docentes.service;

import com.example.gestionacademica.docentes.domain.Docente;
import com.example.gestionacademica.docentes.domain.DocenteSeccion;
import com.example.gestionacademica.docentes.domain.DocenteSeccion.DocenteSeccionId;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.docentes.repository.DocenteRepository;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para asignaciones {@link DocenteSeccion}.
 */
@Service
@RequiredArgsConstructor
public class DocenteSeccionService {

    private final DocenteSeccionRepository docenteSeccionRepository;
    private final DocenteRepository docenteRepository;
    private final SeccionRepository seccionRepository;

    /**
     * Lista todas las asignaciones docente-seccion.
     *
     * @return asignaciones registradas
     */
    public List<DocenteSeccion> listarTodas() {
        return docenteSeccionRepository.findAll();
    }

    /**
     * Obtiene una asignacion por su llave compuesta.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     * @return asignacion encontrada
     */
    public DocenteSeccion buscarPorId(Integer idDocente, Integer idSeccion) {
        DocenteSeccionId id = new DocenteSeccionId(idDocente, idSeccion);
        return docenteSeccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Asignacion docente-seccion no encontrada para docente " + idDocente
                                + " y seccion " + idSeccion));
    }

    /**
     * Lista asignaciones por docente.
     *
     * @param idDocente identificador del docente
     * @return asignaciones del docente
     */
    public List<DocenteSeccion> listarPorDocente(Integer idDocente) {
        return docenteSeccionRepository.findByDocente_IdUsuario(idDocente);
    }

    /**
     * Lista asignaciones por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return asignaciones de la seccion
     */
    public List<DocenteSeccion> listarPorSeccion(Integer idSeccion) {
        return docenteSeccionRepository.findBySeccion_IdSeccion(idSeccion);
    }

    /**
     * Crea una nueva asignacion docente-seccion.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     * @return asignacion creada
     */
    @Transactional
    public DocenteSeccion crear(Integer idDocente, Integer idSeccion) {
        if (docenteSeccionRepository.existsByDocente_IdUsuarioAndSeccion_IdSeccion(idDocente, idSeccion)) {
            throw new RuntimeException("El docente ya esta asignado a la seccion indicada.");
        }

        Docente docente = docenteRepository.findById(idDocente)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + idDocente));

        Seccion seccion = seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new RuntimeException("Seccion no encontrada con ID: " + idSeccion));

        DocenteSeccion asignacion = new DocenteSeccion();
        asignacion.setId(new DocenteSeccionId(idDocente, idSeccion));
        asignacion.setDocente(docente);
        asignacion.setSeccion(seccion);

        return docenteSeccionRepository.save(asignacion);
    }

    /**
     * Elimina una asignacion docente-seccion.
     *
     * @param idDocente identificador del docente
     * @param idSeccion identificador de la seccion
     */
    @Transactional
    public void eliminar(Integer idDocente, Integer idSeccion) {
        DocenteSeccionId id = new DocenteSeccionId(idDocente, idSeccion);
        if (!docenteSeccionRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Asignacion docente-seccion no encontrada.");
        }
        docenteSeccionRepository.deleteById(id);
    }
}
