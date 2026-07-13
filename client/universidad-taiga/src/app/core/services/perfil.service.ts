import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import type {
  PerfilResponse,
  ActualizarPerfilRequest,
  CambiarPasswordRequest,
  CambiarPasswordResponse,
} from '../../models/auth.model';

/**
 * Servicio para operaciones del perfil del usuario autenticado.
 */
@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${inject(APP_API_URL)}/auth`;

  /** Obtiene el perfil del usuario autenticado. */
  getPerfil(): Promise<PerfilResponse> {
    return lastValueFrom(this.http.get<PerfilResponse>(`${this.apiUrl}/me`));
  }

  /** Actualiza datos editables del perfil. */
  actualizarPerfil(data: ActualizarPerfilRequest): Promise<PerfilResponse> {
    return lastValueFrom(this.http.put<PerfilResponse>(`${this.apiUrl}/me`, data));
  }

  /** Cambia la contraseña del usuario autenticado. */
  cambiarPassword(data: CambiarPasswordRequest): Promise<CambiarPasswordResponse> {
    return lastValueFrom(
      this.http.put<CambiarPasswordResponse>(`${this.apiUrl}/me/password`, data),
    );
  }
}
