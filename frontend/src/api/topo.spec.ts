import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent } from 'vue'

import { api } from './client'
import { ApiError } from './errors'
import {
  ACKNOWLEDGEMENT_BATCH,
  MY_FAVORITES_KEY,
  ROUTES_KEY,
  UNACKNOWLEDGED_LAST_CHANCE_KEY,
  useAcknowledgeLastChance,
  useAddFavorite,
  useToggleCheck,
  type RouteSummary,
} from './topo'

vi.mock('./client', () => ({ api: { GET: vi.fn(), PUT: vi.fn(), DELETE: vi.fn(), POST: vi.fn() } }))

const route = (id: string, overrides: Partial<RouteSummary> = {}): RouteSummary => ({
  id,
  name: id,
  grade: '6a',
  gradeRank: 13,
  cordNumber: 12,
  sectorId: 's',
  sectorName: 'Négatif',
  sectorPosition: 4,
  holds: [],
  status: 'ACTIVE',
  openedOn: '2026-09-01',
  styles: [],
  favoriteCount: 3,
  commentCount: 0,
  favorite: false,
  checks: [],
  ...overrides,
})

const noContent = {
  data: undefined,
  error: undefined,
  response: new Response(null, { status: 204 }),
}
const conflict = (code: string) => ({
  data: undefined,
  error: { status: 409, code },
  response: new Response(null, { status: 409 }),
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

function deferred<T>() {
  let resolve: (value: T) => void = () => undefined
  const promise = new Promise<T>((done) => {
    resolve = done
  })
  return { promise, resolve }
}

const routesIn = (queryClient: QueryClient, key: readonly unknown[]) =>
  queryClient.getQueryData<RouteSummary[]>(key) ?? []

beforeEach(() => {
  vi.resetAllMocks()
})

describe('useToggleCheck', () => {
  it('ticks at once, and unticks again when the server refuses', async () => {
    const answer = deferred<unknown>()
    vi.mocked(api.PUT).mockReturnValue(answer.promise)
    const { result, queryClient } = withQueries(() => useToggleCheck())
    queryClient.setQueryData(ROUTES_KEY, [route('r1'), route('r2')])

    result.mutate({ id: 'r1', mode: 'LEAD', checked: true })
    await flushPromises()
    expect(routesIn(queryClient, ROUTES_KEY).map((item) => item.checks)).toEqual([['LEAD'], []])

    answer.resolve(conflict('ROUTE_DISMOUNTED'))
    await flushPromises()
    expect(result.error.value).toBeInstanceOf(ApiError)
    expect(routesIn(queryClient, ROUTES_KEY)[0]?.checks).toEqual([])
  })

  it('unticks with DELETE', async () => {
    vi.mocked(api.DELETE).mockResolvedValue(noContent)
    const { result, queryClient } = withQueries(() => useToggleCheck())
    queryClient.setQueryData(ROUTES_KEY, [route('r1', { checks: ['TOP_ROPE', 'LEAD'] })])

    result.mutate({ id: 'r1', mode: 'TOP_ROPE', checked: false })
    await flushPromises()
    expect(api.DELETE).toHaveBeenCalledWith('/api/v1/routes/{id}/checks/{mode}', {
      params: { path: { id: 'r1', mode: 'TOP_ROPE' } },
    })
    expect(routesIn(queryClient, ROUTES_KEY)[0]?.checks).toEqual(['LEAD'])
  })
})

describe('useAddFavorite', () => {
  it('moves the heart from the released route to the new one', async () => {
    vi.mocked(api.PUT).mockResolvedValue(noContent)
    const { result, queryClient } = withQueries(() => useAddFavorite())
    queryClient.setQueryData(ROUTES_KEY, [route('new'), route('old', { favorite: true })])
    queryClient.setQueryData(MY_FAVORITES_KEY, [route('old', { favorite: true })])

    result.mutate({ id: 'new', replaceRouteId: 'old' })
    await flushPromises()
    expect(api.PUT).toHaveBeenCalledWith('/api/v1/routes/{id}/favorite', {
      params: { path: { id: 'new' } },
      body: { replaceRouteId: 'old' },
    })
    expect(
      routesIn(queryClient, ROUTES_KEY).map((item) => [item.id, item.favorite, item.favoriteCount]),
    ).toEqual([
      ['new', true, 4],
      ['old', false, 2],
    ])
  })

  it('puts everything back when the heart is full', async () => {
    vi.mocked(api.PUT).mockResolvedValue(conflict('FAVORITES_FULL'))
    const { result, queryClient } = withQueries(() => useAddFavorite())
    queryClient.setQueryData(ROUTES_KEY, [route('new')])

    result.mutate({ id: 'new' })
    await flushPromises()
    expect((result.error.value as ApiError).code).toBe('FAVORITES_FULL')
    expect(routesIn(queryClient, ROUTES_KEY)[0]).toMatchObject({
      favorite: false,
      favoriteCount: 3,
    })
  })
})

describe('useAcknowledgeLastChance', () => {
  it('sends long lists in batches the server accepts, then forgets them', async () => {
    vi.mocked(api.POST).mockResolvedValue(noContent)
    const { result, queryClient } = withQueries(() => useAcknowledgeLastChance())
    const ids = Array.from({ length: ACKNOWLEDGEMENT_BATCH + 3 }, (_, index) => `r${String(index)}`)
    queryClient.setQueryData(
      UNACKNOWLEDGED_LAST_CHANCE_KEY,
      ids.map((id) => route(id, { status: 'LAST_CHANCE' })),
    )

    result.mutate(ids)
    await flushPromises()
    const batches = vi
      .mocked(api.POST)
      .mock.calls.map((call) => (call[1] as { body: { routeIds: string[] } }).body.routeIds.length)
    expect(batches).toEqual([ACKNOWLEDGEMENT_BATCH, 3])
    expect(routesIn(queryClient, UNACKNOWLEDGED_LAST_CHANCE_KEY)).toEqual([])
  })
})
