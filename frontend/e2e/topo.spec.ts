import type { Page } from '@playwright/test'

import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

/** The routes of the list, in their order on screen. */
const rows = (page: Page) => page.getByRole('main').getByRole('listitem').getByRole('link')
const sheet = (page: Page, name: RegExp) => page.getByRole('dialog', { name })

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

test.describe('the topo', () => {
  test('sorts and filters the routes, and keeps it in the link', async ({ page }) => {
    await page.goto('/topo')
    await expect(page.getByText('11 voies')).toBeVisible()
    // Grade by default, easiest first.
    await expect(rows(page).first()).toHaveAccessibleName(/P’tite Line, 4a/)

    await page.getByLabel('Trier par').selectOption({ label: 'Ouverture' })
    await expect(page).toHaveURL(/tri=ouverture/)
    await expect(rows(page).first()).toHaveAccessibleName(/OH purée/)

    await page.getByRole('button', { name: 'Filtres' }).click()
    const filters = page.getByRole('dialog', { name: 'Filtrer le topo' })
    await filters.getByRole('button', { name: 'Négatif et ses 2 dièdres' }).click()
    await expect(filters.getByRole('button', { name: 'Négatif et ses 2 dièdres' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await filters.getByRole('button', { name: 'Voir la voie' }).click()
    await expect(filters).toBeHidden()
    await expect(page.getByText('1 sur 11 voies')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Filtres, 1 actif' })).toBeVisible()
    await expect(rows(page)).toHaveCount(1)

    // The sheet opens over the list and closing it finds the list as it was.
    await rows(page).first().click()
    await expect(page).toHaveURL(/\/topo\/voies\/.+tri=ouverture.*secteur=/)
    await sheet(page, /Bye Bye Morice/)
      .getByRole('button', { name: 'Fermer la fiche' })
      .click()
    await expect(page).toHaveURL(/\/topo\?tri=ouverture&secteur=/)
    await expect(rows(page)).toHaveCount(1)

    // A shared link shows the same list.
    await page.reload()
    await expect(page.getByText('1 sur 11 voies')).toBeVisible()
    await expect(page.getByLabel('Trier par')).toHaveValue('opened')
  })

  test('groups the routes by sector when sorted by sector', async ({ page }) => {
    await page.goto('/topo?tri=secteur')
    await expect(page.getByRole('heading', { level: 2 }).first()).toHaveText('Neutre 8 m')
    await expect(page.getByRole('heading', { level: 2, name: 'Grand dévers' })).toBeVisible()
  })

  test('opens a route sheet from a link, ticks "je sais la faire"', async ({ page, club }) => {
    const morice = club.topo.route('Bye Bye Morice')
    await page.goto(`/topo/voies/${morice.id}`)
    const routeSheet = sheet(page, /Corde 12, Bye Bye Morice, 6b, couleur jaune et bleu/)
    await expect(routeSheet).toBeVisible()
    await expect(routeSheet.getByText('Négatif et ses 2 dièdres · corde 12')).toBeVisible()
    await expect(routeSheet.getByText('Prises : jaune et bleu')).toBeVisible()
    await expect(routeSheet.getByText('Ouverte le 16 mai 2026 par Sam')).toBeVisible()
    await expect(routeSheet.getByText('Le conseil de l’ouvreur')).toBeVisible()
    await expect(routeSheet.getByText('« Garde du jus pour la sortie. »')).toBeVisible()
    await expect(routeSheet.getByText('5 coups de cœur')).toBeVisible()

    const lead = routeSheet.getByRole('button', { name: 'Je sais la faire en tête' })
    await expect(lead).toHaveAttribute('aria-pressed', 'false')
    await lead.click()
    await expect(lead).toHaveAttribute('aria-pressed', 'true')

    // Deep link: closing leads to the list, which shows the mark.
    await routeSheet.getByRole('button', { name: 'Fermer la fiche' }).click()
    await expect(page).toHaveURL(/\/topo$/)
    await expect(page.getByRole('link', { name: /Bye Bye Morice/ })).toHaveAccessibleDescription(
      /Je sais la faire en tête/,
    )
    await page.goto(`/topo/voies/${morice.id}`)
    await expect(
      sheet(page, /Bye Bye Morice/).getByRole('button', { name: 'Je sais la faire en tête' }),
    ).toHaveAttribute('aria-pressed', 'true')
  })

  test('a full heart asks which favourite to give up', async ({ page, club, me }) => {
    const marie = signedIn(me)
    club.topo.favorite(marie, 'Réglisse')
    club.topo.favorite(marie, 'Iron Man')
    await page.goto(`/topo/voies/${club.topo.route('Bye Bye Morice').id}`)
    const routeSheet = sheet(page, /Bye Bye Morice/)
    const heart = routeSheet.getByRole('button', { name: 'Coup de cœur' })
    await expect(heart).toHaveAttribute('aria-pressed', 'false')

    await heart.click()
    const full = page.getByRole('dialog', { name: 'Ton cœur est plein. Lequel tu lâches ?' })
    await expect(full).toBeVisible()
    await full.getByRole('button', { name: /^Lâcher Corde 27, Réglisse/ }).click()
    await expect(full).toBeHidden()
    await expect(heart).toHaveAttribute('aria-pressed', 'true')
    await expect(
      routeSheet.getByText('C’est fait : « Bye Bye Morice » a pris la place.'),
    ).toBeVisible()
    await expect(routeSheet.getByText('6 coups de cœur')).toBeVisible()
    expect(club.topo.favorites.get(marie.id)).toEqual([
      club.topo.route('Iron Man').id,
      club.topo.route('Bye Bye Morice').id,
    ])

    // Taking the heart back just removes it.
    await heart.click()
    await expect(heart).toHaveAttribute('aria-pressed', 'false')
    await expect(full).toBeHidden()
    await expect(routeSheet.getByText('5 coups de cœur')).toBeVisible()
  })

  test('comments on a route and deletes the comment', async ({ page, club }) => {
    await page.goto(`/topo/voies/${club.topo.route('Bye Bye Morice').id}`)
    const routeSheet = sheet(page, /Bye Bye Morice/)
    await expect(routeSheet.getByRole('heading', { name: 'Commentaires (1)' })).toBeVisible()
    // Not mine, and I am not on the board: no way to delete it.
    await expect(
      routeSheet.getByRole('button', { name: 'Supprimer le commentaire de tonton.talon' }),
    ).toHaveCount(0)

    await routeSheet.getByRole('button', { name: 'Publier' }).click()
    await expect(routeSheet.getByText('Écris quelque chose avant de publier.')).toBeVisible()
    await routeSheet.getByLabel('Ton commentaire').fill('Le pied gauche sur la réglette bleue.')
    await expect(routeSheet.getByText('37 sur 500 caractères')).toBeVisible()
    await routeSheet.getByRole('button', { name: 'Publier' }).click()
    await expect(routeSheet.getByText('Le pied gauche sur la réglette bleue.')).toBeVisible()
    await expect(routeSheet.getByRole('heading', { name: 'Commentaires (2)' })).toBeVisible()
    await expect(routeSheet.getByLabel('Ton commentaire')).toHaveValue('')

    await routeSheet.getByRole('button', { name: 'Supprimer le commentaire de marie' }).click()
    const confirm = page.getByRole('alertdialog', { name: 'Supprimer ce commentaire ?' })
    await confirm.getByRole('button', { name: 'Supprimer' }).click()
    await expect(routeSheet.getByText('Commentaire supprimé.')).toBeVisible()
    await expect(routeSheet.getByText('Le pied gauche sur la réglette bleue.')).toHaveCount(0)
    await expect(routeSheet.getByRole('heading', { name: 'Commentaires (1)' })).toBeVisible()
  })

  test('shows dismounted routes in the archives, read-only', async ({ page }) => {
    await page.goto('/topo')
    await page.getByRole('link', { name: 'Voies démontées (archives)' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Voies démontées' })).toBeVisible()
    await expect(rows(page)).toHaveCount(2)
    await expect(rows(page).first()).toHaveAccessibleName(/Bloque !.*démontée$/)
    await expect(rows(page).first()).toHaveAccessibleDescription(/Démontée le 5 octobre 2026/)

    await rows(page).first().click()
    await expect(page).toHaveURL(/\/topo\/archives\/voies\//)
    const routeSheet = sheet(page, /Bloque !/)
    await expect(routeSheet.getByText(/^Démontée le 5 octobre 2026\./)).toBeVisible()
    await expect(routeSheet.getByRole('button', { name: 'Coup de cœur' })).toHaveCount(0)
    await expect(routeSheet.getByRole('button', { name: /Je sais la faire/ })).toHaveCount(0)
    await expect(routeSheet.getByLabel('Ton commentaire')).toHaveCount(0)
    await page.keyboard.press('Escape')
    await expect(page).toHaveURL(/\/topo\/archives$/)
  })
})

test.describe('the board', () => {
  test.use({ signedInAs: 'BOARD' })

  test('may delete anyone’s comment', async ({ page, club }) => {
    await page.goto(`/topo/voies/${club.topo.route('Bye Bye Morice').id}`)
    await sheet(page, /Bye Bye Morice/)
      .getByRole('button', { name: 'Supprimer le commentaire de tonton.talon' })
      .click()
    await page.getByRole('alertdialog').getByRole('button', { name: 'Supprimer' }).click()
    await expect(page.getByText('Pas encore de commentaire.', { exact: false })).toBeVisible()
  })
})

test.describe('last chance', () => {
  test.use({ lastChancePopup: true })

  test('lists the routes about to go, once', async ({ page }) => {
    // Never over the session entry.
    await page.goto('/seance/nouvelle')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    await expect(page.getByRole('dialog')).toHaveCount(0)

    await page.goto('/actu')
    const popup = page.getByRole('dialog', { name: 'Dernière chance !' })
    await expect(popup).toBeVisible()
    await expect(popup.getByText('Tonton Talon', { exact: true })).toBeVisible()
    await expect(
      popup.getByText('Démontage prévu le 2 novembre 2026 · 2 coups de cœur'),
    ).toBeVisible()
    await expect(
      popup.getByText('Date de démontage pas encore fixée · Aucun coup de cœur'),
    ).toBeVisible()
    await popup.getByRole('button', { name: 'J’accepte leurs destins' }).click()
    await expect(popup).toBeHidden()

    await page.reload()
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
    await expect(page.getByRole('dialog')).toHaveCount(0)
  })
})

test.describe('the favourite route', () => {
  test('is picked during the first steps and shown on the profile', async ({ page, club, me }) => {
    await page.goto('/premiers-pas/voie')
    await expect(page.getByRole('heading', { name: 'Ta voie préférée ?' })).toBeVisible()
    await page.getByLabel('Chercher une voie').fill('reglisse')
    await page.getByRole('button', { name: /Réglisse/ }).click()
    await expect(page.getByRole('button', { name: /Réglisse/ })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await page.getByRole('button', { name: 'Choisir cette voie' }).click()
    await expect(page).toHaveURL(/\/premiers-pas\/installer$/)
    expect(signedIn(me).favoriteRouteId).toBe(club.topo.route('Réglisse').id)

    await page.goto('/profil')
    const favorite = page.getByRole('region', { name: 'Voie préférée' })
    await favorite.getByRole('link', { name: /Réglisse/ }).click()
    await expect(sheet(page, /Réglisse/)).toBeVisible()
  })

  test('is changed and cleared in the profile settings', async ({ page }) => {
    await page.goto('/profil/modifier')
    await expect(page.getByText('Pas encore de voie préférée.')).toBeVisible()
    await page.getByRole('button', { name: 'Choisir ma voie préférée' }).click()
    await page.getByLabel('Chercher une voie').fill('12')
    await page.getByRole('button', { name: /Bye Bye Morice/ }).click()
    await expect(page.getByText('Voie préférée enregistrée.')).toBeVisible()
    await expect(page.getByRole('link', { name: /Bye Bye Morice/ })).toBeVisible()

    await page.getByRole('button', { name: 'Retirer ma voie préférée' }).click()
    await expect(page.getByText('Voie préférée retirée.')).toBeVisible()
    await expect(page.getByText('Pas encore de voie préférée.')).toBeVisible()
  })

  test('the profile lists my favourites, dismounted ones as history', async ({
    page,
    club,
    me,
  }) => {
    club.topo.favorite(signedIn(me), 'Schtroumpfs')
    club.topo.favorite(signedIn(me), 'Réglisse')
    await page.goto('/profil')
    const favorites = page.getByRole('region', { name: 'Mes coups de cœur' })
    await expect(favorites.getByRole('link', { name: /Réglisse/ })).toBeVisible()
    await expect(favorites.getByText('Anciens coups de cœur, démontés depuis')).toBeVisible()
    await expect(favorites.getByRole('link', { name: /Schtroumpfs.*démontée$/ })).toBeVisible()
  })
})
