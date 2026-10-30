import { describe, expect, it } from 'vitest'

import type { RankingLine } from '@/api/rankings'

import {
  belowPodium,
  FORMER_MEMBER_ID,
  formatFreezeDay,
  isTie,
  podiumSteps,
  profileLink,
} from './podium'

function line(nickname: string, rank: number, value = 1): RankingLine {
  return { rank, value, member: { id: `id-${nickname}`, nickname } }
}

describe('podiumSteps', () => {
  it('stands the second on the left, the first in the middle and the third on the right', () => {
    const steps = podiumSteps([line('ana', 1), line('bob', 2), line('cyd', 3), line('dan', 4)])

    expect(steps.map((step) => step.line.member.nickname)).toEqual(['ana', 'bob', 'cyd'])
    expect(steps.map((step) => step.column)).toEqual([2, 1, 3])
    expect(steps.map((step) => step.height)).toEqual(['first', 'second', 'third'])
  })

  it('raises tied climbers to the same height', () => {
    const steps = podiumSteps([line('ana', 1), line('bob', 1), line('cyd', 3)])

    expect(steps.map((step) => step.height)).toEqual(['first', 'first', 'third'])
  })

  it('stands with fewer than three climbers', () => {
    expect(podiumSteps([line('ana', 1)]).map((step) => step.column)).toEqual([2])
    expect(podiumSteps([line('ana', 1), line('bob', 2)]).map((step) => step.column)).toEqual([2, 1])
    expect(podiumSteps([])).toEqual([])
  })
})

describe('belowPodium and isTie', () => {
  const ana = line('ana', 1)
  const dan = line('dan', 4)
  const lines = [ana, line('bob', 2), line('cyd', 3), dan, line('eve', 4)]

  it('lists everyone after the top three', () => {
    expect(belowPodium(lines).map((l) => l.member.nickname)).toEqual(['dan', 'eve'])
  })

  it('spots the shared ranks', () => {
    expect(isTie(lines, dan)).toBe(true)
    expect(isTie(lines, ana)).toBe(false)
    // My line comes apart from the list: the same member is no tie.
    expect(isTie(lines, { ...ana })).toBe(false)
  })

  it('spots a tie between two former members, who share one id', () => {
    const former = { id: FORMER_MEMBER_ID, nickname: 'Ancien·ne membre' }
    const both = [
      { rank: 1, value: 3, member: former },
      { rank: 1, value: 3, member: former },
    ]
    expect(isTie(both, both[1] ?? ana)).toBe(true)
  })
})

describe('profileLink', () => {
  it('opens my profile, a member page, or nothing for a former member', () => {
    expect(profileLink(line('ana', 1), 'id-ana')).toBe('/profil')
    expect(profileLink(line('ana', 1), 'id-bob')).toBe('/membres/id-ana')
    expect(
      profileLink({ rank: 1, value: 2, member: { id: FORMER_MEMBER_ID, nickname: 'x' } }, 'id'),
    ).toBeUndefined()
  })
})

describe('formatFreezeDay', () => {
  it('names the day in Gravelines time', () => {
    // 04:00 in Paris on 8 November 2026 is 03:00 UTC.
    expect(formatFreezeDay('2026-11-08T03:00:00Z')).toBe('dimanche 8 novembre')
  })
})
