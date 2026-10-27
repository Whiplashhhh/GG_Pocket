import { RouterLinkStub } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import type { RouteSummary } from '@/api/topo'
import { mountWithApp } from '@/test/render'

import RouteRow from './RouteRow.vue'

const tonton: RouteSummary = {
  id: 'r3',
  name: 'Tonton Talon',
  grade: '6a+',
  gradeRank: 14,
  cordNumber: 15,
  sectorId: 's5',
  sectorName: 'Grand dévers',
  sectorPosition: 5,
  holds: [{ id: 'c1', label: 'orange', hex: '#f28c28', multicolor: false }],
  status: 'LAST_CHANCE',
  openedOn: '2026-03-07',
  plannedDismountOn: '2026-11-02',
  styles: ['PHYSICAL', 'OVERHANG'],
  favoriteCount: 2,
  commentCount: 0,
  toppedCount: 0,
  favorite: false,
  checks: ['LEAD'],
}

function mountRow(route: RouteSummary) {
  return mountWithApp(RouteRow, {
    props: { route, to: `/topo/voies/${route.id}` },
    global: { stubs: { RouterLink: RouterLinkStub } },
  })
}

describe('RouteRow', () => {
  it('is a link named by the route label and described by the rest', () => {
    const wrapper = mountRow(tonton)
    const link = wrapper.get('a')
    const name = wrapper.get(`#${link.attributes('aria-labelledby') ?? ''}`)
    expect(name.text()).toBe('6a+, Tonton Talon, couleur orange, corde 15, dernière chance')
    const description = wrapper.get(`#${link.attributes('aria-describedby') ?? ''}`).text()
    expect(description).toContain('Grand dévers')
    expect(description).toContain('Physique Dévers')
    expect(description).toContain('2 coups de cœur')
    expect(description).toContain('Je sais la faire en tête')
    expect(description).not.toContain('mouli')
    expect(description).toContain('Démontage prévu le 2 novembre 2026')
  })

  it('leaves out counters at zero', () => {
    const wrapper = mountRow({ ...tonton, favoriteCount: 0, checks: [], status: 'ACTIVE' })
    const text = wrapper.text()
    expect(text).not.toContain('coup de cœur')
    expect(text).not.toContain('commentaire')
    expect(text).not.toContain('Démontage prévu')
  })
})
