import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'

export type Notifications = components['schemas']['Notifications']
export type Notification = components['schemas']['Notification']

export const NOTIFICATIONS_KEY = ['me', 'notifications'] as const

/** How often the bell looks for news while the app is open (D-128): no push needed for the dot. */
export const NOTIFICATIONS_POLL_MS = 60_000

/**
 * The bell (D-128): what happened to me lately and how much is new. Asked again every minute
 * while the app is in front, and when it comes back to the front.
 */
export function useNotifications(enabled: MaybeRefOrGetter<boolean> = true) {
  return useQuery({
    queryKey: NOTIFICATIONS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/notifications', { signal })),
    enabled: computed(() => toValue(enabled)),
    refetchInterval: NOTIFICATIONS_POLL_MS,
    refetchOnWindowFocus: true,
  })
}

/**
 * I opened the bell: the dot goes at once. The items keep their « new » mark until the next
 * visit, so I still see what was new when I opened it.
 */
export function useMarkNotificationsSeen() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => requestNoContent(api.PUT('/api/v1/me/notifications/seen')),
    onMutate: () => {
      queryClient.setQueryData<Notifications>(NOTIFICATIONS_KEY, (current) =>
        current === undefined ? current : { ...current, unreadCount: 0 },
      )
    },
  })
}
