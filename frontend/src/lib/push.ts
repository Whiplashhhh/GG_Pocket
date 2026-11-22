/**
 * Push notifications on this device (D-128, D-130). Pure functions: the browser facts come in,
 * so the rules are tested without a browser.
 */

/** Where this device stands. */
export type PushSupport =
  /** No service worker, no Push API, or the server has push off. */
  | 'unsupported'
  /** iPhone or iPad outside the installed app: Apple only allows push to installed apps. */
  | 'needsInstall'
  /** The member said no in the browser: only the browser's settings can undo it. */
  | 'denied'
  /** Ready to be turned on, or already on. */
  | 'ready'

export interface PushFacts {
  hasPushApi: boolean
  permission: NotificationPermission | 'unsupported'
  ios: boolean
  standalone: boolean
  serverKey: string | undefined
}

export function pushSupport(facts: PushFacts): PushSupport {
  if (facts.serverKey === undefined) return 'unsupported'
  // Safari on iOS only shows the Push API inside the installed app.
  if (facts.ios && !facts.standalone) return 'needsInstall'
  if (!facts.hasPushApi || facts.permission === 'unsupported') return 'unsupported'
  if (facts.permission === 'denied') return 'denied'
  return 'ready'
}

/** The server's VAPID key (base64url) as `pushManager.subscribe` wants it. */
export function applicationServerKey(base64url: string): Uint8Array<ArrayBuffer> {
  const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
  const binary = atob(padded)
  const bytes = new Uint8Array(new ArrayBuffer(binary.length))
  for (let index = 0; index < binary.length; index++) bytes[index] = binary.charCodeAt(index)
  return bytes
}

/**
 * Whether a subscription was made with this server key. After a change of keys (D-130), the old
 * subscription is refused by the push service: it has to be made again.
 */
export function sameServerKey(subscriptionKey: ArrayBuffer | null, serverKey: string): boolean {
  if (subscriptionKey === null) return false
  const expected = applicationServerKey(serverKey)
  const actual = new Uint8Array(subscriptionKey)
  return (
    actual.length === expected.length && actual.every((byte, index) => byte === expected[index])
  )
}

/** What the service worker receives (PushDelivery.payload on the server). */
export interface PushMessage {
  kind: 'REACTION' | 'COMMENT' | 'REPLY' | 'FRIEND_SESSION' | 'FRIEND_REQUEST' | 'FRIEND_ACCEPTED'
  nickname: string
  climbedOn: string | null
  reaction: string | null
  excerpt: string | null
  url: string
}
