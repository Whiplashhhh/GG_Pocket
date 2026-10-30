import { describe, expect, it } from 'vitest'

import type { Session } from '@/api/sessions'
import type { DirectoryEntry } from '@/api/social'

import { belayerChoices, sessionBelayers } from './belayers'

const entry = (
  nickname: string,
  firstName: string,
  friendship: DirectoryEntry['friendship'],
): DirectoryEntry => ({
  member: { id: nickname, nickname, firstName, lastName: 'Martin', role: 'CLIMBER' },
  friendship,
})

const club = [
  entry('ad', 'Adèle', 'NONE'),
  entry('bo', 'Bob', 'FRIENDS'),
  entry('ce', 'Céline', 'REQUESTED'),
  entry('du', 'Élodie', 'FRIENDS'),
]

describe('belayerChoices', () => {
  it('puts my friends first, then the rest of the club, in directory order', () => {
    const { friends, others } = belayerChoices(club, '')
    expect(friends.map((member) => member.nickname)).toEqual(['bo', 'du'])
    expect(others.map((member) => member.nickname)).toEqual(['ad', 'ce'])
  })

  it('searches everyone, accents aside', () => {
    const { friends, others } = belayerChoices(club, 'elo')
    expect(friends.map((member) => member.nickname)).toEqual(['du'])
    expect(others).toEqual([])
  })
})

describe('sessionBelayers', () => {
  it('names each belayer once, in the order of the routes', () => {
    const tom = { id: 't', nickname: 'tom' }
    const lea = { id: 'l', nickname: 'léa' }
    const session = {
      ascents: [{ belayer: tom }, {}, { belayer: lea }, { belayer: tom }],
    } as unknown as Session
    expect(sessionBelayers(session)).toEqual([tom, lea])
  })
})
