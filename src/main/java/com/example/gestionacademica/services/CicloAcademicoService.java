package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.CicloAcademico;
import com.example.gestionacademica.repositories.CicloAcademicoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link CicloAcademico}.
 */
@Service
@RequiredArgsConstructor
public class CicloAcademicoService {

    private final CicloAcademicoRepository cicloAcademicoRepository;

    /**
     * Lista los ciclos academicos registrados.
     *
     * @return ciclos academicos
     */
    public List<CicloAcademico> listarTodos() {
        return cicloAcademicoRepository.findAll();
    }

    /**
     * Busca un ciclo academico por ID.
     *
     * @param id identificador del ciclo
     * @return ciclo encontrado
     */
    public CicloAcademico buscarPorId(Integer id) {
        return cicloAcademicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ciclo academico no encontrado con ID: " + id));
    }

    /**
     * Crea un ciclo academico.
     *
     * @param ciclo datos a registrar
     * @return ciclo creado
     */
    @Transactional
    public CicloAcademico crear(CicloAcademico ciclo) {
        validarFechas(ciclo);
        if (cicloAcademicoRepository.findByNombre(ciclo.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un ciclo academico con el nombre: " + ciclo.getNombre());
        }
        return cicloAcademicoRepository.save(ciclo);
    }

    /**
     * Actualiza un ciclo academico existente.
     *
     * @param id identificador del ciclo
     * @param datos nuevos datos
     * @return ciclo actualizado
     */
    @Transactional
    public CicloAcademico actualizar(Integer id, CicloAcademico datos) {
        CicloAcademico existente = buscarPorId(id);
        validarFechas(datos);

        boolean nombreCambio = !existente.getNombre().equals(datos.getNombre());
        if (nombreCambio && cicloAcademicoRepository.findByNombre(datos.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un ciclo academico con el nombre: " + datos.getNombre());
        }

        existente.setNombre(datos.getNombre());
        existente.setFechaInicio(datos.getFechaInicio());
        existente.setFechaFin(datos.getFechaFin());

        return cicloAcademicoRepository.save(existente);
    }

    /**
     * Elimina un ciclo academico por ID.
     *
     * @param id identificador del ciclo
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!cicloAcademicoRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Ciclo academico no encontrado con ID: " + id);
        }
        cicloAcademicoRepository.deleteById(id);
    }

    private void validarFechas(CicloAcademico ciclo) {
        if (ciclo.getNombre() == null || ciclo.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del ciclo academico es obligatorio.");
        }
        if (ciclo.getFechaInicio() == null || ciclo.getFechaFin() == null) {
            throw new RuntimeException("Las fechas de inicio y fin son obligatorias.");
        }
        if (!ciclo.getFechaInicio().isBefore(ciclo.getFechaFin())) {
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin.");
        }
    }
}
