package com.example.gestionacademica.matriculas.service;

import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.matriculas.domain.Matricula;
import com.example.gestionacademica.matriculas.domain.MatriculaEstado;
import com.example.gestionacademica.matriculas.dto.MatriculaMisCursosDTO;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.repository.EstudianteRepository;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.example.gestionacademica.cursos.repository.SeccionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de logica de negocio para Matricula.
 */
@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final SeccionRepository seccionRepository;

    /**
     * Lista todas las matrículas.
     */
    public List<Matricula> listarTodas() {
        return matriculaRepository.findAll();
    }

    /**
     * Busca una matrícula por ID.
     */
    public Matricula buscarPorId(Integer id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Matrícula no encontrada con ID: " + id));
    }

    /**
     * Lista matrículas de un estudiante.
     */
    public List<Matricula> listarPorEstudiante(Integer idEstudiante) {
        return matriculaRepository.findByEstudiante_IdUsuario(idEstudiante);
    }

    /**
     * Obtiene los cursos activos del estudiante autenticado con datos
     * de sección, curso y ciclo académico.
     *
     * @param idEstudiante ID del estudiante (idUsuario).
     * @return Lista de DTOs con datos de matrícula, sección y curso.
     */
    public List<MatriculaMisCursosDTO> listarMisCursos(Integer idEstudiante) {
        return matriculaRepository.findActiveByEstudianteIdWithSeccionCurso(idEstudiante)
                .stream()
                .map(this::aMisCursosDTO)
                .toList();
    }

    private MatriculaMisCursosDTO aMisCursosDTO(Matricula matricula) {
        var seccion = matricula.getSeccion();
        var curso = seccion.getCurso();
        return new MatriculaMisCursosDTO(
                matricula.getIdMatricula(),
                matricula.getEstado().name(),
                seccion.getIdSeccion(),
                seccion.getCodigoSeccion(),
                seccion.getCicloAcademicoNombre(),
                curso.getIdCurso(),
                curso.getNombre(),
                curso.getCreditos()
        );
    }

    /**
     * Lista matrículas de una sección.
     */
    public List<Matricula> listarPorSeccion(Integer idSeccion) {
        return matriculaRepository.findBySeccion_IdSeccion(idSeccion);
    }

    /**
     * Registra una nueva matrícula.
     * Valida: duplicado, vacantes disponibles.
     */
    @Transactional
    public Matricula matricular(Integer idEstudiante, Integer idSeccion) {

        // Validar duplicado
        if (matriculaRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(
                idEstudiante, idSeccion)) {
            throw new RuntimeException(
                    "El estudiante ya está matriculado en esta sección.");
        }

        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new RuntimeException(
                        "Estudiante no encontrado con ID: " + idEstudiante));

        Seccion seccion = seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new RuntimeException(
                        "Sección no encontrada con ID: " + idSeccion));

        // Validar vacantes disponibles
        Long ocupados = matriculaRepository.countMatriculadosActivos(idSeccion);
        if (ocupados >= seccion.getVacantes()) {
            throw new RuntimeException(
                    "No hay vacantes disponibles en la sección: " + seccion.getCodigoSeccion());
        }

        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setSeccion(seccion);
        matricula.setFechaMatricula(LocalDateTime.now());
        matricula.setEstado(MatriculaEstado.ACTIVA);

        return matriculaRepository.save(matricula);
    }

    /**
     * Cambia el estado de una matrícula.
     * Estados válidos: ACTIVA, RETIRADA, APROBADA, DESAPROBADA.
     */
    @Transactional
    public Matricula cambiarEstado(Integer id, MatriculaEstado nuevoEstado) {
        Matricula matricula = buscarPorId(id);
        matricula.setEstado(nuevoEstado);
        return matriculaRepository.save(matricula);
    }

    /**
     * Elimina una matrícula por ID.
     */
    @Transactional
    public void eliminar(Integer id) {
        if (!matriculaRepository.existsById(id)) {
            throw new RuntimeException(
                    "No se puede eliminar. Matrícula no encontrada con ID: " + id);
        }
        matriculaRepository.deleteById(id);
    }
}