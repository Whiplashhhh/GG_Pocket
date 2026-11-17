import type { Session } from '@/api/sessions'

import { clubToday } from './age'
import { formatDate, formatDayMonth, formatTime } from './format'

type Ascent = Session['ascents'][number]

/** "aujourd'hui", "hier", or the date: a session is a day at the gym, not an instant. */
export function sessionDay(
  climbedOn: string,
  today: string = clubToday(),
): { kind: 'today' | 'yesterday' } | { kind: 'date'; date: string } {
  if (climbedOn === today) return { kind: 'today' }
  const yesterday = new Date(`${today}T12:00:00Z`)
  yesterday.setUTCDate(yesterday.getUTCDate() - 1)
  if (climbedOn === yesterday.toISOString().slice(0, 10)) return { kind: 'yesterday' }
  return { kind: 'date', date: formatDate(climbedOn) }
}

/**
 * When the session went up in the feed (club feedback, 2026-11-16, « comme sur Strava »). The app
 * knows the save time, not the climbing time: saved on the day climbed, the time alone ("19 h 42");
 * saved later, the day it was saved too, so nobody reads a time on a day it did not happen.
 */
export function sessionPublished(
  climbedOn: string,
  createdAt: string,
  today: string = clubToday(),
):
  | { kind: 'sameDay'; time: string }
  | { kind: 'today' | 'yesterday'; time: string }
  | { kind: 'date'; date: string; time: string } {
  const time = formatTime(createdAt)
  const savedOn = clubToday(new Date(createdAt))
  if (savedOn === climbedOn) return { kind: 'sameDay', time }
  const day = sessionDay(savedOn, today)
  return day.kind === 'date'
    ? { kind: 'date', date: formatDayMonth(savedOn), time }
    : { kind: day.kind, time }
}

/** How an ascent went, as an i18n key and its count of falls. */
export function ascentOutcome(
  ascent: Pick<Ascent, 'topped' | 'clean' | 'falls'> & Partial<Pick<Ascent, 'pastHalf'>>,
): {
  key:
    | 'session.outcome.clean'
    | 'session.outcome.topped'
    | 'session.outcome.pastHalf'
    | 'session.outcome.notTopped'
  falls: number
} {
  if (ascent.clean) return { key: 'session.outcome.clean', falls: 0 }
  if (ascent.topped) return { key: 'session.outcome.topped', falls: ascent.falls }
  return {
    key: ascent.pastHalf === true ? 'session.outcome.pastHalf' : 'session.outcome.notTopped',
    falls: ascent.falls,
  }
}
