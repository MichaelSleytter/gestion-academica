/**
 * Respuesta de un docente desde el backend.
 * Corresponde a DocenteResponseDTO del backend.
 */
export interface DocenteResponse {
  /** ID único del usuario en el sistema */
  idUsuario: number;
  /** Nombre del docente */
  nombre: string;
  /** Apellido del docente */
  apellido: string;
  /** Correo institucional */
  email: string;
  /** Número de documento de identidad */
  numeroDocumento: string;
  /** Nombre del tipo de documento (ej: "DNI", "Pasaporte") */
  tipoDocumento: string;
  /** Indica si el docente está activo */
  estado: boolean;
  /** Especialidad legacy del docente */
  especialidad: string | null;
  /** ID de la especialización de catálogo */
  idEspecializacion: number | null;
  /** Nombre de la especialización de catálogo */
  nombreEspecializacion: string | null;
  /** ID del grado académico */
  idGrado: number;
  /** Nombre del grado académico */
  nombreGrado: string;
}
