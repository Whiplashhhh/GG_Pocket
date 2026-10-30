import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { queryClient } from '@/api/queryClient'
import { sendSession, sessionsChanged, type SessionRequest } from '@/api/sessions'
import { localStore } from '@/lib/localStore'
import { belongsTo, flushOutbox, queuedItem, type OutboxItem } from '@/lib/outbox'

const RETRY_EVERY_MS = 60_000

const store = localStore<OutboxItem>('outbox')

/**
 * Sessions waiting for the network, and the ones the server refused (see lib/outbox.ts). Only
 * those of the member signed in show and leave: the others wait for their owner on this phone.
 */
export const useOutboxStore = defineStore('outbox', () => {
  const all = ref<OutboxItem[]>([])
  const owner = ref<string | undefined>(undefined)
  const items = computed(() => all.value.filter((item) => belongsTo(item, owner.value)))
  const flushing = ref(false)
  let started = false

  const waiting = computed(() => items.value.filter((item) => item.state === 'waiting'))
  const problems = computed(() => items.value.filter((item) => item.state !== 'waiting'))

  async function refresh() {
    all.value = (await store.values()).sort((a, b) => a.queuedAt.localeCompare(b.queuedAt))
  }

  async function flush() {
    const memberId = owner.value
    if (flushing.value || memberId === undefined) return
    flushing.value = true
    try {
      const sent = await flushOutbox(
        store,
        (body, photo) =>
          sendSession({
            body,
            photo: photo === undefined ? { kind: 'keep' } : { kind: 'replace', photo },
          }),
        memberId,
      )
      if (sent.length > 0) sessionsChanged(queryClient)
    } finally {
      flushing.value = false
      await refresh()
    }
  }

  async function enqueue(body: SessionRequest, photo: Blob | undefined, memberId: string) {
    await store.set(body.clientRef, queuedItem(body, photo, memberId))
    await refresh()
  }

  /** Lets a refused session go, or closes the notice of a refused photo. */
  async function dismiss(clientRef: string) {
    await store.delete(clientRef)
    await refresh()
  }

  /** Takes a refused session out of the queue, to open it again in the session sheet. */
  async function takeBack(clientRef: string): Promise<OutboxItem | undefined> {
    const item = await store.get(clientRef)
    await store.delete(clientRef)
    await refresh()
    return item
  }

  /**
   * Once per app: sends what is waiting now, when the network is back, and every minute. Again
   * at every sign-in, for the queue of whoever is now signed in.
   */
  function start(memberId: string) {
    const changed = owner.value !== memberId
    owner.value = memberId
    if (started) {
      if (changed) void refresh().then(flush)
      return
    }
    started = true
    void refresh().then(flush)
    window.addEventListener('online', () => void flush())
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') void flush()
    })
    window.setInterval(() => {
      if (waiting.value.length > 0) void flush()
    }, RETRY_EVERY_MS)
  }

  return { items, waiting, problems, flushing, flush, enqueue, dismiss, takeBack, start }
})
