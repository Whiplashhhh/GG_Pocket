import { readFileSync } from 'node:fs'

import { expect, test, type Page } from '@playwright/test'

const VERSION = readFileSync(new URL('../../VERSION', import.meta.url), 'utf8').trim()
const SEED_PASSWORD = process.env.GG_SEED_PASSWORD ?? ''

/** Every message the browser prints when the Content Security Policy blocks something. */
function cspViolations(page: Page): string[] {
  const violations: string[] = []
  page.on('console', (message) => {
    if (/Content Security Policy/i.test(message.text())) violations.push(message.text())
  })
  return violations
}

test('the page comes with its security headers (spec §14)', async ({ request }) => {
  const response = await request.get('/')
  expect(response.ok()).toBe(true)
  const headers = response.headers()
  expect(headers['content-security-policy']).toContain("script-src 'self'")
  expect(headers['content-security-policy']).not.toMatch(/script-src[^;]*unsafe-inline/)
  expect(headers['content-security-policy']).toContain("frame-ancestors 'none'")
  expect(headers['strict-transport-security']).toContain('max-age=')
  expect(headers['x-content-type-options']).toBe('nosniff')
  expect(headers['referrer-policy']).toBe('same-origin')
  expect(headers['permissions-policy']).toContain('geolocation=()')
  expect(headers['x-robots-tag']).toBe('noindex, nofollow')
  expect(headers['cache-control']).toBe('no-cache')
  expect(headers.server).toBeUndefined()
})

test('the API answers through the web container with the version of the build', async ({
  request,
}) => {
  const response = await request.get('/api/v1/meta')
  expect(response.ok()).toBe(true)
  const meta = (await response.json()) as { version: string; environment: string }
  expect(meta.version).toBe(VERSION)
  expect(meta.environment).toBe('dev')
})

test('the internals of the API stay inside the stack', async ({ request }) => {
  for (const path of ['/actuator/health', '/v3/api-docs']) {
    const response = await request.get(path)
    // The page of the app answers, not the API.
    expect(response.headers()['content-type'], path).toContain('text/html')
  }
})

test('a demo climber signs in, browses and signs out without any CSP violation', async ({
  page,
}) => {
  expect(SEED_PASSWORD, 'GG_SEED_PASSWORD must be set').not.toBe('')
  // The invitation to install opens by itself on a first visit (modal): already declined here.
  await page.addInitScript(() => {
    localStorage.setItem(
      'gg-pocket:install-invite',
      JSON.stringify({ dismissals: 3, lastDismissedAt: Date.now() }),
    )
  })
  const violations = cspViolations(page)
  const calls: string[] = []
  page.on('response', (response) => {
    if (response.url().includes('/api/')) {
      calls.push(
        `${response.request().method()} ${new URL(response.url()).pathname} ${String(response.status())}`,
      )
    }
  })

  await page.goto('/connexion')
  await page.getByLabel('Pseudo').fill('grimpeur')
  await page.getByLabel('Mot de passe', { exact: true }).fill(SEED_PASSWORD)
  const [login] = await Promise.all([
    page.waitForResponse('**/api/v1/auth/login'),
    page.getByRole('button', { name: 'Se connecter' }).click(),
  ])
  expect(login.status(), await login.text()).toBe(200)
  expect((await login.allHeaders())['set-cookie'], 'session cookie').toContain('__Host-GG_SESSION=')
  const navigation = page.getByRole('navigation', { name: 'Navigation principale' })
  await page.waitForLoadState('networkidle')
  const where = [
    page.url(),
    `h1: ${(await page.locator('h1').allTextContents()).join(' / ')}`,
    `cookies: ${(await page.context().cookies()).map((c) => `${c.name} (${c.path})`).join(', ')}`,
    `alerts: ${(await page.getByRole('alert').allTextContents()).join(' / ')}`,
    `calls: ${calls.join(', ')}`,
  ].join('\n')
  await expect(navigation, where).toBeVisible()

  const cookies = await page.context().cookies()
  const session = cookies.find((cookie) => cookie.name === '__Host-GG_SESSION')
  expect(session?.httpOnly).toBe(true)
  expect(session?.secure).toBe(true)

  await page.goto('/profil')
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  await page.goto('/profil/appareils')
  await expect(page.getByRole('heading', { level: 1, name: 'Mes appareils' })).toBeVisible()

  expect(violations).toEqual([])
})

test('static assets are cached for good, the service worker never', async ({ page, request }) => {
  await page.goto('/accueil')
  const script = await page.locator('script[type="module"]').first().getAttribute('src')
  expect(script).toMatch(/^\/assets\//)
  const asset = await request.get(script ?? '')
  expect(asset.headers()['cache-control']).toContain('immutable')
  // gzip even when the browser offers zstd, which Safari 27 failed to decode (DECISIONS D-073).
  const compressed = await request.get(script ?? '', {
    headers: { 'accept-encoding': 'gzip, deflate, br, zstd' },
  })
  expect(compressed.headers()['content-encoding']).toBe('gzip')
  // A file of a previous version, asked for by an app left open: never cached.
  const gone = await request.get('/assets/ProfileView-oldhash.js')
  expect(gone.status()).toBe(404)
  expect(gone.headers()['cache-control']).toBe('no-store')
  const worker = await request.get('/sw.js')
  expect(worker.ok()).toBe(true)
  expect(worker.headers()['cache-control']).toBe('no-cache')
})
