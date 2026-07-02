import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { TuiNotificationService } from '@taiga-ui/core';
import {
  TuiButton,
  TuiDialog,
  TuiDropdown,
  TuiIcon,
  TuiInput,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiPlatform } from '@taiga-ui/cdk';
import { FormBuilder, FormsModule, Validators } from '@angular/forms';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiSegmented, TuiSkeleton } from '@taiga-ui/kit';
import { DocenteResponse } from '../../../models/docente/docente.response';
import { DocenteCreateRequest } from '../../../models/docente/docente.request';
import {
  useDocentesPaginadosQuery,
  useCrearDocenteMutation,
  useActualizarDocenteMutation,
  useEliminarDocenteMutation,
} from '../../../queries/docente.query';
import { useGradosAcademicosQuery, useTiposDocumentoQuery } from '../../../queries/catalogo.query';
import type { GradoAcademico, TipoDocumento } from '../../../models/catalogos/catalogo.response';
import { getIniciales } from '../../../shared/utils/estudiante.util';
import { CardDocente } from './card-docente/card-docente.component';
import { DocenteForm } from './docente-form/docente-form.component';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-docentes',
  imports: [
    FormsModule,
    TuiButton,
    TuiCardLarge,
    TuiDialog,
    TuiDropdown,
    TuiHeader,
    TuiIcon,
    TuiInput,
    TuiPlatform,
    TuiSegmented,
    TuiSkeleton,
    TuiTable,
    TuiTextfield,
    TuiTitle,
    CardDocente,
    DocenteForm,
  ],
  templateUrl: './docentes.html',
  styleUrl: './docentes.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de docentes.
 * Lista, crea, edita y elimina docentes con modal de formulario y confirmación.
 */
export class Docentes {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);

  /** Columnas de la tabla. */
  readonly columns = ['nombre', 'documento', 'especialidad', 'grado', 'estado', 'acciones'] as const;

  /** Modo de visualización. */
  readonly viewMode = signal<'grid' | 'row'>('row');

  // ─── Paginación y búsqueda ───────────────────────────────────────────

  readonly pagina = signal(0);
  readonly tamaño = signal(10);
  readonly busqueda = signal('');
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  readonly docentesQuery = useDocentesPaginadosQuery(this.pagina, this.tamaño, this.busqueda);
  readonly totalDocentes = computed(() => this.docentesQuery.data()?.totalElements ?? 0);
  readonly totalPaginas = computed(() => this.docentesQuery.data()?.totalPages ?? 0);
  readonly isLoading = computed(() => this.docentesQuery.isPending());

  // ─── Formulario ───────────────────────────────────────────────────────

  readonly docenteForm = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    apellido: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    email: this.formBuilder.nonNullable.control('', [Validators.required, Validators.email]),
    password: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(8)]),
    numeroDocumento: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(8)]),
    tipoDocumento: this.formBuilder.control<TipoDocumento | null>(null, Validators.required),
    especialidad: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(3)]),
    grado: this.formBuilder.control<GradoAcademico | null>(null, Validators.required),
  });

  readonly docenteModalAbierto = signal(false);
  readonly eliminarModalAbierto = signal(false);
  readonly modoFormulario = signal<ModoFormulario>('crear');
  readonly docenteSeleccionado = signal<DocenteResponse | null>(null);

  readonly tiposDocumentoQuery = useTiposDocumentoQuery();
  readonly gradosQuery = useGradosAcademicosQuery();
  readonly crearDocenteMutation = useCrearDocenteMutation();
  readonly actualizarDocenteMutation = useActualizarDocenteMutation();
  readonly eliminarDocenteMutation = useEliminarDocenteMutation();

  // ─── View mode ───────────────────────────────────────────────────────

  setViewMode(index: number): void {
    this.viewMode.set(index === 0 ? 'grid' : 'row');
  }

  // ─── Búsqueda ────────────────────────────────────────────────────────

  onBusquedaChange(texto: string): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => {
      this.busqueda.set(texto.trim());
      this.pagina.set(0);
    }, 300);
  }

  // ─── Paginación ──────────────────────────────────────────────────────

  paginaAnterior(): void {
    if (this.pagina() > 0) this.pagina.update(p => p - 1);
  }

  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) this.pagina.update(p => p + 1);
  }

  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  readonly paginasArray = computed(() =>
    Array.from({ length: this.totalPaginas() }, (_, i) => i),
  );

  readonly infoPaginacion = computed(() => {
    const data = this.docentesQuery.data();
    if (!data || data.totalElements === 0) return '0 docentes';
    const desde = data.number * data.size + 1;
    const hasta = Math.min((data.number + 1) * data.size, data.totalElements);
    return `${desde}–${hasta} de ${data.totalElements}`;
  });

  // ─── Iniciales ───────────────────────────────────────────────────────

  getIniciales(nombre: string): string {
    return getIniciales(nombre);
  }

  // ─── Estado ──────────────────────────────────────────────────────────

  getEstado(docente: DocenteResponse): { label: string; classes: string; dotClasses: string } {
    return docente.estado
      ? { label: 'ACTIVO', classes: 'bg-success-bg text-success', dotClasses: 'bg-success' }
      : { label: 'INACTIVO', classes: 'bg-danger-bg text-danger', dotClasses: 'bg-danger' };
  }

  // ─── Modal formulario ────────────────────────────────────────────────

  openNuevoDocenteModal(): void {
    this.modoFormulario.set('crear');
    this.docenteSeleccionado.set(null);
    this.docenteForm.controls.password.setValidators([Validators.required, Validators.minLength(8)]);
    this.docenteForm.controls.password.updateValueAndValidity();
    this.resetFormulario();
    this.docenteModalAbierto.set(true);
  }

  openEditarDocenteModal(docente: DocenteResponse): void {
    this.modoFormulario.set('editar');
    this.docenteSeleccionado.set(docente);
    this.docenteForm.controls.password.setValidators([Validators.minLength(8)]);
    this.docenteForm.controls.password.updateValueAndValidity();
    this.cargarFormularioDesdeDocente(docente);
    this.docenteModalAbierto.set(true);
  }

  closeDocenteModal(): void {
    this.docenteModalAbierto.set(false);
    this.docenteSeleccionado.set(null);
    this.resetFormulario();
  }

  guardarDocente(observer: DialogObserver): void {
    if (this.docenteForm.invalid) {
      this.docenteForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();
    if (!payload) return;

    if (this.modoFormulario() === 'crear') {
      this.crearDocenteMutation.mutate(payload, {
        onSuccess: () => {
          this.notifications
            .open('Docente creado exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
          observer.complete();
          this.closeDocenteModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al crear docente', { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      });
      return;
    }

    const seleccionado = this.docenteSeleccionado();
    if (!seleccionado) return;

    this.actualizarDocenteMutation.mutate(
      { id: seleccionado.idUsuario, docente: payload },
      {
        onSuccess: () => {
          this.notifications
            .open('Docente actualizado exitosamente', { label: 'Éxito', appearance: 'success', autoClose: 3000 })
            .subscribe();
          observer.complete();
          this.closeDocenteModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar docente', { label: 'Error', appearance: 'error', autoClose: 5000 })
            .subscribe();
        },
      },
    );
  }

  isGuardandoDocente(): boolean {
    return this.crearDocenteMutation.isPending() || this.actualizarDocenteMutation.isPending();
  }

  // ─── Eliminación ────────────────────────────────────────────────────

  openEliminarModal(docente: DocenteResponse): void {
    this.docenteSeleccionado.set(docente);
    this.eliminarModalAbierto.set(true);
  }

  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.docenteSeleccionado.set(null);
  }

  confirmarEliminar(observer: DialogObserver): void {
    const seleccionado = this.docenteSeleccionado();
    if (!seleccionado) return;

    this.eliminarDocenteMutation.mutate(seleccionado.idUsuario, {
      onSuccess: () => {
        this.notifications
          .open('Docente eliminado exitosamente', { label: 'Eliminado', appearance: 'success', autoClose: 3000 })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar docente', { label: 'Error', appearance: 'error', autoClose: 5000 })
          .subscribe();
      },
    });
  }

  isEliminando(): boolean {
    return this.eliminarDocenteMutation.isPending();
  }

  // ─── Privados ────────────────────────────────────────────────────────

  private resetFormulario(): void {
    this.docenteForm.reset({
      nombre: '',
      apellido: '',
      email: '',
      password: '',
      numeroDocumento: '',
      tipoDocumento: null,
      especialidad: '',
      grado: null,
    });
    this.docenteForm.markAsPristine();
    this.docenteForm.markAsUntouched();
  }

  private cargarFormularioDesdeDocente(docente: DocenteResponse): void {
    const tipoDocumento = this.buscarTipoDocumento(docente.tipoDocumento);
    const grado = (this.gradosQuery.data() ?? []).find((g) => g.idGrado === docente.idGrado) ?? null;
    const nombreCompleto = (docente.nombre ?? '').split(' ');
    const apellidoCompleto = (docente.apellido ?? '').split(' ');

    this.docenteForm.reset({
      nombre: nombreCompleto[0] ?? '',
      apellido: apellidoCompleto[0] ?? '',
      email: docente.email,
      password: '',
      numeroDocumento: docente.numeroDocumento,
      tipoDocumento,
      especialidad: docente.especialidad,
      grado,
    });
    this.docenteForm.markAsPristine();
    this.docenteForm.markAsUntouched();
  }

  private buscarTipoDocumento(nombre: string): TipoDocumento | null {
    const normalizado = nombre.trim().toLowerCase();
    return (this.tiposDocumentoQuery.data() ?? []).find(
      (t) => t.nombre.trim().toLowerCase() === normalizado,
    ) ?? null;
  }

  private construirPayload(): DocenteCreateRequest | null {
    const value = this.docenteForm.getRawValue();
    const tipoDocumento = value.tipoDocumento;
    const grado = value.grado;

    if (!tipoDocumento || !grado) return null;

    return {
      nombre: value.nombre.trim(),
      apellido: value.apellido.trim(),
      email: value.email.trim(),
      password: value.password.trim(),
      numeroDocumento: value.numeroDocumento.trim(),
      idTipoDocumento: tipoDocumento.idTipoDocumento,
      especialidad: value.especialidad.trim(),
      idGrado: grado.idGrado,
    };
  }
}
