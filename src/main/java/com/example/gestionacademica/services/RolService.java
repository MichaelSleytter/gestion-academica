package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Rol;
import com.example.gestionacademica.repositories.RolRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link Rol}.
 */
@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    /**
     * Lista todos los roles.
     *
     * @return roles del sistema
     */
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    /**
     * Busca un rol por ID.
     *
     * @param id identificador del rol
     * @return rol encontrado
     */
    public Rol buscarPorId(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
    }

    /**
     * Crea un nuevo rol.
     *
     * @param rol datos del rol
     * @return rol creado
     */
    @Transactional
    public Rol crear(Rol rol) {
        validarNombre(rol.getNombre());
        if (rolRepository.existsByNombreIgnoreCase(rol.getNombre())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + rol.getNombre());
        }
        return rolRepository.save(rol);
    }

    /**
     * Actualiza un rol existente.
     *
     * @param id identificador del rol
     * @param datos nuevos datos
     * @return rol actualizado
     */
    @Transactional
    public Rol actualizar(Integer id, Rol datos) {
        Rol existente = buscarPorId(id);
        validarNombre(datos.getNombre());

        boolean nombreCambio = !existente.getNombre().equalsIgnoreCase(datos.getNombre());
        if (nombreCambio && rolRepository.existsByNombreIgnoreCase(datos.getNombre())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        return rolRepository.save(existente);
    }

    /**
     * Elimina un rol por ID.
     *
     * @param id identificador del rol
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!rolRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Rol no encontrado con ID: " + id);
        }
        rolRepository.deleteById(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre del rol es obligatorio.");
        }
    }
}
