import { Component } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { EMPTY } from 'rxjs';
import { TuiNotificationService, TuiRoot } from '@taiga-ui/core';
import {
  SeccionService,
  type CicloAcademicoResponse,
} from '../../../core/services/seccion.service';
import type { CursoResponse } from '../../../models/curso/curso.response';
import type { SeccionResponse } from '../../../models/seccion/seccion.response';
import {
  emptyPage,
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';

import { Secciones } from './secciones.component';

describe('Secciones', () => {
  let component: Secciones;
  let fixture: ComponentFixture<SeccionesHost>;
  let seccionService: ReturnType<typeof createSeccionServiceMock>;
  let notifications: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    seccionService = createSeccionServiceMock();
    notifications = { open: vi.fn(() => EMPTY) };

    await TestBed.configureTestingModule({
      imports: [SeccionesHost],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: SeccionService, useValue: seccionService },
        { provide: TuiNotificationService, useValue: notifications },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SeccionesHost);
    component = fixture.debugElement.query(By.directive(Secciones)).componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create and load section catalogs', async () => {
    expect(component).toBeTruthy();
    expect(seccionService.getCursosList).toHaveBeenCalledOnce();
    expect(seccionService.getDocentesList).toHaveBeenCalledOnce();
    expect(seccionService.getCiclosAcademicosList).toHaveBeenCalledOnce();

    // Esperar a que carguen los catálogos y se active el filtro
    await fixture.whenStable();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    // El filtro default se activa y la query se dispara con el ciclo 2026-I
    expect(component.filterReady()).toBe(true);
    expect(component.idCicloFiltro()).toBe(10);
    expect(seccionService.getSeccionesPaginado).toHaveBeenCalledWith(0, 10, '', 10);
  });

  it('should request and apply the next section code when course and cycle change in automatic mode', async () => {
    seccionService.getProximoCodigo.mockResolvedValueOnce('MAT-I-001');

    component.modoFormulario.set('crear');
    component.codigoAutomatico.set(true);
    component.seccionForm.controls.idCurso.setValue(cursoMatematicas);
    component.seccionForm.controls.idCiclo.setValue(ciclo2026I);

    await flushPromises();

    expect(seccionService.getProximoCodigo).toHaveBeenCalledWith(1, 10);
    expect(component.seccionForm.controls.codigoSeccion.value).toBe('MAT-I-001');
    expect(component.codigoAutomatico()).toBe(true);
  });

  it('should keep a manually entered section code when manual mode is enabled', async () => {
    component.modoFormulario.set('crear');
    component.codigoAutomatico.set(true);
    component.activarCodigoManual(true);
    component.seccionForm.controls.codigoSeccion.setValue('CUSTOM-01');
    component.seccionForm.controls.idCurso.setValue(cursoProgramacion);
    component.seccionForm.controls.idCiclo.setValue(ciclo2026II);

    await flushPromises();

    expect(seccionService.getProximoCodigo).not.toHaveBeenCalled();
    expect(component.seccionForm.controls.codigoSeccion.value).toBe('CUSTOM-01');
    expect(component.codigoAutomatico()).toBe(false);
  });

  it('should not show an automatic code error after switching to manual mode', async () => {
    seccionService.getProximoCodigo.mockRejectedValueOnce(new Error('network'));

    component.modoFormulario.set('crear');
    component.codigoAutomatico.set(true);
    component.seccionForm.controls.idCurso.setValue(cursoMatematicas);
    component.seccionForm.controls.idCiclo.setValue(ciclo2026I);
    component.activarCodigoManual(true);

    await flushPromises();

    expect(component.codigoAutomatico()).toBe(false);
    expect(component.codigoAutomaticoError()).toBeNull();
  });

  it('should expose the automatic mode contract through the code input and loading text', async () => {
    let resolveCode: (value: string) => void = () => {};
    seccionService.getProximoCodigo.mockReturnValueOnce(
      new Promise<string>((resolve) => {
        resolveCode = resolve;
      }),
    );

    component.openNuevaSeccionModal();
    fixture.detectChanges();

    component.seccionForm.controls.idCurso.setValue(cursoMatematicas);
    component.seccionForm.controls.idCiclo.setValue(ciclo2026I);
    fixture.detectChanges();

    const automaticInput = getCodeInput();
    expect(automaticInput?.readOnly).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Generando código');

    component.activarCodigoManual(true);
    fixture.detectChanges();

    const manualInput = getCodeInput();
    expect(manualInput?.readOnly).toBe(false);
    expect(fixture.nativeElement.textContent).not.toContain('Generando código');

    resolveCode('MAT-I-004');
    await flushPromises();
  });

  it('should show a friendly warning when deleting a section with active enrollments', () => {
    vi.spyOn(component.eliminarSeccionMutation, 'mutate').mockImplementation(((
      _id: number,
      options: { onError?: (error: unknown) => void },
    ) => {
      options.onError?.({
        status: 400,
        error: {
          mensaje: 'No se puede eliminar la sección. Tiene 4 matrícula(s) activa(s).',
          error: 'Error de negocio',
          status: 400,
        },
      });
    }) as never);

    component.openEliminarSeccionModal(seccionMatematicas);
    component.confirmarEliminar({ complete: vi.fn() });

    expect(notifications.open).toHaveBeenCalledWith(
      'Esta sección tiene 4 estudiantes matriculados. Primero desmatriculalos o trasladalos a otra sección.',
      {
        label: 'No se puede eliminar la sección',
        appearance: 'warning',
        autoClose: 5000,
      },
    );
  });

  it('should show a generic friendly message for unknown delete failures', () => {
    vi.spyOn(component.eliminarSeccionMutation, 'mutate').mockImplementation(((
      _id: number,
      options: { onError?: (error: unknown) => void },
    ) => {
      options.onError?.({ message: 'Http failure response for /api/secciones/1: 500' });
    }) as never);

    component.openEliminarSeccionModal(seccionMatematicas);
    component.confirmarEliminar({ complete: vi.fn() });

    expect(notifications.open).toHaveBeenCalledWith(
      'No se pudo eliminar la sección. Intentalo nuevamente.',
      {
        label: 'No se pudo eliminar la sección',
        appearance: 'error',
        autoClose: 5000,
      },
    );
  });
});

@Component({
  imports: [Secciones, TuiRoot],
  template: '<tui-root><app-secciones /></tui-root>',
})
class SeccionesHost {}

const cursoMatematicas: CursoResponse = {
  idCurso: 1,
  nombre: 'Matemáticas I',
  creditos: 4,
  descripcion: null,
};

const cursoProgramacion: CursoResponse = {
  idCurso: 2,
  nombre: 'Programación Web',
  creditos: 4,
  descripcion: null,
};

const ciclo2026I: CicloAcademicoResponse = {
  idCiclo: 10,
  nombre: '2026-I',
  fechaInicio: '2026-03-01',
  fechaFin: '2026-07-31',
};

const ciclo2026II: CicloAcademicoResponse = {
  idCiclo: 20,
  nombre: '2026-II',
  fechaInicio: '2026-08-01',
  fechaFin: '2026-12-20',
};

const seccionMatematicas: SeccionResponse = {
  idSeccion: 100,
  codigoSeccion: 'MAT-I-001',
  cicloAcademicoNombre: '2026-I',
  vacantes: 30,
  color: '#2563EB',
  curso: cursoMatematicas,
  cicloAcademico: ciclo2026I,
};

function createSeccionServiceMock() {
  return {
    getSeccionesPaginado: vi.fn().mockResolvedValue(emptyPage()),
    getCursosList: vi.fn().mockResolvedValue({
      ...emptyPage<CursoResponse>(),
      content: [cursoMatematicas, cursoProgramacion],
    }),
    getDocentesList: vi.fn().mockResolvedValue(emptyPage()),
    getCiclosAcademicosList: vi.fn().mockResolvedValue([ciclo2026I, ciclo2026II]),
    crearSeccion: vi.fn(),
    actualizarSeccion: vi.fn(),
    eliminarSeccion: vi.fn(),
    getProximoCodigo: vi.fn().mockResolvedValue('MAT-I-001'),
  };
}

function getCodeInput(): HTMLInputElement | null {
  return document.querySelector('input[formcontrolname="codigoSeccion"]');
}

function flushPromises(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve));
}
