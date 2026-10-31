import { expect, test } from './fixtures'

/** New accounts awaiting validation by the board (D-107). */
test.describe('an account awaiting validation', () => {
  test.use({ signedInAs: 'TOURIST' })

  test('keeps its own logbook and is told why the rest of the club is closed', async ({
    page,
    me,
  }) => {
    if (me) me.validated = false

    await page.goto('/profil')
    const notice = page.getByRole('heading', {
      name: 'Ton compte attend le feu vert de la direction',
    })
    await expect(notice).toBeVisible()
    await expect(page.getByRole('link', { name: 'Mes amis' })).toHaveCount(0)

    await page.goto('/annuaire')
    await expect(page).toHaveURL(/\/profil$/)

    await page
      .getByRole('navigation', { name: 'Navigation principale' })
      .getByRole('link', { name: 'Podium' })
      .click()
    await expect(
      page.getByText('Le podium s’ouvre dès que la direction a validé ton compte.', {
        exact: false,
      }),
    ).toBeVisible()

    await page
      .getByRole('navigation', { name: 'Navigation principale' })
      .getByRole('link', { name: 'Topo' })
      .click()
    await expect(page).toHaveURL(/\/topo/)
  })
})

test.describe('the board', () => {
  test.use({ signedInAs: 'BOARD' })

  test('validates a visitor as a tourist after reading their note', async ({ page, club }) => {
    club.addMember({
      nickname: 'alix',
      firstName: 'Alix',
      lastName: 'Moreau',
      role: 'TOURIST',
      validated: false,
      declaredLicensed: false,
      joinNote: 'Collègue de Lou, je viens un jeudi sur deux.',
    })

    await page.goto('/gestion/membres')
    const pending = page.getByRole('region', { name: /En attente de validation/ })
    await expect(pending).toBeVisible()
    await pending.getByRole('link', { name: /Alix Moreau/ }).click()

    await expect(page.getByRole('heading', { name: 'Nouveau compte à valider' })).toBeVisible()
    await expect(page.getByText('Collègue de Lou, je viens un jeudi sur deux.')).toBeVisible()
    await page.getByRole('button', { name: 'Valider : Touriste (pas licencié·e)' }).click()
    await expect(
      page.getByText('Compte validé : Alix Moreau est maintenant Touriste.'),
    ).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Nouveau compte à valider' })).toHaveCount(0)
    expect(club.members.find((member) => member.nickname === 'alix')).toMatchObject({
      validated: true,
      role: 'TOURIST',
      joinNote: undefined,
    })

    await page.goto('/gestion/membres')
    await expect(page.getByRole('link', { name: /Alix Moreau/ })).toBeVisible()
    await expect(page.getByRole('region', { name: /En attente de validation/ })).toHaveCount(0)
  })

  test('refuses an account it does not recognise', async ({ page, club }) => {
    club.addMember({
      nickname: 'inconnu',
      firstName: 'Ino',
      lastName: 'Connu',
      role: 'TOURIST',
      validated: false,
      declaredLicensed: true,
    })

    await page.goto('/gestion/membres')
    await page
      .getByRole('region', { name: /En attente de validation/ })
      .getByRole('link', { name: /Ino Connu/ })
      .click()
    await expect(page.getByText('Se déclare licencié·e au club.')).toBeVisible()
    await page.getByRole('button', { name: 'Refuser et supprimer le compte' }).click()
    const dialog = page.getByRole('alertdialog', { name: 'Refuser le compte de Ino Connu ?' })
    await dialog.getByRole('button', { name: 'Refuser' }).click()

    await expect(page).toHaveURL(/\/gestion\/membres$/)
    await expect(page.getByRole('link', { name: /Ino Connu/ })).toHaveCount(0)
    expect(club.audit[0]).toMatchObject({ action: 'MEMBER_REFUSED', targetLabel: 'inconnu' })
  })
})
