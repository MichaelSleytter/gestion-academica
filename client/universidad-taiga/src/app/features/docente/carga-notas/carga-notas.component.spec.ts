import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { convertToParamMap, provideRouter } from '@angular/router';
import { provideTanStackQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { ActivatedRoute } from '@angular/router';
import { DocenteRoleService } from '../../../core/services/docente-role.service';

import { CargaNotas } from './carga-notas.component';

describe('CargaNotas', () => {
  let component: CargaNotas;
  let fixture: ComponentFixture<CargaNotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargaNotas],
      providers: [
        provideRouter([]),
        provideTanStackQuery(new QueryClient()),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '7' }) } },
        },
        {
          provide: DocenteRoleService,
          useValue: {
            getEvaluacionesBySeccion: vi.fn().mockResolvedValue([]),
            getMatriculasBySeccion: vi.fn().mockResolvedValue([]),
            getNotasByEvaluacion: vi.fn().mockResolvedValue([]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CargaNotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create with section id from route', () => {
    expect(component).toBeTruthy();
    expect(component.sectionId()).toBe(7);
  });

  it('should read student id from section enrollment DTO', () => {
    expect(
      component.getStudentId({
        idMatricula: 1,
        fechaMatricula: null,
        estado: 'ACTIVA',
        idEstudiante: 9,
      }),
    ).toBe(9);
  });

  it('should find existing notes by evaluation and student id from note DTO', () => {
    component.allNotesMap.set({ 3: [{ idNota: 5, nota: 18, idEstudiante: 9 }] });

    expect(component.findNotaByStudent(3, 9)?.idNota).toBe(5);
  });
});
