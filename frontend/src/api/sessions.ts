import {
  useInfiniteQuery,
  useMutation,
  useQuery,
  useQueryClient,
  type QueryClient,
} from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { MY_BADGES_KEY } from './badges'
import { api } from './client'
import { RANKINGS_KEY } from './rankings'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { MY_TODO_KEY, ROUTES_KEY } from './topo'
import type { PhotoChange } from './topoAdmin'

export type Session = components['schemas']['Session']
export type SessionRequest = components['schemas']['SessionRequest']
export type AscentRequest = components['schemas']['AscentRequest']
export type SessionPage = components['schemas']['SessionPage']
export type ClimbedRoute = components['schemas']['ClimbedRoute']
export type SessionVisibility = SessionRequest['visibility']
export type Planning = components['schemas']['Planning']
export type PlanningDay = components['schemas']['PlanningDay']
export type Logbook = components['schemas']['Logbook']
export type LogbookRoute = components['schemas']['LogbookRoute']
export type AscentBest = LogbookRoute['best']

export const MY_SESSIONS_KEY = ['me', 'sessions'] as const
/** The feed (api/feed.ts): here because a saved session changes it. */
export const FEED_KEY = ['feed'] as const
export const CLIMBED_ROUTES_KEY = ['me', 'climbed-routes'] as const
export const sessionKey = (id: string) => ['sessions', id] as const
/** One month under it: [...MY_PLANNING_KEY, '2026-10']. */
export const MY_PLANNING_KEY = ['me', 'planning'] as const
export const MY_LOGBOOK_KEY = ['me', 'logbook'] as const

const PAGE_SIZE = 10

/** My sessions, latest day first, page by page (cursor). */
export function useMySessions() {
  return useInfiniteQuery({
    queryKey: MY_SESSIONS_KEY,
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/me/sessions', {
          params: {
            query: { size: PAGE_SIZE, ...(pageParam === undefined ? {} : { cursor: pageParam }) },
          },
          signal,
        }),
      ),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (page) => page.nextCursor,
  })
}

export function useSession(id: MaybeRefOrGetter<string | undefined>) {
  return useQuery({
    queryKey: computed(() => sessionKey(toValue(id) ?? '')),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/sessions/{id}', { params: { path: { id: toValue(id) ?? '' } }, signal }),
      ),
    enabled: computed(() => toValue(id) !== undefined),
  })
}

/** Routes I climbed, most recent first: the quick picker and the "first try" box use it. */
export function useClimbedRoutes() {
  return useQuery({
    queryKey: CLIMBED_ROUTES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/climbed-routes', { signal })),
  })
}

/** The session was saved, but its photo was not: the screen says so and keeps the session. */
export class SessionPhotoNotSaved extends Error {
  constructor(
    readonly saved: Session,
    cause: unknown,
  ) {
    super('Session saved, photo not saved', { cause })
    this.name = 'SessionPhotoNotSaved'
  }
}

function uploadPhoto(id: string, photo: Blob) {
  return request(
    api.PUT('/api/v1/sessions/{id}/photo', {
      params: { path: { id } },
      // The contract types binary parts as strings; the browser sends the Blob itself.
      body: { file: photo as unknown as string },
      bodySerializer: () => {
        const form = new FormData()
        form.append('file', photo, 'photo.jpg')
        return form
      },
    }),
  )
}

async function savePhoto(saved: Session, change: PhotoChange): Promise<Session> {
  try {
    if (change.kind === 'replace') return await uploadPhoto(saved.id, change.photo)
    if (change.kind === 'remove' && saved.photoUrl !== undefined) {
      await requestNoContent(
        api.DELETE('/api/v1/sessions/{id}/photo', { params: { path: { id: saved.id } } }),
      )
      return { ...saved, photoUrl: undefined }
    }
    return saved
  } catch (cause) {
    throw new SessionPhotoNotSaved(saved, cause)
  }
}

/**
 * Creates (no id) or changes a session, then sends its photo. Creating twice with the same
 * clientRef gives back the session already saved: the offline queue relies on it.
 */
export async function sendSession(input: {
  id?: string
  body: SessionRequest
  photo: PhotoChange
}): Promise<Session> {
  const saved = await request(
    input.id === undefined
      ? api.POST('/api/v1/sessions', { body: input.body })
      : api.PUT('/api/v1/sessions/{id}', { params: { path: { id: input.id } }, body: input.body }),
  )
  return savePhoto(saved, input.photo)
}

/**
 * What a saved, changed or deleted session touches: my lists, my badges, and the routes (stats,
 * checks).
 */
export function sessionsChanged(queryClient: QueryClient, saved?: Session) {
  if (saved !== undefined) queryClient.setQueryData(sessionKey(saved.id), saved)
  void queryClient.invalidateQueries({ queryKey: MY_SESSIONS_KEY })
  void queryClient.invalidateQueries({ queryKey: FEED_KEY })
  void queryClient.invalidateQueries({ queryKey: CLIMBED_ROUTES_KEY })
  void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_PLANNING_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_LOGBOOK_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_TODO_KEY })
  void queryClient.invalidateQueries({ queryKey: RANKINGS_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_BADGES_KEY })
}

export function useSaveSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: sendSession,
    onSuccess: (saved) => {
      sessionsChanged(queryClient, saved)
    },
    onError: (error) => {
      if (error instanceof SessionPhotoNotSaved) sessionsChanged(queryClient, error.saved)
    },
  })
}

export function useDeleteSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      requestNoContent(api.DELETE('/api/v1/sessions/{id}', { params: { path: { id } } })),
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: sessionKey(id) })
      sessionsChanged(queryClient)
    },
  })
}

/** One month of my planning (spec §8.6); the previous month stays on screen while the next loads. */
export function useMyPlanning(month: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => [...MY_PLANNING_KEY, toValue(month)] as const),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/me/planning', { params: { query: { month: toValue(month) } }, signal }),
      ),
    placeholderData: (previous) => previous,
  })
}

/** My logbook ("carnet"): totals, grade pyramid, every route I climbed. */
export function useMyLogbook() {
  return useQuery({
    queryKey: MY_LOGBOOK_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/logbook', { signal })),
  })
}
