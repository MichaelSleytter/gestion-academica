import { isCurrentAcademicPeriod } from './academic-period';

describe('isCurrentAcademicPeriod', () => {
  it('includes the start and end dates', () => {
    const period = { fechaInicio: '2026-03-01', fechaFin: '2026-07-15' };

    expect(isCurrentAcademicPeriod(period, new Date(2026, 2, 1, 23))).toBe(true);
    expect(isCurrentAcademicPeriod(period, new Date(2026, 6, 15, 1))).toBe(true);
  });

  it.each([
    null,
    {},
    { fechaInicio: 'invalid', fechaFin: '2026-07-15' },
    { fechaInicio: '2026-02-30', fechaFin: '2026-07-15' },
    { fechaInicio: '2026-08-01', fechaFin: '2026-07-15' },
  ])('returns false for missing, malformed, or inverted dates: %o', (period) => {
    expect(isCurrentAcademicPeriod(period, new Date(2026, 6, 15))).toBe(false);
  });
});
