import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { SeccionResponse } from '../../models/seccion/seccion.response';
import { SeccionCreateRequest } from '../../models/seccion/seccion.request';
import { PageResponse } from '../../models/shared/page.response';
import { CursoResponse } from '../../models/curso/curso.response';

/**
 * Servicio para interactuar con el API de secciones.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class SeccionService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene secciones con paginación y búsqueda opcional.
   *
   * @param pagina  número de página (0-based)
   * @param tamaño  elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de secciones
   */
  getSeccionesPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<SeccionResponse>> {
    const url = `${this.apiBaseUrl}/secciones`;
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamaño', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(this.http.get<PageResponse<SeccionResponse>>(url, { params }));
  }

  /**
   * Obtiene la lista de cursos para el dropdown del formulario.
   * Usa el endpoint paginado de cursos con un tamaño grande.
   *
   * @returns lista de cursos
   */
  getCursosList(): Promise<PageResponse<CursoResponse>> {
    const url = `${this.apiBaseUrl}/cursos`;
    const params = new HttpParams()
      .set('pagina', '0')
      .set('tamaño', '100');
    return lastValueFrom(this.http.get<PageResponse<CursoResponse>>(url, { params }));
  }

  /**
   * Obtiene la lista de ciclos académicos para el dropdown del formulario.
   *
   * @returns lista de ciclos académicos
   */
  getCiclosAcademicosList(): Promise<CicloAcademicoResponse[]> {
    const url = `${this.apiBaseUrl}/ciclos-academicos`;
    return lastValueFrom(this.http.get<CicloAcademicoResponse[]>(url));
  }

  /**
   * Crea una nueva sección.
   *
   * @param seccion datos de la sección
   * @param idCurso ID del curso
   * @param idCiclo ID del ciclo académico
   * @returns sección creada
   */
  crearSeccion(seccion: SeccionCreateRequest, idCurso: number, idCiclo: number): Promise<SeccionResponse> {
    const url = `${this.apiBaseUrl}/secciones?idCurso=${idCurso}&idCiclo=${idCiclo}`;
    return lastValueFrom(this.http.post<SeccionResponse>(url, seccion));
  }

  /**
   * Actualiza una sección existente.
   *
   * @param id ID de la sección
   * @param seccion datos actualizados
   * @param idCurso ID del curso
   * @param idCiclo ID del ciclo académico
   * @returns sección actualizada
   */
  actualizarSeccion(id: number, seccion: SeccionCreateRequest, idCurso: number, idCiclo: number): Promise<SeccionResponse> {
    const url = `${this.apiBaseUrl}/secciones/${id}?idCurso=${idCurso}&idCiclo=${idCiclo}`;
    return lastValueFrom(this.http.put<SeccionResponse>(url, seccion));
  }

  /**
   * Elimina una sección por ID.
   *
   * @param id ID de la sección a eliminar
   */
  eliminarSeccion(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/secciones/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }
}

/**
 * Respuesta mínima de un ciclo académico para el dropdown.
 */
export interface CicloAcademicoResponse {
  idCiclo: number;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
}
