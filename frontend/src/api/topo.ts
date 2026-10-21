import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { MY_PROFILE_KEY, type MyProfile } from './me'
import { request, requestNoContent } from './request'
import type { components } from './schema'

export type TopoConfig = components['schemas']['TopoConfig']
export type Sector = components['schemas']['Sector']
export type Cord = components['schemas']['Cord']
export type HoldColorConfig = components['schemas']['HoldColor']
export type Grade = components['schemas']['Grade']
export type RouteHold = components['schemas']['RouteHold']
export type RouteSummary = components['schemas']['RouteSummary']
export type RouteDetail = components['schemas']['RouteDetail']
export type RouteComment = components['schemas']['RouteComment']
export type MemberRef = components['schemas']['MemberRef']
export type ClimbMode = RouteSummary['checks'][number]

export const TOPO_CONFIG_KEY = ['topo', 'config'] as const
/** The list of routes on the wall; one route's sheet lives under it ([...ROUTES_KEY, id]). */
export const ROUTES_KEY = ['topo', 'routes'] as const
export const ARCHIVE_KEY = ['topo', 'archive'] as const
export const MY_FAVORITES_KEY = ['me', 'favorites'] as const
export const UNACKNOWLEDGED_LAST_CHANCE_KEY = ['topo', 'last-chance', 'unacknowledged'] as const

const routeKey = (id: string) => [...ROUTES_KEY, id] as const
const commentsKey = (id: string) => [...ROUTES_KEY, id, 'comments'] as const

/** Configuration hardly ever changes: no refetch while the app is open. */
export function useTopoConfig() {
  return useQuery({
    queryKey: TOPO_CONFIG_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/topo/config', { signal })),
    staleTime: 10 * 60 * 1000,
  })
}

export function useRoutes(enabled: MaybeRefOrGetter<boolean> = true) {
  return useQuery({
    queryKey: ROUTES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/routes', { signal })),
    enabled: computed(() => toValue(enabled)),
  })
}

export function useDismountedRoutes(enabled: MaybeRefOrGetter<boolean> = true) {
  return useQuery({
    queryKey: ARCHIVE_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/routes/archive', { signal })),
    enabled: computed(() => toValue(enabled)),
  })
}

export function useRouteDetail(id: MaybeRefOrGetter<string | undefined>) {
  return useQuery({
    queryKey: computed(() => routeKey(toValue(id) ?? '')),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/routes/{id}', { params: { path: { id: toValue(id) ?? '' } }, signal }),
      ),
    enabled: computed(() => toValue(id) !== undefined),
  })
}

export function useRouteComments(id: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => commentsKey(toValue(id))),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/routes/{id}/comments', { params: { path: { id: toValue(id) } }, signal }),
      ),
  })
}

export function useMyFavorites() {
  return useQuery({
    queryKey: MY_FAVORITES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/favorites', { signal })),
  })
}

export function useUnacknowledgedLastChance(enabled: MaybeRefOrGetter<boolean>) {
  return useQuery({
    queryKey: UNACKNOWLEDGED_LAST_CHANCE_KEY,
    queryFn: ({ signal }) =>
      request(api.GET('/api/v1/routes/last-chance/unacknowledged', { signal })),
    enabled: computed(() => toValue(enabled)),
  })
}

/**
 * Applies a change to one route wherever it is cached (topo, archives, its sheet, my favourites),
 * so a tap shows at once. Returns how to put everything back if the server says no.
 */
function patchRoute(
  queryClient: QueryClient,
  id: string,
  patch: (route: RouteSummary) => RouteSummary,
): () => void {
  const lists = [ROUTES_KEY, ARCHIVE_KEY, MY_FAVORITES_KEY]
  const previousLists = lists.map((key) => queryClient.getQueryData<RouteSummary[]>(key))
  const previousDetail = queryClient.getQueryData<RouteDetail>(routeKey(id))
  for (const key of lists) {
    queryClient.setQueryData<RouteSummary[]>(key, (routes) =>
      routes?.map((route) => (route.id === id ? patch(route) : route)),
    )
  }
  queryClient.setQueryData<RouteDetail>(routeKey(id), (detail) =>
    detail ? { ...detail, route: patch(detail.route) } : detail,
  )
  return () => {
    lists.forEach((key, index) => queryClient.setQueryData(key, previousLists[index]))
    queryClient.setQueryData(routeKey(id), previousDetail)
  }
}

/** After a change the server may have recounted: refresh what shows routes. */
function refreshRoutes(queryClient: QueryClient) {
  void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
  void queryClient.invalidateQueries({ queryKey: ARCHIVE_KEY })
  void queryClient.invalidateQueries({ queryKey: MY_FAVORITES_KEY })
}

export function useAddFavorite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, replaceRouteId }: { id: string; replaceRouteId?: string }) =>
      requestNoContent(
        api.PUT('/api/v1/routes/{id}/favorite', {
          params: { path: { id } },
          body: replaceRouteId === undefined ? {} : { replaceRouteId },
        }),
      ),
    onMutate: ({ id, replaceRouteId }) => {
      const undoAdd = patchRoute(queryClient, id, (route) =>
        route.favorite
          ? route
          : { ...route, favorite: true, favoriteCount: route.favoriteCount + 1 },
      )
      const undoReplace =
        replaceRouteId === undefined
          ? () => undefined
          : patchRoute(queryClient, replaceRouteId, (route) =>
              route.favorite
                ? { ...route, favorite: false, favoriteCount: Math.max(0, route.favoriteCount - 1) }
                : route,
            )
      return () => {
        undoReplace()
        undoAdd()
      }
    },
    onError: (_error, _variables, undo) => {
      undo?.()
    },
    onSettled: () => {
      refreshRoutes(queryClient)
    },
  })
}

export function useRemoveFavorite() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      requestNoContent(api.DELETE('/api/v1/routes/{id}/favorite', { params: { path: { id } } })),
    onMutate: (id) =>
      patchRoute(queryClient, id, (route) =>
        route.favorite
          ? { ...route, favorite: false, favoriteCount: Math.max(0, route.favoriteCount - 1) }
          : route,
      ),
    onError: (_error, _id, undo) => {
      undo?.()
    },
    onSettled: () => {
      refreshRoutes(queryClient)
    },
  })
}

/** "Je sais la faire en mouli / en tête": ticks or unticks one mode, at once on screen. */
export function useToggleCheck() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, mode, checked }: { id: string; mode: ClimbMode; checked: boolean }) => {
      const params = { params: { path: { id, mode } } }
      return requestNoContent(
        checked
          ? api.PUT('/api/v1/routes/{id}/checks/{mode}', params)
          : api.DELETE('/api/v1/routes/{id}/checks/{mode}', params),
      )
    },
    onMutate: ({ id, mode, checked }) =>
      patchRoute(queryClient, id, (route) => ({
        ...route,
        checks: checked
          ? [...route.checks.filter((check) => check !== mode), mode]
          : route.checks.filter((check) => check !== mode),
      })),
    onError: (_error, _variables, undo) => {
      undo?.()
    },
    onSettled: () => {
      refreshRoutes(queryClient)
    },
  })
}

export function useAddComment(routeId: MaybeRefOrGetter<string>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: string) =>
      request(
        api.POST('/api/v1/routes/{id}/comments', {
          params: { path: { id: toValue(routeId) } },
          body: { body },
        }),
      ),
    onSuccess: (comment) => {
      const id = toValue(routeId)
      queryClient.setQueryData<RouteComment[]>(commentsKey(id), (comments) =>
        comments ? [...comments, comment] : comments,
      )
      patchRoute(queryClient, id, (route) => ({
        ...route,
        commentCount: route.commentCount + 1,
      }))
    },
  })
}

export function useDeleteComment(routeId: MaybeRefOrGetter<string>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (commentId: string) =>
      requestNoContent(
        api.DELETE('/api/v1/route-comments/{id}', { params: { path: { id: commentId } } }),
      ),
    onSuccess: (_nothing, commentId) => {
      const id = toValue(routeId)
      queryClient.setQueryData<RouteComment[]>(commentsKey(id), (comments) =>
        comments?.filter((comment) => comment.id !== commentId),
      )
      patchRoute(queryClient, id, (route) => ({
        ...route,
        commentCount: Math.max(0, route.commentCount - 1),
      }))
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: commentsKey(toValue(routeId)) }),
  })
}

/** "J'accepte leurs destins": the pop-up only comes back for routes announced afterwards. */
export function useAcknowledgeLastChance() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (routeIds: string[]) =>
      requestNoContent(
        api.POST('/api/v1/routes/last-chance/acknowledgements', { body: { routeIds } }),
      ),
    onSuccess: (_nothing, routeIds) => {
      queryClient.setQueryData<RouteSummary[]>(UNACKNOWLEDGED_LAST_CHANCE_KEY, (routes) =>
        routes?.filter((route) => !routeIds.includes(route.id)),
      )
    },
  })
}

/** My favourite route (voie préférée, D-055): undefined clears it. */
export function useSetFavoriteRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (routeId: string | undefined) =>
      requestNoContent(
        routeId === undefined
          ? api.DELETE('/api/v1/me/favorite-route')
          : api.PUT('/api/v1/me/favorite-route', { body: { routeId } }),
      ),
    onSuccess: (_nothing, routeId) => {
      queryClient.setQueryData<MyProfile>(MY_PROFILE_KEY, (profile) =>
        profile ? { ...profile, favoriteRouteId: routeId } : profile,
      )
    },
  })
}
