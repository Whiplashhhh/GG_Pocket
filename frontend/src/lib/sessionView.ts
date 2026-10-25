import type { Session } from '@/api/sessions'

import { clubToday } from './age'
import { formatDate } from './format'

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

/** How an ascent went, as an i18n key and its count of falls. */
export function ascentOutcome(ascent: Pick<Ascent, 'topped' | 'clean' | 'falls'>): {
  key: 'session.outcome.clean' | 'session.outcome.topped' | 'session.outcome.notTopped'
  falls: number
} {
  if (ascent.clean) return { key: 'session.outcome.clean', falls: 0 }
  return {
    key: ascent.topped ? 'session.outcome.topped' : 'session.outcome.notTopped',
    falls: ascent.falls,
  }
}
