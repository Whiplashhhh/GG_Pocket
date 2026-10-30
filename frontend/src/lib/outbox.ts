import { ApiError } from '@/api/errors'
import { SessionPhotoNotSaved, type Session, type SessionRequest } from '@/api/sessions'

import type { KeyValueStore } from './localStore'

/**
 * Sessions waiting for the network (spec §8.3, §12). A session that could not leave goes here
 * with its photo, and is sent again when the network is back. The server knows it by its
 * clientRef: sending it twice never makes two sessions.
 *
 * - `waiting`: not sent yet, or sent without an answer. Retried.
 * - `refused`: the server said no (a frozen month, a dismounted route…). Kept until the climber
 *   takes it back into the session sheet, or lets it go.
 * - `photoRefused`: the session is saved, its photo was refused. Kept to tell the climber.
 */
export interface OutboxItem {
  clientRef: string
  body: SessionRequest
  photo?: Blob
  queuedAt: string
  state: 'waiting' | 'refused' | 'photoRefused'
  errorCode?: string
  /** Once saved (photoRefused). */
  sessionId?: string
  /** Who logged it: a shared phone never sends a session in someone else's name. */
  memberId?: string
}

/** Mine, or queued before the queue knew whose it was. */
export function belongsTo(item: OutboxItem, memberId: string | undefined): boolean {
  return item.memberId === undefined || item.memberId === memberId
}

export type SessionSender = (body: SessionRequest, photo: Blob | undefined) => Promise<Session>

/** Worth trying again later: no network, a server hiccup, a rate limit, a sign-in to redo. */
export function canWait(error: unknown): boolean {
  if (!(error instanceof ApiError)) return true
  if (error.code === 'NETWORK' || error.status === undefined) return true
  return error.status >= 500 || [401, 403, 408, 429].includes(error.status)
}

function codeOf(error: unknown): string {
  return error instanceof ApiError ? error.code : 'UNKNOWN'
}

export function queuedItem(
  body: SessionRequest,
  photo: Blob | undefined,
  memberId: string,
  now = new Date(),
) {
  const item: OutboxItem = {
    clientRef: body.clientRef,
    body,
    queuedAt: now.toISOString(),
    state: 'waiting',
    memberId,
  }
  if (photo !== undefined) item.photo = photo
  return item
}

/**
 * Sends the waiting sessions of the member signed in, oldest first. Stops at the first one that cannot go yet (still
 * offline): the next ones would not go either. Returns the sessions the server now has.
 */
export async function flushOutbox(
  store: KeyValueStore<OutboxItem>,
  send: SessionSender,
  memberId: string,
): Promise<Session[]> {
  const waiting = (await store.values())
    .filter((item) => item.state === 'waiting' && belongsTo(item, memberId))
    .sort((a, b) => a.queuedAt.localeCompare(b.queuedAt))
  const sent: Session[] = []
  for (const item of waiting) {
    try {
      sent.push(await send(item.body, item.photo))
      await store.delete(item.clientRef)
    } catch (error) {
      if (error instanceof SessionPhotoNotSaved) {
        if (canWait(error.cause)) break
        sent.push(error.saved)
        // The photo goes: the server will not take it, and the phone has little room.
        const saved: OutboxItem = {
          ...item,
          state: 'photoRefused',
          errorCode: codeOf(error.cause),
          sessionId: error.saved.id,
        }
        delete saved.photo
        await store.set(item.clientRef, saved)
        continue
      }
      if (canWait(error)) break
      await store.set(item.clientRef, { ...item, state: 'refused', errorCode: codeOf(error) })
    }
  }
  return sent
}
