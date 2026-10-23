import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { queryClient } from '@/api/queryClient'
import { sendSession, sessionsChanged, type SessionRequest } from '@/api/sessions'
import { localStore } from '@/lib/localStore'
import { flushOutbox, queuedItem, type OutboxItem } from '@/lib/outbox'

const RETRY_EVERY_MS = 60_000

const store = localStore<OutboxItem>('outbox')

/** Sessions waiting for the network, and the ones the server refused (see lib/outbox.ts). */
export const useOutboxStore = defineStore('outbox', () => {
  const items = ref<OutboxItem[]>([])
  const flushing = ref(false)
  let started = false

  const waiting = computed(() => items.value.filter((item) => item.state === 'waiting'))
  const problems = computed(() => items.value.filter((item) => item.state !== 'waiting'))

  async function refresh() {
    items.value = (await store.values()).sort((a, b) => a.queuedAt.localeCompare(b.queuedAt))
  }

  async function flush() {
    if (flushing.value) return
    flushing.value = true
    try {
      const sent = await flushOutbox(store, (body, photo) =>
        sendSession({
          body,
          photo: photo === undefined ? { kind: 'keep' } : { kind: 'replace', photo },
        }),
      )
      if (sent.length > 0) sessionsChanged(queryClient)
    } finally {
      flushing.value = false
      await refresh()
    }
  }

  async function enqueue(body: SessionRequest, photo: Blob | undefined) {
    await store.set(body.clientRef, queuedItem(body, photo))
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

  /** Once per app: sends what is waiting now, when the network is back, and every minute. */
  function start() {
    if (started) return
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
