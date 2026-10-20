/** Dates as members read them, in French and in the club's time zone. */
const ZONE = 'Europe/Paris'

const DAY = new Intl.DateTimeFormat('fr-FR', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: ZONE,
})
const DAY_MONTH = new Intl.DateTimeFormat('fr-FR', {
  day: 'numeric',
  month: 'long',
  timeZone: ZONE,
})
const HOUR_MINUTE = new Intl.DateTimeFormat('en-GB', {
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
  timeZone: ZONE,
})
const RELATIVE = new Intl.RelativeTimeFormat('fr-FR', { numeric: 'auto' })

/** "12 octobre 2026". Accepts an instant or a calendar date (YYYY-MM-DD). */
export function formatDate(value: string): string {
  // A bare date is a calendar day: read it at noon UTC so no time zone moves it to another day.
  const date = /^\d{4}-\d{2}-\d{2}$/.test(value) ? new Date(`${value}T12:00:00Z`) : new Date(value)
  return DAY.format(date)
}

/** "12 octobre à 18 h 30", "12 octobre à 10 h": the French way of writing a time. */
export function formatDateTime(value: string): string {
  const date = new Date(value)
  const [hours = '0', minutes = '00'] = HOUR_MINUTE.format(date).split(':')
  const time =
    minutes === '00'
      ? `${String(Number(hours))}\u00a0h`
      : `${String(Number(hours))}\u00a0h\u00a0${minutes}`
  return `${DAY_MONTH.format(date)} à ${time}`
}

const UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
  ['year', 365 * 24 * 3600],
  ['month', 30 * 24 * 3600],
  ['week', 7 * 24 * 3600],
  ['day', 24 * 3600],
  ['hour', 3600],
  ['minute', 60],
]

/** "il y a 3 jours", "à l’instant". */
export function formatRelative(value: string, now: number = Date.now()): string {
  const seconds = Math.round((new Date(value).getTime() - now) / 1000)
  for (const [unit, size] of UNITS) {
    if (Math.abs(seconds) >= size) {
      return RELATIVE.format(Math.trunc(seconds / size), unit)
    }
  }
  return RELATIVE.format(0, 'second')
}
