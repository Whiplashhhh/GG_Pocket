import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { nextTick } from 'vue'

import { THEME_STORAGE_KEY, applyThemePreference, isThemePreference, useThemeStore } from './theme'

describe('theme preference', () => {
  beforeEach(() => {
    localStorage.clear()
    document.documentElement.removeAttribute('data-theme')
    setActivePinia(createPinia())
  })

  it('follows the system by default (no data-theme)', async () => {
    const store = useThemeStore()
    await nextTick()
    expect(store.preference).toBe('auto')
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false)
  })

  it('forces dark, remembers it, then goes back to auto', async () => {
    const store = useThemeStore()
    store.preference = 'dark'
    await nextTick()
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('dark')

    store.preference = 'auto'
    await nextTick()
    expect(document.documentElement.hasAttribute('data-theme')).toBe(false)
  })

  it('ignores a corrupted stored value', async () => {
    localStorage.setItem(THEME_STORAGE_KEY, 'neon')
    const store = useThemeStore()
    await nextTick()
    expect(store.preference).toBe('auto')
  })

  it('only accepts the three known preferences', () => {
    expect(isThemePreference('light')).toBe(true)
    expect(isThemePreference('neon')).toBe(false)
    expect(isThemePreference(undefined)).toBe(false)
  })

  it('applies light by setting the attribute on the given element', () => {
    const element = document.createElement('div')
    applyThemePreference('light', element)
    expect(element.getAttribute('data-theme')).toBe('light')
  })
})
