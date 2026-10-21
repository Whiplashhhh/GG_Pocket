import type { Page } from '@playwright/test'

import { expect, test } from './fixtures'

/** Labels of the row actions on large screens; on phones, the same words in the actions sheet. */
const ACTIONS = {
  edit: ['Modifier', (name: string) => `Modifier ${name}`],
  duplicate: ['Dupliquer', (name: string) => `Dupliquer ${name}`],
  lastChance: ['Dernière chance', (name: string) => `Passer ${name} en dernière chance`],
  dismount: ['Démonter', (name: string) => `Démonter ${name}`],
  remount: ['Remonter', (name: string) => `Remonter ${name}`],
} as const

/** Runs a route action from the management list, the phone way or the desktop way. */
async function routeAction(
  page: Page,
  isMobile: boolean,
  name: string,
  action: keyof typeof ACTIONS,
): Promise<void> {
  const [word, label] = ACTIONS[action]
  if (isMobile) {
    await page
      .getByRole('main')
      .getByRole('button', { name: new RegExp(name) })
      .click()
    await page
      .getByRole('dialog', { name: `Actions pour ${name}` })
      .getByText(word, { exact: true })
      .click()
  } else {
    await page.getByLabel(label(name), { exact: true }).click()
  }
}

test.describe('a setter', () => {
  test.use({ signedInAs: 'SETTER' })

  test.beforeEach(async ({ page, club }) => {
    // The form opens routes today: the browser and the fake server agree on the day.
    await page.clock.setSystemTime(new Date(club.now))
    club.addMember({ nickname: 'sam', role: 'SETTER' })
    club.addMember({ nickname: 'jo', role: 'CLIMBER' })
  })

  test('opens a new route, edits it, and the topo shows it', async ({ page, isMobile }) => {
    await page.goto('/gestion')
    await page.getByRole('link', { name: 'Voies' }).first().click()
    await expect(page.getByRole('heading', { level: 1, name: 'Voies' })).toBeVisible()
    await expect(page.getByText('11 voies')).toBeVisible()
    await page.getByRole('link', { name: 'Nouvelle voie' }).click()

    await expect(page.getByRole('heading', { level: 1, name: 'Nouvelle voie' })).toBeVisible()
    await expect(page.getByLabel('Date d’ouverture')).toHaveValue('2026-10-20')
    await page.getByLabel('Nom de la voie').fill('Avale la corde')
    await page.getByLabel('Cotation').selectOption({ label: '5c' })
    await page.getByLabel('Corde', { exact: true }).selectOption({ label: 'Corde 4' })
    await page
      .getByRole('group', { name: 'Couleur des prises' })
      .getByText('vert', { exact: true })
      .click()
    await page
      .getByRole('group', { name: /Deuxième couleur/ })
      .getByText('jaune', { exact: true })
      .click()
    await page.getByRole('button', { name: 'Dalle', exact: true }).click()
    await page.getByLabel('Le conseil de l’ouvreur').fill('Les pieds, toujours les pieds.')
    // The label is drawn as it will be taped at the foot of the route.
    await expect(page.getByRole('region', { name: 'Aperçu de l’étiquette' })).toContainText(
      'Corde 4, Avale la corde, 5c, couleur vert et jaune',
    )

    // No opener yet: said before anything is sent.
    await page.getByRole('button', { name: 'Enregistrer la voie' }).click()
    await expect(
      page.getByText('Il faut au moins un ouvreur : coche un membre ou écris un nom.'),
    ).toBeVisible()
    const openers = page.getByRole('group', { name: 'Ouvreurs' })
    await expect(openers.getByRole('button', { name: 'jo' })).toHaveCount(0)
    await openers.getByRole('button', { name: 'sam' }).click()
    await page.getByRole('button', { name: 'Enregistrer la voie' }).click()

    await expect(page).toHaveURL(/\/gestion\/voies$/)
    await expect(page.getByText('« Avale la corde » est ouverte : elle est au topo.')).toBeVisible()
    await expect(page.getByText('12 voies')).toBeVisible()

    await routeAction(page, isMobile, 'Avale la corde', 'edit')
    await expect(
      page.getByRole('heading', { level: 1, name: 'Modifier « Avale la corde »' }),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: 'sam' })).toHaveAttribute('aria-pressed', 'true')
    await page.getByLabel('Nom de la voie').fill('Avale la corde !')
    await page.getByLabel('Cotation').selectOption({ label: '6a' })
    await page.getByRole('button', { name: 'Enregistrer la voie' }).click()
    await expect(page.getByText('« Avale la corde ! » est à jour.')).toBeVisible()

    await page.goto('/topo')
    await page.getByRole('link', { name: /Avale la corde !, 6a/ }).click()
    await expect(page.getByRole('dialog', { name: /Avale la corde !/ })).toContainText(
      'Les pieds, toujours les pieds.',
    )
  })

  test('asks before leaving a route half typed', async ({ page }) => {
    await page.goto('/gestion/voies/nouvelle')
    await page.getByLabel('Nom de la voie').fill('Pas finie')
    await page.getByRole('link', { name: 'Annuler' }).click()
    const leave = page.getByRole('alertdialog', { name: 'Quitter sans enregistrer ?' })
    await leave.getByRole('button', { name: 'Annuler' }).click()
    await expect(page).toHaveURL(/\/gestion\/voies\/nouvelle$/)
    await expect(page.getByLabel('Nom de la voie')).toHaveValue('Pas finie')

    await page.getByRole('link', { name: 'Annuler' }).click()
    await leave.getByRole('button', { name: 'Quitter sans enregistrer' }).click()
    await expect(page).toHaveURL(/\/gestion\/voies$/)
  })

  test('duplicates a route to reopen on the same cord', async ({ page, isMobile }) => {
    await page.goto('/gestion/voies')
    await routeAction(page, isMobile, 'Bye Bye Morice', 'duplicate')
    await expect(
      page.getByRole('heading', { level: 1, name: 'Dupliquer « Bye Bye Morice »' }),
    ).toBeVisible()
    // Everything is taken over but the date (today) and the photo.
    await expect(page.getByLabel('Nom de la voie')).toHaveValue('Bye Bye Morice')
    await expect(page.getByLabel('Date d’ouverture')).toHaveValue('2026-10-20')
    await expect(page.getByLabel('Corde', { exact: true })).toHaveValue(/.+/)
    await expect(page.getByLabel('Ouvreur sans compte')).toHaveValue('Sam')
    await expect(page.getByRole('button', { name: 'Conti', exact: true })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await page.getByLabel('Nom de la voie').fill('Bye Bye Morice, le retour')
    await page.getByRole('button', { name: 'Enregistrer la voie' }).click()
    await expect(
      page.getByText('« Bye Bye Morice, le retour » est ouverte : elle est au topo.'),
    ).toBeVisible()
    await page.getByLabel('Chercher une voie').fill('12')
    await expect(page.getByText('2 voies')).toBeVisible()
  })

  test('announces the last chance of a route, then cancels it', async ({ page, isMobile }) => {
    await page.goto('/gestion/voies')
    await routeAction(page, isMobile, 'Réglisse', 'lastChance')
    const dialog = page.getByRole('dialog', { name: 'Dernière chance pour « Réglisse »' })
    await dialog.getByLabel('Date prévue de démontage').fill('2026-11-15')
    await dialog.getByRole('button', { name: 'Annoncer la dernière chance' }).click()
    await expect(
      page.getByText('« Réglisse » est en dernière chance : les grimpeurs vont être prévenus.'),
    ).toBeVisible()

    await page.goto('/gestion/derniere-chance')
    await expect(page.getByRole('heading', { level: 1, name: 'Dernière chance' })).toBeVisible()
    await expect(page.getByText('12 grimpeurs ont accepté son destin')).toBeVisible()
    // Avant-bras en béton and Réglisse.
    await expect(page.getByText('Personne n’a encore accepté son destin')).toHaveCount(2)
    await expect(page.getByText('Démontage prévu le 15 novembre 2026')).toBeVisible()

    await page.getByRole('button', { name: 'Annuler la dernière chance de Réglisse' }).click()
    await expect(
      page.getByText('Dernière chance annulée : « Réglisse » reste au mur.'),
    ).toBeVisible()
    await expect(page.getByText('Démontage prévu le 15 novembre 2026')).toHaveCount(0)
  })

  test('dismounts a loved route after a warning, then remounts it', async ({
    page,
    club,
    isMobile,
  }) => {
    await page.goto('/gestion/voies')
    await routeAction(page, isMobile, 'Bye Bye Morice', 'dismount')
    const confirm = page.getByRole('alertdialog', { name: 'Démonter « Bye Bye Morice » ?' })
    await expect(confirm).toContainText('5 grimpeurs l’ont en coup de cœur. Tu es sûr·e ?')
    await confirm.getByRole('button', { name: 'Démonter la voie' }).click()
    await expect(
      page.getByText('« Bye Bye Morice » est démontée. Elle a bien servi.'),
    ).toBeVisible()
    await expect(page.getByText('10 voies')).toBeVisible()

    await page.getByRole('radio', { name: 'Archives' }).click()
    await expect(page).toHaveURL(/vue=archives/)
    await expect(page.getByText('3 voies')).toBeVisible()
    await routeAction(page, isMobile, 'Bye Bye Morice', 'remount')
    await page
      .getByRole('alertdialog', { name: 'Remonter « Bye Bye Morice » ?' })
      .getByRole('button', { name: 'Remonter la voie' })
      .click()
    await expect(page.getByText('« Bye Bye Morice » est de retour au mur.')).toBeVisible()
    await expect(page.getByText('2 voies')).toBeVisible()

    expect(club.audit.map((entry) => entry.action)).toEqual(['ROUTE_REMOUNTED', 'ROUTE_DISMOUNTED'])
  })

  test('has no way into the club configuration', async ({ page }) => {
    await page.goto('/gestion')
    await expect(page.getByRole('link', { name: 'Dernière chance' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Club', exact: true })).toHaveCount(0)
    await page.goto('/gestion/club')
    await expect(page).toHaveURL(/\/actu$/)
  })
})

test.describe('a climber', () => {
  test('cannot reach the routes management', async ({ page }) => {
    await page.goto('/gestion/voies')
    await expect(page).toHaveURL(/\/actu$/)
    await page.goto('/gestion/voies/nouvelle')
    await expect(page).toHaveURL(/\/actu$/)
  })
})

test.describe('the admin', () => {
  test.use({ signedInAs: 'ADMIN' })

  test('sets up sectors, cords and hold colours', async ({ page }) => {
    await page.goto('/gestion/club')
    await expect(
      page.getByRole('heading', { level: 1, name: 'Configuration du club' }),
    ).toBeVisible()
    await expect(page.getByText('Rien ne se supprime', { exact: false })).toBeVisible()

    // Rename a sector.
    await page.getByRole('button', { name: 'Modifier le secteur Positif' }).click()
    const sector = page.getByRole('dialog', { name: 'Modifier le secteur Positif' })
    await sector.getByLabel('Nom du secteur').fill('Le positif')
    await sector.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByText('Secteur « Le positif » mis à jour.')).toBeVisible()

    // A second sector with the same name is refused, on the name.
    await page.getByRole('button', { name: 'Ajouter un secteur' }).click()
    const added = page.getByRole('dialog', { name: 'Nouveau secteur' })
    await added.getByLabel('Nom du secteur').fill('neutre 8 m')
    await added.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(added.getByLabel('Nom du secteur')).toHaveAttribute('aria-invalid', 'true')
    await expect(added.getByText('Ça existe déjà', { exact: false })).toBeVisible()
    await added.getByRole('button', { name: 'Annuler' }).click()

    // A new cord, numbered after the last one.
    await page.getByRole('button', { name: 'Ajouter une corde au secteur Petit toit' }).click()
    const cord = page.getByRole('dialog', { name: 'Nouvelle corde' })
    await expect(cord.getByLabel('Numéro de corde')).toHaveValue('30')
    await cord.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByText('Corde 30 ajoutée.')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Modifier la corde 30' })).toBeVisible()

    // A new hold colour; a second « Bleu » is refused.
    await page.getByRole('button', { name: 'Ajouter une couleur' }).click()
    const color = page.getByRole('dialog', { name: 'Nouvelle couleur de prises' })
    await color.getByLabel('Nom de la couleur').fill('turquoise')
    await color.getByLabel('Code couleur').fill('#30d5c8')
    await color.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByText('Couleur « turquoise » ajoutée.')).toBeVisible()
    await expect(page.getByText('#30D5C8')).toBeVisible()
    await page.getByRole('button', { name: 'Ajouter une couleur' }).click()
    await color.getByLabel('Nom de la couleur').fill('Bleu')
    await color.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(color.getByLabel('Nom de la couleur')).toHaveAttribute('aria-invalid', 'true')
    await color.getByRole('button', { name: 'Annuler' }).click()

    // Sectors follow the wall from left to right.
    await page.getByRole('button', { name: 'Descendre Neutre 8 m' }).click()
    await expect(page.getByText('« Neutre 8 m » est maintenant en position 2.')).toBeVisible()
    await expect(page.getByRole('heading', { level: 3 }).first()).toHaveText('Neutre 12 m')

    await page.goto('/gestion/journal')
    await expect(page.getByText('marie a modifié le secteur « Le positif »')).toBeVisible()
    await expect(page.getByText('marie a créé la corde 30')).toBeVisible()
    await expect(page.getByText('marie a créé la couleur de prises « turquoise »')).toBeVisible()
  })
})
