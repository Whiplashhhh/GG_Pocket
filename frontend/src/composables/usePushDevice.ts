import { onMounted, ref } from 'vue'

import { addPushDevice, currentPushSubscription, removePushDevice } from '@/api/push'
import { applicationServerKey } from '@/lib/push'

/**
 * This browser's side of push (D-128): its subscription, turning it on (the browser asks the
 * member) and off. The server learns of it in the same move.
 */
export function usePushDevice() {
  const endpoint = ref<string | undefined>()
  const loaded = ref(false)
  const permission = ref<NotificationPermission | 'unsupported'>(
    typeof Notification === 'undefined' ? 'unsupported' : Notification.permission,
  )

  onMounted(async () => {
    try {
      endpoint.value = (await currentPushSubscription())?.endpoint
    } finally {
      loaded.value = true
    }
  })

  /** @returns false when the member (or the browser) said no */
  async function enable(serverKey: string): Promise<boolean> {
    const answer = await Notification.requestPermission()
    permission.value = answer
    if (answer !== 'granted') return false
    const registration = await navigator.serviceWorker.getRegistration()
    if (registration === undefined) throw new Error('No service worker')
    const subscription =
      (await registration.pushManager.getSubscription()) ??
      (await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: applicationServerKey(serverKey),
      }))
    await addPushDevice(subscription)
    endpoint.value = subscription.endpoint
    return true
  }

  async function disable(): Promise<void> {
    const subscription = await currentPushSubscription()
    if (subscription !== null) {
      await removePushDevice(subscription.endpoint)
      await subscription.unsubscribe()
    }
    endpoint.value = undefined
  }

  return { endpoint, loaded, permission, enable, disable }
}
