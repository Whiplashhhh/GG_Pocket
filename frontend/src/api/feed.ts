import {
  useInfiniteQuery,
  useMutation,
  useQuery,
  useQueryClient,
  type InfiniteData,
  type QueryClient,
} from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { withMyReaction, type ReactionKind, type SessionReactions } from '@/lib/reactions'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { FEED_KEY, MY_SESSIONS_KEY, sessionKey, type Session, type SessionPage } from './sessions'
import { MEMBERS_KEY } from './social'

export type SessionComment = components['schemas']['SessionComment']

export { FEED_KEY }
export const sessionCommentsKey = (id: string) => [...sessionKey(id), 'comments'] as const

const PAGE_SIZE = 10

/** The feed (spec §8.2): my sessions and my friends' shared ones, latest day first, page by page. */
export function useFeed() {
  return useInfiniteQuery({
    queryKey: FEED_KEY,
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/feed', {
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

/**
 * Changes one session wherever it is shown: its own page, the feed, my sessions, a friend's
 * sessions. The lists are pages of sessions; anything else under these keys is left alone.
 */
export function patchSession(
  queryClient: QueryClient,
  id: string,
  patch: (session: Session) => Session,
) {
  queryClient.setQueryData<Session>(sessionKey(id), (session) => session && patch(session))
  const patchPages = (data: unknown) => {
    if (typeof data !== 'object' || data === null || !('pages' in data)) return data
    const pages = data as InfiniteData<SessionPage>
    return {
      ...pages,
      pages: pages.pages.map((page) => ({
        ...page,
        items: page.items.map((session) => (session.id === id ? patch(session) : session)),
      })),
    }
  }
  for (const queryKey of [FEED_KEY, MY_SESSIONS_KEY, MEMBERS_KEY]) {
    queryClient.setQueriesData({ queryKey }, patchPages)
  }
}

/**
 * Gives, changes or takes back my reaction. The counts move at once; the server's answer then
 * puts the true ones (someone else may have reacted meanwhile), or the old ones on failure.
 */
export function useReact(session: MaybeRefOrGetter<Session>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (next: ReactionKind | undefined) => {
      const path = { path: { id: toValue(session).id } }
      return request(
        next === undefined
          ? api.DELETE('/api/v1/sessions/{id}/reaction', { params: path })
          : api.PUT('/api/v1/sessions/{id}/reaction', { params: path, body: { kind: next } }),
      )
    },
    onMutate: (next) => {
      const current = toValue(session)
      const before = current.reactions
      patchSession(queryClient, current.id, (found) => ({
        ...found,
        reactions: withMyReaction(found.reactions, next),
      }))
      return { before }
    },
    onSuccess: (reactions: SessionReactions) => {
      patchSession(queryClient, toValue(session).id, (found) => ({ ...found, reactions }))
    },
    onError: (_error, _next, context) => {
      const before = context?.before
      if (before === undefined) return
      patchSession(queryClient, toValue(session).id, (found) => ({ ...found, reactions: before }))
    },
  })
}

export function useSessionComments(id: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => sessionCommentsKey(toValue(id))),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/sessions/{id}/comments', {
          params: { path: { id: toValue(id) } },
          signal,
        }),
      ),
  })
}

export function useAddSessionComment(sessionId: MaybeRefOrGetter<string>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: string) =>
      request(
        api.POST('/api/v1/sessions/{id}/comments', {
          params: { path: { id: toValue(sessionId) } },
          body: { body },
        }),
      ),
    onSuccess: (comment) => {
      const id = toValue(sessionId)
      queryClient.setQueryData<SessionComment[]>(sessionCommentsKey(id), (comments) =>
        comments ? [...comments, comment] : comments,
      )
      patchSession(queryClient, id, (session) => ({
        ...session,
        commentCount: session.commentCount + 1,
      }))
    },
  })
}

export function useDeleteSessionComment(sessionId: MaybeRefOrGetter<string>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (commentId: string) =>
      requestNoContent(
        api.DELETE('/api/v1/session-comments/{id}', { params: { path: { id: commentId } } }),
      ),
    onSuccess: (_nothing, commentId) => {
      const id = toValue(sessionId)
      queryClient.setQueryData<SessionComment[]>(sessionCommentsKey(id), (comments) =>
        comments?.filter((comment) => comment.id !== commentId),
      )
      patchSession(queryClient, id, (session) => ({
        ...session,
        commentCount: Math.max(0, session.commentCount - 1),
      }))
    },
  })
}
