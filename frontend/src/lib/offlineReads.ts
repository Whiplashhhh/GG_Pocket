/**
 * Spec §12: the topo, the profile, the planning and the "à faire" list stay readable offline. The
 * service worker reads them network first and keeps the last answer in this cache (vite.config.ts).
 * They belong to the member signed in: the cache goes whenever someone signs in or out.
 */
export const OFFLINE_READS_CACHE = 'gg-offline-reads'

/** The API reads kept for offline use (GET only, 200 only). */
export const OFFLINE_READS =
  /\/api\/v1\/(?:routes(?:\/[0-9a-f-]{36})?|topo\/config|me|me\/(?:favorites|todo|planning))(?:\?.*)?$/

export function forgetOfflineReads(): void {
  if (typeof caches === 'undefined') return
  void caches.delete(OFFLINE_READS_CACHE).catch(() => undefined)
}
