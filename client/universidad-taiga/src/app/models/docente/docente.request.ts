/**
 * DTO para crear o actualizar un docente.
 * Corresponde a DocenteRequestDTO del backend.
 */
export interface DocenteCreateRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  numeroDocumento: string;
  idTipoDocumento: number;
  /** Legacy free-text specialization kept during backend transition. */
  especialidad?: string | null;
  /** Preferred specialization catalog ID. */
  idEspecializacion: number;
  idGrado: number;
}

export interface DocenteUpdateRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  numeroDocumento: string;
  idTipoDocumento: number;
  /** Legacy free-text specialization kept during backend transition. */
  especialidad?: string | null;
  /** Preferred specialization catalog ID. */
  idEspecializacion: number;
  idGrado: number;
}
