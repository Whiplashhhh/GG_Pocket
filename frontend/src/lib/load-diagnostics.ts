import { isStandalone } from '@/stores/install'

import { fetchProblem, refusedFiles } from './appUpdate'

import type { ScreenLoadFailure } from './stale-chunk'

/**
 * What a member can copy and send when a screen refuses to load: no personal data, only the app's
 * own files and how the phone answered for them.
 */
export async function collectLoadDiagnostics(failure: ScreenLoadFailure): Promise<string[]> {
  const lines = [
    `version ${__APP_VERSION__}`,
    `page ${failure.path}`,
    `erreur ${failure.message}`,
    `mode ${isStandalone() ? 'appli installée' : 'navigateur'}`,
    `navigateur ${navigator.userAgent}`,
  ]
  // Absent outside HTTPS and localhost.
  const controller = 'serviceWorker' in navigator ? navigator.serviceWorker.controller : null
  lines.push(`service worker ${controller ? controller.state : 'aucun'}`)
  lines.push(`inscription ${await registrationState()}`)
  if ('caches' in window) {
    lines.push(`caches ${(await caches.keys()).join(', ') || 'aucun'}`)
  }
  lines.push(...(await probeScripts(scriptUrls())), ...(await probeNames()))
  const refused = await refusedFiles()
  lines.push(`fichiers de l'appli ${refused.length === 0 ? 'tous OK' : 'refusés :'}`, ...refused)
  return lines
}

/** The app's scripts this page asked for, plus those it was about to load. */
export function scriptUrls(): string[] {
  const urls = new Set<string>()
  for (const entry of performance.getEntriesByType('resource')) {
    if (isAppScript(entry.name)) urls.add(entry.name)
  }
  for (const link of document.querySelectorAll<HTMLLinkElement>('link[rel="modulepreload"]')) {
    if (isAppScript(link.href)) urls.add(link.href)
  }
  return [...urls]
}

function isAppScript(url: string): boolean {
  return new URL(url, window.location.href).pathname.match(/^\/assets\/.+\.js$/) !== null
}

/** Asks for each script again (through the service worker, like the page does) and reports misfits. */
async function probeScripts(urls: string[]): Promise<string[]> {
  const misfits: string[] = []
  await Promise.all(
    urls.map(async (url) => {
      const file = new URL(url).pathname
      const answer = await probe(url)
      if (answer !== null) {
        // Same file under another address: tells a refused address from a file that breaks in transit.
        const name = file.replace(/^\/assets\//, '')
        misfits.push(
          `${file} → ${answer}`,
          `  ${file}?sonde → ${(await probe(`${url}?sonde=${String(Date.now())}`)) ?? 'OK'}`,
          `  même contenu, autre nom → ${(await fetchProblem(`/sonde/temoin.js?f=${name}`)) ?? 'OK'}`,
        )
      }
    }),
  )
  return [`scripts ${String(urls.length - misfits.length)}/${String(urls.length)} OK`, ...misfits]
}

/**
 * Files that do not exist: the server answers 404 for all of them. A filter on the phone (content
 * blocker, tracking protection, parental control) refuses some names before asking (`Load failed`).
 */
const NAME_PROBES = [
  'ProfileView-sonde.js',
  'profile-sonde.js',
  'Profil-sonde.js',
  'Temoin-sonde.js',
]

async function probeNames(): Promise<string[]> {
  return Promise.all(
    NAME_PROBES.map(async (name) => {
      try {
        const response = await fetch(`/assets/${name}`, { cache: 'no-store' })
        return `nom ${name} → ${String(response.status)}`
      } catch (error) {
        return `nom ${name} → ${error instanceof Error ? `${error.name}: ${error.message}` : String(error)}`
      }
    }),
  )
}

/** Each version the service worker registration holds: active, waiting, installing. */
async function registrationState(): Promise<string> {
  if (!('serviceWorker' in navigator)) return 'impossible'
  const registration = await navigator.serviceWorker.getRegistration()
  if (registration === undefined) return 'aucune'
  const states = [
    `active ${registration.active?.state ?? 'non'}`,
    `en attente ${registration.waiting ? 'oui' : 'non'}`,
    `en installation ${registration.installing?.state ?? 'non'}`,
  ]
  return states.join(', ')
}

/** null when the script comes back fine, else what went wrong. */
async function probe(url: string): Promise<string | null> {
  try {
    const response = await fetch(url, { cache: 'no-store' })
    const type = response.headers.get('content-type') ?? '?'
    if (!response.ok || !type.includes('javascript')) {
      return `${String(response.status)} ${response.type} ${type}`
    }
    const body = await response.text()
    return body.length > 0 ? null : 'vide'
  } catch (error) {
    return error instanceof Error ? `${error.name}: ${error.message}` : String(error)
  }
}

/** Drops the service worker and its caches: the next load comes straight from the server. */
export async function resetAppCaches(): Promise<void> {
  const registrations =
    'serviceWorker' in navigator ? await navigator.serviceWorker.getRegistrations() : []
  await Promise.all(registrations.map((registration) => registration.unregister()))
  if ('caches' in window) {
    await Promise.all((await caches.keys()).map((name) => caches.delete(name)))
  }
}
