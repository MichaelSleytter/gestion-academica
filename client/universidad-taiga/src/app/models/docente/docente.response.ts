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
  /** Especialidad del docente */
  especialidad: string;
  /** ID del grado académico */
  idGrado: number;
  /** Nombre del grado académico */
  nombreGrado: string;
}
