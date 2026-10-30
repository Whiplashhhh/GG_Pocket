import { describe, expect, it } from 'vitest'

import type { Session } from '@/api/sessions'

import {
  addAscent,
  dateBounds,
  duplicateAscent,
  emptySessionForm,
  formProblems,
  moveAscent,
  removeAscent,
  removeLastOf,
  requestToForm,
  restoredForm,
  sessionToForm,
  setClean,
  setFalls,
  setTopped,
  toSessionRequest,
  withoutGoneRoutes,
  type AscentLine,
} from './sessionForm'

const NEVER = new Set<string>()

function line(overrides: Partial<AscentLine> = {}): AscentLine {
  return {
    key: 'k',
    routeId: 'r1',
    mode: 'TOP_ROPE',
    topped: true,
    clean: false,
    falls: 0,
    firstTry: false,
    feeling: null,
    belayerId: null,
    ...overrides,
  }
}

describe('adding a route', () => {
  it('ticks "first try" only for a route never climbed, nor higher in the session', () => {
    const first = addAscent([], 'r1', NEVER)
    expect(first[0]).toMatchObject({ routeId: 'r1', topped: true, clean: false, firstTry: true })
    const again = addAscent(first, 'r1', NEVER)
    expect(again[1]?.firstTry).toBe(false)
    expect(addAscent([], 'r2', new Set(['r2']))[0]?.firstTry).toBe(false)
  })

  it('keeps the mode of the line before, top rope to start with', () => {
    const lines = addAscent([line({ mode: 'LEAD' })], 'r2', NEVER)
    expect(lines[1]?.mode).toBe('LEAD')
    expect(addAscent([], 'r2', NEVER)[0]?.mode).toBe('TOP_ROPE')
  })

  it('keeps the belayer of the line before, nobody to start with', () => {
    const lines = addAscent([line({ belayerId: 'm1' })], 'r2', NEVER)
    expect(lines[1]?.belayerId).toBe('m1')
    expect(addAscent([], 'r2', NEVER)[0]?.belayerId).toBeNull()
    expect(duplicateAscent(lines, 1)[2]?.belayerId).toBe('m1')
  })

  it('stops at 50 lines', () => {
    const full = Array.from({ length: 50 }, (_, i) => line({ key: String(i) }))
    expect(addAscent(full, 'r9', NEVER)).toHaveLength(50)
    expect(duplicateAscent(full, 0)).toHaveLength(50)
  })
})

describe('the rules of a line', () => {
  it('a clean ascent is topped, with 0 falls locked', () => {
    const clean = setClean(line({ falls: 3 }), true)
    expect(clean).toMatchObject({ clean: true, falls: 0 })
    expect(setFalls(clean, 2).falls).toBe(0)
    expect(setClean(line({ topped: false }), true).clean).toBe(false)
  })

  it('not topped is never clean', () => {
    expect(setTopped(line({ clean: true }), false)).toMatchObject({ topped: false, clean: false })
    expect(setTopped(line({ topped: false }), true).clean).toBe(false)
  })

  it('falls stay between 0 and 99', () => {
    expect(setFalls(line(), -1).falls).toBe(0)
    expect(setFalls(line(), 120).falls).toBe(99)
    expect(setFalls(line(), 2.7).falls).toBe(2)
  })
})

describe('arranging lines', () => {
  const lines = [line({ key: 'a' }), line({ key: 'b', firstTry: true }), line({ key: 'c' })]

  it('duplicates right after, never as a first try', () => {
    const copied = duplicateAscent(lines, 1)
    expect(copied.map((item) => item.key.length > 1 || item.key)).toEqual(['a', 'b', true, 'c'])
    expect(copied[2]?.firstTry).toBe(false)
  })

  it('moves up and down, not past the ends', () => {
    expect(moveAscent(lines, 2, -1).map((item) => item.key)).toEqual(['a', 'c', 'b'])
    expect(moveAscent(lines, 0, -1).map((item) => item.key)).toEqual(['a', 'b', 'c'])
    expect(moveAscent(lines, 2, 1).map((item) => item.key)).toEqual(['a', 'b', 'c'])
  })

  it('removes a line', () => {
    expect(removeAscent(lines, 1).map((item) => item.key)).toEqual(['a', 'c'])
  })

  it('takes back the last line of a route only', () => {
    const tries = [
      line({ key: 'a', routeId: 'r1' }),
      line({ key: 'b', routeId: 'r2' }),
      line({ key: 'c', routeId: 'r1' }),
    ]
    expect(removeLastOf(tries, 'r1').map((item) => item.key)).toEqual(['a', 'b'])
    expect(removeLastOf(tries, 'r3').map((item) => item.key)).toEqual(['a', 'b', 'c'])
  })
})

describe('the date', () => {
  it('goes back 7 days', () => {
    expect(dateBounds('2026-10-03')).toEqual({ min: '2026-09-26', max: '2026-10-03' })
  })

  it('is checked with the other problems', () => {
    const form = { ...emptySessionForm('2026-10-20'), climbedOn: '2026-10-10' }
    expect(formProblems(form, dateBounds('2026-10-20'))).toEqual(['noAscent', 'dateOutOfRange'])
    form.ascents = [line()]
    form.climbedOn = '2026-10-20'
    form.comment = 'x'.repeat(501)
    expect(formProblems(form, dateBounds('2026-10-20'))).toEqual(['commentTooLong'])
  })
})

describe('what is sent', () => {
  it('leaves out what is empty and sends 0 falls for a clean ascent', () => {
    const form = {
      ...emptySessionForm('2026-10-20'),
      comment: '  Grosse séance  ',
      ascents: [line({ clean: true, falls: 0, feeling: 7 }), line({ topped: false, falls: 2 })],
    }
    const body = toSessionRequest(form)
    expect(body).toMatchObject({
      climbedOn: '2026-10-20',
      comment: 'Grosse séance',
      visibility: 'FRIENDS',
    })
    expect(body).not.toHaveProperty('feeling')
    expect(body.ascents[0]).toEqual({
      routeId: 'r1',
      mode: 'TOP_ROPE',
      topped: true,
      clean: true,
      falls: 0,
      firstTry: false,
      feeling: 7,
    })
    expect(body.ascents[1]).not.toHaveProperty('feeling')
    expect(requestToForm(body)).toMatchObject({
      clientRef: form.clientRef,
      comment: 'Grosse séance',
    })
  })

  it('sends the belayer only when there is one, and gets it back', () => {
    const form = {
      ...emptySessionForm('2026-10-20'),
      ascents: [line({ belayerId: 'm1' }), line({ key: 'k2' })],
    }
    const body = toSessionRequest(form)
    expect(body.ascents[0]?.belayerId).toBe('m1')
    expect(body.ascents[1]).not.toHaveProperty('belayerId')
    expect(requestToForm(body).ascents.map((item) => item.belayerId)).toEqual(['m1', null])
  })

  it('a draft from before belayers gets none', () => {
    const old: Omit<AscentLine, 'belayerId'> = {
      key: 'k',
      routeId: 'r1',
      mode: 'TOP_ROPE',
      topped: true,
      clean: false,
      falls: 0,
      firstTry: false,
      feeling: null,
    }
    expect(restoredForm({ ...emptySessionForm(), ascents: [old] }).ascents[0]?.belayerId).toBeNull()
  })

  it('a saved session comes back into the form with a new clientRef', () => {
    const session = {
      id: 's1',
      climbedOn: '2026-10-19',
      feeling: 8,
      visibility: 'PRIVATE',
      createdAt: '2026-10-19T18:00:00Z',
      editable: true,
      summary: { ascentCount: 1, toppedCount: 1, leadCount: 0 },
      ascents: [
        {
          id: 'a1',
          route: { id: 'r7' },
          mode: 'LEAD',
          topped: true,
          clean: true,
          falls: 0,
          firstTry: true,
          belayer: { id: 'm1', nickname: 'tom' },
        },
      ],
    } as unknown as Session
    const form = sessionToForm(session)
    expect(form).toMatchObject({
      climbedOn: '2026-10-19',
      feeling: 8,
      comment: '',
      visibility: 'PRIVATE',
    })
    expect(form.ascents[0]).toMatchObject({
      routeId: 'r7',
      mode: 'LEAD',
      clean: true,
      feeling: null,
      belayerId: 'm1',
    })
  })
})

describe('a draft kept while the wall changed', () => {
  it('drops the lines on a route gone since, and says how many', () => {
    const form = {
      ...emptySessionForm('2026-10-20'),
      ascents: [line({ key: 'a', routeId: 'r1' }), line({ key: 'b', routeId: 'gone' })],
    }
    const kept = withoutGoneRoutes(form, new Set(['r1']))
    expect(kept.dropped).toBe(1)
    expect(kept.form.ascents.map((ascent) => ascent.key)).toEqual(['a'])
    expect(withoutGoneRoutes(kept.form, new Set(['r1']))).toEqual({ form: kept.form, dropped: 0 })
  })
})
