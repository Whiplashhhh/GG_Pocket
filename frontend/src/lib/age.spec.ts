import { describe, expect, it } from 'vitest'

import { clubToday, needsParentalConsent, yearsOld } from './age'

describe('age rules', () => {
  it('counts whole years', () => {
    expect(yearsOld('2011-10-21', '2026-10-20')).toBe(14)
    expect(yearsOld('2011-10-21', '2026-10-21')).toBe(15)
    expect(yearsOld('2012-02-29', '2027-02-28')).toBe(14)
  })

  it('asks for a parental consent until the fifteenth birthday', () => {
    expect(needsParentalConsent('2011-10-21', '2026-10-20')).toBe(true)
    expect(needsParentalConsent('2011-10-21', '2026-10-21')).toBe(false)
    expect(needsParentalConsent('', '2026-10-21')).toBe(false)
  })

  it('takes today in Gravelines', () => {
    expect(clubToday(new Date('2026-10-20T22:30:00Z'))).toBe('2026-10-21')
  })
})
