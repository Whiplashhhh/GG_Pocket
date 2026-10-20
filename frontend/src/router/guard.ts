import type { RouteLocationNormalized, RouteLocationRaw } from 'vue-router'

import type { AuthState } from '@/api/auth'
import { isAtLeast } from '@/lib/roles'

export const HOME = '/actu'
export const WELCOME = '/accueil'
/** Query parameter carrying where to go back after signing in. */
export const RETURN_PARAM = 'retour'

/**
 * Where a navigation may go. `'unknown'`: the server could not be reached (offline); the
 * app shell then shows its offline screen rather than pretending the member is signed out.
 */
export function decideNavigation(
  to: Pick<RouteLocationNormalized, 'meta' | 'fullPath'>,
  state: AuthState | 'unknown',
): true | RouteLocationRaw {
  if (state === 'unknown') {
    return true
  }
  if (to.meta.public === true) {
    return to.meta.guestOnly === true && state.authenticated ? HOME : true
  }
  if (!state.authenticated) {
    return to.fullPath === HOME || to.fullPath === '/'
      ? WELCOME
      : { path: WELCOME, query: { [RETURN_PARAM]: to.fullPath } }
  }
  if (to.meta.minRole !== undefined && !isAtLeast(state.member?.role, to.meta.minRole)) {
    return HOME
  }
  return true
}

/** Only paths of this app: never send a member to another site after signing in. */
export function safeReturnPath(value: unknown): string {
  return typeof value === 'string' && value.startsWith('/') && !value.startsWith('//')
    ? value
    : HOME
}
