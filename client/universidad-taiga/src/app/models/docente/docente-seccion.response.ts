import { SeccionResponse } from '../seccion/seccion.response';

/**
 * Respuesta de una asignación docente-sección desde el backend.
 */
export interface DocenteSeccionResponse {
  /** Llave compuesta de la asignación. */
  id: {
    idDocente: number;
    idSeccion: number;
  };
  /** Sección expandida cuando el backend la serializa. */
  seccion?: SeccionResponse;
}
