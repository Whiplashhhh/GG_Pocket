/**
 * Kept apart from reactions.ts, without any import: the build reads it too (the push service
 * worker, vite.config.ts), where the app's path aliases do not exist.
 */
export type ReactionKind = 'FIRE' | 'STRONG' | 'CLAP' | 'GORILLA'

/** The only emoji of the interface: reactions are emoji by nature (docs/DESIGN.md). */
export const REACTION_EMOJI: Record<ReactionKind, string> = {
  FIRE: '🔥',
  STRONG: '💪',
  CLAP: '👏',
  GORILLA: '🦍',
}
