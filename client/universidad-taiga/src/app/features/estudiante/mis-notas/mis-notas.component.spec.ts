import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { of } from 'rxjs';

import { MisNotas } from './mis-notas.component';
import {
  EstudianteRoleService,
  type CursoMatriculadoResponse,
} from '../../../core/services/estudiante-role.service';
import { APP_API_URL } from '../../../core/tokens/api.tokens';
import {
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';

describe('MisNotas', () => {
  let component!: MisNotas;
  let fixture!: ComponentFixture<MisNotas>;
  let estudianteRoleService: { getMisCursos: ReturnType<typeof vi.fn> };
  let activatedRouteStub: ReturnType<typeof createActivatedRouteStub>;
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date(2026, 6, 15, 12));
    estudianteRoleService = { getMisCursos: vi.fn().mockResolvedValue([]) };
    activatedRouteStub = createActivatedRouteStub();
    router = { navigate: vi.fn().mockResolvedValue(true) };

    await TestBed.configureTestingModule({
      imports: [MisNotas],
      providers: [
        provideHttpClient(),
        provideAngularComponentTest(),
        provideQueryTestClient(),
        { provide: EstudianteRoleService, useValue: estudianteRoleService },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: Router, useValue: router },
        { provide: APP_API_URL, useValue: 'http://localhost:8080/api/v1' },
      ],
    }).compileComponents();
  });

  async function createComponent(): Promise<void> {
    fixture = TestBed.createComponent(MisNotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  afterEach(() => {
    vi.useRealTimers();
    TestBed.inject(QueryClient).clear();
  });

  it('should create', async () => {
    await createComponent();

    expect(component).toBeTruthy();
  });

  it('uses cycle dates for actual and preserves todos and named periods', async () => {
    const cursos = [
      curso(1, '2026-I', '2026-03-01', '2026-07-15'),
      curso(2, '2026-II', '2026-08-01', '2026-12-15'),
      curso(3, '2025-II', '2025-08-01', '2025-12-15'),
      curso(4, '2026-I', '2026-03-01', '2026-07-15', 'RETIRADA'),
    ];
    estudianteRoleService.getMisCursos.mockResolvedValue(cursos);
    await createComponent();
    await vi.waitFor(() => expect(component.cursosMatriculados()).toEqual(cursos));

    expect(component.cursosFiltrados().map((item) => item.idSeccion)).toEqual([1]);
    expect(component.cursosActivos()).toBe(1);

    component.periodoSeleccionado.set('todos');

    expect(component.cursosFiltrados().map((item) => item.idSeccion)).toEqual([1, 2, 3, 4]);

    component.periodoSeleccionado.set('2026-II');

    expect(component.cursosFiltrados().map((item) => item.idSeccion)).toEqual([2]);
  });

  it('falls back to actual when periodo query param is stale', async () => {
    activatedRouteStub.snapshot.queryParamMap = convertToParamMap({ periodo: '2024-I' });
    estudianteRoleService.getMisCursos.mockResolvedValue([
      curso(1, '2026-I', '2026-03-01', '2026-07-15'),
      curso(2, '2026-II', '2026-08-01', '2026-12-15'),
    ]);
    await createComponent();
    await vi.waitFor(() => expect(component.cursosMatriculados().length).toBe(2));

    expect(component.periodoSeleccionado()).toBe('actual');
    expect(component.cursosFiltrados().map((item) => item.idSeccion)).toEqual([1]);
    expect(router.navigate).toHaveBeenCalledWith([], expect.objectContaining({ queryParams: { periodo: null } }));
  });
});

function createActivatedRouteStub() {
  return {
    paramMap: of(convertToParamMap({})),
    url: of([]),
    snapshot: {
      paramMap: convertToParamMap({}),
      queryParamMap: convertToParamMap({}),
    },
  };
}

function curso(
  idSeccion: number,
  cicloAcademicoNombre: string,
  fechaInicio: string,
  fechaFin: string,
  estado = 'ACTIVA',
): CursoMatriculadoResponse {
  return {
    idMatricula: idSeccion,
    estado,
    idSeccion,
    codigoSeccion: `SEC-${idSeccion}`,
    cicloAcademicoNombre,
    fechaInicio,
    fechaFin,
    idCurso: idSeccion,
    nombreCurso: `Curso ${idSeccion}`,
    creditos: 4,
  };
}
