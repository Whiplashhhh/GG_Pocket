import { describe, expect, it } from 'vitest'

import type { Notification } from '@/api/notifications'

import { notificationLine } from './notifications'

const tom = { id: 'tom-id', nickname: 'tom' }

function item(overrides: Partial<Notification>): Notification {
  return {
    id: 'x',
    kind: 'REACTION',
    at: '2026-10-20T08:00:00Z',
    actor: tom,
    unread: true,
    ...overrides,
  }
}

describe('notificationLine', () => {
  it('leads to the session, with its day and the reaction', () => {
    expect(
      notificationLine(
        item({ kind: 'REACTION', sessionId: 's1', climbedOn: '2026-10-19', reaction: 'FIRE' }),
        '2026-10-20',
      ),
    ).toEqual({
      key: 'notifications.kinds.REACTION',
      params: { nickname: 'tom', dayKind: 'yesterday', emoji: '🔥' },
      to: '/seances/s1',
    })
  })

  it('keeps the excerpt of a comment and the date of an older session', () => {
    expect(
      notificationLine(
        item({ kind: 'COMMENT', sessionId: 's1', climbedOn: '2026-10-01', excerpt: 'Bravo' }),
        '2026-10-20',
      ).params,
    ).toEqual({ nickname: 'tom', dayKind: 'date', date: '1er octobre 2026', excerpt: 'Bravo' })
  })

  it('sends a request to my profile, an accepted one to the friend', () => {
    expect(notificationLine(item({ kind: 'FRIEND_REQUEST' })).to).toBe('/profil')
    expect(notificationLine(item({ kind: 'FRIEND_ACCEPTED' })).to).toBe('/membres/tom-id')
  })
})
