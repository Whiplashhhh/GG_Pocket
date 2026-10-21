import { describe, expect, it } from 'vitest'

import type { RouteSummary } from '@/api/topo'

import {
  activeFilterCount,
  applyTopoView,
  filterRoutes,
  groupBySector,
  NO_FILTERS,
  parseTopoQuery,
  searchRoutes,
  sortRoutes,
  toTopoQuery,
  type TopoView,
} from './topo'

const YELLOW = { id: 'c-yellow', label: 'jaune', hex: '#f5c518', multicolor: false }
const BLUE = { id: 'c-blue', label: 'bleu', hex: '#1f5fbf', multicolor: false }
const LIGHT_BLUE = { id: 'c-light-blue', label: 'bleu clair', hex: '#5bc0eb', multicolor: false }

const NEGATIVE = {
  sectorId: 's-negatif',
  sectorName: 'Négatif et ses 2 dièdres',
  sectorPosition: 4,
}
const NEUTRAL = { sectorId: 's-neutre', sectorName: 'Neutre 8 m', sectorPosition: 1 }
const OVERHANG = { sectorId: 's-devers', sectorName: 'Grand dévers', sectorPosition: 5 }

function route(overrides: Partial<RouteSummary> & Pick<RouteSummary, 'id' | 'name'>): RouteSummary {
  return {
    grade: '6a',
    gradeRank: 13,
    cordNumber: 12,
    ...NEGATIVE,
    holds: [YELLOW],
    status: 'ACTIVE',
    openedOn: '2026-09-01',
    styles: [],
    favoriteCount: 0,
    commentCount: 0,
    favorite: false,
    checks: [],
    ...overrides,
  }
}

const morice = route({
  id: 'r1',
  name: 'Bye Bye Morice',
  grade: '6b',
  gradeRank: 15,
  cordNumber: 12,
  holds: [YELLOW, BLUE],
  openedOn: '2026-05-16',
  styles: ['CONTINUITY', 'ENDURANCE'],
  favoriteCount: 5,
  commentCount: 1,
  checks: ['LEAD'],
})
const puree = route({
  id: 'r2',
  name: 'OH purée !',
  grade: '6b',
  gradeRank: 15,
  cordNumber: 1,
  ...NEUTRAL,
  openedOn: '2026-10-02',
  favoriteCount: 2,
  commentCount: 4,
})
const tonton = route({
  id: 'r3',
  name: 'Tonton Talon',
  grade: '6a+',
  gradeRank: 14,
  cordNumber: 15,
  ...OVERHANG,
  holds: [LIGHT_BLUE],
  status: 'LAST_CHANCE',
  styles: ['PHYSICAL', 'OVERHANG'],
  favoriteCount: 2,
  commentCount: 0,
  checks: ['TOP_ROPE'],
})
const reglisse = route({
  id: 'r4',
  name: 'Réglisse',
  grade: '5b',
  gradeRank: 9,
  cordNumber: 3,
  ...NEUTRAL,
  holds: [BLUE],
  openedOn: '2026-10-02',
  styles: ['SLAB'],
})
const ALL = [morice, puree, tonton, reglisse]
const names = (routes: RouteSummary[]) => routes.map((item) => item.name)

describe('sortRoutes', () => {
  it('sorts by grade, easiest first by default, cord then name on a tie', () => {
    expect(names(sortRoutes(ALL, 'grade', 'asc'))).toEqual([
      'Réglisse',
      'Tonton Talon',
      'OH purée !',
      'Bye Bye Morice',
    ])
  })

  it('keeps the same tie-breaks when the order is reversed', () => {
    expect(names(sortRoutes(ALL, 'grade', 'desc'))).toEqual([
      'OH purée !',
      'Bye Bye Morice',
      'Tonton Talon',
      'Réglisse',
    ])
  })

  it('sorts by opening date', () => {
    expect(names(sortRoutes(ALL, 'opened', 'desc'))).toEqual([
      'Réglisse',
      'OH purée !',
      'Tonton Talon',
      'Bye Bye Morice',
    ])
  })

  it('sorts by sector position, then cord', () => {
    expect(names(sortRoutes(ALL, 'sector', 'asc'))).toEqual([
      'OH purée !',
      'Réglisse',
      'Bye Bye Morice',
      'Tonton Talon',
    ])
    expect(names(sortRoutes(ALL, 'sector', 'desc'))).toEqual([
      'Tonton Talon',
      'Bye Bye Morice',
      'Réglisse',
      'OH purée !',
    ])
  })

  it('sorts by favourites and by comments', () => {
    expect(names(sortRoutes(ALL, 'favorites', 'desc'))).toEqual([
      'Bye Bye Morice',
      'Tonton Talon',
      'OH purée !',
      'Réglisse',
    ])
    expect(names(sortRoutes(ALL, 'comments', 'asc'))).toEqual([
      'Réglisse',
      'Tonton Talon',
      'Bye Bye Morice',
      'OH purée !',
    ])
  })

  it('does not touch the list it was given', () => {
    const copy = [...ALL]
    sortRoutes(ALL, 'grade', 'desc')
    expect(ALL).toEqual(copy)
  })
})

describe('filterRoutes', () => {
  it('keeps everything without a filter', () => {
    expect(filterRoutes(ALL, NO_FILTERS)).toHaveLength(4)
  })

  it('filters by sector, grade range, colour, style and "not done by me", combined', () => {
    expect(names(filterRoutes(ALL, { ...NO_FILTERS, sectorIds: ['s-neutre'] }))).toEqual([
      'OH purée !',
      'Réglisse',
    ])
    expect(names(filterRoutes(ALL, { ...NO_FILTERS, minRank: 14, maxRank: 14 }))).toEqual([
      'Tonton Talon',
    ])
    expect(names(filterRoutes(ALL, { ...NO_FILTERS, holdColorIds: ['c-blue'] }))).toEqual([
      'Bye Bye Morice',
      'Réglisse',
    ])
    expect(names(filterRoutes(ALL, { ...NO_FILTERS, styles: ['SLAB', 'OVERHANG'] }))).toEqual([
      'Tonton Talon',
      'Réglisse',
    ])
    expect(names(filterRoutes(ALL, { ...NO_FILTERS, notDoneByMe: true }))).toEqual([
      'OH purée !',
      'Réglisse',
    ])
    expect(
      names(
        filterRoutes(ALL, {
          ...NO_FILTERS,
          sectorIds: ['s-neutre', 's-negatif'],
          minRank: 10,
          notDoneByMe: true,
        }),
      ),
    ).toEqual(['OH purée !'])
  })

  it('counts one badge per kind of filter', () => {
    expect(activeFilterCount(NO_FILTERS)).toBe(0)
    expect(
      activeFilterCount({
        ...NO_FILTERS,
        sectorIds: ['a', 'b'],
        minRank: 3,
        maxRank: 9,
        notDoneByMe: true,
      }),
    ).toBe(3)
  })
})

describe('groupBySector', () => {
  it('groups consecutive routes of a sector, in the sorted order', () => {
    const groups = groupBySector(sortRoutes(ALL, 'sector', 'asc'))
    expect(groups.map((group) => [group.sectorName, names(group.routes)])).toEqual([
      ['Neutre 8 m', ['OH purée !', 'Réglisse']],
      ['Négatif et ses 2 dièdres', ['Bye Bye Morice']],
      ['Grand dévers', ['Tonton Talon']],
    ])
  })
})

describe('topo query', () => {
  it('reads defaults from an empty query', () => {
    expect(parseTopoQuery({})).toEqual({ sort: 'grade', direction: 'asc', filters: NO_FILTERS })
  })

  it('round-trips through the URL, leaving defaults out', () => {
    const view: TopoView = {
      sort: 'opened',
      direction: 'asc',
      filters: {
        sectorIds: ['s1', 's2'],
        minRank: 7,
        maxRank: 15,
        holdColorIds: ['c1'],
        styles: ['SLAB'],
        notDoneByMe: true,
      },
    }
    const query = toTopoQuery(view)
    expect(query).toEqual({
      tri: 'ouverture',
      ordre: 'asc',
      secteur: ['s1', 's2'],
      min: '7',
      max: '15',
      couleur: ['c1'],
      style: ['SLAB'],
      'pas-faite': '1',
    })
    expect(parseTopoQuery(query as Record<string, string | string[]>)).toEqual(view)
    expect(toTopoQuery({ sort: 'opened', direction: 'desc', filters: NO_FILTERS })).toEqual({
      tri: 'ouverture',
    })
  })

  it('ignores what it does not know', () => {
    const view = parseTopoQuery({
      tri: 'realisations',
      ordre: 'n-importe',
      min: '-2',
      style: 'YOGA',
    })
    expect(view).toEqual({ sort: 'grade', direction: 'asc', filters: NO_FILTERS })
  })

  it('applies filters then sort', () => {
    const view = parseTopoQuery({ tri: 'coups-de-coeur', secteur: 's-neutre' })
    expect(names(applyTopoView(ALL, view))).toEqual(['OH purée !', 'Réglisse'])
  })
})

describe('searchRoutes', () => {
  it('finds a route by cord, name without accents, colour or grade', () => {
    expect(names(searchRoutes(ALL, '12'))).toEqual(['Bye Bye Morice'])
    expect(names(searchRoutes(ALL, 'reglisse'))).toEqual(['Réglisse'])
    expect(names(searchRoutes(ALL, 'bleu clair'))).toEqual(['Tonton Talon'])
    expect(names(searchRoutes(ALL, '6a'))).toEqual(['Tonton Talon'])
    expect(names(searchRoutes(ALL, 'jaune 6b'))).toEqual(['OH purée !', 'Bye Bye Morice'])
  })

  it('lists everything, by cord, for an empty search', () => {
    expect(names(searchRoutes(ALL, '  '))).toEqual([
      'OH purée !',
      'Réglisse',
      'Bye Bye Morice',
      'Tonton Talon',
    ])
  })
})
