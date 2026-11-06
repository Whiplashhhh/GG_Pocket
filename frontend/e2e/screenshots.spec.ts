import { expect, test } from './fixtures'
import { CAPTURE_META, CAPTURES } from './scenarios'

test.use({ meta: CAPTURE_META })

for (const capture of CAPTURES) {
  test.describe(capture.name, () => {
    test.use({
      signedInAs: capture.as,
      installInvite: capture.installInvite ?? false,
      lastChancePopup: capture.lastChancePopup ?? false,
      releaseNotesPopup: capture.releaseNotesPopup ?? false,
    })

    test(`capture ${capture.name}`, async ({ page, club, isMobile }, testInfo) => {
      test.skip(!isMobile && capture.desktop !== true, 'Phone screens are reviewed at 360 px')
      // The phone and the fake server agree on the day (the session sheet reads it).
      await page.clock.setSystemTime(new Date(club.now))
      capture.prepare?.(club)
      await page.goto(capture.path)
      await expect(page.locator('h1').first()).toBeVisible()
      await capture.act?.(page, club)
      await page.evaluate(() => document.fonts.ready)
      await page.screenshot({
        path: `screenshots/${testInfo.project.name}/${capture.name}.png`,
        fullPage: true,
        // Finished transitions only: a chip caught mid-fade reads grey on grey.
        animations: 'disabled',
      })
    })
  })
}
