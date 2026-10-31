import { describe, expect, it } from 'vitest'

import type { BadgeLibraryEntry } from '@/api/badges'
import { mountWithApp } from '@/test/render'

import BadgeLibrary from './BadgeLibrary.vue'

const entries: BadgeLibraryEntry[] = [
  {
    code: 'TOPPED_1',
    family: 'TOPPED',
    repeatable: false,
    count: 1,
    firstEarnedOn: '2026-10-02',
    lastEarnedOn: '2026-10-02',
  },
  { code: 'TOPPED_5', family: 'TOPPED', repeatable: false, count: 0 },
  {
    code: 'GRADE_6A',
    family: 'FIRST_GRADE',
    repeatable: false,
    count: 1,
    firstEarnedOn: '2026-10-01',
    lastEarnedOn: '2026-10-01',
  },
  {
    code: 'WEEK_2',
    family: 'REPEATABLE',
    repeatable: true,
    count: 3,
    firstEarnedOn: '2026-09-01',
    lastEarnedOn: '2026-10-13',
  },
  { code: 'COMEBACK', family: 'REPEATABLE', repeatable: true, count: 0 },
]

describe('BadgeLibrary', () => {
  const wrapper = mountWithApp(BadgeLibrary, { props: { entries, idPrefix: 'badges' } })
  const items = wrapper.findAll('li')

  it('names each tier and says whether it is earned', () => {
    expect(items[0]?.text()).toContain('1 voie topée : débloqué le 2 octobre 2026')
    expect(items[1]?.text()).toContain(
      '5 voies topées : pas encore débloqué. Toper 5 voies, tous modes confondus.',
    )
    expect(items[2]?.text()).toContain('Premier 6a : débloqué le 1er octobre 2026')
  })

  it('shows the marks of the tiers under their medals', () => {
    expect(items[1]?.text()).toMatch(/^5/)
    expect(items[2]?.text()).toMatch(/^6a/)
  })

  it('counts the repeatable badges and gives their last time', () => {
    expect(items[3]?.text()).toContain('×3')
    expect(items[3]?.text()).toContain('Dernière fois le 13 octobre 2026')
    expect(items[3]?.attributes('data-earned')).toBe('true')
  })

  it('tells what an unearned badge takes, without a count', () => {
    expect(items[4]?.text()).toContain('après au moins 30 jours sans')
    expect(items[4]?.text()).not.toContain('×')
    expect(items[4]?.attributes('data-earned')).toBe('false')
  })

  it('titles each family with its earned count', () => {
    expect(wrapper.get('#badges-TOPPED').text()).toMatch(/Voies topées\s+1\/2/)
  })
})
