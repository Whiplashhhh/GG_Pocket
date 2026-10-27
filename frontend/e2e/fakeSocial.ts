import type { components } from '../src/api/schema'

import type { FakeMember } from './fakeClub'
import type { FakeSessions } from './fakeSessions'
import type { FakeTopo, TopoAnswer } from './fakeTopo'

type Schemas = components['schemas']

interface FakeFriendship {
  requesterId: string
  addresseeId: string
  accepted: boolean
  createdAt: string
  acceptedAt?: string
}

/**
 * Friendships, the directory and public profiles (FriendshipService, DirectoryService), close
 * enough for the e2e tests: one row per pair, only active members, everything from my side.
 */
export class FakeSocial {
  readonly friendships: FakeFriendship[] = []

  constructor(
    private readonly clock: () => number,
    private readonly members: () => FakeMember[],
    private readonly topo: FakeTopo,
    climbs: FakeSessions,
  ) {
    climbs.isFriend = (a, b) => this.state(a, b) === 'FRIENDS' && this.active(b) !== undefined
  }

  /** A request from `requester`, accepted already when `accepted`. */
  link(requester: FakeMember, addressee: FakeMember, accepted = true): void {
    const at = new Date(this.clock()).toISOString()
    this.friendships.push({
      requesterId: requester.id,
      addresseeId: addressee.id,
      accepted,
      createdAt: at,
      ...(accepted ? { acceptedAt: at } : {}),
    })
  }

  private between(a: string, b: string): FakeFriendship | undefined {
    return this.friendships.find(
      (f) =>
        (f.requesterId === a && f.addresseeId === b) ||
        (f.requesterId === b && f.addresseeId === a),
    )
  }

  private state(me: string, other: string): Schemas['DirectoryEntry']['friendship'] {
    if (me === other) return 'SELF'
    const friendship = this.between(me, other)
    if (friendship === undefined) return 'NONE'
    if (friendship.accepted) return 'FRIENDS'
    return friendship.requesterId === me ? 'REQUESTED' : 'INCOMING'
  }

  private active(id: string): FakeMember | undefined {
    return this.members().find((member) => member.id === id && member.status === 'ACTIVE')
  }

  private card(member: FakeMember): Schemas['MemberCard'] {
    return {
      id: member.id,
      nickname: member.nickname,
      firstName: member.firstName,
      lastName: member.lastName,
      role: member.role,
      ...(member.avatarUrl === undefined ? {} : { avatarUrl: member.avatarUrl }),
      ...(member.clubArrivalYear === undefined ? {} : { clubArrivalYear: member.clubArrivalYear }),
    }
  }

  handle(me: FakeMember, method: string, path: string): TopoAnswer | undefined {
    const json = (data: unknown, status?: number): TopoAnswer => ({ kind: 'json', data, status })
    const notFound: TopoAnswer = { kind: 'problem', status: 404, code: 'NOT_FOUND' }

    if (path === '/members' && method === 'GET') {
      const active = this.members()
        .filter((member) => member.status === 'ACTIVE')
        .sort((a, b) => a.lastName.localeCompare(b.lastName, 'fr'))
      return json({
        climberCount: active.length,
        members: active
          .filter((member) => member.id !== me.id)
          .map((member) => ({
            member: this.card(member),
            friendship: this.state(me.id, member.id),
          })),
      } satisfies Schemas['Directory'])
    }
    const profile = /^\/members\/([^/]+)$/.exec(path)
    if (profile !== null && method === 'GET') {
      const member = this.active(profile[1])
      if (member === undefined) return notFound
      const hearts = this.topo.favorites.get(member.id) ?? []
      return json({
        member: this.card(member),
        ...(member.showAge && member.birthDate !== undefined
          ? {
              age: Math.floor(
                (this.clock() - Date.parse(member.birthDate)) / (365.25 * 24 * 3600 * 1000),
              ),
            }
          : {}),
        ...(member.climbingSinceYear === undefined
          ? {}
          : { climbingSinceYear: member.climbingSinceYear }),
        ...(member.bio === undefined ? {} : { bio: member.bio }),
        ...(member.favoriteRouteId === undefined
          ? {}
          : { favoriteRouteId: member.favoriteRouteId }),
        favorites: this.topo.routes
          .filter((route) => hearts.includes(route.id))
          .map((route) => this.topo.summary(me, route)),
        friendship: this.state(me.id, member.id),
      } satisfies Schemas['MemberProfile'])
    }
    if (path === '/me/friends' && method === 'GET') {
      const mine = this.friendships
        .filter((f) => f.requesterId === me.id || f.addresseeId === me.id)
        .map((f) => ({
          f,
          other: this.active(f.requesterId === me.id ? f.addresseeId : f.requesterId),
        }))
        .filter(
          (item): item is { f: FakeFriendship; other: FakeMember } => item.other !== undefined,
        )
        .reverse()
      const friend = (item: { f: FakeFriendship; other: FakeMember }, since: string) => ({
        member: this.card(item.other),
        since,
      })
      return json({
        incoming: mine
          .filter(({ f }) => !f.accepted && f.addresseeId === me.id)
          .map((item) => friend(item, item.f.createdAt)),
        outgoing: mine
          .filter(({ f }) => !f.accepted && f.requesterId === me.id)
          .map((item) => friend(item, item.f.createdAt)),
        friends: mine
          .filter(({ f }) => f.accepted)
          .map((item) => friend(item, item.f.acceptedAt ?? item.f.createdAt)),
      } satisfies Schemas['Friends'])
    }
    const change = /^\/me\/friends\/([^/]+)$/.exec(path)
    if (change !== null) {
      const otherId = change[1]
      const existing = this.between(me.id, otherId)
      if (method === 'DELETE') {
        if (existing !== undefined) this.friendships.splice(this.friendships.indexOf(existing), 1)
        return { kind: 'empty' }
      }
      if (method === 'PUT') {
        if (otherId === me.id) return { kind: 'problem', status: 400, code: 'VALIDATION_FAILED' }
        const other = this.active(otherId)
        if (other === undefined) return notFound
        if (existing === undefined) this.link(me, other, false)
        else if (!existing.accepted && existing.addresseeId === me.id) {
          existing.accepted = true
          existing.acceptedAt = new Date(this.clock()).toISOString()
        }
        return json({ friendship: this.state(me.id, otherId) })
      }
    }
    return undefined
  }
}
