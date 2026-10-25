import type { LocationQuery } from 'vue-router'

import { clubToday } from './age'
import { dateBounds } from './sessionForm'

/** A month as the API writes it: YYYY-MM. */
export type Month = string

const MONTH = /^(\d{4})-(0[1-9]|1[0-2])$/
/** The API knows nothing before (DECISIONS: planning month). */
const FIRST_MONTH: Month = '2000-01'

const MONTH_LABEL = new Intl.DateTimeFormat('fr-FR', {
  month: 'long',
  year: 'numeric',
  timeZone: 'UTC',
})

export function monthOf(day: string): Month {
  return day.slice(0, 7)
}

export function shiftMonth(month: Month, delta: number): Month {
  const [year = 0, monthNumber = 1] = month.split('-').map(Number)
  const date = new Date(Date.UTC(year, monthNumber - 1 + delta, 1))
  return date.toISOString().slice(0, 7)
}

/** The month in the URL (`?mois=2026-10`), this month when absent, malformed or in the future. */
export function parseMonthQuery(query: LocationQuery, today: string = clubToday()): Month {
  const value = query.mois
  const current = monthOf(today)
  if (typeof value !== 'string' || !MONTH.test(value)) return current
  return value > current || value < FIRST_MONTH ? current : value
}

export function canShowNext(month: Month, today: string = clubToday()): boolean {
  return month < monthOf(today)
}

export function canShowPrevious(month: Month): boolean {
  return month > FIRST_MONTH
}

/** Monday first: narrow ("L") and full ("lundi") names, from a known Monday (2024-01-01). */
export const WEEKDAYS: readonly { short: string; name: string }[] = Array.from(
  { length: 7 },
  (_, index) => {
    const day = new Date(Date.UTC(2024, 0, 1 + index, 12))
    const format = (weekday: 'narrow' | 'long') =>
      new Intl.DateTimeFormat('fr-FR', { weekday, timeZone: 'UTC' }).format(day)
    return { short: format('narrow'), name: format('long') }
  },
)

/** "octobre 2026". */
export function formatMonth(month: Month): string {
  return MONTH_LABEL.format(new Date(`${month}-01T12:00:00Z`))
}

/** A cell of the calendar: a day of the month, or null to pad the first and last weeks. */
export type CalendarCell = { date: string; day: number } | null

/** The weeks of a month, Monday first (spec §8.6), padded with null. */
export function calendarWeeks(month: Month): CalendarCell[][] {
  const first = new Date(`${month}-01T12:00:00Z`)
  // getUTCDay: 0 = Sunday. Monday first: Monday → 0 … Sunday → 6.
  const lead = (first.getUTCDay() + 6) % 7
  const [year = 0, monthNumber = 1] = month.split('-').map(Number)
  const length = new Date(Date.UTC(year, monthNumber, 0)).getUTCDate()
  const cells: CalendarCell[] = Array.from({ length: lead }, () => null)
  for (let day = 1; day <= length; day++) {
    cells.push({ date: `${month}-${String(day).padStart(2, '0')}`, day })
  }
  while (cells.length % 7 !== 0) cells.push(null)
  const weeks: CalendarCell[][] = []
  for (let start = 0; start < cells.length; start += 7) weeks.push(cells.slice(start, start + 7))
  return weeks
}

/** A session can still be logged on this day (today or up to a week back). */
export function canLogOn(day: string, today: string = clubToday()): boolean {
  const { min, max } = dateBounds(today)
  return day >= min && day <= max
}

/** "7,5" : the average feeling, French style. */
export function formatFeeling(value: number): string {
  return new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 }).format(value)
}
