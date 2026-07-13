package com.example.gestionacademica.catalogos.service;

import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.catalogos.repository.EspecializacionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Business service for {@link Especializacion}.
 */
@Service
@RequiredArgsConstructor
public class EspecializacionService {

    private final EspecializacionRepository especializacionRepository;

    /**
     * Lists all registered specializations.
     *
     * @return registered specializations
     */
    public List<Especializacion> listarTodas() {
        return especializacionRepository.findAll();
    }

    /**
     * Finds a specialization by ID.
     *
     * @param id specialization ID
     * @return found specialization
     */
    public Especializacion buscarPorId(Integer id) {
        return especializacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especializacion no encontrada con ID: " + id));
    }

    /**
     * Creates a specialization after validating the name.
     *
     * @param especializacion data to persist
     * @return created specialization
     */
    @Transactional
    public Especializacion crear(Especializacion especializacion) {
        validarNombre(especializacion.getNombre());
        if (especializacionRepository.existsByNombre(especializacion.getNombre())) {
            throw new RuntimeException("Ya existe una especializacion con el nombre: " + especializacion.getNombre());
        }
        return especializacionRepository.save(especializacion);
    }

    /**
     * Updates an existing specialization.
     *
     * @param id specialization ID
     * @param datos updated data
     * @return updated specialization
     */
    @Transactional
    public Especializacion actualizar(Integer id, Especializacion datos) {
        Especializacion existente = buscarPorId(id);
        validarNombre(datos.getNombre());

        boolean nombreCambio = !existente.getNombre().equals(datos.getNombre());
        if (nombreCambio && especializacionRepository.existsByNombre(datos.getNombre())) {
            throw new RuntimeException("Ya existe una especializacion con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        return especializacionRepository.save(existente);
    }

    /**
     * Deletes a specialization by ID.
     *
     * @param id specialization ID
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!especializacionRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Especializacion no encontrada con ID: " + id);
        }
        especializacionRepository.deleteById(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la especializacion es obligatorio.");
        }
    }
}
