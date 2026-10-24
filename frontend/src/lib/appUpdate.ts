/**
 * Keeps the installed app up to date (spec §8.12) and says why when it cannot.
 *
 * The browser looks for a new service worker only when the app starts from scratch. An installed
 * app on a phone is mostly resumed from the background: without help it can run an old version for
 * weeks. So we also look when the app comes back to the foreground, and every hour.
 *
 * A new version installs only if every file of the app downloads. One file refused by the phone
 * (a filter, docs/DECISIONS.md D-073) and the installation fails without a word: the old version
 * stays. `checkForUpdate` reports it, with the files that did not come.
 */

const CHECK_EVERY_MS = 60 * 60 * 1000
const INSTALL_TIMEOUT_MS = 60 * 1000

let watched: ServiceWorkerRegistration | undefined

/** Called once the service worker is registered (UpdateBanner). */
export function watchForUpdates(registration: ServiceWorkerRegistration): void {
  if (watched !== undefined) return
  watched = registration
  const look = () => {
    registration.update().catch(() => undefined)
  }
  window.setInterval(look, CHECK_EVERY_MS)
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') look()
  })
}

export type UpdateCheck =
  | { kind: 'unsupported' }
  | { kind: 'upToDate' }
  /** Installed, waiting for the member to reload (the update banner offers it). */
  | { kind: 'ready' }
  /** The new version could not install; `refused`: files that did not download, with why. */
  | { kind: 'failed'; refused: string[] }
  | { kind: 'error'; message: string }

function describe(error: unknown): string {
  return error instanceof Error ? `${error.name}: ${error.message}` : String(error)
}

/** Until the worker is installed (or activated), or gives up (redundant). */
function settled(worker: ServiceWorker): Promise<ServiceWorkerState | 'timeout'> {
  return new Promise((resolve) => {
    const done = (state: ServiceWorkerState | 'timeout') => {
      worker.removeEventListener('statechange', onChange)
      window.clearTimeout(timer)
      resolve(state)
    }
    const onChange = () => {
      if (['installed', 'activated', 'redundant'].includes(worker.state)) done(worker.state)
    }
    const timer = window.setTimeout(() => {
      done('timeout')
    }, INSTALL_TIMEOUT_MS)
    worker.addEventListener('statechange', onChange)
    onChange()
  })
}

/** Looks for a new version now, and tells what came of it. */
export async function checkForUpdate(): Promise<UpdateCheck> {
  if (!('serviceWorker' in navigator)) return { kind: 'unsupported' }
  try {
    const registration = watched ?? (await navigator.serviceWorker.getRegistration())
    if (registration === undefined) return { kind: 'failed', refused: await refusedFiles() }
    await registration.update()
    const installing = registration.installing
    if (installing !== null) {
      const state = await settled(installing)
      if (state === 'redundant' || state === 'timeout') {
        return { kind: 'failed', refused: await refusedFiles() }
      }
    }
    if (registration.waiting !== null) return { kind: 'ready' }
    // Registered, but no version ever took over: the first installation failed.
    if (registration.active === null) return { kind: 'failed', refused: await refusedFiles() }
    return { kind: 'upToDate' }
  } catch (error) {
    return { kind: 'error', message: describe(error) }
  }
}

/** The files the service worker downloads to install (Workbox precache manifest). */
export function precacheUrls(serviceWorker: string): string[] {
  const urls = [...serviceWorker.matchAll(/\burl:\s*"([^"]+)"/g)].map((match) => match[1] ?? '')
  return [...new Set(urls.filter((url) => url !== ''))]
}

/**
 * Asks for every file of the new version, like the installation does, and lists the misfits. For
 * a script, the same bytes are also asked under a neutral name (`/sonde/temoin.js`, see the web
 * Caddyfile): refused there too, the phone judges the content; fine there, it judges the name.
 */
export async function refusedFiles(): Promise<string[]> {
  let manifest: string
  try {
    manifest = await (await fetch('/sw.js', { cache: 'no-store' })).text()
  } catch (error) {
    return [`/sw.js → ${describe(error)}`]
  }
  const answers = await Promise.all(
    precacheUrls(manifest).map(async (url): Promise<string[]> => {
      const path = `/${url.replace(/^\//, '')}`
      const answer = await fetchProblem(path)
      if (answer === null) return []
      const file = /^\/assets\/([A-Za-z0-9_-]+\.js)$/.exec(path)?.[1]
      if (file === undefined) return [`${path} → ${answer}`]
      const neutral = await fetchProblem(`/sonde/temoin.js?f=${file}`)
      return [`${path} → ${answer}`, `  même contenu, autre nom → ${neutral ?? 'OK'}`]
    }),
  )
  return answers.flat()
}

/** null when the file comes back fine, else what went wrong. */
export async function fetchProblem(url: string): Promise<string | null> {
  try {
    const response = await fetch(url, { cache: 'no-store' })
    if (!response.ok) return `${String(response.status)} ${response.statusText}`.trim()
    await response.arrayBuffer()
    return null
  } catch (error) {
    return describe(error)
  }
}
