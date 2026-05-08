package com.example.gestionacademica.catalogos.service;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para la entidad {@link Carrera}.
 */
@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;

    /**
     * Lista todas las carreras registradas.
     *
     * @return lista de carreras
     */
    public List<Carrera> listarTodas() {
        return carreraRepository.findAll();
    }

    /**
     * Busca una carrera por su identificador.
     *
     * @param id identificador de carrera
     * @return carrera encontrada
     */
    public Carrera buscarPorId(Integer id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada con ID: " + id));
    }

    /**
     * Crea una nueva carrera validando nombre unico.
     *
     * @param carrera datos a persistir
     * @return carrera creada
     */
    @Transactional
    public Carrera crear(Carrera carrera) {
        validarNombreCarrera(carrera.getNombre());
        if (carreraRepository.existsByNombre(carrera.getNombre())) {
            throw new RuntimeException("Ya existe una carrera con el nombre: " + carrera.getNombre());
        }
        return carreraRepository.save(carrera);
    }

    /**
     * Actualiza una carrera existente.
     *
     * @param id identificador de carrera
     * @param datos nuevos datos
     * @return carrera actualizada
     */
    @Transactional
    public Carrera actualizar(Integer id, Carrera datos) {
        Carrera existente = buscarPorId(id);
        validarNombreCarrera(datos.getNombre());

        boolean nombreCambio = !existente.getNombre().equals(datos.getNombre());
        if (nombreCambio && carreraRepository.existsByNombre(datos.getNombre())) {
            throw new RuntimeException("Ya existe una carrera con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        return carreraRepository.save(existente);
    }

    /**
     * Elimina una carrera por su identificador.
     *
     * @param id identificador de carrera
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!carreraRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Carrera no encontrada con ID: " + id);
        }
        carreraRepository.deleteById(id);
    }

    private void validarNombreCarrera(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la carrera es obligatorio.");
        }
    }
}
