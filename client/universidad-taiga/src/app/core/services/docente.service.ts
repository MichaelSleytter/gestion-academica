import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { DocenteResponse } from '../../models/docente/docente.response';
import { PageResponse } from '../../models/shared/page.response';
import { DocenteCreateRequest, DocenteUpdateRequest } from '../../models/docente/docente.request';

/**
 * Servicio para interactuar con el API de docentes.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class DocenteService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene docentes con paginación y búsqueda opcional.
   *
   * @param pagina   número de página (0-based)
   * @param tamaño   elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de docentes
   */
  getDocentesPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<DocenteResponse>> {
    const url = `${this.apiBaseUrl}/docentes`;
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamaño', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(this.http.get<PageResponse<DocenteResponse>>(url, { params }));
  }

  /**
   * Elimina un docente por ID.
   *
   * @param id identificador del docente
   */
  eliminarDocente(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/docentes/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }

  /**
   * Crea un nuevo docente.
   *
   * @param docente datos del docente a crear
   * @returns docente creado
   */
  crearDocente(docente: DocenteCreateRequest): Promise<DocenteResponse> {
    const url = `${this.apiBaseUrl}/docentes`;
    return lastValueFrom(this.http.post<DocenteResponse>(url, docente));
  }

  /**
   * Actualiza un docente existente.
   *
   * @param id      identificador del docente
   * @param docente datos actualizados
   * @returns docente actualizado
   */
  actualizarDocente(id: number, docente: DocenteUpdateRequest): Promise<DocenteResponse> {
    const url = `${this.apiBaseUrl}/docentes/${id}`;
    return lastValueFrom(this.http.put<DocenteResponse>(url, docente));
  }
}
