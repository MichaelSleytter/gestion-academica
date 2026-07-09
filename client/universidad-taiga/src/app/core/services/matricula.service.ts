import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import type { MatriculaResponse } from '../../models/matricula/matricula.response';

/**
 * Servicio para interactuar con el API de matrículas.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class MatriculaService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene las matrículas de una sección.
   *
   * @param idSeccion - ID de la sección
   * @returns Lista de matrículas con datos del estudiante
   */
  getMatriculasBySeccion(idSeccion: number): Promise<MatriculaResponse[]> {
    const url = `${this.apiBaseUrl}/matriculas/seccion/${idSeccion}`;
    return lastValueFrom(this.http.get<MatriculaResponse[]>(url));
  }

  /**
   * Matricula un estudiante en una sección.
   *
   * @param idEstudiante - ID del estudiante
   * @param idSeccion - ID de la sección
   * @returns Matrícula creada
   */
  matricular(idEstudiante: number, idSeccion: number): Promise<MatriculaResponse> {
    const url = `${this.apiBaseUrl}/matriculas`;
    const params = new HttpParams()
      .set('idEstudiante', idEstudiante.toString())
      .set('idSeccion', idSeccion.toString());
    return lastValueFrom(this.http.post<MatriculaResponse>(url, null, { params }));
  }

  /**
   * Cambia el estado de una matrícula.
   * Estados válidos: ACTIVA, RETIRADA, APROBADA, DESAPROBADA.
   *
   * @param idMatricula - ID de la matrícula
   * @param estado - Nuevo estado
   * @returns Matrícula actualizada
   */
  cambiarEstado(idMatricula: number, estado: string): Promise<MatriculaResponse> {
    const url = `${this.apiBaseUrl}/matriculas/${idMatricula}/estado`;
    const params = new HttpParams().set('estado', estado);
    return lastValueFrom(this.http.patch<MatriculaResponse>(url, null, { params }));
  }
}
