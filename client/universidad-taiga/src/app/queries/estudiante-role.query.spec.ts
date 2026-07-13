import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { QueryClient } from '@tanstack/angular-query-experimental';

import {
  EstudianteRoleService,
  type CursoMatriculadoResponse,
} from '../core/services/estudiante-role.service';
import type { HorarioResponse } from '../models/horario/horario.response';
import { provideQueryTestClient } from '../testing/angular-test-providers';
import { useMiHorarioQuery } from './estudiante-role.query';

describe('useMiHorarioQuery', () => {
  let service: {
    getMisCursos: ReturnType<typeof vi.fn>;
    getHorariosBySeccion: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(new Date(2026, 6, 15, 12));

    service = {
      getMisCursos: vi.fn(),
      getHorariosBySeccion: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [HorarioQueryHost],
      providers: [
        provideQueryTestClient(),
        { provide: EstudianteRoleService, useValue: service },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.useRealTimers();
    TestBed.inject(QueryClient).clear();
  });

  it('requests horarios only for active courses in the current period', async () => {
    const currentHorario = horario(1, '2026-I', '2026-03-01', '2026-07-15');
    service.getMisCursos.mockResolvedValue([
      curso(1, '2026-I', '2026-03-01', '2026-07-15'),
      curso(2, '2025-II', '2025-08-01', '2025-12-15'),
      curso(3, '2026-II', '2026-08-01', '2026-12-15'),
    ]);
    service.getHorariosBySeccion.mockResolvedValue([currentHorario]);

    const fixture = TestBed.createComponent(HorarioQueryHost);
    await fixture.whenStable();

    await vi.waitFor(() =>
      expect(fixture.componentInstance.query.data()?.map((item) => item.idHorario)).toEqual([1]),
    );
    expect(service.getHorariosBySeccion).toHaveBeenCalledOnce();
    expect(service.getHorariosBySeccion).toHaveBeenCalledWith(1);
  });
});

@Component({ template: '' })
class HorarioQueryHost {
  readonly query = useMiHorarioQuery();
}

function curso(
  idSeccion: number,
  cicloAcademicoNombre: string,
  fechaInicio: string,
  fechaFin: string,
): CursoMatriculadoResponse {
  return {
    idMatricula: idSeccion,
    estado: 'ACTIVA',
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

function horario(
  idHorario: number,
  cicloAcademicoNombre: string,
  fechaInicio: string,
  fechaFin: string,
): HorarioResponse {
  return {
    idHorario,
    diaSemana: 'Lunes',
    horaInicio: '09:00:00',
    horaFin: '11:00:00',
    aula: 'Aula 1',
    seccion: {
      idSeccion: idHorario,
      codigoSeccion: `SEC-${idHorario}`,
      cicloAcademicoNombre,
      vacantes: 30,
      curso: {
        idCurso: idHorario,
        nombre: `Curso ${idHorario}`,
        creditos: 4,
        descripcion: null,
      },
      cicloAcademico: {
        idCiclo: idHorario,
        nombre: cicloAcademicoNombre,
        fechaInicio,
        fechaFin,
      },
    },
  };
}
