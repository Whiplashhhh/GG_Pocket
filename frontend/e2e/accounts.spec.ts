import { expect, test } from './fixtures'

test.describe('a visitor', () => {
  test.use({ signedInAs: null })

  test('lands on the welcome screen with the two doors', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/\/accueil$/)
    await expect(page.getByRole('heading', { level: 1, name: 'GG Pocket' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'J’ai un code du club' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'J’ai déjà un compte' })).toBeVisible()
    await expect(page.getByRole('navigation', { name: 'Navigation principale' })).toHaveCount(0)
  })

  test('is sent back where they wanted to go after signing in', async ({ page, club }) => {
    club.addMember({ nickname: 'Tonton.Talon', password: 'un mot de passe solide' })
    await page.goto('/profil/appareils')
    await expect(page).toHaveURL(/\/accueil\?retour=/)
    await page.getByRole('link', { name: 'J’ai déjà un compte' }).click()
    await page.getByLabel('Pseudo').fill('tonton.talon')
    await page.getByLabel('Mot de passe', { exact: true }).fill('un mot de passe solide')
    await page.getByRole('button', { name: 'Se connecter' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Mes appareils' })).toBeVisible()
  })

  test('is told plainly when the nickname and password do not match', async ({ page, club }) => {
    club.addMember({ nickname: 'marie' })
    await page.goto('/connexion')
    await page.getByLabel('Pseudo').fill('marie')
    await page.getByLabel('Mot de passe', { exact: true }).fill('pas le bon')
    await page.getByRole('button', { name: 'Se connecter' }).click()
    await expect(page.getByRole('alert')).toHaveText(
      'Ce pseudo et ce mot de passe ne s’assurent pas mutuellement. Réessaie.',
    )
  })

  test('reads the privacy page without an account', async ({ page }) => {
    await page.goto('/tes-donnees')
    await expect(page.getByRole('heading', { level: 1, name: 'Tes données' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Combien de temps' })).toBeVisible()
  })

  test('cannot join with a wrong code', async ({ page }) => {
    await page.goto('/inscription')
    await page.getByLabel('Code du club').fill('ZZZZ-ZZZZ')
    await page.getByRole('button', { name: 'Vérifier le code' }).click()
    await expect(page.getByText('Ce code ne mène nulle part', { exact: false })).toBeVisible()
  })

  test('chooses a new password with the link from the board', async ({ page, club }) => {
    const member = club.addMember({ nickname: 'marie', firstName: 'Marie' })
    club.resetTokens.set('jeton-secret', member.id)
    await page.goto('/nouveau-mot-de-passe#jeton-secret')
    await expect(
      page.getByText('Salut Marie ! Pour mémoire, ton pseudo, c’est marie.'),
    ).toBeVisible()
    await page.getByLabel('Nouveau mot de passe').fill('du mou du mou du mou')
    await page.getByRole('button', { name: 'Enregistrer et me connecter' }).click()
    await expect(page).toHaveURL(/\/actu$/)
    expect(member.password).toBe('du mou du mou du mou')
    // The secret left the address bar.
    expect(page.url()).not.toContain('jeton-secret')
  })

  test('is told when a reset link has served', async ({ page }) => {
    await page.goto('/nouveau-mot-de-passe#deja-utilise')
    await expect(
      page.getByRole('heading', { name: 'Ce lien ne mène plus nulle part.' }),
    ).toBeVisible()
  })
})

test.describe('joining the club', () => {
  test.use({ signedInAs: null, installInvite: true })

  test('code, form, first steps, then still signed in 30 days later', async ({
    page,
    club,
    isMobile,
  }) => {
    club.addCode('K7XM-P2QA')
    await page.clock.setSystemTime(new Date(club.now))

    await page.goto('/')
    await page.getByRole('link', { name: 'J’ai un code du club' }).click()
    await page.getByLabel('Code du club').fill('k7xm p2qa')
    await page.getByRole('button', { name: 'Vérifier le code' }).click()

    await expect(page.getByRole('heading', { name: 'Ta fiche de grimpeur·se' })).toBeVisible()
    await page.getByLabel('Prénom').fill('Léa')
    await page.getByLabel('Nom', { exact: true }).fill('Dupont')
    await page.getByLabel('Pseudo').fill('lea.d')
    await page.getByLabel('Mot de passe', { exact: true }).fill('azertyuiop')
    await page.getByRole('checkbox', { name: /J’ai lu la page/ }).check()
    await page.getByRole('button', { name: 'Créer mon compte' }).click()
    await expect(
      page.getByText('Ce mot de passe fait partie des plus courants', { exact: false }),
    ).toBeVisible()
    await page.getByLabel('Mot de passe', { exact: true }).fill('avant-bras en béton')
    await page.getByRole('button', { name: 'Créer mon compte' }).click()

    // First steps: photo (skipped), details, invitation to install.
    await expect(page).toHaveURL(/\/premiers-pas\/photo$/)
    await expect(page.getByText('Étape 1 sur 4')).toBeVisible()
    await page.getByRole('button', { name: 'Passer cette étape' }).click()
    await page.getByLabel('Je grimpe depuis').fill('2015')
    await page.getByRole('button', { name: 'Continuer' }).click()
    await expect(page.getByRole('heading', { name: 'Ta voie préférée ?' })).toBeVisible()
    await page.getByRole('button', { name: 'Passer cette étape' }).click()
    await expect(page.getByRole('heading', { name: 'Installe GG Pocket' })).toBeVisible()
    if (isMobile) {
      await expect(page.getByText('Sur iPhone et iPad, ça se fait à la main')).toBeVisible()
    } else {
      await expect(
        page.getByText('Ouvre le menu de ton navigateur', { exact: false }),
      ).toBeVisible()
    }
    await page.getByRole('button', { name: 'C’est parti !' }).click()
    await expect(page).toHaveURL(/\/actu$/)
    await expect(page.getByRole('link', { name: 'Mon profil' })).toContainText('lea.d')

    // 30 days later: no sign-in, and the postponed invitation comes back.
    club.advance(30)
    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/actu')
    await expect(page).toHaveURL(/\/actu$/)
    const invitation = page.getByRole('dialog', { name: 'Installe GG Pocket' })
    await expect(invitation).toBeVisible()
    await invitation.getByRole('button', { name: 'Plus tard' }).click()
    await expect(invitation).toBeHidden()
    await expect(page.getByRole('link', { name: 'Mon profil' })).toContainText('lea.d')

    // Six months without opening the app: the session is gone.
    club.advance(181)
    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/actu')
    await expect(page).toHaveURL(/\/accueil$/)
  })
})

test.describe('a signed-in member', () => {
  test('signs out from the profile', async ({ page }) => {
    await page.goto('/profil')
    await page.getByRole('button', { name: 'Redescendre' }).click()
    const confirm = page.getByRole('alertdialog', { name: 'Tu redescends ?' })
    await confirm.getByRole('button', { name: 'Redescendre' }).click()
    await expect(page).toHaveURL(/\/accueil$/)
  })

  test('whose session ended is asked to sign in again, with an explanation', async ({
    page,
    club,
  }) => {
    await page.goto('/actu')
    const profileLink = page.getByRole('link', { name: 'Mon profil' })
    await expect(profileLink).toBeVisible()
    club.advance(181)
    await profileLink.click()
    await expect(page).toHaveURL(/\/connexion\?fin=1/)
    await expect(page.getByRole('status')).toContainText('Tu n’es plus encordé·e')
  })

  test('edits their profile', async ({ page }) => {
    await page.goto('/profil')
    await page.getByRole('link', { name: 'Modifier mon profil' }).click()
    await page.getByLabel('Bio').fill('Ennemie jurée des dévers.')
    await page.getByText('Afficher mon âge sur mon profil').click()
    await page.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Mon profil' })).toBeVisible()
    await expect(page.getByText('Ennemie jurée des dévers.')).toBeVisible()
  })

  test('sees their devices', async ({ page }) => {
    await page.goto('/profil/appareils')
    await expect(page.getByText('Safari sur iPhone')).toBeVisible()
    await expect(page.getByText('Cet appareil', { exact: true })).toBeVisible()
    await expect(page.getByText('Tu n’es connecté·e que sur cet appareil.')).toBeVisible()
  })
})
