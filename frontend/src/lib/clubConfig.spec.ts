import { describe, expect, it } from 'vitest'

import type { TopoConfig } from '@/api/topo'

import {
  cordSettings,
  holdColorSettings,
  moveSector,
  orderedSectors,
  sectorSettings,
  suggestedCordNumber,
  toCordForm,
  toHoldColorForm,
  withSectorOrder,
} from './clubConfig'

const CONFIG: TopoConfig = {
  sectors: [
    {
      id: 'b',
      name: 'Neutre 12 m',
      position: 2,
      cords: [{ id: 'c4', number: 4, sectorId: 'b', active: true }],
    },
    {
      id: 'a',
      name: 'Neutre 8 m',
      position: 1,
      cords: [{ id: 'c1', number: 1, sectorId: 'a', active: true }],
    },
    { id: 'c', name: 'Positif', position: 3, cords: [] },
  ],
  holdColors: [],
  grades: [],
}

describe('sector order', () => {
  it('reads the wall from left to right', () => {
    expect(orderedSectors(CONFIG).map((sector) => sector.id)).toEqual(['a', 'b', 'c'])
  })

  it('moves a sector up or down, not past the ends', () => {
    const sectors = orderedSectors(CONFIG)
    expect(moveSector(sectors, 'b', -1)).toEqual(['b', 'a', 'c'])
    expect(moveSector(sectors, 'b', 1)).toEqual(['a', 'c', 'b'])
    expect(moveSector(sectors, 'a', -1)).toBeNull()
    expect(moveSector(sectors, 'c', 1)).toBeNull()
    expect(moveSector(sectors, 'inconnu', 1)).toBeNull()
  })

  it('renumbers the positions of the new order', () => {
    const moved = withSectorOrder(CONFIG, ['c', 'a', 'b'])
    expect(moved.sectors.map((sector) => [sector.id, sector.position])).toEqual([
      ['c', 1],
      ['a', 2],
      ['b', 3],
    ])
  })
})

describe('sector form', () => {
  it('needs a name, keeps an optional description', () => {
    expect(sectorSettings({ name: '  ', description: '' })).toEqual({
      errors: { name: { key: 'validation.required' } },
    })
    expect(sectorSettings({ name: ' Grand toit ', description: '  ' })).toEqual({
      settings: { name: 'Grand toit' },
    })
    expect(sectorSettings({ name: 'Grand toit', description: 'x'.repeat(161) })).toEqual({
      errors: { description: { key: 'validation.tooLong', params: { max: 160 } } },
    })
  })
})

describe('cord form', () => {
  it('suggests the number after the highest cord', () => {
    expect(suggestedCordNumber(CONFIG)).toBe(5)
    expect(toCordForm(undefined, 'c', 5)).toEqual({ number: '5', sectorId: 'c', active: true })
  })

  it('takes a whole number from 1 to 99', () => {
    expect(cordSettings({ number: '30', sectorId: 'c', active: false })).toEqual({
      settings: { number: 30, sectorId: 'c', active: false },
    })
    for (const number of ['0', '100', '3.5', 'douze', '']) {
      expect(cordSettings({ number, sectorId: 'c', active: true })).toEqual({
        errors: { number: { key: 'clubConfig.errors.cordNumber', params: { max: 99 } } },
      })
    }
  })
})

describe('hold colour form', () => {
  it('starts grey and active', () => {
    expect(toHoldColorForm(undefined)).toEqual({
      label: '',
      hex: '#808080',
      multicolor: false,
      active: true,
    })
  })

  it('sends the code in upper case, with its hash', () => {
    expect(
      holdColorSettings({ label: ' turquoise ', hex: '30d5c8', multicolor: false, active: true }),
    ).toEqual({ settings: { label: 'turquoise', hex: '#30D5C8', multicolor: false, active: true } })
  })

  it('refuses an empty label and a code that is not #RRGGBB', () => {
    expect(
      holdColorSettings({ label: '', hex: '#30d5c', multicolor: false, active: true }),
    ).toEqual({
      errors: {
        label: { key: 'validation.required' },
        hex: { key: 'clubConfig.errors.hex' },
      },
    })
  })
})
