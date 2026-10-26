import type { LocationQuery, LocationQueryRaw } from 'vue-router'

import type { TodoRoute } from '@/api/todo'

import type { ClimbMode } from './route'

export type TodoDirection = 'asc' | 'desc'

/** The "à faire" screen as the URL keeps it: `?mode=tete&secteur=…&cotation=6a&ordre=desc`. */
export interface TodoView {
  mode: ClimbMode
  sectorId?: string
  /** A grade family: "6a" covers 6a-, 6a and 6a+. */
  family?: string
  direction: TodoDirection
}

const FIELD = { TOP_ROPE: 'topRope', LEAD: 'lead' } as const satisfies Record<
  ClimbMode,
  keyof TodoRoute
>

export function stateIn(item: TodoRoute, mode: ClimbMode) {
  return item[FIELD[mode]]
}

/** "6a+" → "6a", "4a-" → "4a". */
export function gradeFamily(grade: string): string {
  return grade.replace(/[+-]$/, '')
}

const single = (value: LocationQuery[string] | undefined) =>
  typeof value === 'string' ? value : undefined

export function parseTodoQuery(query: LocationQuery): TodoView {
  const sectorId = single(query.secteur)
  const family = single(query.cotation)
  return {
    mode: single(query.mode) === 'tete' ? 'LEAD' : 'TOP_ROPE',
    ...(sectorId ? { sectorId } : {}),
    ...(family ? { family } : {}),
    direction: single(query.ordre) === 'desc' ? 'desc' : 'asc',
  }
}

export function toTodoQuery(view: TodoView): LocationQueryRaw {
  return {
    ...(view.mode === 'LEAD' ? { mode: 'tete' } : {}),
    ...(view.sectorId ? { secteur: view.sectorId } : {}),
    ...(view.family ? { cotation: view.family } : {}),
    ...(view.direction === 'desc' ? { ordre: 'desc' } : {}),
  }
}

/** The routes still to do in the view's mode (state TODO), filtered and sorted by grade. */
export function todoRoutes(routes: readonly TodoRoute[], view: TodoView): TodoRoute[] {
  return arrange(
    routes.filter((item) => stateIn(item, view.mode) === 'TODO'),
    view,
  )
}

/** The routes I know in the view's mode, filtered and sorted the same way. */
export function knownRoutes(routes: readonly TodoRoute[], view: TodoView): TodoRoute[] {
  return arrange(
    routes.filter((item) => stateIn(item, view.mode) !== 'TODO'),
    view,
  )
}

function arrange(routes: TodoRoute[], view: TodoView): TodoRoute[] {
  const sign = view.direction === 'asc' ? 1 : -1
  return routes
    .filter((item) => view.sectorId === undefined || item.route.sectorId === view.sectorId)
    .filter((item) => view.family === undefined || gradeFamily(item.route.grade) === view.family)
    .sort(
      (a, b) =>
        sign * (a.route.gradeRank - b.route.gradeRank) ||
        a.route.name.localeCompare(b.route.name, 'fr'),
    )
}

export interface SectorOption {
  id: string
  name: string
}

/** The sectors that have routes on the wall, in wall order. */
export function sectorOptions(routes: readonly TodoRoute[]): SectorOption[] {
  const byId = new Map<string, { name: string; position: number }>()
  for (const { route } of routes) {
    byId.set(route.sectorId, { name: route.sectorName, position: route.sectorPosition })
  }
  return [...byId.entries()]
    .sort(([, a], [, b]) => a.position - b.position)
    .map(([id, { name }]) => ({ id, name }))
}

/** The grade families present on the wall, easiest first. */
export function familyOptions(routes: readonly TodoRoute[]): string[] {
  const lowest = new Map<string, number>()
  for (const { route } of routes) {
    const family = gradeFamily(route.grade)
    lowest.set(family, Math.min(lowest.get(family) ?? Infinity, route.gradeRank))
  }
  return [...lowest.entries()].sort(([, a], [, b]) => a - b).map(([family]) => family)
}
