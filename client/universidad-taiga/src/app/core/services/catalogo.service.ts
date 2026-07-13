import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { APP_API_URL } from '../tokens/api.tokens';
import type {
  Carrera,
  CatalogKind,
  CatalogNameItem,
  CatalogNameRequest,
  CicloAcademico,
  Especializacion,
  GenerarCiclosAnioRequest,
  GradoAcademico,
  TipoDocumento,
} from '../../models/catalogos/catalogo.response';
import { lastValueFrom } from 'rxjs';

/**
 * Servicio de catálogos para obtener datos de referencia del backend.
 * Proporciona métodos para cargar tipos de documento y administrar catálogos simples.
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

  getCarrera(id: number): Promise<Carrera> {
    return lastValueFrom(this.http.get<Carrera>(`${this.apiBaseUrl}/carreras/${id}`));
  }

  createCarrera(request: CatalogNameRequest): Promise<Carrera> {
    return lastValueFrom(this.http.post<Carrera>(`${this.apiBaseUrl}/carreras`, request));
  }

  updateCarrera(id: number, request: CatalogNameRequest): Promise<Carrera> {
    return lastValueFrom(this.http.put<Carrera>(`${this.apiBaseUrl}/carreras/${id}`, request));
  }

  deleteCarrera(id: number): Promise<void> {
    return lastValueFrom(this.http.delete<void>(`${this.apiBaseUrl}/carreras/${id}`));
  }

  /**
   * Obtiene la lista de grados académicos disponibles.
   *
   * @returns Promesa con el listado de grados académicos.
   */
  getGradosAcademicos(): Promise<GradoAcademico[]> {
    return lastValueFrom(
      this.http.get<GradoAcademico[]>(`${this.apiBaseUrl}/grados-academicos`),
    );
  }

  createGradoAcademico(request: CatalogNameRequest): Promise<GradoAcademico> {
    return lastValueFrom(
      this.http.post<GradoAcademico>(`${this.apiBaseUrl}/grados-academicos`, request),
    );
  }

  updateGradoAcademico(id: number, request: CatalogNameRequest): Promise<GradoAcademico> {
    return lastValueFrom(
      this.http.put<GradoAcademico>(`${this.apiBaseUrl}/grados-academicos/${id}`, request),
    );
  }

  deleteGradoAcademico(id: number): Promise<void> {
    return lastValueFrom(this.http.delete<void>(`${this.apiBaseUrl}/grados-academicos/${id}`));
  }

  getEspecializaciones(): Promise<Especializacion[]> {
    return lastValueFrom(this.http.get<Especializacion[]>(`${this.apiBaseUrl}/especializaciones`));
  }

  createEspecializacion(request: CatalogNameRequest): Promise<Especializacion> {
    return lastValueFrom(
      this.http.post<Especializacion>(`${this.apiBaseUrl}/especializaciones`, request),
    );
  }

  updateEspecializacion(id: number, request: CatalogNameRequest): Promise<Especializacion> {
    return lastValueFrom(
      this.http.put<Especializacion>(`${this.apiBaseUrl}/especializaciones/${id}`, request),
    );
  }

  deleteEspecializacion(id: number): Promise<void> {
    return lastValueFrom(this.http.delete<void>(`${this.apiBaseUrl}/especializaciones/${id}`));
  }

  getCiclosAcademicos(): Promise<CicloAcademico[]> {
    return lastValueFrom(this.http.get<CicloAcademico[]>(`${this.apiBaseUrl}/ciclos-academicos`));
  }

  generarCiclosAcademicos(request: GenerarCiclosAnioRequest): Promise<CicloAcademico[]> {
    return lastValueFrom(
      this.http.post<CicloAcademico[]>(`${this.apiBaseUrl}/ciclos-academicos/generar-anio`, request),
    );
  }

  getCatalogItems(kind: CatalogKind): Promise<CatalogNameItem[] | CicloAcademico[]> {
    switch (kind) {
      case 'carreras':
        return this.getCarreras();
      case 'grados-academicos':
        return this.getGradosAcademicos();
      case 'especializaciones':
        return this.getEspecializaciones();
      case 'ciclos':
        return this.getCiclosAcademicos();
    }
  }

  createCatalogItem(kind: Exclude<CatalogKind, 'ciclos'>, request: CatalogNameRequest): Promise<CatalogNameItem> {
    switch (kind) {
      case 'carreras':
        return this.createCarrera(request);
      case 'grados-academicos':
        return this.createGradoAcademico(request);
      case 'especializaciones':
        return this.createEspecializacion(request);
    }
  }

  updateCatalogItem(
    kind: Exclude<CatalogKind, 'ciclos'>,
    id: number,
    request: CatalogNameRequest,
  ): Promise<CatalogNameItem> {
    switch (kind) {
      case 'carreras':
        return this.updateCarrera(id, request);
      case 'grados-academicos':
        return this.updateGradoAcademico(id, request);
      case 'especializaciones':
        return this.updateEspecializacion(id, request);
    }
  }

  deleteCatalogItem(kind: Exclude<CatalogKind, 'ciclos'>, id: number): Promise<void> {
    switch (kind) {
      case 'carreras':
        return this.deleteCarrera(id);
      case 'grados-academicos':
        return this.deleteGradoAcademico(id);
      case 'especializaciones':
        return this.deleteEspecializacion(id);
    }
  }
}
