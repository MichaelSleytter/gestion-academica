/**
 * Respuesta de un curso desde el backend.
 */
export interface CursoResponse {
  /** ID único del curso */
  idCurso: number;
  /** Nombre del curso */
  nombre: string;
  /** Cantidad de créditos del curso */
  creditos: number;
  /** Descripción opcional del curso */
  descripcion: string | null;
}
