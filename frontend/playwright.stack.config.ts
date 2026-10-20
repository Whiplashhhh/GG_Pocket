import { defineConfig, devices } from '@playwright/test'

/**
 * End-to-end tests against the real Docker stack (web, api, db) started by the CI job "Docker
 * stack" with the demo seed: what the fake API of e2e/ cannot prove (headers, CSP, cookies,
 * proxying, the real sign-in). Chromium only: the session cookie is Secure, and only Chromium
 * accepts it on http://localhost (DECISIONS D-067).
 *
 * GG_E2E_BASE_URL (default http://localhost:8080) and GG_SEED_PASSWORD (demo accounts) come from
 * the environment.
 */
const chromiumExecutable = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH

export default defineConfig({
  testDir: './e2e-stack',
  fullyParallel: false,
  workers: 1,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env.GG_E2E_BASE_URL ?? 'http://localhost:8080',
    trace: 'retain-on-failure',
    locale: 'fr-FR',
    timezoneId: 'Europe/Paris',
  },
  projects: [
    {
      name: 'Desktop Chrome',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: chromiumExecutable ? { executablePath: chromiumExecutable } : {},
      },
    },
  ],
})
