import { expect, test } from './fixtures'

/** Pages captured for visual review (the gallery only exists on the dev server). */
const PAGES = [
  { name: 'actu', path: '/actu' },
  { name: 'topo', path: '/topo' },
  { name: 'profil', path: '/profil' },
  { name: 'nouvelle-seance', path: '/seance/nouvelle' },
  { name: '404', path: '/nulle-part' },
  { name: 'galerie', path: '/dev/galerie' },
] as const

test.use({ meta: { environment: 'dev', branch: 'feature/fondations-0.0.0', commit: 'e0d3317abc' } })

for (const target of PAGES) {
  test(`capture ${target.name}`, async ({ page }, testInfo) => {
    await page.goto(target.path)
    await expect(page.locator('main h1, main h2').first()).toBeVisible()
    await page.evaluate(() => document.fonts.ready)
    await page.screenshot({
      path: `screenshots/${testInfo.project.name}/${target.name}.png`,
      fullPage: true,
    })
  })
}
