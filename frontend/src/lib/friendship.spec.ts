import { describe, expect, it } from 'vitest'

import { friendshipControls } from './friendship'

const labels = (state: Parameters<typeof friendshipControls>[0], place: 'row' | 'profile') =>
  friendshipControls(state, place).actions.map((action) => action.label)

describe('friendshipControls', () => {
  it('offers to ask a stranger, everywhere', () => {
    expect(labels('NONE', 'row')).toEqual(['ask'])
    expect(labels('NONE', 'profile')).toEqual(['ask'])
    expect(friendshipControls('NONE', 'row').status).toBeNull()
  })

  it('lets me answer a request, everywhere', () => {
    expect(labels('INCOMING', 'row')).toEqual(['accept', 'decline'])
    expect(labels('INCOMING', 'profile')).toEqual(['accept', 'decline'])
  })

  it('shows a pending request as a label, cancellable from the profile only', () => {
    expect(friendshipControls('REQUESTED', 'row')).toEqual({ status: 'requested', actions: [] })
    expect(labels('REQUESTED', 'profile')).toEqual(['cancel'])
  })

  it('ends a friendship from the profile only, after asking', () => {
    expect(friendshipControls('FRIENDS', 'row')).toEqual({ status: 'friends', actions: [] })
    const [remove] = friendshipControls('FRIENDS', 'profile').actions
    expect(remove).toMatchObject({ kind: 'unfriend', label: 'remove', confirm: true })
  })

  it('offers nothing about myself', () => {
    expect(friendshipControls('SELF', 'profile')).toEqual({ status: null, actions: [] })
  })
})
