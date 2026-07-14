import { Component } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { TuiRoot } from '@taiga-ui/core';
import { of } from 'rxjs';

import { EstudianteService } from '../../../core/services/estudiante.service';
import { MatriculaService } from '../../../core/services/matricula.service';
import { SeccionService } from '../../../core/services/seccion.service';
import type { MatriculaResponse } from '../../../models/matricula';
import type { SeccionResponse } from '../../../models/seccion/seccion.response';
import {
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';
import { Matriculas } from './matriculas.component';

describe('Matriculas', () => {
  let fixture: ComponentFixture<MatriculasHost>;
  let component: Matriculas;
  let matriculaService: ReturnType<typeof createMatriculaServiceMock>;
  let seccionService: ReturnType<typeof createSeccionServiceMock>;

  beforeEach(async () => {
    matriculaService = createMatriculaServiceMock();
    seccionService = createSeccionServiceMock();

    await TestBed.configureTestingModule({
      imports: [MatriculasHost],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: ActivatedRoute, useValue: activatedRouteStub() },
        { provide: EstudianteService, useValue: {} },
        { provide: MatriculaService, useValue: matriculaService },
        { provide: SeccionService, useValue: seccionService },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    TestBed.inject(QueryClient).clear();
  });

  it('computes available vacancies from total capacity and active enrollments only', async () => {
    seccionService.getSeccionById.mockResolvedValue(seccion(4));
    matriculaService.getMatriculasBySeccion.mockResolvedValue([
      matricula('ACTIVA'),
      matricula('ACTIVA'),
      matricula('RETIRADA'),
    ]);

    createComponent();

    await vi.waitFor(() => expect(component.vacantesDisponibles()).toBe(2));
    expect(component.ocupacionPorcentaje()).toBe(50);
  });

  it('renders vacancy capacity as a meter', async () => {
    seccionService.getSeccionById.mockResolvedValue(seccion(4));
    matriculaService.getMatriculasBySeccion.mockResolvedValue([
      matricula('ACTIVA'),
      matricula('ACTIVA'),
      matricula('RETIRADA'),
    ]);

    createComponent();
    fixture.detectChanges();

    await vi.waitFor(() => expect(fixture.nativeElement.textContent).toContain('2 de 4 ocupadas · 2 vacantes'));
    fixture.detectChanges();

    const meter = fixture.nativeElement.querySelector('[role="meter"]');
    expect(meter).not.toBeNull();
    expect(meter.getAttribute('aria-valuenow')).toBe('2');
    expect(meter.getAttribute('aria-valuemax')).toBe('4');
    expect(meter.getAttribute('aria-valuetext')).toBe('2 de 4 ocupadas, 2 vacantes');
  });

  it('clamps vacancy and occupancy when active enrollments exceed capacity', async () => {
    seccionService.getSeccionById.mockResolvedValue(seccion(2));
    matriculaService.getMatriculasBySeccion.mockResolvedValue([
      matricula('ACTIVA'),
      matricula('ACTIVA'),
      matricula('ACTIVA'),
    ]);

    createComponent();

    await vi.waitFor(() => expect(component.vacantesDisponibles()).toBe(0));
    expect(component.ocupacionPorcentaje()).toBe(100);
  });

  it('keeps vacancy metrics empty when section capacity is missing', async () => {
    seccionService.getSeccionById.mockResolvedValue(null as unknown as SeccionResponse);
    matriculaService.getMatriculasBySeccion.mockResolvedValue([matricula('ACTIVA')]);

    createComponent();

    await vi.waitFor(() => expect(component.ocupados()).toBe(1));
    expect(component.capacidad()).toBeNull();
    expect(component.vacantesDisponibles()).toBeNull();
    expect(component.ocupacionPorcentaje()).toBe(0);
  });

  it('retires an enrollment and refreshes enrollments and section on success', async () => {
    seccionService.getSeccionById.mockResolvedValue(seccion(4));
    matriculaService.getMatriculasBySeccion.mockResolvedValue([matricula('ACTIVA')]);
    matriculaService.cambiarEstado.mockResolvedValue(matricula('RETIRADA'));
    createComponent();
    const matriculasRefetch = vi.spyOn(component.matriculasQuery, 'refetch');
    const seccionRefetch = vi.spyOn(component.seccionQuery, 'refetch');
    const observer = { complete: vi.fn() };

    component.openRetirarModal(matricula('ACTIVA'));
    component.confirmarRetirar(observer);

    await vi.waitFor(() => expect(matriculaService.cambiarEstado).toHaveBeenCalledWith(1, 'RETIRADA'));
    await vi.waitFor(() => expect(matriculasRefetch).toHaveBeenCalled());
    expect(seccionRefetch).toHaveBeenCalled();
    expect(observer.complete).toHaveBeenCalled();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(MatriculasHost);
    fixture.detectChanges();
    component = fixture.debugElement.query(By.directive(Matriculas)).componentInstance;
  }
});

@Component({
  imports: [Matriculas, TuiRoot],
  template: '<tui-root><app-matriculas /></tui-root>',
})
class MatriculasHost {}

function activatedRouteStub() {
  return {
    paramMap: of(convertToParamMap({ id: '7' })),
    snapshot: { paramMap: convertToParamMap({ id: '7' }) },
  };
}

function createMatriculaServiceMock() {
  return {
    getMatriculasBySeccion: vi.fn(),
    matricular: vi.fn(),
    cambiarEstado: vi.fn(),
  };
}

function createSeccionServiceMock() {
  return {
    getSeccionById: vi.fn(),
  };
}

function matricula(estado: string): MatriculaResponse {
  return {
    idMatricula: 1,
    fechaMatricula: null,
    estado,
  };
}

function seccion(vacantes: number): SeccionResponse {
  return {
    idSeccion: 7,
    codigoSeccion: 'SEC-7',
    cicloAcademicoNombre: '2026-I',
    vacantes,
    curso: {
      idCurso: 1,
      nombre: 'Matemática',
      creditos: 4,
      descripcion: null,
    },
    cicloAcademico: {
      idCiclo: 1,
      nombre: '2026-I',
      fechaInicio: '2026-03-01',
      fechaFin: '2026-07-15',
    },
  };
}
