import type { Role } from './route'

const ORDER: Role[] = ['TOURIST', 'CLIMBER', 'SETTER', 'BOARD', 'ADMIN']

/** Spec §5: each role has the rights of the roles below it (a tourist, those of a climber: D-107). */
export function isAtLeast(role: Role | undefined, minimum: Role): boolean {
  return role !== undefined && ORDER.indexOf(role) >= ORDER.indexOf(minimum)
}

export const ROLES = ORDER
