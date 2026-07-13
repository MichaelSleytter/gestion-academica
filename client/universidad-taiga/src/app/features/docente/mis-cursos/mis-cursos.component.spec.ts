import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { TokenService } from '../../../core/services/token.service';
import { DocenteRoleService } from '../../../core/services/docente-role.service';
import type { SeccionResponse } from '../../../models/seccion/seccion.response';
import { provideQueryTestClient } from '../../../testing/angular-test-providers';

import { MisCursos } from './mis-cursos.component';

describe('MisCursos', () => {
  let component: MisCursos;
  let fixture: ComponentFixture<MisCursos>;
  let docenteRoleService: { getSeccionesByDocente: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    const router = { navigate: vi.fn().mockResolvedValue(true) };
    docenteRoleService = { getSeccionesByDocente: vi.fn().mockResolvedValue([]) };
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date(2026, 6, 15, 12));

    await TestBed.configureTestingModule({
      imports: [MisCursos],
      providers: [
        provideQueryTestClient(),
        { provide: Router, useValue: router },
        { provide: TokenService, useValue: { extractCurrentUserId: () => 42 } },
        { provide: DocenteRoleService, useValue: docenteRoleService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MisCursos);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.useRealTimers();
    TestBed.inject(QueryClient).clear();
  });

  it('should create with docente id from token', () => {
    expect(component).toBeTruthy();
    expect(component.docenteId()).toBe(42);
  });

  it('should navigate to grade loading route', () => {
    component.navigateToNotas(7);

    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/app/docente/mis-cursos', 7, 'notas']);
  });

  it('filters current period by default and includes the local end date', async () => {
    await renderWithSecciones([
      seccion({ idSeccion: 1, nombrePeriodo: '2026-I', fechaInicio: '2026-03-01', fechaFin: '2026-07-15' }),
      seccion({ idSeccion: 2, nombrePeriodo: '2025-II', fechaInicio: '2025-08-01', fechaFin: '2025-12-15' }),
    ]);

    expect(component.periodoSeleccionado()).toBe('actual');
    expect(component.seccionesFiltradas().map((seccion) => seccion.idSeccion)).toEqual([1]);
    expect(component.totalSecciones()).toBe(1);
  });

  it('shows every section when period filter is todos', async () => {
    await renderWithSecciones([
      seccion({ idSeccion: 1, nombrePeriodo: '2026-I', fechaInicio: '2026-03-01', fechaFin: '2026-07-15' }),
      seccion({ idSeccion: 2, nombrePeriodo: '2025-II', fechaInicio: '2025-08-01', fechaFin: '2025-12-15' }),
    ]);

    component.periodoSeleccionado.set('todos');

    expect(component.seccionesFiltradas()).toHaveLength(2);
    expect(component.totalSecciones()).toBe(2);
  });

  it('filters sections by named period', async () => {
    await renderWithSecciones([
      seccion({ idSeccion: 1, nombrePeriodo: '2026-I', fechaInicio: '2026-03-01', fechaFin: '2026-07-15' }),
      seccion({ idSeccion: 2, nombrePeriodo: '2025-II', fechaInicio: '2025-08-01', fechaFin: '2025-12-15' }),
    ]);

    component.periodoSeleccionado.set('2025-II');

    expect(component.seccionesFiltradas().map((seccion) => seccion.idSeccion)).toEqual([2]);
    expect(component.periodos()).toEqual(['2026-I', '2025-II']);
  });

  it('returns an empty list when current period has no sections', async () => {
    await renderWithSecciones([
      seccion({ idSeccion: 1, nombrePeriodo: '2025-II', fechaInicio: '2025-08-01', fechaFin: '2025-12-15' }),
    ]);

    expect(component.seccionesFiltradas()).toEqual([]);
    expect(component.totalSecciones()).toBe(0);
  });

  async function renderWithSecciones(secciones: SeccionResponse[]): Promise<void> {
    docenteRoleService.getSeccionesByDocente.mockResolvedValue(secciones);
    TestBed.inject(QueryClient).clear();
    fixture = TestBed.createComponent(MisCursos);
    component = fixture.componentInstance;
    await fixture.whenStable();
    await vi.waitFor(() => expect(component.secciones()).toEqual(secciones));
  }
});

function seccion(overrides: {
  idSeccion: number;
  nombrePeriodo: string;
  fechaInicio: string;
  fechaFin: string;
}): SeccionResponse {
  return {
    idSeccion: overrides.idSeccion,
    codigoSeccion: `SEC-${overrides.idSeccion}`,
    cicloAcademicoNombre: overrides.nombrePeriodo,
    vacantes: 30,
    curso: {
      idCurso: overrides.idSeccion,
      nombre: `Curso ${overrides.idSeccion}`,
      creditos: 4,
      descripcion: null,
    },
    cicloAcademico: {
      idCiclo: overrides.idSeccion,
      nombre: overrides.nombrePeriodo,
      fechaInicio: overrides.fechaInicio,
      fechaFin: overrides.fechaFin,
    },
  };
}
