import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'

import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import tailwindcss from '@tailwindcss/vite'
import vue from '@vitejs/plugin-vue'
import { defineConfig, type UserConfig } from 'vite'
import { VitePWA, type VitePWAOptions } from 'vite-plugin-pwa'

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
  },
} satisfies Partial<VitePWAOptions>

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
      tailwindcss(),
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
