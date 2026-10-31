import { describe, expect, it } from 'vitest'

import type { BadgeLibraryEntry } from '@/api/badges'

import {
  badgeOf,
  badgeStates,
  isMaxLevel,
  levelOfCode,
  nextProgress,
  nextTier,
  rarity,
  rarityColor,
  RARITY_COLORS,
  recentBadges,
  shownFavorites,
  type BadgeCode,
  unlocks,
} from './badges'

const TOPPED: BadgeCode[] = [
  'TOPPED_1',
  'TOPPED_5',
  'TOPPED_10',
  'TOPPED_20',
  'TOPPED_50',
  'TOPPED_100',
  'TOPPED_250',
  'TOPPED_500',
  'TOPPED_750',
  'TOPPED_1000',
]
const GRADES: BadgeCode[] = [
  'GRADE_5B',
  'GRADE_5C',
  'GRADE_6A',
  'GRADE_6B',
  'GRADE_6C',
  'GRADE_7A',
  'GRADE_7B',
  'GRADE_7C',
]

const entry = (
  code: BadgeCode,
  count = 0,
  firstEarnedOn?: string,
  lastEarnedOn = firstEarnedOn,
): BadgeLibraryEntry => ({
  code,
  family: 'REPEATABLE',
  repeatable: true,
  count,
  ...(firstEarnedOn === undefined ? {} : { firstEarnedOn, lastEarnedOn }),
})

/** Six topped (two tiers), a 6a (three tiers), the flash three times. */
const library: BadgeLibraryEntry[] = [
  ...TOPPED.map((code, index) =>
    index < 2 ? entry(code, 1, '2026-09-0' + String(index + 1)) : entry(code),
  ),
  ...GRADES.map((code, index) => (index < 3 ? entry(code, 1, '2026-10-02') : entry(code))),
  entry('FLASH', 3, '2026-08-01', '2026-10-12'),
  entry('COMEBACK'),
]

describe('badgeOf', () => {
  it('folds the tiers of a family into one badge', () => {
    expect(badgeOf('TOPPED_250')).toBe('TOPPED')
    expect(badgeOf('LEAD_1')).toBe('LEAD')
    expect(badgeOf('GRADE_6A')).toBe('FIRST_GRADE')
    expect(badgeOf('DAYS_1000')).toBe('CLIMBING_DAYS')
  })

  it('keeps a repeatable badge as it is', () => {
    expect(badgeOf('WEEK_2')).toBe('WEEK_2')
    expect(badgeOf('MONTH_NO_MISS')).toBe('MONTH_NO_MISS')
  })
})

describe('rarity', () => {
  it('colours a tier family by its level, the same for every family', () => {
    expect(rarity('TOPPED', 3)).toBe(3)
    expect(rarityColor('FIRST_GRADE', 3)).toBe(rarityColor('TOPPED', 3))
    expect(rarityColor('TOPPED', 10)).toBe(RARITY_COLORS[9])
  })

  it('colours a repeatable badge by the times earned, gold for good after 100', () => {
    expect(rarity('FLASH', 1)).toBe(1)
    expect(rarity('FLASH', 4)).toBe(3)
    expect(rarity('FLASH', 5)).toBe(4)
    expect(rarity('FLASH', 99)).toBe(9)
    expect(rarity('FLASH', 100)).toBe(10)
    expect(rarity('FLASH', 340)).toBe(10)
  })

  it('leaves a badge not earned without colour', () => {
    expect(rarity('LEAD', 0)).toBe(0)
    expect(rarityColor('COMEBACK', 0)).toBeUndefined()
  })

  it('knows the last level of each family', () => {
    expect(isMaxLevel('FIRST_GRADE', 8)).toBe(true)
    expect(isMaxLevel('TOPPED', 8)).toBe(false)
    expect(isMaxLevel('FLASH', 500)).toBe(false)
  })
})

describe('badgeStates', () => {
  const states = badgeStates(library)
  const of = (badge: string) => states.find((state) => state.badge === badge)

  it('lists every badge once, in the catalogue order', () => {
    expect(states).toHaveLength(17)
    expect(states.slice(0, 4).map((state) => state.badge)).toEqual([
      'TOPPED',
      'LEAD',
      'FIRST_GRADE',
      'CLIMBING_DAYS',
    ])
  })

  it('counts the tiers earned as the level, the times earned for a repeatable badge', () => {
    expect(of('TOPPED')?.level).toBe(2)
    expect(of('FIRST_GRADE')?.level).toBe(3)
    expect(of('FLASH')?.level).toBe(3)
    expect(of('COMEBACK')?.level).toBe(0)
  })

  it('dates a badge by its latest level', () => {
    expect(of('TOPPED')?.lastEarnedOn).toBe('2026-09-02')
    expect(of('FLASH')).toMatchObject({ firstEarnedOn: '2026-08-01', lastEarnedOn: '2026-10-12' })
  })

  it('gives the three badges earned last, latest first', () => {
    expect(recentBadges(states, 3).map((state) => state.badge)).toEqual([
      'FLASH',
      'FIRST_GRADE',
      'TOPPED',
    ])
  })
})

describe('nextProgress', () => {
  const states = badgeStates(library)
  const of = (badge: string) => states.find((state) => state.badge === badge)
  const progress = { topped: 6, lead: 0, climbingDays: 3 }

  it('counts toward the next tier from zero, not from the last tier', () => {
    const topped = of('TOPPED')
    if (topped === undefined) throw new Error('No TOPPED')
    expect(nextTier(topped)).toBe('TOPPED_10')
    expect(nextProgress(topped, progress)).toEqual({ value: 6, target: 10 })
  })

  it('has no bar for the grades, nor without my progress', () => {
    const grade = of('FIRST_GRADE')
    const topped = of('TOPPED')
    if (grade === undefined || topped === undefined) throw new Error('Missing badges')
    expect(nextTier(grade)).toBe('GRADE_6B')
    expect(nextProgress(grade, progress)).toBeUndefined()
    expect(nextProgress(topped, undefined)).toBeUndefined()
  })
})

describe('shownFavorites', () => {
  it('shows the favourites at their level, leaving out the ones lost since', () => {
    expect(shownFavorites({ badges: library, favorites: ['FLASH', 'COMEBACK', 'TOPPED'] })).toEqual(
      [
        { badge: 'FLASH', level: 3 },
        { badge: 'TOPPED', level: 2 },
      ],
    )
  })
})

describe('unlocks', () => {
  it('reads the level of a tier from its code', () => {
    expect(levelOfCode('TOPPED_1')).toBe(1)
    expect(levelOfCode('TOPPED_10')).toBe(3)
    expect(levelOfCode('GRADE_7C')).toBe(8)
    expect(levelOfCode('DAYS_25')).toBe(2)
    expect(levelOfCode('FLASH')).toBe(1)
  })

  it('folds the tiers of a family into the highest, a first 7a included', () => {
    expect(
      unlocks(['GRADE_5B', 'GRADE_5C', 'GRADE_6A', 'GRADE_6B', 'GRADE_6C', 'GRADE_7A']),
    ).toEqual([{ badge: 'FIRST_GRADE', level: 6, times: 1 }])
  })

  it('counts a repeatable badge earned twice by the same session', () => {
    expect(unlocks(['FLASH', 'TOPPED_5', 'FLASH', 'COMEBACK'])).toEqual([
      { badge: 'TOPPED', level: 2, times: 1 },
      { badge: 'COMEBACK', level: 1, times: 1 },
      { badge: 'FLASH', level: 1, times: 2 },
    ])
  })
})
