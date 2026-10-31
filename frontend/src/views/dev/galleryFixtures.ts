import type { BadgeCode, Seniority } from '@/lib/badges'
import type { HoldColor } from '@/lib/route'

/** Default hold colours of the club configuration seed (docs/DESIGN.md §2). Dev gallery only. */
export const HOLD_COLORS: Record<string, HoldColor> = {
  yellow: { label: 'jaune', hex: '#f5c518' },
  orange: { label: 'orange', hex: '#f28c28' },
  red: { label: 'rouge', hex: '#d7263d' },
  pink: { label: 'rose', hex: '#f06eaa' },
  purple: { label: 'violet', hex: '#7b4fb8' },
  blue: { label: 'bleu', hex: '#1f5fbf' },
  lightBlue: { label: 'bleu clair', hex: '#5bc0eb' },
  green: { label: 'vert', hex: '#2e9e44' },
  lightGreen: { label: 'vert clair', hex: '#9bd34a' },
  black: { label: 'noir', hex: '#1a1a1a' },
  white: { label: 'blanc', hex: '#f7f7f2' },
  grey: { label: 'gris', hex: '#9a9a9a' },
  brown: { label: 'marron', hex: '#7a4b2a' },
  multicolor: { label: 'multicolore', hex: '#9a9a9a', multicolor: true },
}

const color = (key: string): HoldColor => HOLD_COLORS[key] ?? { label: key, hex: '#9a9a9a' }

/** Route names in the spirit of the club (the first ones come from the real Oblyk topo). */
export const SAMPLE_ROUTES = [
  { cord: 12, name: 'Bye Bye Morice', grade: '6b', holds: [color('yellow')], status: 'ACTIVE' },
  { cord: 1, name: 'OH purée !', grade: '6b', holds: [color('yellow')], status: 'ACTIVE' },
  {
    cord: 15,
    name: 'Tonton Talon',
    grade: '6a+',
    holds: [color('orange'), color('black')],
    status: 'LAST_CHANCE',
  },
  { cord: 27, name: 'Réglisse', grade: '6a+', holds: [color('purple')], status: 'ACTIVE' },
  { cord: 26, name: 'Schtroumpfs', grade: '5b', holds: [color('blue')], status: 'DISMOUNTED' },
  { cord: 23, name: "P'tite Line", grade: '4a', holds: [color('white')], status: 'ACTIVE' },
  {
    cord: 20,
    name: 'Le grand toit qui ne pardonne pas les bras de T-Rex',
    grade: '7c',
    holds: [color('multicolor')],
    status: 'ACTIVE',
  },
] as const

/** One badge per drawing (docs/DESIGN.md §7). Dev gallery only. */
export const SAMPLE_BADGES: readonly BadgeCode[] = [
  'TOPPED_50',
  'LEAD_10',
  'GRADE_6A',
  'DAYS_100',
  'WEEK_2',
  'MONTH_COMPLETE',
  'STREAK_3W',
  'MONTH_NO_MISS',
  'FULL_LEAD',
  'PERFECT',
  'ZERO_REST',
  'COMEBACK',
  'FLASH',
  'RECORD',
]

export const SENIORITIES: readonly Seniority[] = [
  'FRESH',
  'BABY_CARABINER',
  'PRO_BEGINNER',
  'FORCER',
  'RESIDENT',
  'FOSSIL',
]
