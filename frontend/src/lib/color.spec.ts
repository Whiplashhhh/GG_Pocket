import { describe, expect, it } from 'vitest'

import { contrastRatio, darken, relativeLuminance } from './color'

describe('darken', () => {
  it('keeps the colour at 0 and reaches black at 1', () => {
    expect(darken('#f9561a', 0)).toBe('#f9561a')
    expect(darken('#f9561a', 1)).toBe('#000000')
  })

  it('halves each channel at 0.5', () => {
    expect(darken('#808080', 0.5)).toBe('#404040')
  })

  it('rejects anything that is not #rrggbb', () => {
    expect(() => darken('orange', 0.2)).toThrow('#rrggbb')
  })
})

describe('contrast', () => {
  it('matches the WCAG reference values', () => {
    expect(relativeLuminance('#ffffff')).toBeCloseTo(1)
    expect(contrastRatio('#000000', '#ffffff')).toBeCloseTo(21)
  })

  // The design system promises AA for text (docs/DESIGN.md §2): keep it true.
  it.each([
    ['ink on magnésie', '#17140f', '#f4f1ea'],
    ['muted on magnésie', '#5e574c', '#f4f1ea'],
    ['brique on magnésie', '#b23e0b', '#f4f1ea'],
    ['ink on orange GG', '#17140f', '#f9561a'],
    ['vert relais on magnésie', '#4a5a0c', '#f4f1ea'],
    ['danger on magnésie', '#a3261b', '#f4f1ea'],
    ['magnésie on night', '#f3efe6', '#12110e'],
    ['muted on tapis', '#ada594', '#1e1c18'],
    ['orange text on night', '#ff7340', '#12110e'],
    ['vert relais on night', '#c9c653', '#12110e'],
    ['danger on night', '#ff8a7a', '#12110e'],
    ['tab label on bar', '#a39b8b', '#17140f'],
    ['label ink on dimmed paper', '#17140f', '#e9e2d0'],
  ])('%s reaches 4.5:1', (_name, foreground, background) => {
    expect(contrastRatio(foreground, background)).toBeGreaterThanOrEqual(4.5)
  })
})
