/**
 * Respuesta de un horario desde el backend.
 * Incluye el objeto seccion expandido con curso y cicloAcademico.
 */
export interface HorarioResponse {
  /** ID único del horario */
  idHorario: number;
  /** Día de la semana (Lunes, Martes, etc.) */
  diaSemana: string;
  /** Hora de inicio en formato HH:mm:ss */
  horaInicio: string;
  /** Hora de fin en formato HH:mm:ss */
  horaFin: string;
  /** Aula o salón (opcional) */
  aula: string | null;
  /** Sección asociada al horario */
  seccion: {
    idSeccion: number;
    codigoSeccion: string;
    cicloAcademicoNombre: string;
    vacantes: number;
    curso: {
      idCurso: number;
      nombre: string;
      creditos: number;
      descripcion: string | null;
    };
    cicloAcademico: {
      idCiclo: number;
      nombre: string;
      fechaInicio: string;
      fechaFin: string;
    };
  };
}
