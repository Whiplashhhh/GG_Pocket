import { readFileSync } from 'node:fs'

import { parseChangelog } from '../src/lib/changelog'

import { expect, test } from './fixtures'

/** « Quoi de neuf ? » (spec §8.12, D-108). */
const VERSION = readFileSync(new URL('../../VERSION', import.meta.url), 'utf8').trim()
/** Newest first; the running version is the newest published one. */
const RELEASES = parseChangelog(
  readFileSync(new URL('../../CHANGELOG.md', import.meta.url), 'utf8'),
).map((release) => release.version)

test.describe('after an update', () => {
  test.use({ releaseNotesPopup: true })

  test('the news of the version pops up once, then stays quiet', async ({ page, me }) => {
    await page.goto('/actu')
    const dialog = page.getByRole('dialog', { name: 'Quoi de neuf ?' })
    await expect(dialog).toBeVisible()
    await expect(dialog.getByRole('heading', { name: `Version ${VERSION}` })).toBeVisible()
    await dialog.getByRole('button', { name: 'C’est noté, on grimpe !' }).click()
    await expect(dialog).toBeHidden()

    const key = `gg-pocket:release-notes:${me?.id ?? ''}`
    expect(await page.evaluate((name) => localStorage.getItem(name), key)).toBe(VERSION)
    await page.reload()
    await expect(page.getByRole('navigation', { name: 'Navigation principale' })).toBeVisible()
    // Leaves the pop-up the time to show up, if it were to.
    await page.waitForTimeout(500)
    await expect(dialog).toHaveCount(0)
  })

  test('catches up on every version since the last one seen', async ({ page, me }) => {
    expect(RELEASES[0]).toBe(VERSION)
    // Two versions behind: the pop-up tells about the last two.
    const seen = RELEASES[2] ?? ''
    await page.addInitScript(
      ([id, seen]) => {
        localStorage.setItem(`gg-pocket:release-notes:${id}`, seen)
      },
      [me?.id ?? '', seen] as const,
    )
    await page.goto('/actu')
    const dialog = page.getByRole('dialog', { name: 'Quoi de neuf ?' })
    await expect(dialog.getByRole('heading', { name: /^Version / })).toHaveText(
      RELEASES.slice(0, 2).map((version) => `Version ${version}`),
    )
    await dialog.getByRole('link', { name: 'Toutes les nouveautés' }).click()
    await expect(dialog).toBeHidden()
    await expect(page).toHaveURL(/\/profil\/nouveautes$/)
  })
})

test('every release stays readable from the profile', async ({ page }) => {
  await page.goto('/profil')
  await page.getByRole('link', { name: 'Quoi de neuf ?' }).click()
  await expect(page.getByRole('heading', { level: 1, name: 'Quoi de neuf ?' })).toBeVisible()
  await expect(page.getByRole('heading', { level: 2, name: `Version ${VERSION}` })).toBeVisible()
  const versions = page.getByRole('heading', { level: 2, name: /^Version / })
  await expect(versions).toHaveCount(3)
  await page
    .getByRole('button', { name: `Voir les ${String(RELEASES.length - 3)} versions d’avant` })
    .click()
  await expect(versions).toHaveCount(RELEASES.length)
  await expect(page.getByRole('heading', { name: 'Nouveau' }).first()).toBeVisible()
})
