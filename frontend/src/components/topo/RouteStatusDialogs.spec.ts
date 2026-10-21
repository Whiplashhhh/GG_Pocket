import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { flushPromises } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

import type { RouteSummary } from '@/api/topo'
import { mountWithApp } from '@/test/render'

import RouteStatusDialogs from './RouteStatusDialogs.vue'

vi.mock('@/api/client', () => ({
  api: { GET: vi.fn(), PUT: vi.fn(), DELETE: vi.fn(), POST: vi.fn() },
}))

const morice = (favoriteCount: number): RouteSummary => ({
  id: 'r1',
  name: 'Bye Bye Morice',
  grade: '6b',
  gradeRank: 15,
  cordNumber: 12,
  sectorId: 's4',
  sectorName: 'Négatif et ses 2 dièdres',
  sectorPosition: 4,
  holds: [{ id: 'c1', label: 'jaune', hex: '#f5c518', multicolor: false }],
  status: 'ACTIVE',
  openedOn: '2026-05-16',
  styles: [],
  favoriteCount,
  commentCount: 0,
  favorite: false,
  checks: [],
})

async function askToDismount(favoriteCount: number): Promise<string> {
  mountWithApp(RouteStatusDialogs, {
    props: { action: { kind: 'dismount', route: morice(favoriteCount) } },
    attachTo: document.body,
    global: { plugins: [[VueQueryPlugin, { queryClient: new QueryClient() }]] },
  })
  await flushPromises()
  return document.body.textContent
}

afterEach(() => {
  document.body.innerHTML = ''
})

describe('RouteStatusDialogs', () => {
  it('warns explicitly when climbers have the route as a favourite (spec §8.10)', async () => {
    const text = await askToDismount(4)
    expect(text).toContain('Démonter « Bye Bye Morice » ?')
    expect(text).toContain('4 grimpeurs l’ont en coup de cœur. Tu es sûr·e ?')
  })

  it('says it in the singular for one climber', async () => {
    expect(await askToDismount(1)).toContain('1 grimpeur l’a en coup de cœur. Tu es sûr·e ?')
  })

  it('does not warn about favourites nobody gave', async () => {
    const text = await askToDismount(0)
    expect(text).toContain('Elle part aux archives')
    expect(text).not.toContain('coup de cœur')
  })
})
