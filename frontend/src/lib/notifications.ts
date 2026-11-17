import type { Notification } from '@/api/notifications'

import { REACTION_EMOJI } from './reactions'
import { sessionDay } from './sessionView'

/** An i18n key and its parameters, with where a tap leads (D-128). */
export interface NotificationLine {
  key: string
  params: Record<string, string>
  to: string
}

/**
 * What a line of the bell says, and where it leads: a session for what happened around one, my
 * profile for a request to answer, the friend's profile once they said yes.
 */
export function notificationLine(item: Notification, today?: string): NotificationLine {
  const params: Record<string, string> = { nickname: item.actor.nickname }
  if (item.climbedOn !== undefined) {
    const day = sessionDay(item.climbedOn, today)
    params.dayKind = day.kind
    if (day.kind === 'date') params.date = day.date
  }
  if (item.reaction !== undefined) params.emoji = REACTION_EMOJI[item.reaction]
  if (item.excerpt !== undefined) params.excerpt = item.excerpt
  const to =
    item.sessionId !== undefined
      ? `/seances/${item.sessionId}`
      : item.kind === 'FRIEND_REQUEST'
        ? '/profil'
        : `/membres/${item.actor.id}`
  return { key: `notifications.kinds.${item.kind}`, params, to }
}
