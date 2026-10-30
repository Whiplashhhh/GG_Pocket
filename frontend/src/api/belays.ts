import {
  useInfiniteQuery,
  useMutation,
  useQueryClient,
  type InfiniteData,
} from '@tanstack/vue-query'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { FEED_KEY, sessionKey } from './sessions'
import { MEMBERS_KEY } from './social'

export type Belay = components['schemas']['Belay']
export type BelayPage = components['schemas']['BelayPage']

/** The sessions where someone named me as their belayer (D-101). */
export const MY_BELAYS_KEY = ['me', 'belays'] as const
const PAGE_SIZE = 20

export function useMyBelays() {
  return useInfiniteQuery({
    queryKey: MY_BELAYS_KEY,
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/me/belays', {
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

/** Takes my name back from a session: it leaves my list, and the session as others see it. */
export function useWithdrawBelay() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (sessionId: string) =>
      requestNoContent(
        api.DELETE('/api/v1/sessions/{id}/belayer', { params: { path: { id: sessionId } } }),
      ),
    onSuccess: (_nothing, sessionId) => {
      queryClient.setQueryData<InfiniteData<BelayPage>>(MY_BELAYS_KEY, (data) =>
        data === undefined
          ? data
          : {
              ...data,
              pages: data.pages.map((page) => ({
                ...page,
                items: page.items.filter((belay) => belay.sessionId !== sessionId),
              })),
            },
      )
      void queryClient.invalidateQueries({ queryKey: sessionKey(sessionId) })
      void queryClient.invalidateQueries({ queryKey: FEED_KEY })
      void queryClient.invalidateQueries({ queryKey: MEMBERS_KEY })
    },
  })
}
