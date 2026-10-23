import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent } from 'vue'

import { api } from './client'
import { ApiError } from './errors'
import { ROUTES_KEY, TOPO_CONFIG_KEY, type RouteDetail, type TopoConfig } from './topo'
import {
  RoutePhotoNotSaved,
  useReorderSectors,
  useSaveRoute,
  type RouteSettings,
} from './topoAdmin'

vi.mock('./client', () => ({ api: { GET: vi.fn(), PUT: vi.fn(), DELETE: vi.fn(), POST: vi.fn() } }))

const SETTINGS: RouteSettings = {
  name: 'Sec !',
  gradeId: 'g',
  cordId: 'c',
  holdColorIds: ['jaune'],
  openedOn: '2026-10-20',
  setterIds: ['m'],
  styles: [],
}

const detail = (photoUrl?: string): RouteDetail => ({
  route: {
    id: 'r9',
    name: 'Sec !',
    grade: '5b',
    gradeRank: 9,
    cordNumber: 5,
    sectorId: 's',
    sectorName: 'Neutre 12 m',
    sectorPosition: 2,
    holds: [],
    status: 'ACTIVE',
    openedOn: '2026-10-20',
    photoUrl,
    styles: [],
    favoriteCount: 0,
    commentCount: 0,
    toppedCount: 0,
    favorite: false,
    checks: [],
  },
  gradeId: 'g',
  cordId: 'c',
  setters: [],
  createdAt: '2026-10-20T08:00:00Z',
  stats: { ascentCount: 0, toppedCount: 0, climberCount: 0 },
  myAscents: [],
})

const ok = (data: unknown, status = 200) => ({
  data,
  error: undefined,
  response: new Response(null, { status }),
})
const refused = (status: number, code: string) => ({
  data: undefined,
  error: { status, code },
  response: new Response(null, { status }),
})

/** Runs composables inside a component, as the app does. */
function withQueries<T>(use: () => T) {
  const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  let result: T | undefined
  mount(
    defineComponent({
      setup() {
        result = use()
        return () => null
      },
    }),
    { global: { plugins: [[VueQueryPlugin, { queryClient }]] } },
  )
  if (result === undefined) throw new Error('setup did not run')
  return { result, queryClient }
}

beforeEach(() => {
  vi.resetAllMocks()
})

describe('useSaveRoute', () => {
  it('creates the route, then sends its photo, and keeps the sheet up to date', async () => {
    vi.mocked(api.POST).mockResolvedValue(ok(detail(), 201))
    vi.mocked(api.PUT).mockResolvedValue(ok(detail('/api/v1/media/p9')))
    const { result, queryClient } = withQueries(() => useSaveRoute())

    const saved = await result.mutateAsync({
      settings: SETTINGS,
      photo: { kind: 'replace', photo: new Blob(['jpeg']) },
    })

    expect(api.POST).toHaveBeenCalledWith('/api/v1/routes', { body: SETTINGS })
    expect(vi.mocked(api.PUT).mock.calls[0]?.[0]).toBe('/api/v1/routes/{id}/photo')
    expect(saved.route.photoUrl).toBe('/api/v1/media/p9')
    expect(queryClient.getQueryData<RouteDetail>([...ROUTES_KEY, 'r9'])?.route.photoUrl).toBe(
      '/api/v1/media/p9',
    )
  })

  it('removes the photo of an edited route', async () => {
    vi.mocked(api.PUT).mockResolvedValue(ok(detail('/api/v1/media/old')))
    vi.mocked(api.DELETE).mockResolvedValue(ok(undefined, 204))
    const { result } = withQueries(() => useSaveRoute())

    const saved = await result.mutateAsync({
      id: 'r9',
      settings: SETTINGS,
      photo: { kind: 'remove' },
    })

    expect(api.PUT).toHaveBeenCalledWith('/api/v1/routes/{id}', {
      params: { path: { id: 'r9' } },
      body: SETTINGS,
    })
    expect(api.DELETE).toHaveBeenCalledWith('/api/v1/routes/{id}/photo', {
      params: { path: { id: 'r9' } },
    })
    expect(saved.route.photoUrl).toBeUndefined()
  })

  it('says the route is saved when only its photo failed', async () => {
    vi.mocked(api.POST).mockResolvedValue(ok(detail(), 201))
    vi.mocked(api.PUT).mockResolvedValue(refused(400, 'IMAGE_UNSUPPORTED'))
    const { result } = withQueries(() => useSaveRoute())

    const failure: unknown = await result
      .mutateAsync({ settings: SETTINGS, photo: { kind: 'replace', photo: new Blob(['?']) } })
      .catch((error: unknown) => error)

    expect(failure).toBeInstanceOf(RoutePhotoNotSaved)
    const notSaved = failure as RoutePhotoNotSaved
    expect(notSaved.saved.route.id).toBe('r9')
    expect(notSaved.cause).toBeInstanceOf(ApiError)
  })
})

describe('useReorderSectors', () => {
  const config = (ids: string[]): TopoConfig => ({
    sectors: ids.map((id, index) => ({ id, name: id, position: index + 1, cords: [] })),
    holdColors: [],
    grades: [],
  })
  const order = (queryClient: QueryClient) =>
    queryClient.getQueryData<TopoConfig>(TOPO_CONFIG_KEY)?.sectors.map((sector) => sector.id)

  it('shows the new order at once, and puts it back when the server refuses', async () => {
    vi.mocked(api.PUT).mockResolvedValue(refused(403, 'ACCESS_DENIED'))
    const { result, queryClient } = withQueries(() => useReorderSectors())
    queryClient.setQueryData(TOPO_CONFIG_KEY, config(['a', 'b']))

    result.mutate({ sectorIds: ['b', 'a'], preview: config(['b', 'a']) })
    await flushPromises()

    expect(api.PUT).toHaveBeenCalledWith('/api/v1/admin/club/sector-order', {
      body: { sectorIds: ['b', 'a'] },
    })
    expect(order(queryClient)).toEqual(['a', 'b'])
  })

  it('keeps the configuration the server answers', async () => {
    vi.mocked(api.PUT).mockResolvedValue(ok(config(['b', 'a'])))
    const { result, queryClient } = withQueries(() => useReorderSectors())
    queryClient.setQueryData(TOPO_CONFIG_KEY, config(['a', 'b']))

    await result.mutateAsync({ sectorIds: ['b', 'a'], preview: config(['b', 'a']) })
    expect(order(queryClient)).toEqual(['b', 'a'])
  })
})
