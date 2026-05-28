import { EstudianteBase } from './estudiante.base';

/**
 * Respuesta completa de un estudiante desde el backend.
 * Extiende EstudianteBase con datos de identificación y estado.
 */
export interface EstudianteResponse extends EstudianteBase {
  /** ID único del usuario en el sistema */
  idUsuario: number;
  /** Número de documento de identidad */
  numeroDocumento: string;
  /** ID del tipo de documento (opcional) */
  idTipoDocumento?: number;
  /** Nombre del tipo de documento (ej: "DNI", "Pasaporte") */
  tipoDocumento: string;
  /** Indica si el estudiante está activo */
  estado: boolean;
  /** Nombre de la carrera asociada */
  nombreCarrera: string;
  /** Email personal del estudiante */
  emailPersonal: string;
}
