import { afterEach, describe, expect, it, vi } from 'vitest'

import { precacheUrls, refusedFiles } from './appUpdate'

const SERVICE_WORKER =
  'e.precacheAndRoute([{url:"assets/AbCd1234.js",revision:null},{url:"index.html",revision:"9"},' +
  '{url:"pwa-192.png",revision:"1"},{url:"index.html",revision:"9"}],{})'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('precacheUrls', () => {
  it('reads the files of the Workbox manifest, once each', () => {
    expect(precacheUrls(SERVICE_WORKER)).toEqual([
      'assets/AbCd1234.js',
      'index.html',
      'pwa-192.png',
    ])
  })
})

describe('refusedFiles', () => {
  it('lists the files the phone refused, and tells a refused name from a refused content', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((url: string) => {
        if (url === '/sw.js') return Promise.resolve(new Response(SERVICE_WORKER))
        if (url === '/assets/AbCd1234.js') return Promise.reject(new TypeError('Load failed'))
        if (url === '/pwa-192.png') return Promise.resolve(new Response('', { status: 404 }))
        return Promise.resolve(new Response('ok'))
      }),
    )
    expect(await refusedFiles()).toEqual([
      '/assets/AbCd1234.js → TypeError: Load failed',
      '  même contenu, autre nom → OK',
      '/pwa-192.png → 404',
    ])
  })

  it('is empty when everything comes', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((url: string) =>
        Promise.resolve(new Response(url === '/sw.js' ? SERVICE_WORKER : 'ok')),
      ),
    )
    expect(await refusedFiles()).toEqual([])
  })
})
