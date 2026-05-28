/**
 * Datos base de un estudiante, comunes a creación y respuesta.
 * No incluye datos sensibles ni relacionales.
 */
export interface EstudianteBase {
  /** Nombre del estudiante */
  nombre: string;
  /** Apellido del estudiante */
  apellido: string;
  /** Email institucional */
  email: string;
  /** Código único de estudiante */
  codigoEstudiante: string;
  /** Ciclo académico actual */
  ciclo: number;
  /** Estado académico (ej: "Activo", "Suspendido") */
  estadoAcademico: string;
  /** ID de la carrera a la que pertenece */
  idCarrera: number;
}
