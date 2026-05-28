import { EstudianteBase } from './estudiante.base';

export interface EstudianteCreateRequest {
  /** Datos del usuario */
  nombre: string;
  apellido: string;
  numeroDocumento: string;
  idTipoDocumento: number;
  emailPersonal: string;
  /** Datos académicos */
  ciclo: number;
  idCarrera: number;
}

export interface EstudianteUpdateRequest extends EstudianteCreateRequest {}

export type EstudiantePatchRequest = Partial<EstudianteUpdateRequest>;
