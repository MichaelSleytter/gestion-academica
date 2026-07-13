import { ChangeDetectionStrategy, Component, computed } from '@angular/core';
import { TuiLoader } from '@taiga-ui/core';
import { TuiCardLarge, TuiHeader } from '@taiga-ui/layout';
import { useMiHorarioQuery } from '../../../queries/estudiante-role.query';
import {
  CALENDAR_DAYS,
  END_HOUR,
  HOUR_HEIGHT,
  START_HOUR,
  type CalendarHorarioEvent,
  mapHorarioToCalendarEvent,
  uniqueEventSections,
} from '../../admin/horarios/calendar.helpers';

@Component({
  selector: 'app-horario-estudiante',
  imports: [TuiCardLarge, TuiHeader, TuiLoader],
  templateUrl: './horario-estudiante.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HorarioEstudiante {
  readonly horarioQuery = useMiHorarioQuery();
  readonly calendarDays = CALENDAR_DAYS;
  readonly calendarHours = Array.from({ length: END_HOUR - START_HOUR }, (_, index) => START_HOUR + index);
  readonly hourHeight = HOUR_HEIGHT;

  readonly calendarEvents = computed(() =>
    (this.horarioQuery.data() ?? []).map((horario) => mapHorarioToCalendarEvent(horario)),
  );
  readonly calendarLegend = computed(() => uniqueEventSections(this.calendarEvents()));
  readonly isLoading = computed(() => this.horarioQuery.isPending());
  readonly isError = computed(() => this.horarioQuery.isError());

  eventsForDay(dayIndex: number): CalendarHorarioEvent[] {
    return this.calendarEvents().filter((event) => event.dayIndex === dayIndex);
  }

  formatHour(hour: number): string {
    return `${String(hour).padStart(2, '0')}:00`;
  }
}
