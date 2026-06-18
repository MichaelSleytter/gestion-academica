import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
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
import { TuiCardLarge, TuiHeader, TuiSearch } from '@taiga-ui/layout';
import { TuiSegmented, TuiSkeleton } from '@taiga-ui/kit';
import { CardEstudiante } from './card-estudiante/card-estudiante.component';
import { EstudianteForm } from './estudiante-form/estudiante-form.component';
import { EstudianteDeleteDialog } from './estudiante-delete-dialog/estudiante-delete-dialog.component';
import { EstudianteResponse } from '../../../models/estudiante/estudiante.response';
import {
  useActualizarEstudianteMutation,
  useCrearEstudianteMutation,
  useEliminarEstudianteMutation,
  useEstudiantesQuery,
} from '../../../queries/estudiante.query';
import { TuiBlockStatus } from '@taiga-ui/layout';
import { useCarrerasQuery, useTiposDocumentoQuery } from '../../../queries/catalogo.query';
import type { Carrera, TipoDocumento } from '../../../models/catalogos/catalogo.response';
import { EstudianteCreateRequest } from '../../../models/estudiante/estudiante.request';

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
    TuiSearch,
    TuiSegmented,
    TuiIcon,
    TuiSkeleton,
    CardEstudiante,
    EstudianteForm,
    EstudianteDeleteDialog,
    TuiBlockStatus,
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

  /** Query de estudiantes gestionada por TanStack Query. */
  readonly estudiantesQuery = useEstudiantesQuery();
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
   *
   * @param nombre Nombre completo del estudiante.
   * @returns Máximo 2 caracteres en mayúscula.
   */
  getIniciales(nombre: string): string {
    const partes = (nombre ?? '').trim().split(/\s+/).filter(Boolean);

    if (partes.length === 0) {
      return 'NA';
    }

    if (partes.length === 1) {
      return partes[0].slice(0, 2).toUpperCase();
    }

    return `${partes[0][0]}${partes[1][0]}`.toUpperCase();
  }

  /**
   * Extrae solo los dígitos de un ciclo académico.
   *
   * @param ciclo Representación del ciclo (ej: "5to Ciclo").
   * @returns Solo los dígitos, o el valor original si no hay dígitos.
   */
  getCicloNumero(ciclo: string): string {
    const soloDigitos = ciclo.replace(/\D+/g, '');

    return soloDigitos || ciclo;
  }

  /**
   * Obtiene el estado formateado del estudiante con clases CSS asociadas.
   *
   * @param estudiante Datos del estudiante.
   * @returns Objeto con label, clases de texto/fondo y clases del indicador.
   */
  getEstado(estudiante: EstudianteResponse): { label: string; classes: string; dotClasses: string } {
    const estado = (estudiante.estadoAcademico ?? 'INACTIVO').toUpperCase();

    if (estado === 'ACTIVO') {
      return {
        label: 'REGULAR',
        classes: 'bg-success-bg text-success',
        dotClasses: 'bg-success',
      };
    }

    if (estado === 'SUSPENDIDO') {
      return {
        label: 'SUSPENDIDO',
        classes: 'bg-warning-bg text-warning',
        dotClasses: 'bg-warning',
      };
    }

    return {
      label: 'INACTIVO',
      classes: 'bg-danger-bg text-danger',
      dotClasses: 'bg-danger',
    };
  }

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
