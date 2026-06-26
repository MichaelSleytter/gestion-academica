/**
 * Datos necesarios para crear un horario.
 */
export interface HorarioCreateRequest {
  /** Día de la semana */
  diaSemana: string;
  /** Hora de inicio (HH:mm) */
  horaInicio: string;
  /** Hora de fin (HH:mm) */
  horaFin: string;
  /** Aula o salón (opcional) */
  aula: string | null;
}

/**
 * Datos necesarios para actualizar un horario (misma estructura que creación).
 */
export interface HorarioUpdateRequest extends HorarioCreateRequest {}
