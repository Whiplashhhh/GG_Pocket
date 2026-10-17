import { describe, expect, it } from 'vitest'

import { AVATAR_COLORS, avatarColor, initialsOf } from './avatar'
import { contrastRatio } from './color'

describe('initialsOf', () => {
  it('takes the first letter of first and last name', () => {
    expect(initialsOf('Marie', 'Hennebert')).toBe('MH')
  })

  it('keeps accents and upper-cases them', () => {
    expect(initialsOf('élodie', 'œuvray')).toBe('ÉŒ')
  })

  it('ignores missing parts and surrounding spaces', () => {
    expect(initialsOf('  Sam ', '')).toBe('S')
  })
})

describe('avatarColor', () => {
  it('always gives the same colour for the same seed', () => {
    expect(avatarColor('mariehh')).toBe(avatarColor('mariehh'))
  })

  it('spreads seeds over the palette', () => {
    const colors = new Set(['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'].map(avatarColor))
    expect(colors.size).toBeGreaterThan(3)
  })

  it.each(AVATAR_COLORS)('keeps ink initials readable on %s', (color) => {
    expect(contrastRatio('#17140f', color)).toBeGreaterThanOrEqual(4.5)
  })
})
