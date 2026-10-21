import { ClipboardList, Hourglass, Route, Settings2, Ticket, Users } from 'lucide-vue-next'
import type { Component } from 'vue'

import type { Role } from '@/lib/route'

/** Sections of the management space, each with the lowest role allowed (spec §5). */
export interface ManagementSection {
  to: string
  labelKey:
    | 'management.routes'
    | 'management.lastChance'
    | 'management.members'
    | 'management.audit'
    | 'management.codes'
    | 'management.club'
  icon: Component
  minRole: Role
  /** Not built yet: shown as coming soon. */
  soon?: boolean
}

export const SECTIONS: readonly ManagementSection[] = [
  { to: '/gestion/voies', labelKey: 'management.routes', icon: Route, minRole: 'SETTER' },
  {
    to: '/gestion/derniere-chance',
    labelKey: 'management.lastChance',
    icon: Hourglass,
    minRole: 'SETTER',
  },
  { to: '/gestion/membres', labelKey: 'management.members', icon: Users, minRole: 'BOARD' },
  { to: '/gestion/journal', labelKey: 'management.audit', icon: ClipboardList, minRole: 'BOARD' },
  { to: '/gestion/codes', labelKey: 'management.codes', icon: Ticket, minRole: 'ADMIN' },
  { to: '/gestion/club', labelKey: 'management.club', icon: Settings2, minRole: 'ADMIN' },
]
