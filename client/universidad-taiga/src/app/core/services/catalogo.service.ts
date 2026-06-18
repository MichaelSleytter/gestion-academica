import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { Carrera, TipoDocumento } from '../../models/catalogos/catalogo.response';
import { lastValueFrom } from 'rxjs';

/**
 * Servicio de catálogos para obtener datos de referencia del backend.
 * Proporciona métodos para cargar tipos de documento y carreras.
 */
@Injectable({
  providedIn: 'root',
})
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene la lista de tipos de documento disponibles.
   *
   * @returns Promesa con el listado de tipos de documento.
   */
  getTipoDocumento(): Promise<TipoDocumento[]> {
    return lastValueFrom(this.http.get<TipoDocumento[]>(`${this.apiBaseUrl}/tipos-documento`));
  }

  /**
   * Obtiene la lista de carreras disponibles.
   *
   * @returns Promesa con el listado de carreras.
   */
  getCarreras(): Promise<Carrera[]> {
    return lastValueFrom(this.http.get<Carrera[]>(`${this.apiBaseUrl}/carreras`));
  }
}
