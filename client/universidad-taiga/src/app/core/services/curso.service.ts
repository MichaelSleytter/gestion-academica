import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { CursoResponse } from '../../models/curso/curso.response';
import { PageResponse } from '../../models/shared/page.response';
import { CursoCreateRequest } from '../../models/curso/curso.request';

/**
 * Servicio para interactuar con el API de cursos.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class CursoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene cursos con paginación y búsqueda opcional.
   *
   * @param pagina  número de página (0-based)
   * @param tamaño  elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de cursos
   */
  getCursosPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<CursoResponse>> {
    const url = `${this.apiBaseUrl}/cursos`;
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamaño', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(this.http.get<PageResponse<CursoResponse>>(url, { params }));
  }

  /**
   * Crea un nuevo curso.
   *
   * @param curso datos del curso a crear
   * @returns curso creado
   */
  crearCurso(curso: CursoCreateRequest): Promise<CursoResponse> {
    const url = `${this.apiBaseUrl}/cursos`;
    return lastValueFrom(this.http.post<CursoResponse>(url, curso));
  }

  /**
   * Actualiza un curso existente.
   *
   * @param id   ID del curso
   * @param curso datos actualizados
   * @returns curso actualizado
   */
  actualizarCurso(id: number, curso: CursoCreateRequest): Promise<CursoResponse> {
    const url = `${this.apiBaseUrl}/cursos/${id}`;
    return lastValueFrom(this.http.put<CursoResponse>(url, curso));
  }

  /**
   * Elimina un curso por ID.
   *
   * @param id ID del curso a eliminar
   */
  eliminarCurso(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/cursos/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }
}
