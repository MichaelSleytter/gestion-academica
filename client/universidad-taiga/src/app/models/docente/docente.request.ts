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
  especialidad: string;
  idGrado: number;
}

export interface DocenteUpdateRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  numeroDocumento: string;
  idTipoDocumento: number;
  especialidad: string;
  idGrado: number;
}
