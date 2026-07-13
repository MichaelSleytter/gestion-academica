import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import type { FormGroup } from '@angular/forms';
import { TuiButton, TuiInput, TuiTextfield } from '@taiga-ui/core';
import { TuiChevron, TuiDataListWrapper, TuiSelect } from '@taiga-ui/kit';
import { TuiForm } from '@taiga-ui/layout';
import type { Especializacion, GradoAcademico, TipoDocumento } from '../../../../models/catalogos/catalogo.response';

type ModoFormulario = 'crear' | 'editar';

@Component({
  selector: 'app-docente-form',
  imports: [ReactiveFormsModule, TuiButton, TuiChevron, TuiDataListWrapper, TuiForm, TuiInput, TuiSelect, TuiTextfield],
  templateUrl: './docente-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Formulario reutilizable para crear o editar docentes.
 *
 * Recibe el FormGroup desde el componente padre y emite eventos
 * para guardar o cancelar.
 */
export class DocenteForm {
  readonly form = input.required<FormGroup>();
  readonly modo = input.required<ModoFormulario>();
  readonly tiposDocumento = input<TipoDocumento[]>([]);
  readonly grados = input<GradoAcademico[]>([]);
  readonly especializaciones = input<Especializacion[]>([]);
  readonly isGuardando = input(false);
  readonly guardar = output();
  readonly cancelar = output();

  getTituloModal(): string {
    return this.modo() === 'crear' ? 'Nuevo docente' : 'Editar docente';
  }

  getTextoBotonGuardar(): string {
    return this.modo() === 'crear' ? 'Crear docente' : 'Guardar cambios';
  }

  getFieldError(controlName: string): string | null {
    const control = this.form().get(controlName);
    if (!control || !control.touched || !control.errors) return null;
    if (control.errors['required']) return 'Este campo es obligatorio';
    if (control.errors['email']) return 'Ingresa un email válido';
    if (control.errors['minlength'])
      return `Mínimo ${control.errors['minlength'].requiredLength} caracteres`;
    return 'Valor inválido';
  }

  stringifyTipoDocumento(item: TipoDocumento | null): string {
    return item?.nombre ?? '';
  }

  stringifyGrado(item: GradoAcademico | null): string {
    return item?.nombre ?? '';
  }

  stringifyEspecializacion(item: Especializacion | null): string {
    return item?.nombre ?? '';
  }
}
