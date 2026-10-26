import type { components } from '../src/api/schema'

import type { AscentSource, FakeTopo, TopoAnswer, TopoMember } from './fakeTopo'

type Schemas = components['schemas']
type AscentRequest = Schemas['AscentRequest']

interface FakeSession {
  id: string
  memberId: string
  clientRef: string
  climbedOn: string
  feeling?: number
  comment?: string
  visibility: Schemas['Session']['visibility']
  photo: boolean
  createdAt: string
  /** A month already frozen (spec §9.2): read-only. */
  frozen: boolean
  ascents: (AscentRequest & { id: string })[]
}

const DAY = 24 * 3600 * 1000

/**
 * Sessions as the backend keeps them (SessionService), close enough for the e2e tests: dates up to
 * 7 days back, idempotent creation by clientRef, my sessions only, clean ascents ticking "je sais
 * la faire".
 */
export class FakeSessions implements AscentSource {
  readonly sessions: FakeSession[] = []
  /** No network for the session writes: the requests fail like in a dead zone of the gym. */
  offline = false

  constructor(
    private readonly nextId: () => string,
    private readonly clock: () => number,
    private readonly topo: FakeTopo,
  ) {
    topo.ascents = this
  }

  private day(offset = 0): string {
    return new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Paris' }).format(
      this.clock() + offset * DAY,
    )
  }

  /** A session logged before the test, `daysBack` days ago. */
  add(
    member: TopoMember,
    daysBack: number,
    ascents: { route: string; mode?: AscentRequest['mode']; topped?: boolean; clean?: boolean }[],
    extra: Partial<Pick<FakeSession, 'comment' | 'feeling' | 'frozen' | 'visibility'>> = {},
  ): FakeSession {
    const session: FakeSession = {
      id: this.nextId(),
      memberId: member.id,
      clientRef: this.nextId(),
      climbedOn: this.day(-daysBack),
      visibility: 'FRIENDS',
      photo: false,
      createdAt: new Date(this.clock() - daysBack * DAY).toISOString(),
      frozen: false,
      ...extra,
      ascents: ascents.map((ascent) => ({
        id: this.nextId(),
        routeId: this.topo.route(ascent.route).id,
        mode: ascent.mode ?? 'TOP_ROPE',
        topped: ascent.topped ?? true,
        clean: ascent.clean ?? false,
        falls: ascent.clean === true || ascent.topped === false ? 0 : 1,
        firstTry: false,
      })),
    }
    this.sessions.push(session)
    this.refreshChecks(member.id)
    return session
  }

  toppedCount(routeId: string): number {
    return this.allAscents().filter(({ ascent }) => ascent.routeId === routeId && ascent.topped)
      .length
  }

  stats(routeId: string): Schemas['RouteStats'] {
    const on = this.allAscents().filter(({ ascent }) => ascent.routeId === routeId)
    const lead = on.filter(({ ascent }) => ascent.mode === 'LEAD').length
    return {
      ascentCount: on.length,
      toppedCount: on.filter(({ ascent }) => ascent.topped).length,
      climberCount: new Set(on.map(({ session }) => session.memberId)).size,
      ...(on.length === 0 ? {} : { leadPercent: Math.round((100 * lead) / on.length) }),
    }
  }

  mine(memberId: string, routeId: string): Schemas['MyRouteAscent'][] {
    return this.ordered(memberId)
      .flatMap((session) => [...session.ascents].reverse().map((ascent) => ({ session, ascent })))
      .filter(({ ascent }) => ascent.routeId === routeId)
      .map(({ session, ascent }) => ({
        sessionId: session.id,
        climbedOn: session.climbedOn,
        mode: ascent.mode,
        topped: ascent.topped,
        clean: ascent.clean,
        falls: ascent.falls,
        firstTry: ascent.firstTry,
      }))
  }

  hasClean(memberId: string, routeId: string, mode: AscentRequest['mode']): boolean {
    return this.allAscents().some(
      ({ session, ascent }) =>
        session.memberId === memberId &&
        ascent.routeId === routeId &&
        ascent.mode === mode &&
        ascent.clean,
    )
  }

  /** TodoService: every route on the wall, lead known means top rope known. */
  private todo(me: TopoMember): Schemas['Todo'] {
    const state = (routeId: string, mode: AscentRequest['mode']): Schemas['TodoRoute']['lead'] => {
      if (this.hasClean(me.id, routeId, mode)) return 'FROM_ASCENT'
      return this.topo.checks.has(`${me.id}:${routeId}:${mode}`) ? 'CHECKED' : 'TODO'
    }
    const routes = this.topo.routes
      .filter((route) => route.status !== 'DISMOUNTED')
      .map((route) => {
        const lead = state(route.id, 'LEAD')
        const topRope = lead === 'TODO' ? state(route.id, 'TOP_ROPE') : 'FROM_LEAD'
        return { route: this.topo.summary(me, route), topRope, lead }
      })
      .sort((a, b) => a.route.gradeRank - b.route.gradeRank)
    return {
      routeCount: routes.length,
      topRopeKnown: routes.filter((item) => item.topRope !== 'TODO').length,
      leadKnown: routes.filter((item) => item.lead !== 'TODO').length,
      routes,
    }
  }

  /** PlanningService, Streak and MonthTotals. */
  private planning(me: TopoMember, month: string): Schemas['Planning'] {
    const mine = this.ordered(me.id).reverse()
    const inMonth = mine.filter((session) => session.climbedOn.startsWith(month))
    const days = new Map<string, Schemas['PlanningSession'][]>()
    for (const session of inMonth) {
      const view = this.view(me, session)
      days.set(session.climbedOn, [
        ...(days.get(session.climbedOn) ?? []),
        {
          id: view.id,
          ...(view.feeling === undefined ? {} : { feeling: view.feeling }),
          summary: view.summary,
        },
      ])
    }
    const views = inMonth.map((session) => this.view(me, session))
    const hardest = views
      .flatMap((view) => view.ascents.filter((ascent) => ascent.topped))
      .sort((a, b) => b.route.gradeRank - a.route.gradeRank)
      .at(0)
    const feelings = inMonth.flatMap((session) =>
      session.feeling === undefined ? [] : [session.feeling],
    )
    const monday = (day: string) => {
      const date = new Date(`${day}T12:00:00Z`)
      date.setUTCDate(date.getUTCDate() - ((date.getUTCDay() + 6) % 7))
      return date.toISOString().slice(0, 10)
    }
    const perWeek = new Map<string, Set<string>>()
    for (const session of mine) {
      const week = monday(session.climbedOn)
      perWeek.set(week, (perWeek.get(week) ?? new Set()).add(session.climbedOn))
    }
    const daysIn = (week: string) => perWeek.get(week)?.size ?? 0
    const previous = (week: string) => {
      const date = new Date(`${week}T12:00:00Z`)
      date.setUTCDate(date.getUTCDate() - 7)
      return date.toISOString().slice(0, 10)
    }
    let week = monday(this.day())
    const currentWeekDays = daysIn(week)
    if (currentWeekDays < 2) week = previous(week)
    let weeks = 0
    while (daysIn(week) >= 2) {
      weeks++
      week = previous(week)
    }
    return {
      month,
      frozen: inMonth.some((session) => session.frozen),
      days: [...days.entries()].map(([date, sessions]) => ({ date, sessions })),
      totals: {
        climbingDays: days.size,
        sessionCount: inMonth.length,
        toppedCount: views.reduce((sum, view) => sum + view.summary.toppedCount, 0),
        ...(hardest === undefined
          ? {}
          : { maxGrade: hardest.route.grade, maxGradeRank: hardest.route.gradeRank }),
        ...(feelings.length === 0
          ? {}
          : {
              averageFeeling:
                Math.round((feelings.reduce((a, b) => a + b, 0) / feelings.length) * 10) / 10,
            }),
      },
      streak: { weeks, minDaysPerWeek: 2, currentWeekDays },
    }
  }

  /** LogbookService and Logbook. */
  private logbook(me: TopoMember): Schemas['Logbook'] {
    const BEST = [
      'LEAD_CLEAN',
      'LEAD_TOPPED',
      'TOP_ROPE_CLEAN',
      'TOP_ROPE_TOPPED',
      'TRIED',
    ] as const
    type Best = (typeof BEST)[number]
    const bestOf = (ascent: AscentRequest): Best => {
      if (!ascent.topped) return 'TRIED'
      if (ascent.mode === 'LEAD') return ascent.clean ? 'LEAD_CLEAN' : 'LEAD_TOPPED'
      return ascent.clean ? 'TOP_ROPE_CLEAN' : 'TOP_ROPE_TOPPED'
    }
    const mine = this.allAscents().filter(({ session }) => session.memberId === me.id)
    const byRoute = new Map<string, typeof mine>()
    for (const item of mine) {
      byRoute.set(item.ascent.routeId, [...(byRoute.get(item.ascent.routeId) ?? []), item])
    }
    const routes = [...byRoute.entries()].map(([routeId, items]) => {
      const route = this.topo.routes.find((candidate) => candidate.id === routeId)
      if (route === undefined) throw new Error(`No route ${routeId}`)
      const days = items.map(({ session }) => session.climbedOn).sort()
      const best = items
        .map(({ ascent }) => bestOf(ascent))
        .sort((a, b) => BEST.indexOf(a) - BEST.indexOf(b))[0]
      return {
        route: this.topo.summary(me, route),
        ascentCount: items.length,
        toppedCount: items.filter(({ ascent }) => ascent.topped).length,
        best,
        firstClimbedOn: days[0],
        lastClimbedOn: days.at(-1) as string,
      }
    })
    routes.sort(
      (a, b) =>
        b.lastClimbedOn.localeCompare(a.lastClimbedOn) || b.route.gradeRank - a.route.gradeRank,
    )
    const topped = mine.filter(({ ascent }) => ascent.topped)
    const gradeOf = (routeId: string) => routes.find((line) => line.route.id === routeId)?.route
    const hardest = (items: typeof mine) =>
      items
        .map(({ ascent }) => gradeOf(ascent.routeId))
        .sort((a, b) => (b?.gradeRank ?? 0) - (a?.gradeRank ?? 0))
        .at(0)
    const max = hardest(topped)
    const maxLead = hardest(topped.filter(({ ascent }) => ascent.mode === 'LEAD'))
    const grades = new Map<number, Schemas['LogbookGrade']>()
    for (const line of routes.filter((line) => line.best !== 'TRIED')) {
      const entry = grades.get(line.route.gradeRank) ?? {
        grade: line.route.grade,
        gradeRank: line.route.gradeRank,
        topRope: 0,
        lead: 0,
      }
      if (line.best.startsWith('LEAD')) entry.lead++
      else entry.topRope++
      grades.set(line.route.gradeRank, entry)
    }
    return {
      totals: {
        sessionCount: new Set(mine.map(({ session }) => session.id)).size,
        climbingDays: new Set(mine.map(({ session }) => session.climbedOn)).size,
        ascentCount: mine.length,
        toppedCount: topped.length,
        leadToppedCount: topped.filter(({ ascent }) => ascent.mode === 'LEAD').length,
        cleanCount: mine.filter(({ ascent }) => ascent.clean).length,
        routeCount: routes.length,
        ...(max === undefined ? {} : { maxGrade: max.grade, maxGradeRank: max.gradeRank }),
        ...(maxLead === undefined
          ? {}
          : { maxLeadGrade: maxLead.grade, maxLeadGradeRank: maxLead.gradeRank }),
      },
      grades: [...grades.values()].sort((a, b) => b.gradeRank - a.gradeRank),
      routes,
    }
  }

  private allAscents() {
    return this.sessions.flatMap((session) =>
      session.ascents.map((ascent) => ({ session, ascent })),
    )
  }

  /** Mine, latest day first, then latest saved first. */
  private ordered(memberId: string): FakeSession[] {
    return this.sessions
      .filter((session) => session.memberId === memberId)
      .sort(
        (a, b) => b.climbedOn.localeCompare(a.climbedOn) || b.createdAt.localeCompare(a.createdAt),
      )
  }

  /** Clean ascents tick the route in their mode. */
  private refreshChecks(memberId: string) {
    for (const { session, ascent } of this.allAscents()) {
      if (session.memberId === memberId && ascent.clean) {
        this.topo.checks.add(`${memberId}:${ascent.routeId}:${ascent.mode}`)
      }
    }
  }

  private view(me: TopoMember, session: FakeSession): Schemas['Session'] {
    const ascents = session.ascents.map((ascent) => {
      const route = this.topo.routes.find((candidate) => candidate.id === ascent.routeId)
      if (route === undefined) throw new Error(`No route ${ascent.routeId}`)
      return { ascent, route: this.topo.summary(me, route) }
    })
    const topped = ascents.filter(({ ascent }) => ascent.topped)
    const hardest = topped
      .map(({ route }) => route)
      .sort((a, b) => b.gradeRank - a.gradeRank)
      .at(0)
    return {
      id: session.id,
      climbedOn: session.climbedOn,
      ...(session.feeling === undefined ? {} : { feeling: session.feeling }),
      ...(session.comment === undefined ? {} : { comment: session.comment }),
      ...(session.photo ? { photoUrl: `/api/v1/media/${session.id}` } : {}),
      visibility: session.visibility,
      createdAt: session.createdAt,
      editable: !session.frozen,
      summary: {
        ascentCount: ascents.length,
        toppedCount: topped.length,
        leadCount: ascents.filter(({ ascent }) => ascent.mode === 'LEAD').length,
        ...(hardest === undefined
          ? {}
          : { maxGrade: hardest.grade, maxGradeRank: hardest.gradeRank }),
      },
      ascents: ascents.map(({ ascent, route }) => ({
        id: ascent.id,
        route,
        mode: ascent.mode,
        topped: ascent.topped,
        clean: ascent.clean,
        falls: ascent.falls,
        firstTry: ascent.firstTry,
        ...(ascent.feeling === undefined ? {} : { feeling: ascent.feeling }),
      })),
    }
  }

  /** The checks of SessionService, in the order it runs them. */
  private refusal(sent: Schemas['SessionRequest'], dateChanged: boolean): TopoAnswer | undefined {
    const problem = (status: number, code: string): TopoAnswer => ({
      kind: 'problem',
      status,
      code,
    })
    if (!Array.isArray(sent.ascents) || sent.ascents.length === 0 || sent.ascents.length > 50) {
      return problem(400, 'VALIDATION_FAILED')
    }
    if (sent.ascents.some((ascent) => ascent.clean && (!ascent.topped || ascent.falls > 0))) {
      return problem(400, 'INVALID_ASCENT')
    }
    if (dateChanged && sent.climbedOn > this.day()) return problem(400, 'DATE_IN_THE_FUTURE')
    if (dateChanged && sent.climbedOn < this.day(-7)) return problem(400, 'SESSION_TOO_OLD')
    if (sent.ascents.some((ascent) => !this.topo.routes.some((r) => r.id === ascent.routeId))) {
      return problem(404, 'NOT_FOUND')
    }
    return undefined
  }

  private apply(session: FakeSession, sent: Schemas['SessionRequest']) {
    session.climbedOn = sent.climbedOn
    session.visibility = sent.visibility
    if (sent.feeling === undefined) delete session.feeling
    else session.feeling = sent.feeling
    const comment = sent.comment?.trim()
    if (comment === undefined || comment === '') delete session.comment
    else session.comment = comment
    session.ascents = sent.ascents.map((ascent) => ({ ...ascent, id: this.nextId() }))
  }

  handle(me: TopoMember, method: string, path: string, body: unknown): TopoAnswer | undefined {
    const json = (data: unknown, status?: number): TopoAnswer => ({ kind: 'json', data, status })
    const empty: TopoAnswer = { kind: 'empty' }
    const problem = (status: number, code: string): TopoAnswer => ({
      kind: 'problem',
      status,
      code,
    })
    const url = new URL(path, 'http://fake')
    const pathname = url.pathname

    if (pathname === '/me/sessions' && method === 'GET') {
      const all = this.ordered(me.id)
      const start = Number(url.searchParams.get('cursor') ?? '0')
      const size = Number(url.searchParams.get('size') ?? '10')
      const page = all.slice(start, start + size)
      return json({
        items: page.map((session) => this.view(me, session)),
        ...(start + size < all.length ? { nextCursor: String(start + size) } : {}),
      })
    }
    if (pathname === '/me/climbed-routes' && method === 'GET') {
      const last = new Map<string, string>()
      for (const session of this.ordered(me.id).reverse()) {
        for (const ascent of session.ascents) last.set(ascent.routeId, session.climbedOn)
      }
      return json(
        [...last.entries()]
          .sort((a, b) => b[1].localeCompare(a[1]))
          .map(([routeId, lastClimbedOn]) => ({ routeId, lastClimbedOn })),
      )
    }
    if (pathname === '/me/todo' && method === 'GET') return json(this.todo(me))
    if (pathname === '/me/logbook' && method === 'GET') return json(this.logbook(me))
    if (pathname === '/me/planning' && method === 'GET') {
      const month = url.searchParams.get('month') ?? this.day().slice(0, 7)
      if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || month > this.day().slice(0, 7)) {
        return problem(400, 'VALIDATION_FAILED')
      }
      return json(this.planning(me, month))
    }
    if (pathname === '/sessions' && method === 'POST') {
      const sent = body as Schemas['SessionRequest']
      const existing = this.sessions.find(
        (session) => session.memberId === me.id && session.clientRef === sent.clientRef,
      )
      if (existing !== undefined) return json(this.view(me, existing))
      const refused = this.refusal(sent, true)
      if (refused !== undefined) return refused
      const session: FakeSession = {
        id: this.nextId(),
        memberId: me.id,
        clientRef: sent.clientRef,
        climbedOn: sent.climbedOn,
        visibility: sent.visibility,
        photo: false,
        createdAt: new Date(this.clock()).toISOString(),
        frozen: false,
        ascents: [],
      }
      this.apply(session, sent)
      this.sessions.push(session)
      this.refreshChecks(me.id)
      return json(this.view(me, session), 201)
    }

    const match = /^\/sessions\/([^/]+)(\/photo)?$/.exec(pathname)
    if (match === null) return undefined
    const [, id, photo] = match as (string | undefined)[]
    const session = this.sessions.find(
      (candidate) => candidate.id === id && candidate.memberId === me.id,
    )
    if (session === undefined) return problem(404, 'NOT_FOUND')
    if (method === 'GET' && photo === undefined) return json(this.view(me, session))
    if (session.frozen) return problem(409, 'MONTH_FROZEN')
    if (photo !== undefined) {
      session.photo = method === 'PUT'
      return method === 'PUT' ? json(this.view(me, session)) : empty
    }
    if (method === 'PUT') {
      const sent = body as Schemas['SessionRequest']
      const refused = this.refusal(sent, sent.climbedOn !== session.climbedOn)
      if (refused !== undefined) return refused
      this.apply(session, sent)
      this.refreshChecks(me.id)
      return json(this.view(me, session))
    }
    if (method === 'DELETE') {
      this.sessions.splice(this.sessions.indexOf(session), 1)
      return empty
    }
    return undefined
  }
}
