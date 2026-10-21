import type { LocationQuery, LocationQueryRaw } from 'vue-router'

import type { RouteSummary } from '@/api/topo'

import { ROUTE_STYLES, type RouteStyle } from './route'

/**
 * Sorting, filtering and grouping of the topo, done in the browser (DECISIONS D-078), with their
 * state kept in the URL query so a shared link or a step back finds the same list.
 * Sorting by ascents arrives with sessions (phase 4, D-080).
 */
export const TOPO_SORTS = ['grade', 'opened', 'sector', 'favorites', 'comments'] as const
export type TopoSort = (typeof TOPO_SORTS)[number]
export type SortDirection = 'asc' | 'desc'

export interface TopoFilters {
  sectorIds: string[]
  /** Grade ranks (Grade.rank), both ends included. */
  minRank?: number
  maxRank?: number
  /** Routes with at least one of these hold colours. */
  holdColorIds: string[]
  /** Routes with at least one of these styles. */
  styles: RouteStyle[]
  /** "Pas encore faite par moi": neither in top rope nor in lead. */
  notDoneByMe: boolean
}

export interface TopoView {
  sort: TopoSort
  direction: SortDirection
  filters: TopoFilters
}

/** What reads naturally first: easy grades, new routes, the wall from left to right, the most loved. */
const DEFAULT_DIRECTION: Record<TopoSort, SortDirection> = {
  grade: 'asc',
  opened: 'desc',
  sector: 'asc',
  favorites: 'desc',
  comments: 'desc',
}

export function defaultDirection(sort: TopoSort): SortDirection {
  return DEFAULT_DIRECTION[sort]
}

export const NO_FILTERS: TopoFilters = {
  sectorIds: [],
  holdColorIds: [],
  styles: [],
  notDoneByMe: false,
}

/** Query parameter names, in French: they show in shared links. */
const PARAM = {
  sort: 'tri',
  direction: 'ordre',
  sector: 'secteur',
  min: 'min',
  max: 'max',
  color: 'couleur',
  style: 'style',
  notDone: 'pas-faite',
} as const

const SORT_PARAM: Record<TopoSort, string> = {
  grade: 'cotation',
  opened: 'ouverture',
  sector: 'secteur',
  favorites: 'coups-de-coeur',
  comments: 'commentaires',
}

function values(query: LocationQuery, name: string): string[] {
  const raw = query[name]
  const list = Array.isArray(raw) ? raw : [raw]
  return list.filter((value): value is string => typeof value === 'string' && value !== '')
}

function rank(query: LocationQuery, name: string): number | undefined {
  const [value] = values(query, name)
  const parsed = value === undefined ? NaN : Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined
}

/** Reads the topo state from the URL; anything unknown falls back to the default. */
export function parseTopoQuery(query: LocationQuery): TopoView {
  const [sortParam] = values(query, PARAM.sort)
  const sort = TOPO_SORTS.find((candidate) => SORT_PARAM[candidate] === sortParam) ?? 'grade'
  const [directionParam] = values(query, PARAM.direction)
  const direction =
    directionParam === 'asc' || directionParam === 'desc' ? directionParam : defaultDirection(sort)
  return {
    sort,
    direction,
    filters: {
      sectorIds: values(query, PARAM.sector),
      minRank: rank(query, PARAM.min),
      maxRank: rank(query, PARAM.max),
      holdColorIds: values(query, PARAM.color),
      styles: values(query, PARAM.style).filter((style): style is RouteStyle =>
        ROUTE_STYLES.includes(style as RouteStyle),
      ),
      notDoneByMe: values(query, PARAM.notDone).includes('1'),
    },
  }
}

/** The URL query of a topo state, without the defaults so plain links stay plain. */
export function toTopoQuery(view: TopoView): LocationQueryRaw {
  const { filters } = view
  const query: LocationQueryRaw = {}
  if (view.sort !== 'grade') query[PARAM.sort] = SORT_PARAM[view.sort]
  if (view.direction !== defaultDirection(view.sort)) query[PARAM.direction] = view.direction
  if (filters.sectorIds.length > 0) query[PARAM.sector] = filters.sectorIds
  if (filters.minRank !== undefined) query[PARAM.min] = String(filters.minRank)
  if (filters.maxRank !== undefined) query[PARAM.max] = String(filters.maxRank)
  if (filters.holdColorIds.length > 0) query[PARAM.color] = filters.holdColorIds
  if (filters.styles.length > 0) query[PARAM.style] = filters.styles
  if (filters.notDoneByMe) query[PARAM.notDone] = '1'
  return query
}

/** Number shown on the "Filtres" button: one per kind of filter in use. */
export function activeFilterCount(filters: TopoFilters): number {
  return [
    filters.sectorIds.length > 0,
    filters.minRank !== undefined || filters.maxRank !== undefined,
    filters.holdColorIds.length > 0,
    filters.styles.length > 0,
    filters.notDoneByMe,
  ].filter(Boolean).length
}

export function filterRoutes(routes: readonly RouteSummary[], filters: TopoFilters) {
  return routes.filter(
    (route) =>
      (filters.sectorIds.length === 0 || filters.sectorIds.includes(route.sectorId)) &&
      (filters.minRank === undefined || route.gradeRank >= filters.minRank) &&
      (filters.maxRank === undefined || route.gradeRank <= filters.maxRank) &&
      (filters.holdColorIds.length === 0 ||
        route.holds.some((hold) => filters.holdColorIds.includes(hold.id))) &&
      (filters.styles.length === 0 ||
        route.styles.some((style) => filters.styles.includes(style))) &&
      (!filters.notDoneByMe || route.checks.length === 0),
  )
}

const NAMES = new Intl.Collator('fr', { sensitivity: 'base', numeric: true })

type Compare = (a: RouteSummary, b: RouteSummary) => number

const byGrade: Compare = (a, b) => a.gradeRank - b.gradeRank
const byCord: Compare = (a, b) => a.cordNumber - b.cordNumber
const byName: Compare = (a, b) => NAMES.compare(a.name, b.name) || a.id.localeCompare(b.id)
const bySector: Compare = (a, b) => a.sectorPosition - b.sectorPosition

/** The main key of each sort; ties are broken the same way whatever the direction. */
const MAIN_KEY: Record<TopoSort, Compare> = {
  grade: byGrade,
  opened: (a, b) => a.openedOn.localeCompare(b.openedOn),
  sector: (a, b) => bySector(a, b) || byCord(a, b),
  favorites: (a, b) => a.favoriteCount - b.favoriteCount,
  comments: (a, b) => a.commentCount - b.commentCount,
}

/** Ties: easier grade first, then cord from left to right, then name. */
const TIE_BREAK: Compare = (a, b) => byGrade(a, b) || byCord(a, b) || byName(a, b)

export function sortRoutes(
  routes: readonly RouteSummary[],
  sort: TopoSort,
  direction: SortDirection,
): RouteSummary[] {
  const sign = direction === 'asc' ? 1 : -1
  const main = MAIN_KEY[sort]
  return [...routes].sort((a, b) => sign * main(a, b) || TIE_BREAK(a, b))
}

export interface SectorGroup {
  sectorId: string
  sectorName: string
  routes: RouteSummary[]
}

/** Consecutive routes of the same sector, for the list sorted by sector. Keeps the order. */
export function groupBySector(sorted: readonly RouteSummary[]): SectorGroup[] {
  const groups: SectorGroup[] = []
  for (const route of sorted) {
    const last = groups.at(-1)
    if (last?.sectorId === route.sectorId) {
      last.routes.push(route)
    } else {
      groups.push({ sectorId: route.sectorId, sectorName: route.sectorName, routes: [route] })
    }
  }
  return groups
}

/** The whole topo pipeline: filters, then sort. */
export function applyTopoView(routes: readonly RouteSummary[], view: TopoView): RouteSummary[] {
  return sortRoutes(filterRoutes(routes, view.filters), view.sort, view.direction)
}

/** Lower case without accents, to search "reglisse" and find « Réglisse ». */
function fold(text: string): string {
  return text
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim()
}

/**
 * Route picker search: by cord number ("12"), name, hold colour or grade ("6a" finds 6a and 6a+).
 * Every word must match something.
 */
export function matchesRouteSearch(route: RouteSummary, search: string): boolean {
  const words = fold(search).split(/\s+/).filter(Boolean)
  const name = fold(route.name)
  const colors = route.holds.map((hold) => fold(hold.label))
  const grade = route.grade.toLowerCase()
  return words.every(
    (word) =>
      (/^\d+$/.test(word) && String(route.cordNumber) === word) ||
      name.includes(word) ||
      colors.some((color) => color.includes(word)) ||
      grade.startsWith(word),
  )
}

/** Picker results: matches sorted by cord, then grade, then name. */
export function searchRoutes(routes: readonly RouteSummary[], search: string): RouteSummary[] {
  return routes
    .filter((route) => matchesRouteSearch(route, search))
    .sort((a, b) => byCord(a, b) || byGrade(a, b) || byName(a, b))
}
