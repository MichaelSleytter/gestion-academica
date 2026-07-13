import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTanStackQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { of } from 'rxjs';
import { HistorialProgresoService } from '../../../core/services/historial-progreso.service';

import { Historial } from './historial.component';

const progresoMock = {
  estudiante: {
    id: 10,
    codigo: 'EST-001',
    nombres: 'Ana',
    apellidos: 'Torres',
  },
  carrera: {
    id: 3,
    nombre: 'Ingeniería de Sistemas',
    creditosTotales: 42,
  },
  resumen: {
    totalCursos: 2,
    cursosAprobados: 1,
    cursosEnProgreso: 1,
    cursosPendientes: 0,
    creditosAprobados: 4,
    creditosRestantes: 38,
    promedioPonderado: 15.2,
    porcentajeAvance: 9.52,
  },
  cursos: [],
};

describe('Historial', () => {
  let component: Historial;
  let fixture: ComponentFixture<Historial>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Historial],
      providers: [
        provideTanStackQuery(new QueryClient()),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of({ get: () => null }) },
        },
        {
          provide: HistorialProgresoService,
          useValue: {
            getMiProgreso: vi.fn().mockResolvedValue(progresoMock),
            getProgresoEstudiante: vi.fn().mockResolvedValue(progresoMock),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Historial);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should format null numeric values as dash', () => {
    expect(component.formatNumber(null)).toBe('—');
  });
});
