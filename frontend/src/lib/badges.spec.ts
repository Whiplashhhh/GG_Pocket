import { describe, expect, it } from 'vitest'

import type { BadgeLibraryEntry } from '@/api/badges'

import { badgeFamily, badgeGlyph, badgeMark, badgeSections, type BadgeCode } from './badges'

const entry = (code: BadgeCode, count = 0): BadgeLibraryEntry => ({
  code,
  family: badgeFamily(code),
  repeatable: badgeFamily(code) === 'REPEATABLE',
  count,
})

describe('badgeFamily', () => {
  it('reads the family of a tier from its code', () => {
    expect(badgeFamily('TOPPED_250')).toBe('TOPPED')
    expect(badgeFamily('LEAD_1')).toBe('LEAD')
    expect(badgeFamily('GRADE_6A')).toBe('FIRST_GRADE')
    expect(badgeFamily('DAYS_1000')).toBe('CLIMBING_DAYS')
  })

  it('files every other badge as repeatable', () => {
    expect(badgeFamily('WEEK_2')).toBe('REPEATABLE')
    expect(badgeFamily('MONTH_NO_MISS')).toBe('REPEATABLE')
    expect(badgeFamily('FLASH')).toBe('REPEATABLE')
  })
})

describe('badgeGlyph', () => {
  it('draws one thing per tier family', () => {
    expect(badgeGlyph('TOPPED_1')).toBe('topped')
    expect(badgeGlyph('TOPPED_1000')).toBe('topped')
    expect(badgeGlyph('LEAD_50')).toBe('lead')
    expect(badgeGlyph('GRADE_7C')).toBe('grade')
    expect(badgeGlyph('DAYS_10')).toBe('days')
  })

  it('draws each repeatable badge, weeks and months sharing theirs', () => {
    expect(badgeGlyph('WEEK_3')).toBe('week')
    expect(badgeGlyph('WEEK_4')).toBe('week')
    expect(badgeGlyph('MONTH_COMPLETE')).toBe('month')
    expect(badgeGlyph('MONTH_12')).toBe('month')
    expect(badgeGlyph('COMEBACK')).toBe('comeback')
    expect(badgeGlyph('RECORD')).toBe('record')
  })
})

describe('badgeMark', () => {
  it('marks a tier with its threshold, a grade in lower case', () => {
    expect(badgeMark('TOPPED_250')).toBe('250')
    expect(badgeMark('DAYS_10')).toBe('10')
    expect(badgeMark('GRADE_6B')).toBe('6b')
  })

  it('marks only the repeatable badges that share a drawing', () => {
    expect(badgeMark('WEEK_2')).toBe('2')
    expect(badgeMark('MONTH_COMPLETE')).toBe('10')
    expect(badgeMark('MONTH_12')).toBe('12')
    expect(badgeMark('FLASH')).toBeUndefined()
  })
})

describe('badgeSections', () => {
  it('groups the library by family in the catalogue order, counting the earned ones', () => {
    const sections = badgeSections([
      entry('FLASH', 3),
      entry('TOPPED_1', 1),
      entry('TOPPED_5'),
      entry('GRADE_5B', 1),
    ])

    expect(sections.map((section) => section.family)).toEqual([
      'TOPPED',
      'FIRST_GRADE',
      'REPEATABLE',
    ])
    expect(sections[0]?.entries.map((e) => e.code)).toEqual(['TOPPED_1', 'TOPPED_5'])
    expect(sections.map((section) => section.earned)).toEqual([1, 1, 1])
  })
})
