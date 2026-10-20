import { describe, expect, it } from 'vitest'

import type { MemberSummary } from '@/api/admin'

import { filterMembers, sortMembers } from './members'

const member = (
  id: string,
  firstName: string,
  lastName: string,
  overrides: Partial<MemberSummary> = {},
): MemberSummary => ({
  id,
  firstName,
  lastName,
  nickname: firstName.toLowerCase(),
  role: 'CLIMBER',
  status: 'ACTIVE',
  registeredAt: '2026-01-01T10:00:00Z',
  ...overrides,
})

const MEMBERS = [
  member('1', 'Élodie', 'Zébulon', { role: 'ADMIN', lastSeenAt: '2026-10-19T10:00:00Z' }),
  member('2', 'Marc', 'Abel', { status: 'DISABLED', nickname: 'tonton.talon' }),
  member('3', 'Léa', 'Dupont', { role: 'SETTER', lastSeenAt: '2026-10-20T10:00:00Z' }),
]

describe('filterMembers', () => {
  it('finds a member by any part of the name or nickname, accents aside', () => {
    expect(filterMembers(MEMBERS, 'elodie').map((m) => m.id)).toEqual(['1'])
    expect(filterMembers(MEMBERS, 'TALON').map((m) => m.id)).toEqual(['2'])
    expect(filterMembers(MEMBERS, 'lea dup').map((m) => m.id)).toEqual(['3'])
    expect(filterMembers(MEMBERS, '  ')).toHaveLength(3)
  })
})

describe('sortMembers', () => {
  it('sorts by last name by default, the French way', () => {
    expect(sortMembers(MEMBERS, 'name', 'asc').map((m) => m.lastName)).toEqual([
      'Abel',
      'Dupont',
      'Zébulon',
    ])
  })

  it('sorts roles by rank and puts members never seen last', () => {
    expect(sortMembers(MEMBERS, 'role', 'desc').map((m) => m.role)).toEqual([
      'ADMIN',
      'SETTER',
      'CLIMBER',
    ])
    expect(sortMembers(MEMBERS, 'lastSeenAt', 'desc').map((m) => m.id)).toEqual(['3', '1', '2'])
  })
})
