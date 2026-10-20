/**
 * Installing the app on the home screen (spec §12, DECISIONS D-041). Pure functions: the browser
 * facts are passed in, so every case can be tested.
 */

/** How this browser can install the app. */
export type InstallMethod =
  /** Already running as an installed app: never invite. */
  | 'installed'
  /** Browser of another app (WhatsApp, Instagram…): open the real browser first. */
  | 'in-app-browser'
  /** iPhone or iPad: manual steps through the Share button. */
  | 'ios'
  /** Chrome, Edge, Samsung Internet: the browser offers its own install prompt. */
  | 'prompt'
  /** Safari on a Mac: File, then Add to Dock. */
  | 'mac-safari'
  /** Firefox on Android, or anything else: browser menu. */
  | 'menu'

export interface BrowserFacts {
  userAgent: string
  /** display-mode standalone, or navigator.standalone on iOS. */
  standalone: boolean
  /** A beforeinstallprompt event was received and kept. */
  canPrompt: boolean
  /** navigator.maxTouchPoints: iPadOS presents itself as a Mac. */
  maxTouchPoints: number
}

/** User agent markers of in-app browsers that cannot add to the home screen. */
const IN_APP_MARKERS = [
  'FBAN',
  'FBAV',
  'FB_IAB',
  'Instagram',
  'BytedanceWebview',
  'musical_ly',
  'WhatsApp/',
  'Snapchat',
  'LinkedInApp',
] as const

export function isInAppBrowser(userAgent: string): boolean {
  return IN_APP_MARKERS.some((marker) => userAgent.includes(marker))
}

export function isIos(userAgent: string, maxTouchPoints: number): boolean {
  return (
    /iPhone|iPad|iPod/.test(userAgent) || (userAgent.includes('Macintosh') && maxTouchPoints > 1)
  )
}

export function installMethod(facts: BrowserFacts): InstallMethod {
  if (facts.standalone) {
    return 'installed'
  }
  if (isInAppBrowser(facts.userAgent)) {
    return 'in-app-browser'
  }
  if (isIos(facts.userAgent, facts.maxTouchPoints)) {
    return 'ios'
  }
  if (facts.canPrompt) {
    return 'prompt'
  }
  if (
    facts.userAgent.includes('Macintosh') &&
    facts.userAgent.includes('Safari/') &&
    !/Chrome\/|Chromium\/|Edg\/|Firefox\//.test(facts.userAgent)
  ) {
    return 'mac-safari'
  }
  return 'menu'
}

/** "Plus tard": shown again after 7 days, and automatically 3 times at most. */
export const INVITE_REPEAT_DAYS = 7
export const INVITE_MAX_DISMISSALS = 3

export interface InviteHistory {
  dismissals: number
  /** Epoch milliseconds of the last "Plus tard". */
  lastDismissedAt: number | null
}

export const NO_INVITE_HISTORY: InviteHistory = { dismissals: 0, lastDismissedAt: null }

export function shouldInviteAutomatically(
  method: InstallMethod,
  history: InviteHistory,
  now: number,
): boolean {
  if (method === 'installed' || history.dismissals >= INVITE_MAX_DISMISSALS) {
    return false
  }
  return (
    history.lastDismissedAt === null ||
    now - history.lastDismissedAt >= INVITE_REPEAT_DAYS * 24 * 60 * 60 * 1000
  )
}

export function dismissed(history: InviteHistory, now: number): InviteHistory {
  return { dismissals: history.dismissals + 1, lastDismissedAt: now }
}

/** Reads what was stored, ignoring anything malformed (old versions, manual edits). */
export function parseInviteHistory(raw: string | null): InviteHistory {
  if (raw === null) {
    return NO_INVITE_HISTORY
  }
  try {
    const value: unknown = JSON.parse(raw)
    if (
      typeof value === 'object' &&
      value !== null &&
      'dismissals' in value &&
      typeof value.dismissals === 'number' &&
      'lastDismissedAt' in value &&
      (typeof value.lastDismissedAt === 'number' || value.lastDismissedAt === null)
    ) {
      return { dismissals: value.dismissals, lastDismissedAt: value.lastDismissedAt }
    }
  } catch {
    // Malformed: start over.
  }
  return NO_INVITE_HISTORY
}
