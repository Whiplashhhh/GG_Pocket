const HEX_COLOR = /^#([0-9a-f]{6})$/i

function channels(hex: string): [number, number, number] {
  const match = HEX_COLOR.exec(hex)
  if (!match?.[1]) {
    throw new Error(`Expected a #rrggbb colour, got "${hex}"`)
  }
  const value = Number.parseInt(match[1], 16)
  return [(value >> 16) & 0xff, (value >> 8) & 0xff, value & 0xff]
}

function toHex(red: number, green: number, blue: number): string {
  return `#${[red, green, blue].map((c) => Math.round(c).toString(16).padStart(2, '0')).join('')}`
}

/** Mixes a #rrggbb colour towards black: 0 keeps it, 1 gives black. */
export function darken(hex: string, amount: number): string {
  const ratio = 1 - Math.min(Math.max(amount, 0), 1)
  const [red, green, blue] = channels(hex)
  return toHex(red * ratio, green * ratio, blue * ratio)
}

/** WCAG relative luminance, from 0 (black) to 1 (white). */
export function relativeLuminance(hex: string): number {
  const [red, green, blue] = channels(hex).map((c) => {
    const s = c / 255
    return s <= 0.03928 ? s / 12.92 : ((s + 0.055) / 1.055) ** 2.4
  }) as [number, number, number]
  return 0.2126 * red + 0.7152 * green + 0.0722 * blue
}

/** WCAG contrast ratio between two #rrggbb colours (1 to 21). */
export function contrastRatio(first: string, second: string): number {
  const [lighter, darker] = [relativeLuminance(first), relativeLuminance(second)].sort(
    (a, b) => b - a,
  ) as [number, number]
  return (lighter + 0.05) / (darker + 0.05)
}
