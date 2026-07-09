import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import type { HistorialProgresoResponse } from '../../models/historial';
import { APP_API_URL } from '../tokens/api.tokens';

/**
 * Service for academic progress API calls.
 */
@Injectable({ providedIn: 'root' })
export class HistorialProgresoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Gets academic progress for the authenticated student.
   */
  getMiProgreso(): Promise<HistorialProgresoResponse> {
    const url = `${this.apiBaseUrl}/historial-academico/progreso/me`;
    return lastValueFrom(this.http.get<HistorialProgresoResponse>(url));
  }

  /**
   * Gets academic progress for an authorized student lookup.
   * Reserved for future admin/docente screens.
   */
  getProgresoEstudiante(estudianteId: number): Promise<HistorialProgresoResponse> {
    const url = `${this.apiBaseUrl}/historial-academico/progreso/estudiante/${estudianteId}`;
    return lastValueFrom(this.http.get<HistorialProgresoResponse>(url));
  }
}
