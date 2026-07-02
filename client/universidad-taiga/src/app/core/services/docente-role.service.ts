import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { APP_API_URL } from '../tokens/api.tokens';
import type { DocenteSeccionResponse } from '../../models/docente/docente-seccion.response';
import type { SeccionResponse } from '../../models/seccion/seccion.response';
import type { EvaluacionResponse } from '../../models/evaluacion/evaluacion.response';
import type { MatriculaResponse } from '../../models/matricula';
import type { NotaRequest, NotaResponse } from '../../models/nota';

/**
 * Servicio para las vistas operativas del rol docente.
 */
@Injectable({ providedIn: 'root' })
export class DocenteRoleService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene las secciones asignadas a un docente.
   *
   * @param idDocente - ID del docente autenticado.
   * @returns Lista de secciones asignadas.
   */
  async getSeccionesByDocente(idDocente: number): Promise<SeccionResponse[]> {
    const asignaciones = await lastValueFrom(
      this.http.get<DocenteSeccionResponse[]>(`${this.apiBaseUrl}/docentes-secciones/docente/${idDocente}`),
    );

    const secciones = await Promise.all(
      asignaciones.map((asignacion) =>
        asignacion.seccion
          ? Promise.resolve(asignacion.seccion)
          : this.getSeccionById(asignacion.id.idSeccion),
      ),
    );

    return secciones;
  }

  /**
   * Obtiene una sección por ID.
   *
   * @param idSeccion - ID de la sección.
   * @returns Sección encontrada.
   */
  getSeccionById(idSeccion: number): Promise<SeccionResponse> {
    return lastValueFrom(this.http.get<SeccionResponse>(`${this.apiBaseUrl}/secciones/${idSeccion}`));
  }

  /**
   * Obtiene evaluaciones asociadas a una sección.
   *
   * @param idSeccion - ID de la sección.
   * @returns Lista de evaluaciones.
   */
  getEvaluacionesBySeccion(idSeccion: number): Promise<EvaluacionResponse[]> {
    return lastValueFrom(this.http.get<EvaluacionResponse[]>(`${this.apiBaseUrl}/evaluaciones/seccion/${idSeccion}`));
  }

  /**
   * Obtiene matrículas asociadas a una sección.
   *
   * @param idSeccion - ID de la sección.
   * @returns Lista de matrículas.
   */
  getMatriculasBySeccion(idSeccion: number): Promise<MatriculaResponse[]> {
    return lastValueFrom(this.http.get<MatriculaResponse[]>(`${this.apiBaseUrl}/matriculas/seccion/${idSeccion}`));
  }

  /**
   * Obtiene notas por evaluación.
   *
   * @param idEvaluacion - ID de la evaluación.
   * @returns Lista de notas.
   */
  getNotasByEvaluacion(idEvaluacion: number): Promise<NotaResponse[]> {
    return lastValueFrom(this.http.get<NotaResponse[]>(`${this.apiBaseUrl}/notas/evaluacion/${idEvaluacion}`));
  }

  /**
   * Crea una nota para estudiante y evaluación.
   *
   * @param nota - Valor de la nota.
   * @param idEvaluacion - ID de la evaluación.
   * @param idEstudiante - ID del estudiante.
   * @returns Nota creada.
   */
  createNota(nota: NotaRequest, idEvaluacion: number, idEstudiante: number): Promise<NotaResponse> {
    const params = new HttpParams()
      .set('idEvaluacion', idEvaluacion.toString())
      .set('idEstudiante', idEstudiante.toString());

    return lastValueFrom(this.http.post<NotaResponse>(`${this.apiBaseUrl}/notas`, nota, { params }));
  }

  /**
   * Actualiza una nota existente.
   *
   * @param idNota - ID de la nota.
   * @param nota - Valor actualizado.
   * @param idEvaluacion - ID de la evaluación.
   * @param idEstudiante - ID del estudiante.
   * @returns Nota actualizada.
   */
  updateNota(idNota: number, nota: NotaRequest, idEvaluacion: number, idEstudiante: number): Promise<NotaResponse> {
    const params = new HttpParams()
      .set('idEvaluacion', idEvaluacion.toString())
      .set('idEstudiante', idEstudiante.toString());

    return lastValueFrom(this.http.put<NotaResponse>(`${this.apiBaseUrl}/notas/${idNota}`, nota, { params }));
  }
}
