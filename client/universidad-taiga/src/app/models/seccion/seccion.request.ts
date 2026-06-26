/**
 * Datos necesarios para crear una sección.
 */
export interface SeccionCreateRequest {
  /** Código identificador de la sección */
  codigoSeccion: string;
  /** Nombre del ciclo académico */
  cicloAcademicoNombre: string;
  /** Cantidad de vacantes */
  vacantes: number;
}

/**
 * Datos necesarios para actualizar una sección (misma estructura que creación).
 */
export interface SeccionUpdateRequest extends SeccionCreateRequest {}
