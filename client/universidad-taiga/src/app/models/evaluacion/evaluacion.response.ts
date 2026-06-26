/**
 * Respuesta de una evaluación desde el backend.
 * Incluye el objeto seccion expandido con curso y cicloAcademico.
 */
export interface EvaluacionResponse {
  /** ID único de la evaluación */
  idEvaluacion: number;
  /** Nombre de la evaluación */
  nombre: string;
  /** Porcentaje de la evaluación sobre la nota final */
  porcentaje: number;
  /** Sección asociada a la evaluación */
  seccion: {
    idSeccion: number;
    codigoSeccion: string;
    cicloAcademicoNombre: string;
    vacantes: number;
    curso: {
      idCurso: number;
      nombre: string;
      creditos: number;
      descripcion: string | null;
    };
    cicloAcademico: {
      idCiclo: number;
      nombre: string;
      fechaInicio: string;
      fechaFin: string;
    };
  };
}
