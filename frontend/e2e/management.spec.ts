import { expect, test } from './fixtures'

test.describe('a climber', () => {
  test('has no way into the management space', async ({ page }) => {
    await page.goto('/actu')
    await expect(page.getByRole('link', { name: 'Espace gestion' })).toHaveCount(0)
    await page.goto('/gestion/membres')
    await expect(page).toHaveURL(/\/actu$/)
  })
})

test.describe('the board', () => {
  test.use({ signedInAs: 'BOARD' })

  test('finds a member, makes them a setter, creates a reset link, disables them', async ({
    page,
    club,
    isMobile,
  }) => {
    club.addMember({ nickname: 'tonton.talon', firstName: 'Tonton', lastName: 'Talon' })
    club.addMember({ nickname: 'jo', firstName: 'Jo', lastName: 'Bleau' })

    await page.goto('/actu')
    await page.getByRole('link', { name: 'Espace gestion' }).click()
    if (isMobile) {
      await page.getByRole('link', { name: 'Membres' }).click()
    } else {
      await page
        .getByRole('navigation', { name: 'Sections de la gestion' })
        .getByRole('link', { name: 'Membres' })
        .click()
    }
    await expect(page.getByRole('heading', { level: 1, name: 'Membres' })).toBeVisible()
    await expect(page.getByText('3 membres')).toBeVisible()
    await page.getByLabel('Chercher un membre').fill('talon')
    await expect(page.getByText('1 membre')).toBeVisible()
    await page.getByRole('link', { name: /Talon/ }).first().click()

    await expect(page.getByRole('heading', { level: 1, name: 'Tonton Talon' })).toBeVisible()
    // The board only hands out the setter role.
    await expect(page.getByLabel('Choisir le rôle').locator('option')).toHaveText([
      'Grimpeur·se',
      'Ouvreur·se',
    ])
    await page.getByLabel('Choisir le rôle').selectOption({ label: 'Ouvreur·se' })
    await page.getByRole('button', { name: 'Changer le rôle' }).click()
    await expect(page.getByText('Rôle mis à jour.')).toBeVisible()

    await page.getByRole('button', { name: 'Créer un lien de réinitialisation' }).click()
    await expect(page.getByLabel('Lien de réinitialisation')).toHaveValue(
      /\/nouveau-mot-de-passe#jeton-/,
    )

    // Deleting an account for good is the admin's call.
    await expect(
      page.getByRole('button', { name: 'Supprimer définitivement le compte' }),
    ).toHaveCount(0)
    await page.getByRole('button', { name: 'Désactiver le compte' }).click()
    await page
      .getByRole('alertdialog', { name: 'Désactiver le compte de Tonton Talon ?' })
      .getByRole('button', { name: 'Désactiver le compte' })
      .click()
    await expect(page.getByText('Compte désactivé.')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Réactiver le compte' })).toBeVisible()

    await page.goto('/gestion/journal')
    await expect(page.getByText('marie a désactivé le compte de tonton.talon')).toBeVisible()
    await expect(
      page.getByText('marie a fait passer tonton.talon de Grimpeur·se à Ouvreur·se'),
    ).toBeVisible()
  })

  test('does not see the registration codes', async ({ page }) => {
    await page.goto('/gestion')
    await expect(page.getByRole('link', { name: 'Membres' }).first()).toBeVisible()
    await expect(page.getByRole('link', { name: 'Codes d’inscription' })).toHaveCount(0)
    await page.goto('/gestion/codes')
    await expect(page).toHaveURL(/\/actu$/)
  })
})

test.describe('the admin', () => {
  test.use({ signedInAs: 'ADMIN' })

  test('deletes an account for good', async ({ page, club }) => {
    club.addMember({ nickname: 'tonton.talon', firstName: 'Tonton', lastName: 'Talon' })
    await page.goto('/gestion/membres')
    await page.getByRole('link', { name: /Talon/ }).first().click()
    await expect(page.getByRole('heading', { level: 1, name: 'Tonton Talon' })).toBeVisible()
    await expect(page.getByText('Séances', { exact: true })).toBeVisible()

    await page.getByRole('button', { name: 'Supprimer définitivement le compte' }).click()
    await page
      .getByRole('alertdialog', { name: 'Supprimer pour de bon le compte de Tonton Talon ?' })
      .getByRole('button', { name: 'Supprimer pour de bon' })
      .click()
    await expect(page).toHaveURL(/\/gestion\/membres$/)
    await expect(page.getByText('1 membre', { exact: true })).toBeVisible()

    await page.goto('/gestion/journal')
    await expect(page.getByText('marie a supprimé le compte de tonton.talon')).toBeVisible()
  })

  test('creates a registration code for the gym poster', async ({ page }) => {
    await page.goto('/gestion/codes')
    await expect(page.getByText('Aucun code pour l’instant', { exact: false })).toBeVisible()
    await page.getByRole('button', { name: 'Nouveau code' }).click()
    const dialog = page.getByRole('dialog', { name: 'Nouveau code d’inscription' })
    await dialog.getByLabel('Libellé').fill('Affiche saison 2026')
    await dialog.getByLabel('Inscriptions maximum').fill('30')
    await dialog.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByText(/Code créé\s:\sK7XM-/)).toBeVisible()
    await expect(page.getByText('Affiche saison 2026')).toBeVisible()
    await expect(page.getByText('0 sur 30 inscriptions')).toBeVisible()
  })
})
