import { describe, expect, it } from 'vitest'

import { ascentOutcome, sessionDay, sessionPublished } from './sessionView'

describe('sessionDay', () => {
  it('says today and yesterday, the date otherwise', () => {
    expect(sessionDay('2026-10-20', '2026-10-20')).toEqual({ kind: 'today' })
    expect(sessionDay('2026-10-19', '2026-10-20')).toEqual({ kind: 'yesterday' })
    expect(sessionDay('2026-09-30', '2026-10-01')).toEqual({ kind: 'yesterday' })
    expect(sessionDay('2026-10-01', '2026-10-20')).toEqual({
      kind: 'date',
      date: '1er octobre 2026',
    })
  })
})

describe('ascentOutcome', () => {
  it('tells clean, topped and not topped apart, with the falls', () => {
    expect(ascentOutcome({ topped: true, clean: true, falls: 0 })).toEqual({
      key: 'session.outcome.clean',
      falls: 0,
    })
    expect(ascentOutcome({ topped: true, clean: false, falls: 2 })).toEqual({
      key: 'session.outcome.topped',
      falls: 2,
    })
    expect(ascentOutcome({ topped: false, clean: false, falls: 1 }).key).toBe(
      'session.outcome.notTopped',
    )
    expect(ascentOutcome({ topped: false, pastHalf: true, clean: false, falls: 1 })).toEqual({
      key: 'session.outcome.pastHalf',
      falls: 1,
    })
  })
})

describe('sessionPublished', () => {
  it('gives the time alone when the session was saved on the day climbed (Paris time)', () => {
    // 22:30 UTC on 19 October is 0:30 on the 20th in Paris.
    expect(sessionPublished('2026-10-20', '2026-10-19T22:30:00Z', '2026-10-21')).toEqual({
      kind: 'sameDay',
      time: '0\u00a0h\u00a030',
    })
    expect(sessionPublished('2026-10-20', '2026-10-20T17:42:00Z', '2026-10-20')).toEqual({
      kind: 'sameDay',
      time: '19\u00a0h\u00a042',
    })
  })

  it('names the day of saving when the session was logged later', () => {
    expect(sessionPublished('2026-10-19', '2026-10-20T07:05:00Z', '2026-10-20')).toEqual({
      kind: 'today',
      time: '9\u00a0h\u00a005',
    })
    expect(sessionPublished('2026-10-18', '2026-10-19T19:00:00Z', '2026-10-20')).toEqual({
      kind: 'yesterday',
      time: '21\u00a0h',
    })
    expect(sessionPublished('2026-10-01', '2026-10-03T08:00:00Z', '2026-10-20')).toEqual({
      kind: 'date',
      date: '3 octobre',
      time: '10\u00a0h',
    })
  })
})
