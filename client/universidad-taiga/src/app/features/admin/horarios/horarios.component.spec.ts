import { Component } from '@angular/core';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TuiRoot } from '@taiga-ui/core';
import { HorarioService } from '../../../core/services/horario.service';
import type { HorarioResponse } from '../../../models/horario/horario.response';
import {
  emptyPage,
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';

import { Horarios } from './horarios.component';

describe('Horarios', () => {
  let component: Horarios;
  let fixture: ComponentFixture<HorariosHost>;
  let horarioService: ReturnType<typeof createHorarioServiceMock>;

  beforeEach(async () => {
    horarioService = createHorarioServiceMock();

    await TestBed.configureTestingModule({
      imports: [HorariosHost],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: HorarioService, useValue: horarioService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HorariosHost);
    component = fixture.debugElement.query(By.directive(Horarios)).componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create and load section catalog data', () => {
    expect(component).toBeTruthy();
    expect(horarioService.getHorariosPaginado).toHaveBeenCalledWith(0, 10, '');
    expect(horarioService.getSeccionesList).toHaveBeenCalledOnce();
  });

  it('should render a weekly calendar with section-colored schedule events', async () => {
    await refreshFixture();
    await flushPromises();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Vista semanal');
    expect(fixture.nativeElement.textContent).toContain('MAT-I-001');
    expect(fixture.nativeElement.textContent).toContain('A-101');
  });

  it('should prefill the create form when an empty calendar slot is selected', () => {
    component.openCrearDesdeSlot(2, 9);

    expect(component.horarioModalAbierto()).toBe(true);
    expect(component.horarioForm.controls.diaSemana.value).toBe('Miércoles');
    expect(component.horarioForm.controls.horaInicio.value).toBe('09:00');
    expect(component.horarioForm.controls.horaFin.value).toBe('10:00');
  });

  it('should open edit mode when a calendar event is selected', () => {
    const horario = createHorario();

    component.openEditarHorarioModal(horario);

    expect(component.modoFormulario()).toBe('editar');
    expect(component.horarioSeleccionado()).toBe(horario);
    expect(component.horarioForm.controls.idSeccion.value).toBe(10);
  });

  it('should navigate to the previous and next week', () => {
    const initialStart = component.currentWeekStart();
    component.previousWeek();
    expect(component.currentWeekStart().getTime()).toBeLessThan(initialStart.getTime());
    component.nextWeek();
    expect(component.currentWeekStart().toDateString()).toBe(initialStart.toDateString());
  });

  it('should go to the current week', () => {
    component.currentWeekStart.set(new Date(2025, 0, 1));
    component.goToCurrentWeek();
    const now = new Date();
    expect(component.currentWeekStart().getFullYear()).toBe(now.getFullYear());
    expect(component.currentWeekStart().getMonth()).toBe(now.getMonth());
  });

  it('should disable the Today button when already on the current week', () => {
    expect(component.isCurrentWeek()).toBe(true);
  });

  it('should detect mobile viewport and show single day index by default', () => {
    component.isMobile.set(true);
    const visible = component.visibleCalendarDays();
    expect(visible.length).toBe(1); // single day when mobile
  });

  it('should update day and time when an event is dropped into another slot', () => {
    const mutateSpy = vi
      .spyOn(component.actualizarHorarioMutation, 'mutate')
      .mockImplementation(() => undefined as never);
    const event = component.toCalendarEvent(createHorario());

    component.moveCalendarEvent(event, 1, 11);

    expect(mutateSpy).toHaveBeenCalledWith(
      {
        id: 1,
        idSeccion: 10,
        horario: {
          diaSemana: 'Martes',
          horaInicio: '11:00:00',
          horaFin: '12:30:00',
          aula: 'A-101',
        },
      },
      expect.any(Object),
    );
  });

  async function refreshFixture(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }
});

function flushPromises(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve));
}

@Component({
  imports: [Horarios, TuiRoot],
  template: '<tui-root><app-horarios /></tui-root>',
})
class HorariosHost {}

function createHorarioServiceMock() {
  return {
    getHorariosPaginado: vi.fn().mockResolvedValue(pageWith([createHorario()])),
    getSeccionesList: vi.fn().mockResolvedValue(emptyPage()),
    crearHorario: vi.fn(),
    actualizarHorario: vi.fn(),
    eliminarHorario: vi.fn(),
  };
}

function createHorario(overrides: Partial<HorarioResponse & { color: string; idSeccion: number }> = {}): HorarioResponse {
  return {
    idHorario: overrides.idHorario ?? 1,
    diaSemana: overrides.diaSemana ?? 'Lunes',
    horaInicio: overrides.horaInicio ?? '08:30:00',
    horaFin: overrides.horaFin ?? '10:00:00',
    aula: overrides.aula ?? 'A-101',
    seccion: {
      idSeccion: overrides.idSeccion ?? 10,
      codigoSeccion: 'MAT-I-001',
      color: overrides.color ?? '#2563EB',
      cicloAcademicoNombre: '2026-I',
      vacantes: 30,
      curso: { idCurso: 1, nombre: 'Matemáticas', creditos: 4, descripcion: null },
      cicloAcademico: {
        idCiclo: 1,
        nombre: '2026-I',
        fechaInicio: '2026-01-01',
        fechaFin: '2026-06-30',
      },
    },
  };
}

function pageWith(content: HorarioResponse[]) {
  return {
    ...emptyPage<HorarioResponse>(),
    content,
    totalElements: content.length,
    totalPages: 1,
    numberOfElements: content.length,
    empty: content.length === 0,
  };
}
