import { defineConfig } from '@playwright/test'

import baseConfig from './playwright.config.ts'

/**
 * Lighthouse audit of the main screens (e2e/lighthouse.spec.ts): `pnpm build && pnpm lighthouse`.
 * It serves dist/ itself and launches its own Chromium (PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH
 * honoured); one screen at a time, Lighthouse wants the machine to itself.
 */
export default defineConfig({
  ...baseConfig,
  testIgnore: undefined,
  testMatch: '**/lighthouse.spec.ts',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 180_000,
  reporter: 'list',
  webServer: undefined,
  projects: [{ name: 'lighthouse' }],
})
