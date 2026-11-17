import type { FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

function signedIn(me: FakeMember | null): FakeMember {
  if (me === null) throw new Error('This test needs a signed-in member')
  return me
}

test.describe('the bell (D-128)', () => {
  test('a dot says something happened; opening the bell lists it and puts the dot out', async ({
    page,
    club,
    me,
  }) => {
    const marie = signedIn(me)
    const tom = club.addMember({ nickname: 'tom', firstName: 'Tom', lastName: 'Bloc' })
    club.social.link(tom, marie)
    const mine = club.climbs.add(marie, 1, [{ route: 'Réglisse' }])
    const actor = { id: tom.id, nickname: 'tom' }
    club.notify(marie, { kind: 'FRIEND_REQUEST', actor }, 60)
    club.notify(
      marie,
      { kind: 'REACTION', actor, sessionId: mine.id, climbedOn: mine.climbedOn, reaction: 'FIRE' },
      20,
    )
    club.notify(
      marie,
      {
        kind: 'COMMENT',
        actor,
        sessionId: mine.id,
        climbedOn: mine.climbedOn,
        excerpt: 'Propre !',
      },
      10,
    )

    await page.clock.setSystemTime(new Date(club.now))
    await page.goto('/actu')
    const bell = page.getByRole('link', { name: 'Notifications, 3 nouvelles' })
    await expect(bell).toBeVisible()
    await bell.click()

    await expect(page.getByRole('heading', { level: 1, name: 'Notifications' })).toBeVisible()
    const list = page.getByRole('list', { name: 'Notifications' })
    await expect(list.getByRole('listitem')).toHaveText([
      /tom a commenté ta séance d’hier : « Propre ! ».*Nouveau/,
      /tom a réagi 🔥 à ta séance d’hier\..*Nouveau/,
      /tom te demande en ami\..*Nouveau/,
    ])
    // Opened: the dot is out, the marks stay for this visit.
    await expect(page.getByRole('link', { name: 'Notifications', exact: true })).toBeVisible()
    expect(club.notificationsSeenAt.get(marie.id)).toBe(club.now)

    await list.getByRole('link', { name: /a réagi/ }).click()
    await expect(page).toHaveURL(`/seances/${mine.id}`)
  })

  test('says so when nothing happened', async ({ page, me }) => {
    signedIn(me)
    await page.goto('/notifications')
    await expect(page.getByText('Rien de neuf au pied de la voie')).toBeVisible()
  })
})
