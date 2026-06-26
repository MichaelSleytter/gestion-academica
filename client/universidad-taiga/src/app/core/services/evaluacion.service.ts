import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { EvaluacionResponse } from '../../models/evaluacion/evaluacion.response';
import { EvaluacionCreateRequest } from '../../models/evaluacion/evaluacion.request';
import { PageResponse } from '../../models/shared/page.response';

/**
 * Servicio para interactuar con el API de evaluaciones.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class EvaluacionService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene evaluaciones con paginación y búsqueda opcional.
   *
   * @param pagina   número de página (0-based)
   * @param tamaño   elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de evaluaciones
   */
  getEvaluacionesPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<EvaluacionResponse>> {
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamano', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(
      this.http.get<PageResponse<EvaluacionResponse>>(`${this.apiBaseUrl}/evaluaciones`, { params }),
    );
  }

  /**
   * Crea una nueva evaluación.
   *
   * @param evaluacion datos de la evaluación
   * @param idSeccion  ID de la sección asociada
   * @returns evaluación creada
   */
  crearEvaluacion(evaluacion: EvaluacionCreateRequest, idSeccion: number): Promise<EvaluacionResponse> {
    const url = `${this.apiBaseUrl}/evaluaciones?idSeccion=${idSeccion}`;
    return lastValueFrom(this.http.post<EvaluacionResponse>(url, evaluacion));
  }

  /**
   * Actualiza una evaluación existente.
   *
   * @param id         ID de la evaluación
   * @param evaluacion datos actualizados
   * @param idSeccion  ID de la sección asociada
   * @returns evaluación actualizada
   */
  actualizarEvaluacion(id: number, evaluacion: EvaluacionCreateRequest, idSeccion: number): Promise<EvaluacionResponse> {
    const url = `${this.apiBaseUrl}/evaluaciones/${id}?idSeccion=${idSeccion}`;
    return lastValueFrom(this.http.put<EvaluacionResponse>(url, evaluacion));
  }

  /**
   * Elimina una evaluación por ID.
   *
   * @param id ID de la evaluación a eliminar
   */
  eliminarEvaluacion(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/evaluaciones/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }

  /**
   * Obtiene la lista de secciones para el dropdown del formulario.
   * Usa el endpoint paginado de secciones con un tamaño grande.
   *
   * @returns página de secciones
   */
  getSeccionesList(): Promise<PageResponse<EvaluacionResponse['seccion']>> {
    const url = `${this.apiBaseUrl}/secciones`;
    const params = new HttpParams()
      .set('pagina', '0')
      .set('tamano', '100');
    return lastValueFrom(this.http.get<PageResponse<EvaluacionResponse['seccion']>>(url, { params }));
  }
}
