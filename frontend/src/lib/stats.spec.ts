import { describe, expect, it } from 'vitest'

import type { Attendance, RouteAttendance } from '@/api/stats'
import type { Grade, RouteSummary, Sector } from '@/api/topo'

import {
  attendanceCsv,
  attendanceFileName,
  cordGrid,
  csvCell,
  gradeDistribution,
  isAttendanceSort,
  leastClimbed,
  mostClimbed,
  mostLoved,
  oldestFirst,
  sectorDistribution,
  sortAttendance,
  wallAge,
  type CsvHeaders,
} from './stats'

const YELLOW = { id: 'c-yellow', label: 'jaune', hex: '#f5c518', multicolor: false }
const BLUE = { id: 'c-blue', label: 'bleu', hex: '#1f5fbf', multicolor: false }

function route(overrides: Partial<RouteSummary> & Pick<RouteSummary, 'id'>): RouteSummary {
  return {
    name: `Voie ${overrides.id}`,
    grade: '6a',
    gradeRank: 13,
    cordNumber: 12,
    sectorId: 's-negatif',
    sectorName: 'Négatif',
    sectorPosition: 2,
    holds: [YELLOW],
    status: 'ACTIVE',
    openedOn: '2026-09-01',
    styles: [],
    favoriteCount: 0,
    commentCount: 0,
    toppedCount: 0,
    favorite: false,
    checks: [],
    ...overrides,
  }
}

function line(
  overrides: Partial<RouteSummary> & Pick<RouteSummary, 'id'>,
  attendance: Partial<Attendance> = {},
): RouteAttendance {
  return {
    route: route(overrides),
    attendance: { ascentCount: 0, toppedCount: 0, cleanCount: 0, climberCount: 0, ...attendance },
  }
}

const ids = (lines: readonly { route: RouteSummary }[]) => lines.map((item) => item.route.id)

describe('sortAttendance', () => {
  const busy = line(
    { id: 'busy', cordNumber: 3 },
    { ascentCount: 9, climberCount: 2, successPercent: 33 },
  )
  const popular = line(
    { id: 'popular', cordNumber: 7, gradeRank: 5 },
    { ascentCount: 5, climberCount: 5, successPercent: 80 },
  )
  const untouched = line({ id: 'untouched', cordNumber: 1, gradeRank: 20 })
  const tied = line(
    { id: 'tied', cordNumber: 2 },
    { ascentCount: 9, climberCount: 1, successPercent: 33 },
  )
  const lines = [untouched, popular, busy, tied]

  it('puts the most climbed first, ties by cord', () => {
    expect(ids(sortAttendance(lines, 'ascents'))).toEqual(['tied', 'busy', 'popular', 'untouched'])
  })

  it('puts the most climbers first', () => {
    expect(ids(sortAttendance(lines, 'climbers'))).toEqual(['popular', 'busy', 'tied', 'untouched'])
  })

  it('puts the best success rate first and routes nobody tried last', () => {
    expect(ids(sortAttendance(lines, 'success'))).toEqual(['popular', 'tied', 'busy', 'untouched'])
  })

  it('sorts by cord and by grade in their natural order', () => {
    expect(ids(sortAttendance(lines, 'cord'))).toEqual(['untouched', 'tied', 'busy', 'popular'])
    expect(ids(sortAttendance(lines, 'grade'))).toEqual(['popular', 'tied', 'busy', 'untouched'])
  })

  it('leaves the given list alone', () => {
    sortAttendance(lines, 'cord')
    expect(ids(lines)).toEqual(['untouched', 'popular', 'busy', 'tied'])
  })

  it('recognises the sorts of the address bar', () => {
    expect(isAttendanceSort('success')).toBe(true)
    expect(isAttendanceSort('nope')).toBe(false)
    expect(isAttendanceSort(undefined)).toBe(false)
  })
})

describe('attendanceCsv', () => {
  const headers: CsvHeaders = [
    'Corde',
    'Secteur',
    'Voie',
    'Cotation',
    'Prises',
    'Ouverte le',
    'Ascensions',
    'Topées',
    'Enchaînées',
    'Grimpeurs',
    'Réussite (%)',
  ]

  it('writes one line per route under the headers, for French spreadsheets', () => {
    const csv = attendanceCsv(
      [
        line(
          { id: 'a', name: 'La "Dalle"; facile', cordNumber: 4, holds: [YELLOW, BLUE] },
          { ascentCount: 3, toppedCount: 2, cleanCount: 1, climberCount: 2, successPercent: 67 },
        ),
        line({ id: 'b', name: 'Personne', grade: '7a' }),
      ],
      headers,
    )

    expect(csv.startsWith('﻿')).toBe(true)
    expect(csv.slice(1).split('\r\n')).toEqual([
      'Corde;Secteur;Voie;Cotation;Prises;Ouverte le;Ascensions;Topées;Enchaînées;Grimpeurs;Réussite (%)',
      '4;Négatif;"La ""Dalle""; facile";6a;jaune / bleu;2026-09-01;3;2;1;2;67',
      '12;Négatif;Personne;7a;jaune;2026-09-01;0;0;0;0;',
      '',
    ])
  })

  it('never lets a spreadsheet run a cell as a formula', () => {
    expect(csvCell('=HYPERLINK("x")')).toBe(`"'=HYPERLINK(""x"")"`)
    expect(csvCell('+33')).toBe("'+33")
    expect(csvCell('-1')).toBe("'-1")
    expect(csvCell('@moi')).toBe("'@moi")
    expect(csvCell('Le dièdre')).toBe('Le dièdre')
  })

  it('names the file after the month', () => {
    expect(attendanceFileName({ month: '2026-10' })).toBe('gg-pocket-frequentation-2026-10.csv')
  })
})

const grade = (code: string, rank: number): Grade => ({ id: `g-${code}`, code, rank })
const GRADES = [grade('5c', 9), grade('5a', 7), grade('5b', 8), grade('6a', 10), grade('6b', 11)]
const SECTORS: Sector[] = [
  {
    id: 's-negatif',
    name: 'Négatif',
    position: 2,
    cords: [
      { id: 'k3', number: 3, sectorId: 's-negatif', active: true },
      { id: 'k4', number: 4, sectorId: 's-negatif', active: false },
    ],
  },
  {
    id: 's-neutre',
    name: 'Neutre',
    position: 1,
    cords: [
      { id: 'k1', number: 1, sectorId: 's-neutre', active: true },
      { id: 'k2', number: 2, sectorId: 's-neutre', active: true },
    ],
  },
]

describe('distributions', () => {
  const routes = [
    route({ id: 'a', gradeRank: 7, sectorId: 's-neutre', cordNumber: 1 }),
    route({ id: 'b', gradeRank: 9, sectorId: 's-neutre', cordNumber: 1 }),
    route({ id: 'c', gradeRank: 9, cordNumber: 3 }),
    route({ id: 'd', gradeRank: 9, cordNumber: 4 }),
  ]

  it('counts the routes per grade between the easiest and the hardest, gaps included', () => {
    expect(
      gradeDistribution(routes, GRADES).map((bar) => [bar.item.code, bar.count, bar.percent]),
    ).toEqual([
      ['5a', 1, 33],
      ['5b', 0, 0],
      ['5c', 3, 100],
    ])
    expect(gradeDistribution([], GRADES)).toEqual([])
  })

  it('counts the routes per sector in the order of the wall', () => {
    expect(
      sectorDistribution(routes, SECTORS).map((bar) => [bar.item.name, bar.count, bar.percent]),
    ).toEqual([
      ['Neutre', 2, 100],
      ['Négatif', 2, 100],
    ])
    expect(sectorDistribution([], SECTORS).every((bar) => bar.percent === 0)).toBe(true)
  })

  it('lays out every cord in use, and an unused cord that still has routes', () => {
    const grid = cordGrid(routes, SECTORS)
    expect(
      grid.map((cell) => [
        cell.number,
        cell.sectorName,
        ids(cell.routes.map((r) => ({ route: r }))),
      ]),
    ).toEqual([
      [1, 'Neutre', ['a', 'b']],
      [2, 'Neutre', []],
      [3, 'Négatif', ['c']],
      [4, 'Négatif', ['d']],
    ])
  })
})

describe('rankings of the topo', () => {
  const old = line({ id: 'old', openedOn: '2026-01-10', cordNumber: 5 }, { ascentCount: 1 })
  const fresh = line({ id: 'fresh', openedOn: '2026-10-20', cordNumber: 2 })
  const forgotten = line({ id: 'forgotten', openedOn: '2026-03-01', cordNumber: 9 })
  const star = line({ id: 'star', cordNumber: 1 }, { ascentCount: 40 })
  const lines = [old, fresh, forgotten, star]

  it('lists the most climbed, never a route nobody climbed', () => {
    expect(ids(mostClimbed(lines, 5))).toEqual(['star', 'old'])
    expect(ids(mostClimbed(lines, 1))).toEqual(['star'])
  })

  it('lists the least climbed, the oldest first among equals', () => {
    expect(ids(leastClimbed(lines, 3))).toEqual(['forgotten', 'fresh', 'old'])
  })

  it('lists the routes on the wall for the longest time first', () => {
    expect(oldestFirst(lines.map((item) => item.route)).map((r) => r.id)).toEqual([
      'old',
      'forgotten',
      'star',
      'fresh',
    ])
  })

  it('lists the most loved routes, never one without a favourite', () => {
    const routes = [
      route({ id: 'meh' }),
      route({ id: 'liked', favoriteCount: 2, cordNumber: 8 }),
      route({ id: 'loved', favoriteCount: 5 }),
      route({ id: 'liked-too', favoriteCount: 2, cordNumber: 3 }),
    ]
    expect(mostLoved(routes, 10).map((r) => r.id)).toEqual(['loved', 'liked-too', 'liked'])
    expect(mostLoved(routes, 1).map((r) => r.id)).toEqual(['loved'])
  })
})

describe('wallAge', () => {
  it('counts days for two weeks, then weeks, then months', () => {
    expect(wallAge('2026-10-30', '2026-10-30')).toEqual({ unit: 'days', value: 0 })
    expect(wallAge('2026-10-17', '2026-10-30')).toEqual({ unit: 'days', value: 13 })
    expect(wallAge('2026-10-16', '2026-10-30')).toEqual({ unit: 'weeks', value: 2 })
    expect(wallAge('2026-09-01', '2026-10-30')).toEqual({ unit: 'weeks', value: 8 })
    expect(wallAge('2026-08-30', '2026-10-30')).toEqual({ unit: 'months', value: 2 })
    expect(wallAge('2025-10-30', '2026-10-30')).toEqual({ unit: 'months', value: 12 })
    expect(wallAge('2025-10-31', '2026-10-30')).toEqual({ unit: 'months', value: 11 })
  })

  it('never says a route opened in the future is negative days old', () => {
    expect(wallAge('2026-11-02', '2026-10-30')).toEqual({ unit: 'days', value: 0 })
  })
})
