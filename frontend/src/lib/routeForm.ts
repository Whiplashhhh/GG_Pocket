import type { Cord, Grade, HoldColorConfig, RouteDetail, TopoConfig } from '@/api/topo'
import type { RouteSettings } from '@/api/topoAdmin'

import type { FieldErrors } from './forms'
import { ROUTE_STYLES, type HoldColor, type RouteStyle } from './route'

/**
 * The route form of the management space (spec §8.10): what the setter types, how it becomes
 * the API's RouteSettings, and the same rules as the backend, checked before sending.
 */

export const ROUTE_NAME_MAX = 60
export const SETTER_NOTE_MAX = 60
export const ADVICE_MAX = 500
export const MAX_STYLES = 5
/** A route has one hold colour, sometimes two (« jaune et bleu »). */
export const MAX_HOLD_COLORS = 2

export interface RouteForm {
  name: string
  gradeId: string
  cordId: string
  /** The main colour first, then the optional second one. */
  holdColorIds: string[]
  /** YYYY-MM-DD. */
  openedOn: string
  setterIds: string[]
  /** An opener without an account (a guest, a former member). */
  setterNote: string
  advice: string
  styles: RouteStyle[]
}

export type RouteFormField =
  | 'name'
  | 'gradeId'
  | 'cordId'
  | 'holdColorIds'
  | 'openedOn'
  | 'setters'
  | 'setterNote'
  | 'advice'
  | 'styles'

export const ROUTE_FORM_FIELDS: readonly RouteFormField[] = [
  'name',
  'gradeId',
  'cordId',
  'holdColorIds',
  'openedOn',
  'setters',
  'setterNote',
  'advice',
  'styles',
]

/** API fields that the form shows under another name. */
export const ROUTE_FORM_ALIASES: Partial<Record<string, RouteFormField>> = { setterIds: 'setters' }

export function emptyRouteForm(today: string): RouteForm {
  return {
    name: '',
    gradeId: '',
    cordId: '',
    holdColorIds: [],
    openedOn: today,
    setterIds: [],
    setterNote: '',
    advice: '',
    styles: [],
  }
}

export function toRouteForm(detail: RouteDetail): RouteForm {
  return {
    name: detail.route.name,
    gradeId: detail.gradeId,
    cordId: detail.cordId,
    holdColorIds: detail.route.holds.map((hold) => hold.id),
    openedOn: detail.route.openedOn,
    setterIds: detail.setters.map((setter) => setter.id),
    setterNote: detail.setterNote ?? '',
    advice: detail.advice ?? '',
    styles: [...detail.route.styles],
  }
}

/**
 * Reopening on the same cord: everything of the model route (name kept, to be changed or not),
 * opened today. The photo is not copied: the new holds are not the old ones.
 */
export function duplicateRouteForm(detail: RouteDetail, today: string): RouteForm {
  return { ...toRouteForm(detail), openedOn: today }
}

/** Same content, whatever the order of the setters: to know if there is anything to lose. */
export function sameRouteForm(a: RouteForm, b: RouteForm): boolean {
  const key = (form: RouteForm) =>
    JSON.stringify({
      ...form,
      setterIds: [...form.setterIds].sort(),
      styles: [...form.styles].sort(),
    })
  return key(a) === key(b)
}

export function routeSettings(
  form: RouteForm,
  today: string,
): { settings: RouteSettings } | { errors: FieldErrors<RouteFormField> } {
  const errors: FieldErrors<RouteFormField> = {}
  const name = form.name.trim()
  const setterNote = form.setterNote.trim()
  const advice = form.advice.trim()
  if (name === '') {
    errors.name = { key: 'validation.required' }
  } else if (name.length > ROUTE_NAME_MAX) {
    errors.name = { key: 'validation.tooLong', params: { max: ROUTE_NAME_MAX } }
  }
  if (form.gradeId === '') {
    errors.gradeId = { key: 'routeForm.errors.grade' }
  }
  if (form.cordId === '') {
    errors.cordId = { key: 'routeForm.errors.cord' }
  }
  if (form.holdColorIds.length === 0) {
    errors.holdColorIds = { key: 'routeForm.errors.color' }
  } else if (
    form.holdColorIds.length > MAX_HOLD_COLORS ||
    new Set(form.holdColorIds).size !== form.holdColorIds.length
  ) {
    errors.holdColorIds = { key: 'routeForm.errors.colorsDistinct' }
  }
  if (!/^\d{4}-\d{2}-\d{2}$/.test(form.openedOn)) {
    errors.openedOn = { key: 'validation.required' }
  } else if (form.openedOn > today) {
    errors.openedOn = { key: 'validation.dateInFuture' }
  }
  if (form.setterIds.length === 0 && setterNote === '') {
    errors.setters = { key: 'routeForm.errors.setters' }
  }
  if (setterNote.length > SETTER_NOTE_MAX) {
    errors.setterNote = { key: 'validation.tooLong', params: { max: SETTER_NOTE_MAX } }
  }
  if (advice.length > ADVICE_MAX) {
    errors.advice = { key: 'validation.tooLong', params: { max: ADVICE_MAX } }
  }
  if (form.styles.length > MAX_STYLES) {
    errors.styles = { key: 'routeForm.errors.styles', params: { max: MAX_STYLES } }
  }
  if (Object.keys(errors).length > 0) {
    return { errors }
  }
  return {
    settings: {
      name,
      gradeId: form.gradeId,
      cordId: form.cordId,
      holdColorIds: [...form.holdColorIds],
      openedOn: form.openedOn,
      setterIds: [...new Set(form.setterIds)],
      ...(setterNote === '' ? {} : { setterNote }),
      ...(advice === '' ? {} : { advice }),
      styles: ROUTE_STYLES.filter((style) => form.styles.includes(style)),
    },
  }
}

/** Grades, easiest first. */
export function gradeChoices(config: TopoConfig): Grade[] {
  return [...config.grades].sort((a, b) => a.rank - b.rank)
}

export interface CordGroup {
  sectorId: string
  sectorName: string
  cords: Cord[]
}

/**
 * Cords to choose from, by sector from left to right. A deactivated cord only shows when the
 * route is already on it: the server accepts it as long as it does not change.
 */
export function cordChoices(config: TopoConfig, currentCordId = ''): CordGroup[] {
  return [...config.sectors]
    .sort((a, b) => a.position - b.position)
    .map((sector) => ({
      sectorId: sector.id,
      sectorName: sector.name,
      cords: sector.cords
        .filter((cord) => cord.active || cord.id === currentCordId)
        .sort((a, b) => a.number - b.number),
    }))
    .filter((group) => group.cords.length > 0)
}

/** Hold colours to choose from: the active ones, plus those the route already has. */
export function colorChoices(
  config: TopoConfig,
  currentIds: readonly string[] = [],
): HoldColorConfig[] {
  return config.holdColors
    .filter((color) => color.active || currentIds.includes(color.id))
    .sort((a, b) => a.position - b.position)
}

/** Picks the main colour; the second one stays unless it is the same. */
export function withMainColor(ids: readonly string[], id: string): string[] {
  const second = ids[1]
  return second === undefined || second === id ? [id] : [id, second]
}

/** Picks the second colour, or none (undefined). Never the main one twice. */
export function withSecondColor(ids: readonly string[], id: string | undefined): string[] {
  const [main] = ids
  if (main === undefined) return id === undefined ? [] : [id]
  return id === undefined || id === main ? [main] : [main, id]
}

/** Adds or removes a style; a sixth one is not added. */
export function toggleStyle(styles: readonly RouteStyle[], style: RouteStyle): RouteStyle[] {
  if (styles.includes(style)) return styles.filter((candidate) => candidate !== style)
  return styles.length >= MAX_STYLES ? [...styles] : [...styles, style]
}

export function toggleId(ids: readonly string[], id: string): string[] {
  return ids.includes(id) ? ids.filter((candidate) => candidate !== id) : [...ids, id]
}

export interface LabelPreview {
  cord: number
  grade: string
  holds: HoldColor[]
}

/** What the paper label will look like, once cord, grade and a colour are chosen. */
export function labelPreview(form: RouteForm, config: TopoConfig): LabelPreview | null {
  const cord = config.sectors
    .flatMap((sector) => sector.cords)
    .find((candidate) => candidate.id === form.cordId)
  const grade = config.grades.find((candidate) => candidate.id === form.gradeId)
  const holds = form.holdColorIds
    .map((id) => config.holdColors.find((color) => color.id === id))
    .filter((color): color is HoldColorConfig => color !== undefined)
  if (cord === undefined || grade === undefined || holds.length === 0) return null
  return { cord: cord.number, grade: grade.code, holds }
}
