import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'

import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import tailwindcss from '@tailwindcss/vite'
import vue from '@vitejs/plugin-vue'
import { defineConfig, type Plugin, type UserConfig } from 'vite'
import { VitePWA, type VitePWAOptions } from 'vite-plugin-pwa'

import { parseChangelog } from './src/lib/changelog.ts'
import { renderGuide } from './src/lib/guide.ts'
import { OFFLINE_READS, OFFLINE_READS_CACHE } from './src/lib/offlineReads.ts'

/** Single source of truth for the version: the repository's VERSION file (docs/DECISIONS.md D-032). */
const appVersion = readFileSync(new URL('../VERSION', import.meta.url), 'utf8').trim()

/**
 * Installable app (spec §12). "prompt": a new version waits until the member accepts to reload,
 * never in the middle of a form. Only the app shell is cached; API answers never are (sign-in,
 * private photos), so a shared phone keeps nothing of the previous member.
 */
const pwaOptions = {
  registerType: 'prompt',
  injectRegister: false,
  includeAssets: ['favicon-48.png', 'apple-touch-icon.png', 'theme-init.js'],
  manifest: {
    id: '/',
    name: 'GG Pocket',
    short_name: 'GG Pocket',
    description: 'L’appli du club d’escalade Gravelines Grimp.',
    lang: 'fr',
    dir: 'ltr',
    start_url: '/',
    scope: '/',
    display: 'standalone',
    orientation: 'portrait',
    background_color: '#f4f1ea',
    theme_color: '#f4f1ea',
    categories: ['sports', 'lifestyle'],
    icons: [
      { src: '/pwa-192.png', sizes: '192x192', type: 'image/png' },
      { src: '/pwa-512.png', sizes: '512x512', type: 'image/png' },
      { src: '/pwa-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
    ],
  },
  workbox: {
    globPatterns: ['**/*.{js,css,html,woff2,png,webp,svg}'],
    navigateFallback: '/index.html',
    navigateFallbackDenylist: [/^\/api\//],
    cleanupOutdatedCaches: true,
    // Spec §12: topo, profile, planning and "à faire" readable offline (src/lib/offlineReads.ts).
    runtimeCaching: [
      {
        urlPattern: OFFLINE_READS,
        method: 'GET',
        handler: 'NetworkFirst',
        options: {
          cacheName: OFFLINE_READS_CACHE,
          networkTimeoutSeconds: 5,
          cacheableResponse: { statuses: [200] },
          expiration: { maxEntries: 60, maxAgeSeconds: 14 * 24 * 60 * 60 },
        },
      },
    ],
  },
} satisfies Partial<VitePWAOptions>

/**
 * A repository document turned into data at build time, served as a virtual module: the release
 * notes (spec §8.12, CHANGELOG.md → `virtual:changelog`) and the club guide (D-110,
 * docs/GUIDE_CLUB.md → `virtual:club-guide`). The app never parses Markdown itself.
 */
function documentModule(id: string, path: string, transform: (text: string) => unknown): Plugin {
  const file = fileURLToPath(new URL(path, import.meta.url))
  return {
    name: `gg-document:${id}`,
    resolveId: (source) => (source === id ? `\0${id}` : undefined),
    load(source) {
      if (source !== `\0${id}`) return undefined
      this.addWatchFile(file)
      return `export default ${JSON.stringify(transform(readFileSync(file, 'utf8')))}`
    },
  }
}

/**
 * Preloads the text font (latin, upright): it arrives with the stylesheet instead of after the
 * first paint, so the text does not reflow under the reader (layout shift, Lighthouse CLS). The
 * handwritten font of the route labels is not needed by every screen and stays lazy.
 */
function preloadTextFont(): Plugin {
  const FACE = /archivo-latin-standard-normal\.woff2$/
  return {
    name: 'gg-preload-text-font',
    apply: 'build',
    transformIndexHtml: {
      order: 'post',
      handler: (_html, context) =>
        Object.values(context.bundle ?? {})
          .filter(
            (output) =>
              output.type === 'asset' && output.originalFileNames.some((name) => FACE.test(name)),
          )
          .map((font) => ({
            tag: 'link',
            attrs: {
              rel: 'preload',
              href: `/${font.fileName}`,
              as: 'font',
              type: 'font/woff2',
              crossorigin: '',
            },
            injectTo: 'head',
          })),
    },
  }
}

/**
 * Shared by the app and the unit tests. Unit tests skip the i18n precompilation (its virtual
 * modules do not load in Vitest, and the runtime compiler is fine there) and the service worker.
 */
export function createViteConfig({
  precompileMessages,
  pwa,
}: {
  precompileMessages: boolean
  pwa: boolean
}): UserConfig {
  return {
    plugins: [
      vue(),
      documentModule('virtual:changelog', '../CHANGELOG.md', parseChangelog),
      documentModule('virtual:club-guide', '../docs/GUIDE_CLUB.md', renderGuide),
      tailwindcss(),
      preloadTextFont(),
      // Precompiles messages at build time: no runtime message compiler, so no eval (strict CSP).
      precompileMessages &&
        VueI18nPlugin({
          include: [fileURLToPath(new URL('./src/i18n/*.json', import.meta.url))],
          runtimeOnly: true,
          compositionOnly: true,
          fullInstall: false,
        }),
      pwa && VitePWA(pwaOptions),
    ],
    define: {
      __APP_VERSION__: JSON.stringify(appVersion),
    },
    build: {
      rolldownOptions: {
        output: {
          // Hashes only, no screen names: a filter on one iPhone refused every file named
          // "…profil…" (docs/DECISIONS.md D-073). Blocker lists judge names, not content.
          chunkFileNames: 'assets/[hash].js',
          assetFileNames: 'assets/[hash][extname]',
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      // Same origin as in production: the API is reached through /api.
      proxy: {
        '/api': 'http://localhost:8080',
      },
    },
    preview: {
      proxy: {
        '/api': 'http://localhost:8080',
      },
    },
  }
}

export default defineConfig(createViteConfig({ precompileMessages: true, pwa: true }))
