import type { HorarioCreateRequest } from '../../../models/horario/horario.request';
import type { HorarioResponse } from '../../../models/horario/horario.response';

export const CALENDAR_DAYS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'] as const;
export const START_HOUR = 7;
export const END_HOUR = 22;
export const HOUR_HEIGHT = 64;

export type CalendarDay = (typeof CALENDAR_DAYS)[number];

export interface CalendarHorarioEvent {
  idHorario: number;
  dayIndex: number;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
  aula: string | null;
  idSeccion: number;
  title: string;
  subtitle: string;
  color: string;
  backgroundColor: string;
  top: number;
  height: number;
  source: HorarioResponse;
}

export interface CalendarEventFilters {
  sectionId?: number | null;
  dayIndex?: number | null;
  aula?: string;
}

export function parseTimeToMinutes(time: string): number {
  const [hours = '0', minutes = '0'] = time.split(':');
  return Number(hours) * 60 + Number(minutes);
}

export function formatMinutesAsTime(totalMinutes: number): string {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:00`;
}

export function addMinutesToTime(time: string, minutes: number): string {
  return formatMinutesAsTime(parseTimeToMinutes(time) + minutes);
}

export function getEventTop(horaInicio: string): number {
  const minutesFromStart = parseTimeToMinutes(horaInicio) - START_HOUR * 60;
  return Math.max(0, (minutesFromStart / 60) * HOUR_HEIGHT);
}

export function getEventHeight(horaInicio: string, horaFin: string): number {
  const duration = parseTimeToMinutes(horaFin) - parseTimeToMinutes(horaInicio);
  return Math.max(32, (duration / 60) * HOUR_HEIGHT);
}

export function dayNameToIndex(day: string): number {
  const normalized = day.toLocaleLowerCase('es-PE');
  return CALENDAR_DAYS.findIndex((candidate) => candidate.toLocaleLowerCase('es-PE') === normalized);
}

export function dayIndexToName(dayIndex: number): CalendarDay {
  return CALENDAR_DAYS[dayIndex] ?? CALENDAR_DAYS[0];
}

export function mapHorarioToCalendarEvent(horario: HorarioResponse): CalendarHorarioEvent {
  const color = horario.seccion.color ?? hslColorForSection(horario.seccion.idSeccion);

  return {
    idHorario: horario.idHorario,
    dayIndex: Math.max(0, dayNameToIndex(horario.diaSemana)),
    diaSemana: horario.diaSemana,
    horaInicio: horario.horaInicio,
    horaFin: horario.horaFin,
    aula: horario.aula,
    idSeccion: horario.seccion.idSeccion,
    title: horario.seccion.codigoSeccion,
    subtitle: horario.seccion.curso.nombre,
    color,
    backgroundColor: colorToSoftBackground(color),
    top: getEventTop(horario.horaInicio),
    height: getEventHeight(horario.horaInicio, horario.horaFin),
    source: horario,
  };
}

export function moveEventToSlot(
  event: CalendarHorarioEvent,
  dayIndex: number,
  startMinutes: number,
): HorarioCreateRequest {
  const duration = parseTimeToMinutes(event.horaFin) - parseTimeToMinutes(event.horaInicio);
  const horaInicio = formatMinutesAsTime(startMinutes);

  return {
    diaSemana: dayIndexToName(dayIndex),
    horaInicio,
    horaFin: addMinutesToTime(horaInicio, duration),
    aula: event.aula,
  };
}

export function uniqueEventSections(events: CalendarHorarioEvent[]): Array<{ id: number; label: string; color: string }> {
  const map = new Map<number, { id: number; label: string; color: string }>();
  for (const event of events) {
    map.set(event.idSeccion, { id: event.idSeccion, label: event.title, color: event.color });
  }
  return [...map.values()];
}

export function filterCalendarEvents(
  events: CalendarHorarioEvent[],
  filters: CalendarEventFilters,
): CalendarHorarioEvent[] {
  const aula = filters.aula?.trim().toLocaleLowerCase('es-PE') ?? '';

  return events.filter((event) => {
    const matchesSection = !filters.sectionId || event.idSeccion === filters.sectionId;
    const matchesDay = filters.dayIndex == null || event.dayIndex === filters.dayIndex;
    const matchesAula = !aula || (event.aula ?? '').toLocaleLowerCase('es-PE').includes(aula);
    return matchesSection && matchesDay && matchesAula;
  });
}

export function getCurrentWeekStart(): Date {
  const now = new Date();
  const day = now.getDay();
  const diff = now.getDate() - day + (day === 0 ? -6 : 1);
  const start = new Date(now.getFullYear(), now.getMonth(), diff);
  start.setHours(0, 0, 0, 0);
  return start;
}

export function getWeekLabel(weekStart: Date): string {
  const start = new Date(weekStart);
  const end = new Date(weekStart);
  end.setDate(end.getDate() + 4);
  const firstJan = new Date(start.getFullYear(), 0, 1);
  const days = Math.floor((start.getTime() - firstJan.getTime()) / 86400000);
  const weekNumber = Math.ceil((days + firstJan.getDay() + 1) / 7);
  const month = start.toLocaleDateString('es-PE', { month: 'long' });
  return `Semana ${weekNumber} — ${month} ${start.getDate()}–${end.getDate()}, ${start.getFullYear()}`;
}

export function addWeeks(date: Date, weeks: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + weeks * 7);
  return result;
}

export function isCurrentWeek(weekStart: Date): boolean {
  return weekStart.toDateString() === getCurrentWeekStart().toDateString();
}

function hslColorForSection(idSeccion: number): string {
  return `hsl(${(idSeccion * 47) % 360} 72% 44%)`;
}

function colorToSoftBackground(color: string): string {
  if (color.startsWith('#')) {
    return `${color}24`;
  }

  return color.replace(')', ' / 0.14)').replace('hsl(', 'hsl(');
}
