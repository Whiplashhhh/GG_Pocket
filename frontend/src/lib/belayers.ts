import type { Session } from '@/api/sessions'
import type { DirectoryEntry, MemberCard } from '@/api/social'
import type { MemberRef } from '@/api/topo'

import { filterMembers } from './members'

/**
 * The belayer, route by route (D-101). Anyone in the club may have held the rope: the picker shows
 * my friends first, then everyone else, in the order of the directory (by name).
 */
export function belayerChoices(
  entries: readonly DirectoryEntry[],
  query: string,
): { friends: MemberCard[]; others: MemberCard[] } {
  const found = new Set(
    filterMembers(
      entries.map((entry) => entry.member),
      query,
    ).map((member) => member.id),
  )
  const shown = entries.filter((entry) => found.has(entry.member.id))
  return {
    friends: shown.filter((entry) => entry.friendship === 'FRIENDS').map((entry) => entry.member),
    others: shown.filter((entry) => entry.friendship !== 'FRIENDS').map((entry) => entry.member),
  }
}

/** Who held the rope during the session, each once, in the order of the routes. */
export function sessionBelayers(session: Pick<Session, 'ascents'>): MemberRef[] {
  const seen = new Map<string, MemberRef>()
  for (const ascent of session.ascents) {
    if (ascent.belayer !== undefined && !seen.has(ascent.belayer.id)) {
      seen.set(ascent.belayer.id, ascent.belayer)
    }
  }
  return [...seen.values()]
}
