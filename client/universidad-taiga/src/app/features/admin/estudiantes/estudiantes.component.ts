import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TuiPlatform } from '@taiga-ui/cdk';
import { FormBuilder, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDialog,
  TuiDropdown,
  TuiIcon,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiSegmented, TuiSkeleton } from '@taiga-ui/kit';
import { CardEstudiante } from './card-estudiante/card-estudiante.component';
import { EstudianteForm } from './estudiante-form/estudiante-form.component';
import { EstudianteDeleteDialog } from './estudiante-delete-dialog/estudiante-delete-dialog.component';
import { EstudianteResponse } from '../../../models/estudiante/estudiante.response';
import {
  useActualizarEstudianteMutation,
  useCrearEstudianteMutation,
  useEliminarEstudianteMutation,
  useEstudiantesPaginadosQuery,
} from '../../../queries/estudiante.query';
import { useCarrerasQuery, useTiposDocumentoQuery } from '../../../queries/catalogo.query';
import type { Carrera, TipoDocumento } from '../../../models/catalogos/catalogo.response';
import { EstudianteCreateRequest } from '../../../models/estudiante/estudiante.request';
import { getIniciales, getEstadoEstudiante } from '../../../shared/utils/estudiante.util';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-estudiantes',
  imports: [
  TuiButton,
  TuiCardLarge,
  TuiDialog,
  TuiDropdown,
  TuiHeader,
  TuiInput,
  TuiPlatform,
  TuiTextfield,
    TuiTable,
    TuiTitle,
    TuiSegmented,
    TuiIcon,
    TuiSkeleton,
    CardEstudiante,
    EstudianteForm,
    EstudianteDeleteDialog,
  ],
  templateUrl: './estudiantes.html',
  styleUrl: './estudiantes.less',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de estudiantes.
 * Lista, crea, edita y elimina estudiantes con modal de formulario y confirmación.
 */
export class Estudiantes {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);

  /** Columnas de la tabla de estudiantes. */
  readonly columns = ['estudiante', 'codigo', 'carrera', 'ciclo', 'estado', 'acciones'] as const;

  /** Modo de visualización: grilla de tarjetas o filas de tabla. */
  readonly viewMode = signal<'grid' | 'row'>('grid');

  // ─── Paginación y búsqueda ───────────────────────────────────────────

  /** Página actual (0-based). */
  readonly pagina = signal(0);
  /** Elementos por página. */
  readonly tamaño = signal(10);
  /** Texto de búsqueda actual. */
  readonly busqueda = signal('');
  /** Timer para debounce del search. */
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  /** Query paginada que se refresca al cambiar página, tamaño o búsqueda. */
  readonly estudiantesQuery = useEstudiantesPaginadosQuery(
    this.pagina,
    this.tamaño,
    this.busqueda,
  );

  /** Total de elementos (de la respuesta paginada). */
  readonly totalEstudiantes = computed(() => this.estudiantesQuery.data()?.totalElements ?? 0);

  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.estudiantesQuery.data()?.totalPages ?? 0);

  /** Controla la apertura/cierre del modal de formulario (crear/editar). */
  readonly estudianteModalAbierto = signal(false);

  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);

  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');

  /** Estudiante seleccionado para edición o eliminación. */
  readonly estudianteSeleccionado = signal<EstudianteResponse | null>(null);

  /** Indica si la consulta de estudiantes está cargando. */
  readonly isLoading = computed(() => this.estudiantesQuery.isPending());

  readonly estudianteForm = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    apellido: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    email: this.formBuilder.nonNullable.control('', [Validators.required, Validators.email]),
    ciclo: this.formBuilder.nonNullable.control(1, [Validators.required, Validators.min(1)]),
    numeroDocumento: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(8)]),
    tipoDocumento: this.formBuilder.control<TipoDocumento | null>(null, Validators.required),
    carrera: this.formBuilder.control<Carrera | null>(null, Validators.required),
  });

  /** Query de tipos de documento. */
  readonly tiposDocumentoQuery = useTiposDocumentoQuery();
  /** Query de carreras. */
  readonly carrerasQuery = useCarrerasQuery();
  /** Mutación para crear un nuevo estudiante. */
  readonly crearEstudianteMutation = useCrearEstudianteMutation();
  /** Mutación para actualizar un estudiante existente. */
  readonly actualizarEstudianteMutation = useActualizarEstudianteMutation();
  /** Mutación para eliminar un estudiante. */
  readonly eliminarEstudianteMutation = useEliminarEstudianteMutation();

  /**
   * Cambia el modo de visualización entre grilla y tabla.
   *
   * @param index 0 para grilla, 1 para tabla.
   */
  setViewMode(index: number): void {
    this.viewMode.set(index === 0 ? 'grid' : 'row');
  }

  /** Abre modal de creación con formulario limpio. */
  openNuevoEstudianteModal(): void {
    this.modoFormulario.set('crear');
    this.estudianteSeleccionado.set(null);
    this.resetFormulario();
    this.estudianteModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo del estudiante. */
  openEditarEstudianteModal(estudiante: EstudianteResponse): void {
    this.modoFormulario.set('editar');
    this.estudianteSeleccionado.set(estudiante);
    this.cargarFormularioDesdeEstudiante(estudiante);
    this.estudianteModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeEstudianteModal(): void {
    this.estudianteModalAbierto.set(false);
    this.estudianteSeleccionado.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarEstudianteModal(estudiante: EstudianteResponse): void {
    this.estudianteSeleccionado.set(estudiante);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.estudianteSeleccionado.set(null);
  }

  /**
   * Guarda estudiante según modo actual (crear o editar).
   * Marca controles como tocados cuando formulario es inválido.
   */
  guardarEstudiante(observer: DialogObserver): void {
    if (this.estudianteForm.invalid) {
      this.estudianteForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayloadFormulario();

    if (!payload) {
      return;
    }

    if (this.modoFormulario() === 'crear') {
      this.crearEstudianteMutation.mutate(payload, {
        onSuccess: () => {
          this.notifications
            .open('Estudiante creado exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeEstudianteModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al crear estudiante', {
              label: 'Error',
              appearance: 'error',
              autoClose: 5000,
            })
            .subscribe();
        },
      });

      return;
    }

    const seleccionado = this.estudianteSeleccionado();

    if (!seleccionado) {
      return;
    }

    this.actualizarEstudianteMutation.mutate(
      { id: seleccionado.idUsuario, estudiante: payload },
      {
        onSuccess: () => {
          this.notifications
            .open('Estudiante actualizado exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeEstudianteModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar estudiante', {
              label: 'Error',
              appearance: 'error',
              autoClose: 5000,
            })
            .subscribe();
        },
      },
    );
  }

  /** Ejecuta eliminación confirmada del estudiante seleccionado. */
  confirmarEliminarEstudiante(observer: DialogObserver): void {
    const seleccionado = this.estudianteSeleccionado();

    if (!seleccionado) {
      return;
    }

    this.eliminarEstudianteMutation.mutate(seleccionado.idUsuario, {
      onSuccess: () => {
        this.notifications
          .open('Estudiante eliminado exitosamente', {
            label: 'Eliminado',
            appearance: 'success',
            autoClose: 3000,
          })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar estudiante', {
            label: 'Error',
            appearance: 'error',
            autoClose: 5000,
          })
          .subscribe();
      },
    });
  }

  /**
   * Indica si hay una operación de guardado en curso.
   *
   * @returns true si alguna mutación de creación o actualización está pendiente.
   */
  isGuardandoEstudiante(): boolean {
    return this.crearEstudianteMutation.isPending() || this.actualizarEstudianteMutation.isPending();
  }

  /**
   * Indica si hay una operación de eliminación en curso.
   *
   * @returns true si la mutación de eliminación está pendiente.
   */
  isEliminandoEstudiante(): boolean {
    return this.eliminarEstudianteMutation.isPending();
  }

  /**
   * Navega al historial académico del estudiante.
   * Reservado para implementación futura.
   */
  onVerHistorial(_estudiante: EstudianteResponse): void {
    // Reservado para flujo de historial académico.
  }

  /**
   * Obtiene las iniciales a partir de un nombre completo.
   * Delega a la utility compartida.
   */
  getIniciales(nombre: string): string {
    return getIniciales(nombre);
  }

  /**
   * Obtiene el estado formateado del estudiante con clases CSS asociadas.
   * Delega a la utility compartida.
   */
  getEstado(estudiante: EstudianteResponse): { label: string; classes: string; dotClasses: string } {
    return getEstadoEstudiante(estudiante.estadoAcademico);
  }

  /**
   * Maneja el cambio en el input de búsqueda con debounce de 300ms.
   * Al escribir, vuelve a la página 0 para mostrar resultados desde el inicio.
   */
  onBusquedaChange(texto: string): void {
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => {
      this.busqueda.set(texto.trim());
      this.pagina.set(0);
    }, 300);
  }

  /** Navega a la página anterior si no es la primera. */
  paginaAnterior(): void {
    if (this.pagina() > 0) {
      this.pagina.update(p => p - 1);
    }
  }

  /** Navega a la página siguiente si no es la última. */
  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) {
      this.pagina.update(p => p + 1);
    }
  }

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);

  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.estudiantesQuery.data();
    if (!data || data.totalElements === 0) return '0 estudiantes';
    const desde = data.number * data.size + 1;
    const hasta = Math.min((data.number + 1) * data.size, data.totalElements);
    return `${desde}–${hasta} de ${data.totalElements}`;
  });

  /** Array de números de página para iterar en el template. */
  readonly paginasArray = computed(() =>
    Array.from({ length: this.totalPaginas() }, (_, i) => i),
  );

  protected readonly String = String;

  private resetFormulario(): void {
    this.estudianteForm.reset({
      nombre: '',
      apellido: '',
      email: '',
      ciclo: 1,
      numeroDocumento: '',
      tipoDocumento: null,
      carrera: null,
    });
    this.estudianteForm.markAsPristine();
    this.estudianteForm.markAsUntouched();
  }

  private cargarFormularioDesdeEstudiante(estudiante: EstudianteResponse): void {
    const tipoDocumento = this.buscarTipoDocumentoPorCodigoONombre(estudiante);
    const carrera = (this.carrerasQuery.data() ?? []).find((item) => item.idCarrera === estudiante.idCarrera) ?? null;

    this.estudianteForm.reset({
      nombre: estudiante.nombre,
      apellido: estudiante.apellido,
      email: estudiante.emailPersonal,
      ciclo: estudiante.ciclo,
      numeroDocumento: estudiante.numeroDocumento,
      tipoDocumento,
      carrera,
    });
    this.estudianteForm.markAsPristine();
    this.estudianteForm.markAsUntouched();
  }

  private buscarTipoDocumentoPorCodigoONombre(estudiante: EstudianteResponse): TipoDocumento | null {
    const tipos = this.tiposDocumentoQuery.data() ?? [];
    const tipoPorCodigo =
      typeof estudiante.idTipoDocumento === 'number'
        ? tipos.find((item) => item.idTipoDocumento === estudiante.idTipoDocumento)
        : undefined;

    if (tipoPorCodigo) {
      return tipoPorCodigo;
    }

    const tipoNormalizado = estudiante.tipoDocumento.trim().toLowerCase();
    return tipos.find((item) => item.nombre.trim().toLowerCase() === tipoNormalizado) ?? null;
  }

  private construirPayloadFormulario(): EstudianteCreateRequest | null {
    const value = this.estudianteForm.getRawValue();
    const carrera = value.carrera;
    const tipoDocumento = value.tipoDocumento;

    if (!carrera || !tipoDocumento) {
      return null;
    }

    return {
      nombre: value.nombre.trim(),
      apellido: value.apellido.trim(),
      emailPersonal: value.email.trim(),
      numeroDocumento: value.numeroDocumento.trim(),
      idTipoDocumento: tipoDocumento.idTipoDocumento,
      ciclo: Number(value.ciclo),
      idCarrera: carrera.idCarrera,
    };
  }
}
