import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  type OnDestroy,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
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
import { TuiInputColor, TuiSkeleton, TuiSelect, TuiSwitch } from '@taiga-ui/kit';
import type { SeccionResponse } from '../../../models/seccion/seccion.response';
import type { SeccionCreateRequest } from '../../../models/seccion/seccion.request';
import {
  useSeccionesPaginadosQuery,
  useCrearSeccionMutation,
  useActualizarSeccionMutation,
  useEliminarSeccionMutation,
} from '../../../queries/seccion.query';
import {
  SeccionService,
  type CicloAcademicoResponse,
} from '../../../core/services/seccion.service';
import { RoleService } from '../../../core/services/role.service';
import type { CursoResponse } from '../../../models/curso/curso.response';
import type { DocenteResponse } from '../../../models/docente/docente.response';

type ModoFormulario = 'crear' | 'editar';

interface DialogObserver {
  complete(): void;
}

@Component({
  selector: 'app-secciones',
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
    TuiInputColor,
    TuiSkeleton,
    TuiSelect,
    TuiSwitch,
  ],
  templateUrl: './secciones.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
/**
 * Página de gestión de secciones académicas.
 * Lista, crea, edita y elimina secciones con modal de formulario y confirmación.
 * Cada sección agrupa un curso dentro de un ciclo académico.
 *
 * Accesible para: ADMIN, DOCENTE
 */
export class Secciones implements OnDestroy {
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);
  private readonly notifications = inject(TuiNotificationService);
  private readonly seccionService = inject(SeccionService);
  private readonly roleService = inject(RoleService);

  readonly isAdmin = this.roleService.isAdmin;

  /** Columnas de la tabla de secciones. */
  readonly columns = ['codigo', 'curso', 'ciclo', 'vacantes', 'acciones'] as const;

  // ─── Paginación y búsqueda ───────────────────────────────────────────

  /** Página actual (0-based). */
  readonly pagina = signal(0);
  /** Elementos por página. */
  readonly tamaño = signal(10);
  /** Texto de búsqueda actual. */
  readonly busqueda = signal('');
  /** Timer para debounce del search. */
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly formSubscriptions = new Subscription();
  private codigoSolicitudSecuencia = 0;

  /** Query paginada que se refresca al cambiar página, tamaño o búsqueda. */
  readonly seccionesQuery = useSeccionesPaginadosQuery(this.pagina, this.tamaño, this.busqueda);

  /** Total de elementos (de la respuesta paginada). */
  readonly totalSecciones = computed(() => this.seccionesQuery.data()?.totalElements ?? 0);
  /** Total de páginas. */
  readonly totalPaginas = computed(() => this.seccionesQuery.data()?.totalPages ?? 0);
  /** Indica si la consulta está cargando. */
  readonly isLoading = computed(() => this.seccionesQuery.isPending());

  // ─── Listas para dropdowns ───────────────────────────────────────────

  /** Lista de cursos para el select del formulario. */
  readonly cursosList = signal<CursoResponse[]>([]);
  /** Lista de ciclos académicos para el select del formulario. */
  readonly ciclosList = signal<CicloAcademicoResponse[]>([]);
  /** Lista de docentes para asignar a una sección. */
  readonly docentesList = signal<DocenteResponse[]>([]);
  /** Indica si las listas de catálogo están cargando. */
  readonly catalogosLoading = signal(true);
  /** Error al cargar catálogos. */
  readonly catalogosError = signal<string | null>(null);

  constructor() {
    this.cargarCatalogos();
    this.sincronizarFormularioConCatalogos();
  }

  ngOnDestroy(): void {
    this.formSubscriptions.unsubscribe();
    if (this.debounceTimer) clearTimeout(this.debounceTimer);
  }

  private sincronizarFormularioConCatalogos(): void {
    const actualizarCodigo = () => {
      this.actualizarNombreCicloSeleccionado();
      void this.solicitarCodigoAutomatico();
    };

    this.formSubscriptions.add(
      this.seccionForm.controls.idCurso.valueChanges.subscribe(actualizarCodigo),
    );
    this.formSubscriptions.add(
      this.seccionForm.controls.idCiclo.valueChanges.subscribe(actualizarCodigo),
    );
  }

  private actualizarNombreCicloSeleccionado(): void {
    const idCiclo = this.seccionForm.controls.idCiclo.value;
    const ciclo = this.ciclosList().find((item) => item.idCiclo === idCiclo);
    if (ciclo && this.seccionForm.controls.cicloAcademicoNombre.value !== ciclo.nombre) {
      this.seccionForm.controls.cicloAcademicoNombre.setValue(ciclo.nombre);
    }
  }

  private cargarCatalogos(): void {
    this.catalogosLoading.set(true);
    this.catalogosError.set(null);

    Promise.all([
      this.seccionService.getCursosList(),
      this.seccionService.getCiclosAcademicosList(),
      this.seccionService.getDocentesList(),
    ])
      .then(([cursosPage, ciclos, docentesPage]) => {
        this.cursosList.set(cursosPage.content);
        this.ciclosList.set(ciclos);
        this.docentesList.set(docentesPage.content);
        this.catalogosLoading.set(false);
      })
      .catch(() => {
        this.catalogosError.set('Error al cargar datos del formulario');
        this.catalogosLoading.set(false);
      });
  }

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Controla la apertura/cierre del modal de formulario (crear/editar). */
  readonly seccionModalAbierto = signal(false);
  /** Controla la apertura/cierre del diálogo de confirmación de eliminación. */
  readonly eliminarModalAbierto = signal(false);
  /** Controla la apertura/cierre del modal de asignación docente. */
  readonly docentesModalAbierto = signal(false);
  /** Modo actual del formulario: 'crear' o 'editar'. */
  readonly modoFormulario = signal<ModoFormulario>('crear');
  /** Sección seleccionada para edición o eliminación. */
  readonly seccionSeleccionada = signal<SeccionResponse | null>(null);
  /** Indica si el código se mantiene sincronizado con el backend. */
  readonly codigoAutomatico = signal(true);
  /** Indica si se está consultando el próximo código. */
  readonly codigoAutomaticoCargando = signal(false);
  /** Error al generar el próximo código. */
  readonly codigoAutomaticoError = signal<string | null>(null);

  // ─── Formulario ──────────────────────────────────────────────────────

  readonly seccionForm = this.formBuilder.group({
    codigoSeccion: this.formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2),
    ]),
    vacantes: this.formBuilder.nonNullable.control(1, [Validators.required, Validators.min(1)]),
    cicloAcademicoNombre: this.formBuilder.nonNullable.control('', [Validators.required]),
    idCurso: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
    idCiclo: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
    color: this.formBuilder.control<string | null>(null),
  });

  readonly asignacionDocenteForm = this.formBuilder.group({
    idDocente: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
  });

  readonly docentesAsignados = signal<DocenteResponse[]>([]);
  readonly docentesAsignadosLoading = signal(false);
  readonly asignacionDocenteGuardando = signal(false);

  readonly docentesDisponibles = computed(() => {
    const asignados = new Set(this.docentesAsignados().map((docente) => docente.idUsuario));
    return this.docentesList().filter((docente) => !asignados.has(docente.idUsuario));
  });

  // ─── Mutaciones ──────────────────────────────────────────────────────

  readonly crearSeccionMutation = useCrearSeccionMutation();
  readonly actualizarSeccionMutation = useActualizarSeccionMutation();
  readonly eliminarSeccionMutation = useEliminarSeccionMutation();

  // ─── Navegación ───────────────────────────────────────────────────────

  /** Navega a la vista de matriculados de una sección. */
  verMatriculados(idSeccion: number): void {
    void this.router.navigate(['/app/secciones', idSeccion, 'matriculas']);
  }

  // ─── Paginación ──────────────────────────────────────────────────────

  /** Indica si hay una página anterior. */
  readonly hayPaginaAnterior = computed(() => this.pagina() > 0);
  /** Indica si hay una página siguiente. */
  readonly hayPaginaSiguiente = computed(() => this.pagina() < this.totalPaginas() - 1);

  /** Rango mostrado: "1–10 de 150" */
  readonly infoPaginacion = computed(() => {
    const data = this.seccionesQuery.data();
    if (!data || data.totalElements === 0) return '0 secciones';
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

  activarCodigoManual(manual: boolean): void {
    this.codigoAutomatico.set(!manual);
    this.codigoAutomaticoError.set(null);
    if (manual) {
      this.codigoSolicitudSecuencia++;
      this.codigoAutomaticoCargando.set(false);
      return;
    }

    void this.solicitarCodigoAutomatico();
  }

  regenerarCodigoAutomatico(): void {
    this.codigoAutomatico.set(true);
    void this.solicitarCodigoAutomatico();
  }

  // ─── Modales ─────────────────────────────────────────────────────────

  /** Abre modal de creación con formulario limpio. */
  openNuevaSeccionModal(): void {
    this.modoFormulario.set('crear');
    this.seccionSeleccionada.set(null);
    this.resetFormulario();
    this.codigoAutomatico.set(true);
    this.seccionModalAbierto.set(true);
  }

  /** Abre modal de edición usando objeto completo de la sección. */
  openEditarSeccionModal(seccion: SeccionResponse): void {
    this.modoFormulario.set('editar');
    this.seccionSeleccionada.set(seccion);
    this.cargarFormulario(seccion);
    this.codigoAutomatico.set(false);
    this.seccionModalAbierto.set(true);
  }

  /** Cierra modal de formulario y limpia estado interno. */
  closeSeccionModal(): void {
    this.seccionModalAbierto.set(false);
    this.seccionSeleccionada.set(null);
    this.resetFormulario();
  }

  /** Abre diálogo de confirmación para eliminación. */
  openEliminarSeccionModal(seccion: SeccionResponse): void {
    this.seccionSeleccionada.set(seccion);
    this.eliminarModalAbierto.set(true);
  }

  /** Cierra diálogo de eliminación. */
  closeEliminarModal(): void {
    this.eliminarModalAbierto.set(false);
    this.seccionSeleccionada.set(null);
  }

  openDocentesModal(seccion: SeccionResponse): void {
    if (!this.isAdmin()) return;
    this.seccionSeleccionada.set(seccion);
    this.asignacionDocenteForm.reset({ idDocente: null });
    this.docentesModalAbierto.set(true);
    void this.cargarDocentesAsignados(seccion.idSeccion);
  }

  closeDocentesModal(): void {
    this.docentesModalAbierto.set(false);
    this.seccionSeleccionada.set(null);
    this.docentesAsignados.set([]);
    this.asignacionDocenteForm.reset({ idDocente: null });
  }

  // ─── Guardar (crear/editar) ──────────────────────────────────────────

  guardarSeccion(observer: DialogObserver): void {
    if (this.seccionForm.invalid) {
      this.seccionForm.markAllAsTouched();
      return;
    }

    const payload = this.construirPayload();
    if (!payload) return;

    const raw = this.seccionForm.getRawValue();
    const idCurso = raw.idCurso!;
    const idCiclo = raw.idCiclo!;

    if (this.modoFormulario() === 'crear') {
      this.crearSeccionMutation.mutate(
        { seccion: payload, idCurso, idCiclo },
        {
          onSuccess: () => {
            this.notifications
              .open('Sección creada exitosamente', {
                label: 'Éxito',
                appearance: 'success',
                autoClose: 3000,
              })
              .subscribe();
            observer.complete();
            this.closeSeccionModal();
          },
          onError: (error) => {
            this.notifications
              .open(error?.message ?? 'Error al crear sección', {
                label: 'Error',
                appearance: 'error',
                autoClose: 5000,
              })
              .subscribe();
          },
        },
      );
      return;
    }

    const seleccionada = this.seccionSeleccionada();
    if (!seleccionada) return;

    this.actualizarSeccionMutation.mutate(
      { id: seleccionada.idSeccion, seccion: payload, idCurso, idCiclo },
      {
        onSuccess: () => {
          this.notifications
            .open('Sección actualizada exitosamente', {
              label: 'Éxito',
              appearance: 'success',
              autoClose: 3000,
            })
            .subscribe();
          observer.complete();
          this.closeSeccionModal();
        },
        onError: (error) => {
          this.notifications
            .open(error?.message ?? 'Error al actualizar sección', {
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

  confirmarEliminar(observer: DialogObserver): void {
    const seleccionada = this.seccionSeleccionada();
    if (!seleccionada) return;

    this.eliminarSeccionMutation.mutate(seleccionada.idSeccion, {
      onSuccess: () => {
        this.notifications
          .open('Sección eliminada exitosamente', {
            label: 'Eliminada',
            appearance: 'success',
            autoClose: 3000,
          })
          .subscribe();
        observer.complete();
        this.closeEliminarModal();
      },
      onError: (error) => {
        this.notifications
          .open(error?.message ?? 'Error al eliminar sección', {
            label: 'Error',
            appearance: 'error',
            autoClose: 5000,
          })
          .subscribe();
      },
    });
  }

  async asignarDocente(): Promise<void> {
    const seccion = this.seccionSeleccionada();
    const idDocente = this.asignacionDocenteForm.controls.idDocente.value;

    if (!seccion || !idDocente || this.asignacionDocenteForm.invalid) {
      this.asignacionDocenteForm.markAllAsTouched();
      return;
    }

    this.asignacionDocenteGuardando.set(true);
    try {
      await this.seccionService.asignarDocente(seccion.idSeccion, idDocente);
      await this.cargarDocentesAsignados(seccion.idSeccion);
      this.asignacionDocenteForm.reset({ idDocente: null });
      this.notifications.open('Docente asignado exitosamente', {
        label: 'Éxito',
        appearance: 'success',
        autoClose: 3000,
      }).subscribe();
    } catch (error) {
      this.notifications.open(error instanceof Error ? error.message : 'Error al asignar docente', {
        label: 'Error',
        appearance: 'error',
        autoClose: 5000,
      }).subscribe();
    } finally {
      this.asignacionDocenteGuardando.set(false);
    }
  }

  async removerDocente(idDocente: number): Promise<void> {
    const seccion = this.seccionSeleccionada();
    if (!seccion) return;

    this.asignacionDocenteGuardando.set(true);
    try {
      await this.seccionService.removerDocente(seccion.idSeccion, idDocente);
      await this.cargarDocentesAsignados(seccion.idSeccion);
      this.notifications.open('Docente removido de la sección', {
        label: 'Actualizado',
        appearance: 'success',
        autoClose: 3000,
      }).subscribe();
    } catch (error) {
      this.notifications.open(error instanceof Error ? error.message : 'Error al remover docente', {
        label: 'Error',
        appearance: 'error',
        autoClose: 5000,
      }).subscribe();
    } finally {
      this.asignacionDocenteGuardando.set(false);
    }
  }

  // ─── Estado de carga ─────────────────────────────────────────────────

  isGuardando(): boolean {
    return this.crearSeccionMutation.isPending() || this.actualizarSeccionMutation.isPending();
  }

  isEliminando(): boolean {
    return this.eliminarSeccionMutation.isPending();
  }

  docenteNombre(docente: DocenteResponse): string {
    return `${docente.nombre} ${docente.apellido}`;
  }

  // ─── Navegación de páginas ───────────────────────────────────────────

  paginaAnterior(): void {
    if (this.pagina() > 0) this.pagina.update((p) => p - 1);
  }

  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) this.pagina.update((p) => p + 1);
  }

  // ─── Métodos privados ────────────────────────────────────────────────

  private async solicitarCodigoAutomatico(): Promise<void> {
    const value = this.seccionForm.getRawValue();
    const idCurso = value.idCurso;
    const idCiclo = value.idCiclo;

    if (this.modoFormulario() !== 'crear' || !this.codigoAutomatico() || !idCurso || !idCiclo) {
      return;
    }

    const solicitudActual = ++this.codigoSolicitudSecuencia;
    this.codigoAutomaticoCargando.set(true);
    this.codigoAutomaticoError.set(null);

    try {
      const codigo = await this.seccionService.getProximoCodigo(idCurso, idCiclo);
      if (solicitudActual === this.codigoSolicitudSecuencia && this.codigoAutomatico()) {
        this.seccionForm.controls.codigoSeccion.setValue(codigo);
        this.seccionForm.controls.codigoSeccion.markAsDirty();
      }
    } catch {
      if (solicitudActual === this.codigoSolicitudSecuencia && this.codigoAutomatico()) {
        this.codigoAutomaticoError.set('No se pudo generar el código automáticamente');
      }
    } finally {
      if (solicitudActual === this.codigoSolicitudSecuencia && this.codigoAutomatico()) {
        this.codigoAutomaticoCargando.set(false);
      }
    }
  }

  private resetFormulario(): void {
    this.seccionForm.reset({
      codigoSeccion: '',
      vacantes: 1,
      cicloAcademicoNombre: '',
      idCurso: null,
      idCiclo: null,
      color: null,
    });
    this.seccionForm.markAsPristine();
    this.seccionForm.markAsUntouched();
  }

  private cargarFormulario(seccion: SeccionResponse): void {
    this.seccionForm.reset({
      codigoSeccion: seccion.codigoSeccion,
      vacantes: seccion.vacantes,
      cicloAcademicoNombre: seccion.cicloAcademicoNombre,
      idCurso: seccion.curso.idCurso,
      idCiclo: seccion.cicloAcademico.idCiclo,
      color: seccion.color ?? null,
    });
    this.seccionForm.markAsPristine();
    this.seccionForm.markAsUntouched();
  }

  private construirPayload(): SeccionCreateRequest | null {
    const value = this.seccionForm.getRawValue();
    const color = value.color?.trim() || null;
    return {
      codigoSeccion: value.codigoSeccion.trim(),
      vacantes: Number(value.vacantes),
      cicloAcademicoNombre: value.cicloAcademicoNombre.trim(),
      color,
    };
  }

  private async cargarDocentesAsignados(idSeccion: number): Promise<void> {
    this.docentesAsignadosLoading.set(true);
    try {
      this.docentesAsignados.set(await this.seccionService.getDocentesAsignados(idSeccion));
    } catch {
      this.docentesAsignados.set([]);
      this.notifications.open('No se pudieron cargar los docentes asignados', {
        label: 'Error',
        appearance: 'error',
        autoClose: 5000,
      }).subscribe();
    } finally {
      this.docentesAsignadosLoading.set(false);
    }
  }
}
