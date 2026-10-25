import { describe, expect, it } from 'vitest'

import { ApiError } from '@/api/errors'
import { SessionPhotoNotSaved, type Session, type SessionRequest } from '@/api/sessions'

import { memoryStore } from './localStore'
import { canWait, flushOutbox, queuedItem, type OutboxItem } from './outbox'

function body(clientRef: string): SessionRequest {
  return {
    clientRef,
    climbedOn: '2026-10-20',
    visibility: 'FRIENDS',
    ascents: [
      { routeId: 'r1', mode: 'LEAD', topped: true, clean: true, falls: 0, firstTry: false },
    ],
  }
}

const saved = (id: string) => ({ id }) as unknown as Session

async function queueOf(...refs: string[]) {
  const store = memoryStore<OutboxItem>()
  for (const [index, ref] of refs.entries()) {
    await store.set(
      ref,
      queuedItem(body(ref), undefined, new Date(Date.UTC(2026, 9, 20, 10, index))),
    )
  }
  return store
}

describe('canWait', () => {
  it('keeps for later what may work later, not what the server refused', () => {
    expect(canWait(new ApiError('NETWORK'))).toBe(true)
    expect(canWait(new ApiError('INTERNAL_ERROR', { status: 503 }))).toBe(true)
    expect(canWait(new ApiError('RATE_LIMITED', { status: 429 }))).toBe(true)
    expect(canWait(new ApiError('UNAUTHENTICATED', { status: 401 }))).toBe(true)
    expect(canWait(new ApiError('MONTH_FROZEN', { status: 409 }))).toBe(false)
    expect(canWait(new ApiError('VALIDATION_FAILED', { status: 400 }))).toBe(false)
  })
})

describe('flushOutbox', () => {
  it('sends the oldest first and empties the queue', async () => {
    const store = await queueOf('a', 'b')
    const order: string[] = []
    const sent = await flushOutbox(store, (request) => {
      order.push(request.clientRef)
      return Promise.resolve(saved(request.clientRef))
    })
    expect(order).toEqual(['a', 'b'])
    expect(sent).toHaveLength(2)
    expect(await store.values()).toEqual([])
  })

  it('stops while the network is down, and keeps everything', async () => {
    const store = await queueOf('a', 'b')
    let calls = 0
    const sent = await flushOutbox(store, () => {
      calls++
      return Promise.reject(new ApiError('NETWORK'))
    })
    expect(calls).toBe(1)
    expect(sent).toEqual([])
    expect((await store.values()).map((item) => item.state)).toEqual(['waiting', 'waiting'])
  })

  it('keeps a refused session aside and goes on with the next one', async () => {
    const store = await queueOf('a', 'b')
    const sent = await flushOutbox(store, (request) =>
      request.clientRef === 'a'
        ? Promise.reject(new ApiError('MONTH_FROZEN', { status: 409 }))
        : Promise.resolve(saved('b')),
    )
    expect(sent).toHaveLength(1)
    expect(await store.get('a')).toMatchObject({ state: 'refused', errorCode: 'MONTH_FROZEN' })
    expect(await store.get('b')).toBeUndefined()
  })

  it('a refused photo: the session is saved, the notice stays without the photo', async () => {
    const store = memoryStore<OutboxItem>()
    await store.set('a', queuedItem(body('a'), new Blob(['x'])))
    const sent = await flushOutbox(store, () =>
      Promise.reject(
        new SessionPhotoNotSaved(saved('s1'), new ApiError('IMAGE_UNSUPPORTED', { status: 400 })),
      ),
    )
    expect(sent).toHaveLength(1)
    const item = await store.get('a')
    expect(item).toMatchObject({
      state: 'photoRefused',
      sessionId: 's1',
      errorCode: 'IMAGE_UNSUPPORTED',
    })
    expect(item?.photo).toBeUndefined()
  })

  it('a photo lost on the way is sent again with its session, which the server knows', async () => {
    const store = memoryStore<OutboxItem>()
    await store.set('a', queuedItem(body('a'), new Blob(['x'])))
    await flushOutbox(store, () =>
      Promise.reject(new SessionPhotoNotSaved(saved('s1'), new ApiError('NETWORK'))),
    )
    expect(await store.get('a')).toMatchObject({ state: 'waiting' })
  })

  it('leaves refused sessions alone', async () => {
    const store = memoryStore<OutboxItem>()
    await store.set('a', { ...queuedItem(body('a'), undefined), state: 'refused' })
    let calls = 0
    await flushOutbox(store, () => {
      calls++
      return Promise.resolve(saved('a'))
    })
    expect(calls).toBe(0)
  })
})
