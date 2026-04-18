package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Curso;
import com.example.gestionacademica.repositories.CursoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de logica de negocio para Curso.
 */
@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    /**
     * Lista todos los cursos.
     */
    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    /**
     * Busca un curso por ID.
     */
    public Curso buscarPorId(Integer id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado con ID: " + id));
    }

    /**
     * Busca cursos por nombre (busqueda parcial).
     */
    public List<Curso> buscarPorNombre(String nombre) {
        return cursoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Crea un nuevo curso.
     */
    @Transactional
    public Curso crear(Curso curso) {
        if (cursoRepository.existsByNombre(curso.getNombre())) {
            throw new RuntimeException(
                    "Ya existe un curso con el nombre: " + curso.getNombre());
        }
        return cursoRepository.save(curso);
    }

    /**
     * Actualiza un curso existente.
     */
    @Transactional
    public Curso actualizar(Integer id, Curso datos) {
        Curso existente = buscarPorId(id);

        existente.setNombre(datos.getNombre());
        existente.setCreditos(datos.getCreditos());
        existente.setDescripcion(datos.getDescripcion());

        return cursoRepository.save(existente);
    }

    /**
     * Elimina un curso por ID.
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!cursoRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Curso no encontrado con ID: " + id);
        }
        cursoRepository.deleteById(id);
    }
}