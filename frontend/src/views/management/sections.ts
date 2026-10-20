import { ClipboardList, KeyRound, Route, Users } from 'lucide-vue-next'
import type { Component } from 'vue'

import type { Role } from '@/lib/route'

/** Sections of the management space, each with the lowest role allowed (spec §5). */
export interface ManagementSection {
  to: string
  labelKey: 'management.routes' | 'management.members' | 'management.audit' | 'management.codes'
  icon: Component
  minRole: Role
}

export const SECTIONS: readonly ManagementSection[] = [
  { to: '/gestion/voies', labelKey: 'management.routes', icon: Route, minRole: 'SETTER' },
  { to: '/gestion/membres', labelKey: 'management.members', icon: Users, minRole: 'BOARD' },
  { to: '/gestion/journal', labelKey: 'management.audit', icon: ClipboardList, minRole: 'BOARD' },
  { to: '/gestion/codes', labelKey: 'management.codes', icon: KeyRound, minRole: 'ADMIN' },
]
