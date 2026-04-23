package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.repositories.TipoDocumentoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link TipoDocumento}.
 */
@Service
@RequiredArgsConstructor
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    /**
     * Lista todos los tipos de documento.
     *
     * @return tipos de documento
     */
    public List<TipoDocumento> listarTodos() {
        return tipoDocumentoRepository.findAll();
    }

    /**
     * Busca un tipo de documento por ID.
     *
     * @param id identificador
     * @return tipo de documento encontrado
     */
    public TipoDocumento buscarPorId(Integer id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado con ID: " + id));
    }

    /**
     * Crea un nuevo tipo de documento.
     *
     * @param tipoDocumento datos a crear
     * @return tipo de documento creado
     */
    @Transactional
    public TipoDocumento crear(TipoDocumento tipoDocumento) {
        validarNombre(tipoDocumento.getNombre());
        if (tipoDocumentoRepository.existsByNombreIgnoreCase(tipoDocumento.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de documento con el nombre: " + tipoDocumento.getNombre());
        }
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    /**
     * Actualiza un tipo de documento.
     *
     * @param id identificador a actualizar
     * @param datos datos nuevos
     * @return tipo de documento actualizado
     */
    @Transactional
    public TipoDocumento actualizar(Integer id, TipoDocumento datos) {
        TipoDocumento existente = buscarPorId(id);
        validarNombre(datos.getNombre());

        boolean nombreCambio = !existente.getNombre().equalsIgnoreCase(datos.getNombre());
        if (nombreCambio && tipoDocumentoRepository.existsByNombreIgnoreCase(datos.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de documento con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        return tipoDocumentoRepository.save(existente);
    }

    /**
     * Elimina un tipo de documento.
     *
     * @param id identificador
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!tipoDocumentoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Tipo de documento no encontrado con ID: " + id);
        }
        tipoDocumentoRepository.deleteById(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre del tipo de documento es obligatorio.");
        }
    }
}
