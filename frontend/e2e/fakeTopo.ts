import type { components } from '../src/api/schema'

type Schemas = components['schemas']
type Role = Schemas['CurrentMember']['role']
type RouteStyle = Schemas['RouteSummary']['styles'][number]
type ClimbMode = Schemas['RouteSummary']['checks'][number]

/** The member asking, as the topo needs to know them. */
export interface TopoMember {
  id: string
  nickname: string
  role: Role
  avatarUrl?: string
  favoriteRouteId?: string
}

export interface FakeRoute {
  id: string
  name: string
  grade: string
  cordNumber: number
  /** Hold colour labels, as in the configuration. */
  colors: string[]
  status: Schemas['RouteSummary']['status']
  openedOn: string
  plannedDismountOn?: string
  dismountedAt?: string
  styles: RouteStyle[]
  setterNote?: string
  advice?: string
  photo: boolean
  /** Hearts of members who are not part of the test. */
  othersFavorites: number
}

interface FakeComment {
  id: string
  routeId: string
  author: Schemas['MemberRef']
  body: string
  createdAt: string
  deleted: boolean
}

/** What a handler answers: a body (status 200 unless said), or 204, or a Problem Details code. */
export type TopoAnswer =
  | { kind: 'json'; data: unknown; status?: number }
  | { kind: 'empty' }
  | { kind: 'problem'; status: number; code: string }

const SECTORS: [string, number[]][] = [
  ['Neutre 8 m', [1, 2, 3]],
  ['Neutre 12 m', [4, 5, 6]],
  ['Positif', [7, 8, 9]],
  ['Négatif et ses 2 dièdres', [10, 11, 12, 13, 14]],
  ['Grand dévers', [15, 16, 17]],
  ['Cheminée', [18, 19]],
  ['Grand toit', [20, 21]],
  ['Sous toit', [22, 23, 24]],
  ['Petit dévers', [25, 26, 27]],
  ['Petit toit', [28, 29]],
]

const COLORS: [string, string][] = [
  ['jaune', '#F5C518'],
  ['orange', '#F28C28'],
  ['rouge', '#D7263D'],
  ['rose', '#F06EAA'],
  ['violet', '#7B4FB8'],
  ['bleu', '#1F5FBF'],
  ['bleu clair', '#5BC0EB'],
  ['vert', '#2E9E44'],
  ['vert clair', '#9BD34A'],
  ['noir', '#1A1A1A'],
  ['blanc', '#F7F7F2'],
  ['gris', '#9A9A9A'],
  ['marron', '#7A4B2A'],
  ['multicolore', '#808080'],
]

const GRADES = [
  '4a',
  '4a+',
  '4b',
  '4b+',
  '4c',
  '4c+',
  '5a',
  '5a+',
  '5b',
  '5b+',
  '5c',
  '5c+',
  '6a',
  '6a+',
  '6b',
  '6b+',
  '6c',
  '6c+',
  '7a',
  '7a+',
  '7b',
  '7b+',
  '7c',
]

type Seed = Omit<FakeRoute, 'id' | 'photo' | 'othersFavorites' | 'status' | 'openedOn'> &
  Partial<Pick<FakeRoute, 'photo' | 'othersFavorites' | 'status' | 'openedOn'>>

/** A dozen routes in the spirit of the club: on the wall, in last chance, dismounted. */
const ROUTES: Seed[] = [
  {
    name: 'Bye Bye Morice',
    grade: '6b',
    cordNumber: 12,
    colors: ['jaune', 'bleu'],
    openedOn: '2026-05-16',
    styles: ['CONTINUITY', 'ENDURANCE', 'PHYSICAL', 'COMPLEX'],
    setterNote: 'Sam',
    advice: 'Garde du jus pour la sortie.',
    othersFavorites: 5,
  },
  {
    name: 'OH purée !',
    grade: '6b',
    cordNumber: 1,
    colors: ['jaune'],
    openedOn: '2026-10-02',
    styles: ['TECHNICAL'],
    photo: true,
    othersFavorites: 2,
  },
  {
    name: 'Tonton Talon',
    grade: '6a+',
    cordNumber: 15,
    colors: ['orange', 'noir'],
    status: 'LAST_CHANCE',
    plannedDismountOn: '2026-11-02',
    openedOn: '2026-03-07',
    styles: ['PHYSICAL', 'OVERHANG'],
    setterNote: 'Tonton',
    othersFavorites: 2,
  },
  {
    name: 'Avant-bras en béton',
    grade: '7a',
    cordNumber: 16,
    colors: ['noir'],
    status: 'LAST_CHANCE',
    openedOn: '2026-02-14',
    styles: ['ENDURANCE', 'OVERHANG'],
  },
  { name: 'Réglisse', grade: '6a+', cordNumber: 27, colors: ['violet'], styles: ['SLAB'] },
  { name: 'P’tite Line', grade: '4a', cordNumber: 23, colors: ['blanc'], styles: ['JUGS'] },
  {
    name: 'Le grand toit qui ne pardonne pas les bras de T-Rex',
    grade: '7c',
    cordNumber: 20,
    colors: ['multicolore'],
    styles: ['ROOF', 'PHYSICAL'],
    othersFavorites: 1,
  },
  {
    name: 'Iron Man',
    grade: '6a+',
    cordNumber: 3,
    colors: ['rouge'],
    styles: ['TECHNICAL', 'CRIMPS'],
  },
  {
    name: 'Pied-main party',
    grade: '5c',
    cordNumber: 8,
    colors: ['vert'],
    openedOn: '2026-09-20',
    styles: ['SLAB', 'TECHNICAL'],
  },
  {
    name: 'Du mou !',
    grade: '6c',
    cordNumber: 18,
    colors: ['bleu clair'],
    styles: ['DIHEDRAL', 'COMPLEX'],
  },
  { name: 'Sec !', grade: '5b', cordNumber: 5, colors: ['rose'], styles: ['JUGS'] },
  {
    name: 'Schtroumpfs',
    grade: '5b',
    cordNumber: 26,
    colors: ['bleu'],
    status: 'DISMOUNTED',
    openedOn: '2026-01-10',
    dismountedAt: '2026-09-15T17:00:00Z',
    styles: [],
    othersFavorites: 3,
  },
  {
    name: 'Bloque !',
    grade: '6a',
    cordNumber: 10,
    colors: ['marron'],
    status: 'DISMOUNTED',
    openedOn: '2025-12-01',
    dismountedAt: '2026-10-05T17:00:00Z',
    styles: ['PINCHES'],
  },
]

const ROLES: Role[] = ['CLIMBER', 'SETTER', 'BOARD', 'ADMIN']

/**
 * The topo part of {@link FakeClub} (phase 3): configuration, routes, comments, favourites with
 * their limit, "je sais la faire", last chance acknowledgements and the favourite route.
 */
export class FakeTopo {
  readonly config: Schemas['TopoConfig']
  readonly routes: FakeRoute[] = []
  readonly comments: FakeComment[] = []
  /** Member id → favourite route ids, oldest first. */
  readonly favorites = new Map<string, string[]>()
  readonly checks = new Set<string>()
  readonly acknowledgements = new Set<string>()
  /** Most tests are not about the last chance pop-up: it stays quiet unless asked. */
  quietLastChance = true

  constructor(
    private readonly nextId: () => string,
    private readonly clock: () => number,
  ) {
    const holdColors = COLORS.map(([label, hex], index) => ({
      id: nextId(),
      label,
      hex,
      multicolor: label === 'multicolore',
      active: true,
      position: index + 1,
    }))
    const sectors = SECTORS.map(([name, numbers], index) => {
      const id = nextId()
      return {
        id,
        name,
        position: index + 1,
        cords: numbers.map((number) => ({ id: nextId(), number, sectorId: id, active: true })),
      }
    })
    const grades = GRADES.map((code, index) => ({ id: nextId(), code, rank: index + 1 }))
    this.config = { sectors, holdColors, grades }
    for (const seed of ROUTES) {
      this.routes.push({
        status: 'ACTIVE',
        openedOn: '2026-09-01',
        photo: false,
        othersFavorites: 0,
        ...seed,
        id: nextId(),
      })
    }
    const morice = this.route('Bye Bye Morice')
    this.comments.push({
      id: nextId(),
      routeId: morice.id,
      author: { id: nextId(), nickname: 'tonton.talon' },
      body: 'Le pied-main au troisième point, ça passe tout seul.',
      createdAt: '2026-10-18T19:00:00Z',
      deleted: false,
    })
  }

  route(name: string): FakeRoute {
    const found = this.routes.find((route) => route.name === name)
    if (found === undefined) throw new Error(`No route named ${name}`)
    return found
  }

  /** Hearts a route for a member, as if they had done it before the test. */
  favorite(member: TopoMember, name: string): void {
    const list = this.favorites.get(member.id) ?? []
    list.push(this.route(name).id)
    this.favorites.set(member.id, list)
  }

  check(member: TopoMember, name: string, mode: ClimbMode): void {
    this.checks.add(`${member.id}:${this.route(name).id}:${mode}`)
  }

  private holds(route: FakeRoute): Schemas['RouteHold'][] {
    return route.colors.map((label) => {
      const color = this.config.holdColors.find((candidate) => candidate.label === label)
      if (color === undefined) throw new Error(`No colour ${label}`)
      return { id: color.id, label: color.label, hex: color.hex, multicolor: color.multicolor }
    })
  }

  private summary(me: TopoMember, route: FakeRoute): Schemas['RouteSummary'] {
    const sector = this.config.sectors.find((candidate) =>
      candidate.cords.some((cord) => cord.number === route.cordNumber),
    )
    const grade = this.config.grades.find((candidate) => candidate.code === route.grade)
    if (sector === undefined || grade === undefined) throw new Error(`Bad route ${route.name}`)
    const hearts = [...this.favorites.values()].filter((list) => list.includes(route.id)).length
    return {
      id: route.id,
      name: route.name,
      grade: grade.code,
      gradeRank: grade.rank,
      cordNumber: route.cordNumber,
      sectorId: sector.id,
      sectorName: sector.name,
      sectorPosition: sector.position,
      holds: this.holds(route),
      status: route.status,
      openedOn: route.openedOn,
      ...(route.plannedDismountOn ? { plannedDismountOn: route.plannedDismountOn } : {}),
      ...(route.dismountedAt ? { dismountedAt: route.dismountedAt } : {}),
      ...(route.photo ? { photoUrl: `/api/v1/media/${route.id}` } : {}),
      styles: route.styles,
      favoriteCount: route.othersFavorites + hearts,
      commentCount: this.comments.filter(
        (comment) => comment.routeId === route.id && !comment.deleted,
      ).length,
      favorite: (this.favorites.get(me.id) ?? []).includes(route.id),
      checks: (['TOP_ROPE', 'LEAD'] as const).filter((mode) =>
        this.checks.has(`${me.id}:${route.id}:${mode}`),
      ),
    }
  }

  private detail(me: TopoMember, route: FakeRoute): Schemas['RouteDetail'] {
    const summary = this.summary(me, route)
    const grade = this.config.grades.find((candidate) => candidate.code === route.grade)
    const cord = this.config.sectors
      .flatMap((sector) => sector.cords)
      .find((candidate) => candidate.number === route.cordNumber)
    return {
      route: summary,
      gradeId: grade?.id ?? '',
      cordId: cord?.id ?? '',
      setters: [],
      ...(route.setterNote ? { setterNote: route.setterNote } : {}),
      ...(route.advice ? { advice: route.advice } : {}),
      createdAt: `${route.openedOn}T10:00:00Z`,
    }
  }

  private comment(me: TopoMember, comment: FakeComment): Schemas['RouteComment'] {
    return {
      id: comment.id,
      author: comment.author,
      body: comment.body,
      createdAt: comment.createdAt,
      deletable: comment.author.id === me.id || ROLES.indexOf(me.role) >= ROLES.indexOf('BOARD'),
    }
  }

  /** Answers a topo call, or undefined when the path is not the topo's. */
  handle(me: TopoMember, method: string, path: string, body: unknown): TopoAnswer | undefined {
    const json = (data: unknown, status?: number): TopoAnswer => ({ kind: 'json', data, status })
    const empty: TopoAnswer = { kind: 'empty' }
    const problem = (status: number, code: string): TopoAnswer => ({
      kind: 'problem',
      status,
      code,
    })
    const sent = (body ?? {}) as Record<string, unknown>

    if (path === '/topo/config') return json(this.config)
    if (path === '/routes' && method === 'GET') {
      return json(
        this.routes
          .filter((route) => route.status !== 'DISMOUNTED')
          .map((route) => this.summary(me, route)),
      )
    }
    if (path === '/routes/archive') {
      return json(
        this.routes
          .filter((route) => route.status === 'DISMOUNTED')
          .sort((a, b) => (b.dismountedAt ?? '').localeCompare(a.dismountedAt ?? ''))
          .map((route) => this.summary(me, route)),
      )
    }
    if (path === '/routes/last-chance/unacknowledged') {
      if (this.quietLastChance) return json([])
      return json(
        this.routes
          .filter(
            (route) =>
              route.status === 'LAST_CHANCE' && !this.acknowledgements.has(`${me.id}:${route.id}`),
          )
          .map((route) => this.summary(me, route)),
      )
    }
    if (path === '/routes/last-chance/acknowledgements' && method === 'POST') {
      const ids = sent.routeIds
      if (!Array.isArray(ids) || ids.length === 0) return problem(400, 'VALIDATION_FAILED')
      for (const id of ids) this.acknowledgements.add(`${me.id}:${String(id)}`)
      return empty
    }
    if (path === '/me/favorites') {
      const mine = [...(this.favorites.get(me.id) ?? [])].reverse()
      return json(
        mine
          .map((id) => this.routes.find((route) => route.id === id))
          .filter((route): route is FakeRoute => route !== undefined)
          .map((route) => this.summary(me, route)),
      )
    }
    if (path === '/me/favorite-route') {
      if (method === 'DELETE') {
        delete me.favoriteRouteId
        return empty
      }
      const routeId = sent.routeId
      if (!this.routes.some((route) => route.id === routeId)) return problem(404, 'NOT_FOUND')
      me.favoriteRouteId = String(routeId)
      return empty
    }
    if (path.startsWith('/route-comments/') && method === 'DELETE') {
      const comment = this.comments.find(
        (candidate) => candidate.id === path.split('/')[2] && !candidate.deleted,
      )
      if (comment === undefined) return problem(404, 'NOT_FOUND')
      if (!this.comment(me, comment).deletable) return problem(403, 'ACCESS_DENIED')
      comment.deleted = true
      return empty
    }

    const match = /^\/routes\/([^/]+)(?:\/(comments|favorite|checks)(?:\/([A-Z_]+))?)?$/.exec(path)
    if (match === null) return undefined
    // Optional groups that did not match are undefined, whatever the typings say.
    const [, id, part, mode] = match as (string | undefined)[]
    const route = this.routes.find((candidate) => candidate.id === id)
    if (route === undefined) return problem(404, 'NOT_FOUND')
    const readOnly = route.status === 'DISMOUNTED'

    if (part === undefined && method === 'GET') return json(this.detail(me, route))
    if (part === 'comments') {
      if (method === 'GET') {
        return json(
          this.comments
            .filter((comment) => comment.routeId === route.id && !comment.deleted)
            .map((comment) => this.comment(me, comment)),
        )
      }
      if (readOnly) return problem(409, 'ROUTE_DISMOUNTED')
      const text = typeof sent.body === 'string' ? sent.body.trim() : ''
      if (text.length === 0 || text.length > 500) return problem(400, 'VALIDATION_FAILED')
      const comment: FakeComment = {
        id: this.nextId(),
        routeId: route.id,
        author: {
          id: me.id,
          nickname: me.nickname,
          ...(me.avatarUrl ? { avatarUrl: me.avatarUrl } : {}),
        },
        body: text,
        createdAt: new Date(this.clock()).toISOString(),
        deleted: false,
      }
      this.comments.push(comment)
      return json(this.comment(me, comment), 201)
    }
    if (part === 'favorite') {
      if (readOnly) return problem(409, 'ROUTE_DISMOUNTED')
      const mine = this.favorites.get(me.id) ?? []
      this.favorites.set(me.id, mine)
      if (method === 'DELETE') {
        this.favorites.set(
          me.id,
          mine.filter((favorite) => favorite !== route.id),
        )
        return empty
      }
      if (mine.includes(route.id)) return empty
      const active = mine.filter(
        (favorite) =>
          this.routes.find((candidate) => candidate.id === favorite)?.status !== 'DISMOUNTED',
      )
      if (active.length >= 2) {
        const replaced = sent.replaceRouteId
        if (typeof replaced !== 'string' || !active.includes(replaced)) {
          return problem(409, 'FAVORITES_FULL')
        }
        mine.splice(mine.indexOf(replaced), 1)
      }
      mine.push(route.id)
      return empty
    }
    if (part === 'checks' && (mode === 'TOP_ROPE' || mode === 'LEAD')) {
      if (readOnly) return problem(409, 'ROUTE_DISMOUNTED')
      const key = `${me.id}:${route.id}:${mode}`
      if (method === 'PUT') this.checks.add(key)
      else this.checks.delete(key)
      return empty
    }
    return undefined
  }
}
