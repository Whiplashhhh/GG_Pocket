import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

test.describe('badges', () => {
  test('my profile shows every badge: earned ones with ×N, the others with what it takes', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    marie.clubArrivalYear = 2025
    club.social.award(marie, 'TOPPED_1', '2026-10-02')
    club.social.award(marie, 'WEEK_2', '2026-10-05', 3, '2026-10-13')

    await page.goto('/profil')
    await expect(page.getByText('Le bébé mousqueton')).toBeVisible()
    const badges = page.getByRole('region', { name: 'Mes badges' })
    await expect(badges.getByText('2 sur 49 débloqués')).toBeVisible()

    const topped = badges.getByRole('list', { name: /Voies topées/ }).getByRole('listitem')
    await expect(topped).toHaveCount(10)
    await expect(topped.first()).toHaveAttribute('data-earned', 'true')
    await expect(topped.first()).toContainText('1 voie topée : débloqué le 2 octobre 2026')
    await expect(topped.nth(1)).toHaveAttribute('data-earned', 'false')
    await expect(topped.nth(1)).toContainText('Toper 5 voies, tous modes confondus.')

    const repeatable = badges.getByRole('list', { name: /À répéter/ })
    const week = repeatable.getByRole('listitem').filter({ hasText: '2 séances dans la semaine' })
    await expect(week.getByText('×3')).toBeVisible()
    await expect(week.getByText('Dernière fois le 13 octobre 2026')).toBeVisible()
    const comeback = repeatable.getByRole('listitem').filter({ hasText: 'Le retour en force' })
    await expect(comeback).toHaveAttribute('data-earned', 'false')
    await expect(comeback.getByText(/après au moins 30 jours sans/)).toBeVisible()
  })

  test('another member’s badges and seniority show in the directory and on their profile', async ({
    page,
    club,
    me,
  }) => {
    signedIn(me)
    const tom = club.addMember({
      nickname: 'tom',
      firstName: 'Tom',
      lastName: 'Bloc',
      clubArrivalYear: 2015,
    })
    club.social.award(tom, 'FLASH', '2026-10-14')

    await page.goto('/annuaire')
    await expect(page.getByText('@tom · Le résident de la salle')).toBeVisible()
    await page.getByRole('link', { name: /Tom Bloc/ }).click()
    await expect(page.getByRole('heading', { level: 2, name: 'Tom Bloc' })).toBeVisible()
    await expect(page.getByText('Le résident de la salle')).toBeVisible()
    const badges = page.getByRole('region', { name: 'Ses badges' })
    await expect(badges.getByText('1 sur 49 débloqués')).toBeVisible()
    const flash = badges.getByRole('listitem').filter({ hasText: 'Le flash' })
    await expect(flash.getByText('×1')).toBeVisible()
    await expect(flash.getByText('Débloqué le 14 octobre 2026')).toBeVisible()
  })
})
