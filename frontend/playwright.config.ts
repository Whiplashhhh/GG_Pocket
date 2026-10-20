import { defineConfig, devices } from '@playwright/test'

/**
 * End-to-end tests against the production build served by `vite preview`.
 * Projects required by the Definition of Done: iPhone 13 (WebKit) and Desktop Chrome.
 *
 * PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH (optional) points Chromium projects to a browser installed
 * outside Playwright, for machines where `playwright install` cannot download one.
 */
const chromiumExecutable = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH
const PORT = 4173

export default defineConfig({
  testDir: './e2e',
  testIgnore: '**/screenshots.spec.ts',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: `http://localhost:${String(PORT)}`,
    trace: 'retain-on-failure',
    locale: 'fr-FR',
    timezoneId: 'Europe/Paris',
    // The API is answered by the tests (e2e/fakeClub.ts): a service worker would get in between.
    serviceWorkers: 'block',
  },
  projects: [
    { name: 'iPhone 13', use: { ...devices['iPhone 13'] } },
    {
      name: 'Desktop Chrome',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: chromiumExecutable ? { executablePath: chromiumExecutable } : {},
      },
    },
  ],
  webServer: {
    command: `pnpm preview --port ${String(PORT)} --strictPort`,
    port: PORT,
    reuseExistingServer: !process.env.CI,
  },
})
