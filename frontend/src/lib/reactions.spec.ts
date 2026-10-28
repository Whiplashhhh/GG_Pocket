import { describe, expect, it } from 'vitest'

import { nextReaction, reactionTotal, withMyReaction, type SessionReactions } from './reactions'

const reactions = (fire: number, strong: number, mine?: 'FIRE' | 'STRONG'): SessionReactions => ({
  counts: [
    { kind: 'FIRE', count: fire },
    { kind: 'STRONG', count: strong },
    { kind: 'CLAP', count: 0 },
    { kind: 'GORILLA', count: 0 },
  ],
  ...(mine === undefined ? {} : { mine }),
})

describe('nextReaction', () => {
  it('gives, changes and takes back', () => {
    expect(nextReaction(undefined, 'FIRE')).toBe('FIRE')
    expect(nextReaction('FIRE', 'CLAP')).toBe('CLAP')
    expect(nextReaction('FIRE', 'FIRE')).toBeUndefined()
  })
})

describe('withMyReaction', () => {
  it('adds my first reaction', () => {
    expect(withMyReaction(reactions(2, 0), 'FIRE')).toEqual(reactions(3, 0, 'FIRE'))
  })

  it('moves my reaction from one kind to another', () => {
    expect(withMyReaction(reactions(3, 1, 'FIRE'), 'STRONG')).toEqual(reactions(2, 2, 'STRONG'))
  })

  it('takes my reaction back', () => {
    expect(withMyReaction(reactions(3, 1, 'FIRE'), undefined)).toEqual(reactions(2, 1))
  })

  it('never goes below zero, even on stale counts', () => {
    expect(withMyReaction(reactions(0, 0, 'FIRE'), undefined)).toEqual(reactions(0, 0))
  })
})

describe('reactionTotal', () => {
  it('adds the four counts', () => {
    expect(reactionTotal(reactions(3, 1))).toBe(4)
  })
})
