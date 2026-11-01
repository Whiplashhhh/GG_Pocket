/**
 * The release notes (spec §8.12): CHANGELOG.md, written for the members, is the only source. It is
 * parsed at build time (vite.config.ts, module `virtual:changelog`) into the structure below, so
 * the app never renders raw Markdown. Only the small subset the CHANGELOG uses is understood:
 * `## version — date` sections, `### group` headings, bullet lists (nested by two spaces), bold and
 * links. Pure functions: every case is tested.
 */

export type Inline =
  | { kind: 'text'; text: string }
  | { kind: 'strong'; text: string }
  | { kind: 'link'; text: string; href: string }

export interface ChangelogItem {
  inlines: Inline[]
  children: ChangelogItem[]
}

export interface ChangelogGroup {
  /** As written in the CHANGELOG: « Ajouté », « Modifié », « Corrigé »… */
  title: string
  items: ChangelogItem[]
}

export interface ChangelogRelease {
  version: string
  /** ISO date (YYYY-MM-DD), null when the heading has none. */
  date: string | null
  groups: ChangelogGroup[]
}

const RELEASE_HEADING = /^## +(\d+\.\d+\.\d+)(?: +[—–-] +(\d{4}-\d{2}-\d{2}))? *$/
const BULLET = /^( *)[-*] +(.*)$/

interface OpenItem {
  indent: number
  item: ChangelogItem
  text: string[]
}

/** Every published release, newest first as in the file. « Non publié » is left out. */
export function parseChangelog(markdown: string): ChangelogRelease[] {
  const releases: ChangelogRelease[] = []
  let release: ChangelogRelease | null = null
  let group: ChangelogGroup | null = null
  // The open items, by depth: a bullet indented deeper than the last one is its child.
  const open: OpenItem[] = []

  const close = (entry: OpenItem) => {
    entry.item.inlines = parseInlines(entry.text.join(' '))
  }
  const closeItemsFrom = (indent: number) => {
    for (let last = open.at(-1); last !== undefined && last.indent >= indent; last = open.at(-1)) {
      close(last)
      open.pop()
    }
  }

  for (const line of markdown.split(/\r?\n/)) {
    if (line.startsWith('## ')) {
      closeItemsFrom(0)
      const [, version, date] = RELEASE_HEADING.exec(line) ?? []
      release = version === undefined ? null : { version, date: date ?? null, groups: [] }
      if (release) releases.push(release)
      group = null
      continue
    }
    if (release === null) continue
    if (line.startsWith('### ')) {
      closeItemsFrom(0)
      group = { title: line.slice(4).trim(), items: [] }
      release.groups.push(group)
      continue
    }
    if (line.trim() === '') continue
    const [, spaces, text] = BULLET.exec(line) ?? []
    if (spaces === undefined || text === undefined) {
      // A wrapped line belongs to the deepest open item; loose text outside a list is ignored.
      open.at(-1)?.text.push(line.trim())
      continue
    }
    const entry: OpenItem = {
      indent: spaces.length,
      item: { inlines: [], children: [] },
      text: [text.trim()],
    }
    closeItemsFrom(entry.indent)
    const parent = open.at(-1)
    if (parent) {
      parent.item.children.push(entry.item)
    } else {
      if (group === null) {
        group = { title: '', items: [] }
        release.groups.push(group)
      }
      group.items.push(entry.item)
    }
    open.push(entry)
  }
  closeItemsFrom(0)
  return releases.filter((entry) => entry.groups.some((g) => g.items.length > 0))
}

const INLINE = /\*\*(.+?)\*\*|\[([^\]]+)\]\(([^)\s]+)\)/g

/** Bold and links; anything else stays plain text. Links other than https are kept as text. */
export function parseInlines(text: string): Inline[] {
  const inlines: Inline[] = []
  let last = 0
  const pushText = (value: string) => {
    if (value === '') return
    const previous = inlines.at(-1)
    if (previous?.kind === 'text') previous.text += value
    else inlines.push({ kind: 'text', text: value })
  }
  for (const match of text.matchAll(INLINE)) {
    const [whole, strong, label = '', href = ''] = match
    pushText(text.slice(last, match.index))
    if (strong !== undefined) {
      inlines.push({ kind: 'strong', text: strong })
    } else if (href.startsWith('https://')) {
      inlines.push({ kind: 'link', text: label, href })
    } else {
      pushText(label)
    }
    last = match.index + whole.length
  }
  pushText(text.slice(last))
  return inlines
}

/** Compares two X.Y.Z versions; anything malformed counts as 0.0.0. */
export function compareVersions(a: string, b: string): number {
  const parts = (version: string): number[] => {
    const match = /^(\d+)\.(\d+)\.(\d+)$/.exec(version.trim())
    return match ? match.slice(1).map(Number) : [0, 0, 0]
  }
  const right = parts(b)
  for (const [index, left] of parts(a).entries()) {
    const difference = left - (right[index] ?? 0)
    if (difference !== 0) return Math.sign(difference)
  }
  return 0
}

export function isVersion(value: string | null): value is string {
  return value !== null && /^\d+\.\d+\.\d+$/.test(value)
}

/** At most this many releases in the pop-up; the full list is one tap away. */
export const ANNOUNCED_RELEASES_MAX = 3

/**
 * What the pop-up presents after an update: the releases since the last one I saw, up to the
 * running one, newest first. Never seen anything on this device: only the running release.
 */
export function releasesToAnnounce(
  releases: readonly ChangelogRelease[],
  current: string,
  lastSeen: string | null,
): ChangelogRelease[] {
  const floor = isVersion(lastSeen) ? lastSeen : null
  return releases
    .filter((release) => compareVersions(release.version, current) <= 0)
    .filter((release) =>
      floor === null
        ? compareVersions(release.version, current) === 0
        : compareVersions(release.version, floor) > 0,
    )
    .sort((a, b) => compareVersions(b.version, a.version))
    .slice(0, ANNOUNCED_RELEASES_MAX)
}
