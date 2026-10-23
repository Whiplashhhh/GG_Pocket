import { describe, expect, it } from 'vitest'

import { ascentOutcome, sessionDay } from './sessionView'

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
  })
})
