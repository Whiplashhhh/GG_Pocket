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

/** `days` sessions this month (one a day, from yesterday back), `topped` routes topped in each. */
function climbs(club: FakeClub, member: FakeMember, days: number, topped = 1) {
  for (let day = 1; day <= days; day++) {
    club.climbs.add(
      member,
      day,
      Array.from({ length: topped }, () => ({ route: 'Bye Bye Morice' })),
    )
  }
}

const podium = (page: Page) => page.getByRole('list', { name: 'Les trois premiers' })
const rest = (page: Page) => page.getByRole('region', { name: 'La suite du classement' })

test.describe('podium', () => {
  test('ranks the month with ties, and keeps my place in sight', async ({ page, club, me }) => {
    await sameDay(page, club)
    const marie = signedIn(me)
    const lea = club.addMember({ nickname: 'lea.dyno', firstName: 'Léa', lastName: 'Dupont' })
    const tom = club.addMember({ nickname: 'Tom', firstName: 'Tom', lastName: 'Bloc' })
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    climbs(club, lea, 9, 1)
    climbs(club, tom, 9, 2)
    climbs(club, jules, 8, 1)
    for (const [index, name] of ['ana', 'bea', 'cyril', 'dede', 'eli', 'fanny', 'gus'].entries()) {
      climbs(club, club.addMember({ nickname: name }), 7 - Math.floor(index / 3), 1)
    }
    climbs(club, marie, 2, 3)

    await page.goto('/podium')
    await expect(page.getByRole('heading', { level: 1, name: 'Podium' })).toBeVisible()
    await expect(page.getByRole('heading', { level: 2, name: /octobre 2026/i })).toBeVisible()
    await expect(page.getByText('En direct : ça bouge à chaque séance.')).toBeVisible()

    // Léa and Tom tie on 9 days: both first, in alphabetical order whatever the case.
    const top = podium(page).getByRole('listitem')
    await expect(top).toHaveCount(3)
    await expect(top.nth(0)).toContainText('lea.dyno')
    await expect(top.nth(0)).toContainText('1er')
    await expect(top.nth(1)).toContainText('Tom')
    await expect(top.nth(1)).toContainText('1er')
    await expect(top.nth(2)).toContainText('jules')
    await expect(top.nth(2)).toContainText('3e')
    await expect(top.nth(2)).toContainText('8 jours')

    // Far down the list, my place stays in a bar until my line shows.
    const myPlace = page.getByTestId('podium-my-place')
    await expect(myPlace).toContainText('11e')
    await expect(myPlace).toContainText('2 jours')
    const myLine = rest(page).getByRole('listitem').filter({ hasText: 'toi' })
    await expect(myLine).toContainText('marie')
    await myLine.scrollIntoViewIfNeeded()
    await expect(myPlace).toBeHidden()

    // Routes: Tom tops two a session.
    await page.getByRole('radio', { name: 'Voies' }).click()
    await expect(page).toHaveURL(/classement=voies/)
    await expect(top.nth(0)).toContainText('Tom')
    await expect(top.nth(0)).toContainText('18 voies')
    await expect(top.nth(1)).toContainText('lea.dyno')
    await expect(top.nth(1)).toContainText('2e')
  })

  test('goes back to a frozen month, not before the first one', async ({ page, club, me }) => {
    await sameDay(page, club)
    const marie = signedIn(me)
    club.climbs.add(marie, 30, [{ route: 'Bye Bye Morice' }, { route: 'Réglisse' }])

    await page.goto('/podium')
    await expect(
      page.getByRole('heading', { name: 'Le podium attend ses premières grimpes.' }),
    ).toBeVisible()
    // The empty wall says it all: no "not ranked yet" on top of it.
    await expect(page.getByText('Pas encore classé·e ce mois-ci')).toHaveCount(0)

    await page.getByRole('button', { name: 'Mois précédent' }).click()
    await expect(page).toHaveURL(/mois=2026-09/)
    await expect(page.getByRole('heading', { level: 2, name: /septembre 2026/i })).toBeVisible()
    await expect(page.getByText('Figé : c’est gravé dans la résine.')).toBeVisible()
    await expect(podium(page).getByRole('listitem')).toHaveCount(1)
    await expect(podium(page)).toContainText('toi')
    await expect(page.getByRole('button', { name: 'Mois précédent' })).toBeDisabled()
    await expect(page.getByRole('button', { name: 'Mois suivant' })).toBeEnabled()
  })

  test('tells who chose to stay out of the rankings how to come back', async ({
    page,
    club,
    me,
  }) => {
    await sameDay(page, club)
    const marie = signedIn(me)
    marie.appearsInRankings = false
    climbs(club, marie, 3)
    climbs(club, club.addMember({ nickname: 'lea.dyno' }), 1)

    await page.goto('/podium')
    await expect(podium(page).getByRole('listitem')).toHaveCount(1)
    await expect(page.getByTestId('podium-opted-out')).toContainText('tu grimpes incognito')
    await page.getByRole('link', { name: 'Changer d’avis' }).click()
    await expect(page).toHaveURL(/\/profil\/modifier$/)
  })
})
