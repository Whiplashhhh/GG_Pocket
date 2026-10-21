/**
 * A screen's code is loaded on first visit, from a file whose name changes with its content.
 * An installed app can stay in memory across a release (iOS keeps it suspended for days) while the
 * new service worker drops the old files: the next visit to a changed screen then asks for a file
 * that no longer exists, and the navigation fails without a word. Reloading the target page fetches
 * the new version.
 */

/** Messages of a failed `import()`: Chromium, WebKit, Firefox, then Vite's CSS preload. */
const CHUNK_ERROR =
  /Failed to fetch dynamically imported module|Importing a module script failed|error loading dynamically imported module|Unable to preload CSS/i

export function isStaleChunkError(error: unknown): boolean {
  return error instanceof Error && CHUNK_ERROR.test(error.message)
}

const LAST_RELOAD_KEY = 'gg-stale-chunk-reload'
/** A second failure this soon after a reload is not a stale app (server down, offline): give up. */
const RELOAD_COOLDOWN_MS = 30_000

/** Whether to reload now; records the attempt. Without storage, reloads (better than a dead tap). */
export function shouldReload(storage: Pick<Storage, 'getItem' | 'setItem'>, now: number): boolean {
  try {
    const last = Number(storage.getItem(LAST_RELOAD_KEY))
    if (Number.isFinite(last) && last > 0 && now - last < RELOAD_COOLDOWN_MS) {
      return false
    }
    storage.setItem(LAST_RELOAD_KEY, String(now))
  } catch {
    // Private mode or blocked storage: no loop guard, reload anyway.
  }
  return true
}

/** `router.onError` handler: loads `path` from the server when a screen's code is missing. */
export function reloadOnStaleChunk(error: unknown, path: string): void {
  if (!isStaleChunkError(error)) {
    return
  }
  let storage: Storage | undefined
  try {
    storage = window.sessionStorage
  } catch {
    storage = undefined
  }
  if (storage === undefined || shouldReload(storage, Date.now())) {
    window.location.assign(path)
  }
}
