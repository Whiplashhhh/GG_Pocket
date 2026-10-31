import type { BadgeLibraryEntry } from '@/api/badges'
import type { components } from '@/api/schema'

export type BadgeCode = BadgeLibraryEntry['code']
export type BadgeFamily = BadgeLibraryEntry['family']
export type Seniority = NonNullable<components['schemas']['MemberCard']['seniority']>

/** The drawings of BadgeIcon: one per tier family, one per repeatable badge (or pair of them). */
export type BadgeGlyph =
  | 'topped'
  | 'lead'
  | 'grade'
  | 'days'
  | 'week'
  | 'month'
  | 'streak'
  | 'noMiss'
  | 'fullLead'
  | 'perfect'
  | 'zeroRest'
  | 'comeback'
  | 'flash'
  | 'record'

/** The library's sections, in the catalogue's order (spec §9.5). */
export const BADGE_FAMILIES: readonly BadgeFamily[] = [
  'TOPPED',
  'LEAD',
  'FIRST_GRADE',
  'CLIMBING_DAYS',
  'REPEATABLE',
]

/**
 * Resin colours of the earned medals, one per family: hold colours of the club light enough for
 * the dark drawing on them (≥ 3:1), whatever the theme.
 */
export const BADGE_COLORS: Readonly<Record<BadgeFamily, string>> = {
  TOPPED: '#f5c518',
  LEAD: '#f28c28',
  FIRST_GRADE: '#f06eaa',
  CLIMBING_DAYS: '#9bd34a',
  REPEATABLE: '#5bc0eb',
}

const FAMILY_GLYPHS: Readonly<Record<Exclude<BadgeFamily, 'REPEATABLE'>, BadgeGlyph>> = {
  TOPPED: 'topped',
  LEAD: 'lead',
  FIRST_GRADE: 'grade',
  CLIMBING_DAYS: 'days',
}

const REPEATABLE_GLYPHS: Readonly<Partial<Record<BadgeCode, BadgeGlyph>>> = {
  WEEK_2: 'week',
  WEEK_3: 'week',
  WEEK_4: 'week',
  MONTH_COMPLETE: 'month',
  MONTH_12: 'month',
  STREAK_3W: 'streak',
  MONTH_NO_MISS: 'noMiss',
  FULL_LEAD: 'fullLead',
  PERFECT: 'perfect',
  ZERO_REST: 'zeroRest',
  COMEBACK: 'comeback',
  FLASH: 'flash',
  RECORD: 'record',
}

/** Badges sharing a drawing tell themselves apart by this mark on the medal. */
const REPEATABLE_MARKS: Readonly<Partial<Record<BadgeCode, string>>> = {
  WEEK_2: '2',
  WEEK_3: '3',
  WEEK_4: '4',
  MONTH_COMPLETE: '10',
  MONTH_12: '12',
}

const TIER_PREFIXES: Readonly<Record<string, BadgeFamily>> = {
  TOPPED: 'TOPPED',
  LEAD: 'LEAD',
  GRADE: 'FIRST_GRADE',
  DAYS: 'CLIMBING_DAYS',
}

/** The family of a code, from its name (the API sends it too; this one serves the feed's codes). */
export function badgeFamily(code: BadgeCode): BadgeFamily {
  return TIER_PREFIXES[code.slice(0, code.indexOf('_'))] ?? 'REPEATABLE'
}

export function badgeGlyph(code: BadgeCode): BadgeGlyph {
  const family = badgeFamily(code)
  return family === 'REPEATABLE' ? (REPEATABLE_GLYPHS[code] ?? 'record') : FAMILY_GLYPHS[family]
}

/** The tier of a tier badge: "50" (voies), "6a" (cotation); the count of a week or month badge. */
export function badgeMark(code: BadgeCode): string | undefined {
  if (badgeFamily(code) === 'REPEATABLE') return REPEATABLE_MARKS[code]
  return code.slice(code.indexOf('_') + 1).toLowerCase()
}

export interface BadgeSection {
  family: BadgeFamily
  entries: BadgeLibraryEntry[]
  earned: number
}

/** The library cut into its families, in the catalogue's order; empty families are left out. */
export function badgeSections(entries: readonly BadgeLibraryEntry[]): BadgeSection[] {
  return BADGE_FAMILIES.map((family) => {
    const mine = entries.filter((entry) => entry.family === family)
    return { family, entries: mine, earned: mine.filter((entry) => entry.count > 0).length }
  }).filter((section) => section.entries.length > 0)
}
