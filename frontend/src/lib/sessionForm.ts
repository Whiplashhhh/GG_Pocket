import type { ClimbMode } from '@/api/topo'
import type { Session, SessionRequest, SessionVisibility } from '@/api/sessions'

import { clubToday } from './age'

/**
 * The session sheet (spec §8.3) as plain data: what the climber types, kept in the draft between
 * two openings. The server checks everything again; these rules only keep the form coherent
 * (a clean ascent is topped, without a fall; only a route not topped is past its half).
 */

/** How far back a session may be dated (D-009); the server says the same. */
export const SESSION_DAYS_BACK = 7
export const MAX_ASCENTS = 50
export const MAX_FALLS = 99
export const MAX_COMMENT_LENGTH = 500

export interface AscentLine {
  /** Stable key of the line in the list (not sent). */
  key: string
  routeId: string
  mode: ClimbMode
  topped: boolean
  /** "Plus de la moitié" (D-121): not topped, but past the half; half a point on the podium. */
  pastHalf: boolean
  clean: boolean
  falls: number
  firstTry: boolean
  feeling: number | null
  /** Who held the rope (a club member), if I say so. */
  belayerId: string | null
}

export interface SessionForm {
  /** Drawn once per new session: sending it twice gives back the same session. */
  clientRef: string
  climbedOn: string
  feeling: number | null
  comment: string
  visibility: SessionVisibility
  /**
   * The belayer of the whole session (D-118): every line takes it, new ones too. Kept while
   * `manyBelayers` is on, as the belayer of a first line added then.
   */
  belayerId: string | null
  /** "Plusieurs assureur·ses": each line has its own belayer field. */
  manyBelayers: boolean
  ascents: AscentLine[]
}

export function newKey(): string {
  return crypto.randomUUID()
}

export function emptySessionForm(today: string = clubToday()): SessionForm {
  return {
    clientRef: crypto.randomUUID(),
    climbedOn: today,
    feeling: null,
    comment: '',
    visibility: 'FRIENDS',
    belayerId: null,
    manyBelayers: false,
    ascents: [],
  }
}

/** Whether all lines have the same belayer (or nobody); true without lines. */
export function sameBelayer(lines: readonly Pick<AscentLine, 'belayerId'>[]): boolean {
  return lines.every((line) => line.belayerId === lines[0]?.belayerId)
}

/**
 * The belayer fields of a form taken back (saved session, draft, refused session): one field when
 * all lines share their belayer, the box "Plusieurs assureur·ses" ticked otherwise (D-118).
 */
function belayerFields(
  lines: readonly Pick<AscentLine, 'belayerId'>[],
  kept: { belayerId?: string | null; manyBelayers?: boolean } = {},
): Pick<SessionForm, 'belayerId' | 'manyBelayers'> {
  const same = sameBelayer(lines)
  const first = lines[0]
  return {
    belayerId: same && first !== undefined ? first.belayerId : (kept.belayerId ?? null),
    manyBelayers: kept.manyBelayers === true || !same,
  }
}

/** The belayer of the whole session: every line takes it (D-118). */
export function setSessionBelayer(form: SessionForm, belayerId: string | null): SessionForm {
  return {
    ...form,
    belayerId,
    ascents: form.ascents.map((line) => ({ ...line, belayerId })),
  }
}

/**
 * Ticks or unticks "Plusieurs assureur·ses". Ticked, each line keeps its belayer (the session's
 * until changed). Unticked, the session takes the belayer of the first line, and so does every
 * line: when they differ, the sheet asks first (see `sameBelayer`).
 */
export function setManyBelayers(form: SessionForm, many: boolean): SessionForm {
  if (many) return { ...form, manyBelayers: true }
  const first = form.ascents[0]
  return {
    ...setSessionBelayer(form, first === undefined ? form.belayerId : first.belayerId),
    manyBelayers: false,
  }
}

/**
 * Who a new line names: the belayer of the session, or, line by line, the one of the line before
 * (one usually climbs a whole evening with the same partner, D-101).
 */
export function belayerForNewLine(form: SessionForm): string | null {
  const last = form.ascents.at(-1)
  return form.manyBelayers && last !== undefined ? last.belayerId : form.belayerId
}

/** The form of a saved session, to change it. */
export function sessionToForm(session: Session): SessionForm {
  return {
    clientRef: crypto.randomUUID(),
    climbedOn: session.climbedOn,
    feeling: session.feeling ?? null,
    comment: session.comment ?? '',
    visibility: session.visibility,
    ...belayerFields(session.ascents.map((ascent) => ({ belayerId: ascent.belayer?.id ?? null }))),
    ascents: session.ascents.map((ascent) => ({
      key: newKey(),
      routeId: ascent.route.id,
      mode: ascent.mode,
      topped: ascent.topped,
      pastHalf: !ascent.topped && ascent.pastHalf,
      clean: ascent.clean,
      falls: ascent.falls,
      firstTry: ascent.firstTry,
      feeling: ascent.feeling ?? null,
      belayerId: ascent.belayer?.id ?? null,
    })),
  }
}

/**
 * A draft kept on the phone, maybe from before belayers (D-101: its lines get none), before the
 * belayer of the session (D-118: the box is ticked when its lines differ) or before « Plus de la
 * moitié » (D-121: its lines are not past the half).
 */
export function restoredForm(
  form: Omit<SessionForm, 'ascents' | 'belayerId' | 'manyBelayers'> &
    Partial<Pick<SessionForm, 'belayerId' | 'manyBelayers'>> & {
      ascents: (Omit<AscentLine, 'belayerId' | 'pastHalf'> & Partial<AscentLine>)[]
    },
): SessionForm {
  const ascents = form.ascents.map((line) => ({
    ...line,
    belayerId: line.belayerId ?? null,
    pastHalf: !line.topped && line.pastHalf === true,
  }))
  return { ...form, ...belayerFields(ascents, form), ascents }
}

/**
 * A draft waits on the phone while the wall changes: its lines on a route gone since (dismounted,
 * or a demo topo seeded again) go, as the server would refuse them. `dropped` says how many.
 */
export function withoutGoneRoutes(
  form: SessionForm,
  wall: { has(routeId: string): boolean },
): { form: SessionForm; dropped: number } {
  const ascents = form.ascents.filter((line) => wall.has(line.routeId))
  const dropped = form.ascents.length - ascents.length
  return { form: dropped === 0 ? form : { ...form, ascents }, dropped }
}

/** Earliest and latest day the date field offers (YYYY-MM-DD). */
export function dateBounds(today: string = clubToday()): { min: string; max: string } {
  const day = new Date(`${today}T12:00:00Z`)
  day.setUTCDate(day.getUTCDate() - SESSION_DAYS_BACK)
  return { min: day.toISOString().slice(0, 10), max: today }
}

/**
 * A new line for a route, topped in the mode of the line before (top rope for the first one), with
 * the given belayer (see `belayerForNewLine`; by default the one of the line before).
 * "Premier essai" is ticked when I never logged this route before, nor higher in this session.
 */
export function addAscent(
  lines: readonly AscentLine[],
  routeId: string,
  climbedBefore: ReadonlySet<string>,
  {
    mode = lines.at(-1)?.mode ?? 'TOP_ROPE',
    belayerId = lines.at(-1)?.belayerId ?? null,
  }: { mode?: ClimbMode; belayerId?: string | null } = {},
): AscentLine[] {
  if (lines.length >= MAX_ASCENTS) return [...lines]
  const firstTry = !climbedBefore.has(routeId) && !lines.some((line) => line.routeId === routeId)
  return [
    ...lines,
    {
      key: newKey(),
      routeId,
      mode,
      topped: true,
      pastHalf: false,
      clean: false,
      falls: 0,
      firstTry,
      feeling: null,
      belayerId,
    },
  ]
}

/** Not topped cannot be clean; topped is more than past the half (the box goes). */
export function setTopped(line: AscentLine, topped: boolean): AscentLine {
  return { ...line, topped, clean: topped && line.clean, pastHalf: !topped && line.pastHalf }
}

/** « Plus de la moitié » only goes with a route not topped (D-121). */
export function setPastHalf(line: AscentLine, pastHalf: boolean): AscentLine {
  if (pastHalf && line.topped) return line
  return { ...line, pastHalf }
}

/** Clean means topped, without a fall: falls go back to 0 (and the stepper locks). */
export function setClean(line: AscentLine, clean: boolean): AscentLine {
  if (clean && !line.topped) return line
  return { ...line, clean, falls: clean ? 0 : line.falls }
}

export function setFalls(line: AscentLine, falls: number): AscentLine {
  if (line.clean) return line
  return { ...line, falls: Math.min(MAX_FALLS, Math.max(0, Math.trunc(falls))) }
}

/** A second try of the same route, right after it: never a first try. */
export function duplicateAscent(lines: readonly AscentLine[], index: number): AscentLine[] {
  const line = lines[index]
  if (line === undefined || lines.length >= MAX_ASCENTS) return [...lines]
  const copy: AscentLine = { ...line, key: newKey(), firstTry: false }
  return [...lines.slice(0, index + 1), copy, ...lines.slice(index + 1)]
}

/** Moves a line up (-1) or down (+1); does nothing at the ends. */
export function moveAscent(lines: readonly AscentLine[], index: number, delta: -1 | 1) {
  const target = index + delta
  const moved = [...lines]
  const line = moved[index]
  const other = moved[target]
  if (line === undefined || other === undefined) return moved
  moved[index] = other
  moved[target] = line
  return moved
}

export function removeAscent(lines: readonly AscentLine[], index: number): AscentLine[] {
  return lines.filter((_, i) => i !== index)
}

/** Takes back the last line of a route, the one a tap too many just added. */
export function removeLastOf(lines: readonly AscentLine[], routeId: string): AscentLine[] {
  for (let index = lines.length - 1; index >= 0; index--) {
    if (lines[index]?.routeId === routeId) return removeAscent(lines, index)
  }
  return [...lines]
}

export type FormProblem = 'noAscent' | 'dateOutOfRange' | 'commentTooLong'

/** What the form still lacks before it can be sent; empty when ready. */
export function formProblems(
  form: SessionForm,
  { min, max }: { min: string; max: string } = dateBounds(),
): FormProblem[] {
  const problems: FormProblem[] = []
  if (form.ascents.length === 0) problems.push('noAscent')
  if (form.climbedOn < min || form.climbedOn > max) problems.push('dateOutOfRange')
  if (form.comment.trim().length > MAX_COMMENT_LENGTH) problems.push('commentTooLong')
  return problems
}

export function toSessionRequest(form: SessionForm): SessionRequest {
  const comment = form.comment.trim()
  return {
    clientRef: form.clientRef,
    climbedOn: form.climbedOn,
    ...(form.feeling === null ? {} : { feeling: form.feeling }),
    ...(comment === '' ? {} : { comment }),
    visibility: form.visibility,
    ascents: form.ascents.map((line) => ({
      routeId: line.routeId,
      mode: line.mode,
      topped: line.topped,
      pastHalf: !line.topped && line.pastHalf,
      clean: line.clean,
      falls: line.clean ? 0 : line.falls,
      firstTry: line.firstTry,
      ...(line.feeling === null ? {} : { feeling: line.feeling }),
      ...(line.belayerId === null ? {} : { belayerId: line.belayerId }),
    })),
  }
}

/** A session refused by the server, taken back from the offline queue into the sheet. */
export function requestToForm(body: SessionRequest): SessionForm {
  return {
    clientRef: body.clientRef,
    climbedOn: body.climbedOn,
    feeling: body.feeling ?? null,
    comment: body.comment ?? '',
    visibility: body.visibility,
    ...belayerFields(body.ascents.map((ascent) => ({ belayerId: ascent.belayerId ?? null }))),
    ascents: body.ascents.map((ascent) => ({
      key: newKey(),
      routeId: ascent.routeId,
      mode: ascent.mode,
      topped: ascent.topped,
      pastHalf: !ascent.topped && ascent.pastHalf === true,
      clean: ascent.clean,
      falls: ascent.falls,
      firstTry: ascent.firstTry,
      feeling: ascent.feeling ?? null,
      belayerId: ascent.belayerId ?? null,
    })),
  }
}

/**
 * Who may see the session, smallest audience first (D-120). « Tout le club » is offered to a member
 * of the club feed; a session that already has it (or a draft that asks for it) keeps it on screen
 * after its climber left, so they see it and change it.
 */
export function visibilityChoices(
  inClubFeed: boolean,
  saved: SessionVisibility | undefined,
  current: SessionVisibility,
): SessionVisibility[] {
  const club = inClubFeed || saved === 'CLUB' || current === 'CLUB'
  return club ? ['PRIVATE', 'FRIENDS', 'CLUB'] : ['PRIVATE', 'FRIENDS']
}

/** The line under the choice: what it means, or why « Tout le club » no longer reaches the club. */
export function visibilityHintKey(visibility: SessionVisibility, inClubFeed: boolean): string {
  switch (visibility) {
    case 'CLUB':
      return inClubFeed ? 'session.visibilityClubHint' : 'session.visibilityClubLeft'
    case 'FRIENDS':
      return 'session.visibilityFriendsHint'
    default:
      return 'session.visibilityPrivateHint'
  }
}
