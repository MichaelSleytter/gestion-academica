package com.example.gestionacademica.cursos.service;

import com.example.gestionacademica.cursos.domain.Horario;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.HorarioRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Servicio de negocio para {@link Horario}.
 */
@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final SeccionRepository seccionRepository;

    /**
     * Lista todos los horarios.
     *
     * @return horarios registrados
     */
    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    /**
     * Lista horarios con paginación y búsqueda opcional.
     *
     * @param busqueda  texto para filtrar por díaSemana o aula (opcional)
     * @param paginacion objeto Pageable con página y tamaño
     * @return página de horarios
     */
    public Page<Horario> listarPaginado(String busqueda, Pageable paginacion) {
        Specification<Horario> spec = (root, query, cb) -> {
            if (busqueda == null || busqueda.isBlank()) return cb.conjunction();
            String patron = "%" + busqueda.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("diaSemana")), patron),
                cb.like(cb.lower(root.get("aula")), patron)
            );
        };
        return horarioRepository.findAll(spec, paginacion);
    }

    /**
     * Busca un horario por ID.
     *
     * @param id identificador del horario
     * @return horario encontrado
     */
    public Horario buscarPorId(Integer id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + id));
    }

    /**
     * Lista horarios de una seccion.
     *
     * @param idSeccion identificador de seccion
     * @return horarios de la seccion
     */
    public List<Horario> listarPorSeccion(Integer idSeccion) {
        return horarioRepository.findBySeccion_IdSeccion(idSeccion);
    }

    /**
     * Crea un nuevo horario.
     *
     * @param horario datos del horario
     * @param idSeccion identificador de seccion
     * @return horario creado
     */
    @Transactional
    public Horario crear(Horario horario, Integer idSeccion) {
        validarRangoHorario(horario);

        Seccion seccion = obtenerSeccion(idSeccion);
        horario.setSeccion(seccion);

        return horarioRepository.save(horario);
    }

    /**
     * Actualiza un horario existente.
     *
     * @param id identificador del horario
     * @param datos nuevos datos
     * @param idSeccion identificador de seccion
     * @return horario actualizado
     */
    @Transactional
    public Horario actualizar(Integer id, Horario datos, Integer idSeccion) {
        Horario existente = buscarPorId(id);
        validarRangoHorario(datos);

        Seccion seccion = obtenerSeccion(idSeccion);
        existente.setDiaSemana(datos.getDiaSemana());
        existente.setHoraInicio(datos.getHoraInicio());
        existente.setHoraFin(datos.getHoraFin());
        existente.setAula(datos.getAula());
        existente.setSeccion(seccion);

        return horarioRepository.save(existente);
    }

    /**
     * Elimina un horario por ID.
     *
     * @param id identificador del horario
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!horarioRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. Horario no encontrado con ID: " + id);
        }
        horarioRepository.deleteById(id);
    }

    private Seccion obtenerSeccion(Integer idSeccion) {
        return seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new RuntimeException("Seccion no encontrada con ID: " + idSeccion));
    }

    private void validarRangoHorario(Horario horario) {
        if (horario.getDiaSemana() == null || horario.getDiaSemana().trim().isEmpty()) {
            throw new RuntimeException("El dia de la semana es obligatorio.");
        }
        if (horario.getHoraInicio() == null || horario.getHoraFin() == null) {
            throw new RuntimeException("La hora de inicio y fin son obligatorias.");
        }
        if (!horario.getHoraInicio().isBefore(horario.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser anterior a la hora fin.");
        }
    }
}
