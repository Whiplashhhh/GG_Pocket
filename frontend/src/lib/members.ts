import type { MemberSummary } from '@/api/admin'

import { ROLES } from './roles'

export type MemberSortKey = 'name' | 'nickname' | 'role' | 'status' | 'registeredAt' | 'lastSeenAt'
export type SortDirection = 'asc' | 'desc'

/** "Élodie" matches "elodie": accents and case are ignored. */
export function normalize(text: string): string {
  return text
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLocaleLowerCase('fr')
}

export function filterMembers(members: readonly MemberSummary[], query: string): MemberSummary[] {
  const words = normalize(query).split(/\s+/).filter(Boolean)
  if (words.length === 0) {
    return [...members]
  }
  return members.filter((member) => {
    const haystack = normalize(`${member.firstName} ${member.lastName} ${member.nickname}`)
    return words.every((word) => haystack.includes(word))
  })
}

const collator = new Intl.Collator('fr', { sensitivity: 'base', numeric: true })

function compare(a: MemberSummary, b: MemberSummary, key: MemberSortKey): number {
  switch (key) {
    case 'name':
      return collator.compare(a.lastName, b.lastName) || collator.compare(a.firstName, b.firstName)
    case 'nickname':
      return collator.compare(a.nickname, b.nickname)
    case 'role':
      return ROLES.indexOf(a.role) - ROLES.indexOf(b.role)
    case 'status':
      return a.status.localeCompare(b.status)
    case 'registeredAt':
      return a.registeredAt.localeCompare(b.registeredAt)
    case 'lastSeenAt':
      // Never seen sorts as the oldest.
      return (a.lastSeenAt ?? '').localeCompare(b.lastSeenAt ?? '')
  }
}

/** Stable sort; ties are broken by name so the table never jumps around. */
export function sortMembers(
  members: readonly MemberSummary[],
  key: MemberSortKey,
  direction: SortDirection,
): MemberSummary[] {
  const sign = direction === 'asc' ? 1 : -1
  return [...members].sort(
    (a, b) => sign * compare(a, b, key) || compare(a, b, 'name') || a.id.localeCompare(b.id),
  )
}
