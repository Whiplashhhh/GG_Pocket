import type { ClimbMode } from '@/api/topo'
import type { Session, SessionRequest, SessionVisibility } from '@/api/sessions'

import { clubToday } from './age'

/**
 * The session sheet (spec §8.3) as plain data: what the climber types, kept in the draft between
 * two openings. The server checks everything again; these rules only keep the form coherent
 * (a clean ascent is topped, without a fall).
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
  clean: boolean
  falls: number
  firstTry: boolean
  feeling: number | null
}

export interface SessionForm {
  /** Drawn once per new session: sending it twice gives back the same session. */
  clientRef: string
  climbedOn: string
  feeling: number | null
  comment: string
  visibility: SessionVisibility
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
    ascents: [],
  }
}

/** The form of a saved session, to change it. */
export function sessionToForm(session: Session): SessionForm {
  return {
    clientRef: crypto.randomUUID(),
    climbedOn: session.climbedOn,
    feeling: session.feeling ?? null,
    comment: session.comment ?? '',
    visibility: session.visibility,
    ascents: session.ascents.map((ascent) => ({
      key: newKey(),
      routeId: ascent.route.id,
      mode: ascent.mode,
      topped: ascent.topped,
      clean: ascent.clean,
      falls: ascent.falls,
      firstTry: ascent.firstTry,
      feeling: ascent.feeling ?? null,
    })),
  }
}

/** Earliest and latest day the date field offers (YYYY-MM-DD). */
export function dateBounds(today: string = clubToday()): { min: string; max: string } {
  const day = new Date(`${today}T12:00:00Z`)
  day.setUTCDate(day.getUTCDate() - SESSION_DAYS_BACK)
  return { min: day.toISOString().slice(0, 10), max: today }
}

/**
 * A new line for a route, topped in the mode of the line before (top rope for the first one).
 * "Premier essai" is ticked when I never logged this route before, nor higher in this session.
 */
export function addAscent(
  lines: readonly AscentLine[],
  routeId: string,
  climbedBefore: ReadonlySet<string>,
  mode: ClimbMode = lines.at(-1)?.mode ?? 'TOP_ROPE',
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
      clean: false,
      falls: 0,
      firstTry,
      feeling: null,
    },
  ]
}

/** Not topped cannot be clean. */
export function setTopped(line: AscentLine, topped: boolean): AscentLine {
  return { ...line, topped, clean: topped && line.clean }
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
      clean: line.clean,
      falls: line.clean ? 0 : line.falls,
      firstTry: line.firstTry,
      ...(line.feeling === null ? {} : { feeling: line.feeling }),
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
    ascents: body.ascents.map((ascent) => ({
      key: newKey(),
      routeId: ascent.routeId,
      mode: ascent.mode,
      topped: ascent.topped,
      clean: ascent.clean,
      falls: ascent.falls,
      firstTry: ascent.firstTry,
      feeling: ascent.feeling ?? null,
    })),
  }
}
