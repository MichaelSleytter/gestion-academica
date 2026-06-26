import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { EstudianteResponse } from '../../models/estudiante/estudiante.response';
import { PageResponse } from '../../models/shared/page.response';
import {
  EstudianteCreateRequest,
  EstudianteUpdateRequest,
} from '../../models/estudiante/estudiante.request';

/**
 * Servicio para interactuar con el API de estudiantes.
 * La URL base se inyecta mediante APP_API_URL, que se configura en providers
 * usando los archivos de entorno (environment.ts / environment.prod.ts).
 */
@Injectable({
  providedIn: 'root',
})
export class EstudianteService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene todos los estudiantes desde el endpoint /estudiantes
   * @returns Promise<EstudianteResponse[]>
   */
  getEstudiantes(): Promise<EstudianteResponse[]> {
    const url = `${this.apiBaseUrl}/estudiantes`;
    return lastValueFrom(this.http.get<EstudianteResponse[]>(url));
  }

  /**
   * Obtiene estudiantes con paginación y búsqueda opcional.
   *
   * @param pagina  número de página (0-based)
   * @param tamaño  elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de estudiantes
   */
  getEstudiantesPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<EstudianteResponse>> {
    const url = `${this.apiBaseUrl}/estudiantes`;
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamaño', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(this.http.get<PageResponse<EstudianteResponse>>(url, { params }));
  }

  crearEstudiante(estudiante: EstudianteCreateRequest): Promise<EstudianteResponse> {
    const url = `${this.apiBaseUrl}/estudiantes`;
    return lastValueFrom(this.http.post<EstudianteResponse>(url, estudiante));
  }

  eliminarEstudiante(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/estudiantes/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }

  actualizarEstudiante(
    id: number,
    estudiante: EstudianteUpdateRequest,
  ): Promise<EstudianteResponse> {
    const url = `${this.apiBaseUrl}/estudiantes/${id}`;
    return lastValueFrom(this.http.put<EstudianteResponse>(url, estudiante));
  }
}
