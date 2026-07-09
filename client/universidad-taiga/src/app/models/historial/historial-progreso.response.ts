/**
 * Academic progress response returned by the backend.
 */
export interface HistorialProgresoResponse {
  estudiante: EstudianteResumen;
  carrera: CarreraResumen;
  resumen: ProgresoResumen;
  cursos: CursoProgreso[];
}

/** Student metadata included in the academic progress view. */
export interface EstudianteResumen {
  id: number;
  codigo: string;
  nombres: string;
  apellidos: string;
}

/** Career metadata included in the academic progress view. */
export interface CarreraResumen {
  id: number;
  nombre: string;
  creditosTotales: number;
}

/** Aggregated progress metrics for a student. */
export interface ProgresoResumen {
  totalCursos: number;
  cursosAprobados: number;
  cursosEnProgreso: number;
  cursosPendientes: number;
  creditosAprobados: number;
  creditosRestantes: number;
  promedioPonderado: number | null;
  porcentajeAvance: number;
}

/** Course status values returned by the academic progress API. */
export type EstadoCursoProgreso =
  | 'PASSED'
  | 'IN_PROGRESS'
  | 'PENDING_AVAILABLE'
  | 'PENDING_BLOCKED'
  | 'FAILED';

/** Prerequisite rule types returned by the academic progress API. */
export type TipoReglaPrerrequisito = 'HARD';

/** Course-level progress entry. */
export interface CursoProgreso {
  cursoId: number;
  codigo: string | null;
  nombre: string;
  cicloRecomendado: number;
  obligatorio: boolean;
  creditos: number;
  estado: EstadoCursoProgreso;
  notaFinal: number | null;
  prerrequisitos: PrerrequisitoProgreso[];
}

/** Prerequisite state shown in blocked course details. */
export interface PrerrequisitoProgreso {
  cursoId: number;
  codigo: string | null;
  nombre: string;
  tipoRegla: TipoReglaPrerrequisito;
  cumplido: boolean;
}
