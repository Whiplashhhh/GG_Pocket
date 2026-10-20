import { test as base } from '@playwright/test'

import type { components } from '../src/api/schema'

import { FakeClub, type FakeMember } from './fakeClub'

type Role = components['schemas']['CurrentMember']['role']

export interface MetaOverride {
  environment?: 'local' | 'dev' | 'prod'
  branch?: string
  commit?: string
}

/**
 * The e2e tests run against the static build, with the API answered by {@link FakeClub}
 * (DECISIONS D-040). Phase 2 runs them against the real Docker stack.
 */
export const test = base.extend<{
  meta: MetaOverride
  /** Signed in as a member of this role before the test starts; null: a visitor. */
  signedInAs: Role | null
  /** The invitation to install pops up by itself; most tests keep it quiet. */
  installInvite: boolean
  club: FakeClub
  me: FakeMember | null
}>({
  meta: [{}, { option: true }],
  signedInAs: ['CLIMBER', { option: true }],
  installInvite: [false, { option: true }],
  club: async ({ signedInAs }, use) => {
    const club = new FakeClub()
    if (signedInAs !== null) {
      club.signIn(club.addMember({ nickname: 'marie', role: signedInAs }))
    }
    await use(club)
  },
  me: async ({ club }, use) => {
    await use(club.members.find((member) => member.nickname === 'marie') ?? null)
  },
  page: async ({ page, meta, club, installInvite }, use) => {
    await club.install(page)
    if (Object.keys(meta).length > 0) {
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
    }
    if (!installInvite) {
      await page.addInitScript(() => {
        localStorage.setItem(
          'gg-pocket:install-invite',
          JSON.stringify({ dismissals: 3, lastDismissedAt: Date.now() }),
        )
      })
    }
    await use(page)
  },
})

export { expect } from '@playwright/test'
