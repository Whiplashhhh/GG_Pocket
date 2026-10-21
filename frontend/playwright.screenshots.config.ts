import { defineConfig, devices } from '@playwright/test'

import baseConfig from './playwright.config.ts'

/**
 * Screenshots for visual review (Definition of Done: light and dark, 360 px and 1280 px).
 * Output: screenshots/ (git-ignored). Runs against the dev server, so the component gallery
 * (/dev/galerie, absent from production builds) is captured too: `pnpm screenshots`.
 */
const DEV_PORT = 5174
const chromiumExecutable = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH
// --lang: native date fields follow the browser's language, not only the context locale.
const launchOptions = {
  args: ['--lang=fr-FR'],
  ...(chromiumExecutable ? { executablePath: chromiumExecutable } : {}),
}

const variants = [
  { name: 'mobile-light', width: 360, colorScheme: 'light' },
  { name: 'mobile-dark', width: 360, colorScheme: 'dark' },
  { name: 'desktop-light', width: 1280, colorScheme: 'light' },
  { name: 'desktop-dark', width: 1280, colorScheme: 'dark' },
] as const

export default defineConfig({
  ...baseConfig,
  testIgnore: undefined,
  testMatch: '**/screenshots.spec.ts',
  retries: 0,
  reporter: 'list',
  use: { ...baseConfig.use, baseURL: `http://localhost:${String(DEV_PORT)}` },
  webServer: {
    command: `pnpm dev --port ${String(DEV_PORT)} --strictPort`,
    port: DEV_PORT,
    reuseExistingServer: true,
  },
  projects: variants.map((variant) => ({
    name: variant.name,
    use: {
      ...devices['Desktop Chrome'],
      viewport: { width: variant.width, height: variant.width < 500 ? 780 : 900 },
      deviceScaleFactor: variant.width < 500 ? 2 : 1,
      isMobile: variant.width < 500,
      hasTouch: variant.width < 500,
      colorScheme: variant.colorScheme,
      locale: 'fr-FR',
      launchOptions,
    },
  })),
})
