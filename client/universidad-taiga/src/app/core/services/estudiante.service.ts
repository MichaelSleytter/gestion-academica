import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { EstudianteResponse } from '../../models/estudiante/estudiante.response';
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
