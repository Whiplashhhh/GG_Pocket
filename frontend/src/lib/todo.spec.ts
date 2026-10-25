import { describe, expect, it } from 'vitest'

import type { RouteSummary } from '@/api/topo'
import type { TodoRoute, TodoState } from '@/api/todo'

import {
  familyOptions,
  gradeFamily,
  knownRoutes,
  parseTodoQuery,
  sectorOptions,
  todoRoutes,
  toTodoQuery,
  type TodoView,
} from './todo'

const NEUTRAL = { sectorId: 's-neutre', sectorName: 'Neutre 8 m', sectorPosition: 1 }
const NEGATIVE = { sectorId: 's-negatif', sectorName: 'Négatif', sectorPosition: 4 }

function item(
  route: Partial<RouteSummary> & Pick<RouteSummary, 'id' | 'name' | 'grade' | 'gradeRank'>,
  topRope: TodoState,
  lead: TodoState,
): TodoRoute {
  return {
    route: {
      cordNumber: 1,
      ...NEUTRAL,
      holds: [],
      status: 'ACTIVE',
      openedOn: '2026-09-01',
      styles: [],
      favoriteCount: 0,
      commentCount: 0,
      toppedCount: 0,
      favorite: false,
      checks: [],
      ...route,
    },
    topRope,
    lead,
  }
}

const easy = item({ id: 'a', name: 'Apéro', grade: '5a', gradeRank: 7 }, 'TODO', 'TODO')
const known = item(
  { id: 'b', name: 'Bye Bye Morice', grade: '6a', gradeRank: 13 },
  'CHECKED',
  'TODO',
)
const plus = item(
  { id: 'c', name: 'Cachou', grade: '6a+', gradeRank: 14, ...NEGATIVE },
  'FROM_LEAD',
  'FROM_ASCENT',
)
const hard = item(
  { id: 'd', name: 'Dalle', grade: '6c', gradeRank: 18, ...NEGATIVE },
  'TODO',
  'TODO',
)
const all = [hard, plus, known, easy]

const view = (overrides: Partial<TodoView> = {}): TodoView => ({
  mode: 'TOP_ROPE',
  direction: 'asc',
  ...overrides,
})

describe('todo', () => {
  it('lists the routes still to do in the mode, easiest first', () => {
    expect(todoRoutes(all, view()).map((i) => i.route.id)).toEqual(['a', 'd'])
    expect(todoRoutes(all, view({ mode: 'LEAD' })).map((i) => i.route.id)).toEqual(['a', 'b', 'd'])
    expect(todoRoutes(all, view({ direction: 'desc' })).map((i) => i.route.id)).toEqual(['d', 'a'])
  })

  it('lists the known ones whatever made them known', () => {
    expect(knownRoutes(all, view()).map((i) => i.route.id)).toEqual(['b', 'c'])
    expect(knownRoutes(all, view({ mode: 'LEAD' })).map((i) => i.route.id)).toEqual(['c'])
  })

  it('filters by sector and by grade family', () => {
    expect(todoRoutes(all, view({ sectorId: 's-negatif' })).map((i) => i.route.id)).toEqual(['d'])
    expect(knownRoutes(all, view({ family: '6a' })).map((i) => i.route.id)).toEqual(['b', 'c'])
  })

  it('groups grades by family', () => {
    expect(gradeFamily('6a+')).toBe('6a')
    expect(gradeFamily('4a-')).toBe('4a')
    expect(gradeFamily('7c')).toBe('7c')
    expect(familyOptions(all)).toEqual(['5a', '6a', '6c'])
  })

  it('offers the sectors in wall order', () => {
    expect(sectorOptions(all)).toEqual([
      { id: 's-neutre', name: 'Neutre 8 m' },
      { id: 's-negatif', name: 'Négatif' },
    ])
  })

  it('keeps the view in the URL', () => {
    const full = view({ mode: 'LEAD', sectorId: 's1', family: '6a', direction: 'desc' })
    expect(toTodoQuery(full)).toEqual({
      mode: 'tete',
      secteur: 's1',
      cotation: '6a',
      ordre: 'desc',
    })
    expect(parseTodoQuery({ mode: 'tete', secteur: 's1', cotation: '6a', ordre: 'desc' })).toEqual(
      full,
    )
    expect(toTodoQuery(view())).toEqual({})
    expect(parseTodoQuery({ mode: 'n’importe', ordre: ['desc'] })).toEqual(view())
  })
})
