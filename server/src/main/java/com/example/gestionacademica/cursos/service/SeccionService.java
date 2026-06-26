package com.example.gestionacademica.cursos.service;

import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.cursos.repository.CicloAcademicoRepository;
import com.example.gestionacademica.cursos.repository.CursoRepository;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de logica de negocio para Seccion.
 */
@Service
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final CursoRepository cursoRepository;
    private final CicloAcademicoRepository cicloAcademicoRepository;
    private final MatriculaRepository matriculaRepository;

    /**
     * Lista todas las secciones.
     */
    public List<Seccion> listarTodas() {
        return seccionRepository.findAll();
    }

    /**
     * Busca secciones con paginación y filtro de búsqueda opcional.
     * <p>
     * La búsqueda se aplica sobre: código de sección y nombre del ciclo académico.
     *
     * @param busqueda   texto para filtrar (opcional)
     * @param paginacion objeto con página, tamaño y ordenamiento
     * @return página de secciones que coinciden con el filtro
     */
    public Page<Seccion> listarPaginado(String busqueda, Pageable paginacion) {
        Specification<Seccion> spec = (root, query, cb) -> {
            if (busqueda == null || busqueda.isBlank()) {
                return cb.conjunction();
            }
            String patron = "%" + busqueda.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("codigoSeccion")), patron),
                    cb.like(cb.lower(root.get("cicloAcademicoNombre")), patron)
            );
        };
        return seccionRepository.findAll(spec, paginacion);
    }

    /**
     * Busca una sección por ID.
     */
    public Seccion buscarPorId(Integer id) {
        return seccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Sección no encontrada con ID: " + id));
    }

    /**
     * Lista secciones por curso.
     */
    public List<Seccion> listarPorCurso(Integer idCurso) {
        return seccionRepository.findByCurso_IdCurso(idCurso);
    }

    /**
     * Lista secciones por ciclo academico.
     */
    public List<Seccion> listarPorCiclo(Integer idCiclo) {
        return seccionRepository.findByCicloAcademico_IdCiclo(idCiclo);
    }

    /**
     * Crea una nueva sección.
     * Valida que el codigo de sección sea unico.
     * Valida que haya vacantes disponibles (mayor a 0).
     */
    @Transactional
    public Seccion crear(Seccion seccion, Integer idCurso, Integer idCiclo) {

        if (seccionRepository.existsByCodigoSeccion(seccion.getCodigoSeccion())) {
            throw new RuntimeException(
                    "Ya existe una sección con el codigo: " + seccion.getCodigoSeccion());
        }

        if (seccion.getVacantes() <= 0) {
            throw new RuntimeException("Las vacantes deben ser mayor a 0.");
        }

        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado con ID: " + idCurso));

        CicloAcademico ciclo = cicloAcademicoRepository.findById(idCiclo)
                .orElseThrow(() -> new RuntimeException(
                        "Ciclo academico no encontrado con ID: " + idCiclo));

        seccion.setCurso(curso);
        seccion.setCicloAcademico(ciclo);

        return seccionRepository.save(seccion);
    }

    /**
     * Actualiza una sección existente.
     */
    @Transactional
    public Seccion actualizar(Integer id, Seccion datos, Integer idCurso, Integer idCiclo) {

        Seccion existente = buscarPorId(id);

        // Validar codigo unico si cambió
        if (!existente.getCodigoSeccion().equals(datos.getCodigoSeccion())
                && seccionRepository.existsByCodigoSeccion(datos.getCodigoSeccion())) {
            throw new RuntimeException(
                    "Ya existe una sección con el codigo: " + datos.getCodigoSeccion());
        }

        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado con ID: " + idCurso));

        CicloAcademico ciclo = cicloAcademicoRepository.findById(idCiclo)
                .orElseThrow(() -> new RuntimeException(
                        "Ciclo academico no encontrado con ID: " + idCiclo));

        existente.setCodigoSeccion(datos.getCodigoSeccion());
        existente.setVacantes(datos.getVacantes());
        existente.setCicloAcademicoNombre(datos.getCicloAcademicoNombre());
        existente.setCurso(curso);
        existente.setCicloAcademico(ciclo);

        return seccionRepository.save(existente);
    }

    /**
     * Elimina una sección.
     * Valida que no tenga matrículas activas antes de eliminar.
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!seccionRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Sección no encontrada con ID: " + id);
        }

        Long matriculados = matriculaRepository.countMatriculadosActivos(id);
        if (matriculados > 0) {
            throw new RuntimeException(
                    "No se puede eliminar la sección. Tiene " + matriculados + " matrícula(s) activa(s).");
        }

        seccionRepository.deleteById(id);
    }
}