import type { RegistrationCode, RegistrationCodeSettings } from '@/api/admin'

import { clubToday } from './age'
import type { FieldErrors } from './forms'

/** A registration code as typed by the admin: an expiry day rather than an instant. */
export interface CodeForm {
  label: string
  /** YYYY-MM-DD; the code stops working at the end of that day in Gravelines. */
  expiresOn: string
  maxUses: string
  active: boolean
}

/** Midnight at the end of a day in Gravelines, as an instant. */
export function endOfClubDay(day: string): string {
  const [year = 0, month = 1, date = 1] = day.split('-').map(Number)
  // Try both offsets of Paris (UTC+1 in winter, UTC+2 in summer) and keep the one that lands on
  // midnight there.
  for (const offset of [1, 2]) {
    const candidate = new Date(Date.UTC(year, month - 1, date + 1, -offset))
    const local = new Intl.DateTimeFormat('en-GB', {
      timeZone: 'Europe/Paris',
      hour: '2-digit',
      hourCycle: 'h23',
    }).format(candidate)
    if (local === '00') {
      return candidate.toISOString()
    }
  }
  return new Date(Date.UTC(year, month - 1, date + 1)).toISOString()
}

/** The day (in Gravelines) just before an expiry instant: what the form shows. */
export function clubDayBefore(instant: string): string {
  return clubToday(new Date(new Date(instant).getTime() - 1))
}

export function toCodeForm(code: RegistrationCode | undefined): CodeForm {
  return {
    label: code?.label ?? '',
    expiresOn: code?.expiresAt ? clubDayBefore(code.expiresAt) : '',
    maxUses: code?.maxUses?.toString() ?? '',
    active: code?.active ?? true,
  }
}

export function codeSettings(
  form: CodeForm,
): { settings: RegistrationCodeSettings } | { errors: FieldErrors<keyof CodeForm> } {
  const errors: FieldErrors<keyof CodeForm> = {}
  if (form.label.trim() === '') {
    errors.label = { key: 'validation.required' }
  }
  const maxUses = form.maxUses.trim()
  if (maxUses !== '' && (!/^\d+$/.test(maxUses) || Number(maxUses) < 1)) {
    errors.maxUses = { key: 'validation.number' }
  }
  if (Object.keys(errors).length > 0) {
    return { errors }
  }
  return {
    settings: {
      label: form.label.trim(),
      expiresAt: form.expiresOn === '' ? undefined : endOfClubDay(form.expiresOn),
      maxUses: maxUses === '' ? undefined : Number(maxUses),
      active: form.active,
    },
  }
}
