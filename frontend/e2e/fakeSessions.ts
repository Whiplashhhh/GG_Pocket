import type { components } from '../src/api/schema'

import type { AscentSource, FakeTopo, TopoAnswer, TopoMember } from './fakeTopo'

type Schemas = components['schemas']
type AscentRequest = Schemas['AscentRequest']
type ReactionKind = Schemas['ReactionCount']['kind']

const REACTION_KINDS: ReactionKind[] = ['FIRE', 'STRONG', 'CLAP', 'GORILLA']

interface FakeSessionComment {
  id: string
  authorId: string
  body: string
  createdAt: string
  deleted: boolean
}

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
  /** Member id → their reaction. */
  reactions: Map<string, ReactionKind>
  comments: FakeSessionComment[]
}

const DAY = 24 * 3600 * 1000

/**
 * Sessions as the backend keeps them (SessionService), close enough for the e2e tests: dates up to
 * 7 days back, idempotent creation by clientRef, friends reading the shared ones (SessionAudience),
 * clean ascents ticking "je sais la faire".
 */
export class FakeSessions implements AscentSource {
  readonly sessions: FakeSession[] = []
  /** No network for the session writes: the requests fail like in a dead zone of the gym. */
  offline = false
  /** Accepted friends with an active account (FakeClub knows both). */
  isFriend: (readerId: string, climberId: string) => boolean = () => false
  /** Active and not opted out of the podium (FakeSocial knows). */
  inRankings: (id: string) => boolean = () => true
  /** Nickname and photo of a member (FakeSocial knows them). */
  memberRef: (id: string) => Schemas['MemberRef'] = (id) => ({ id, nickname: id })

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
    ascents: {
      route: string
      mode?: AscentRequest['mode']
      topped?: boolean
      clean?: boolean
      belayerId?: string
    }[],
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
      reactions: new Map(),
      comments: [],
      ...extra,
      ascents: ascents.map((ascent) => ({
        id: this.nextId(),
        routeId: this.topo.route(ascent.route).id,
        mode: ascent.mode ?? 'TOP_ROPE',
        topped: ascent.topped ?? true,
        clean: ascent.clean ?? false,
        falls: ascent.clean === true || ascent.topped === false ? 0 : 1,
        firstTry: false,
        ...(ascent.belayerId === undefined ? {} : { belayerId: ascent.belayerId }),
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

  /** Ascents counted for the management space: every attempt, every visibility. */
  private attendance(
    on: { session: FakeSession; ascent: FakeSession['ascents'][number] }[],
  ): Schemas['Attendance'] {
    const topped = on.filter(({ ascent }) => ascent.topped).length
    return {
      ascentCount: on.length,
      toppedCount: topped,
      cleanCount: on.filter(({ ascent }) => ascent.clean).length,
      climberCount: new Set(on.map(({ session }) => session.memberId)).size,
      ...(on.length === 0 ? {} : { successPercent: Math.round((100 * topped) / on.length) }),
    }
  }

  /** Spec §8.10: the routes on the wall during the month and those climbed in it. */
  private monthAttendance(me: TopoMember, month: string): Schemas['MonthAttendance'] {
    const inMonth = this.allAscents().filter(({ session }) => session.climbedOn.startsWith(month))
    const routes = this.topo.routes.filter(
      (route) =>
        inMonth.some(({ ascent }) => ascent.routeId === route.id) ||
        (route.openedOn.slice(0, 7) <= month &&
          (route.dismountedAt === undefined || route.dismountedAt.slice(0, 7) >= month)),
    )
    return {
      month,
      total: this.attendance(inMonth),
      routes: routes.map((route) => ({
        route: this.topo.summary(me, route),
        attendance: this.attendance(inMonth.filter(({ ascent }) => ascent.routeId === route.id)),
      })),
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

  /**
   * RankingService, counted live for every month: the fake has no closure (a frozen month counts
   * the same, its sessions no longer change).
   */
  private ranking(me: TopoMember, month: string): Schemas['Ranking'] {
    const days = new Map<string, Set<string>>()
    const topped = new Map<string, number>()
    for (const session of this.sessions) {
      if (!session.climbedOn.startsWith(month) || !this.inRankings(session.memberId)) continue
      days.set(session.memberId, (days.get(session.memberId) ?? new Set()).add(session.climbedOn))
      topped.set(
        session.memberId,
        (topped.get(session.memberId) ?? 0) + session.ascents.filter((a) => a.topped).length,
      )
    }
    const board = (scores: Map<string, number>): Schemas['RankingBoard'] => {
      const sorted = [...scores.entries()]
        .filter(([, value]) => value > 0)
        .map(([id, value]) => ({ member: this.memberRef(id), value }))
        .sort(
          (a, b) =>
            b.value - a.value ||
            a.member.nickname.localeCompare(b.member.nickname, 'fr', { sensitivity: 'accent' }),
        )
      const lines: Schemas['RankingLine'][] = []
      sorted.forEach((line, index) => {
        const previous = index > 0 ? lines[index - 1] : undefined
        const rank = previous?.value === line.value ? previous.rank : index + 1
        lines.push({ ...line, rank })
      })
      const mine = lines.find((line) => line.member.id === me.id)
      return { lines, ...(mine === undefined ? {} : { me: mine }) }
    }
    const status = this.rankingStatus(month)
    const next = new Date(`${month}-01T12:00:00Z`)
    next.setUTCMonth(next.getUTCMonth() + 1)
    return {
      month,
      status,
      ...(status === 'FROZEN'
        ? {}
        : { freezesAt: `${next.toISOString().slice(0, 7)}-08T03:00:00Z` }),
      appearsInRankings: this.inRankings(me.id),
      sessions: board(new Map([...days.entries()].map(([id, set]) => [id, set.size]))),
      ascents: board(topped),
    }
  }

  /** RankingStatus: the previous month freezes on the 8th at 04:00 (spec §9.2). */
  private rankingStatus(month: string): Schemas['Ranking']['status'] {
    const today = this.day()
    if (month === today.slice(0, 7)) return 'LIVE'
    const next = new Date(`${month}-01T12:00:00Z`)
    next.setUTCMonth(next.getUTCMonth() + 1)
    const isLastMonth = next.toISOString().slice(0, 7) === today.slice(0, 7)
    return isLastMonth && today.slice(8, 10) < '08' ? 'PROVISIONAL' : 'FROZEN'
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
  private logbook(
    me: TopoMember,
    climberId: string = me.id,
    sharedOnly = false,
  ): Schemas['Logbook'] {
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
    const mine = this.allAscents().filter(
      ({ session }) =>
        session.memberId === climberId && (!sharedOnly || session.visibility === 'FRIENDS'),
    )
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

  /** A reaction given before the test. */
  react(member: TopoMember, session: FakeSession, kind: ReactionKind): void {
    session.reactions.set(member.id, kind)
  }

  /** A comment written before the test, `minutesAgo` minutes ago. */
  comment(member: TopoMember, session: FakeSession, body: string, minutesAgo = 5): void {
    session.comments.push({
      id: this.nextId(),
      authorId: member.id,
      body,
      createdAt: new Date(this.clock() - minutesAgo * 60_000).toISOString(),
      deleted: false,
    })
  }

  /** The visible comments on sessions, newest first (the moderation list). */
  latestComments(): Schemas['ModeratedComment'][] {
    return this.sessions
      .flatMap((session) =>
        session.comments
          .filter((comment) => !comment.deleted)
          .map((comment) => ({
            id: comment.id,
            kind: 'SESSION' as const,
            author: this.memberRef(comment.authorId),
            body: comment.body,
            createdAt: comment.createdAt,
            climber: this.memberRef(session.memberId),
            climbedOn: session.climbedOn,
          })),
      )
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  }

  private readable(me: TopoMember, session: FakeSession): boolean {
    return (
      session.memberId === me.id ||
      (session.visibility === 'FRIENDS' && this.isFriend(me.id, session.memberId))
    )
  }

  private reactions(me: TopoMember, session: FakeSession): Schemas['SessionReactions'] {
    const mine = session.reactions.get(me.id)
    return {
      counts: REACTION_KINDS.map((kind) => ({
        kind,
        count: [...session.reactions.values()].filter((given) => given === kind).length,
      })),
      ...(mine === undefined ? {} : { mine }),
    }
  }

  private commentView(me: TopoMember, comment: FakeSessionComment): Schemas['SessionComment'] {
    return {
      id: comment.id,
      author: this.memberRef(comment.authorId),
      body: comment.body,
      createdAt: comment.createdAt,
      deletable: comment.authorId === me.id || me.role === 'BOARD' || me.role === 'ADMIN',
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
      memberId: session.memberId,
      author: this.memberRef(session.memberId),
      climbedOn: session.climbedOn,
      ...(session.feeling === undefined ? {} : { feeling: session.feeling }),
      ...(session.comment === undefined ? {} : { comment: session.comment }),
      ...(session.photo ? { photoUrl: `/api/v1/media/${session.id}` } : {}),
      visibility: session.visibility,
      createdAt: session.createdAt,
      editable: session.memberId === me.id && !session.frozen,
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
        ...(ascent.belayerId === undefined ? {} : { belayer: this.memberRef(ascent.belayerId) }),
      })),
      reactions: this.reactions(me, session),
      commentCount: session.comments.filter((comment) => !comment.deleted).length,
      badges: [],
      unlockedBadges: [],
      authorBadges: [],
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

    if (pathname === '/feed' && method === 'GET') {
      const all = this.sessions
        .filter(
          (session) =>
            session.memberId === me.id ||
            (session.visibility === 'FRIENDS' && this.isFriend(me.id, session.memberId)),
        )
        .sort(
          (a, b) =>
            b.climbedOn.localeCompare(a.climbedOn) || b.createdAt.localeCompare(a.createdAt),
        )
      const start = Number(url.searchParams.get('cursor') ?? '0')
      const size = Number(url.searchParams.get('size') ?? '10')
      return json({
        items: all.slice(start, start + size).map((session) => this.view(me, session)),
        ...(start + size < all.length ? { nextCursor: String(start + size) } : {}),
      })
    }
    const deleteComment = /^\/session-comments\/([^/]+)$/.exec(pathname)
    if (deleteComment !== null && method === 'DELETE') {
      const session = this.sessions.find((candidate) =>
        candidate.comments.some((comment) => comment.id === deleteComment[1] && !comment.deleted),
      )
      const comment = session?.comments.find((candidate) => candidate.id === deleteComment[1])
      if (comment === undefined || !this.commentView(me, comment).deletable) {
        return problem(404, 'NOT_FOUND')
      }
      comment.deleted = true
      return empty
    }
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
    const friendMatch = /^\/members\/([^/]+)\/(sessions|logbook)$/.exec(pathname)
    if (friendMatch !== null && method === 'GET') {
      const [, climberId = '', what] = friendMatch
      const mine = climberId === me.id
      if (!mine && !this.isFriend(me.id, climberId)) return problem(404, 'NOT_FOUND')
      if (what === 'logbook') return json(this.logbook(me, climberId, !mine))
      const all = this.ordered(climberId).filter(
        (session) => mine || session.visibility === 'FRIENDS',
      )
      const start = Number(url.searchParams.get('cursor') ?? '0')
      const size = Number(url.searchParams.get('size') ?? '10')
      return json({
        items: all.slice(start, start + size).map((session) => this.view(me, session)),
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
    if (pathname.startsWith('/stats/') && me.role === 'CLIMBER') {
      return problem(403, 'ACCESS_DENIED')
    }
    if (pathname === '/stats/attendance' && method === 'GET') {
      const month = url.searchParams.get('month') ?? this.day().slice(0, 7)
      if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || month > this.day().slice(0, 7)) {
        return problem(400, 'VALIDATION_FAILED')
      }
      return json(this.monthAttendance(me, month))
    }
    if (pathname === '/stats/routes' && method === 'GET') {
      const all = this.allAscents()
      return json(
        this.topo.routes
          .filter((route) => route.status !== 'DISMOUNTED')
          .map((route) => ({
            route: this.topo.summary(me, route),
            attendance: this.attendance(all.filter(({ ascent }) => ascent.routeId === route.id)),
          })),
      )
    }
    if (pathname === '/rankings' && method === 'GET') {
      const month = url.searchParams.get('month') ?? this.day().slice(0, 7)
      if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month) || month > this.day().slice(0, 7)) {
        return problem(400, 'VALIDATION_FAILED')
      }
      return json(this.ranking(me, month))
    }
    if (pathname === '/rankings/months' && method === 'GET') {
      const current = this.day().slice(0, 7)
      const first = this.sessions.reduce(
        (oldest, session) => (session.climbedOn < oldest ? session.climbedOn : oldest),
        current,
      )
      const months: Schemas['RankingMonth'][] = []
      const cursor = new Date(`${current}-01T12:00:00Z`)
      while (cursor.toISOString().slice(0, 7) >= first.slice(0, 7)) {
        const month = cursor.toISOString().slice(0, 7)
        months.push({ month, status: this.rankingStatus(month) })
        cursor.setUTCMonth(cursor.getUTCMonth() - 1)
      }
      return json(months)
    }
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
        reactions: new Map(),
        comments: [],
      }
      this.apply(session, sent)
      this.sessions.push(session)
      this.refreshChecks(me.id)
      return json(this.view(me, session), 201)
    }

    if (pathname === '/me/belays' && method === 'GET') {
      const all = this.sessions
        .filter((session) => session.ascents.some((ascent) => ascent.belayerId === me.id))
        .sort(
          (a, b) =>
            b.climbedOn.localeCompare(a.climbedOn) || b.createdAt.localeCompare(a.createdAt),
        )
      const start = Number(url.searchParams.get('cursor') ?? '0')
      const size = Number(url.searchParams.get('size') ?? '20')
      return json({
        items: all.slice(start, start + size).map((session) => ({
          sessionId: session.id,
          climber: this.memberRef(session.memberId),
          climbedOn: session.climbedOn,
          routeCount: session.ascents.filter((ascent) => ascent.belayerId === me.id).length,
          readable: this.readable(me, session),
        })),
        ...(start + size < all.length ? { nextCursor: String(start + size) } : {}),
      })
    }
    // Named or not is all that counts: the belayer may not read the session (D-101).
    const belay = /^\/sessions\/([^/]+)\/belayer$/.exec(pathname)
    if (belay !== null && method === 'DELETE') {
      const named = this.sessions
        .find((candidate) => candidate.id === belay[1])
        ?.ascents.filter((ascent) => ascent.belayerId === me.id)
      if (named === undefined || named.length === 0) return problem(404, 'NOT_FOUND')
      for (const ascent of named) delete ascent.belayerId
      return empty
    }

    const match = /^\/sessions\/([^/]+)(\/photo|\/reaction|\/comments)?$/.exec(pathname)
    if (match === null) return undefined
    const [, id, part] = match as (string | undefined)[]
    const found = this.sessions.find((candidate) => candidate.id === id)
    if (found === undefined || !this.readable(me, found)) return problem(404, 'NOT_FOUND')
    if (part === '/reaction') {
      if (method === 'PUT') {
        if (found.memberId === me.id) return problem(400, 'VALIDATION_FAILED')
        found.reactions.set(me.id, (body as Schemas['SessionReactionRequest']).kind)
      } else {
        found.reactions.delete(me.id)
      }
      return json(this.reactions(me, found))
    }
    if (part === '/comments') {
      if (method === 'GET') {
        return json(
          found.comments
            .filter((comment) => !comment.deleted)
            .map((comment) => this.commentView(me, comment)),
        )
      }
      const text = (body as Schemas['SessionCommentRequest']).body.trim()
      if (text === '' || text.length > 500) return problem(400, 'VALIDATION_FAILED')
      const comment: FakeSessionComment = {
        id: this.nextId(),
        authorId: me.id,
        body: text,
        createdAt: new Date(this.clock()).toISOString(),
        deleted: false,
      }
      found.comments.push(comment)
      return json(this.commentView(me, comment), 201)
    }
    const photo = part
    if (method === 'GET' && photo === undefined) return json(this.view(me, found))
    if (found.memberId !== me.id) return problem(404, 'NOT_FOUND')
    const session = found
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
