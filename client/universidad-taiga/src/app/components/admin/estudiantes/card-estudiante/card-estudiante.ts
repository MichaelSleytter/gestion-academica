import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { EstudianteResponse } from '../../../../models/estudiante/estudiante.response';

@Component({
  selector: 'app-card-estudiante',
  imports: [TuiButton, TuiIcon],
  templateUrl: './card-estudiante.html',
  styleUrl: './card-estudiante.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Tarjeta resumen de un estudiante.
 * Muestra datos básicos y provee acciones de editar, eliminar y ver historial.
 */
export class CardEstudiante {
  /** Estudiante a mostrar en la tarjeta */
  readonly estudiante = input.required<EstudianteResponse>();

  /** Emite el estudiante al solicitar edición */
  readonly editar = output<EstudianteResponse>();

  /** Emite el estudiante al solicitar eliminación */
  readonly eliminar = output<EstudianteResponse>();

  /** Emite el estudiante al solicitar ver historial */
  readonly historial = output<EstudianteResponse>();

  /** Iniciales del estudiante (máx 2 caracteres) */
  readonly iniciales = computed(() => {
    const nombre = this.estudiante().nombre.trim();
    const partes = nombre.split(/\s+/).filter(Boolean);

    if (partes.length === 0) {
      return 'NA';
    }

    if (partes.length === 1) {
      return partes[0].slice(0, 2).toUpperCase();
    }

    return `${partes[0][0]}${partes[1][0]}`.toUpperCase();
  });



readonly estado = computed(() => {
    const valor = (this.estudiante().estadoAcademico ?? 'INACTIVO').toUpperCase();

    if (valor === 'ACTIVO') {
      return {
        etiqueta: 'REGULAR',
        clases: 'bg-success-bg text-success',
      };
    }

    if (valor === 'SUSPENDIDO') {
      return {
        etiqueta: 'SUSPENDIDO',
        clases: 'bg-warning-bg text-warning',
      };
    }

    return {
      etiqueta: 'INACTIVO',
      clases: 'bg-danger-bg text-danger',
    };
  });

  /** Emite el evento de edición con el estudiante actual. */
  onEditar(): void {
    this.editar.emit(this.estudiante());
  }

  /** Emite el evento de eliminación con el estudiante actual. */
  onEliminar(): void {
    this.eliminar.emit(this.estudiante());
  }

  /** Emite el evento de ver historial con el estudiante actual. */
  onVerHistorial(): void {
    this.historial.emit(this.estudiante());
  }
}
