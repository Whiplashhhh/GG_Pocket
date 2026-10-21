import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/vue-query'

import { AUDIT_KEY } from './admin'
import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import {
  ARCHIVE_KEY,
  MY_FAVORITES_KEY,
  ROUTES_KEY,
  TOPO_CONFIG_KEY,
  type RouteDetail,
  type TopoConfig,
} from './topo'

/** The management side of the topo (spec §8.10, §8.11): setters and the admin. */
export type RouteSettings = components['schemas']['RouteSettings']
export type LastChanceRoute = components['schemas']['LastChanceRoute']
export type SectorSettings = components['schemas']['SectorSettings']
export type CordSettings = components['schemas']['CordSettings']
export type HoldColorSettings = components['schemas']['HoldColorSettings']

/** Both last chance lists (the setters' view and the climbers' pop-up) live under this key. */
const LAST_CHANCE_KEY = ['topo', 'last-chance'] as const
export const LAST_CHANCE_ROUTES_KEY = [...LAST_CHANCE_KEY, 'routes'] as const
export const SETTERS_KEY = ['topo', 'setters'] as const

/** The routes in last chance, with how many climbers have accepted their fate. */
export function useLastChanceRoutes() {
  return useQuery({
    queryKey: LAST_CHANCE_ROUTES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/routes/last-chance', { signal })),
  })
}

/** Members who can be credited as openers (setters and above, active). */
export function useSetters() {
  return useQuery({
    queryKey: SETTERS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/routes/setters', { signal })),
  })
}

/**
 * A route changed: its sheet takes the server's answer, and everything that lists routes is
 * refreshed (topo, archives, favourites, last chance). Audited changes refresh the log too.
 */
function routeChanged(queryClient: QueryClient, detail: RouteDetail, audited = false) {
  queryClient.setQueryData([...ROUTES_KEY, detail.route.id], detail)
  void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
  void queryClient.invalidateQueries({ queryKey: ARCHIVE_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_FAVORITES_KEY })
  void queryClient.invalidateQueries({ queryKey: LAST_CHANCE_KEY })
  if (audited) void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
}

/** What happens to the photo when the form is saved. */
export type PhotoChange = { kind: 'keep' } | { kind: 'replace'; photo: Blob } | { kind: 'remove' }

/** The route was saved, but its photo was not: the form says so and stays on the saved route. */
export class RoutePhotoNotSaved extends Error {
  constructor(
    readonly saved: RouteDetail,
    cause: unknown,
  ) {
    super('Route saved, photo not saved', { cause })
    this.name = 'RoutePhotoNotSaved'
  }
}

function uploadPhoto(id: string, photo: Blob) {
  return request(
    api.PUT('/api/v1/routes/{id}/photo', {
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

async function savePhoto(saved: RouteDetail, change: PhotoChange): Promise<RouteDetail> {
  const id = saved.route.id
  try {
    if (change.kind === 'replace') return await uploadPhoto(id, change.photo)
    if (change.kind === 'remove' && saved.route.photoUrl !== undefined) {
      await requestNoContent(api.DELETE('/api/v1/routes/{id}/photo', { params: { path: { id } } }))
      return { ...saved, route: { ...saved.route, photoUrl: undefined } }
    }
    return saved
  } catch (cause) {
    throw new RoutePhotoNotSaved(saved, cause)
  }
}

/**
 * Creates a route (no id) or updates it, then sends its photo change. The photo goes after the
 * route because a new route has no id before it is created.
 */
export function useSaveRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async ({
      id,
      settings,
      photo,
    }: {
      id?: string
      settings: RouteSettings
      photo: PhotoChange
    }) => {
      const saved = await request(
        id === undefined
          ? api.POST('/api/v1/routes', { body: settings })
          : api.PUT('/api/v1/routes/{id}', { params: { path: { id } }, body: settings }),
      )
      return savePhoto(saved, photo)
    },
    onSuccess: (detail) => {
      routeChanged(queryClient, detail)
    },
    onError: (error) => {
      if (error instanceof RoutePhotoNotSaved) routeChanged(queryClient, error.saved)
    },
  })
}

/** « Dernière chance », with an optional planned dismount date (today or later). */
export function useAnnounceLastChance() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, plannedDismountOn }: { id: string; plannedDismountOn?: string }) =>
      request(
        api.PUT('/api/v1/routes/{id}/last-chance', {
          params: { path: { id } },
          body: plannedDismountOn === undefined ? {} : { plannedDismountOn },
        }),
      ),
    onSuccess: (detail) => {
      routeChanged(queryClient, detail)
    },
  })
}

export function useCancelLastChance() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      request(api.DELETE('/api/v1/routes/{id}/last-chance', { params: { path: { id } } })),
    onSuccess: (detail) => {
      routeChanged(queryClient, detail)
    },
  })
}

export function useDismountRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      request(api.POST('/api/v1/routes/{id}/dismount', { params: { path: { id } } })),
    onSuccess: (detail) => {
      routeChanged(queryClient, detail, true)
    },
  })
}

/** Undoes a dismount: the route is back on the wall, as it was. */
export function useRemountRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      request(api.POST('/api/v1/routes/{id}/remount', { params: { path: { id } } })),
    onSuccess: (detail) => {
      routeChanged(queryClient, detail, true)
    },
  })
}

/**
 * The club configuration changed: sector names, cords and colours show in every route, so the
 * route lists are refreshed with it, and the audit log too.
 */
function configChanged(queryClient: QueryClient) {
  void queryClient.invalidateQueries({ queryKey: TOPO_CONFIG_KEY })
  void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
  void queryClient.invalidateQueries({ queryKey: ARCHIVE_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_FAVORITES_KEY })
  void queryClient.invalidateQueries({ queryKey: LAST_CHANCE_KEY })
  void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
}

function useConfigMutation<V, R>(mutationFn: (variables: V) => Promise<R>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: () => {
      configChanged(queryClient)
    },
  })
}

export function useCreateSector() {
  return useConfigMutation((body: SectorSettings) =>
    request(api.POST('/api/v1/admin/club/sectors', { body })),
  )
}

export function useUpdateSector() {
  return useConfigMutation(({ id, settings }: { id: string; settings: SectorSettings }) =>
    request(
      api.PUT('/api/v1/admin/club/sectors/{id}', { params: { path: { id } }, body: settings }),
    ),
  )
}

/**
 * New order of all the sectors. Shown at once (`preview`), put back if the server says no; the
 * server's answer is the whole configuration, kept as is.
 */
export function useReorderSectors() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ sectorIds }: { sectorIds: string[]; preview: TopoConfig }) =>
      request(api.PUT('/api/v1/admin/club/sector-order', { body: { sectorIds } })),
    onMutate: ({ preview }) => {
      const previous = queryClient.getQueryData<TopoConfig>(TOPO_CONFIG_KEY)
      queryClient.setQueryData(TOPO_CONFIG_KEY, preview)
      return previous
    },
    onError: (_error, _variables, previous) => {
      if (previous !== undefined) queryClient.setQueryData(TOPO_CONFIG_KEY, previous)
    },
    onSuccess: (config) => {
      queryClient.setQueryData(TOPO_CONFIG_KEY, config)
      void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
      void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
    },
  })
}

export function useCreateCord() {
  return useConfigMutation((body: CordSettings) =>
    request(api.POST('/api/v1/admin/club/cords', { body })),
  )
}

export function useUpdateCord() {
  return useConfigMutation(({ id, settings }: { id: string; settings: CordSettings }) =>
    request(api.PUT('/api/v1/admin/club/cords/{id}', { params: { path: { id } }, body: settings })),
  )
}

export function useCreateHoldColor() {
  return useConfigMutation((body: HoldColorSettings) =>
    request(api.POST('/api/v1/admin/club/hold-colors', { body })),
  )
}

export function useUpdateHoldColor() {
  return useConfigMutation(({ id, settings }: { id: string; settings: HoldColorSettings }) =>
    request(
      api.PUT('/api/v1/admin/club/hold-colors/{id}', { params: { path: { id } }, body: settings }),
    ),
  )
}
