/**
 * Utilidades compartidas para el módulo de estudiantes.
 * Centraliza lógica repetida entre Estudiantes, CardEstudiante, etc.
 */

/**
 * Obtiene las iniciales a partir de un nombre completo.
 *
 * @param nombre Nombre completo del estudiante.
 * @returns Máximo 2 caracteres en mayúscula.
 */
export function getIniciales(nombre: string): string {
  const partes = (nombre ?? '').trim().split(/\s+/).filter(Boolean);

  if (partes.length === 0) {
    return 'NA';
  }

  if (partes.length === 1) {
    return partes[0].slice(0, 2).toUpperCase();
  }

  return `${partes[0][0]}${partes[1][0]}`.toUpperCase();
}

/**
 * Representación del estado de un estudiante con clases CSS.
 */
export interface EstadoEstudiante {
  label: string;
  classes: string;
  dotClasses: string;
}

/**
 * Obtiene el estado formateado del estudiante con clases CSS asociadas.
 *
 * @param estadoAcademico Valor del campo estadoAcademico del estudiante.
 * @returns Objeto con label, classes y dotClasses.
 */
export function getEstadoEstudiante(estadoAcademico?: string): EstadoEstudiante {
  const estado = (estadoAcademico ?? 'INACTIVO').toUpperCase();

  if (estado === 'ACTIVO') {
    return {
      label: 'REGULAR',
      classes: 'bg-success-bg text-success',
      dotClasses: 'bg-success',
    };
  }

  if (estado === 'SUSPENDIDO') {
    return {
      label: 'SUSPENDIDO',
      classes: 'bg-warning-bg text-warning',
      dotClasses: 'bg-warning',
    };
  }

  return {
    label: 'INACTIVO',
    classes: 'bg-danger-bg text-danger',
    dotClasses: 'bg-danger',
  };
}
