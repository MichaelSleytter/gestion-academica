package com.example.gestionacademica.cursos.service;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.repository.CicloAcademicoRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
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
     * Generates the two standard academic periods for a year.
     * Existing periods are returned without duplication.
     *
     * @param anio academic year
     * @return existing or created periods for the year
     */
    @Transactional
    public List<CicloAcademico> generarAnio(Integer anio) {
        validarAnio(anio);

        List<CicloAcademico> periodos = new ArrayList<>();
        periodos.add(obtenerOCrearPeriodo(anio + "-I", LocalDate.of(anio, 1, 1), LocalDate.of(anio, 6, 30)));
        periodos.add(obtenerOCrearPeriodo(anio + "-II", LocalDate.of(anio, 7, 1), LocalDate.of(anio, 12, 31)));
        return periodos;
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

    private CicloAcademico obtenerOCrearPeriodo(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        return cicloAcademicoRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    CicloAcademico ciclo = new CicloAcademico();
                    ciclo.setNombre(nombre);
                    ciclo.setFechaInicio(fechaInicio);
                    ciclo.setFechaFin(fechaFin);
                    return cicloAcademicoRepository.save(ciclo);
                });
    }

    private void validarAnio(Integer anio) {
        if (anio == null || anio < 2020 || anio > 2099) {
            throw new RuntimeException("El año academico debe estar entre 2020 y 2099.");
        }
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
