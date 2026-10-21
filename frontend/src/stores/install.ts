import { useLocalStorage } from '@vueuse/core'
import { defineStore } from 'pinia'
import { computed, shallowRef } from 'vue'

import {
  dismissed,
  installMethod,
  parseInviteHistory,
  shouldInviteAutomatically,
  type InviteHistory,
} from '@/lib/install'

/** Chrome, Edge and Samsung Internet only; not in the DOM typings. */
export interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>
  readonly userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

const HISTORY_KEY = 'gg-pocket:install-invite'

/** Kept from page load on: the browser fires it once, often before the app is mounted. */
let pendingPrompt: BeforeInstallPromptEvent | null = null
let onPromptAvailable: ((event: BeforeInstallPromptEvent) => void) | null = null

export function listenForInstallPrompt(target: Window = window): void {
  target.addEventListener('beforeinstallprompt', (event) => {
    // Keep the browser's mini infobar away: our own invitation explains why to install.
    event.preventDefault()
    pendingPrompt = event as BeforeInstallPromptEvent
    onPromptAvailable?.(pendingPrompt)
  })
}

export function isStandalone(): boolean {
  const iosStandalone = (navigator as Navigator & { standalone?: boolean }).standalone === true
  return iosStandalone || window.matchMedia('(display-mode: standalone)').matches
}

export const useInstallStore = defineStore('install', () => {
  const prompt = shallowRef<BeforeInstallPromptEvent | null>(pendingPrompt)
  onPromptAvailable = (event) => (prompt.value = event)
  const justInstalled = shallowRef(false)
  window.addEventListener('appinstalled', () => {
    justInstalled.value = true
    prompt.value = null
  })

  const stored = useLocalStorage<string | null>(HISTORY_KEY, null, {
    // Private browsing may block storage: the invitation then shows at most once per visit.
    onError: () => undefined,
  })
  const history = computed<InviteHistory>(() => parseInviteHistory(stored.value))

  const method = computed(() =>
    installMethod({
      userAgent: navigator.userAgent,
      standalone: isStandalone(),
      canPrompt: prompt.value !== null,
      maxTouchPoints: navigator.maxTouchPoints,
    }),
  )

  const shouldInvite = computed(
    () =>
      !justInstalled.value && shouldInviteAutomatically(method.value, history.value, Date.now()),
  )

  /** "Plus tard". */
  function postpone() {
    stored.value = JSON.stringify(dismissed(history.value, Date.now()))
  }

  /** Opens the browser's own install dialog, when it offered one. */
  async function install(): Promise<boolean> {
    const event = prompt.value
    if (event === null) {
      return false
    }
    await event.prompt()
    const choice = await event.userChoice
    // A prompt can only be used once.
    prompt.value = null
    pendingPrompt = null
    if (choice.outcome === 'accepted') {
      justInstalled.value = true
    }
    return choice.outcome === 'accepted'
  }

  return { method, shouldInvite, justInstalled, postpone, install }
})
