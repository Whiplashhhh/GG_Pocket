import type { RankingLine } from '@/api/rankings'

/** "Ancien·ne membre": the frozen ranks of deleted accounts (D-096), with no profile to open. */
export const FORMER_MEMBER_ID = '00000000-0000-0000-0000-00000000a5c1'

export type PodiumStep = {
  line: RankingLine
  /** The height of the step: by rank, so that ties stand at the same height. */
  height: 'first' | 'second' | 'third'
  /** Where it stands: second on the left, first in the middle, third on the right. */
  column: 1 | 2 | 3
}

const COLUMNS = [2, 1, 3] as const

/** The top three, best first (the reading order); each says where it stands. */
export function podiumSteps(lines: readonly RankingLine[]): PodiumStep[] {
  const height = (rank: number): PodiumStep['height'] =>
    rank === 1 ? 'first' : rank === 2 ? 'second' : 'third'
  return lines.slice(0, 3).map((line, index) => ({
    line,
    height: height(line.rank),
    column: COLUMNS[index] ?? 3,
  }))
}

/** Everyone under the podium. */
export function belowPodium(lines: readonly RankingLine[]): RankingLine[] {
  return lines.slice(3)
}

/** Whether someone else shares this line's rank. */
export function isTie(lines: readonly RankingLine[], line: RankingLine): boolean {
  return lines.some((other) => other.member.id !== line.member.id && other.rank === line.rank)
}

/** Where I am: the profile page for me, a member's page otherwise, nowhere for a former member. */
export function profileLink(line: RankingLine, myId: string | undefined): string | undefined {
  if (line.member.id === FORMER_MEMBER_ID) return undefined
  return line.member.id === myId ? '/profil' : `/membres/${line.member.id}`
}

const FREEZE_DAY = new Intl.DateTimeFormat('fr-FR', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  timeZone: 'Europe/Paris',
})

/** "samedi 8 novembre": the day a month freezes, Gravelines time. */
export function formatFreezeDay(instant: string): string {
  return FREEZE_DAY.format(new Date(instant))
}
