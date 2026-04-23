package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Evaluacion;
import com.example.gestionacademica.entities.Seccion;
import com.example.gestionacademica.repositories.EvaluacionRepository;
import com.example.gestionacademica.repositories.SeccionRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link Evaluacion}.
 */
@Service
@RequiredArgsConstructor
public class EvaluacionService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final EvaluacionRepository evaluacionRepository;
    private final SeccionRepository seccionRepository;

    /**
     * Lista todas las evaluaciones.
     *
     * @return evaluaciones registradas
     */
    public List<Evaluacion> listarTodas() {
        return evaluacionRepository.findAll();
    }

    /**
     * Busca una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     * @return evaluacion encontrada
     */
    public Evaluacion buscarPorId(Integer id) {
        return evaluacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluacion no encontrada con ID: " + id));
    }

    /**
     * Lista evaluaciones por seccion.
     *
     * @param idSeccion identificador de seccion
     * @return evaluaciones asociadas
     */
    public List<Evaluacion> listarPorSeccion(Integer idSeccion) {
        return evaluacionRepository.findBySeccion_IdSeccion(idSeccion);
    }

    /**
     * Crea una evaluacion asociada a una seccion.
     *
     * @param evaluacion datos de evaluacion
     * @param idSeccion identificador de seccion
     * @return evaluacion creada
     */
    @Transactional
    public Evaluacion crear(Evaluacion evaluacion, Integer idSeccion) {
        validarPorcentaje(evaluacion.getPorcentaje());
        validarNombre(evaluacion.getNombre());

        if (evaluacionRepository.existsByNombreAndSeccion_IdSeccion(evaluacion.getNombre(), idSeccion)) {
            throw new RuntimeException("Ya existe una evaluacion con ese nombre en la seccion indicada.");
        }

        Seccion seccion = obtenerSeccion(idSeccion);
        evaluacion.setSeccion(seccion);

        return evaluacionRepository.save(evaluacion);
    }

    /**
     * Actualiza una evaluacion existente.
     *
     * @param id identificador de evaluacion
     * @param datos nuevos datos
     * @param idSeccion identificador de seccion
     * @return evaluacion actualizada
     */
    @Transactional
    public Evaluacion actualizar(Integer id, Evaluacion datos, Integer idSeccion) {
        Evaluacion existente = buscarPorId(id);
        validarPorcentaje(datos.getPorcentaje());
        validarNombre(datos.getNombre());

        boolean cambioNombre = !existente.getNombre().equals(datos.getNombre());
        boolean cambioSeccion = !existente.getSeccion().getIdSeccion().equals(idSeccion);
        if ((cambioNombre || cambioSeccion)
                && evaluacionRepository.existsByNombreAndSeccion_IdSeccion(datos.getNombre(), idSeccion)) {
            throw new RuntimeException("Ya existe una evaluacion con ese nombre en la seccion indicada.");
        }

        Seccion seccion = obtenerSeccion(idSeccion);
        existente.setNombre(datos.getNombre());
        existente.setPorcentaje(datos.getPorcentaje());
        existente.setSeccion(seccion);

        return evaluacionRepository.save(existente);
    }

    /**
     * Elimina una evaluacion por ID.
     *
     * @param id identificador de evaluacion
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!evaluacionRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Evaluacion no encontrada con ID: " + id);
        }
        evaluacionRepository.deleteById(id);
    }

    private Seccion obtenerSeccion(Integer idSeccion) {
        return seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new RuntimeException("Seccion no encontrada con ID: " + idSeccion));
    }

    private void validarPorcentaje(BigDecimal porcentaje) {
        if (porcentaje == null) {
            throw new RuntimeException("El porcentaje de la evaluacion es obligatorio.");
        }
        if (porcentaje.compareTo(BigDecimal.ZERO) <= 0 || porcentaje.compareTo(CIEN) > 0) {
            throw new RuntimeException("El porcentaje debe ser mayor a 0 y menor o igual a 100.");
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la evaluacion es obligatorio.");
        }
    }
}
