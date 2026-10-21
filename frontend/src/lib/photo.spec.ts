import { describe, expect, it } from 'vitest'

import { fitWithin, squareCrop } from './photo'

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

describe('fitWithin', () => {
  it('shrinks a portrait photo to 1600 px high, without cropping', () => {
    expect(fitWithin(3000, 4000, 1600)).toEqual({ width: 1200, height: 1600 })
  })

  it('shrinks a landscape photo to 1600 px wide', () => {
    expect(fitWithin(4032, 3024, 1600)).toEqual({ width: 1600, height: 1200 })
  })

  it('never enlarges a small picture', () => {
    expect(fitWithin(800, 600, 1600)).toEqual({ width: 800, height: 600 })
  })
})
