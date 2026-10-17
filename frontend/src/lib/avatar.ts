/**
 * Background colours for initials avatars. All are light enough for ink text (#17140f) to reach
 * the WCAG AA contrast in both themes (checked in avatar.spec.ts).
 */
export const AVATAR_COLORS = [
  '#c3c04c',
  '#f4a582',
  '#e3c79a',
  '#9cc9e0',
  '#eba6b8',
  '#a9c8a0',
  '#c9b6e4',
] as const

/** "Marie", "Hhh" → "MH". Keeps accents, ignores empty parts. */
export function initialsOf(firstName: string, lastName: string): string {
  return [firstName, lastName]
    .map((part) => part.trim().charAt(0))
    .join('')
    .toLocaleUpperCase('fr')
}

/** Same seed, same colour: a member keeps their colour everywhere. */
export function avatarColor(seed: string): (typeof AVATAR_COLORS)[number] {
  let hash = 0
  for (const char of seed) {
    hash = (hash * 31 + (char.codePointAt(0) ?? 0)) | 0
  }
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length] ?? AVATAR_COLORS[0]
}
