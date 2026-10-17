import { test as base } from '@playwright/test'

export interface MetaOverride {
  environment?: 'local' | 'dev' | 'prod'
  branch?: string
  commit?: string
}

/**
 * The phase 0 e2e tests run against the static build only: API calls are answered here.
 * Phase 2 runs them against the real Docker stack.
 */
export const test = base.extend<{ meta: MetaOverride }>({
  meta: [{}, { option: true }],
  page: async ({ page, meta }, use) => {
    await page.route('**/api/v1/meta', (route) =>
      route.fulfill({
        json: {
          version: '0.0.0',
          environment: meta.environment ?? 'local',
          branch: meta.branch ?? 'main',
          commit: meta.commit ?? '0123456789abcdef',
        },
      }),
    )
    await use(page)
  },
})

export { expect } from '@playwright/test'
