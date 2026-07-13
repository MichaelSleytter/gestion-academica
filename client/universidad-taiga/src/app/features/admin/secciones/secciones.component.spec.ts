import { Component } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TuiRoot } from '@taiga-ui/core';
import { SeccionService } from '../../../core/services/seccion.service';
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

  beforeEach(async () => {
    seccionService = createSeccionServiceMock();

    await TestBed.configureTestingModule({
      imports: [SeccionesHost],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: SeccionService, useValue: seccionService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SeccionesHost);
    component = fixture.debugElement.query(By.directive(Secciones)).componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create and load section catalogs', () => {
    expect(component).toBeTruthy();
    expect(seccionService.getSeccionesPaginado).toHaveBeenCalledWith(0, 10, '');
    expect(seccionService.getCursosList).toHaveBeenCalledOnce();
    expect(seccionService.getDocentesList).toHaveBeenCalledOnce();
    expect(seccionService.getCiclosAcademicosList).toHaveBeenCalledOnce();
  });

  it('should request and apply the next section code when course and cycle change in automatic mode', async () => {
    seccionService.getProximoCodigo.mockResolvedValueOnce('MAT-I-001');

    component.modoFormulario.set('crear');
    component.codigoAutomatico.set(true);
    component.seccionForm.controls.idCurso.setValue(1);
    component.seccionForm.controls.idCiclo.setValue(10);

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
    component.seccionForm.controls.idCurso.setValue(2);
    component.seccionForm.controls.idCiclo.setValue(20);

    await flushPromises();

    expect(seccionService.getProximoCodigo).not.toHaveBeenCalled();
    expect(component.seccionForm.controls.codigoSeccion.value).toBe('CUSTOM-01');
    expect(component.codigoAutomatico()).toBe(false);
  });

  it('should not show an automatic code error after switching to manual mode', async () => {
    seccionService.getProximoCodigo.mockRejectedValueOnce(new Error('network'));

    component.modoFormulario.set('crear');
    component.codigoAutomatico.set(true);
    component.seccionForm.controls.idCurso.setValue(3);
    component.seccionForm.controls.idCiclo.setValue(30);
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

    component.seccionForm.controls.idCurso.setValue(4);
    component.seccionForm.controls.idCiclo.setValue(40);
    fixture.detectChanges();

    const automaticInput = getCodeInput();
    expect(automaticInput?.readOnly).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Generando código...');

    component.activarCodigoManual(true);
    fixture.detectChanges();

    const manualInput = getCodeInput();
    expect(manualInput?.readOnly).toBe(false);
    expect(fixture.nativeElement.textContent).not.toContain('Generando código...');

    resolveCode('MAT-I-004');
    await flushPromises();
  });

  it('should include the optional color in the section payload', () => {
    const mutateSpy = vi
      .spyOn(component.crearSeccionMutation, 'mutate')
      .mockImplementation(() => undefined as never);

    component.modoFormulario.set('crear');
    component.seccionForm.patchValue({
      codigoSeccion: 'MAT-I-001',
      vacantes: 30,
      cicloAcademicoNombre: '2026-I',
      idCurso: 1,
      idCiclo: 10,
      color: '#2563EB',
    });

    component.guardarSeccion({ complete: vi.fn() });

    expect(mutateSpy).toHaveBeenCalledWith(
      {
        seccion: {
          codigoSeccion: 'MAT-I-001',
          vacantes: 30,
          cicloAcademicoNombre: '2026-I',
          color: '#2563EB',
        },
        idCurso: 1,
        idCiclo: 10,
      },
      expect.any(Object),
    );
  });
});

@Component({
  imports: [Secciones, TuiRoot],
  template: '<tui-root><app-secciones /></tui-root>',
})
class SeccionesHost {}

function createSeccionServiceMock() {
  return {
    getSeccionesPaginado: vi.fn().mockResolvedValue(emptyPage()),
    getCursosList: vi.fn().mockResolvedValue(emptyPage()),
    getDocentesList: vi.fn().mockResolvedValue(emptyPage()),
    getCiclosAcademicosList: vi.fn().mockResolvedValue([]),
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
