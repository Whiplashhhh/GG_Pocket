import { describe, expect, it } from 'vitest'

import type { RouteDetail, TopoConfig } from '@/api/topo'

import {
  colorChoices,
  cordChoices,
  duplicateRouteForm,
  emptyRouteForm,
  gradeChoices,
  labelPreview,
  routeSettings,
  sameRouteForm,
  toggleStyle,
  toRouteForm,
  withMainColor,
  withSecondColor,
  type RouteForm,
} from './routeForm'

const TODAY = '2026-10-20'

const CONFIG: TopoConfig = {
  sectors: [
    {
      id: 's-devers',
      name: 'Grand dévers',
      position: 2,
      cords: [
        { id: 'c15', number: 15, sectorId: 's-devers', active: true },
        { id: 'c16', number: 16, sectorId: 's-devers', active: false },
      ],
    },
    {
      id: 's-neutre',
      name: 'Neutre 8 m',
      position: 1,
      cords: [
        { id: 'c2', number: 2, sectorId: 's-neutre', active: true },
        { id: 'c1', number: 1, sectorId: 's-neutre', active: true },
      ],
    },
    {
      id: 's-vide',
      name: 'Vieux pan',
      position: 3,
      cords: [{ id: 'c30', number: 30, sectorId: 's-vide', active: false }],
    },
  ],
  holdColors: [
    { id: 'bleu', label: 'bleu', hex: '#1F5FBF', multicolor: false, active: true, position: 2 },
    { id: 'jaune', label: 'jaune', hex: '#F5C518', multicolor: false, active: true, position: 1 },
    { id: 'gris', label: 'gris', hex: '#9A9A9A', multicolor: false, active: false, position: 3 },
  ],
  grades: [
    { id: 'g6b', code: '6b', rank: 15 },
    { id: 'g4a', code: '4a', rank: 1 },
  ],
}

const MORICE: RouteDetail = {
  route: {
    id: 'r1',
    name: 'Bye Bye Morice',
    grade: '6b',
    gradeRank: 15,
    cordNumber: 15,
    sectorId: 's-devers',
    sectorName: 'Grand dévers',
    sectorPosition: 2,
    holds: [
      { id: 'jaune', label: 'jaune', hex: '#F5C518', multicolor: false },
      { id: 'gris', label: 'gris', hex: '#9A9A9A', multicolor: false },
    ],
    status: 'ACTIVE',
    openedOn: '2026-05-16',
    photoUrl: '/api/v1/media/p1',
    styles: ['ENDURANCE', 'CONTINUITY'],
    favoriteCount: 5,
    commentCount: 1,
    favorite: false,
    checks: [],
  },
  gradeId: 'g6b',
  cordId: 'c15',
  setters: [{ id: 'm-sam', nickname: 'sam' }],
  setterNote: 'Tonton',
  advice: 'Garde du jus pour la sortie.',
  createdAt: '2026-05-16T10:00:00Z',
}

const filled = (overrides: Partial<RouteForm> = {}): RouteForm => ({
  ...emptyRouteForm(TODAY),
  name: '  Sec ! ',
  gradeId: 'g4a',
  cordId: 'c1',
  holdColorIds: ['jaune'],
  setterIds: ['m-sam'],
  ...overrides,
})

describe('route form ↔ API', () => {
  it('starts empty, opened today', () => {
    expect(emptyRouteForm(TODAY)).toMatchObject({ name: '', openedOn: TODAY, holdColorIds: [] })
  })

  it('reads a route to edit it', () => {
    expect(toRouteForm(MORICE)).toEqual({
      name: 'Bye Bye Morice',
      gradeId: 'g6b',
      cordId: 'c15',
      holdColorIds: ['jaune', 'gris'],
      openedOn: '2026-05-16',
      setterIds: ['m-sam'],
      setterNote: 'Tonton',
      advice: 'Garde du jus pour la sortie.',
      styles: ['ENDURANCE', 'CONTINUITY'],
    })
  })

  it('duplicates everything but the date, which becomes today', () => {
    const copy = duplicateRouteForm(MORICE, TODAY)
    expect(copy).toEqual({ ...toRouteForm(MORICE), openedOn: TODAY })
  })

  it('sends trimmed texts, leaves out the empty ones, styles in the club order', () => {
    const result = routeSettings(
      filled({ advice: '   ', setterNote: ' Jo ', styles: ['SLAB', 'CONTINUITY'] }),
      TODAY,
    )
    expect(result).toEqual({
      settings: {
        name: 'Sec !',
        gradeId: 'g4a',
        cordId: 'c1',
        holdColorIds: ['jaune'],
        openedOn: TODAY,
        setterIds: ['m-sam'],
        setterNote: 'Jo',
        styles: ['CONTINUITY', 'SLAB'],
      },
    })
  })

  it('tells when nothing changed, whatever the order of setters and styles', () => {
    const form = toRouteForm(MORICE)
    expect(sameRouteForm(form, { ...form, styles: ['CONTINUITY', 'ENDURANCE'] })).toBe(true)
    expect(sameRouteForm(form, { ...form, name: 'Bye Bye Maurice' })).toBe(false)
  })
})

describe('route form rules', () => {
  const errorsOf = (form: RouteForm) => {
    const result = routeSettings(form, TODAY)
    return 'errors' in result ? result.errors : {}
  }

  it('needs a name, a grade, a cord and a colour', () => {
    expect(errorsOf(emptyRouteForm(TODAY))).toMatchObject({
      name: { key: 'validation.required' },
      gradeId: { key: 'routeForm.errors.grade' },
      cordId: { key: 'routeForm.errors.cord' },
      holdColorIds: { key: 'routeForm.errors.color' },
      setters: { key: 'routeForm.errors.setters' },
    })
  })

  it('accepts an opener without an account instead of a member', () => {
    expect(errorsOf(filled({ setterIds: [], setterNote: 'Un invité' }))).toEqual({})
    expect(errorsOf(filled({ setterIds: [], setterNote: '  ' }))).toEqual({
      setters: { key: 'routeForm.errors.setters' },
    })
  })

  it('refuses a date in the future, two same colours and too long texts', () => {
    expect(
      errorsOf(
        filled({
          name: 'x'.repeat(61),
          openedOn: '2026-10-21',
          holdColorIds: ['jaune', 'jaune'],
          advice: 'x'.repeat(501),
        }),
      ),
    ).toEqual({
      name: { key: 'validation.tooLong', params: { max: 60 } },
      openedOn: { key: 'validation.dateInFuture' },
      holdColorIds: { key: 'routeForm.errors.colorsDistinct' },
      advice: { key: 'validation.tooLong', params: { max: 500 } },
    })
  })

  it('keeps five styles at most', () => {
    const five = toggleStyle(['CONTINUITY', 'ENDURANCE', 'PHYSICAL', 'TECHNICAL'], 'COMPLEX')
    expect(five).toHaveLength(5)
    expect(toggleStyle(five, 'SLAB')).toEqual(five)
    expect(toggleStyle(five, 'PHYSICAL')).not.toContain('PHYSICAL')
  })
})

describe('route form choices', () => {
  it('lists grades easiest first', () => {
    expect(gradeChoices(CONFIG).map((grade) => grade.code)).toEqual(['4a', '6b'])
  })

  it('groups active cords by sector from left to right, keeping the current one', () => {
    expect(
      cordChoices(CONFIG).map((group) => [group.sectorName, group.cords.map((c) => c.number)]),
    ).toEqual([
      ['Neutre 8 m', [1, 2]],
      ['Grand dévers', [15]],
    ])
    expect(cordChoices(CONFIG, 'c16')[1]?.cords.map((cord) => cord.number)).toEqual([15, 16])
  })

  it('offers active colours, plus those the route already has', () => {
    expect(colorChoices(CONFIG).map((color) => color.label)).toEqual(['jaune', 'bleu'])
    expect(colorChoices(CONFIG, ['gris']).map((color) => color.label)).toEqual([
      'jaune',
      'bleu',
      'gris',
    ])
  })

  it('keeps the two colours distinct', () => {
    expect(withMainColor(['jaune', 'bleu'], 'rouge')).toEqual(['rouge', 'bleu'])
    expect(withMainColor(['jaune', 'bleu'], 'bleu')).toEqual(['bleu'])
    expect(withSecondColor(['jaune'], 'bleu')).toEqual(['jaune', 'bleu'])
    expect(withSecondColor(['jaune', 'bleu'], 'jaune')).toEqual(['jaune'])
    expect(withSecondColor(['jaune', 'bleu'], undefined)).toEqual(['jaune'])
  })

  it('previews the label once cord, grade and a colour are chosen', () => {
    expect(labelPreview(emptyRouteForm(TODAY), CONFIG)).toBeNull()
    expect(labelPreview(filled({ holdColorIds: ['jaune', 'bleu'] }), CONFIG)).toMatchObject({
      cord: 1,
      grade: '4a',
      holds: [{ label: 'jaune' }, { label: 'bleu' }],
    })
  })
})
