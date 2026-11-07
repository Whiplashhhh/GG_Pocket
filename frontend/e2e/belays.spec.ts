import type { Page } from '@playwright/test'

import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

const line = (page: Page, n: number) =>
  page.getByRole('listitem').filter({ has: page.getByLabel(`Actions sur la voie n° ${String(n)}`) })

async function addRoute(page: Page, search: string, name: string) {
  await page.getByRole('searchbox', { name: 'Ajouter une voie' }).fill(search)
  await page.getByRole('button', { name: `Ajouter ${name}` }).click()
}

test.describe('the belayer', () => {
  test('is picked once for the session, friends first, or route by route', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    club.social.link(tom, marie)

    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/seance/nouvelle')
    await page
      .getByRole('button', { name: 'À l’assurage pour toute la séance : Pas précisé. Changer' })
      .click()
    const picker = page.getByRole('dialog', { name: 'Qui t’assurait ?' })
    // No keyboard over the list: the current choice has the focus, not the search box.
    await expect(picker.getByRole('button', { name: 'Pas précisé' })).toBeFocused()
    await expect(
      picker.getByRole('region', { name: 'Tes amis' }).getByRole('button', { name: /Tom Bloc/ }),
    ).toBeVisible()
    await expect(
      picker
        .getByRole('region', { name: 'Le reste du club' })
        .getByRole('button', { name: /Jules Martin/ }),
    ).toBeVisible()
    await picker.getByRole('searchbox', { name: 'Chercher un membre' }).fill('jul')
    await expect(picker.getByRole('button', { name: /Tom Bloc/ })).toHaveCount(0)
    await picker.getByRole('button', { name: /Jules Martin/ }).click()
    await expect(picker).toBeHidden()

    // One belayer for the whole session: no field on the routes.
    await addRoute(page, '12', 'Bye Bye Morice')
    await addRoute(page, 'reglisse', 'Réglisse')
    await expect(page.getByRole('button', { name: /^À l’assurage sur la voie/ })).toHaveCount(0)

    // Several belayers: each route has its field, the next route keeps the same partner.
    const many = page.getByRole('checkbox', { name: 'Plusieurs assureur·ses' })
    await many.check()
    await expect(
      line(page, 2).getByRole('button', { name: /^À l’assurage sur la voie n° 2 : jules/ }),
    ).toBeVisible()
    await line(page, 2)
      .getByRole('button', { name: /^À l’assurage sur la voie n° 2/ })
      .click()
    await picker.getByRole('button', { name: /Tom Bloc/ }).click()
    await addRoute(page, '12', 'Bye Bye Morice encore une fois')
    await expect(
      line(page, 3).getByRole('button', { name: /^À l’assurage sur la voie n° 3 : tom/ }),
    ).toBeVisible()
    await line(page, 3)
      .getByRole('button', { name: /^À l’assurage sur la voie n° 3/ })
      .click()
    await picker.getByRole('button', { name: 'Pas précisé' }).click()

    // Back to one belayer asks first, as the names differ; cancelling keeps them.
    await many.uncheck()
    const ask = page.getByRole('alertdialog', { name: 'Un·e seul·e assureur·se pour la séance ?' })
    await expect(ask.getByText(/Toutes les voies prendront jules/)).toBeVisible()
    await ask.getByRole('button', { name: 'Annuler' }).click()
    await expect(many).toBeChecked()

    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page).toHaveURL(/\/actu$/)
    const card = page.getByRole('article', { name: /Séance d’aujourd’hui/ })
    await expect(card.getByText('À l’assurage : jules et tom')).toBeVisible()
    const saved = club.climbs.sessions.at(-1)
    expect(saved?.ascents.map((ascent) => ascent.belayerId)).toEqual([jules.id, tom.id, undefined])

    await card.getByRole('link', { name: /Séance d’aujourd’hui/ }).click()
    await expect(page.getByText(/à l’assurage : jules/)).toBeVisible()
  })

  test('a session with several belayers opens with the box ticked', async ({ page, club, me }) => {
    const marie = signedIn(me)
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    club.social.link(tom, marie)
    const mine = club.climbs.add(marie, 1, [
      { route: 'Bye Bye Morice', belayerId: tom.id },
      { route: 'Réglisse', belayerId: jules.id },
    ])

    await page.clock.setSystemTime(new Date(club.now))
    await page.goto(`/seances/${mine.id}/modifier`)
    const many = page.getByRole('checkbox', { name: 'Plusieurs assureur·ses' })
    await expect(many).toBeChecked()
    await expect(
      line(page, 2).getByRole('button', { name: /^À l’assurage sur la voie n° 2 : jules/ }),
    ).toBeVisible()

    await many.uncheck()
    const ask = page.getByRole('alertdialog', { name: 'Un·e seul·e assureur·se pour la séance ?' })
    await ask.getByRole('button', { name: 'Appliquer à toutes les voies' }).click()
    await expect(many).not.toBeChecked()
    await expect(
      page.getByRole('button', { name: 'À l’assurage pour toute la séance : tom. Changer' }),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: /^À l’assurage sur la voie/ })).toHaveCount(0)

    await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
    await expect(page).toHaveURL(new RegExp(`/seances/${mine.id}$`))
    expect(mine.ascents.map((ascent) => ascent.belayerId)).toEqual([tom.id, tom.id])
  })

  test('sees where they held the rope and takes their name back', async ({ page, club, me }) => {
    const marie = signedIn(me)
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    club.social.link(tom, marie)
    const toms = club.climbs.add(tom, 1, [
      { route: 'Bye Bye Morice', belayerId: marie.id },
      { route: 'Réglisse', belayerId: marie.id },
    ])
    club.climbs.add(jules, 2, [{ route: 'Réglisse', belayerId: marie.id }])

    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/profil')
    await page.getByRole('link', { name: 'Mes assurages' }).click()
    const list = page.getByRole('list', { name: 'Mes assurages' })
    await expect(list.getByRole('listitem')).toHaveCount(2)
    // A friend's shared session opens; a stranger's does not.
    await expect(list.getByRole('link', { name: 'Séance d’hier de tom' })).toBeVisible()
    await expect(list.getByText('2 voies assurées')).toBeVisible()
    await expect(list.getByText(/^Séance du .* de jules$/)).toBeVisible()
    await expect(list.getByRole('link', { name: /jules/ })).toHaveCount(0)

    await list.getByRole('button', { name: 'Retirer mon nom : Séance d’hier de tom' }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: 'Retirer mon nom' }).click()
    await expect(list.getByRole('listitem')).toHaveCount(1)
    expect(toms.ascents.some((ascent) => ascent.belayerId === marie.id)).toBe(false)
  })
})
