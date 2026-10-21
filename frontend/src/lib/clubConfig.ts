import type { Cord, HoldColorConfig, Sector, TopoConfig } from '@/api/topo'
import type { CordSettings, HoldColorSettings, SectorSettings } from '@/api/topoAdmin'

import type { FieldErrors } from './forms'

/**
 * The club configuration (spec §8.11, admin only): sectors, cords and hold colours. Nothing is
 * deleted, so the history of the routes stays right: cords and colours are deactivated instead.
 * Same rules as the backend, checked before sending.
 */
export const SECTOR_NAME_MAX = 60
export const SECTOR_DESCRIPTION_MAX = 160
export const COLOR_LABEL_MAX = 30
export const CORD_NUMBER_MAX = 99
export const HEX_PATTERN = /^#[0-9A-Fa-f]{6}$/

/** Sectors in their order on the wall, from left to right. */
export function orderedSectors(config: TopoConfig): Sector[] {
  return [...config.sectors].sort((a, b) => a.position - b.position)
}

/** The new order of all the sectors after moving one up (-1) or down (+1); null at an end. */
export function moveSector(
  sectors: readonly Pick<Sector, 'id'>[],
  id: string,
  offset: -1 | 1,
): string[] | null {
  const ids = sectors.map((sector) => sector.id)
  const from = ids.indexOf(id)
  const to = from + offset
  if (from === -1 || to < 0 || to >= ids.length) return null
  ids.splice(from, 1)
  ids.splice(to, 0, id)
  return ids
}

/** The configuration as it will be once reordered: shown at once, before the server answers. */
export function withSectorOrder(config: TopoConfig, sectorIds: readonly string[]): TopoConfig {
  const sectors = sectorIds
    .map((id) => config.sectors.find((sector) => sector.id === id))
    .filter((sector): sector is Sector => sector !== undefined)
    .map((sector, index) => ({ ...sector, position: index + 1 }))
  return { ...config, sectors }
}

export interface SectorForm {
  name: string
  description: string
}

export function toSectorForm(sector: Sector | undefined): SectorForm {
  return { name: sector?.name ?? '', description: sector?.description ?? '' }
}

export function sectorSettings(
  form: SectorForm,
): { settings: SectorSettings } | { errors: FieldErrors<keyof SectorForm> } {
  const errors: FieldErrors<keyof SectorForm> = {}
  const name = form.name.trim()
  const description = form.description.trim()
  if (name === '') {
    errors.name = { key: 'validation.required' }
  } else if (name.length > SECTOR_NAME_MAX) {
    errors.name = { key: 'validation.tooLong', params: { max: SECTOR_NAME_MAX } }
  }
  if (description.length > SECTOR_DESCRIPTION_MAX) {
    errors.description = { key: 'validation.tooLong', params: { max: SECTOR_DESCRIPTION_MAX } }
  }
  if (Object.keys(errors).length > 0) return { errors }
  return { settings: { name, ...(description === '' ? {} : { description }) } }
}

export interface CordForm {
  number: string
  sectorId: string
  active: boolean
}

/** The next number after the highest cord: what a new cord usually gets. */
export function suggestedCordNumber(config: TopoConfig): number {
  const numbers = config.sectors.flatMap((sector) => sector.cords.map((cord) => cord.number))
  return Math.min(CORD_NUMBER_MAX, Math.max(0, ...numbers) + 1)
}

export function toCordForm(cord: Cord | undefined, sectorId: string, number?: number): CordForm {
  return {
    number: String(cord?.number ?? number ?? ''),
    sectorId: cord?.sectorId ?? sectorId,
    active: cord?.active ?? true,
  }
}

export function cordSettings(
  form: CordForm,
): { settings: CordSettings } | { errors: FieldErrors<keyof CordForm> } {
  const errors: FieldErrors<keyof CordForm> = {}
  const number = form.number.trim()
  if (!/^\d{1,2}$/.test(number) || Number(number) < 1) {
    errors.number = { key: 'clubConfig.errors.cordNumber', params: { max: CORD_NUMBER_MAX } }
  }
  if (form.sectorId === '') {
    errors.sectorId = { key: 'validation.required' }
  }
  if (Object.keys(errors).length > 0) return { errors }
  return { settings: { number: Number(number), sectorId: form.sectorId, active: form.active } }
}

export interface HoldColorForm {
  label: string
  hex: string
  multicolor: boolean
  active: boolean
}

export function toHoldColorForm(color: HoldColorConfig | undefined): HoldColorForm {
  return {
    label: color?.label ?? '',
    hex: color?.hex.toUpperCase() ?? '#808080',
    multicolor: color?.multicolor ?? false,
    active: color?.active ?? true,
  }
}

export function holdColorSettings(
  form: HoldColorForm,
): { settings: HoldColorSettings } | { errors: FieldErrors<keyof HoldColorForm> } {
  const errors: FieldErrors<keyof HoldColorForm> = {}
  const label = form.label.trim()
  const typed = form.hex.trim()
  // « F28C28 » typed without its hash is understood.
  const hex = /^[0-9A-Fa-f]{6}$/.test(typed) ? `#${typed}` : typed
  if (label === '') {
    errors.label = { key: 'validation.required' }
  } else if (label.length > COLOR_LABEL_MAX) {
    errors.label = { key: 'validation.tooLong', params: { max: COLOR_LABEL_MAX } }
  }
  if (!HEX_PATTERN.test(hex)) {
    errors.hex = { key: 'clubConfig.errors.hex' }
  }
  if (Object.keys(errors).length > 0) return { errors }
  return {
    settings: {
      label,
      hex: hex.toUpperCase(),
      multicolor: form.multicolor,
      active: form.active,
    },
  }
}
