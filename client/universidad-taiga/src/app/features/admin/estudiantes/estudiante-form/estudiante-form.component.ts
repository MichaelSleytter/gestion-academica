import { ChangeDetectionStrategy, Component, ElementRef, inject, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import type { FormGroup } from '@angular/forms';
import {
  TuiButton,
  TuiInput,
  TuiTextfield,
} from '@taiga-ui/core';
import { TuiChevron, TuiDataListWrapper, TuiSelect } from '@taiga-ui/kit';
import { TuiForm } from '@taiga-ui/layout';
import type { Carrera, TipoDocumento } from '../../../../models/catalogos/catalogo.response';

/** Modo de operación del formulario de estudiante. */
type ModoFormulario = 'crear' | 'editar';

/**
 * Formulario reutilizable para crear o editar estudiantes.
 *
 * Recibe el FormGroup desde el componente padre y emite eventos
 * para guardar o cancelar. No maneja queries ni mutaciones —
 * esa responsabilidad queda en el padre.
 *
 * @example
 * <app-estudiante-form
 *   [form]="estudianteForm"
 *   [modo]="'crear'"
 *   [tiposDocumento]="tiposDocumentoQuery.data()"
 *   [carreras]="carrerasQuery.data()"
 *   (guardar)="onGuardarEstudiante()"
 *   (cancelar)="onCancelar()"
 * />
 */
@Component({
  selector: 'app-estudiante-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiChevron,
    TuiDataListWrapper,
    TuiForm,
    TuiInput,
    TuiSelect,
    TuiTextfield,
  ],
  templateUrl: './estudiante-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstudianteForm {
  private readonly elementRef = inject(ElementRef<HTMLElement>);

  /** FormGroup con los controles del formulario de estudiante. */
  readonly form = input.required<FormGroup>();

  /** Define si el formulario opera en modo creación o edición. */
  readonly modo = input.required<ModoFormulario>();

  /** Catálogo de tipos de documento disponibles. */
  readonly tiposDocumento = input<TipoDocumento[]>([]);

  /** Catálogo de carreras disponibles. */
  readonly carreras = input<Carrera[]>([]);

  /** Indica si hay una operación de guardado en curso. */
  readonly isGuardando = input(false);

  /** Emite cuando el formulario se envía correctamente. */
  readonly guardar = output();

  /** Emite cuando se cancela la operación. */
  readonly cancelar = output();

  submit(): void {
    if (this.form().invalid) {
      this.form().markAllAsTouched();
      setTimeout(() => this.focusFirstInvalidControl());
      return;
    }

    this.guardar.emit();
  }

  /**
   * Retorna el título del diálogo según el modo actual.
   *
   * @returns "Nuevo estudiante" si es creación, "Editar estudiante" si es edición.
   */
  getTituloModal(): string {
    return this.modo() === 'crear' ? 'Nuevo estudiante' : 'Editar estudiante';
  }

  /**
   * Retorna el texto del botón de guardado según el modo actual.
   *
   * @returns "Crear estudiante" o "Guardar cambios".
   */
  getTextoBotonGuardar(): string {
    return this.modo() === 'crear' ? 'Crear estudiante' : 'Guardar cambios';
  }

  /**
   * Obtiene el mensaje de error de un control del formulario.
   * Solo muestra errores si el control fue tocado (touched).
   *
   * @param controlName - Nombre del control dentro del FormGroup.
   * @returns Mensaje de error traducido, o null si no hay error.
   */
  getFieldError(controlName: string): string | null {
    const control = this.form().get(controlName);

    if (!control || !control.touched || !control.errors) {
      return null;
    }

    if (control.errors['required']) {
      return 'Este campo es obligatorio';
    }

    if (control.errors['email']) {
      return 'Ingresa un email válido';
    }

    if (control.errors['minlength']) {
      return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    }

    if (control.errors['min']) {
      return `El valor mínimo es ${control.errors['min'].min}`;
    }

    return 'Valor inválido';
  }

  /**
   * Convierte un TipoDocumento a string para el selector.
   *
   * @param item - Tipo de documento o null.
   * @returns Nombre del tipo de documento, o cadena vacía.
   */
  stringifyTipoDocumento(item: TipoDocumento | null): string {
    return item?.nombre ?? '';
  }

  /**
   * Convierte una Carrera a string para el selector.
   *
   * @param item - Carrera o null.
   * @returns Nombre de la carrera, o cadena vacía.
   */
  stringifyCarrera(item: Carrera | null): string {
    return item?.nombre ?? '';
  }

  private focusFirstInvalidControl(): void {
    const control = this.elementRef.nativeElement.querySelector('[aria-invalid="true"]') as HTMLElement | null;
    control?.focus();
  }
}
