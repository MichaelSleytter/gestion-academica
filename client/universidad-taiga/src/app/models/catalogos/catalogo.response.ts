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

/**
 * Especialización docente administrada desde catálogos.
 */
export interface Especializacion {
  /** ID único de la especialización */
  idEspecializacion: number;
  /** Nombre de la especialización */
  nombre: string;
}

/**
 * Ciclo académico usado por secciones y horarios.
 */
export interface CicloAcademico {
  idCiclo: number;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
}

export interface CatalogNameRequest {
  nombre: string;
}

export interface GenerarCiclosAnioRequest {
  anio: number;
}

export type CatalogKind = 'carreras' | 'grados-academicos' | 'especializaciones' | 'ciclos';

export type CatalogNameItem = Carrera | GradoAcademico | Especializacion;
