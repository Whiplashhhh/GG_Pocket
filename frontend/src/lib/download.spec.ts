import { describe, expect, it } from 'vitest'

import { myDataFileName } from './download'

describe('myDataFileName', () => {
  it('names the export after the day, month and day on two digits', () => {
    expect(myDataFileName(new Date(2026, 2, 5, 23, 59))).toBe(
      'gg-pocket-mes-donnees-2026-03-05.json',
    )
  })
})
