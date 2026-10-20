import type { BrowserContext, Page, Request, Route } from '@playwright/test'

import type { components } from '../src/api/schema'

type Schemas = components['schemas']
type Role = Schemas['CurrentMember']['role']

/**
 * A small in-memory GG Pocket API for the end-to-end tests (DECISIONS D-040): members, codes,
 * sessions carried by a real cookie, the CSRF check, and a clock the test moves forward. The real
 * rules live in the backend and its integration tests; this only behaves the same at the surface.
 */
export interface FakeMember {
  id: string
  nickname: string
  firstName: string
  lastName: string
  password: string
  role: Role
  status: 'ACTIVE' | 'DISABLED'
  email?: string
  birthDate?: string
  parentalConsent: boolean
  showAge: boolean
  appearsInRankings: boolean
  clubArrivalYear?: number
  climbingSinceYear?: number
  bio?: string
  avatarUrl?: string
  registeredAt: string
  lastSeenAt?: string
}

interface FakeSession {
  memberId: string
  remembered: boolean
  signedInAt: number
  lastActiveAt: number
}

const SESSION_COOKIE = 'GG_SESSION'
const CSRF_COOKIE = 'XSRF-TOKEN'
const DAY = 24 * 3600 * 1000
const REMEMBERED_INACTIVITY = 180 * DAY
const BROWSER_SESSION_INACTIVITY = DAY / 2
const ROLES: Role[] = ['CLIMBER', 'SETTER', 'BOARD', 'ADMIN']

/** 1×1 orange pixel, served as every uploaded photo. */
const PIXEL = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8n8DwHwAFhwJ/lyIPWwAAAABJRU5ErkJggg==',
  'base64',
)

const problem = (status: number, code: string) => ({
  status,
  contentType: 'application/problem+json',
  body: JSON.stringify({ status, code }),
})

export class FakeClub {
  /** Moved forward by tests to simulate time passing (sessions, invitations). */
  now = Date.parse('2026-10-20T08:00:00Z')

  readonly members: FakeMember[] = []
  readonly codes: Schemas['RegistrationCode'][] = []
  readonly audit: Schemas['AuditEntry'][] = []
  readonly resetTokens = new Map<string, string>()
  private readonly sessions = new Map<string, FakeSession>()
  private sequence = 0

  constructor(
    private readonly context: BrowserContext,
    private readonly origin: string,
  ) {}

  addMember(overrides: Partial<FakeMember> & Pick<FakeMember, 'nickname'>): FakeMember {
    const member: FakeMember = {
      id: this.nextId(),
      firstName: 'Marie',
      lastName: 'Hhh',
      password: 'avant-bras en béton',
      role: 'CLIMBER',
      status: 'ACTIVE',
      parentalConsent: false,
      showAge: false,
      appearsInRankings: true,
      registeredAt: new Date(this.now - 30 * DAY).toISOString(),
      ...overrides,
    }
    this.members.push(member)
    return member
  }

  addCode(code: string, label = 'Affiche de la salle'): void {
    this.codes.push({
      id: this.nextId(),
      code,
      label,
      useCount: 0,
      active: true,
      state: 'USABLE',
      createdAt: new Date(this.now).toISOString(),
    })
  }

  /** Signs the browser in as this member, like a past sign-in with "Rester connecté". */
  async signIn(member: FakeMember): Promise<void> {
    await this.openSession(member, true)
  }

  advance(days: number): void {
    this.now += days * DAY
  }

  async install(page: Page): Promise<void> {
    await page.route('**/api/v1/**', (route) => this.handle(route))
  }

  private nextId(): string {
    this.sequence += 1
    return `00000000-0000-4000-8000-${this.sequence.toString().padStart(12, '0')}`
  }

  private async openSession(member: FakeMember, remembered: boolean): Promise<void> {
    const token = `session-${this.nextId()}`
    this.sessions.set(token, {
      memberId: member.id,
      remembered,
      signedInAt: this.now,
      lastActiveAt: this.now,
    })
    await this.context.addCookies([
      {
        name: SESSION_COOKIE,
        value: token,
        url: this.origin,
        httpOnly: true,
        sameSite: 'Lax',
        // Browsers keep remembered sessions; the server decides when they expire.
        ...(remembered ? { expires: Math.floor(Date.now() / 1000) + 400 * 86400 } : {}),
      },
    ])
  }

  private cookie(request: Request, name: string): string | undefined {
    const header = (request.headers().cookie as string | undefined) ?? ''
    return header
      .split(';')
      .map((part) => part.trim().split('='))
      .find(([key]) => key === name)?.[1]
  }

  /** The member of a live session, extending it like Spring Session does on every request. */
  private current(request: Request): FakeMember | undefined {
    const token = this.cookie(request, SESSION_COOKIE)
    const session = token === undefined ? undefined : this.sessions.get(token)
    if (token === undefined || session === undefined) {
      return undefined
    }
    const limit = session.remembered ? REMEMBERED_INACTIVITY : BROWSER_SESSION_INACTIVITY
    const member = this.members.find((candidate) => candidate.id === session.memberId)
    if (this.now - session.lastActiveAt > limit || member?.status !== 'ACTIVE') {
      this.sessions.delete(token)
      return undefined
    }
    session.lastActiveAt = this.now
    member.lastSeenAt = new Date(this.now).toISOString()
    return member
  }

  private currentMember(member: FakeMember): Schemas['CurrentMember'] {
    return {
      id: member.id,
      nickname: member.nickname,
      firstName: member.firstName,
      lastName: member.lastName,
      role: member.role,
      ...(member.avatarUrl ? { avatarUrl: member.avatarUrl } : {}),
    }
  }

  private profile(member: FakeMember): Schemas['MyProfile'] {
    return {
      ...this.currentMember(member),
      email: member.email,
      birthDate: member.birthDate,
      showAge: member.showAge,
      parentalConsent: member.parentalConsent,
      appearsInRankings: member.appearsInRankings,
      clubArrivalYear: member.clubArrivalYear,
      climbingSinceYear: member.climbingSinceYear,
      bio: member.bio,
      createdAt: member.registeredAt,
    }
  }

  private summary(member: FakeMember): Schemas['MemberSummary'] {
    return {
      id: member.id,
      nickname: member.nickname,
      firstName: member.firstName,
      lastName: member.lastName,
      role: member.role,
      status: member.status,
      registeredAt: member.registeredAt,
      lastSeenAt: member.lastSeenAt,
    }
  }

  private detail(actor: FakeMember, member: FakeMember): Schemas['MemberDetail'] {
    const self = actor.id === member.id
    const manageable =
      !self &&
      (actor.role === 'ADMIN' || (actor.role === 'BOARD' && ROLES.indexOf(member.role) < 2))
    const assignable = self
      ? []
      : actor.role === 'ADMIN'
        ? ROLES.filter((role) => role !== member.role)
        : actor.role === 'BOARD' && ROLES.indexOf(member.role) < 2
          ? (['CLIMBER', 'SETTER'] as Role[]).filter((role) => role !== member.role)
          : []
    return {
      member: this.summary(member),
      email: member.email,
      underFifteenWithParentalConsent: false,
      clubArrivalYear: member.clubArrivalYear,
      climbingSinceYear: member.climbingSinceYear,
      permissions: {
        assignableRoles: assignable,
        canDisable: manageable && member.status === 'ACTIVE',
        canEnable: manageable && member.status === 'DISABLED',
        canCreateResetLink: manageable && member.status === 'ACTIVE',
      },
    }
  }

  private record(
    actor: FakeMember,
    action: Schemas['AuditEntry']['action'],
    target: string,
    before?: string,
    after?: string,
  ) {
    this.audit.unshift({
      id: this.nextId(),
      occurredAt: new Date(this.now).toISOString(),
      actorId: actor.id,
      actorNickname: actor.nickname,
      action,
      targetType: action.startsWith('REGISTRATION') ? 'REGISTRATION_CODE' : 'MEMBER',
      targetLabel: target,
      before,
      after,
    })
  }

  private async handle(route: Route): Promise<void> {
    const request = route.request()
    const method = request.method()
    const path = new URL(request.url()).pathname.replace('/api/v1', '')
    const body = (): Record<string, unknown> => {
      try {
        return (request.postDataJSON() as Record<string, unknown> | null) ?? {}
      } catch {
        return {}
      }
    }
    const json = (data: unknown, status = 200) => route.fulfill({ status, json: data })
    const fail = (status: number, code: string) => route.fulfill(problem(status, code))

    if (path === '/meta') {
      return json({
        version: '0.0.0',
        environment: 'local',
        branch: 'main',
        commit: '0123456789abcdef',
      })
    }

    // The app must send the CSRF cookie back in the header on every write.
    if (method !== 'GET') {
      const cookie = this.cookie(request, CSRF_COOKIE)
      if (cookie === undefined || request.headers()['x-xsrf-token'] !== cookie) {
        return fail(403, 'CSRF_TOKEN_INVALID')
      }
    }

    if (path === '/auth/state') {
      await this.context.addCookies([{ name: CSRF_COOKIE, value: 'jeton-csrf', url: this.origin }])
      const member = this.current(request)
      return json(
        member
          ? { authenticated: true, member: this.currentMember(member) }
          : { authenticated: false },
      )
    }
    if (path === '/auth/login') {
      const { nickname, password, rememberMe } = body() as {
        nickname: string
        password: string
        rememberMe: boolean
      }
      const member = this.members.find(
        (candidate) => candidate.nickname.toLowerCase() === nickname.toLowerCase(),
      )
      if (member?.password !== password) return fail(401, 'BAD_CREDENTIALS')
      if (member.status !== 'ACTIVE') return fail(403, 'ACCOUNT_DISABLED')
      await this.openSession(member, rememberMe)
      return json(this.currentMember(member))
    }
    if (path === '/auth/logout') {
      const token = this.cookie(request, SESSION_COOKIE)
      if (token !== undefined) this.sessions.delete(token)
      await this.context.clearCookies({ name: SESSION_COOKIE })
      return route.fulfill({ status: 204 })
    }
    if (path === '/auth/registration-code/check' || path === '/auth/register') {
      const sent = body().code
      const typed = (typeof sent === 'string' ? sent : '').replace(/[\s-]/g, '').toUpperCase()
      const code = this.codes.find(
        (candidate) => candidate.code.replace('-', '') === typed && candidate.state === 'USABLE',
      )
      if (code === undefined) return fail(400, 'REGISTRATION_CODE_INVALID')
      if (path.endsWith('/check')) return route.fulfill({ status: 204 })
      const form = body() as Schemas['RegisterRequest']
      if (
        this.members.some((member) => member.nickname.toLowerCase() === form.nickname.toLowerCase())
      ) {
        return fail(409, 'NICKNAME_TAKEN')
      }
      if (form.password.toLowerCase() === 'azertyuiop') return fail(400, 'PASSWORD_TOO_COMMON')
      const member = this.addMember({
        nickname: form.nickname,
        firstName: form.firstName,
        lastName: form.lastName,
        password: form.password,
        email: form.email,
        birthDate: form.birthDate,
        parentalConsent: form.parentalConsent ?? false,
        registeredAt: new Date(this.now).toISOString(),
      })
      code.useCount += 1
      await this.openSession(member, form.rememberMe ?? false)
      return json(this.currentMember(member), 201)
    }
    if (path === '/auth/password-reset/check' || path === '/auth/password-reset') {
      const { token, newPassword } = body() as { token: string; newPassword?: string }
      const member = this.members.find((candidate) => candidate.id === this.resetTokens.get(token))
      if (member === undefined) return fail(400, 'PASSWORD_RESET_LINK_INVALID')
      if (path.endsWith('/check'))
        return json({ firstName: member.firstName, nickname: member.nickname })
      member.password = newPassword ?? ''
      this.resetTokens.delete(token)
      for (const [key, session] of this.sessions) {
        if (session.memberId === member.id) this.sessions.delete(key)
      }
      await this.openSession(member, true)
      return json(this.currentMember(member))
    }

    if (path.startsWith('/media/')) {
      return route.fulfill({ status: 200, contentType: 'image/png', body: PIXEL })
    }

    const me = this.current(request)
    if (me === undefined) return fail(401, 'UNAUTHENTICATED')

    if (path === '/me' && method === 'GET') return json(this.profile(me))
    if (path === '/me/profile') {
      const update = body() as Schemas['ProfileUpdate']
      Object.assign(me, update)
      return json(this.profile(me))
    }
    if (path === '/me/avatar') {
      me.avatarUrl = method === 'DELETE' ? undefined : `/api/v1/media/${this.nextId()}`
      return method === 'DELETE' ? route.fulfill({ status: 204 }) : json(this.profile(me))
    }
    if (path === '/me/password') {
      const { currentPassword, newPassword } = body() as {
        currentPassword: string
        newPassword: string
      }
      if (currentPassword !== me.password) return fail(400, 'CURRENT_PASSWORD_WRONG')
      me.password = newPassword
      return route.fulfill({ status: 204 })
    }
    if (path === '/me/devices') {
      const token = this.cookie(request, SESSION_COOKIE)
      return json(
        [...this.sessions.entries()]
          .filter(([, session]) => session.memberId === me.id)
          .map(([key, session]) => ({
            id: key,
            browser: 'SAFARI',
            platform: 'IPHONE',
            signedInAt: new Date(session.signedInAt).toISOString(),
            lastActiveAt: new Date(session.lastActiveAt).toISOString(),
            current: key === token,
          })),
      )
    }

    if (path.startsWith('/admin/registration-codes')) {
      if (me.role !== 'ADMIN') return fail(403, 'ACCESS_DENIED')
      if (method === 'GET') return json(this.codes)
      const settings = body() as Schemas['RegistrationCodeSettings']
      if (method === 'POST') {
        this.addCode(`K7XM-P${String(this.codes.length).padStart(3, '0')}`, settings.label)
        const created = this.codes.at(-1)
        if (created === undefined) return fail(500, 'INTERNAL_ERROR')
        Object.assign(created, settings, { state: settings.active ? 'USABLE' : 'INACTIVE' })
        this.record(me, 'REGISTRATION_CODE_CREATED', settings.label)
        return json(created, 201)
      }
      const code = this.codes.find((candidate) => path.endsWith(candidate.id))
      if (code === undefined) return fail(404, 'NOT_FOUND')
      Object.assign(code, settings, { state: settings.active ? 'USABLE' : 'INACTIVE' })
      this.record(me, 'REGISTRATION_CODE_UPDATED', code.label)
      return json(code)
    }
    if (path.startsWith('/admin/')) {
      if (ROLES.indexOf(me.role) < ROLES.indexOf('BOARD')) return fail(403, 'ACCESS_DENIED')
      if (path === '/admin/audit') return json({ entries: this.audit })
      if (path === '/admin/members') return json(this.members.map((member) => this.summary(member)))
      const parts = path.split('/')
      const id = parts[3]
      const action = parts.at(4)
      const member = this.members.find((candidate) => candidate.id === id)
      if (member === undefined) return fail(404, 'NOT_FOUND')
      if (action === undefined) return json(this.detail(me, member))
      if (action === 'role') {
        const { role } = body() as { role: Role }
        if (!this.detail(me, member).permissions.assignableRoles.includes(role))
          return fail(403, 'ACCESS_DENIED')
        this.record(me, 'MEMBER_ROLE_CHANGED', member.nickname, member.role, role)
        member.role = role
        return json(this.detail(me, member))
      }
      if (action === 'status') {
        const { status } = body() as { status: FakeMember['status'] }
        member.status = status
        this.record(
          me,
          status === 'DISABLED' ? 'MEMBER_DISABLED' : 'MEMBER_ENABLED',
          member.nickname,
        )
        return json(this.detail(me, member))
      }
      if (action === 'password-reset-link') {
        const token = `jeton-${this.nextId()}`
        this.resetTokens.set(token, member.id)
        this.record(me, 'PASSWORD_RESET_LINK_CREATED', member.nickname)
        return json({ token, expiresAt: new Date(this.now + DAY).toISOString() })
      }
    }
    return fail(404, 'NOT_FOUND')
  }
}
