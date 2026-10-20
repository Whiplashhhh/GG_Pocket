import { describe, expect, it } from 'vitest'

import { formatDate, formatDateTime, formatRelative } from './format'

describe('formatDate', () => {
  it('writes dates the French way', () => {
    expect(formatDate('2026-10-12T08:00:00Z')).toBe('12 octobre 2026')
  })

  it('keeps a calendar date on its day whatever the time zone', () => {
    expect(formatDate('2011-03-01')).toBe('1 mars 2011')
  })

  it('uses the time in Gravelines', () => {
    // 23:30 UTC on 31 October is already 1 November in Paris (UTC+1).
    expect(formatDate('2026-10-31T23:30:00Z')).toBe('1 novembre 2026')
    expect(formatDateTime('2026-07-14T16:30:00Z')).toBe('14 juillet à 18:30')
  })
})

describe('formatRelative', () => {
  const now = Date.parse('2026-10-20T12:00:00Z')

  it('says how long ago', () => {
    expect(formatRelative('2026-10-17T12:00:00Z', now)).toBe('il y a 3 jours')
    expect(formatRelative('2026-10-20T11:00:00Z', now)).toBe('il y a 1 heure')
    expect(formatRelative('2026-10-20T11:59:50Z', now)).toBe('maintenant')
  })
})
