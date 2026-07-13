import {
  addMinutesToTime,
  addWeeks,
  filterCalendarEvents,
  getCurrentWeekStart,
  getEventHeight,
  getEventTop,
  getWeekLabel,
  isCurrentWeek,
  mapHorarioToCalendarEvent,
  moveEventToSlot,
} from './calendar.helpers';
import type { HorarioResponse } from '../../../models/horario/horario.response';

describe('calendar helpers', () => {
  it('should calculate event position and height from HH:mm:ss times', () => {
    expect(getEventTop('08:30:00')).toBe(96);
    expect(getEventHeight('08:30:00', '10:00:00')).toBe(96);
  });

  it('should clamp invalid or out-of-range event positions', () => {
    expect(getEventTop('06:00:00')).toBe(0);
    expect(getEventHeight('09:00:00', '08:30:00')).toBe(32);
  });

  it('should map backend horarios to calendar events with section colors', () => {
    const event = mapHorarioToCalendarEvent(createHorario({ color: '#2563EB' }));

    expect(event.dayIndex).toBe(0);
    expect(event.color).toBe('#2563EB');
    expect(event.title).toBe('MAT-I-001');
    expect(event.subtitle).toBe('Matemáticas');
  });

  it('should move an event to a new day and keep its duration', () => {
    const moved = moveEventToSlot(mapHorarioToCalendarEvent(createHorario()), 2, 600);

    expect(moved.diaSemana).toBe('Miércoles');
    expect(moved.horaInicio).toBe('10:00:00');
    expect(moved.horaFin).toBe('11:30:00');
  });

  it('should produce a Monday-starting current week', () => {
    const start = getCurrentWeekStart();
    expect(start.getDay()).toBe(1); // Monday
  });

  it('should produce a human-readable week label', () => {
    const label = getWeekLabel(new Date(2026, 0, 5)); // Mon Jan 5 2026
    expect(label).toContain('Enero');
    expect(label).toContain('2026');
  });

  it('should add weeks correctly', () => {
    const date = new Date(2026, 0, 5); // Monday Jan 5
    const result = addWeeks(date, 2);
    expect(result.getDate()).toBe(19);
  });

  it('should detect the current week', () => {
    expect(isCurrentWeek(getCurrentWeekStart())).toBe(true);
  });

  it('should add minutes to a time and filter by section, day, and classroom', () => {
    const events = [
      mapHorarioToCalendarEvent(createHorario({ idHorario: 1, aula: 'A-101' })),
      mapHorarioToCalendarEvent(createHorario({ idHorario: 2, diaSemana: 'Martes', idSeccion: 20, aula: 'B-202' })),
    ];

    expect(addMinutesToTime('21:45:00', 30)).toBe('22:15:00');
    expect(filterCalendarEvents(events, { sectionId: 20, dayIndex: 1, aula: 'b-2' })).toEqual([events[1]]);
  });
});

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
      color: overrides.color,
      cicloAcademicoNombre: '2026-I',
      vacantes: 30,
      curso: {
        idCurso: 1,
        nombre: 'Matemáticas',
        creditos: 4,
        descripcion: null,
      },
      cicloAcademico: {
        idCiclo: 1,
        nombre: '2026-I',
        fechaInicio: '2026-01-01',
        fechaFin: '2026-06-30',
      },
    },
  };
}
