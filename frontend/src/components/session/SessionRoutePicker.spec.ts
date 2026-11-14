import { describe, expect, it } from 'vitest'

import type { RouteSummary } from '@/api/topo'
import { mountWithApp } from '@/test/render'

import SessionRoutePicker from './SessionRoutePicker.vue'

const route = (
  id: string,
  name: string,
  cordNumber: number,
  sector: { id: string; name: string; position: number },
  grade: string,
  gradeRank: number,
): RouteSummary => ({
  id,
  name,
  grade,
  gradeRank,
  cordNumber,
  sectorId: sector.id,
  sectorName: sector.name,
  sectorPosition: sector.position,
  holds: [{ id: 'c1', label: 'jaune', hex: '#f5c518', multicolor: false }],
  status: 'ACTIVE',
  openedOn: '2026-05-16',
  styles: [],
  favoriteCount: 0,
  commentCount: 0,
  toppedCount: 0,
  favorite: false,
  checks: [],
})

const dalle = { id: 's1', name: 'Dalle', position: 1 }
const devers = { id: 's2', name: 'Dévers', position: 2 }
const routes = [
  route('r3', 'Iron Man', 14, devers, '6c', 18),
  route('r1', 'Sec !', 5, dalle, '7a', 21),
  route('r2', 'Réglisse', 7, dalle, '5b', 8),
]

function mountPicker() {
  return mountWithApp(SessionRoutePicker, { props: { routes, recentIds: ['r3'] } })
}

const toggle = (wrapper: ReturnType<typeof mountPicker>) => wrapper.get('button[aria-expanded]')
const listedNames = (wrapper: ReturnType<typeof mountPicker>) =>
  wrapper.findAll('li').map((item) => item.get('button:last-child').attributes('aria-label'))

describe('SessionRoutePicker, every route on the wall', () => {
  it('starts folded: only my last route is offered', () => {
    const wrapper = mountPicker()
    expect(toggle(wrapper).attributes('aria-expanded')).toBe('false')
    expect(toggle(wrapper).text()).toBe('Voir les 3 voies du mur')
    expect(listedNames(wrapper)).toEqual(['Ajouter Iron Man'])
  })

  it('unfolds every route, easiest first, then folds back', async () => {
    const wrapper = mountPicker()
    await toggle(wrapper).trigger('click')
    expect(toggle(wrapper).attributes('aria-expanded')).toBe('true')
    expect(toggle(wrapper).text()).toBe('Replier la liste des voies')
    expect(wrapper.findAll('h3')).toHaveLength(0)
    expect(listedNames(wrapper)).toEqual(['Ajouter Réglisse', 'Ajouter Iron Man', 'Ajouter Sec !'])

    await wrapper.get('li button').trigger('click')
    expect(wrapper.emitted('add')).toEqual([['r2']])

    await toggle(wrapper).trigger('click')
    expect(listedNames(wrapper)).toEqual(['Ajouter Iron Man'])
  })

  it('gives way to the search, and comes back when it is cleared', async () => {
    const wrapper = mountPicker()
    await toggle(wrapper).trigger('click')
    await wrapper.get('input[type="search"]').setValue('regli')
    expect(wrapper.find('button[aria-expanded]').exists()).toBe(false)
    expect(listedNames(wrapper)).toEqual(['Ajouter Réglisse'])

    await wrapper.get('input[type="search"]').setValue('')
    expect(toggle(wrapper).attributes('aria-expanded')).toBe('true')
    expect(listedNames(wrapper)).toHaveLength(3)
  })

  it('has nothing to unfold on an empty wall', () => {
    const wrapper = mountWithApp(SessionRoutePicker, { props: { routes: [], recentIds: [] } })
    expect(wrapper.find('button[aria-expanded]').exists()).toBe(false)
  })
})
