import type { components } from '@/api/schema'

/** Shapes of the route domain, as the topo API sends them. */
export type RouteSummary = components['schemas']['RouteSummary']
export type RouteStatus = RouteSummary['status']
export type RouteStyle = RouteSummary['styles'][number]
export type ClimbMode = RouteSummary['checks'][number]

/**
 * How a hold colour looks (RouteHold, HoldColor): label shown to members, and its swatch.
 * `multicolor` may be left out by drawings that are never multicoloured (empty states).
 */
export type HoldColor = Pick<components['schemas']['RouteHold'], 'label' | 'hex'> &
  Partial<Pick<components['schemas']['RouteHold'], 'multicolor'>>

export type Role = components['schemas']['CurrentMember']['role']

/** Every route style, in the order the club reads them (DECISIONS D-076). Exhaustive by type. */
const STYLE_ORDER = {
  CONTINUITY: 1,
  ENDURANCE: 2,
  PHYSICAL: 3,
  TECHNICAL: 4,
  COMPLEX: 5,
  SLAB: 6,
  OVERHANG: 7,
  CRIMPS: 8,
  JUGS: 9,
  PINCHES: 10,
  SLOPERS: 11,
  DIHEDRAL: 12,
  ROOF: 13,
  REACHY: 14,
} as const satisfies Record<RouteStyle, number>

export const ROUTE_STYLES = Object.keys(STYLE_ORDER) as RouteStyle[]

export const CLIMB_MODES: readonly ClimbMode[] = ['TOP_ROPE', 'LEAD']
