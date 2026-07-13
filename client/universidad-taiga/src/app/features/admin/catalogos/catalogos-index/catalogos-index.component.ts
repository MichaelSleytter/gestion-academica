import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TuiIcon, TuiTitle } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';

interface CatalogCard {
  title: string;
  description: string;
  route: string;
  icon: string;
  action: string;
}

@Component({
  selector: 'app-catalogos-index',
  imports: [RouterLink, TuiCardLarge, TuiHeader, TuiIcon, TuiTitle],
  template: `
    <div class="card flex flex-col gap-4 p-2.5">
      <div appearance="floating" tuiCardLarge>
        <header tuiHeader class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div
              class="flex size-12 items-center justify-center rounded-2xl bg-primary/10 text-primary"
              aria-hidden="true"
            >
              <tui-icon icon="@tui.database" />
            </div>
            <div>
              <h2 tuiTitle>Catálogos</h2>
              <p class="text-sm font-medium text-text-secondary">
                Administra datos maestros usados por docentes, cursos y ciclos académicos.
              </p>
            </div>
          </div>
        </header>
      </div>

      <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4" aria-label="Catálogos administrativos">
        @for (card of cards; track card.route) {
          <a
            appearance="floating"
            tuiCardLarge
            class="group flex min-h-56 flex-col justify-between rounded-3xl border border-border bg-surface p-5 no-underline transition hover:-translate-y-0.5 hover:border-primary hover:shadow-lg focus-visible:outline focus-visible:outline-2 focus-visible:outline-primary"
            [routerLink]="card.route"
            [attr.aria-label]="card.action"
          >
            <div class="flex flex-col gap-4">
              <div
                class="flex size-12 items-center justify-center rounded-2xl bg-surface-hover text-primary group-hover:bg-primary group-hover:text-on-primary"
                aria-hidden="true"
              >
                <tui-icon [icon]="card.icon" />
              </div>
              <div>
                <h3 class="text-lg font-bold text-text-primary">{{ card.title }}</h3>
                <p class="mt-2 text-sm leading-6 text-text-secondary">{{ card.description }}</p>
              </div>
            </div>
            <span class="mt-5 inline-flex items-center gap-2 text-sm font-bold text-primary">
              {{ card.action }}
              <tui-icon icon="@tui.arrow-right" />
            </span>
          </a>
        }
      </section>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogosIndex {
  readonly cards: CatalogCard[] = [
    {
      title: 'Carreras',
      description: 'Mantiene los programas académicos visibles para estudiantes.',
      route: 'carreras',
      icon: '@tui.book-open',
      action: 'Administrar carreras',
    },
    {
      title: 'Grados académicos',
      description: 'Define los grados que se asignan a cada docente.',
      route: 'grados-academicos',
      icon: '@tui.graduation-cap',
      action: 'Administrar grados',
    },
    {
      title: 'Especializaciones',
      description: 'Centraliza especializaciones docentes para formularios y reportes.',
      route: 'especializaciones',
      icon: '@tui.tags',
      action: 'Administrar especializaciones',
    },
    {
      title: 'Ciclos académicos',
      description: 'Consulta ciclos y genera semestres por año académico.',
      route: 'ciclos',
      icon: '@tui.calendar-days',
      action: 'Administrar ciclos',
    },
  ];
}
