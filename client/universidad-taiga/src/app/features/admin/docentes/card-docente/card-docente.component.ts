import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import type { DocenteResponse } from '../../../../models/docente/docente.response';
import { getIniciales } from '../../../../shared/utils/estudiante.util';

@Component({
  selector: 'app-card-docente',
  imports: [TuiButton, TuiIcon],
  templateUrl: './card-docente.html',
  styleUrl: './card-docente.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Tarjeta resumen de un docente.
 * Muestra datos básicos y provee acciones de editar y eliminar.
 */
export class CardDocente {
  /** Docente a mostrar en la tarjeta */
  readonly docente = input.required<DocenteResponse>();

  /** Emite el docente al solicitar edición */
  readonly editar = output<DocenteResponse>();
  /** Emite el docente al solicitar eliminación */
  readonly eliminar = output<DocenteResponse>();

  /** Iniciales del docente */
  readonly iniciales = computed(() =>
    getIniciales(`${this.docente().nombre} ${this.docente().apellido}`),
  );

  /** Especialización formateada, con fallback legacy. */
  readonly especialidad = computed(() => {
    const d = this.docente();
    return d.nombreEspecializacion ?? d.especialidad ?? 'Sin especialización';
  });

  /** Estado formateado. */
  readonly estado = computed(() => {
    const d = this.docente();
    return d.estado
      ? { etiqueta: 'ACTIVO', clases: 'bg-success-bg text-success' }
      : { etiqueta: 'INACTIVO', clases: 'bg-danger-bg text-danger' };
  });

  onEditar(): void {
    this.editar.emit(this.docente());
  }

  onEliminar(): void {
    this.eliminar.emit(this.docente());
  }
}
