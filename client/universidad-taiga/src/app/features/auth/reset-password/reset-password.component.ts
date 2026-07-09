import { Component, inject, signal } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import {
  type AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { TuiButton, TuiLink, TuiLoader } from '@taiga-ui/core';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Página de restablecimiento de contraseña mediante token.
 *
 * @description
 * Layout dividido en dos paneles (imagen + formulario), consistente con login.
 * Permite al usuario establecer una nueva contraseña usando el token UUID
 * recibido por email. Incluye:
 * - Validación de contraseña (mín. 8 caracteres).
 * - Validación de coincidencia entre contraseña y confirmación.
 * - Manejo de token inválido o expirado.
 * - Pantalla de éxito post-restablecimiento.
 *
 * El token se obtiene del query param `?token=...` en la URL.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiLink, TuiLoader],
  template: `
    <div class="reset-page">
      <!-- Left panel: background image -->
      <div class="reset-image">
        <div class="reset-image-overlay"></div>
      </div>

      <!-- Right panel: form content -->
      <div class="reset-content">
        <div class="reset-form">
          <!-- Logo -->
          <img
            src="data:image/svg+xml,%3Csvg width='37' height='49' viewBox='0 0 37 49' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M37 37L37 18.5C37 8.28274 28.7173 1.41726e-05 18.5 1.28328e-05C8.28274 1.1493e-05 1.13952e-05 8.28274 1.00554e-05 18.5L7.62939e-06 37L37 37Z' fill='%234F46E5'/%3E%3Cpath d='M7.78945 36.9999C7.78945 30.9054 12.73 25.9648 18.8245 25.9648C24.919 25.9648 29.8596 30.9054 29.8596 36.9999L7.78945 36.9999Z' fill='%23DC2626'/%3E%3Cpath d='M7.78945 37.0001C7.78945 43.0946 12.73 48.0352 18.8245 48.0352C24.919 48.0352 29.8596 43.0946 29.8596 37.0001L7.78945 37.0001Z' fill='%23CA8A04'/%3E%3Cpath d='M18.5 22.0701C21.5472 22.0701 24.0175 19.5998 24.0175 16.5525C24.0175 13.5053 21.5472 11.035 18.5 11.035C15.4527 11.035 12.9824 13.5053 12.9824 16.5525C12.9824 19.5998 15.4527 22.0701 18.5 22.0701Z' fill='%23FAFAFF'/%3E%3C/svg%3E"
            alt="Logo"
            class="logo"
            width="37"
            height="49"
          />

          @if (!success()) {
            <!-- Header -->
            <h1 class="heading">Restablecer contraseña</h1>

            <!-- Description -->
            <p class="description">Ingresá tu nueva contraseña para acceder a tu cuenta.</p>

            <!-- Form -->
            <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
              <!-- New password -->
              <div class="field-group">
                <label class="field-label" for="password">Nueva contraseña</label>
                <div
                  class="input-wrapper"
                  [class.input-error]="
                    form.controls.password.invalid && form.controls.password.touched
                  "
                >
                  <input
                    id="password"
                    type="password"
                    formControlName="password"
                    placeholder="Mínimo 8 caracteres"
                    class="field-input"
                    autocomplete="new-password"
                  />
                </div>
                @if (form.controls.password.invalid && form.controls.password.touched) {
                  <p class="error-text">La contraseña debe tener al menos 8 caracteres</p>
                }
              </div>

              <!-- Confirm password -->
              <div class="field-group">
                <label class="field-label" for="confirmPassword">Confirmar contraseña</label>
                <div
                  class="input-wrapper"
                  [class.input-error]="
                    form.hasError('mismatch') && form.controls.confirmPassword.touched
                  "
                >
                  <input
                    id="confirmPassword"
                    type="password"
                    formControlName="confirmPassword"
                    placeholder="Repetí la contraseña"
                    class="field-input"
                    autocomplete="new-password"
                  />
                </div>
                @if (form.hasError('mismatch') && form.controls.confirmPassword.touched) {
                  <p class="error-text">Las contraseñas no coinciden</p>
                }
              </div>

              <!-- Error message -->
              @if (error()) {
                <div class="error-message">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
                      fill="#DC2626"
                    />
                  </svg>
                  <span>{{ error() }}</span>
                </div>
              }

              <!-- Submit -->
              <button
                tuiButton
                type="submit"
                size="l"
                appearance="primary"
                class="btn-submit"
                [disabled]="form.invalid || isLoading()"
              >
                @if (isLoading()) {
                  <tui-loader size="s" class="btn-loader" />
                  <span>Restableciendo...</span>
                } @else {
                  Restablecer contraseña
                }
              </button>
            </form>
          } @else {
            <!-- Success message -->
            <div class="success-state">
              <div class="success-icon">
                <svg
                  width="48"
                  height="48"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"
                    fill="#16A34A"
                  />
                </svg>
              </div>
              <h1 class="heading">Contraseña actualizada</h1>
              <p class="description">
                Tu contraseña se restableció correctamente. Ya podés iniciar sesión con tu nueva
                contraseña.
              </p>
              <button
                tuiButton
                size="l"
                appearance="primary"
                class="btn-submit"
                routerLink="/login"
              >
                Ir al Login
              </button>
            </div>
          }

          <!-- Back to login -->
          <a routerLink="/login" tuiLink class="back-link">
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"
                fill="#4F46E5"
              />
            </svg>
            Regresar al Login
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .reset-page {
        display: flex;
        min-height: 100vh;
        background: var(--color-surface, #f8fafc);
        font-family: 'Inter', system-ui, sans-serif;
      }

      .reset-image {
        flex: 1 1 0;
        min-width: 0;
        background: url('/login-bg.png') center / cover no-repeat #d9d9d9;
      }

      .reset-image-overlay {
        width: 100%;
        height: 100%;
        background: rgba(15, 23, 42, 0.25);
      }

      .reset-content {
        flex: 0 0 auto;
        width: 720px;
        background: var(--color-background, #ffffff);
        border-radius: 60px 0 0 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 96px;
      }

      .reset-form {
        width: 100%;
        max-width: 528px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 24px;
      }

      .logo {
        width: 37px;
        height: 49px;
        display: block;
      }

      .heading {
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 600;
        font-size: 32px;
        line-height: 41px;
        text-align: center;
        color: var(--color-text-strong, #0f172a);
        margin: 0;
        letter-spacing: -0.025em;
      }

      .description {
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 400;
        font-size: 16px;
        line-height: 25px;
        text-align: center;
        color: var(--color-text-muted, #64748b);
        margin: -8px 0 0;
        max-width: 400px;
      }

      .form {
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 32px;
      }

      .field-group {
        width: 100%;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .field-label {
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 500;
        font-size: 16px;
        line-height: 25px;
        color: var(--color-text-muted, #64748b);
      }

      .input-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        width: 100%;
        padding: 16px 18px;
        border: 1px solid var(--color-border, #e2e8f0);
        border-radius: 0.375rem;
        background: var(--color-background, #ffffff);
        transition: border-color 0.15s ease;
        box-sizing: border-box;
      }

      .input-wrapper:focus-within {
        border-color: var(--color-primary, #4f46e5);
        box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.15);
      }

      .input-wrapper.input-error {
        border-color: var(--color-danger, #dc2626);
      }

      .field-input {
        flex: 1;
        border: none;
        outline: none;
        background: transparent;
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 400;
        font-size: 16px;
        line-height: 24px;
        color: var(--color-text-strong, #0f172a);
        padding: 0;
        min-width: 0;
      }

      .field-input::placeholder {
        color: var(--color-text-muted, #64748b);
        font-weight: 400;
      }

      .error-text {
        font-family: 'Inter', system-ui, sans-serif;
        font-size: 13px;
        color: var(--color-danger, #dc2626);
        margin: -4px 0 0 2px;
      }

      .error-message {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 14px 16px;
        background: #fef2f2;
        border: 1px solid #fecaca;
        border-radius: 0.5rem;
        font-family: 'Inter', system-ui, sans-serif;
        font-size: 14px;
        line-height: 22px;
        color: #991b1b;
        width: 100%;
        box-sizing: border-box;
      }

      .error-message svg {
        flex-shrink: 0;
        margin-top: 1px;
      }

      .btn-submit {
        width: 100%;
        height: 56px;
        border-radius: 9999px;
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 500;
        font-size: 18px;
        line-height: normal;
      }

      .btn-loader {
        margin-right: 8px;
      }

      .success-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16px;
        text-align: center;
      }

      .success-icon {
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0fdf4;
        border-radius: 50%;
      }

      .back-link {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 500;
        font-size: 16px;
        line-height: 25px;
        color: var(--color-primary, #4f46e5);
        text-decoration: none;
        cursor: pointer;
      }

      .back-link:hover {
        color: var(--color-primary-hover, #4338ca);
      }

      .back-link svg {
        flex-shrink: 0;
      }

      @media (max-width: 900px) {
        .reset-image {
          display: none;
        }

        .reset-content {
          width: 100%;
          border-radius: 0;
          padding: 0 24px;
        }

        .reset-form {
          max-width: 400px;
        }
      }
    `,
  ],
})
export class ResetPasswordComponent {
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);

  token = signal<string | null>(null);

  form = new FormGroup(
    {
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: this.passwordMatchValidator },
  );

  isLoading = signal(false);
  success = signal(false);
  error = signal('');

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.error.set(
        'Token inválido o expirado. Solicitá un nuevo restablecimiento de contraseña.',
      );
    } else {
      this.token.set(token);
    }
  }

  /**
   * Procesa el envío del formulario de nueva contraseña.
   *
   * @description
   * 1. Valida el formulario (contraseña mín. 8 caracteres + coincidencia).
   * 2. Verifica que exista un token válido en la URL.
   * 3. Llama a `AuthService.resetPassword()` con token + nueva contraseña.
   * 4. En éxito, muestra la pantalla de confirmación (`success = true`).
   * 5. En error, muestra mensaje de token inválido o expirado.
   */
  onSubmit(): void {
    if (this.form.invalid || !this.token()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.error.set('');

    const nuevaPassword = this.form.get('password')!.value!;

    this.authService.resetPassword(this.token()!, nuevaPassword).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.success.set(true);
      },
      error: () => {
        this.isLoading.set(false);
        this.error.set(
          'El enlace es inválido o ya expiró. Solicitá un nuevo restablecimiento de contraseña.',
        );
      },
    });
  }

  /**
   * Validador personalizado que verifica que password y confirmPassword coincidan.
   *
   * @param control - FormGroup que contiene los campos `password` y `confirmPassword`.
   * @returns `null` si coinciden, o `{ mismatch: true }` si no.
   */
  private passwordMatchValidator(control: AbstractControl): { mismatch: boolean } | null {
    const password = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }
}
