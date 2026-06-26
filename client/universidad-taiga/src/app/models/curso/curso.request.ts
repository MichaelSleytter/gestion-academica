/**
 * Datos necesarios para crear un curso.
 */
export interface CursoCreateRequest {
  /** Nombre del curso */
  nombre: string;
  /** Cantidad de créditos del curso */
  creditos: number;
  /** Descripción opcional del curso */
  descripcion: string | null;
}

/**
 * Datos necesarios para actualizar un curso (misma estructura que creación).
 */
export interface CursoUpdateRequest extends CursoCreateRequest {}
