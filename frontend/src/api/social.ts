import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { useAwaitingValidation } from './auth'
import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'

export type MemberCard = components['schemas']['MemberCard']
export type Directory = components['schemas']['Directory']
export type DirectoryEntry = components['schemas']['DirectoryEntry']
export type MemberProfile = components['schemas']['MemberProfile']
export type Friends = components['schemas']['Friends']
export type Friend = components['schemas']['Friend']
export type FriendshipState = DirectoryEntry['friendship']

/** Everything about other members sits under it: the directory, profiles, their sessions. */
export const MEMBERS_KEY = ['members'] as const
export const memberKey = (id: string) => [...MEMBERS_KEY, id] as const
export const memberSessionsKey = (id: string) => [...memberKey(id), 'sessions'] as const
export const memberLogbookKey = (id: string) => [...memberKey(id), 'logbook'] as const
export const MY_FRIENDS_KEY = ['me', 'friends'] as const

const PAGE_SIZE = 10

/** The club directory: every active member but me, and the climber count of the feed header. */
export function useDirectory() {
  const awaiting = useAwaitingValidation()
  return useQuery({
    queryKey: MEMBERS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/members', { signal })),
    // Closed until the board validates my account (D-107): no point asking for a refusal.
    enabled: computed(() => !awaiting.value),
  })
}

export function useMemberProfile(id: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => memberKey(toValue(id))),
    queryFn: ({ signal }) =>
      request(api.GET('/api/v1/members/{id}', { params: { path: { id: toValue(id) } }, signal })),
  })
}

/** A friend's shared sessions, latest day first, page by page. */
export function useMemberSessions(
  id: MaybeRefOrGetter<string>,
  enabled: MaybeRefOrGetter<boolean>,
) {
  return useInfiniteQuery({
    queryKey: computed(() => memberSessionsKey(toValue(id))),
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/members/{id}/sessions', {
          params: {
            path: { id: toValue(id) },
            query: { size: PAGE_SIZE, ...(pageParam === undefined ? {} : { cursor: pageParam }) },
          },
          signal,
        }),
      ),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (page) => page.nextCursor,
    enabled: computed(() => toValue(enabled)),
  })
}

export function useMemberLogbook(id: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => memberLogbookKey(toValue(id))),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/members/{id}/logbook', {
          params: { path: { id: toValue(id) } },
          signal,
        }),
      ),
  })
}

/** My friends and pending requests. Checked now and then: the dot on my avatar follows. */
export function useMyFriends(enabled: MaybeRefOrGetter<boolean> = true) {
  return useQuery({
    queryKey: MY_FRIENDS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/friends', { signal })),
    refetchInterval: 2 * 60_000,
    enabled: computed(() => toValue(enabled)),
  })
}

/** How many requests wait for my answer; asked only once {@code enabled} (signed in). */
export function useIncomingRequestCount(enabled: MaybeRefOrGetter<boolean> = true) {
  const friends = useMyFriends(enabled)
  return computed(() => friends.data.value?.incoming.length ?? 0)
}

/** Ask a member to be friends, or accept their request. */
export function useBefriend() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (memberId: string) =>
      request(api.PUT('/api/v1/me/friends/{memberId}', { params: { path: { memberId } } })),
    onSettled: () => friendshipsChanged(queryClient),
  })
}

/** Decline their request, cancel mine, or end our friendship. */
export function useUnfriend() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (memberId: string) =>
      requestNoContent(
        api.DELETE('/api/v1/me/friends/{memberId}', { params: { path: { memberId } } }),
      ),
    onSettled: () => friendshipsChanged(queryClient),
  })
}

function friendshipsChanged(queryClient: ReturnType<typeof useQueryClient>) {
  return Promise.all([
    queryClient.invalidateQueries({ queryKey: MEMBERS_KEY }),
    queryClient.invalidateQueries({ queryKey: MY_FRIENDS_KEY }),
  ])
}
