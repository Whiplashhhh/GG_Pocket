import type { components } from '@/api/schema'

export type SessionReactions = components['schemas']['SessionReactions']
export type ReactionKind = components['schemas']['ReactionCount']['kind']

/** The four reactions of the feed, in their fixed order (spec §8.2, docs/DESIGN.md). */
export const REACTION_KINDS = [
  'FIRE',
  'STRONG',
  'CLAP',
  'GORILLA',
] as const satisfies ReactionKind[]

/** The only emoji of the interface: reactions are emoji by nature (docs/DESIGN.md). */
export const REACTION_EMOJI: Record<ReactionKind, string> = {
  FIRE: '🔥',
  STRONG: '💪',
  CLAP: '👏',
  GORILLA: '🦍',
}

/** Tapping my reaction takes it back; tapping another one changes it. */
export function nextReaction(
  mine: ReactionKind | undefined,
  tapped: ReactionKind,
): ReactionKind | undefined {
  return mine === tapped ? undefined : tapped
}

/** The reactions as they will be once mine is `next`: shown at once, before the server answers. */
export function withMyReaction(
  reactions: SessionReactions,
  next: ReactionKind | undefined,
): SessionReactions {
  const counts = reactions.counts.map((entry) => {
    let count = entry.count
    if (entry.kind === reactions.mine) count -= 1
    if (entry.kind === next) count += 1
    return { kind: entry.kind, count: Math.max(0, count) }
  })
  return next === undefined ? { counts } : { counts, mine: next }
}

/** Total of all four, for the label of a session without any. */
export function reactionTotal(reactions: SessionReactions): number {
  return reactions.counts.reduce((total, entry) => total + entry.count, 0)
}
