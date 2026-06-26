import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  TuiButton,
  TuiDialog,
  TuiIcon,
  TuiInput,
  TuiNotificationService,
  TuiTextfield,
  TuiTitle,
} from '@taiga-ui/core';
import { TuiPlatform } from '@taiga-ui/cdk';
import { TuiTable } from '@taiga-ui/addon-table';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { TuiSkeleton } from '@taiga-ui/kit';
import { CursoResponse } from '../../../models/curso/curso.response';
import { CursoCreateRequest } from '../../../models/curso/curso.request';
import {
  useCursosPaginadosQuery,
  useCrearCursoMutation,
  useActualizarCursoMutation,
  useEliminarCursoMutation,
} from '../../../queries/curso.query';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-cursos',
  imports: [
    ReactiveFormsModule,
    TuiButton,
    TuiCardLarge,
    TuiDialog,
    TuiHeader,
    TuiInput,
    TuiPlatform,
    TuiTextfield,
    TuiTable,
    TuiTitle,
    TuiIcon,
    TuiSkeleton,
  ],
  templateUrl: './cursos.html',
  styleUrl: './cursos.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de cursos académicos.
 * Lista, crea, edita y elimina cursos con modal de formulario y confirmación.
 */
export class Cursos {
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);

  /** Columnas de la tabla de cursos. */
  readonly columns = ['nombre', 'creditos', 'descripcion', 'acciones'] as const;

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
  readonly cursosQuery = useCursosPaginadosQuery(this.pagina, this.tamaño, this.busqueda);

  /** Total de elementos (de la respuesta paginada). */
  readonly totalCursos = computed(() => this.cursosQuery.data()?.totalElements ?? 0);
  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.cursosQuery.data()?.totalPages ?? 0);
  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.cursosQuery.isPending());

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Controla la apertura/cierre del modal de formulario (crear/editar). */
  readonly cursoModalAbierto = signal(false);
  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);
  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');
  /** Curso seleccionado para edición o eliminación. */
  readonly cursoSeleccionado = signal<CursoResponse | null>(null);

  // ─── Formulario ──────────────────────────────────────────────────────

  readonly cursoForm = this.formBuilder.group({
    nombre: this.formBuilder.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    creditos: this.formBuilder.nonNullable.control(1, [Validators.required, Validators.min(1)]),
    descripcion: this.formBuilder.control<string | null>(null),
  });

  // ─── Mutaciones ──────────────────────────────────────────────────────

  readonly crearCursoMutation = useCrearCursoMutation();
  readonly actualizarCursoMutation = useActualizarCursoMutation();
  readonly eliminarCursoMutation = useEliminarCursoMutation();

  // ─── Paginación ──────────────────────────────────────────────────────

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.cursosQuery.data();
    if (!data || data.totalElements === 0) return '0 cursos';
    const desde = data.number * data.size + 1;
    const hasta = Math.min((data.number + 1) * data.size, data.totalElements);
    return `${desde}–${hasta} de ${data.totalElements}`;
  });

  /** Array de números de página para iterar en el template. */
  readonly paginasArray = computed(() => Array.from({ length: this.totalPaginas() }, (_, i) => i));

  protected readonly String = String;

  // ─── Búsqueda con debounce ───────────────────────────────────────────

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

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Abre modal de creación con formulario limpio. */
  openNuevoCursoModal(): void {
    this.modoFormulario.set('crear');
    this.cursoSeleccionado.set(null);
    this.resetFormulario();
    this.cursoModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo del curso. */
  openEditarCursoModal(curso: CursoResponse): void {
    this.modoFormulario.set('editar');
    this.cursoSeleccionado.set(curso);
    this.cargarFormulario(curso);
    this.cursoModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeCursoModal(): void {
    this.cursoModalAbierto.set(false);
    this.cursoSeleccionado.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarCursoModal(curso: CursoResponse): void {
    this.cursoSeleccionado.set(curso);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.cursoSeleccionado.set(null);
  }

  // ─── Guardar (crear/editar) ──────────────────────────────────────────

  /**
   * Guarda curso según modo actual (crear o editar).
   * Marca controles como tocados cuando formulario es inválido.
   */
  guardarCurso(observer: DialogObserver): void {
    if (this.cursoForm.invalid) {
      this.cursoForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();

    if (!payload) {
      return;
    }

    if (this.modoFormulario() === 'crear') {
      this.crearCursoMutation.mutate(payload, {
        onSuccess: () => {
          this.notifications
            .open('Curso creado exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeCursoModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al crear curso', {
              label: 'Error',
              appearance: 'error',
              autoClose: 5000,
            })
            .subscribe();
        },
      });
      return;
    }

    const seleccionado = this.cursoSeleccionado();

    if (!seleccionado) {
      return;
    }

    this.actualizarCursoMutation.mutate(
      { id: seleccionado.idCurso, curso: payload },
      {
        onSuccess: () => {
          this.notifications
            .open('Curso actualizado exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeCursoModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar curso', {
              label: 'Error',
              appearance: 'error',
              autoClose: 5000,
            })
            .subscribe();
        },
      },
    );
  }

  // ─── Eliminar ────────────────────────────────────────────────────────

  /** Ejecuta eliminación confirmada del curso seleccionado. */
  confirmarEliminar(observer: DialogObserver): void {
    const seleccionado = this.cursoSeleccionado();

    if (!seleccionado) {
      return;
    }

    this.eliminarCursoMutation.mutate(seleccionado.idCurso, {
      onSuccess: () => {
        this.notifications
          .open('Curso eliminado exitosamente', {
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
          .open(error?.message ?? 'Error al eliminar curso', {
            label: 'Error',
            appearance: 'error',
            autoClose: 5000,
          })
          .subscribe();
      },
    });
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  /**
   * Indica si hay una operación de guardado en curso.
   */
  isGuardando(): boolean {
    return this.crearCursoMutation.isPending() || this.actualizarCursoMutation.isPending();
  }

  /**
   * Indica si hay una operación de eliminación en curso.
   */
  isEliminando(): boolean {
    return this.eliminarCursoMutation.isPending();
  }

  // ─── Navegación de páginas ───────────────────────────────────────────

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

  // ─── Métodos privados ────────────────────────────────────────────────

  private resetFormulario(): void {
    this.cursoForm.reset({
      nombre: '',
      creditos: 1,
      descripcion: null,
    });
    this.cursoForm.markAsPristine();
    this.cursoForm.markAsUntouched();
  }

  private cargarFormulario(curso: CursoResponse): void {
    this.cursoForm.reset({
      nombre: curso.nombre,
      creditos: curso.creditos,
      descripcion: curso.descripcion,
    });
    this.cursoForm.markAsPristine();
    this.cursoForm.markAsUntouched();
  }

  private construirPayload(): CursoCreateRequest | null {
    const value = this.cursoForm.getRawValue();

    return {
      nombre: value.nombre.trim(),
      creditos: Number(value.creditos),
      descripcion: value.descripcion?.trim() ?? null,
    };
  }
}
