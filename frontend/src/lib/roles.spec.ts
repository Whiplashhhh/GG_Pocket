import { describe, expect, it } from 'vitest'

import { isAtLeast } from './roles'

describe('isAtLeast', () => {
  it('follows the role hierarchy', () => {
    expect(isAtLeast('ADMIN', 'BOARD')).toBe(true)
    expect(isAtLeast('BOARD', 'BOARD')).toBe(true)
    expect(isAtLeast('SETTER', 'BOARD')).toBe(false)
    expect(isAtLeast('CLIMBER', 'SETTER')).toBe(false)
    expect(isAtLeast(undefined, 'CLIMBER')).toBe(false)
  })
})
