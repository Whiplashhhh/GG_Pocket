/** Client-side shapes of the route domain, until the topo API exists (phase 3). */
export type RouteStatus = 'ACTIVE' | 'LAST_CHANCE' | 'DISMOUNTED'

/** A configurable hold colour (HoldColor): label shown to members, and its swatch. */
export interface HoldColor {
  label: string
  hex: string
  multicolor?: boolean
}

export type Role = 'CLIMBER' | 'SETTER' | 'BOARD' | 'ADMIN'
