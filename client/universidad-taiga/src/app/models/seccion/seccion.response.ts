/**
 * Respuesta de una sección desde el backend.
 * Incluye los objetos curso y cicloAcademico expandidos.
 */
export interface SeccionResponse {
  /** ID único de la sección */
  idSeccion: number;
  /** Código identificador de la sección */
  codigoSeccion: string;
  /** Nombre del ciclo académico (campo directo) */
  cicloAcademicoNombre: string;
  /** Cantidad de vacantes disponibles */
  vacantes: number;
  /** Color visual opcional para identificar la sección en horarios */
  color?: string | null;
  /** Curso asociado a la sección */
  curso: {
    idCurso: number;
    nombre: string;
    creditos: number;
    descripcion: string | null;
  };
  /** Ciclo académico asociado a la sección */
  cicloAcademico: {
    idCiclo: number;
    nombre: string;
    fechaInicio: string;
    fechaFin: string;
  };
}
