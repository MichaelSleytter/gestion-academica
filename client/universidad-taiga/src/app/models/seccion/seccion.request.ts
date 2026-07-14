/**
 * Datos necesarios para crear una sección.
 */
export interface SeccionCreateRequest {
  /** Código identificador de la sección */
  codigoSeccion: string;
  /** Nombre del ciclo académico */
  cicloAcademicoNombre: string;
  /** Capacidad total de estudiantes para la sección */
  vacantes: number;
  /** Color visual opcional para identificar la sección en horarios */
  color?: string | null;
}

/**
 * Datos necesarios para actualizar una sección (misma estructura que creación).
 */
export interface SeccionUpdateRequest extends SeccionCreateRequest {}
