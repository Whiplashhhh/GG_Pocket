import { describe, expect, it } from 'vitest'

import {
  closedPeriods,
  closureSettings,
  daysBetween,
  emptyClosureForm,
  splitPeriods,
} from './closedDays'

describe('closedPeriods', () => {
  it('joins days in a row with the same label, across months and years', () => {
    const periods = closedPeriods([
      { date: '2027-01-02', label: 'Noël' },
      { date: '2026-12-31', label: 'Noël' },
      { date: '2027-01-01', label: 'Noël' },
      { date: '2027-05-01' },
    ])
    expect(periods).toEqual([
      { from: '2026-12-31', to: '2027-01-02', label: 'Noël', days: 3 },
      { from: '2027-05-01', to: '2027-05-01', label: undefined, days: 1 },
    ])
  })

  it('keeps apart a gap or another label', () => {
    const periods = closedPeriods([
      { date: '2026-12-24', label: 'Noël' },
      { date: '2026-12-26', label: 'Noël' },
      { date: '2026-12-27', label: 'Travaux' },
    ])
    expect(periods.map((period) => [period.from, period.to])).toEqual([
      ['2026-12-24', '2026-12-24'],
      ['2026-12-26', '2026-12-26'],
      ['2026-12-27', '2026-12-27'],
    ])
  })
})

describe('splitPeriods', () => {
  it('keeps a period going on today with those to come, the past latest first', () => {
    const periods = closedPeriods([
      { date: '2026-01-01' },
      { date: '2026-05-01', label: 'Mai' },
      { date: '2026-11-06', label: 'Pont' },
      { date: '2026-11-07', label: 'Pont' },
      { date: '2026-12-25' },
    ])
    const { upcoming, past } = splitPeriods(periods, '2026-11-07')
    expect(upcoming.map((period) => period.from)).toEqual(['2026-11-06', '2026-12-25'])
    expect(past.map((period) => period.from)).toEqual(['2026-05-01', '2026-01-01'])
  })
})

describe('daysBetween', () => {
  it('counts both ends, over a change of hour', () => {
    expect(daysBetween('2026-03-28', '2026-03-30')).toBe(3)
    expect(daysBetween('2026-07-01', '2026-07-01')).toBe(1)
  })
})

describe('closureSettings', () => {
  it('sends a single day without an end, and a trimmed label', () => {
    expect(closureSettings({ from: '2026-12-25', to: '', label: '  Noël ' })).toEqual({
      settings: { from: '2026-12-25', label: 'Noël' },
    })
    expect(closureSettings({ from: '2026-12-25', to: '2026-12-25', label: '' })).toEqual({
      settings: { from: '2026-12-25' },
    })
  })

  it('sends a range', () => {
    expect(closureSettings({ from: '2026-07-01', to: '2026-08-31', label: 'Été' })).toEqual({
      settings: { from: '2026-07-01', to: '2026-08-31', label: 'Été' },
    })
  })

  it('wants a first day, an end after it, a year at most and a short label', () => {
    expect(closureSettings(emptyClosureForm())).toEqual({
      errors: { from: { key: 'validation.required' } },
    })
    expect(closureSettings({ from: '2026-07-02', to: '2026-07-01', label: '' })).toEqual({
      errors: { to: { key: 'closedDays.toBeforeFrom' } },
    })
    expect(closureSettings({ from: '2026-01-01', to: '2027-01-02', label: '' })).toEqual({
      errors: { to: { key: 'closedDays.tooLong', params: { max: 366 } } },
    })
    expect(closureSettings({ from: '2026-01-01', to: '', label: 'x'.repeat(61) })).toEqual({
      errors: { label: { key: 'validation.tooLong', params: { max: 60 } } },
    })
  })
})
