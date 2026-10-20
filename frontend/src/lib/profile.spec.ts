import { describe, expect, it } from 'vitest'

import type { MyProfile } from '@/api/me'

import { parseYear, toForm, toUpdate } from './profile'

const PROFILE: MyProfile = {
  id: '1',
  nickname: 'marie',
  firstName: 'Marie',
  lastName: 'Hhh',
  role: 'CLIMBER',
  showAge: false,
  parentalConsent: false,
  appearsInRankings: true,
  createdAt: '2026-10-01T10:00:00Z',
  clubArrivalYear: 2019,
}

describe('parseYear', () => {
  it('accepts a past year or nothing', () => {
    expect(parseYear(' 2019 ', 2026)).toBe(2019)
    expect(parseYear('', 2026)).toBeUndefined()
  })

  it('refuses anything else', () => {
    expect(parseYear('19', 2026)).toBe('invalid')
    expect(parseYear('2027', 2026)).toBe('invalid')
    expect(parseYear('1949', 2026)).toBe('invalid')
    expect(parseYear('deux mille', 2026)).toBe('invalid')
  })
})

describe('toUpdate', () => {
  it('sends the profile as edited, empty fields as absent', () => {
    const form = { ...toForm(PROFILE), bio: '  ', climbingSinceYear: '2015' }
    expect(toUpdate(form, 2026)).toEqual({
      update: {
        nickname: 'marie',
        firstName: 'Marie',
        lastName: 'Hhh',
        email: undefined,
        birthDate: undefined,
        parentalConsent: false,
        showAge: false,
        appearsInRankings: true,
        clubArrivalYear: 2019,
        climbingSinceYear: 2015,
        bio: undefined,
      },
    })
  })

  it('reports unreadable years', () => {
    expect(toUpdate({ ...toForm(PROFILE), clubArrivalYear: '3000' }, 2026)).toEqual({
      errors: { clubArrivalYear: { key: 'validation.year' } },
    })
  })
})
