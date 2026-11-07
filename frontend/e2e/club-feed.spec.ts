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

test.describe('the club feed (D-120)', () => {
  test('to see it, one shows oneself: join, read, share a session with the club', async ({
    page,
    club,
    me,
  }) => {
    await sameDay(page, club)
    const marie = signedIn(me)
    const nina = club.addMember({ nickname: 'nina', firstName: 'Nina', lastName: 'Roc' })
    nina.inClubFeed = true
    club.climbs.add(nina, 1, [{ route: 'Iron Man', mode: 'LEAD', clean: true }], {
      visibility: 'CLUB',
      comment: 'Dévers du fond, enfin à moi.',
    })
    const tabs = page.getByRole('radiogroup', { name: 'Quel fil' })

    await page.goto('/actu')
    await tabs.getByRole('radio', { name: 'Le club' }).click()
    await expect(page).toHaveURL(/\/actu\?club/)
    // Closed: templates only, nothing of nina's session reaches the page.
    await expect(
      page.getByRole('heading', { name: 'Le fil du club, c’est donnant-donnant' }),
    ).toBeVisible()
    await expect(page.getByText('Dévers du fond, enfin à moi.')).toHaveCount(0)
    await expect(page.getByRole('article')).toHaveCount(0)

    await page.getByRole('button', { name: 'Je participe au fil du club' }).click()
    const dialog = page.getByRole('alertdialog', { name: 'Tu montres, tu vois' })
    await expect(dialog).toContainText('tes séances « Tout le club » y apparaissent aussi')
    await dialog.getByRole('button', { name: 'J’en suis' }).click()
    await expect(page.getByRole('article', { name: /de nina/ })).toBeVisible()
    await expect(page.getByText('« Dévers du fond, enfin à moi. »')).toBeVisible()
    expect(marie.inClubFeed).toBe(true)

    await page.goto('/seance/nouvelle')
    await page.getByRole('searchbox', { name: 'Ajouter une voie' }).fill('12')
    await page.getByRole('button', { name: 'Ajouter Bye Bye Morice' }).click()
    await page
      .getByRole('radiogroup', { name: 'Qui la voit' })
      .getByRole('radio', { name: 'Tout le club' })
      .click()
    await expect(
      page.getByText('Tes amis, et tous les membres qui participent au fil du club.'),
    ).toBeVisible()
    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page.getByText('Séance au carnet !')).toBeVisible()

    await page.goto('/actu?club')
    const mine = page.getByRole('article', { name: /Séance d’aujourd’hui de marie/ })
    await expect(mine).toBeVisible()
    await expect(mine.getByText('Tout le club')).toBeVisible()

    // Leaving from the profile closes the club feed again, and the editor says what changed.
    await page.goto('/profil/modifier')
    await page.getByRole('button', { name: 'Me retirer du fil du club' }).click()
    await expect(page.getByRole('status')).toContainText('ne sont plus visibles que de tes amis')
    await page.goto('/actu?club')
    await expect(page.getByRole('button', { name: 'Je participe au fil du club' })).toBeVisible()
    await expect(page.getByRole('article')).toHaveCount(0)
  })

  test('an account awaiting validation sees the closed club feed, without the button', async ({
    page,
    me,
  }) => {
    signedIn(me).validated = false
    await page.goto('/actu?club')
    await expect(
      page.getByText('Le fil du club s’ouvrira dès que la direction aura validé ton compte.'),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: 'Je participe au fil du club' })).toHaveCount(0)
  })

  test('without the club feed, « Tout le club » is not offered for a new session', async ({
    page,
    club,
  }) => {
    await sameDay(page, club)
    await page.goto('/seance/nouvelle')
    const choices = page.getByRole('radiogroup', { name: 'Qui la voit' })
    await expect(choices.getByRole('radio', { name: 'Mes amis' })).toBeVisible()
    await expect(choices.getByRole('radio', { name: 'Tout le club' })).toHaveCount(0)
  })
})
