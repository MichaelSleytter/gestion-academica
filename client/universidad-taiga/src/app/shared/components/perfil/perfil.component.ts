import { ChangeDetectionStrategy, Component, inject, type OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiError, TuiIcon, TuiLoader, TuiNotificationService, TuiTextfield, TuiTitle } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { PerfilService } from '../../../core/services/perfil.service';
import type { PerfilResponse } from '../../../models/auth.model';

@Component({
  selector: 'app-perfil',
  imports: [
    ReactiveFormsModule,
    TuiCardLarge,
    TuiHeader,
    TuiTitle,
    TuiTextfield,
    TuiButton,
    TuiIcon,
    TuiLoader,
    TuiError,
  ],
  templateUrl: './perfil.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Perfil implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly perfilService = inject(PerfilService);
  private readonly notifications = inject(TuiNotificationService);

  /** Estado de carga inicial */
  readonly loading = signal(true);
  /** Error de carga inicial */
  readonly loadError = signal('');

  /** Perfil obtenido del backend */
  perfil = signal<PerfilResponse | null>(null);

  // ── Formulario de edición ──────────────────────────────
  readonly editForm = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    emailPersonal: [''],
  });

  readonly editSaving = signal(false);

  // ── Formulario de cambio de contraseña ──────────────────
  readonly passwordForm = this.fb.group({
    passwordActual: ['', Validators.required],
    nuevaPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
  });

  readonly passwordSaving = signal(false);
  readonly passwordError = signal('');
  readonly passwordSuccess = signal(false);

  ngOnInit(): void {
    this.cargarPerfil();
  }

  private cargarPerfil(): void {
    this.loading.set(true);
    this.loadError.set('');

    this.perfilService.getPerfil().then((p) => {
      this.perfil.set(p);
      this.editForm.patchValue({
        nombre: p.nombre,
        apellido: p.apellido,
        emailPersonal: p.emailPersonal ?? '',
      });
      this.loading.set(false);
    }).catch(() => {
      this.loadError.set('No se pudo cargar tu perfil. Intentalo de nuevo.');
      this.loading.set(false);
    });
  }

  guardarPerfil(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.editSaving.set(true);
    const { nombre, apellido, emailPersonal } = this.editForm.value;

    this.perfilService.actualizarPerfil({
      nombre: nombre ?? undefined,
      apellido: apellido ?? undefined,
      emailPersonal: emailPersonal ?? undefined,
    }).then((p) => {
      this.perfil.set(p);
      this.editSaving.set(false);
      this.notifications.open('Perfil actualizado correctamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 }).subscribe();
    }).catch(() => {
      this.editSaving.set(false);
      this.notifications.open('Error al actualizar el perfil', { label: 'Error', appearance: 'error', autoClose: 5000 }).subscribe();
    });
  }

  cambiarPassword(): void {
    this.passwordError.set('');
    this.passwordSuccess.set(false);

    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { passwordActual, nuevaPassword, confirmPassword } = this.passwordForm.value;

    if (nuevaPassword !== confirmPassword) {
      this.passwordError.set('Las contraseñas no coinciden');
      return;
    }

    this.passwordSaving.set(true);

    this.perfilService.cambiarPassword({
      passwordActual: passwordActual!,
      nuevaPassword: nuevaPassword!,
    }).then(() => {
      this.passwordSaving.set(false);
      this.passwordSuccess.set(true);
      this.passwordForm.reset();
      this.notifications.open('Contraseña actualizada correctamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 }).subscribe();
    }).catch((err) => {
      this.passwordSaving.set(false);
      this.passwordError.set(err.error?.message || 'Error al cambiar la contraseña');
    });
  }
}
