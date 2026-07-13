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

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de logica de negocio para Seccion.
 */
@Service
@RequiredArgsConstructor
public class SeccionService {

    private static final Pattern SECTION_SEQUENCE_PATTERN = Pattern.compile(".*-(\\d{3})$");

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

        Curso curso = obtenerCurso(idCurso);
        CicloAcademico ciclo = obtenerCicloAcademico(idCiclo);

        if (esTextoVacio(seccion.getCodigoSeccion())) {
            seccion.setCodigoSeccion(generarProximoCodigo(curso, ciclo));
        }

        if (seccionRepository.existsByCodigoSeccion(seccion.getCodigoSeccion())) {
            throw new RuntimeException(
                    "Ya existe una sección con el codigo: " + seccion.getCodigoSeccion());
        }

        if (seccion.getVacantes() <= 0) {
            throw new RuntimeException("Las vacantes deben ser mayor a 0.");
        }

        seccion.setCurso(curso);
        seccion.setCicloAcademico(ciclo);
        if (esTextoVacio(seccion.getColor())) {
            seccion.setColor(generarColor(seccion.getCodigoSeccion()));
        }

        return seccionRepository.save(seccion);
    }

    /**
     * Generates the next available section code for a course and academic cycle.
     *
     * @param idCurso course ID
     * @param idCiclo academic cycle ID
     * @return generated section code
     */
    public String generarProximoCodigo(Integer idCurso, Integer idCiclo) {
        return generarProximoCodigo(obtenerCurso(idCurso), obtenerCicloAcademico(idCiclo));
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

        Curso curso = obtenerCurso(idCurso);

        CicloAcademico ciclo = obtenerCicloAcademico(idCiclo);

        existente.setCodigoSeccion(datos.getCodigoSeccion());
        existente.setVacantes(datos.getVacantes());
        existente.setCicloAcademicoNombre(datos.getCicloAcademicoNombre());
        existente.setColor(datos.getColor());
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

    private Curso obtenerCurso(Integer idCurso) {
        return cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException(
                        "Curso no encontrado con ID: " + idCurso));
    }

    private CicloAcademico obtenerCicloAcademico(Integer idCiclo) {
        return cicloAcademicoRepository.findById(idCiclo)
                .orElseThrow(() -> new RuntimeException(
                        "Ciclo academico no encontrado con ID: " + idCiclo));
    }

    private String generarProximoCodigo(Curso curso, CicloAcademico ciclo) {
        String prefijo = construirPrefijoCodigo(curso, ciclo);
        int siguienteSecuencia = seccionRepository
                .findTopByCurso_IdCursoAndCicloAcademico_IdCicloAndCodigoSeccionStartingWithOrderByCodigoSeccionDesc(
                        curso.getIdCurso(),
                        ciclo.getIdCiclo(),
                        prefijo)
                .map(Seccion::getCodigoSeccion)
                .map(this::extraerSecuencia)
                .map(numero -> numero + 1)
                .orElse(1);

        return prefijo + String.format("%03d", siguienteSecuencia);
    }

    private String construirPrefijoCodigo(Curso curso, CicloAcademico ciclo) {
        return extraerSiglasCurso(curso.getNombre()) + "-" + extraerSufijoCiclo(ciclo.getNombre()) + "-";
    }

    private String extraerSiglasCurso(String nombreCurso) {
        String normalizado = Normalizer.normalize(nombreCurso, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase(Locale.ROOT);

        if (normalizado.length() >= 3) {
            return normalizado.substring(0, 3);
        }

        return String.format("%-3s", normalizado).replace(' ', 'X');
    }

    private String extraerSufijoCiclo(String nombreCiclo) {
        if (nombreCiclo == null || nombreCiclo.isBlank()) {
            return "GEN";
        }
        int guion = nombreCiclo.lastIndexOf('-');
        String sufijo = guion >= 0 ? nombreCiclo.substring(guion + 1) : nombreCiclo;
        return sufijo.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private Integer extraerSecuencia(String codigoSeccion) {
        Matcher matcher = SECTION_SEQUENCE_PATTERN.matcher(codigoSeccion);
        if (!matcher.matches()) {
            return 0;
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String generarColor(String codigoSeccion) {
        int hue = Math.abs(codigoSeccion.hashCode()) % 360;
        return hslToHex(hue, 70, 45);
    }

    private String hslToHex(int hue, int saturation, int lightness) {
        float s = saturation / 100f;
        float l = lightness / 100f;
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((hue / 60f) % 2 - 1));
        float m = l - c / 2;

        float red;
        float green;
        float blue;
        if (hue < 60) {
            red = c;
            green = x;
            blue = 0;
        } else if (hue < 120) {
            red = x;
            green = c;
            blue = 0;
        } else if (hue < 180) {
            red = 0;
            green = c;
            blue = x;
        } else if (hue < 240) {
            red = 0;
            green = x;
            blue = c;
        } else if (hue < 300) {
            red = x;
            green = 0;
            blue = c;
        } else {
            red = c;
            green = 0;
            blue = x;
        }

        return String.format(
                "#%02X%02X%02X",
                Math.round((red + m) * 255),
                Math.round((green + m) * 255),
                Math.round((blue + m) * 255));
    }

    private boolean esTextoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
