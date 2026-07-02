/**
 * Respuesta de una matrícula desde el backend.
 */
export interface MatriculaResponse {
  /** ID único de la matrícula. */
  idMatricula: number;
  /** Fecha en que se registró la matrícula. */
  fechaMatricula: string | null;
  /** Estado académico de la matrícula. */
  estado: string;
  /** ID del estudiante matriculado cuando se usa el DTO por sección. */
  idEstudiante?: number;
  /** Código académico del estudiante cuando se usa el DTO por sección. */
  codigoEstudiante?: string;
  /** Nombre del estudiante cuando se usa el DTO por sección. */
  nombre?: string;
  /** Apellido del estudiante cuando se usa el DTO por sección. */
  apellido?: string;
  /** Email del estudiante cuando se usa el DTO por sección. */
  email?: string;
  /** Estudiante expandido cuando el backend lo serializa. */
  estudiante?: {
    idUsuario: number;
    codigoEstudiante?: string;
    usuario?: {
      nombre?: string;
      apellido?: string;
      email?: string;
    };
  };
}
