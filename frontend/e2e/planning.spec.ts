import type { Page } from '@playwright/test'

import type { FakeClub, FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

/** The app reads "today" from the phone: the same day as the fake server (Tuesday 20 October). */
async function sameDay(page: Page, club: FakeClub) {
  await page.clock.setSystemTime(new Date(club.now))
}

const todoRow = (page: Page, name: string) =>
  page.getByRole('listitem').filter({ has: page.getByRole('link', { name: new RegExp(name) }) })

test.describe('à faire', () => {
  test('ticks a route away, with a few seconds to take it back', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/a-faire')
    await expect(page.getByRole('heading', { level: 1, name: 'À faire' })).toBeVisible()
    await expect(page.getByText(/voies en mouli\./)).toBeVisible()
    await expect(page.getByText(/Il en reste \d+, courage\./)).toBeVisible()

    await page.getByRole('button', { name: 'Je sais faire Réglisse en mouli' }).click()
    await expect(todoRow(page, 'Réglisse')).toHaveCount(0)
    await expect(page.getByRole('status').getByText('Réglisse : rayée de la liste.')).toBeVisible()
    await page.getByRole('button', { name: 'Annuler' }).click()
    await expect(page.getByText('Réglisse est de retour dans la liste.')).toBeVisible()
    await expect(todoRow(page, 'Réglisse')).toHaveCount(1)
  })

  test('knows that lead means top rope, and what a clean ascent ticked', async ({
    page,
    club,
    me,
  }) => {
    club.climbs.add(signedIn(me), 1, [{ route: 'Bye Bye Morice', mode: 'TOP_ROPE', clean: true }])
    await sameDay(page, club)
    await page.goto('/a-faire?mode=tete')
    await expect(page.getByRole('radio', { name: 'En tête' })).toBeChecked()
    await page.getByRole('button', { name: 'Je sais faire Iron Man en tête' }).click()
    await expect(todoRow(page, 'Iron Man')).toHaveCount(0)

    await page.getByRole('radio', { name: 'En mouli' }).click()
    await expect(page).toHaveURL(/\/a-faire$/)
    await expect(todoRow(page, 'Iron Man')).toHaveCount(0)
    await expect(todoRow(page, 'Bye Bye Morice')).toHaveCount(0)

    await page.getByRole('button', { name: /Voir celles que je sais faire/ }).click()
    await expect(
      page.getByRole('heading', { name: 'Celles que je sais faire en mouli' }),
    ).toBeVisible()
    await expect(todoRow(page, 'Iron Man')).toContainText('Sue en tête, donc en mouli aussi')
    await expect(todoRow(page, 'Bye Bye Morice')).toContainText('Enchaînée en séance')
    await expect(page.getByRole('button', { name: /^Décocher/ })).toHaveCount(0)
  })

  test('unticks a route ticked by hand', async ({ page, club, me }) => {
    club.topo.check(signedIn(me), 'Réglisse', 'TOP_ROPE')
    await sameDay(page, club)
    await page.goto('/a-faire')
    await expect(todoRow(page, 'Réglisse')).toHaveCount(0)
    await page.getByRole('button', { name: /Voir celles que je sais faire/ }).click()
    await page.getByRole('button', { name: 'Décocher Réglisse en mouli' }).click()
    await page.getByRole('button', { name: 'Revenir aux voies à faire' }).click()
    await expect(todoRow(page, 'Réglisse')).toHaveCount(1)
  })

  test('filters by sector and grade', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/a-faire')
    await page.getByLabel('Cotation').selectOption('Les 4a')
    await expect(page).toHaveURL(/cotation=4a/)
    await expect(todoRow(page, 'P’tite Line')).toHaveCount(1)
    await expect(todoRow(page, 'Réglisse')).toHaveCount(0)
  })
})

test.describe('planning', () => {
  test('shows the month, its days, its summary and my streak', async ({ page, club, me }) => {
    const climber = signedIn(me)
    club.climbs.add(climber, 1, [{ route: 'Réglisse' }, { route: 'Iron Man', mode: 'LEAD' }], {
      feeling: 7,
    })
    club.climbs.add(climber, 2, [{ route: 'Bye Bye Morice', topped: false }], { feeling: 8 })
    club.climbs.add(climber, 8, [{ route: 'Réglisse' }])
    club.climbs.add(climber, 35, [{ route: 'Réglisse' }])
    await sameDay(page, club)
    await page.goto('/planning')

    await expect(page.getByRole('heading', { name: 'octobre 2026' })).toBeVisible()
    await expect(page.getByText('3 présences')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Mois suivant' })).toBeDisabled()
    const streak = page.getByRole('region', { name: 'Série en cours' })
    await expect(streak.getByText('1', { exact: true })).toBeVisible()
    await expect(streak.getByText('semaine d’affilée à 2 séances ou plus')).toBeVisible()
    await expect(page.getByText('Cette semaine : 1 séance sur 2, encore une !')).toBeVisible()

    await page.getByRole('button', { name: '19 octobre 2026, grimpé' }).click()
    await expect(page.getByRole('heading', { name: 'Le 19 octobre 2026' })).toBeVisible()
    await page.getByRole('link', { name: /^Voir la séance du 19 octobre 2026/ }).click()
    await expect(page).toHaveURL(/\/seances\//)
    await page.goBack()

    await page.getByRole('button', { name: 'Mois précédent' }).click()
    await expect(page).toHaveURL(/mois=2026-09/)
    await expect(page.getByRole('heading', { name: 'septembre 2026' })).toBeVisible()
    await expect(page.getByText('1 présence')).toBeVisible()
  })

  test('logs a session on a day of the past week', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/planning')
    await page.getByRole('button', { name: '16 octobre 2026' }).click()
    await expect(page.getByText('Rien ce jour-là. Repos mérité ?')).toBeVisible()
    await page.getByRole('link', { name: 'Noter une séance ce jour-là' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Nouvelle séance' })).toBeVisible()
    await expect(page.getByLabel('Jour de la séance')).toHaveValue('2026-10-16')
  })
})

test.describe('logbook', () => {
  test('adds up everything I climbed, from the profile', async ({ page, club, me }) => {
    const climber = signedIn(me)
    club.climbs.add(climber, 3, [
      { route: 'Iron Man', mode: 'LEAD', clean: true },
      { route: 'Réglisse' },
      { route: 'Réglisse', mode: 'LEAD', topped: false },
    ])
    await sameDay(page, club)
    await page.goto('/profil')
    await page.getByRole('link', { name: /Mon carnet/ }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Mon carnet' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Ma pyramide' })).toBeVisible()
    const routes = page.getByRole('region', { name: /Toutes mes voies/ })
    await expect(routes.getByRole('listitem')).toHaveCount(2)
    await expect(routes.getByRole('listitem').filter({ hasText: 'Iron Man' })).toContainText(
      'Enchaînée en tête',
    )
    await expect(routes.getByRole('listitem').filter({ hasText: 'Réglisse' })).toContainText(
      'Topée en mouli',
    )
  })

  test('starts empty', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/carnet')
    await expect(page.getByRole('heading', { name: 'Ton carnet est encore vierge.' })).toBeVisible()
  })
})
