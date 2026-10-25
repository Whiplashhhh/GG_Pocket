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

/** Every member of the club, as the topo sees them (openers of a route). */
export interface TopoPerson extends TopoMember {
  status: 'ACTIVE' | 'DISABLED'
}

/** Writes a line in the audit log of {@link FakeClub}. */
export type AuditRecorder = (
  actor: TopoMember,
  action: Schemas['AuditEntry']['action'],
  target: string,
) => void

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
  setterIds: string[]
  setterNote?: string
  advice?: string
  photo: boolean
  /** Hearts of members who are not part of the test. */
  othersFavorites: number
  /** Last chance acknowledgements of members who are not part of the test. */
  othersAcknowledged: number
}

interface FakeComment {
  id: string
  routeId: string
  author: Schemas['MemberRef']
  body: string
  createdAt: string
  deleted: boolean
}

/** The ascents logged in sessions, as the topo shows them (see FakeSessions). */
export interface AscentSource {
  toppedCount(routeId: string): number
  stats(routeId: string): Schemas['RouteStats']
  mine(memberId: string, routeId: string): Schemas['MyRouteAscent'][]
  /** A clean ascent of mine on the route in this mode ("je sais la faire" automatic). */
  hasClean(memberId: string, routeId: string, mode: ClimbMode): boolean
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
  '4a-',
  '4a',
  '4a+',
  '4b-',
  '4b',
  '4b+',
  '4c-',
  '4c',
  '4c+',
  '5a-',
  '5a',
  '5a+',
  '5b-',
  '5b',
  '5b+',
  '5c-',
  '5c',
  '5c+',
  '6a-',
  '6a',
  '6a+',
  '6b-',
  '6b',
  '6b+',
  '6c-',
  '6c',
  '6c+',
  '7a-',
  '7a',
  '7a+',
  '7b-',
  '7b',
  '7b+',
  '7c-',
  '7c',
  '7c+',
]

type Defaulted =
  'photo' | 'othersFavorites' | 'othersAcknowledged' | 'status' | 'openedOn' | 'setterIds'
type Seed = Omit<FakeRoute, 'id' | Defaulted> & Partial<Pick<FakeRoute, Defaulted>>

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
    othersAcknowledged: 12,
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
    grade: '6c-',
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
const STYLES: RouteStyle[] = [
  'CONTINUITY',
  'ENDURANCE',
  'PHYSICAL',
  'TECHNICAL',
  'COMPLEX',
  'SLAB',
  'OVERHANG',
  'CRIMPS',
  'JUGS',
  'PINCHES',
  'SLOPERS',
  'DIHEDRAL',
  'ROOF',
  'REACHY',
]

const atLeast = (role: Role, minimum: Role) => ROLES.indexOf(role) >= ROLES.indexOf(minimum)
const sameText = (a: string, b: string) => a.trim().toLowerCase() === b.trim().toLowerCase()

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
  /** Set by FakeClub once the sessions exist. */
  ascents: AscentSource | undefined

  constructor(
    private readonly nextId: () => string,
    private readonly clock: () => number,
    private readonly people: () => TopoPerson[] = () => [],
    private readonly audit: AuditRecorder = () => undefined,
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
        othersAcknowledged: 0,
        setterIds: [],
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

  summary(me: TopoMember, route: FakeRoute): Schemas['RouteSummary'] {
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
      toppedCount: this.ascents?.toppedCount(route.id) ?? 0,
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
    const people = this.people()
    return {
      route: summary,
      gradeId: grade?.id ?? '',
      cordId: cord?.id ?? '',
      setters: route.setterIds
        .map((id) => people.find((person) => person.id === id))
        .filter((person): person is TopoPerson => person !== undefined)
        .map((person) => this.memberRef(person)),
      ...(route.setterNote ? { setterNote: route.setterNote } : {}),
      ...(route.advice ? { advice: route.advice } : {}),
      createdAt: `${route.openedOn}T10:00:00Z`,
      stats: this.ascents?.stats(route.id) ?? {
        ascentCount: 0,
        toppedCount: 0,
        climberCount: 0,
      },
      myAscents: this.ascents?.mine(me.id, route.id) ?? [],
    }
  }

  private memberRef(person: TopoMember): Schemas['MemberRef'] {
    return {
      id: person.id,
      nickname: person.nickname,
      ...(person.avatarUrl ? { avatarUrl: person.avatarUrl } : {}),
    }
  }

  /** Today in Gravelines, by the test's clock. */
  private today(): string {
    return new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Paris' }).format(this.clock())
  }

  private cords() {
    return this.config.sectors.flatMap((sector) => sector.cords)
  }

  /**
   * Checks route settings like the backend (RouteService): a problem code, or the route fields.
   * A deactivated cord or colour is only accepted when the route already had it.
   */
  private readRouteSettings(
    sent: Record<string, unknown>,
    current?: FakeRoute,
  ):
    | TopoAnswer
    | Pick<
        FakeRoute,
        | 'name'
        | 'grade'
        | 'cordNumber'
        | 'colors'
        | 'openedOn'
        | 'setterIds'
        | 'setterNote'
        | 'advice'
        | 'styles'
      > {
    const settings = sent as Partial<Schemas['RouteSettings']>
    const invalid: TopoAnswer = { kind: 'problem', status: 400, code: 'VALIDATION_FAILED' }
    const name = typeof settings.name === 'string' ? settings.name.trim() : ''
    if (name.length === 0 || name.length > 60) return invalid
    const grade = this.config.grades.find((candidate) => candidate.id === settings.gradeId)
    const cord = this.cords().find((candidate) => candidate.id === settings.cordId)
    const colorIds = Array.isArray(settings.holdColorIds) ? settings.holdColorIds : []
    const colors = colorIds.map((id) => this.config.holdColors.find((color) => color.id === id))
    if (grade === undefined || cord === undefined || colors.some((color) => color === undefined)) {
      return invalid
    }
    if (colors.length < 1 || colors.length > 2 || new Set(colorIds).size !== colorIds.length) {
      return invalid
    }
    const labels = colors.map((color) => color?.label ?? '')
    const cordChanged = cord.number !== current?.cordNumber
    const colorsChanged = labels.some((label) => !(current?.colors ?? []).includes(label))
    if (
      (cordChanged && !cord.active) ||
      (colorsChanged && colors.some((color) => color?.active === false))
    ) {
      return { kind: 'problem', status: 400, code: 'CLUB_ITEM_INACTIVE' }
    }
    const openedOn = typeof settings.openedOn === 'string' ? settings.openedOn : ''
    if (!/^\d{4}-\d{2}-\d{2}$/.test(openedOn)) return invalid
    if (openedOn > this.today()) {
      return { kind: 'problem', status: 400, code: 'DATE_IN_THE_FUTURE' }
    }
    const setterIds = Array.isArray(settings.setterIds) ? settings.setterIds : []
    const setterNote = settings.setterNote?.trim() ?? ''
    const openers = this.people().filter(
      (person) => person.status === 'ACTIVE' && atLeast(person.role, 'SETTER'),
    )
    if (
      (setterIds.length === 0 && setterNote === '') ||
      setterIds.some((id) => !openers.some((person) => person.id === id))
    ) {
      return { kind: 'problem', status: 400, code: 'INVALID_SETTERS' }
    }
    const styles = Array.isArray(settings.styles) ? settings.styles : []
    if (styles.length > 5 || styles.some((style) => !STYLES.includes(style))) return invalid
    const advice = settings.advice?.trim() ?? ''
    if (advice.length > 500 || setterNote.length > 60) return invalid
    return {
      name,
      grade: grade.code,
      cordNumber: cord.number,
      colors: labels,
      openedOn,
      setterIds,
      setterNote: setterNote === '' ? undefined : setterNote,
      advice: advice === '' ? undefined : advice,
      styles,
    }
  }

  /** Leaving the last chance forgets who had seen it: if it comes back, so does the pop-up. */
  private leaveLastChance(route: FakeRoute) {
    delete route.plannedDismountOn
    route.othersAcknowledged = 0
    for (const key of [...this.acknowledgements]) {
      if (key.endsWith(`:${route.id}`)) this.acknowledgements.delete(key)
    }
  }

  /** The admin's club configuration (spec §8.11): sectors, cords, hold colours. */
  private handleClubConfig(
    me: TopoMember,
    method: string,
    path: string,
    sent: Record<string, unknown>,
  ): TopoAnswer | undefined {
    const json = (data: unknown, status?: number): TopoAnswer => ({ kind: 'json', data, status })
    const problem = (status: number, code: string): TopoAnswer => ({
      kind: 'problem',
      status,
      code,
    })
    if (!atLeast(me.role, 'ADMIN')) return problem(403, 'ACCESS_DENIED')
    const invalid = problem(400, 'VALIDATION_FAILED')
    const [, kind, id] = /^\/admin\/club\/([a-z-]+)(?:\/([^/]+))?$/.exec(path) ?? []
    const { sectors, holdColors } = this.config

    if (kind === 'sector-order' && method === 'PUT') {
      const ids = Array.isArray(sent.sectorIds) ? (sent.sectorIds as string[]) : []
      if (ids.length !== sectors.length || !sectors.every((sector) => ids.includes(sector.id))) {
        return invalid
      }
      for (const sector of sectors) sector.position = ids.indexOf(sector.id) + 1
      sectors.sort((a, b) => a.position - b.position)
      this.audit(me, 'CLUB_SECTORS_REORDERED', '')
      return json(this.config)
    }
    if (kind === 'sectors') {
      const name = typeof sent.name === 'string' ? sent.name.trim() : ''
      const description = typeof sent.description === 'string' ? sent.description.trim() : ''
      if (name.length === 0 || name.length > 60 || description.length > 160) return invalid
      if (sectors.some((sector) => sector.id !== id && sameText(sector.name, name))) {
        return problem(409, 'ALREADY_EXISTS')
      }
      const details = { name, ...(description === '' ? {} : { description }) }
      if (method === 'POST') {
        const sector = { id: this.nextId(), position: sectors.length + 1, cords: [], ...details }
        sectors.push(sector)
        this.audit(me, 'CLUB_SECTOR_CREATED', name)
        return json(sector, 201)
      }
      const sector = sectors.find((candidate) => candidate.id === id)
      if (sector === undefined) return problem(404, 'NOT_FOUND')
      delete sector.description
      Object.assign(sector, details)
      this.audit(me, 'CLUB_SECTOR_UPDATED', name)
      return json(sector)
    }
    if (kind === 'cords') {
      const number = sent.number
      const sector = sectors.find((candidate) => candidate.id === sent.sectorId)
      if (typeof number !== 'number' || !Number.isInteger(number) || number < 1 || number > 99) {
        return invalid
      }
      if (sector === undefined || typeof sent.active !== 'boolean') return invalid
      if (this.cords().some((cord) => cord.id !== id && cord.number === number)) {
        return problem(409, 'ALREADY_EXISTS')
      }
      let cord = this.cords().find((candidate) => candidate.id === id)
      if (method === 'POST') {
        cord = { id: this.nextId(), number, sectorId: sector.id, active: sent.active }
        this.audit(me, 'CLUB_CORD_CREATED', String(number))
      } else if (cord === undefined) {
        return problem(404, 'NOT_FOUND')
      } else {
        // The routes follow their cord, renumbered or moved.
        const before = cord.number
        for (const route of this.routes) if (route.cordNumber === before) route.cordNumber = number
        Object.assign(cord, { number, sectorId: sector.id, active: sent.active })
        this.audit(me, 'CLUB_CORD_UPDATED', String(number))
      }
      const saved = cord
      for (const candidate of sectors) {
        candidate.cords = candidate.cords.filter((existing) => existing.id !== saved.id)
      }
      sector.cords.push(saved)
      sector.cords.sort((a, b) => a.number - b.number)
      return json(saved, method === 'POST' ? 201 : 200)
    }
    if (kind === 'hold-colors') {
      const label = typeof sent.label === 'string' ? sent.label.trim() : ''
      const hex = typeof sent.hex === 'string' ? sent.hex : ''
      if (label.length === 0 || label.length > 30 || !/^#[0-9A-Fa-f]{6}$/.test(hex)) return invalid
      if (typeof sent.multicolor !== 'boolean' || typeof sent.active !== 'boolean') return invalid
      if (holdColors.some((color) => color.id !== id && sameText(color.label, label))) {
        return problem(409, 'ALREADY_EXISTS')
      }
      const details = { label, hex, multicolor: sent.multicolor, active: sent.active }
      if (method === 'POST') {
        const color = { id: this.nextId(), position: holdColors.length + 1, ...details }
        holdColors.push(color)
        this.audit(me, 'CLUB_HOLD_COLOR_CREATED', label)
        return json(color, 201)
      }
      const color = holdColors.find((candidate) => candidate.id === id)
      if (color === undefined) return problem(404, 'NOT_FOUND')
      for (const route of this.routes) {
        route.colors = route.colors.map((existing) => (existing === color.label ? label : existing))
      }
      Object.assign(color, details)
      this.audit(me, 'CLUB_HOLD_COLOR_UPDATED', label)
      return json(color)
    }
    return undefined
  }

  /** What setters do to routes (spec §8.10): create, edit, photo, last chance, dismount, remount. */
  private handleRouteManagement(
    me: TopoMember,
    method: string,
    path: string,
    sent: Record<string, unknown>,
  ): TopoAnswer | undefined {
    const json = (data: unknown, status?: number): TopoAnswer => ({ kind: 'json', data, status })
    const problem = (status: number, code: string): TopoAnswer => ({
      kind: 'problem',
      status,
      code,
    })
    const setter = atLeast(me.role, 'SETTER')

    if (path === '/routes/setters' && method === 'GET') {
      if (!setter) return problem(403, 'ACCESS_DENIED')
      return json(
        this.people()
          .filter((person) => person.status === 'ACTIVE' && atLeast(person.role, 'SETTER'))
          .sort((a, b) => a.nickname.localeCompare(b.nickname))
          .map((person) => this.memberRef(person)),
      )
    }
    if (path === '/routes/last-chance' && method === 'GET') {
      if (!setter) return problem(403, 'ACCESS_DENIED')
      return json(
        this.routes
          .filter((route) => route.status === 'LAST_CHANCE')
          .map((route) => ({
            route: this.summary(me, route),
            acknowledgedCount:
              route.othersAcknowledged +
              [...this.acknowledgements].filter((key) => key.endsWith(`:${route.id}`)).length,
          })),
      )
    }
    if (path === '/routes' && method === 'POST') {
      if (!setter) return problem(403, 'ACCESS_DENIED')
      const fields = this.readRouteSettings(sent)
      if ('kind' in fields) return fields
      const route: FakeRoute = {
        id: this.nextId(),
        status: 'ACTIVE',
        photo: false,
        othersFavorites: 0,
        othersAcknowledged: 0,
        ...fields,
      }
      this.routes.push(route)
      return json(this.detail(me, route), 201)
    }

    const match = /^\/routes\/([^/]+)\/(photo|last-chance|dismount|remount)$/.exec(path)
    const editing = /^\/routes\/([^/]+)$/.exec(path) !== null && method === 'PUT'
    if (match === null && !editing) return undefined
    if (!setter) return problem(403, 'ACCESS_DENIED')
    const id = path.split('/')[2]
    const route = this.routes.find((candidate) => candidate.id === id)
    if (route === undefined) return problem(404, 'NOT_FOUND')
    const part = match?.[2]

    if (part === 'remount') {
      if (route.status !== 'DISMOUNTED') return problem(409, 'ROUTE_STATUS_CONFLICT')
      route.status = 'ACTIVE'
      delete route.dismountedAt
      this.audit(me, 'ROUTE_REMOUNTED', route.name)
      return json(this.detail(me, route))
    }
    if (route.status === 'DISMOUNTED') {
      return problem(409, part === 'dismount' ? 'ROUTE_STATUS_CONFLICT' : 'ROUTE_DISMOUNTED')
    }
    if (editing) {
      const fields = this.readRouteSettings(sent, route)
      if ('kind' in fields) return fields
      delete route.setterNote
      delete route.advice
      Object.assign(route, fields)
      return json(this.detail(me, route))
    }
    if (part === 'photo') {
      route.photo = method === 'PUT'
      return method === 'PUT' ? json(this.detail(me, route)) : { kind: 'empty' }
    }
    if (part === 'last-chance') {
      if (method === 'DELETE') {
        if (route.status !== 'LAST_CHANCE') return problem(409, 'ROUTE_STATUS_CONFLICT')
        route.status = 'ACTIVE'
        this.leaveLastChance(route)
        return json(this.detail(me, route))
      }
      const planned = sent.plannedDismountOn
      if (typeof planned === 'string' && planned < this.today()) {
        return problem(400, 'DATE_IN_THE_PAST')
      }
      route.status = 'LAST_CHANCE'
      if (typeof planned === 'string') route.plannedDismountOn = planned
      else delete route.plannedDismountOn
      return json(this.detail(me, route))
    }
    if (part === 'dismount' && method === 'POST') {
      if (route.status === 'LAST_CHANCE') this.leaveLastChance(route)
      route.status = 'DISMOUNTED'
      route.dismountedAt = new Date(this.clock()).toISOString()
      this.audit(me, 'ROUTE_DISMOUNTED', route.name)
      return json(this.detail(me, route))
    }
    return undefined
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
    if (path.startsWith('/admin/club/')) return this.handleClubConfig(me, method, path, sent)
    const managed = this.handleRouteManagement(me, method, path, sent)
    if (managed !== undefined) return managed
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
      else if (this.ascents?.hasClean(me.id, route.id, mode) === true) {
        return problem(409, 'CHECK_FROM_ASCENT')
      } else this.checks.delete(key)
      return empty
    }
    return undefined
  }
}
