/**
 * Tipo de documento de identidad (ej: DNI, Pasaporte, CE).
 */
export interface TipoDocumento {
  /** ID único del tipo de documento */
  idTipoDocumento: number;
  /** Nombre del tipo de documento */
  nombre: string;
}

/**
 * Carrera universitaria ofrecida por la institución.
 */
export interface Carrera {
  /** ID único de la carrera */
  idCarrera: number;
  /** Nombre de la carrera */
  nombre: string;
}

/**
 * Grado académico (ej: Licenciatura, Maestría, Doctorado).
 */
export interface GradoAcademico {
  /** ID único del grado académico */
  idGrado: number;
  /** Nombre del grado académico */
  nombre: string;
}
