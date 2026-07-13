import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { EvaluacionService } from '../../../core/services/evaluacion.service';
import {
  emptyPage,
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';

import { Evaluaciones } from './evaluaciones.component';

describe('Evaluaciones', () => {
  let component: Evaluaciones;
  let fixture: ComponentFixture<Evaluaciones>;
  let evaluacionService: ReturnType<typeof createEvaluacionServiceMock>;

  beforeEach(async () => {
    evaluacionService = createEvaluacionServiceMock();

    await TestBed.configureTestingModule({
      imports: [Evaluaciones],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: EvaluacionService, useValue: evaluacionService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Evaluaciones);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create and load section catalog data', () => {
    expect(component).toBeTruthy();
    expect(evaluacionService.getEvaluacionesPaginado).toHaveBeenCalledWith(0, 10, '');
    expect(evaluacionService.getCursosList).toHaveBeenCalledOnce();
    expect(evaluacionService.getSeccionesList).toHaveBeenCalledOnce();
  });
});

function createEvaluacionServiceMock() {
  return {
    getEvaluacionesPaginado: vi.fn().mockResolvedValue(emptyPage()),
    getCursosList: vi.fn().mockResolvedValue(emptyPage()),
    getSeccionesList: vi.fn().mockResolvedValue(emptyPage()),
    crearEvaluacion: vi.fn(),
    actualizarEvaluacion: vi.fn(),
    eliminarEvaluacion: vi.fn(),
  };
}
