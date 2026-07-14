import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  type OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  TuiButton,
  TuiDialogService,
  TuiError,
  TuiIcon,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiTable, TuiTablePagination } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiForm, TuiHeader, TuiInputSearch } from '@taiga-ui/layout';
import { TUI_CONFIRM, TuiInputYear, type TuiConfirmData } from '@taiga-ui/kit';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { firstValueFrom } from 'rxjs';
import { CatalogoService } from '../../../../core/services/catalogo.service';
import type {
  CatalogKind,
  CatalogNameItem,
  CatalogNameRequest,
  CicloAcademico,
} from '../../../../models/catalogos/catalogo.response';
import { catalogQueryKey } from '../../../../queries/catalog-crud.query';

interface CatalogPageConfig {
  title: string;
  description: string;
  empty: string;
  createLabel: string;
  icon: string;
}

type DialogMode = 'create' | 'edit';

type CatalogRow = CatalogNameItem | CicloAcademico;

@Component({
  selector: 'app-simple-catalog-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    TuiButton,
    TuiCardLarge,
    TuiError,
    TuiForm,
    TuiHeader,
    TuiIcon,
    TuiInput,
    TuiInputSearch,
    TuiInputYear,
    TuiTable,
    TuiTablePagination,
    TuiTextfield,
    TuiTitle,
  ],
  template: `
    <div class="card flex flex-col gap-3 p-2.5">
      <a class="text-sm font-semibold text-primary hover:underline" routerLink="/app/catalogos">
        ← Volver a catálogos
      </a>

      <div appearance="floating" tuiCardLarge>
        <header
          tuiHeader
          class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between"
        >
          <div class="flex items-center gap-4">
            <div
              class="flex size-12 items-center justify-center rounded-2xl bg-primary/10 text-primary"
              aria-hidden="true"
            >
              <tui-icon [icon]="config().icon" />
            </div>
            <div class="flex flex-col justify-center">
              <h2 tuiTitle>{{ config().title }}</h2>
              <span class="subtitle text-sm font-medium text-text-secondary">
                {{ config().description }}
              </span>
            </div>
          </div>

          @if (isCycleCatalog()) {
            <form
              class="flex items-start gap-3"
              [formGroup]="yearForm"
              tuiForm="m"
              (ngSubmit)="generateYear()"
            >
              <div class="min-w-40">
                <tui-textfield tuiTextfieldSize="m">
                  <label tuiLabel>Año académico</label>
                  <input formControlName="anio" placeholder="2026" tuiInputYear />
                </tui-textfield>
                <tui-error [error]="yearError()" />
              </div>
              <button appearance="primary" tuiButton type="submit" [disabled]="isMutating()">
                Generar ciclos
              </button>
            </form>
          } @else {
            <button appearance="primary" tuiButton type="button" (click)="openCreate()">
              {{ config().createLabel }}
            </button>
          }
        </header>
      </div>

      <div appearance="floating" tuiCardLarge>
        <section>
          <tui-textfield tuiTextfieldSize="m" iconStart="@tui.search">
            <input
              name="buscarCatalogo"
              autocomplete="off"
              placeholder="Buscar por nombre…"
              tuiInputSearch
              (input)="onSearchChange($any($event).target.value)"
            />
          </tui-textfield>
        </section>
      </div>

      <div class="overflow-hidden rounded-2xl border border-border bg-surface">
        <table
          tuiTable
          class="w-full border-separate border-spacing-0 table-auto"
          [columns]="columns"
        >
          <thead tuiThead class="bg-surface-hover">
            <tr
              tuiThGroup
              class="h-12 text-xs font-bold uppercase tracking-wider text-text-secondary"
            >
              <th *tuiHead="'id'" tuiTh class="px-6 text-left">ID</th>
              <th *tuiHead="'nombre'" tuiTh class="px-6 text-left">Nombre</th>
              <th *tuiHead="'acciones'" tuiTh class="px-6 text-right">Acciones</th>
            </tr>
          </thead>

          @if (isLoading()) {
            <tbody>
              @for (row of loadingRows; track row) {
                <tr class="border-b border-border">
                  <td class="px-6 py-4" colspan="3">Cargando catálogo…</td>
                </tr>
              }
            </tbody>
          } @else if (paginatedItems().length === 0) {
            <tbody>
              <tr>
                <td [attr.colspan]="columns.length" class="px-6 py-16">
                  <div class="flex flex-col items-center justify-center gap-4 text-center">
                    <tui-icon
                      [icon]="config().icon"
                      class="text-text-muted"
                      [style.fontSize]="'3rem'"
                      aria-hidden="true"
                    />
                    <p class="text-text-secondary">
                      {{ search() ? noResultsText() : config().empty }}
                    </p>
                    @if (!isCycleCatalog()) {
                      <button appearance="secondary" tuiButton type="button" (click)="openCreate()">
                        Crear primer registro
                      </button>
                    }
                  </div>
                </td>
              </tr>
            </tbody>
          } @else {
            <tbody tuiTbody [data]="paginatedItems()">
              @for (item of paginatedItems(); track itemId(item)) {
                <tr tuiTr class="group hover:bg-surface-hover focus-within:bg-primary/[0.03]">
                  <td
                    *tuiCell="'id'"
                    tuiTd
                    class="border-b border-border px-6 py-4 text-sm font-medium text-text-secondary"
                  >
                    {{ itemId(item) }}
                  </td>
                  <td *tuiCell="'nombre'" tuiTd class="border-b border-border px-6 py-4">
                    <div class="flex items-center gap-3">
                      <span
                        class="flex size-10 items-center justify-center rounded-2xl bg-primary/10 text-primary ring-1 ring-primary/15 transition-colors group-hover:bg-primary group-hover:text-white group-focus-within:bg-primary group-focus-within:text-white"
                        aria-hidden="true"
                      >
                        <tui-icon [icon]="config().icon" />
                      </span>
                      <span class="text-base font-bold text-text-primary">{{
                        itemName(item)
                      }}</span>
                    </div>
                  </td>
                  <td
                    *tuiCell="'acciones'"
                    tuiTd
                    class="border-b border-border px-6 py-4 text-right"
                  >
                    @if (!isCycleCatalog()) {
                      <div class="inline-flex items-center gap-1">
                        <button
                          appearance="action"
                          iconStart="@tui.pencil"
                          size="xs"
                          tuiIconButton
                          type="button"
                          class="text-text-secondary hover:bg-primary/10 hover:text-primary focus-visible:bg-primary/10 focus-visible:text-primary focus-visible:ring-2 focus-visible:ring-primary/30"
                          aria-label="Editar registro"
                          (click)="openEdit(item)"
                        ></button>
                        <button
                          appearance="action"
                          iconStart="@tui.trash-2"
                          size="xs"
                          tuiIconButton
                          type="button"
                          class="text-text-secondary hover:bg-red-50 hover:text-red-700 focus-visible:bg-red-50 focus-visible:text-red-700 focus-visible:ring-2 focus-visible:ring-red-200"
                          aria-label="Eliminar registro"
                          (click)="deleteItem(item)"
                        ></button>
                      </div>
                    } @else {
                      <span class="text-sm text-text-secondary">Gestionado por año</span>
                    }
                  </td>
                </tr>
              }
            </tbody>
          }
        </table>
      </div>

      @if (filteredItems().length > 0) {
        <div appearance="floating" tuiCardLarge>
          <tui-table-pagination
            [page]="page()"
            [size]="pageSize()"
            [total]="filteredItems().length"
            (pageChange)="setPage($event)"
            (sizeChange)="setPageSize($event)"
          />
        </div>
      }

      @if (dialogOpen()) {
        <div
          class="fixed inset-0 z-50 grid place-items-center bg-black/40 p-4"
          role="dialog"
          aria-modal="true"
          [attr.aria-labelledby]="'dialog-title-' + kind()"
        >
          <form
            class="w-full max-w-md rounded-3xl bg-surface p-6 shadow-xl"
            [formGroup]="form"
            tuiForm="m"
            (ngSubmit)="saveItem()"
          >
            <header class="mb-5">
              <h3 [id]="'dialog-title-' + kind()" class="text-xl font-bold text-text-primary">
                {{ dialogTitle() }}
              </h3>
              <p class="text-sm text-text-secondary">Usá un nombre claro y único.</p>
            </header>

            <tui-textfield tuiTextfieldSize="m">
              <label tuiLabel>Nombre</label>
              <input
                name="nombre"
                autocomplete="off"
                formControlName="nombre"
                placeholder="Nombre del catálogo…"
                tuiInput
              />
            </tui-textfield>
            <tui-error [error]="nameError()" />

            <footer class="mt-6 flex justify-end gap-2">
              <button appearance="secondary" tuiButton type="button" (click)="closeDialog()">
                Cancelar
              </button>
              <button appearance="primary" tuiButton type="submit" [disabled]="isMutating()">
                {{ dialogMode() === 'create' ? 'Crear' : 'Guardar' }}
              </button>
            </footer>
          </form>
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SimpleCatalogPage implements OnInit {
  private readonly catalogos = inject(CatalogoService);
  private readonly dialogs = inject(TuiDialogService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);
  private readonly queryClient = inject(QueryClient);

  readonly kind = input.required<CatalogKind>();
  readonly columns = ['id', 'nombre', 'acciones'] as const;
  readonly loadingRows = [1, 2, 3];

  readonly items = signal<CatalogRow[]>([]);
  readonly isLoading = signal(false);
  readonly search = signal('');
  readonly page = signal(0);
  readonly pageSize = signal(10);
  readonly dialogOpen = signal(false);
  readonly dialogMode = signal<DialogMode>('create');
  readonly selectedItem = signal<CatalogRow | null>(null);
  readonly isMutating = signal(false);

  readonly form = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
    ]),
  });

  readonly yearForm = this.formBuilder.group({
    anio: this.formBuilder.nonNullable.control(new Date().getFullYear(), [
      Validators.required,
      Validators.min(2000),
      Validators.max(2100),
    ]),
  });

  readonly config = computed(() => getCatalogConfig(this.kind()));

  readonly filteredItems = computed(() => {
    const term = this.search().trim().toLowerCase();
    const items = this.items();

    if (!term) {
      return items;
    }

    return items.filter((item) => this.itemName(item).toLowerCase().includes(term));
  });

  readonly paginatedItems = computed(() => {
    const start = this.page() * this.pageSize();
    return this.filteredItems().slice(start, start + this.pageSize());
  });

  ngOnInit(): void {
    void this.loadItems();
  }

  isCycleCatalog(): boolean {
    return this.kind() === 'ciclos';
  }

  onSearchChange(value: string): void {
    this.search.set(value);
    this.page.set(0);
  }

  setPage(page: number): void {
    this.page.set(page);
  }

  setPageSize(size: number): void {
    this.pageSize.set(size);
    this.page.set(0);
  }

  itemId(item: CatalogRow): number {
    if ('idCarrera' in item) return item.idCarrera;
    if ('idGrado' in item) return item.idGrado;
    if ('idEspecializacion' in item) return item.idEspecializacion;
    return item.idCiclo;
  }

  itemName(item: CatalogRow): string {
    return item.nombre;
  }

  noResultsText(): string {
    return `No hay resultados para "${this.search()}"`;
  }

  dialogTitle(): string {
    return this.dialogMode() === 'create'
      ? this.config().createLabel
      : `Editar ${this.config().title}`;
  }

  nameError(): string | null {
    const control = this.form.controls.nombre;
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'El nombre es obligatorio';
    if (control.errors['minlength']) return 'Mínimo 2 caracteres';
    return 'Valor inválido';
  }

  yearError(): string | null {
    const control = this.yearForm.controls.anio;
    if (!control.touched || !control.errors) return null;
    if (control.errors['required']) return 'El año es obligatorio';
    if (control.errors['min'] || control.errors['max']) return 'Usá un año entre 2000 y 2100';
    return 'Valor inválido';
  }

  openCreate(): void {
    this.dialogMode.set('create');
    this.selectedItem.set(null);
    this.form.reset({ nombre: '' });
    this.dialogOpen.set(true);
  }

  openEdit(item: CatalogRow): void {
    if (this.isCycleCatalog()) return;
    this.dialogMode.set('edit');
    this.selectedItem.set(item);
    this.form.reset({ nombre: this.itemName(item) });
    this.dialogOpen.set(true);
  }

  closeDialog(): void {
    this.dialogOpen.set(false);
    this.selectedItem.set(null);
    this.form.markAsPristine();
    this.form.markAsUntouched();
  }

  async saveItem(): Promise<void> {
    if (this.form.invalid || this.isCycleCatalog()) {
      this.form.markAllAsTouched();
      return;
    }

    const request: CatalogNameRequest = { nombre: this.form.controls.nombre.value.trim() };
    const kind = this.editableKind();
    this.isMutating.set(true);

    try {
      if (this.dialogMode() === 'create') {
        await this.catalogos.createCatalogItem(kind, request);
        this.notifySuccess('Registro creado exitosamente');
      } else {
        const selected = this.selectedItem();
        if (!selected) return;
        await this.catalogos.updateCatalogItem(kind, this.itemId(selected), request);
        this.notifySuccess('Registro actualizado exitosamente');
      }

      await this.refreshCatalogItems();
      this.closeDialog();
    } catch (error) {
      this.notifyError(error, 'No se pudo guardar el registro');
    } finally {
      this.isMutating.set(false);
    }
  }

  async deleteItem(item: CatalogRow): Promise<void> {
    if (this.isCycleCatalog()) return;

    const confirmed = await this.confirm('Eliminar registro', {
      content: `¿Eliminar ${this.itemName(item)}? Esta acción no se puede deshacer.`,
      yes: 'Eliminar',
      no: 'Cancelar',
      appearance: 'accent',
    });

    if (!confirmed) return;

    this.isMutating.set(true);
    try {
      await this.catalogos.deleteCatalogItem(this.editableKind(), this.itemId(item));
      this.notifySuccess('Registro eliminado exitosamente');
      await this.refreshCatalogItems();
    } catch (error) {
      this.notifyError(error, 'No se pudo eliminar el registro');
    } finally {
      this.isMutating.set(false);
    }
  }

  async generateYear(): Promise<void> {
    if (this.yearForm.invalid) {
      this.yearForm.markAllAsTouched();
      return;
    }

    const anio = this.yearForm.controls.anio.value;
    const confirmed = await this.confirm('Generar ciclos académicos', {
      content: `Se crearán los ciclos ${anio}-I y ${anio}-II si todavía no existen.`,
      yes: 'Generar',
      no: 'Cancelar',
      appearance: 'primary',
    });

    if (!confirmed) return;

    this.isMutating.set(true);
    try {
      await this.catalogos.generarCiclosAcademicos({ anio });
      this.notifySuccess('Ciclos académicos generados');
      await this.refreshCatalogItems();
    } catch (error) {
      this.notifyError(error, 'No se pudieron generar los ciclos');
    } finally {
      this.isMutating.set(false);
    }
  }

  private editableKind(): Exclude<CatalogKind, 'ciclos'> {
    const kind = this.kind();
    if (kind === 'ciclos') {
      throw new Error('Ciclos catalog does not support item mutations');
    }

    return kind;
  }

  private async loadItems(): Promise<void> {
    this.isLoading.set(true);
    try {
      const items = await this.catalogos.getCatalogItems(this.kind());
      this.items.set(items);
    } catch (error) {
      this.notifyError(error, 'No se pudo cargar el catálogo');
    } finally {
      this.isLoading.set(false);
    }
  }

  private async refreshCatalogItems(): Promise<void> {
    await this.queryClient.invalidateQueries({ queryKey: catalogQueryKey(this.kind()) });
    await this.loadItems();
  }

  private async confirm(label: string, data: TuiConfirmData): Promise<boolean> {
    try {
      return await firstValueFrom(
        this.dialogs.open<boolean>(TUI_CONFIRM, {
          label,
          size: 's',
          data,
        }),
      );
    } catch {
      return false;
    }
  }

  private notifySuccess(message: string): void {
    this.notifications
      .open(message, { label: 'Éxito', appearance: 'success', autoClose: 3000 })
      .subscribe();
  }

  private notifyError(error: unknown, fallback: string): void {
    const message = error instanceof Error ? error.message : fallback;
    this.notifications
      .open(message, { label: 'Error', appearance: 'error', autoClose: 5000 })
      .subscribe();
  }
}

function getCatalogConfig(kind: CatalogKind): CatalogPageConfig {
  switch (kind) {
    case 'carreras':
      return {
        title: 'Carreras',
        description: 'Gestiona las carreras disponibles para estudiantes.',
        empty: 'No hay carreras registradas',
        createLabel: 'Nueva carrera',
        icon: '@tui.book-open',
      };
    case 'grados-academicos':
      return {
        title: 'Grados académicos',
        description: 'Administra los grados usados por el formulario docente.',
        empty: 'No hay grados académicos registrados',
        createLabel: 'Nuevo grado',
        icon: '@tui.graduation-cap',
      };
    case 'especializaciones':
      return {
        title: 'Especializaciones',
        description: 'Administra el catálogo de especializaciones docentes.',
        empty: 'No hay especializaciones registradas',
        createLabel: 'Nueva especialización',
        icon: '@tui.tags',
      };
    case 'ciclos':
      return {
        title: 'Ciclos académicos',
        description: 'Consulta ciclos existentes y genera periodos por año.',
        empty: 'No hay ciclos académicos registrados',
        createLabel: 'Generar ciclos',
        icon: '@tui.calendar-days',
      };
  }
}
