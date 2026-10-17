import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'

import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import tailwindcss from '@tailwindcss/vite'
import vue from '@vitejs/plugin-vue'
import { defineConfig, type UserConfig } from 'vite'

/** Single source of truth for the version: the repository's VERSION file (docs/DECISIONS.md D-032). */
const appVersion = readFileSync(new URL('../VERSION', import.meta.url), 'utf8').trim()

/**
 * Shared by the app and the unit tests. Unit tests skip the i18n precompilation: its virtual
 * modules do not load in Vitest, and the runtime compiler is fine there.
 */
export function createViteConfig({
  precompileMessages,
}: {
  precompileMessages: boolean
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
    ],
    define: {
      __APP_VERSION__: JSON.stringify(appVersion),
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

export default defineConfig(createViteConfig({ precompileMessages: true }))
