/** Dates as members read them, in French and in the club's time zone. */
const ZONE = 'Europe/Paris'

const DAY = new Intl.DateTimeFormat('fr-FR', {
  day: 'numeric',
  month: 'long',
  year: 'numeric',
  timeZone: ZONE,
})
const DAY_TIME = new Intl.DateTimeFormat('fr-FR', {
  day: 'numeric',
  month: 'long',
  hour: '2-digit',
  minute: '2-digit',
  timeZone: ZONE,
})
const RELATIVE = new Intl.RelativeTimeFormat('fr-FR', { numeric: 'auto' })

/** "12 octobre 2026". Accepts an instant or a calendar date (YYYY-MM-DD). */
export function formatDate(value: string): string {
  // A bare date is a calendar day: read it at noon UTC so no time zone moves it to another day.
  const date = /^\d{4}-\d{2}-\d{2}$/.test(value) ? new Date(`${value}T12:00:00Z`) : new Date(value)
  return DAY.format(date)
}

/** "12 octobre à 18:30". */
export function formatDateTime(value: string): string {
  return DAY_TIME.format(new Date(value))
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
