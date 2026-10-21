import { afterEach, describe, expect, it, vi } from 'vitest'

import {
  forgetReload,
  isStaleChunkError,
  reloadOnStaleChunk,
  screenLoadFailure,
  shouldReload,
} from './stale-chunk'

function memoryStorage() {
  const items = new Map<string, string>()
  return {
    getItem: (key: string) => items.get(key) ?? null,
    setItem: (key: string, value: string) => void items.set(key, value),
  }
}

describe('isStaleChunkError', () => {
  it.each([
    'Failed to fetch dynamically imported module: https://pocket.example/assets/ProfileView-abc.js',
    'Importing a module script failed.',
    'error loading dynamically imported module: https://pocket.example/assets/ProfileView-abc.js',
    'Unable to preload CSS for /assets/ProfileView-abc.css',
  ])('recognises “%s”', (message) => {
    expect(isStaleChunkError(new TypeError(message))).toBe(true)
  })

  it('ignores other failures', () => {
    expect(isStaleChunkError(new Error('Request failed with status 500'))).toBe(false)
    expect(isStaleChunkError('Failed to fetch dynamically imported module')).toBe(false)
    expect(isStaleChunkError(undefined)).toBe(false)
  })
})

describe('shouldReload', () => {
  it('reloads once, then gives up for a while, then reloads again', () => {
    const storage = memoryStorage()
    expect(shouldReload(storage, 1_000_000)).toBe(true)
    expect(shouldReload(storage, 1_010_000)).toBe(false)
    expect(shouldReload(storage, 1_031_000)).toBe(true)
  })

  it('ignores garbage left in storage', () => {
    const storage = memoryStorage()
    storage.setItem('gg-stale-chunk-reload', 'pas un nombre')
    expect(shouldReload(storage, 1_000_000)).toBe(true)
  })

  it('reloads when storage throws', () => {
    const throwing = {
      getItem: () => {
        throw new Error('SecurityError')
      },
      setItem: () => {
        throw new Error('SecurityError')
      },
    }
    expect(shouldReload(throwing, 1_000_000)).toBe(true)
  })
})

describe('reloadOnStaleChunk', () => {
  const missing = new TypeError('Importing a module script failed.')

  afterEach(() => {
    forgetReload()
    screenLoadFailure.value = null
    vi.restoreAllMocks()
  })

  it('reloads the target page the first time, shows the failure the second', () => {
    const assign = vi.fn()
    vi.spyOn(window, 'location', 'get').mockReturnValue({ assign } as unknown as Location)

    reloadOnStaleChunk(missing, '/profil')
    expect(assign).toHaveBeenCalledExactlyOnceWith('/profil')
    expect(screenLoadFailure.value).toBeNull()

    reloadOnStaleChunk(missing, '/profil')
    expect(assign).toHaveBeenCalledOnce()
    expect(screenLoadFailure.value).toEqual({
      path: '/profil',
      message: 'Importing a module script failed.',
    })
  })

  it('leaves other navigation errors alone', () => {
    reloadOnStaleChunk(new Error('boom'), '/profil')
    expect(screenLoadFailure.value).toBeNull()
  })
})
