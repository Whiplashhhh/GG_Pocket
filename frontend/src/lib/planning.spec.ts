import { describe, expect, it } from 'vitest'

import {
  calendarWeeks,
  canLogOn,
  canShowNext,
  canShowPrevious,
  formatFeeling,
  formatMonth,
  parseMonthQuery,
  shiftMonth,
  WEEKDAYS,
} from './planning'

const TODAY = '2026-10-25'

describe('planning', () => {
  it('moves from month to month across years', () => {
    expect(shiftMonth('2026-10', 1)).toBe('2026-11')
    expect(shiftMonth('2026-12', 1)).toBe('2027-01')
    expect(shiftMonth('2026-01', -1)).toBe('2025-12')
  })

  it('reads the month from the URL, this month otherwise', () => {
    expect(parseMonthQuery({ mois: '2026-09' }, TODAY)).toBe('2026-09')
    expect(parseMonthQuery({}, TODAY)).toBe('2026-10')
    expect(parseMonthQuery({ mois: '2026-11' }, TODAY)).toBe('2026-10')
    expect(parseMonthQuery({ mois: '2026-13' }, TODAY)).toBe('2026-10')
    expect(parseMonthQuery({ mois: '1999-12' }, TODAY)).toBe('2026-10')
    expect(parseMonthQuery({ mois: ['2026-09'] }, TODAY)).toBe('2026-10')
  })

  it('never goes into the future', () => {
    expect(canShowNext('2026-09', TODAY)).toBe(true)
    expect(canShowNext('2026-10', TODAY)).toBe(false)
    expect(canShowPrevious('2000-01')).toBe(false)
    expect(canShowPrevious('2000-02')).toBe(true)
  })

  it('names the days Monday first', () => {
    expect(WEEKDAYS.map((day) => day.short).join('')).toBe('LMMJVSD')
    expect(WEEKDAYS[0]?.name).toBe('lundi')
    expect(WEEKDAYS[6]?.name).toBe('dimanche')
  })

  it('names the month in French', () => {
    expect(formatMonth('2026-10')).toBe('octobre 2026')
  })

  it('lays out the weeks Monday first', () => {
    // October 2026 starts on a Thursday and has 31 days.
    const weeks = calendarWeeks('2026-10')
    expect(weeks).toHaveLength(5)
    expect(weeks[0]?.slice(0, 3)).toEqual([null, null, null])
    expect(weeks[0]?.[3]).toEqual({ date: '2026-10-01', day: 1 })
    expect(weeks[4]?.[5]).toEqual({ date: '2026-10-31', day: 31 })
    expect(weeks[4]?.[6]).toBeNull()
    expect(weeks.every((week) => week.length === 7)).toBe(true)
  })

  it('handles a month starting on a Monday and February', () => {
    // June 2026 starts on a Monday.
    expect(calendarWeeks('2026-06')[0]?.[0]).toEqual({ date: '2026-06-01', day: 1 })
    const february = calendarWeeks('2028-02')
      .flat()
      .filter((cell) => cell !== null)
    expect(february).toHaveLength(29)
  })

  it('lets me log a session up to a week back', () => {
    expect(canLogOn('2026-10-25', TODAY)).toBe(true)
    expect(canLogOn('2026-10-18', TODAY)).toBe(true)
    expect(canLogOn('2026-10-17', TODAY)).toBe(false)
    expect(canLogOn('2026-10-26', TODAY)).toBe(false)
  })

  it('writes the average feeling the French way', () => {
    expect(formatFeeling(7.5)).toBe('7,5')
    expect(formatFeeling(8)).toBe('8')
  })
})
