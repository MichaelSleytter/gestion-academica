export interface AcademicPeriodDates {
  fechaInicio?: string | null;
  fechaFin?: string | null;
}

export function isCurrentAcademicPeriod(
  period: AcademicPeriodDates | null | undefined,
  today = new Date(),
): boolean {
  if (!period || Number.isNaN(today.getTime())) return false;

  const start = parseLocalDate(period.fechaInicio);
  const end = parseLocalDate(period.fechaFin);
  if (start === null || end === null || start > end) return false;

  const current = today.getFullYear() * 10_000 + (today.getMonth() + 1) * 100 + today.getDate();
  return start <= current && current <= end;
}

function parseLocalDate(value: string | null | undefined): number | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value ?? '');
  if (!match) return null;

  const [, yearText, monthText, dayText] = match;
  const year = Number(yearText);
  const month = Number(monthText);
  const day = Number(dayText);
  const date = new Date(year, month - 1, day);

  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return null;
  }

  return year * 10_000 + month * 100 + day;
}
