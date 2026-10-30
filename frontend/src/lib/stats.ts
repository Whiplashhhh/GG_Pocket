import type { MonthAttendance, RouteAttendance } from '@/api/stats'
import type { Grade, Sector } from '@/api/topo'
import type { RouteSummary } from '@/lib/route'

/** Statistics of the management space (spec §8.10): attendance of a month and the topo overview. */

export const ATTENDANCE_SORTS = ['ascents', 'climbers', 'success', 'cord', 'grade'] as const
export type AttendanceSort = (typeof ATTENDANCE_SORTS)[number]

const byCord = (a: RouteAttendance, b: RouteAttendance) =>
  a.route.cordNumber - b.route.cordNumber || a.route.gradeRank - b.route.gradeRank
const byGrade = (a: RouteAttendance, b: RouteAttendance) =>
  a.route.gradeRank - b.route.gradeRank || a.route.cordNumber - b.route.cordNumber

const COMPARATORS: Record<AttendanceSort, (a: RouteAttendance, b: RouteAttendance) => number> = {
  ascents: (a, b) => b.attendance.ascentCount - a.attendance.ascentCount || byCord(a, b),
  climbers: (a, b) =>
    b.attendance.climberCount - a.attendance.climberCount ||
    b.attendance.ascentCount - a.attendance.ascentCount ||
    byCord(a, b),
  // Routes nobody tried have no rate: last.
  success: (a, b) =>
    (b.attendance.successPercent ?? -1) - (a.attendance.successPercent ?? -1) ||
    b.attendance.ascentCount - a.attendance.ascentCount ||
    byCord(a, b),
  cord: byCord,
  grade: byGrade,
}

/** Most first for the counts; cord and grade in their natural order. */
export function sortAttendance(
  lines: readonly RouteAttendance[],
  sort: AttendanceSort,
): RouteAttendance[] {
  return [...lines].sort(COMPARATORS[sort])
}

export function isAttendanceSort(value: unknown): value is AttendanceSort {
  return typeof value === 'string' && (ATTENDANCE_SORTS as readonly string[]).includes(value)
}

/** The column titles of the CSV, in the order of {@link attendanceCsv}. */
export type CsvHeaders = readonly [
  cord: string,
  sector: string,
  route: string,
  grade: string,
  holds: string,
  opened: string,
  ascents: string,
  topped: string,
  clean: string,
  climbers: string,
  success: string,
]

/**
 * The attendance of a month as a spreadsheet file: semicolons and a byte order mark, as French
 * spreadsheets expect, lines in the order shown.
 */
export function attendanceCsv(lines: readonly RouteAttendance[], headers: CsvHeaders): string {
  const rows = lines.map(({ route, attendance }) => [
    String(route.cordNumber),
    route.sectorName,
    route.name,
    route.grade,
    route.holds.map((hold) => hold.label).join(' / '),
    route.openedOn,
    String(attendance.ascentCount),
    String(attendance.toppedCount),
    String(attendance.cleanCount),
    String(attendance.climberCount),
    attendance.successPercent === undefined ? '' : String(attendance.successPercent),
  ])
  return '﻿' + [headers, ...rows].map((row) => row.map(csvCell).join(';')).join('\r\n') + '\r\n'
}

/**
 * One cell: quoted when it holds a separator, a quote or a line break. A cell a spreadsheet would
 * run as a formula (a route named "=1+1") is prefixed with an apostrophe.
 */
export function csvCell(value: string): string {
  const safe = /^[=+\-@\t\r]/.test(value) ? `'${value}` : value
  return /[;"\r\n]/.test(safe) ? `"${safe.replaceAll('"', '""')}"` : safe
}

/** "gg-pocket-frequentation-2026-10.csv". */
export function attendanceFileName(attendance: Pick<MonthAttendance, 'month'>): string {
  return `gg-pocket-frequentation-${attendance.month}.csv`
}

/** A bar of a distribution: what it counts, and its length relative to the longest one. */
export interface Bar<T> {
  item: T
  count: number
  /** 0 to 100: the longest bar is 100. */
  percent: number
}

function bars<T>(counts: readonly { item: T; count: number }[]): Bar<T>[] {
  const max = Math.max(0, ...counts.map((entry) => entry.count))
  return counts.map((entry) => ({
    ...entry,
    percent: max === 0 ? 0 : Math.round((100 * entry.count) / max),
  }))
}

/** "6a" for 6a-, 6a and 6a+: the steps of the grade histogram. */
export function baseGrade(code: string): string {
  return code.replace(/[+-]$/, '')
}

/**
 * Routes per grade, easiest first, the "-" and "+" counted with their grade (6a-, 6a, 6a+ make
 * "6a"). Every grade between the easiest and the hardest route on the wall is shown, even without
 * any route: the gaps are what a setter looks for.
 */
export function gradeDistribution(
  routes: readonly RouteSummary[],
  grades: readonly Grade[],
): Bar<string>[] {
  if (routes.length === 0) return []
  const steps = new Map<string, number[]>()
  for (const grade of [...grades].sort((a, b) => a.rank - b.rank)) {
    const step = baseGrade(grade.code)
    steps.set(step, [...(steps.get(step) ?? []), grade.rank])
  }
  const counts = [...steps.entries()].map(([step, ranks]) => ({
    item: step,
    count: routes.filter((route) => ranks.includes(route.gradeRank)).length,
  }))
  const first = counts.findIndex((entry) => entry.count > 0)
  const last = counts.length - 1 - [...counts].reverse().findIndex((entry) => entry.count > 0)
  return bars(first === -1 ? [] : counts.slice(first, last + 1))
}

/** Routes per sector, in the order of the wall; sectors without any route too. */
export function sectorDistribution(
  routes: readonly RouteSummary[],
  sectors: readonly Sector[],
): Bar<Sector>[] {
  return bars(
    [...sectors]
      .sort((a, b) => a.position - b.position)
      .map((sector) => ({
        item: sector,
        count: routes.filter((route) => route.sectorId === sector.id).length,
      })),
  )
}

/** A cord of the wall and the routes on it, easiest first. */
export interface CordCell {
  number: number
  sectorName: string
  routes: RouteSummary[]
}

/** Every cord in use, in number order, with its routes; an unused cord with routes still shows. */
export function cordGrid(routes: readonly RouteSummary[], sectors: readonly Sector[]): CordCell[] {
  const cells = new Map<number, CordCell>()
  for (const sector of sectors) {
    for (const cord of sector.cords) {
      if (cord.active)
        cells.set(cord.number, { number: cord.number, sectorName: sector.name, routes: [] })
    }
  }
  for (const route of routes) {
    const cell = cells.get(route.cordNumber) ?? {
      number: route.cordNumber,
      sectorName: route.sectorName,
      routes: [],
    }
    cell.routes.push(route)
    cells.set(route.cordNumber, cell)
  }
  return [...cells.values()]
    .sort((a, b) => a.number - b.number)
    .map((cell) => ({ ...cell, routes: cell.routes.sort((a, b) => a.gradeRank - b.gradeRank) }))
}

/** The most climbed routes since they opened, at most `limit`; ties by cord. */
export function mostClimbed(lines: readonly RouteAttendance[], limit: number): RouteAttendance[] {
  return sortAttendance(
    lines.filter((line) => line.attendance.ascentCount > 0),
    'ascents',
  ).slice(0, limit)
}

/** The least climbed routes, the oldest first among equals: those are the ones to question. */
export function leastClimbed(lines: readonly RouteAttendance[], limit: number): RouteAttendance[] {
  return [...lines]
    .sort(
      (a, b) =>
        a.attendance.ascentCount - b.attendance.ascentCount ||
        a.route.openedOn.localeCompare(b.route.openedOn) ||
        byCord(a, b),
    )
    .slice(0, limit)
}

/** The routes on the wall for the longest time first. */
export function oldestFirst(routes: readonly RouteSummary[]): RouteSummary[] {
  return [...routes].sort(
    (a, b) =>
      a.openedOn.localeCompare(b.openedOn) ||
      a.cordNumber - b.cordNumber ||
      a.gradeRank - b.gradeRank,
  )
}

/** The routes with at least one favourite, most loved first, at most `limit`. */
export function mostLoved(routes: readonly RouteSummary[], limit: number): RouteSummary[] {
  return routes
    .filter((route) => route.favoriteCount > 0)
    .sort(
      (a, b) =>
        b.favoriteCount - a.favoriteCount ||
        a.cordNumber - b.cordNumber ||
        a.gradeRank - b.gradeRank,
    )
    .slice(0, limit)
}

/** How long a route has been on the wall: days for two weeks, then weeks, then months. */
export type WallAge = { unit: 'days' | 'weeks' | 'months'; value: number }

export function wallAge(openedOn: string, today: string): WallAge {
  const days = Math.max(
    0,
    Math.round(
      (Date.parse(`${today}T12:00:00Z`) - Date.parse(`${openedOn}T12:00:00Z`)) / 86_400_000,
    ),
  )
  if (days < 14) return { unit: 'days', value: days }
  if (days < 61) return { unit: 'weeks', value: Math.floor(days / 7) }
  // Calendar months: a route opened a year ago today is 12 months old.
  const [openedYear = 0, openedMonth = 1, openedDay = 1] = openedOn.split('-').map(Number)
  const [year = 0, month = 1, day = 1] = today.split('-').map(Number)
  return {
    unit: 'months',
    value: (year - openedYear) * 12 + month - openedMonth - (day < openedDay ? 1 : 0),
  }
}
