import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { APP_API_URL } from '../tokens/api.tokens';
import type { EvaluacionResponse } from '../../models/evaluacion/evaluacion.response';
import type { HorarioResponse } from '../../models/horario/horario.response';

/**
 * Nota del estudiante con el ID de la evaluación asociada.
 */
export interface NotaConEvaluacionResponse {
  /** Valor numérico de la nota. */
  nota: number | null;
  /** ID de la evaluación a la que pertenece. */
  idEvaluacion: number;
  /** ID único de la nota (null si aún no tiene nota). */
  idNota?: number | null;
}

/**
 * Curso en el que el estudiante autenticado está matriculado.
 */
export interface CursoMatriculadoResponse {
  /** ID de la matrícula */
  idMatricula: number;
  /** Estado de la matrícula (ACTIVA, RETIRADA, etc.) */
  estado: string;
  /** ID de la sección */
  idSeccion: number;
  /** Código de la sección */
  codigoSeccion: string;
  /** Nombre del ciclo académico */
  cicloAcademicoNombre: string;
  /** ID del curso */
  idCurso: number;
  /** Nombre del curso */
  nombreCurso: string;
  /** Créditos del curso */
  creditos: number;
}

/**
 * Servicio para las vistas operativas del rol estudiante.
 * Sigue el mismo patrón que DocenteRoleService.
 */
@Injectable({ providedIn: 'root' })
export class EstudianteRoleService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene los cursos en los que el estudiante autenticado está matriculado.
   *
   * @returns Lista de cursos matriculados con datos de sección.
   */
  getMisCursos(): Promise<CursoMatriculadoResponse[]> {
    const url = `${this.apiBaseUrl}/matriculas/mis-cursos`;
    return lastValueFrom(this.http.get<CursoMatriculadoResponse[]>(url));
  }

  /**
   * Obtiene evaluaciones asociadas a una sección.
   *
   * @param idSeccion - ID de la sección.
   * @returns Lista de evaluaciones.
   */
  getEvaluacionesBySeccion(idSeccion: number): Promise<EvaluacionResponse[]> {
    return lastValueFrom(
      this.http.get<EvaluacionResponse[]>(`${this.apiBaseUrl}/evaluaciones/seccion/${idSeccion}`),
    );
  }

  /**
   * Obtiene las notas del estudiante autenticado para todas las evaluaciones
   * de una sección.
   *
   * @param idSeccion - ID de la sección.
   * @returns Lista de notas con evaluación asociada.
   */
  getMisNotasBySeccion(idSeccion: number): Promise<NotaConEvaluacionResponse[]> {
    return lastValueFrom(
      this.http.get<NotaConEvaluacionResponse[]>(
        `${this.apiBaseUrl}/notas/mis-notas/seccion/${idSeccion}`,
      ),
    );
  }

  getHorariosBySeccion(idSeccion: number): Promise<HorarioResponse[]> {
    return lastValueFrom(
      this.http.get<HorarioResponse[]>(`${this.apiBaseUrl}/horarios/seccion/${idSeccion}`),
    );
  }
}
