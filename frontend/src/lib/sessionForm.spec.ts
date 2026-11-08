import { describe, expect, it } from 'vitest'

import type { Session } from '@/api/sessions'

import {
  addAscent,
  belayerForNewLine,
  dateBounds,
  duplicateAscent,
  emptySessionForm,
  formProblems,
  moveAscent,
  visibilityChoices,
  visibilityHintKey,
  removeAscent,
  removeLastOf,
  requestToForm,
  restoredForm,
  sameBelayer,
  sessionToForm,
  setClean,
  setFalls,
  setManyBelayers,
  setPastHalf,
  setSessionBelayer,
  setTopped,
  toSessionRequest,
  withoutGoneRoutes,
  type AscentLine,
  type SessionForm,
} from './sessionForm'

const NEVER = new Set<string>()

function line(overrides: Partial<AscentLine> = {}): AscentLine {
  return {
    key: 'k',
    routeId: 'r1',
    mode: 'TOP_ROPE',
    topped: true,
    pastHalf: false,
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

  it('takes the belayer it is given', () => {
    expect(
      addAscent([line({ belayerId: 'm1' })], 'r2', NEVER, { belayerId: 'm2' })[1],
    ).toMatchObject({ belayerId: 'm2', mode: 'TOP_ROPE' })
    expect(addAscent([], 'r2', NEVER, { belayerId: null, mode: 'LEAD' })[0]?.mode).toBe('LEAD')
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

  it('only a route not topped is past its half, and topping it again unticks the box', () => {
    expect(setPastHalf(line({ topped: false }), true).pastHalf).toBe(true)
    expect(setPastHalf(line(), true).pastHalf).toBe(false)
    const half = setPastHalf(setTopped(line(), false), true)
    expect(setTopped(half, true)).toMatchObject({ topped: true, pastHalf: false })
    expect(setTopped(half, false).pastHalf).toBe(true)
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
      pastHalf: false,
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

  it('sends « Plus de la moitié » only for a route not topped, and gets it back', () => {
    const form = {
      ...emptySessionForm('2026-10-20'),
      ascents: [line({ topped: false, pastHalf: true, falls: 1 }), line({ pastHalf: true })],
    }
    const body = toSessionRequest(form)
    expect(body.ascents.map((item) => item.pastHalf)).toEqual([true, false])
    expect(requestToForm(body).ascents.map((item) => item.pastHalf)).toEqual([true, false])
  })

  it('a draft from before « Plus de la moitié » is not past the half', () => {
    const old: Omit<AscentLine, 'pastHalf'> & { pastHalf?: boolean } = line({ topped: false })
    delete old.pastHalf
    const restored = restoredForm({ ...emptySessionForm(), ascents: [old] })
    expect(restored.ascents[0]?.pastHalf).toBe(false)
  })

  it('a draft from before belayers gets none', () => {
    const old: Omit<AscentLine, 'belayerId' | 'pastHalf'> = {
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
      belayerId: 'm1',
      manyBelayers: false,
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

describe('the belayer of the session (D-118)', () => {
  const form = (overrides: Partial<SessionForm> = {}): SessionForm => ({
    ...emptySessionForm('2026-10-20'),
    ...overrides,
  })

  it('names one belayer for every line, and for the lines added after', () => {
    const named = setSessionBelayer(
      form({ belayerId: 'm1', ascents: [line({ belayerId: 'm1' }), line({ key: 'k2' })] }),
      'm2',
    )
    expect(named.belayerId).toBe('m2')
    expect(named.ascents.map((item) => item.belayerId)).toEqual(['m2', 'm2'])
    expect(belayerForNewLine(form({ belayerId: 'm2' }))).toBe('m2')
    expect(belayerForNewLine(named)).toBe('m2')
    expect(setSessionBelayer(named, null).ascents.map((item) => item.belayerId)).toEqual([
      null,
      null,
    ])
  })

  it('line by line, a new line takes the belayer of the line before, else the session one', () => {
    const many = form({
      belayerId: 'm1',
      manyBelayers: true,
      ascents: [line({ belayerId: 'm1' }), line({ key: 'k2', belayerId: 'm3' })],
    })
    expect(belayerForNewLine(many)).toBe('m3')
    expect(belayerForNewLine({ ...many, ascents: [] })).toBe('m1')
  })

  it('ticking "Plusieurs assureur·ses" keeps every line as it is', () => {
    const one = form({ belayerId: 'm1', ascents: [line({ belayerId: 'm1' })] })
    expect(setManyBelayers(one, true)).toEqual({ ...one, manyBelayers: true })
  })

  it('unticking it gives the session, and every line, the belayer of the first line', () => {
    const many = form({
      belayerId: 'm9',
      manyBelayers: true,
      ascents: [line({ belayerId: 'm2' }), line({ key: 'k2', belayerId: 'm3' })],
    })
    const one = setManyBelayers(many, false)
    expect(one).toMatchObject({ belayerId: 'm2', manyBelayers: false })
    expect(one.ascents.map((item) => item.belayerId)).toEqual(['m2', 'm2'])
    expect(setManyBelayers({ ...many, ascents: [] }, false)).toMatchObject({
      belayerId: 'm9',
      manyBelayers: false,
    })
  })

  it('tells whether all lines share their belayer', () => {
    expect(sameBelayer([])).toBe(true)
    expect(sameBelayer([line(), line({ key: 'k2' })])).toBe(true)
    expect(sameBelayer([line({ belayerId: 'm1' }), line({ key: 'k2', belayerId: 'm1' })])).toBe(
      true,
    )
    expect(sameBelayer([line({ belayerId: 'm1' }), line({ key: 'k2' })])).toBe(false)
  })

  it('a session taken back ticks the box only when its lines differ', () => {
    const body = (belayers: (string | undefined)[]) =>
      toSessionRequest(
        form({
          ascents: belayers.map((belayerId, i) =>
            line({ key: String(i), belayerId: belayerId ?? null }),
          ),
        }),
      )
    expect(requestToForm(body(['m1', 'm1']))).toMatchObject({
      belayerId: 'm1',
      manyBelayers: false,
    })
    expect(requestToForm(body([undefined]))).toMatchObject({ belayerId: null, manyBelayers: false })
    expect(requestToForm(body(['m1', undefined]))).toMatchObject({ manyBelayers: true })
  })

  it('a draft keeps its box, ticked anyway when its lines differ, even from before D-118', () => {
    const { clientRef, climbedOn, feeling, comment, visibility } = form()
    const old = { clientRef, climbedOn, feeling, comment, visibility }
    expect(
      restoredForm({ ...old, ascents: [line({ belayerId: 'm1' }), line({ key: 'k2' })] }),
    ).toMatchObject({ belayerId: null, manyBelayers: true })
    expect(restoredForm({ ...old, ascents: [line({ belayerId: 'm1' })] })).toMatchObject({
      belayerId: 'm1',
      manyBelayers: false,
    })
    expect(
      restoredForm(
        form({ belayerId: 'm1', manyBelayers: true, ascents: [line({ belayerId: 'm1' })] }),
      ),
    ).toMatchObject({ belayerId: 'm1', manyBelayers: true })
    expect(restoredForm(form({ belayerId: 'm4' }))).toMatchObject({
      belayerId: 'm4',
      manyBelayers: false,
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

describe('visibility (D-120)', () => {
  it('offers « Tout le club » to a member of the club feed only', () => {
    expect(visibilityChoices(true, undefined, 'FRIENDS')).toEqual(['PRIVATE', 'FRIENDS', 'CLUB'])
    expect(visibilityChoices(false, undefined, 'FRIENDS')).toEqual(['PRIVATE', 'FRIENDS'])
    expect(visibilityChoices(false, 'FRIENDS', 'PRIVATE')).toEqual(['PRIVATE', 'FRIENDS'])
  })

  it('keeps « Tout le club » on screen for a session that has it, after leaving the club feed', () => {
    expect(visibilityChoices(false, 'CLUB', 'FRIENDS')).toEqual(['PRIVATE', 'FRIENDS', 'CLUB'])
    expect(visibilityChoices(false, undefined, 'CLUB')).toEqual(['PRIVATE', 'FRIENDS', 'CLUB'])
  })

  it('says what « Tout le club » means, or that it no longer reaches the club', () => {
    expect(visibilityHintKey('CLUB', true)).toBe('session.visibilityClubHint')
    expect(visibilityHintKey('CLUB', false)).toBe('session.visibilityClubLeft')
    expect(visibilityHintKey('FRIENDS', false)).toBe('session.visibilityFriendsHint')
    expect(visibilityHintKey('PRIVATE', true)).toBe('session.visibilityPrivateHint')
  })
})
