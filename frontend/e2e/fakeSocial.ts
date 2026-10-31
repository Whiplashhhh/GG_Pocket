import type { components } from '../src/api/schema'

import type { FakeMember } from './fakeClub'
import type { FakeSessions } from './fakeSessions'
import type { FakeTopo, TopoAnswer } from './fakeTopo'

type Schemas = components['schemas']
type BadgeCode = Schemas['BadgeLibraryEntry']['code']
type Seniority = NonNullable<Schemas['MemberCard']['seniority']>

/** The catalogue in the server's order (BadgeCode). */
const TIERS = ['1', '5', '10', '20', '50', '100', '250', '500', '750', '1000']
const BADGE_CODES = [
  ...TIERS.map((n) => `TOPPED_${n}`),
  ...TIERS.map((n) => `LEAD_${n}`),
  ...['5B', '5C', '6A', '6B', '6C', '7A', '7B', '7C'].map((grade) => `GRADE_${grade}`),
  ...['10', '25', '50', '75', '100', '500', '750', '1000'].map((n) => `DAYS_${n}`),
  'WEEK_2',
  'WEEK_3',
  'WEEK_4',
  'MONTH_COMPLETE',
  'MONTH_12',
  'STREAK_3W',
  'MONTH_NO_MISS',
  'FULL_LEAD',
  'PERFECT',
  'ZERO_REST',
  'COMEBACK',
  'FLASH',
  'RECORD',
] as BadgeCode[]
const BADGE_FAMILIES: Record<string, Schemas['BadgeLibraryEntry']['family']> = {
  TOPPED: 'TOPPED',
  LEAD: 'LEAD',
  GRADE: 'FIRST_GRADE',
  DAYS: 'CLIMBING_DAYS',
}

interface FakeAward {
  count: number
  firstEarnedOn: string
  lastEarnedOn: string
}

/** Years at the club as a nickname (Seniority.of), on the fake clock's year. */
export function seniorityOf(arrivalYear: number | undefined, now: number): Seniority | undefined {
  if (arrivalYear === undefined) return undefined
  const years = new Date(now).getUTCFullYear() - arrivalYear
  if (years >= 15) return 'FOSSIL'
  if (years >= 10) return 'RESIDENT'
  if (years >= 5) return 'FORCER'
  if (years >= 2) return 'PRO_BEGINNER'
  if (years >= 1) return 'BABY_CARABINER'
  return 'FRESH'
}

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
  /**
   * Earned badges per member, set by the tests: the fake has no badge engine (the server's is
   * tested on its own), it only serves libraries.
   */
  readonly badges = new Map<string, Map<BadgeCode, FakeAward>>()

  constructor(
    private readonly clock: () => number,
    private readonly members: () => FakeMember[],
    private readonly topo: FakeTopo,
    climbs: FakeSessions,
  ) {
    climbs.isFriend = (a, b) => this.state(a, b) === 'FRIENDS' && this.active(b) !== undefined
    climbs.inRankings = (id) => this.active(id)?.appearsInRankings === true
    climbs.memberRef = (id) => {
      const member = this.members().find((candidate) => candidate.id === id)
      return {
        id,
        nickname: member?.nickname ?? 'Ancien·ne membre',
        ...(member?.avatarUrl === undefined ? {} : { avatarUrl: member.avatarUrl }),
      }
    }
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

  /** `member` earned `code` `count` times, the first on `firstEarnedOn` and the last on `lastEarnedOn`. */
  award(
    member: FakeMember,
    code: BadgeCode,
    firstEarnedOn: string,
    count = 1,
    lastEarnedOn = firstEarnedOn,
  ): void {
    const mine = this.badges.get(member.id) ?? new Map<BadgeCode, FakeAward>()
    mine.set(code, { count, firstEarnedOn, lastEarnedOn })
    this.badges.set(member.id, mine)
  }

  private library(memberId: string): Schemas['BadgeLibrary'] {
    const mine = this.badges.get(memberId)
    return {
      badges: BADGE_CODES.map((code) => {
        const family = BADGE_FAMILIES[code.slice(0, code.indexOf('_'))] ?? 'REPEATABLE'
        const award = mine?.get(code)
        return {
          code,
          family,
          repeatable: family === 'REPEATABLE',
          count: award?.count ?? 0,
          ...(award === undefined
            ? {}
            : { firstEarnedOn: award.firstEarnedOn, lastEarnedOn: award.lastEarnedOn }),
        }
      }),
    }
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
      ...(member.clubArrivalYear === undefined
        ? {}
        : {
            clubArrivalYear: member.clubArrivalYear,
            seniority: seniorityOf(member.clubArrivalYear, this.clock()),
          }),
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
    if (path === '/me/badges' && method === 'GET') return json(this.library(me.id))
    const badges = /^\/members\/([^/]+)\/badges$/.exec(path)
    if (badges !== null && method === 'GET') {
      return this.active(badges[1]) === undefined ? notFound : json(this.library(badges[1]))
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
