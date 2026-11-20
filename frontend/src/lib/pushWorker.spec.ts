import { runInNewContext } from 'node:vm'

import { describe, expect, it, vi } from 'vitest'

import { pageToOpen, pushWorkerSource } from './pushWorker'

const texts = {
  messages: {
    REACTION: '{nickname} a réagi {emoji} à ta séance {day}',
    COMMENT: '{nickname} a commenté ta séance {day}',
  },
  day: { today: 'd’aujourd’hui', yesterday: 'd’hier', date: 'du {date}' },
  emoji: { FIRE: '🔥' },
  fallback: 'Du nouveau sur GG Pocket',
}

/** Runs the generated worker against a fake service worker scope. */
function worker() {
  const listeners = new Map<string, (event: unknown) => void>()
  const showNotification = vi.fn(() => Promise.resolve())
  const scope = {
    addEventListener: (type: string, listener: (event: unknown) => void) =>
      listeners.set(type, listener),
    registration: { showNotification },
    clients: { matchAll: vi.fn(), openWindow: vi.fn() },
  }
  // The worker's own global scope, with this test's (fake) clock.
  runInNewContext(pushWorkerSource(texts), { self: scope, Date, Intl })
  const push = (data: unknown) => {
    listeners.get('push')?.({ data: { json: () => data }, waitUntil: () => undefined })
  }
  return { push, showNotification }
}

describe('pushWorkerSource', () => {
  it('words the message from the texts and keeps where to go', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-11-17T10:00:00Z'))
    const { push, showNotification } = worker()

    push({
      kind: 'REACTION',
      nickname: 'tom',
      climbedOn: '2026-11-16',
      reaction: 'FIRE',
      excerpt: null,
      url: '/seances/s1',
    })
    push({
      kind: 'COMMENT',
      nickname: 'lea',
      climbedOn: '2026-11-01',
      reaction: null,
      excerpt: 'Propre !',
      url: 'https://evil.example/',
    })
    push({ kind: 'SOMETHING_NEW', nickname: 'max', url: '/' })

    expect(showNotification).toHaveBeenNthCalledWith(
      1,
      'tom a réagi 🔥 à ta séance d’hier',
      expect.objectContaining({ body: '', data: { url: '/seances/s1' } }),
    )
    expect(showNotification).toHaveBeenNthCalledWith(
      2,
      'lea a commenté ta séance du 1er novembre 2026',
      expect.objectContaining({ body: '« Propre ! »', data: { url: '/' } }),
    )
    expect(showNotification).toHaveBeenNthCalledWith(
      3,
      'Du nouveau sur GG Pocket',
      expect.anything(),
    )
    vi.useRealTimers()
  })
})

describe('pageToOpen', () => {
  it('only opens a page of the app', () => {
    expect(pageToOpen({ type: 'gg-open', url: '/seances/s1' })).toBe('/seances/s1')
    expect(pageToOpen({ type: 'gg-open', url: '//evil.example' })).toBeUndefined()
    expect(pageToOpen({ type: 'other', url: '/' })).toBeUndefined()
    expect(pageToOpen('gg-open')).toBeUndefined()
  })
})
