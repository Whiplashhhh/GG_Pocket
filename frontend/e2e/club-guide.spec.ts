import { expect, test } from './fixtures'

/** The club guide inside the app (D-110): docs/GUIDE_CLUB.md, rendered at build time. */
test.describe('a setter', () => {
  test.use({ signedInAs: 'SETTER' })

  test('reads the club guide from the management space', async ({ page, isMobile }) => {
    await page.goto('/gestion')
    const link = isMobile
      ? page.getByRole('link', { name: 'Guide du club' })
      : page
          .getByRole('navigation', { name: 'Sections de la gestion' })
          .getByRole('link', { name: 'Guide du club' })
    await link.click()
    await expect(page).toHaveURL(/\/gestion\/guide$/)
    await expect(page.getByRole('heading', { level: 1, name: 'Guide du club' })).toBeVisible()

    const contents = page.getByRole('navigation', { name: 'Sommaire' })
    await contents.getByRole('link', { name: '4. Les voies (ouvreurs)' }).click()
    const heading = page.getByRole('heading', { level: 2, name: '4. Les voies (ouvreurs)' })
    await expect(heading).toBeFocused()
    await expect(heading).toBeInViewport()
    // The route stays the same: a jump inside the page, not a navigation.
    await expect(page).toHaveURL(/\/gestion\/guide$/)
    await expect(page.getByRole('table')).toContainText('Ouvreur·se')
  })
})

test.describe('a climber', () => {
  test('has no access to the guide', async ({ page }) => {
    await page.goto('/gestion/guide')
    await expect(page).toHaveURL(/\/actu$/)
  })
})
