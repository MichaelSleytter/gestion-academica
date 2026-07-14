import { Component } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { TuiRoot } from '@taiga-ui/core';
import { QueryClient } from '@tanstack/angular-query-experimental';

import { CatalogoService } from '../../../core/services/catalogo.service';
import { EstudianteService } from '../../../core/services/estudiante.service';
import {
  emptyPage,
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';
import { Estudiantes } from './estudiantes.component';

describe('Estudiantes', () => {
  let fixture: ComponentFixture<EstudiantesHost>;
  let component: Estudiantes;
  let router: { navigate: ReturnType<typeof vi.fn> };
  let estudianteService: ReturnType<typeof createEstudianteServiceMock>;

  beforeEach(async () => {
    router = { navigate: vi.fn() };
    estudianteService = createEstudianteServiceMock();

    await TestBed.configureTestingModule({
      imports: [EstudiantesHost],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: ActivatedRoute, useValue: activatedRouteStub({ view: 'row', q: 'ada', page: '3' }) },
        { provide: Router, useValue: router },
        { provide: EstudianteService, useValue: estudianteService },
        { provide: CatalogoService, useValue: createCatalogoServiceMock() },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EstudiantesHost);
    component = fixture.debugElement.query(By.directive(Estudiantes)).componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.inject(QueryClient).clear();
  });

  it('restores initial list state from query params without navigating', () => {
    expect(component.viewMode()).toBe('row');
    expect(component.busqueda()).toBe('ada');
    expect(component.pagina()).toBe(2);
    expect(estudianteService.getEstudiantesPaginado).toHaveBeenCalledWith(2, 10, 'ada');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('writes user list changes to query params once per change', async () => {
    component.setViewMode(0);
    expect(router.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { view: null, q: 'ada', page: 3 },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    }));

    component.setPagina(0);
    expect(router.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { view: null, q: 'ada', page: null },
    }));

    component.onBusquedaChange(' grace ');
    await new Promise((resolve) => setTimeout(resolve, 310));

    expect(router.navigate).toHaveBeenCalledTimes(3);
    expect(router.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { view: null, q: 'grace', page: null },
    }));
  });
});

@Component({
  imports: [Estudiantes, TuiRoot],
  template: '<tui-root><app-estudiantes /></tui-root>',
})
class EstudiantesHost {}

function activatedRouteStub(queryParams: Record<string, string>) {
  return {
    snapshot: { queryParamMap: convertToParamMap(queryParams) },
  };
}

function createEstudianteServiceMock() {
  return {
    getEstudiantesPaginado: vi.fn().mockResolvedValue(emptyPage()),
    crearEstudiante: vi.fn(),
    actualizarEstudiante: vi.fn(),
    eliminarEstudiante: vi.fn(),
  };
}

function createCatalogoServiceMock() {
  return {
    getTipoDocumento: vi.fn().mockResolvedValue([]),
    getCarreras: vi.fn().mockResolvedValue([]),
  };
}
