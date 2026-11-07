import type { ClosedDay, ClosedDaysSettings } from '@/api/closedDays'

import { codePointLength, type FieldErrors } from './forms'

/** Same limits as the backend (ClosedDay, ClosedDayRange, D-116). */
export const CLOSED_DAY_LABEL_MAX = 60
export const CLOSED_RANGE_MAX_DAYS = 366

/** Days in a row with the same label: one line of the list, reopened in one go. */
export interface ClosedPeriod {
  /** YYYY-MM-DD, both included. */
  from: string
  to: string
  label?: string
  days: number
}

const DAY_MS = 86_400_000

function dayNumber(date: string): number {
  return Date.parse(`${date}T00:00:00Z`) / DAY_MS
}

/** The days from one date to another, both included (1 for the same day). */
export function daysBetween(from: string, to: string): number {
  return dayNumber(to) - dayNumber(from) + 1
}

/** Closed days, any order, as periods earliest first. */
export function closedPeriods(days: readonly ClosedDay[]): ClosedPeriod[] {
  const periods: ClosedPeriod[] = []
  for (const day of [...days].sort((a, b) => a.date.localeCompare(b.date))) {
    const last = periods.at(-1)
    if (last && last.label === day.label && dayNumber(day.date) === dayNumber(last.to) + 1) {
      last.to = day.date
      last.days++
    } else {
      periods.push({ from: day.date, to: day.date, label: day.label, days: 1 })
    }
  }
  return periods
}

/** Periods still to come (or going on today), and those over, the latest first. */
export function splitPeriods(
  periods: readonly ClosedPeriod[],
  today: string,
): { upcoming: ClosedPeriod[]; past: ClosedPeriod[] } {
  return {
    upcoming: periods.filter((period) => period.to >= today),
    past: periods.filter((period) => period.to < today).reverse(),
  }
}

/** The form to close days: the end day and the label may stay empty. */
export interface ClosureForm {
  from: string
  to: string
  label: string
}

export function emptyClosureForm(): ClosureForm {
  return { from: '', to: '', label: '' }
}

export function closureSettings(
  form: ClosureForm,
): { settings: ClosedDaysSettings } | { errors: FieldErrors<keyof ClosureForm> } {
  const errors: FieldErrors<keyof ClosureForm> = {}
  const label = form.label.trim()
  if (form.from === '') {
    errors.from = { key: 'validation.required' }
  } else if (form.to !== '' && form.to < form.from) {
    errors.to = { key: 'closedDays.toBeforeFrom' }
  } else if (form.to !== '' && daysBetween(form.from, form.to) > CLOSED_RANGE_MAX_DAYS) {
    errors.to = { key: 'closedDays.tooLong', params: { max: CLOSED_RANGE_MAX_DAYS } }
  }
  if (codePointLength(label) > CLOSED_DAY_LABEL_MAX) {
    errors.label = { key: 'validation.tooLong', params: { max: CLOSED_DAY_LABEL_MAX } }
  }
  if (Object.keys(errors).length > 0) return { errors }
  return {
    settings: {
      from: form.from,
      ...(form.to !== '' && form.to !== form.from ? { to: form.to } : {}),
      ...(label !== '' ? { label } : {}),
    },
  }
}

/** The closed days of a planning month, by date. */
export function closedByDate(days: readonly { date: string; label?: string }[]) {
  return new Map(days.map((day) => [day.date, day.label]))
}
