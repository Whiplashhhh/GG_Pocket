import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

test.describe('friends', () => {
  test('a request is answered from my profile, and the directory asks someone new', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const lea = club.addMember({ nickname: 'lea.dyno', firstName: 'Léa', lastName: 'Dupont' })
    club.addMember({
      nickname: 'jules',
      firstName: 'Jules',
      lastName: 'Martin',
      clubArrivalYear: 2019,
    })
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    club.social.link(lea, marie, false)
    club.social.link(tom, marie)

    await page.goto('/actu')
    // The feed stays for sessions: the requests wait on my profile, behind the dot on my avatar.
    await expect(page.getByRole('region', { name: '1 demande d’ami' })).toHaveCount(0)
    await page.getByRole('link', { name: 'Mon profil, 1 demande d’ami en attente' }).click()
    const requests = page.getByRole('region', { name: '1 demande d’ami' })
    await expect(requests.getByText('Léa Dupont')).toBeHidden()
    await requests.getByRole('button', { name: '1 demande d’ami' }).click()
    await expect(requests.getByText('Léa Dupont')).toBeVisible()
    await requests.getByRole('button', { name: 'Accepter la demande de lea.dyno' }).click()
    await expect(requests).toBeHidden()
    await expect(page.getByRole('link', { name: 'Mon profil', exact: true })).toBeVisible()
    await page.goto('/actu')

    await page.getByRole('link', { name: /4 grimpeurs accrochés à l’appli/ }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Annuaire du club' })).toBeVisible()
    await page.getByRole('searchbox', { name: 'Chercher un grimpeur' }).fill('marti')
    await expect(page.getByRole('status')).toHaveText('1 grimpeur')
    await expect(page.getByText('@jules · Le forceur')).toBeVisible()
    await page.getByRole('button', { name: 'Demander jules en ami' }).click()
    await expect(page.getByText('En attente', { exact: true })).toBeVisible()

    await page.getByRole('searchbox', { name: 'Chercher un grimpeur' }).fill('')
    await page.getByRole('radio', { name: 'Mes amis (2)' }).click()
    await expect(page).toHaveURL(/\/annuaire\?amis/)
    await expect(page.getByText('Tom Bloc')).toBeVisible()
    await expect(page.getByText('Léa Dupont')).toBeVisible()
    await expect(page.getByText('Jules Martin')).toBeHidden()
  })

  test('a friend’s profile shows their shared sessions and logbook, until we part', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const tom = club.addMember({
      nickname: 'tom',
      firstName: 'Tom',
      lastName: 'Bloc',
      bio: 'Toujours dans le dévers',
    })
    club.social.link(marie, tom)
    club.climbs.add(tom, 1, [{ route: 'Bye Bye Morice', mode: 'LEAD', clean: true }])
    club.climbs.add(tom, 2, [{ route: 'Bye Bye Morice' }, { route: 'Bye Bye Morice' }], {
      visibility: 'PRIVATE',
    })

    await page.goto(`/membres/${tom.id}`)
    await expect(page.getByRole('heading', { level: 2, name: 'Tom Bloc' })).toBeVisible()
    await expect(page.getByText('Toujours dans le dévers')).toBeVisible()
    await expect(page.getByText('Amis', { exact: true })).toBeVisible()
    const sessions = page.getByRole('region', { name: 'Ses séances' })
    await expect(sessions.getByRole('article')).toHaveCount(1)
    await expect(sessions.getByRole('article').getByText('1 voie')).toBeVisible()

    await sessions.getByRole('link', { name: /Son carnet/ }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Carnet de tom' })).toBeVisible()
    await page.getByRole('link', { name: 'Retour', exact: true }).click()

    await page.getByRole('button', { name: 'Retirer tom de mes amis' }).click()
    const dialog = page.getByRole('alertdialog', { name: 'On se désencorde ?' })
    await dialog.getByRole('button', { name: 'Retirer de mes amis' }).click()
    await expect(
      page.getByText('Ses séances et son carnet sont réservés à ses amis.'),
    ).toBeVisible()
    await expect(page.getByRole('button', { name: 'Demander tom en ami' })).toBeVisible()
  })

  test('a stranger’s sessions stay closed, and a friend’s session is read-only', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    club.social.link(tom, marie)
    const strangers = club.climbs.add(jules, 1, [{ route: 'Bye Bye Morice' }])
    const friends = club.climbs.add(tom, 1, [{ route: 'Bye Bye Morice' }])

    await page.goto(`/membres/${jules.id}`)
    await expect(
      page.getByText('Ses séances et son carnet sont réservés à ses amis.'),
    ).toBeVisible()
    await page.goto(`/seances/${strangers.id}`)
    await expect(page.getByRole('heading', { name: 'Séance introuvable' })).toBeVisible()

    await page.goto(`/seances/${friends.id}`)
    await expect(page.getByRole('article')).toBeVisible()
    await expect(page.getByRole('link', { name: 'Modifier' })).toBeHidden()
    await expect(page.getByRole('button', { name: 'Supprimer' })).toBeHidden()
  })
})

test.describe('the feed', () => {
  test('mixes my sessions with my friends’ shared ones; I react and comment', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    const jules = club.addMember({ nickname: 'jules', firstName: 'Jules', lastName: 'Martin' })
    club.social.link(tom, marie)
    const mine = club.climbs.add(marie, 0, [{ route: 'Réglisse' }])
    const toms = club.climbs.add(tom, 1, [{ route: 'Bye Bye Morice', mode: 'LEAD', clean: true }], {
      comment: 'Le dévers a cédé.',
    })
    club.climbs.add(tom, 2, [{ route: 'Bye Bye Morice' }], { visibility: 'PRIVATE' })
    club.climbs.add(jules, 1, [{ route: 'Bye Bye Morice' }])
    club.climbs.react(tom, mine, 'GORILLA')

    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/actu')
    await expect(page.getByRole('article')).toHaveCount(2)
    const myCard = page.getByRole('article', { name: 'Séance d’aujourd’hui de marie' })
    const tomsCard = page.getByRole('article', { name: 'Séance d’hier de tom' })
    await expect(tomsCard.getByText('« Le dévers a cédé. »')).toBeVisible()
    // On my own session, what the others gave, nothing to tap.
    await expect(myCard.getByText('Mode gorille : 1')).toHaveCount(1)
    await expect(myCard.getByRole('button')).toHaveCount(0)

    await tomsCard.getByRole('button', { name: 'Ça chauffe : 0' }).click()
    await expect(tomsCard.getByRole('button', { name: 'Ça chauffe : 1' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await tomsCard.getByRole('button', { name: 'Costaud : 0' }).click()
    await expect(tomsCard.getByRole('button', { name: 'Costaud : 1' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await expect(tomsCard.getByRole('button', { name: 'Ça chauffe : 0' })).toHaveAttribute(
      'aria-pressed',
      'false',
    )
    expect(toms.reactions.get(marie.id)).toBe('STRONG')

    await tomsCard.getByRole('link', { name: 'Commenter' }).click()
    await expect(page.getByRole('heading', { level: 2, name: 'tom' })).toBeVisible()
    await expect(page.getByText('Pas encore de commentaire.', { exact: false })).toBeVisible()
    await page.getByLabel('Ton commentaire').fill('Propre ! Tu me montres jeudi ?')
    await page.getByRole('button', { name: 'Publier' }).click()
    await expect(page.getByText('Propre ! Tu me montres jeudi ?')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Commentaires (1)' })).toBeVisible()

    await page.goto('/actu')
    await expect(tomsCard.getByRole('link', { name: '1 commentaire' })).toBeVisible()
  })

  test('says what to do when it is empty', async ({ page }) => {
    await page.goto('/actu')
    await expect(page.getByRole('heading', { name: 'Le mur est calme… trop calme.' })).toBeVisible()
    await page.getByRole('link', { name: 'Trouver des copains de cordée' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Annuaire du club' })).toBeVisible()
  })
})
