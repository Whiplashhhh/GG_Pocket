import { readFileSync } from 'node:fs'

import { expect, test } from './fixtures'

const APP_VERSION = readFileSync(new URL('../../VERSION', import.meta.url), 'utf8').trim()

const FAILURE_TITLE = 'Cet écran est resté au pied de la voie'

test.describe('app shell', () => {
  test('opens on Actu with the five tabs at hand', async ({ page }) => {
    await page.goto('/')

    await expect(page).toHaveURL(/\/actu$/)
    await expect(page).toHaveTitle('Actu · GG Pocket')
    const nav = page.getByRole('navigation', { name: 'Navigation principale' })
    await expect(nav.getByRole('link')).toHaveText([
      'Actu',
      'Topo',
      'À faire',
      'Planning',
      'Podium',
    ])
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
  })

  test('each tab shows its own screen', async ({ page }) => {
    await page.goto('/actu')
    const nav = page.getByRole('navigation', { name: 'Navigation principale' })

    for (const [tab, heading] of [
      ['Topo', 'Topo'],
      ['À faire', 'À faire'],
      ['Planning', 'Planning'],
      ['Podium', 'Podium'],
    ] as const) {
      await nav.getByRole('link', { name: tab }).click()
      await expect(page.getByRole('heading', { level: 1, name: heading })).toBeVisible()
      await expect(nav.getByRole('link', { name: tab })).toHaveAttribute('aria-current', 'page')
    }
  })

  test('"Nouvelle séance" is offered on Actu and Planning only', async ({ page }) => {
    const fab = page.getByRole('link', { name: 'Nouvelle séance' })

    await page.goto('/actu')
    await expect(fab).toBeVisible()
    await page.goto('/planning')
    await expect(fab).toBeVisible()
    await page.goto('/topo')
    await expect(fab).toBeHidden()

    await page.goto('/actu')
    await fab.click()
    await expect(page).toHaveURL(/\/seance\/nouvelle$/)
    await expect(page.getByRole('heading', { name: 'Voie en cours d’ouverture' })).toBeVisible()
  })

  test('the avatar opens the profile, where the theme can be forced', async ({ page }) => {
    await page.goto('/actu')
    await page.getByRole('link', { name: 'Mon profil' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Mon profil' })).toBeVisible()

    await page.getByRole('radio', { name: 'Sombre' }).click()
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')

    // The choice survives a reload, applied before the app even starts.
    await page.reload()
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark')

    await page.getByRole('radio', { name: 'Auto' }).click()
    await expect(page.locator('html')).not.toHaveAttribute('data-theme')
  })

  test('the club logo in the header leads back to the feed', async ({ page }) => {
    await page.goto('/profil')
    await page.getByRole('link', { name: 'GG Pocket, retour à l’actu' }).click()
    await expect(page).toHaveURL(/\/actu$/)
  })

  test('an app left open across a release still opens the profile', async ({
    page,
    browserName,
  }) => {
    // Installed app on iOS: the profile screen's file of the previous version is gone (404).
    await page.route(
      '**/assets/ProfileView-*.js',
      (route) => route.fulfill({ status: 404, headers: { 'cache-control': 'no-store' } }),
      { times: 1 },
    )
    await page.goto('/actu')
    await page.evaluate(() => Object.assign(window, { e2eBeforeReload: true }))
    await page.getByRole('link', { name: 'Mon profil' }).click()
    await expect(page).toHaveURL(/\/profil$/)
    // A fresh page, not an in-app navigation: the new version is fetched.
    await expect.poll(() => page.evaluate(() => 'e2eBeforeReload' in window)).toBe(false)

    // WebKit remembers a failed module for the whole tab and does not ask again after the reload:
    // it ends on the failure screen. A real release points to a file with a new name.
    const heading = browserName === 'webkit' ? FAILURE_TITLE : 'Mon profil'
    await expect(page.getByRole('heading', { level: 1, name: heading })).toBeVisible()
  })

  test('a screen that will not load says so instead of a blank page', async ({
    page,
    browserName,
  }) => {
    await page.route('**/assets/ProfileView-*.js', (route) =>
      route.fulfill({ status: 404, headers: { 'cache-control': 'no-store' } }),
    )
    await page.goto('/actu')
    await page.getByRole('link', { name: 'Mon profil' }).click()
    await expect(page.getByRole('heading', { level: 1, name: FAILURE_TITLE })).toBeVisible()

    await page.getByText('Détails pour la direction').click()
    await expect(page.getByText('page /profil')).toBeVisible()
    await expect(page.getByText(/ProfileView-.+\.js → 404/)).toBeVisible()

    // Back to normal: emptying the cache reloads the screen, which loads this time. Not on WebKit,
    // which keeps the failed module for the whole tab (same file name in this test).
    if (browserName === 'webkit') return
    await page.unrouteAll()
    await page.getByRole('button', { name: 'Vider le cache et réessayer' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Mon profil' })).toBeVisible()
  })

  test('shows the app version in the profile, the server version to the board only', async ({
    page,
  }) => {
    await page.goto('/profil')
    // The version of the build (VERSION file), whatever the release; the fake server says 0.0.0.
    await expect(page.getByText(`GG Pocket ${APP_VERSION}`)).toBeVisible()
    await expect(page.getByText('Serveur 0.0.0')).toHaveCount(0)
  })

  test('an unknown address gets a friendly 404', async ({ page }) => {
    await page.goto('/voie-qui-n-existe-pas')
    await expect(page.getByRole('heading', { level: 1, name: 'Voie introuvable.' })).toBeVisible()
    await page.getByRole('link', { name: 'Retour à l’actu', exact: true }).click()
    await expect(page).toHaveURL(/\/actu$/)
  })

  test('keyboard users can skip straight to the content', async ({ page, isMobile }) => {
    test.skip(isMobile, 'Keyboard navigation is a desktop concern')
    await page.goto('/actu')
    await expect(page.getByRole('navigation', { name: 'Navigation principale' })).toBeVisible()
    await page.keyboard.press('Tab')
    const skipLink = page.getByRole('link', { name: 'Aller au contenu' })
    await expect(skipLink).toBeFocused()
    await skipLink.press('Enter')
    await expect(page.locator('#contenu')).toBeFocused()
  })
})

test.describe('dev environment', () => {
  test.use({ meta: { environment: 'dev', branch: 'feature/topo-0.3.0', commit: 'abc1234def' } })

  test('shows a permanent banner with branch and commit', async ({ page }) => {
    await page.goto('/actu')
    await expect(page.getByRole('note')).toHaveText(
      'Environnement de dev — branche feature/topo-0.3.0 @ abc1234',
    )
  })
})

test.describe('production environment', () => {
  test.use({ meta: { environment: 'prod' } })

  test('has no dev banner', async ({ page }) => {
    await page.goto('/actu')
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
    await expect(page.getByRole('note')).toHaveCount(0)
  })
})
