import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { TuiButton, TuiLoader } from '@taiga-ui/core';
import { map } from 'rxjs';
import { useMatriculasBySeccionQuery } from '../../../queries/docente-role.query';

@Component({
  selector: 'app-estudiantes-seccion',
  imports: [TuiButton, TuiLoader],
  template: `
    <div class="flex flex-col gap-4 p-2.5">
      <div class="flex items-center gap-3">
        <button appearance="flat" tuiButton type="button" (click)="goBack()" iconStart="@tui.arrow-left">
          Volver
        </button>
        <h2 class="text-2xl font-bold text-text-primary">Estudiantes de la sección</h2>
      </div>

      @if (query.isPending()) {
        <div class="flex min-h-64 items-center justify-center rounded-2xl border border-border bg-surface">
          <tui-loader size="l">Cargando estudiantes...</tui-loader>
        </div>
      } @else if (query.isError()) {
        <div class="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm font-medium text-red-700">
          No se pudieron cargar los estudiantes.
        </div>
      } @else {
        <div class="overflow-hidden rounded-2xl border border-border bg-surface shadow-sm">
          <table class="w-full text-left text-sm">
            <thead>
              <tr class="border-b border-border bg-surface-hover text-xs font-semibold uppercase tracking-wide text-text-tertiary">
                <th class="px-5 py-3">Código</th>
                <th class="px-5 py-3">Nombre</th>
                <th class="px-5 py-3">Apellido</th>
                <th class="px-5 py-3">Email</th>
                <th class="px-5 py-3">Estado</th>
              </tr>
            </thead>
            <tbody>
              @for (matricula of estudiantes(); track matricula.idMatricula) {
                <tr class="border-b border-border last:border-b-0 hover:bg-surface-hover">
                  <td class="px-5 py-3 font-medium text-text-primary">{{ matricula.codigoEstudiante }}</td>
                  <td class="px-5 py-3 text-text-primary">{{ matricula.nombre }}</td>
                  <td class="px-5 py-3 text-text-primary">{{ matricula.apellido }}</td>
                  <td class="px-5 py-3 text-text-secondary">{{ matricula.email }}</td>
                  <td class="px-5 py-3">
                    <span class="rounded-full bg-success px-2 py-0.5 text-xs font-semibold text-on-success">
                      {{ matricula.estado }}
                    </span>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="px-5 py-10 text-center text-text-secondary">
                    No hay estudiantes matriculados en esta sección.
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EstudiantesSeccion {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly idSeccion = toSignal(
    this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
    { initialValue: 0 },
  );

  readonly query = useMatriculasBySeccionQuery(this.idSeccion);
  readonly estudiantes = this.query.data;

  goBack(): void {
    void this.router.navigate(['/app/docente/mis-cursos']);
  }
}
