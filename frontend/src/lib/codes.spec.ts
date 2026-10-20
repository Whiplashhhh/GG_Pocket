import { describe, expect, it } from 'vitest'

import { clubDayBefore, codeSettings, endOfClubDay } from './codes'

describe('endOfClubDay', () => {
  it('ends the day at midnight in Gravelines, summer and winter', () => {
    expect(endOfClubDay('2026-07-14')).toBe('2026-07-14T22:00:00.000Z')
    expect(endOfClubDay('2026-12-24')).toBe('2026-12-24T23:00:00.000Z')
  })

  it('reads back as the same day', () => {
    expect(clubDayBefore(endOfClubDay('2026-07-14'))).toBe('2026-07-14')
    expect(clubDayBefore(endOfClubDay('2026-12-24'))).toBe('2026-12-24')
  })
})

describe('codeSettings', () => {
  it('turns the form into settings', () => {
    expect(
      codeSettings({ label: ' Affiche ', expiresOn: '', maxUses: '30', active: true }),
    ).toEqual({ settings: { label: 'Affiche', expiresAt: undefined, maxUses: 30, active: true } })
  })

  it('refuses a missing label and a strange maximum', () => {
    expect(codeSettings({ label: '', expiresOn: '', maxUses: '0', active: true })).toEqual({
      errors: { label: { key: 'validation.required' }, maxUses: { key: 'validation.number' } },
    })
  })
})
