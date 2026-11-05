import AxeBuilder from '@axe-core/playwright'

import { expect, test } from './fixtures'
import { CAPTURE_META, CAPTURES } from './scenarios'

/**
 * Accessibility audit (spec §13, phase 8): every screen of the visual review, light and dark,
 * goes through axe-core with the WCAG 2.2 A and AA rules plus axe's best practices. A screen
 * that breaks one fails with the rule, its impact and the elements at fault.
 */
const TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa', 'best-practice']

test.use({ meta: CAPTURE_META, reducedMotion: 'reduce' })

for (const colorScheme of ['light', 'dark'] as const) {
  test.describe(colorScheme, () => {
    test.use({ colorScheme })

    // The component gallery is a development page, absent from the production build.
    for (const capture of CAPTURES.filter((scenario) => scenario.path !== '/dev/galerie')) {
      test.describe(capture.name, () => {
        test.use({
          signedInAs: capture.as,
          installInvite: capture.installInvite ?? false,
          lastChancePopup: capture.lastChancePopup ?? false,
          releaseNotesPopup: capture.releaseNotesPopup ?? false,
        })

        test(`axe ${capture.name}`, async ({ page, club, isMobile }) => {
          test.skip(!isMobile && capture.desktop !== true, 'Phone screens are audited on the phone')
          await page.clock.setSystemTime(new Date(club.now))
          capture.prepare?.(club)
          await page.goto(capture.path)
          await expect(page.locator('h1').first()).toBeVisible()
          await capture.act?.(page, club)
          await page.evaluate(() => document.fonts.ready)

          const { violations } = await new AxeBuilder({ page }).withTags(TAGS).analyze()
          const report = violations.map((violation) => ({
            rule: violation.id,
            impact: violation.impact,
            help: violation.help,
            nodes: violation.nodes
              .slice(0, 5)
              .map((node) => ({ target: node.target.join(' '), html: node.html.slice(0, 160) })),
          }))
          expect(report, JSON.stringify(report, null, 2)).toEqual([])
        })
      })
    }
  })
}

test('a topo line opens from the keyboard, and the focus comes back to it', async ({ page }) => {
  await page.goto('/topo')
  const line = page.getByRole('link', { name: /Bye Bye Morice/ })
  await line.focus()
  await expect(line).toBeFocused()
  // The whole line is clickable, but the link holds the label only: its name is the label's.
  await expect(line).toHaveAccessibleName('6b, Bye Bye Morice, couleur jaune et bleu, corde 12')
  await page.keyboard.press('Enter')
  const sheet = page.getByRole('dialog', { name: /Bye Bye Morice/ })
  await expect(sheet).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(sheet).toBeHidden()
  await expect(line).toBeFocused()
})
