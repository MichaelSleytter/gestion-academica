package com.example.gestionacademica.notas.repository;

import static org.assertj.core.api.Assertions.assertThat;


import com.example.gestionacademica.cursos.domain.CicloAcademico;
import com.example.gestionacademica.cursos.domain.Curso;
import com.example.gestionacademica.cursos.domain.Seccion;
import com.example.gestionacademica.estudiantes.domain.Estudiante;
import com.example.gestionacademica.evaluaciones.domain.Evaluacion;
import com.example.gestionacademica.historial.support.RepositoryProgressTestData;
import com.example.gestionacademica.notas.domain.Nota;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class NotaRepositoryProgressTest extends RepositoryProgressTestData {

    @Autowired
    private NotaRepository notaRepository;

    @Autowired
    private TestEntityManager em;

    private Estudiante estudiante;
    private Seccion seccion;
    private Evaluacion evaluacion;
    private Nota nota;

    @BeforeEach
    void setUp() {
        estudiante = estudiante(em);
        Curso curso = curso(em);
        CicloAcademico ciclo = ciclo(em);
        seccion = seccion(em, curso, ciclo);
        evaluacion = evaluacion(em, seccion);
        nota = nota(em, estudiante, evaluacion);
        em.flush();
    }

    @Test
    void fetchesNotesWithEvaluationForStudentAndSection() {
        List<Nota> result = notaRepository.findByEstudianteIdAndSeccionIdWithEvaluacion(
                estudiante.getIdUsuario(), seccion.getIdSeccion());

        assertThat(result).hasSize(1);
        Nota found = result.get(0);
        assertThat(found.getNota()).isEqualByComparingTo(new BigDecimal("16.00"));
        assertThat(found.getEvaluacion()).isNotNull();
        assertThat(found.getEvaluacion().getPorcentaje()).isEqualByComparingTo(new BigDecimal("40.00"));
    }

    @Test
    void fetchesNotesForMultipleSectionIds() {
        List<Nota> result = notaRepository.findByEstudianteIdAndSeccionIdsWithEvaluacion(
                estudiante.getIdUsuario(), List.of(seccion.getIdSeccion()));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEvaluacion().getNombre()).startsWith("Parcial-");
    }

    @Test
    void returnsEmptyForUnrelatedSection() {
        Curso otroCurso = curso(em);
        CicloAcademico otroCiclo = ciclo(em);
        Seccion otraSeccion = seccion(em, otroCurso, otroCiclo);

        List<Nota> result = notaRepository.findByEstudianteIdAndSeccionIdWithEvaluacion(
                estudiante.getIdUsuario(), otraSeccion.getIdSeccion());

        assertThat(result).isEmpty();
    }
}
