import type { BadgeLibrary, BadgeLibraryEntry } from '@/api/badges'
import type { components } from '@/api/schema'

export type BadgeCode = BadgeLibraryEntry['code']
export type BadgeFamily = BadgeLibraryEntry['family']
export type Badge = components['schemas']['ShownBadge']['badge']
export type ShownBadge = components['schemas']['ShownBadge']
export type BadgeProgress = components['schemas']['BadgeProgress']
export type Seniority = NonNullable<components['schemas']['MemberCard']['seniority']>

/** The drawings of BadgeIcon: one per badge, the weeks and the months sharing theirs. */
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

/** The badges in the catalogue's order: the four tier families, then the repeatable ones. */
export const BADGES: readonly Badge[] = [
  'TOPPED',
  'LEAD',
  'FIRST_GRADE',
  'CLIMBING_DAYS',
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
]

const TIERED: Readonly<Partial<Record<string, Badge>>> = {
  TOPPED: 'TOPPED',
  LEAD: 'LEAD',
  GRADE: 'FIRST_GRADE',
  DAYS: 'CLIMBING_DAYS',
}

/** How many levels each tier family has (spec §9.5). */
export const TIER_COUNTS: Readonly<Partial<Record<Badge, number>>> = {
  TOPPED: 10,
  LEAD: 10,
  FIRST_GRADE: 8,
  CLIMBING_DAYS: 8,
}

const GLYPHS: Readonly<Record<Badge, BadgeGlyph>> = {
  TOPPED: 'topped',
  LEAD: 'lead',
  FIRST_GRADE: 'grade',
  CLIMBING_DAYS: 'days',
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
const MARKS: Readonly<Partial<Record<Badge, string>>> = {
  WEEK_2: '2',
  WEEK_3: '3',
  WEEK_4: '4',
  MONTH_COMPLETE: '10',
  MONTH_12: '12',
}

/**
 * The rarity ladder (D-106), in the club's hold colours: the level is the colour, the same for
 * every badge. Level 1 is brown (bronze), level 10 gold; grey stays for the badges not earned.
 */
export const RARITY_COLORS: readonly string[] = [
  '#7a4b2a',
  '#9bd34a',
  '#2e9e44',
  '#5bc0eb',
  '#1f5fbf',
  '#7b4fb8',
  '#f06eaa',
  '#d7263d',
  '#f28c28',
  '#f5c518',
]

/** Times earned for each step of the ladder of a repeatable badge; gold stays gold after 100. */
export const REPEAT_STEPS: readonly number[] = [1, 2, 3, 5, 10, 15, 25, 50, 75, 100]

export function badgeOf(code: BadgeCode): Badge {
  return TIERED[code.slice(0, code.indexOf('_'))] ?? (code as Badge)
}

export function isTiered(badge: Badge): boolean {
  return TIER_COUNTS[badge] !== undefined
}

export function badgeGlyph(badge: Badge): BadgeGlyph {
  return GLYPHS[badge]
}

export function badgeMark(badge: Badge): string | undefined {
  return MARKS[badge]
}

/** The step of the ladder (1 to 10) of a badge at `level`; 0 when not earned. */
export function rarity(badge: Badge, level: number): number {
  if (level <= 0) return 0
  if (isTiered(badge)) return Math.min(level, RARITY_COLORS.length)
  return REPEAT_STEPS.filter((step) => level >= step).length
}

export function rarityColor(badge: Badge, level: number): string | undefined {
  const step = rarity(badge, level)
  return step === 0 ? undefined : RARITY_COLORS[step - 1]
}

/** A tier family's last level, reached. */
export function isMaxLevel(badge: Badge, level: number): boolean {
  const tiers = TIER_COUNTS[badge]
  return tiers !== undefined && level >= tiers
}

/** "50" for TOPPED_50, "6b" for GRADE_6B. */
export function tierMark(code: BadgeCode): string {
  return code.slice(code.indexOf('_') + 1).toLowerCase()
}

/** A badge of a library: its level (tiers earned, or times earned) and its dates. */
export interface BadgeState {
  badge: Badge
  level: number
  /** The tiers, lowest first; the single code of a repeatable badge. */
  codes: BadgeCode[]
  /** When the current level (or the last time) was earned. */
  lastEarnedOn?: string
  firstEarnedOn?: string
}

/** The library as badges, in the catalogue's order. */
export function badgeStates(entries: readonly BadgeLibraryEntry[]): BadgeState[] {
  return BADGES.map((badge) => {
    const mine = entries.filter((entry) => badgeOf(entry.code) === badge)
    const earned = mine.filter((entry) => entry.count > 0)
    const level = isTiered(badge) ? earned.length : (earned[0]?.count ?? 0)
    const last = earned
      .map((entry) => entry.lastEarnedOn)
      .sort()
      .at(-1)
    const first = earned
      .map((entry) => entry.firstEarnedOn)
      .sort()
      .at(0)
    return {
      badge,
      level,
      codes: mine.map((entry) => entry.code),
      ...(last === undefined ? {} : { lastEarnedOn: last }),
      ...(first === undefined ? {} : { firstEarnedOn: first }),
    }
  })
}

/** The `count` badges earned last, latest first (the catalogue's order among a same day). */
export function recentBadges(states: readonly BadgeState[], count: number): BadgeState[] {
  return states
    .filter((state) => state.level > 0 && state.lastEarnedOn !== undefined)
    .map((state, index) => ({ state, index }))
    .sort(
      (a, b) =>
        (b.state.lastEarnedOn ?? '').localeCompare(a.state.lastEarnedOn ?? '') || a.index - b.index,
    )
    .slice(0, count)
    .map(({ state }) => state)
}

/** The code of the next level of a tier family, undefined at the max level or when repeatable. */
export function nextTier(state: BadgeState): BadgeCode | undefined {
  return isTiered(state.badge) ? state.codes[state.level] : undefined
}

/**
 * Where I stand on the way to the next level, counted from zero (6 topped of 10, not 1 of 5):
 * undefined for the grades (no count to show) and without my progress.
 */
export function nextProgress(
  state: BadgeState,
  progress: BadgeProgress | undefined,
): { value: number; target: number } | undefined {
  const next = nextTier(state)
  if (next === undefined || progress === undefined) return undefined
  const counted: Partial<Record<Badge, number>> = {
    TOPPED: progress.topped,
    LEAD: progress.lead,
    CLIMBING_DAYS: progress.climbingDays,
  }
  const value = counted[state.badge]
  if (value === undefined) return undefined
  return { value, target: Number(tierMark(next)) }
}

/** The favourites of a library at their level, as shown next to a name. */
export function shownFavorites(library: BadgeLibrary): ShownBadge[] {
  const states = badgeStates(library.badges)
  return library.favorites
    .map((badge) => ({ badge, level: states.find((state) => state.badge === badge)?.level ?? 0 }))
    .filter((shown) => shown.level > 0)
}
