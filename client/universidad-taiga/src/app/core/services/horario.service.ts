import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import { lastValueFrom } from 'rxjs';
import { HorarioResponse } from '../../models/horario/horario.response';
import { HorarioCreateRequest } from '../../models/horario/horario.request';
import { PageResponse } from '../../models/shared/page.response';

/**
 * Servicio para interactuar con el API de horarios.
 * La URL base se inyecta mediante APP_API_URL.
 */
@Injectable({
  providedIn: 'root',
})
export class HorarioService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(APP_API_URL);

  /**
   * Obtiene horarios con paginación y búsqueda opcional.
   *
   * @param pagina   número de página (0-based)
   * @param tamaño   elementos por página
   * @param busqueda texto de búsqueda (opcional)
   * @returns página de horarios
   */
  getHorariosPaginado(
    pagina: number,
    tamaño: number,
    busqueda?: string,
  ): Promise<PageResponse<HorarioResponse>> {
    let params = new HttpParams()
      .set('pagina', pagina.toString())
      .set('tamaño', tamaño.toString());

    if (busqueda?.trim()) {
      params = params.set('busqueda', busqueda.trim());
    }

    return lastValueFrom(
      this.http.get<PageResponse<HorarioResponse>>(`${this.apiBaseUrl}/horarios`, { params }),
    );
  }

  /**
   * Crea un nuevo horario.
   *
   * @param horario   datos del horario
   * @param idSeccion ID de la sección asociada
   * @returns horario creado
   */
  crearHorario(horario: HorarioCreateRequest, idSeccion: number): Promise<HorarioResponse> {
    const url = `${this.apiBaseUrl}/horarios?idSeccion=${idSeccion}`;
    return lastValueFrom(this.http.post<HorarioResponse>(url, horario));
  }

  /**
   * Actualiza un horario existente.
   *
   * @param id        ID del horario
   * @param horario   datos actualizados
   * @param idSeccion ID de la sección asociada
   * @returns horario actualizado
   */
  actualizarHorario(id: number, horario: HorarioCreateRequest, idSeccion: number): Promise<HorarioResponse> {
    const url = `${this.apiBaseUrl}/horarios/${id}?idSeccion=${idSeccion}`;
    return lastValueFrom(this.http.put<HorarioResponse>(url, horario));
  }

  /**
   * Elimina un horario por ID.
   *
   * @param id ID del horario a eliminar
   */
  eliminarHorario(id: number): Promise<void> {
    const url = `${this.apiBaseUrl}/horarios/${id}`;
    return lastValueFrom(this.http.delete<void>(url));
  }

  /**
   * Obtiene la lista de secciones para el dropdown del formulario.
   * Usa el endpoint paginado de secciones con un tamaño grande.
   *
   * @returns página de secciones
   */
  getSeccionesList(): Promise<PageResponse<HorarioResponse['seccion']>> {
    const url = `${this.apiBaseUrl}/secciones`;
    const params = new HttpParams()
      .set('pagina', '0')
      .set('tamaño', '100');
    return lastValueFrom(this.http.get<PageResponse<HorarioResponse['seccion']>>(url, { params }));
  }
}
