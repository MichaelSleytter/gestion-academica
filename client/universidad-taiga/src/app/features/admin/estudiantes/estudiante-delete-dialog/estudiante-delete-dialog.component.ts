import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TuiButton } from '@taiga-ui/core';

/**
 * Diálogo de confirmación para eliminar un estudiante.
 *
 * Muestra el nombre del estudiante y solicita confirmación
 * antes de proceder con la eliminación. Emite eventos
 * al padre para ejecutar la acción.
 *
 * @example
 * <app-estudiante-delete-dialog
 *   [nombre]="'Juan Pérez'"
 *   [isEliminando]="false"
 *   (confirmar)="onConfirmarEliminar()"
 *   (cancelar)="onCancelarEliminar()"
 * />
 */
@Component({
  selector: 'app-estudiante-delete-dialog',
  standalone: true,
  imports: [TuiButton],
  template: `
    <div class="flex flex-col gap-4">
      <p class="text-sm text-text-secondary">
        Se eliminará estudiante
        <strong class="text-text-primary">{{ nombre() }}</strong>. Esta acción
        no se puede deshacer.
      </p>

      <footer class="flex items-center justify-end gap-3">
        <button
          appearance="secondary"
          tuiButton
          type="button"
          (click)="cancelar.emit()"
        >
          Cancelar
        </button>
        <button
          appearance="primary-destructive"
          tuiButton
          type="button"
          [disabled]="isEliminando()"
          (click)="confirmar.emit()"
        >
          Eliminar
        </button>
      </footer>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstudianteDeleteDialog {
  /** Nombre del estudiante que se va a eliminar. */
  readonly nombre = input.required<string>();

  /** Indica si hay una operación de eliminación en curso. */
  readonly isEliminando = input(false);

  /** Emite cuando el usuario confirma la eliminación. */
  readonly confirmar = output();

  /** Emite cuando el usuario cancela la eliminación. */
  readonly cancelar = output();
}
