package com.example.gestionacademica.catalogos.service;

import com.example.gestionacademica.catalogos.domain.GradoAcademico;
import com.example.gestionacademica.catalogos.repository.GradoAcademicoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link GradoAcademico}.
 */
@Service
@RequiredArgsConstructor
public class GradoAcademicoService {

    private final GradoAcademicoRepository gradoAcademicoRepository;

    /**
     * Lista todos los grados academicos.
     *
     * @return grados academicos
     */
    public List<GradoAcademico> listarTodos() {
        return gradoAcademicoRepository.findAll();
    }

    /**
     * Busca un grado academico por ID.
     *
     * @param id identificador del grado
     * @return grado encontrado
     */
    public GradoAcademico buscarPorId(Integer id) {
        return gradoAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grado academico no encontrado con ID: " + id));
    }

    /**
     * Crea un grado academico.
     *
     * @param grado datos a registrar
     * @return grado creado
     */
    @Transactional
    public GradoAcademico crear(GradoAcademico grado) {
        validarNombre(grado.getNombre());
        if (gradoAcademicoRepository.existsByNombreIgnoreCase(grado.getNombre())) {
            throw new RuntimeException("Ya existe un grado academico con el nombre: " + grado.getNombre());
        }
        return gradoAcademicoRepository.save(grado);
    }

    /**
     * Actualiza un grado academico existente.
     *
     * @param id identificador del grado
     * @param datos nuevos datos
     * @return grado actualizado
     */
    @Transactional
    public GradoAcademico actualizar(Integer id, GradoAcademico datos) {
        GradoAcademico existente = buscarPorId(id);
        validarNombre(datos.getNombre());

        boolean nombreCambio = !existente.getNombre().equalsIgnoreCase(datos.getNombre());
        if (nombreCambio && gradoAcademicoRepository.existsByNombreIgnoreCase(datos.getNombre())) {
            throw new RuntimeException("Ya existe un grado academico con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        return gradoAcademicoRepository.save(existente);
    }

    /**
     * Elimina un grado academico.
     *
     * @param id identificador del grado
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!gradoAcademicoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Grado academico no encontrado con ID: " + id);
        }
        gradoAcademicoRepository.deleteById(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre del grado academico es obligatorio.");
        }
    }
}
