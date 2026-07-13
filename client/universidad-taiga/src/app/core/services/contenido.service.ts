import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import type { CursoContenidoResponse } from '../../models/contenido/curso-contenido.response';

/**
 * Servicio para interactuar con el API de contenido de cursos.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class ContenidoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Lista el contenido activo de una sección.
   *
   * @param idSeccion identificador de la sección
   * @returns lista de contenido activo
   */
  listarPorSeccion(idSeccion: number): Promise<CursoContenidoResponse[]> {
    const url = `${this.apiBaseUrl}/contenido/seccion/${idSeccion}`;
    return lastValueFrom(this.http.get<CursoContenidoResponse[]>(url));
  }

  subirArchivo(idSeccion: number, semana: number, file: File): Promise<CursoContenidoResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('idSeccion', String(idSeccion));
    formData.append('semana', String(semana));

    return lastValueFrom(this.http.post<CursoContenidoResponse>(`${this.apiBaseUrl}/contenido/upload`, formData));
  }

  /**
   * Elimina (soft delete) un contenido por ID.
   *
   * @param idContenido identificador del contenido
   */
  eliminar(idContenido: number): Promise<void> {
    const url = `${this.apiBaseUrl}/contenido/${idContenido}`;
    return lastValueFrom(this.http.delete<void>(url));
  }
}
