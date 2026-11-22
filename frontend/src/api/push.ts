import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'

export type PushSettings = components['schemas']['PushSettings']
export type PushPreferences = components['schemas']['PushPreferences']

export const pushSettingsKey = (endpoint: string | undefined) =>
  ['me', 'push', endpoint ?? ''] as const

/** Push on the server, my preferences, and whether this device receives them (D-128). */
export function usePushSettings(endpoint: MaybeRefOrGetter<string | undefined>) {
  return useQuery({
    queryKey: computed(() => pushSettingsKey(toValue(endpoint))),
    queryFn: ({ signal }) => {
      const value = toValue(endpoint)
      return request(
        api.GET('/api/v1/me/push', {
          params: { query: value === undefined ? {} : { endpoint: value } },
          signal,
        }),
      )
    },
  })
}

export function useSavePushPreferences() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (preferences: PushPreferences) =>
      request(api.PUT('/api/v1/me/push/preferences', { body: preferences })),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['me', 'push'] }),
  })
}

/** This browser's subscription, as `PushSubscription.toJSON()` gives it. */
export async function addPushDevice(subscription: PushSubscription): Promise<void> {
  const json = subscription.toJSON()
  const p256dh = json.keys?.p256dh
  const auth = json.keys?.auth
  if (json.endpoint === undefined || p256dh === undefined || auth === undefined) {
    throw new Error('Incomplete push subscription')
  }
  await requestNoContent(
    api.PUT('/api/v1/me/push/devices', {
      body: { endpoint: json.endpoint, keys: { p256dh, auth } },
    }),
  )
}

export async function removePushDevice(endpoint: string): Promise<void> {
  await requestNoContent(api.DELETE('/api/v1/me/push/devices', { params: { query: { endpoint } } }))
}

/** The push subscription of this browser, if any (no service worker: none). */
export async function currentPushSubscription(): Promise<PushSubscription | null> {
  if (!('serviceWorker' in navigator)) return null
  const registration = await navigator.serviceWorker.getRegistration()
  // Absent outside the installed app on an iPhone, whatever the types say.
  const manager: PushManager | undefined = registration?.pushManager
  return (await manager?.getSubscription()) ?? null
}

/**
 * Signing out: this device stops receiving my notifications (a shared phone must not show the
 * next member mine). Never in the way of the sign-out itself.
 */
export async function forgetThisPushDevice(): Promise<void> {
  try {
    const subscription = await currentPushSubscription()
    if (subscription === null) return
    await removePushDevice(subscription.endpoint).catch(() => undefined)
    await subscription.unsubscribe()
  } catch {
    // Nothing to forget, or the browser refused: the sign-out goes on.
  }
}
