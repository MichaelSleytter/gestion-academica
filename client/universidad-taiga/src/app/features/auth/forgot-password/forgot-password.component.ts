import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TuiButton, TuiLink, TuiLoader } from '@taiga-ui/core';
import { AuthService } from '../../../core/services/auth.service';

/**
 * Forgot password page — two-panel split layout (image + form).
 *
 * Allows users to request a password reset email.
 * Uses design system tokens matching login page:
 * - **Font:** Inter
 * - **Colors:** Primary #4F46E5, text-strong #0F172A, text-muted #64748B
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TuiButton, TuiLink, TuiLoader],
  template: `
    <div class="forgot-page">
      <!-- Left panel: background image -->
      <div class="forgot-image">
        <div class="forgot-image-overlay"></div>
      </div>

      <!-- Right panel: form content -->
      <div class="forgot-content">
        <div class="forgot-form">
          <!-- Logo -->
          <img
            src="data:image/svg+xml,%3Csvg width='37' height='49' viewBox='0 0 37 49' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M37 37L37 18.5C37 8.28274 28.7173 1.41726e-05 18.5 1.28328e-05C8.28274 1.1493e-05 1.13952e-05 8.28274 1.00554e-05 18.5L7.62939e-06 37L37 37Z' fill='%234F46E5'/%3E%3Cpath d='M7.78945 36.9999C7.78945 30.9054 12.73 25.9648 18.8245 25.9648C24.919 25.9648 29.8596 30.9054 29.8596 36.9999L7.78945 36.9999Z' fill='%23DC2626'/%3E%3Cpath d='M7.78945 37.0001C7.78945 43.0946 12.73 48.0352 18.8245 48.0352C24.919 48.0352 29.8596 43.0946 29.8596 37.0001L7.78945 37.0001Z' fill='%23CA8A04'/%3E%3Cpath d='M18.5 22.0701C21.5472 22.0701 24.0175 19.5998 24.0175 16.5525C24.0175 13.5053 21.5472 11.035 18.5 11.035C15.4527 11.035 12.9824 13.5053 12.9824 16.5525C12.9824 19.5998 15.4527 22.0701 18.5 22.0701Z' fill='%23FAFAFF'/%3E%3C/svg%3E"
            alt="Logo"
            class="logo"
            width="37"
            height="49"
          />

          <!-- Header -->
          <h1 class="heading">¿Olvidaste tu contraseña?</h1>

          <!-- Description -->
          <p class="description">
            Ingresá tu email y te enviaremos un enlace para restablecer tu contraseña.
          </p>

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
                  id="email"
                  type="email"
                  formControlName="email"
                  placeholder="Ingrese su dirección de correo"
                  class="field-input"
                  autocomplete="email"
                />
              </div>
              @if (form.controls.email.invalid && form.controls.email.touched) {
                <p class="error-text">Ingresa un email válido</p>
              }
            </div>

            <!-- Submit -->
            <button
              tuiButton
              type="submit"
              size="l"
              appearance="primary"
              class="btn-submit"
              [disabled]="form.invalid || isLoading() || sent()"
            >
              @if (isLoading()) {
                <tui-loader size="s" class="btn-loader" />
                <span>Enviando...</span>
              } @else {
                Enviar cambio de contraseña
              }
            </button>

            <!-- Success message -->
            @if (sent()) {
              <div class="success-message">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z" fill="#16A34A"/>
                </svg>
                <span>Si el email existe, recibirás un enlace para restablecer tu contraseña.</span>
              </div>
            }

            <!-- Back to login -->
            <a routerLink="/login" tuiLink class="back-link">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" fill="#4F46E5"/>
              </svg>
              Regresar al Login
            </a>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      /* ══════════════════════════════════════════════════════════════
         FORGOT PASSWORD — Two-panel split layout
         Design tokens from design-system/MASTER.md
         ══════════════════════════════════════════════════════════════ */

      .forgot-page {
        display: flex;
        min-height: 100vh;
        background: var(--color-surface, #f8fafc);
        font-family: 'Inter', system-ui, sans-serif;
      }

      /* ─── Left panel: image ───────────────────────────────────── */
      .forgot-image {
        flex: 1 1 0;
        min-width: 0;
        background: url('/login-bg.png') center / cover no-repeat #d9d9d9;
      }

      .forgot-image-overlay {
        width: 100%;
        height: 100%;
        background: rgba(15, 23, 42, 0.25);
      }

      /* ─── Right panel: content ────────────────────────────────── */
      .forgot-content {
        flex: 0 0 auto;
        width: 720px;
        background: var(--color-background, #ffffff);
        border-radius: 60px 0 0 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 96px;
      }

      .forgot-form {
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
      }

      /* ─── Description ─────────────────────────────────────────── */
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

      /* ─── Submit button ───────────────────────────────────────── */
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

      /* ─── Success message ─────────────────────────────────────── */
      .success-message {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 14px 16px;
        background: #f0fdf4;
        border: 1px solid #bbf7d0;
        border-radius: 0.5rem;
        font-family: 'Inter', system-ui, sans-serif;
        font-size: 14px;
        line-height: 22px;
        color: #166534;
        width: 100%;
        box-sizing: border-box;
      }

      .success-message svg {
        flex-shrink: 0;
        margin-top: 1px;
      }

      /* ─── Back to login link ──────────────────────────────────── */
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

      /* ─── Responsive: mobile ──────────────────────────────────── */
      @media (max-width: 900px) {
        .forgot-image {
          display: none;
        }

        .forgot-content {
          width: 100%;
          border-radius: 0;
          padding: 0 24px;
        }

        .forgot-form {
          max-width: 400px;
        }
      }
    `,
  ],
})
export class ForgotPasswordComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  isLoading = signal(false);
  sent = signal(false);
  error = signal('');

  /**
   * Envía la solicitud de restablecimiento de contraseña al backend.
   * Siempre muestra el mismo mensaje de éxito por seguridad
   * (no revela si el email existe o no).
   */
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.error.set('');

    const email = this.form.get('email')!.value!;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.sent.set(true);
      },
      error: () => {
        // Siempre mostrar éxito por seguridad (no revelar existencia del email)
        this.isLoading.set(false);
        this.sent.set(true);
      },
    });
  }
}
