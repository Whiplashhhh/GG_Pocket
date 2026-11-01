import type { ChangelogRelease } from './changelog'

/**
 * "Once per version and per member" (spec §8.12, DECISIONS D-108): the last release whose notes a
 * member saw is remembered in this browser, under their id. Storage may be blocked (private
 * browsing): the pop-up then shows at most once per visit.
 */
const KEY_PREFIX = 'gg-pocket:release-notes:'

export function lastSeenRelease(memberId: string): string | null {
  try {
    return localStorage.getItem(KEY_PREFIX + memberId)
  } catch {
    return null
  }
}

export function markReleaseNotesSeen(memberId: string, version: string = __APP_VERSION__): void {
  try {
    localStorage.setItem(KEY_PREFIX + memberId, version)
  } catch {
    // Blocked storage: nothing to remember.
  }
}

/** The published releases, in their own chunk: only loaded when needed. */
export async function loadReleases(): Promise<ChangelogRelease[]> {
  return (await import('virtual:changelog')).default
}
