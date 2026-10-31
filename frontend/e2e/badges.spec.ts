import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

test.describe('badges', () => {
  test('my profile shows the last badges, unfolds them all, and a badge tells its next level', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    marie.clubArrivalYear = 2025
    club.social.award(marie, 'TOPPED_1', '2026-10-02')
    club.social.award(marie, 'TOPPED_5', '2026-10-02')
    club.social.award(marie, 'FLASH', '2026-10-08')
    club.social.award(marie, 'WEEK_2', '2026-09-01', 3, '2026-10-13')
    club.social.progress.set(marie.id, { topped: 6, lead: 0, climbingDays: 4 })

    await page.goto('/profil')
    await expect(page.getByText('Le bébé mousqueton')).toBeVisible()
    const badges = page.getByRole('region', { name: 'Mes badges' })
    await expect(badges.getByText('3 sur 17 débloqués')).toBeVisible()
    const recent = badges.getByRole('list').first().getByRole('button')
    await expect(recent).toHaveCount(3)
    await expect(recent.nth(0)).toHaveAccessibleName('2 séances dans la semaine, obtenu 3 fois')
    await expect(recent.nth(1)).toHaveAccessibleName('Le flash, obtenu une fois')
    await expect(recent.nth(2)).toHaveAccessibleName('Voies topées, niveau 2')

    const all = badges.getByRole('list', { name: 'Mes badges' })
    await expect(all).toBeHidden()
    await badges.getByRole('button', { name: 'Voir tous les badges' }).click()
    await expect(all.getByRole('button')).toHaveCount(17)
    await expect(
      all.getByRole('button', { name: 'Le retour en force, pas encore débloqué' }),
    ).toBeVisible()

    await all.getByRole('button', { name: 'Voies topées, niveau 2' }).click()
    const dialog = page.getByRole('dialog', { name: 'Voies topées' })
    await expect(dialog.getByText('Niveau 2', { exact: true })).toBeVisible()
    await expect(
      dialog.getByText('Prochain niveau : toper 10 voies, tous modes confondus.'),
    ).toBeVisible()
    const bar = dialog.getByRole('progressbar', { name: 'Vers le niveau suivant' })
    await expect(bar).toHaveAttribute('aria-valuenow', '6')
    await expect(bar).toHaveAttribute('aria-valuemax', '10')

    await dialog.getByRole('button', { name: 'Mettre en avant' }).click()
    await expect(dialog.getByRole('button', { name: 'Ne plus mettre en avant' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await dialog.getByRole('button', { name: 'Fermer' }).click()
    await expect(page.getByText('Badges favoris : Voies topées, niveau 2')).toBeAttached()
  })

  test('another member’s badges, favourites and seniority, in the directory and the feed', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const tom = club.addMember({
      nickname: 'tom',
      firstName: 'Tom',
      lastName: 'Bloc',
      clubArrivalYear: 2015,
    })
    club.social.link(tom, marie)
    club.social.award(tom, 'FLASH', '2026-10-14')
    club.social.favorites.set(tom.id, ['FLASH'])
    club.climbs.add(tom, 1, [{ route: 'Iron Man', mode: 'LEAD', clean: true }])

    await page.goto('/actu')
    await expect(
      page.getByRole('article').getByText('Badges favoris : Le flash, obtenu une fois'),
    ).toBeAttached()

    await page.goto('/annuaire')
    await expect(page.getByText('@tom · Le résident de la salle')).toBeVisible()
    await page.getByRole('link', { name: /Tom Bloc/ }).click()
    await expect(page.getByRole('heading', { level: 2, name: 'Tom Bloc' })).toBeVisible()
    await expect(page.getByText('Le résident de la salle')).toBeVisible()
    const badges = page.getByRole('region', { name: 'Ses badges' })
    await expect(badges.getByText('1 sur 17 débloqués')).toBeVisible()
    await badges.getByRole('button', { name: 'Le flash, obtenu une fois' }).click()
    const dialog = page.getByRole('dialog', { name: 'Le flash' })
    await expect(dialog.getByText('Débloqué le 14 octobre 2026.')).toBeVisible()
    await expect(dialog.getByRole('button', { name: 'Mettre en avant' })).toHaveCount(0)
    await expect(dialog.getByRole('progressbar')).toHaveCount(0)
  })
})
