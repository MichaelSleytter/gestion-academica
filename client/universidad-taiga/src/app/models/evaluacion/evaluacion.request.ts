/**
 * Datos necesarios para crear una evaluación.
 */
export interface EvaluacionCreateRequest {
  /** Nombre de la evaluación */
  nombre: string;
  /** Porcentaje sobre la nota final (0.1 - 100) */
  porcentaje: number;
}

/**
 * Datos necesarios para actualizar una evaluación (misma estructura que creación).
 */
export interface EvaluacionUpdateRequest extends EvaluacionCreateRequest {}
