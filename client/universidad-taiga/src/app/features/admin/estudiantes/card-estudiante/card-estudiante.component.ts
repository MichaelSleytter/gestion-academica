import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { EstudianteResponse } from '../../../../models/estudiante/estudiante.response';
import { getIniciales, getEstadoEstudiante } from '../../../../shared/utils/estudiante.util';

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
  readonly iniciales = computed(() => getIniciales(this.estudiante().nombre));

  /** Estado formateado del estudiante con clases CSS. */
  readonly estado = computed(() => {
    const result = getEstadoEstudiante(this.estudiante().estadoAcademico);
    return {
      etiqueta: result.label,
      clases: result.classes,
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
