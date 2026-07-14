import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiLink, TuiLoader } from '@taiga-ui/core';
import { AuthService } from '../../../core/services/auth.service';
import { RoleService } from '../../../core/services/role.service';
import type { LoginRequest } from '../../../models/auth.model';

/**
 * Página de inicio de sesión con layout dividido en dos paneles (imagen + formulario).
 *
 * @description
 * Adaptada del diseño de Figma usando tokens del design system:
 * - **Font:** Inter
 * - **Colors:** Primary #4F46E5, text-strong #0F172A, text-muted #64748B
 * - **Radius:** 0.375rem (inputs), 9999px (button pill)
 *
 * Maneja estados de carga, validación de formulario (email + password) y errores
 * de autenticación con mensajes específicos según el código HTTP:
 * - 401 → credenciales incorrectas
 * - 403 → cuenta desactivada
 * - 0 → error de conexión
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiLink, TuiLoader],
  template: `
    <div class="login-page">
      <!-- Left panel: background image -->
      <div class="login-image">
        <div class="login-image-overlay"></div>
      </div>

      <a class="skip-link" href="#login-main">Saltar al inicio de sesión</a>

      <!-- Right panel: form content -->
      <main id="login-main" class="login-content" tabindex="-1">
        <div class="login-form">
          <!-- Logo -->
          <img
            src="data:image/svg+xml,%3Csvg width='37' height='49' viewBox='0 0 37 49' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M37 37L37 18.5C37 8.28274 28.7173 1.41726e-05 18.5 1.28328e-05C8.28274 1.1493e-05 1.13952e-05 8.28274 1.00554e-05 18.5L7.62939e-06 37L37 37Z' fill='%234F46E5'/%3E%3Cpath d='M7.78945 36.9999C7.78945 30.9054 12.73 25.9648 18.8245 25.9648C24.919 25.9648 29.8596 30.9054 29.8596 36.9999L7.78945 36.9999Z' fill='%23DC2626'/%3E%3Cpath d='M7.78945 37.0001C7.78945 43.0946 12.73 48.0352 18.8245 48.0352C24.919 48.0352 29.8596 43.0946 29.8596 37.0001L7.78945 37.0001Z' fill='%23CA8A04'/%3E%3Cpath d='M18.5 22.0701C21.5472 22.0701 24.0175 19.5998 24.0175 16.5525C24.0175 13.5053 21.5472 11.035 18.5 11.035C15.4527 11.035 12.9824 13.5053 12.9824 16.5525C12.9824 19.5998 15.4527 22.0701 18.5 22.0701Z' fill='%23FAFAFF'/%3E%3C/svg%3E"
            alt="Logo"
            class="logo"
            width="37"
            height="49"
          />

          <!-- Header -->
          <h1 class="heading">Iniciar Sesión</h1>

          <!-- Form -->
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="form">
            <!-- Email -->
            <div class="field-group">
              <label class="field-label" for="email">Email</label>
              <div
                class="input-wrapper"
                [class.input-error]="form.controls.email.invalid && form.controls.email.touched"
              >
                <input
                  #emailInput
                  id="email"
                  name="email"
                  type="email"
                  formControlName="email"
                  placeholder="Ej. nombre@universidad.edu…"
                  class="field-input"
                  autocomplete="email"
                  spellcheck="false"
                  [attr.aria-invalid]="
                    form.controls.email.invalid && form.controls.email.touched
                  "
                  [attr.aria-describedby]="
                    form.controls.email.invalid && form.controls.email.touched
                      ? 'email-error'
                      : null
                  "
                />
              </div>
              @if (form.controls.email.invalid && form.controls.email.touched) {
                <p id="email-error" class="error-text" aria-live="polite">
                  Ingresa un email válido
                </p>
              }
            </div>

            <!-- Password -->
            <div class="field-group">
              <label class="field-label" for="password">Contraseña</label>
              <div
                class="input-wrapper"
                [class.input-error]="
                  form.controls.password.invalid && form.controls.password.touched
                "
              >
                <input
                  #passwordInput
                  id="password"
                  name="password"
                  [type]="passwordVisible() ? 'text' : 'password'"
                  formControlName="password"
                  placeholder="Ej. mínimo 6 caracteres…"
                  class="field-input"
                  autocomplete="current-password"
                  [attr.aria-invalid]="
                    form.controls.password.invalid && form.controls.password.touched
                  "
                  [attr.aria-describedby]="
                    form.controls.password.invalid && form.controls.password.touched
                      ? 'password-error'
                      : null
                  "
                />
                <button
                  type="button"
                  class="eye-btn"
                  (click)="togglePassword()"
                  [attr.aria-label]="
                    passwordVisible() ? 'Ocultar contraseña' : 'Mostrar contraseña'
                  "
                >
                  @if (passwordVisible()) {
                    <svg
                      aria-hidden="true"
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M12 5C7 5 2.73 8.11 1 12c1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7zm0 12c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"
                        fill="#64748B"
                      />
                      <path
                        d="M12 9c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
                        fill="#64748B"
                      />
                    </svg>
                  } @else {
                    <svg
                      aria-hidden="true"
                      width="24"
                      height="24"
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-3.89-6-7-11-7-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 3.89 6 7 11 7 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"
                        fill="#64748B"
                      />
                    </svg>
                  }
                </button>
              </div>
              @if (form.controls.password.invalid && form.controls.password.touched) {
                <p id="password-error" class="error-text" aria-live="polite">
                  La contraseña es obligatoria (mínimo 6 caracteres)
                </p>
              }
            </div>

            @if (authError()) {
              <p class="auth-error" role="alert">{{ authError() }}</p>
            }

            <!-- Submit -->
            <button
              tuiButton
              type="submit"
              size="l"
              appearance="primary"
              class="btn-submit"
              [disabled]="isLoading()"
            >
              @if (isLoading()) {
                <tui-loader size="s" class="btn-loader" />
                <span>Ingresando…</span>
              } @else {
                Iniciar Sesión
              }
            </button>

            @if (isLoading()) {
              <p class="sr-only" role="status" aria-live="polite">Ingresando…</p>
            }

            <!-- Forgot password -->
            <a routerLink="/forgot-password" tuiLink class="forgot-link"
              >¿Olvidaste tu contraseña?</a
            >
          </form>
        </div>
      </main>
    </div>
  `,
  styles: [
    `
      /* ══════════════════════════════════════════════════════════════
         LOGIN PAGE — Two-panel split layout
         Design tokens from design-system/MASTER.md
         ══════════════════════════════════════════════════════════════ */

      .login-page {
        display: flex;
        min-height: 100vh;
        min-height: 100dvh;
        background: var(--color-surface, #f8fafc);
        font-family: 'Inter', system-ui, sans-serif;
      }

      .skip-link {
        position: absolute;
        top: 16px;
        left: 16px;
        z-index: 10;
        transform: translateY(-200%);
        padding: 10px 14px;
        border-radius: 0.375rem;
        background: var(--color-background, #ffffff);
        color: var(--color-primary, #4f46e5);
        font-weight: 600;
        text-decoration: none;
        transition: transform 0.15s ease;
      }

      .skip-link:focus-visible {
        transform: translateY(0);
      }

      /* ─── Left panel: image ───────────────────────────────────── */
      .login-image {
        flex: 1 1 0;
        min-width: 0;
        background: url('/login-bg.png') center / cover no-repeat #d9d9d9;
      }

      .login-image-overlay {
        width: 100%;
        height: 100%;
        background: rgba(15, 23, 42, 0.25);
      }

      /* ─── Right panel: content ────────────────────────────────── */
      .login-content {
        flex: 0 0 auto;
        width: 720px;
        background: var(--color-background, #ffffff);
        border-radius: 60px 0 0 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 96px;
      }

      .login-form {
        width: 100%;
        max-width: 528px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 24px;
      }

      /* ─── Logo ────────────────────────────────────────────────── */
      .logo {
        width: 37px;
        height: 49px;
        display: block;
      }

      /* ─── Heading ─────────────────────────────────────────────── */
      .heading {
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 600;
        font-size: 32px;
        line-height: 41px;
        text-align: center;
        color: var(--color-text-strong, #0f172a);
        margin: 0;
        letter-spacing: -0.025em;
        text-wrap: balance;
      }

      /* ─── Form ────────────────────────────────────────────────── */
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

      .auth-error {
        width: 100%;
        box-sizing: border-box;
        margin: 0;
        padding: 12px 16px;
        border: 1px solid var(--color-danger, #dc2626);
        border-radius: 0.375rem;
        color: var(--color-danger, #dc2626);
        font-size: 14px;
        line-height: 20px;
        overflow-wrap: anywhere;
      }

      .sr-only {
        position: absolute;
        width: 1px;
        height: 1px;
        padding: 0;
        margin: -1px;
        overflow: hidden;
        clip: rect(0, 0, 0, 0);
        white-space: nowrap;
        border: 0;
      }

      /* ─── Eye toggle button ───────────────────────────────────── */
      .eye-btn {
        flex-shrink: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        padding: 0;
        border: none;
        background: none;
        cursor: pointer;
        color: var(--color-text-muted, #64748b);
        transition: opacity 0.15s ease;
        touch-action: manipulation;
        -webkit-tap-highlight-color: transparent;
      }

      .eye-btn:hover {
        opacity: 0.7;
      }

      .eye-btn svg {
        display: block;
      }

      /* ─── Submit button ───────────────────────────────────────── */
      .btn-submit {
        width: 100%;
        height: 56px;
        border-radius: 9999px;
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 500;
        font-size: 18px;
        line-height: normal;
        touch-action: manipulation;
        -webkit-tap-highlight-color: transparent;
      }

      .btn-loader {
        margin-right: 8px;
      }

      /* ─── Forgot password link ────────────────────────────────── */
      .forgot-link {
        font-family: 'Inter', system-ui, sans-serif;
        font-weight: 500;
        font-size: 16px;
        line-height: 25px;
        color: var(--color-primary, #4f46e5);
        text-decoration: none;
        cursor: pointer;
        touch-action: manipulation;
        -webkit-tap-highlight-color: transparent;
      }

      .forgot-link:hover {
        color: var(--color-primary-hover, #4338ca);
      }

      /* ─── Responsive: mobile ──────────────────────────────────── */
      @media (max-width: 900px) {
        .login-image {
          display: none;
        }

        .login-content {
          width: 100%;
          border-radius: 0;
          padding: max(24px, env(safe-area-inset-top))
            max(24px, env(safe-area-inset-right)) max(24px, env(safe-area-inset-bottom))
            max(24px, env(safe-area-inset-left));
        }

        .login-form {
          max-width: 400px;
        }
      }

      @media (prefers-reduced-motion: reduce) {
        .input-wrapper,
        .skip-link,
        .eye-btn {
          transition: none;
        }
      }
    `,
  ],
})
export class LoginComponent {
  private authService = inject(AuthService);
  private roleService = inject(RoleService);
  private router = inject(Router);

  /** Indica si la contraseña se muestra en texto plano. */
  passwordVisible = signal(false);

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
  });

  isLoading = signal(false);
  authError = signal('');

  private readonly emailInput = viewChild.required<ElementRef<HTMLInputElement>>('emailInput');
  private readonly passwordInput =
    viewChild.required<ElementRef<HTMLInputElement>>('passwordInput');

  /**
   * Alterna la visibilidad del campo de contraseña entre texto y password.
   */
  togglePassword(): void {
    this.passwordVisible.update((v) => !v);
  }

  /**
   * Procesa el envío del formulario de inicio de sesión.
   *
   * @description
   * 1. Valida el formulario (email + password obligatorios).
   * 2. Marca todos los campos como touched si el formulario es inválido.
   * 3. Llama a `AuthService.login()` con las credenciales.
   * 4. En éxito, redirige al home del rol del usuario via `RoleService.getHomeRouteByRole()`.
   * 5. En error, muestra un mensaje persistente con el paso recomendado.
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.focusFirstInvalidControl();
      return;
    }

    this.isLoading.set(true);
    this.authError.set('');

    const credentials: LoginRequest = {
      email: this.form.value.email ?? '',
      password: this.form.value.password ?? '',
    };

    this.authService.login(credentials.email, credentials.password).subscribe({
      next: () => {
        this.router.navigateByUrl(this.roleService.getHomeRouteByRole());
      },
      error: (error) => {
        this.isLoading.set(false);

        if (error.status === 401) {
          this.authError.set(
            'Email o contraseña incorrectos. Revisa tus datos o restablece tu contraseña.',
          );
        } else if (error.status === 403) {
          this.authError.set(
            'Tu cuenta ha sido desactivada. Contacta a la administración para recuperar el acceso.',
          );
        } else if (error.status === 0) {
          this.authError.set(
            'No se pudo conectar al servidor. Verifica tu conexión e intenta de nuevo.',
          );
        } else {
          this.authError.set(error.error?.message || 'Ocurrió un error. Intenta de nuevo.');
        }
      },
    });
  }

  private focusFirstInvalidControl(): void {
    if (this.form.controls.email.invalid) {
      this.emailInput().nativeElement.focus();
    } else {
      this.passwordInput().nativeElement.focus();
    }
  }
}
