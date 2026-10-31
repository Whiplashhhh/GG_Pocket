import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { request } from './request'
import type { components } from './schema'
import { memberKey } from './social'

export type BadgeLibrary = components['schemas']['BadgeLibrary']
export type BadgeLibraryEntry = components['schemas']['BadgeLibraryEntry']
export type FavoriteBadges = components['schemas']['FavoriteBadgesRequest']['badges']

/** My badges change with every saved, changed or deleted session (see sessionsChanged). */
export const MY_BADGES_KEY = ['me', 'badges'] as const

/** Every badge of the catalogue, earned or not: mine, or another member's when given an id. */
export function useBadgeLibrary(memberId: MaybeRefOrGetter<string | undefined>) {
  return useQuery({
    queryKey: computed(() => {
      const id = toValue(memberId)
      return id === undefined ? MY_BADGES_KEY : ([...memberKey(id), 'badges'] as const)
    }),
    queryFn: ({ signal }) => {
      const id = toValue(memberId)
      return request(
        id === undefined
          ? api.GET('/api/v1/me/badges', { signal })
          : api.GET('/api/v1/members/{id}/badges', { params: { path: { id } }, signal }),
      )
    },
  })
}

/**
 * Chooses the badges shown next to my name (up to 3, earned ones). The cards of my sessions show
 * them too: the feed and my lists are read again.
 */
export function useChooseFavoriteBadges() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (badges: FavoriteBadges) =>
      request(api.PUT('/api/v1/me/badges/favorites', { body: { badges } })),
    onSuccess: (library) => {
      queryClient.setQueryData(MY_BADGES_KEY, library)
      void queryClient.invalidateQueries({ queryKey: ['feed'] })
      void queryClient.invalidateQueries({ queryKey: ['me', 'sessions'] })
    },
  })
}
