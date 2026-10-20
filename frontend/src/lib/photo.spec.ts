import { describe, expect, it } from 'vitest'

import { squareCrop } from './photo'

describe('squareCrop', () => {
  it('takes the centred square of a landscape photo', () => {
    expect(squareCrop(4000, 3000, 1024)).toEqual({ sx: 500, sy: 0, side: 3000, target: 1024 })
  })

  it('takes the centred square of a portrait photo', () => {
    expect(squareCrop(3000, 4000, 1024)).toEqual({ sx: 0, sy: 500, side: 3000, target: 1024 })
  })

  it('never enlarges a small picture', () => {
    expect(squareCrop(300, 200, 1024).target).toBe(200)
  })
})
