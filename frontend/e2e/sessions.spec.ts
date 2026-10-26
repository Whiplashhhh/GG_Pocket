import type { Page } from '@playwright/test'

import type { FakeClub, FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

/** The app reads "today" from the phone: the same day as the fake server. */
async function sameDay(page: Page, club: FakeClub) {
  await page.clock.setSystemTime(new Date(club.now))
}

const line = (page: Page, n: number) =>
  page.getByRole('listitem').filter({ has: page.getByLabel(`Actions sur la voie n° ${String(n)}`) })

async function addRoute(page: Page, search: string, name: string) {
  await page.getByRole('searchbox', { name: 'Ajouter une voie' }).fill(search)
  await page.getByRole('button', { name: `Ajouter ${name}` }).click()
}

async function choose(page: Page, n: number, group: string, option: string) {
  await line(page, n)
    .getByRole('radiogroup', { name: group })
    .getByRole('radio', { name: option })
    .click()
}

test.describe('a new session', () => {
  test('is logged in a few taps and shows up everywhere', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/actu')
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
    await page.getByRole('link', { name: 'Nouvelle séance' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Nouvelle séance' })).toBeVisible()
    await expect(page.getByLabel('Jour de la séance')).toHaveValue('2026-10-20')

    // Taping a cord number shows its routes.
    await addRoute(page, '12', 'Bye Bye Morice')
    await expect(page.getByRole('heading', { name: '1 voie' })).toBeVisible()
    // The picker shows it is in the session, and still lets me log another go.
    await expect(
      page.getByRole('button', {
        name: 'Ajouter Bye Bye Morice encore une fois (déjà 1 dans la séance)',
      }),
    ).toBeVisible()
    await expect(page.getByText('Dans la séance', { exact: true })).toBeVisible()
    // A tap too many is taken back right there.
    await page.getByRole('button', { name: 'Ajouter Bye Bye Morice encore une fois' }).click()
    await expect(page.getByRole('heading', { name: '2 voies' })).toBeVisible()
    await expect(page.getByText('Dans la séance, 2 fois')).toBeVisible()
    await page
      .getByRole('button', { name: 'Retirer un passage de Bye Bye Morice (il en restera 1)' })
      .click()
    await expect(page.getByRole('heading', { name: '1 voie' })).toBeVisible()
    await expect(line(page, 1).getByLabel('Premier essai de ma vie sur cette voie')).toBeChecked()
    await choose(page, 1, 'Mode', 'Tête')
    await choose(page, 1, 'Enchaînée', 'Oui')
    await expect(line(page, 1).getByRole('button', { name: 'Une chute de plus' })).toBeDisabled()

    await addRoute(page, 'reglisse', 'Réglisse')
    // The mode of the line before.
    await expect(
      line(page, 2).getByRole('radiogroup', { name: 'Mode' }).getByRole('radio', { name: 'Tête' }),
    ).toBeChecked()
    await choose(page, 2, 'Topée', 'Non')
    await expect(
      line(page, 2)
        .getByRole('radiogroup', { name: 'Enchaînée' })
        .getByRole('radio', { name: 'Oui' }),
    ).toBeDisabled()
    await line(page, 2).getByRole('button', { name: 'Une chute de plus' }).click()
    await line(page, 2).getByRole('button', { name: 'Une chute de plus' }).click()

    // A second try of the first route, right after it.
    await line(page, 1).getByLabel('Actions sur la voie n° 1').click()
    await page.getByRole('menuitem', { name: 'Dupliquer (nouvel essai)' }).click()
    await expect(page.getByRole('heading', { name: '3 voies' })).toBeVisible()
    await expect(
      line(page, 2).getByLabel('Premier essai de ma vie sur cette voie'),
    ).not.toBeChecked()

    await page.getByLabel('Commentaire').fill('Les avant-bras en béton.')
    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page.getByText('Séance au carnet !')).toBeVisible()
    await expect(page).toHaveURL(/\/actu$/)

    const card = page.getByRole('article', { name: 'Séance d’aujourd’hui' })
    await expect(card.getByText('3 voies')).toBeVisible()
    await expect(card.getByText('2 topées')).toBeVisible()
    await expect(card.getByText('3 en tête')).toBeVisible()
    await expect(card.getByText('pas topée, 2 chutes')).toBeVisible()
    await expect(card.getByText('« Les avant-bras en béton. »')).toBeVisible()

    // The route sheet counts it, and the clean ascent ticked "je sais la faire en tête".
    await page.goto(`/topo/voies/${club.topo.route('Bye Bye Morice').id}`)
    const sheet = page.getByRole('dialog', { name: /Bye Bye Morice/ })
    await expect(sheet.getByRole('heading', { name: 'Au compteur' })).toBeVisible()
    await expect(sheet.getByRole('definition').first()).toHaveText('2')
    await expect(sheet.getByRole('heading', { name: 'Mon historique' })).toBeVisible()
    await expect(sheet.getByRole('link', { name: /20 octobre 2026/ })).toHaveCount(2)
    await expect(sheet.getByRole('button', { name: 'Je sais la faire en tête' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
  })

  test('keeps what was typed when the sheet is closed', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/seance/nouvelle')
    await addRoute(page, '12', 'Bye Bye Morice')
    await choose(page, 1, 'Mode', 'Tête')
    // The draft is written a moment after the last change.
    await page.waitForTimeout(600)

    await page.getByRole('link', { name: 'Fermer (ta saisie reste au chaud)' }).click()
    await expect(page).toHaveURL(/\/actu$/)
    await page.goto('/seance/nouvelle')
    await expect(page.getByText('Brouillon retrouvé')).toBeVisible()
    await expect(
      line(page, 1).getByRole('radiogroup', { name: 'Mode' }).getByRole('radio', { name: 'Tête' }),
    ).toBeChecked()

    await page.getByRole('button', { name: 'Repartir de zéro' }).click()
    await expect(page.getByRole('heading', { name: 'Aucune voie pour l’instant' })).toBeVisible()
  })

  test('without network, waits and leaves by itself', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/seance/nouvelle')
    await addRoute(page, 'reglisse', 'Réglisse')

    club.climbs.offline = true
    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page.getByText('Pas de réseau ici')).toBeVisible()
    await expect(page).toHaveURL(/\/actu$/)
    await expect(page.getByText('1 séance en attente de réseau')).toBeVisible()

    club.climbs.offline = false
    await page.evaluate(() => window.dispatchEvent(new Event('online')))
    await expect(page.getByText('1 séance en attente de réseau')).toBeHidden()
    await expect(page.getByRole('article', { name: 'Séance d’aujourd’hui' })).toBeVisible()
    expect(club.climbs.sessions).toHaveLength(1)
  })

  test('refuses a session without any route', async ({ page, club }) => {
    await sameDay(page, club)
    await page.goto('/seance/nouvelle')
    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page.getByRole('alert')).toHaveText(/Ajoute au moins une voie/)
  })
})

test.describe('my sessions', () => {
  test('can be changed, then deleted', async ({ page, club, me }) => {
    await sameDay(page, club)
    club.climbs.add(signedIn(me), 1, [{ route: 'Réglisse' }], { comment: 'Petite séance.' })
    await page.goto('/actu')
    await page.getByRole('link', { name: 'Séance d’hier' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Ma séance' })).toBeVisible()

    await page.getByRole('link', { name: 'Modifier' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Modifier la séance' })).toBeVisible()
    await choose(page, 1, 'Topée', 'Non')
    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page).toHaveURL(/\/seances\/[^/]+$/)
    await expect(page.getByText(/pas topée/)).toBeVisible()

    await page.getByRole('button', { name: 'Supprimer' }).click()
    await page.getByRole('button', { name: 'Supprimer la séance' }).click()
    await expect(page).toHaveURL(/\/actu$/)
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
  })

  test('of a frozen month stay as they are', async ({ page, club, me }) => {
    await sameDay(page, club)
    const old = club.climbs.add(signedIn(me), 40, [{ route: 'Réglisse' }], { frozen: true })
    await page.goto(`/seances/${old.id}`)
    await expect(page.getByText('Ce mois est clos')).toBeVisible()
    await expect(page.getByRole('link', { name: 'Modifier' })).toHaveCount(0)
  })

  test('of someone else are nowhere to be found', async ({ page, club }) => {
    await sameDay(page, club)
    const other = club.addMember({ nickname: 'julie', role: 'CLIMBER' })
    const theirs = club.climbs.add(other, 1, [{ route: 'Réglisse' }])
    await page.goto(`/seances/${theirs.id}`)
    await expect(page.getByRole('heading', { name: 'Séance introuvable' })).toBeVisible()
  })
})
