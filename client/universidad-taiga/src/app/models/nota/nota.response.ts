/**
 * Respuesta de una nota desde el backend.
 */
export interface NotaResponse {
  /** ID único de la nota. */
  idNota: number;
  /** Valor numérico de la nota. */
  nota: number;
  /** ID del estudiante evaluado cuando se usa el DTO por evaluación. */
  idEstudiante?: number;
  /** Estudiante expandido cuando el backend lo serializa. */
  estudiante?: {
    idUsuario: number;
  };
}
