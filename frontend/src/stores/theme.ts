import { useLocalStorage } from '@vueuse/core'
import { defineStore } from 'pinia'
import { computed, watchEffect } from 'vue'

export const THEME_PREFERENCES = ['auto', 'light', 'dark'] as const
export type ThemePreference = (typeof THEME_PREFERENCES)[number]

/** Same key as public/theme-init.js, which applies the theme before the first paint. */
export const THEME_STORAGE_KEY = 'gg-pocket:theme'

export function isThemePreference(value: unknown): value is ThemePreference {
  return THEME_PREFERENCES.includes(value as ThemePreference)
}

/** "auto" leaves the choice to CSS (prefers-color-scheme); light/dark force it. */
export function applyThemePreference(preference: ThemePreference, root: HTMLElement): void {
  if (preference === 'auto') {
    root.removeAttribute('data-theme')
  } else {
    root.setAttribute('data-theme', preference)
  }
}

export const useThemeStore = defineStore('theme', () => {
  const stored = useLocalStorage<string>(THEME_STORAGE_KEY, 'auto', {
    // Private browsing may block storage: the theme then simply follows the system.
    onError: () => undefined,
  })

  const preference = computed<ThemePreference>({
    get: () => (isThemePreference(stored.value) ? stored.value : 'auto'),
    set: (value) => {
      stored.value = value
    },
  })

  watchEffect(() => {
    applyThemePreference(preference.value, document.documentElement)
  })

  return { preference }
})
