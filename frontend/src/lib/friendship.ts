import type { FriendshipState } from '@/api/social'

export type FriendshipPlace = 'row' | 'profile'

export type FriendshipAction = {
  /** befriend: ask or accept; unfriend: decline, cancel or end. */
  kind: 'befriend' | 'unfriend'
  label: 'ask' | 'accept' | 'decline' | 'cancel' | 'remove'
  variant: 'primary' | 'secondary' | 'ghost'
  /** Ending a friendship asks first: the other one is not told, and asking again starts over. */
  confirm: boolean
}

export type FriendshipControls = {
  /** What stands between us, shown as a plain label (spec §8.2: « En attente », « Amis »). */
  status: 'requested' | 'friends' | null
  actions: FriendshipAction[]
}

const ASK: FriendshipAction = { kind: 'befriend', label: 'ask', variant: 'primary', confirm: false }
const ACCEPT: FriendshipAction = {
  kind: 'befriend',
  label: 'accept',
  variant: 'primary',
  confirm: false,
}
const DECLINE: FriendshipAction = {
  kind: 'unfriend',
  label: 'decline',
  variant: 'secondary',
  confirm: false,
}

/**
 * The directory rows stay light: one button or a label. The profile adds the way back (cancel my
 * request, end the friendship).
 */
export function friendshipControls(
  state: FriendshipState,
  place: FriendshipPlace,
): FriendshipControls {
  switch (state) {
    case 'NONE':
      return { status: null, actions: [ASK] }
    case 'INCOMING':
      return { status: null, actions: [ACCEPT, DECLINE] }
    case 'REQUESTED':
      return {
        status: 'requested',
        actions:
          place === 'profile'
            ? [{ kind: 'unfriend', label: 'cancel', variant: 'ghost', confirm: false }]
            : [],
      }
    case 'FRIENDS':
      return {
        status: 'friends',
        actions:
          place === 'profile'
            ? [{ kind: 'unfriend', label: 'remove', variant: 'ghost', confirm: true }]
            : [],
      }
    case 'SELF':
      return { status: null, actions: [] }
  }
}
