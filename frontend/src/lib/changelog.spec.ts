import { readFileSync } from 'node:fs'

import { describe, expect, it } from 'vitest'

import {
  compareVersions,
  parseChangelog,
  parseInlines,
  releasesToAnnounce,
  type ChangelogRelease,
} from './changelog'

const SAMPLE = `# Notes de version

Toutes les nouveautés, voir [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/).

## Non publié

### Ajouté

- Pas encore là.

## 0.2.0 — 2026-10-24

### Ajouté

- Le **carnet** arrive : tes séances,
  jour par jour.
- Deux écrans :
  - **Fréquentation** ;
  - **Bilan du topo**.

### Corrigé

- Un bug.

## 0.1.0 — 2026-10-21

### Ajouté

- La première version.
`

function release(version: string): ChangelogRelease {
  return { version, date: null, groups: [{ title: 'Ajouté', items: [] }] }
}

describe('parseChangelog', () => {
  const releases = parseChangelog(SAMPLE)

  it('keeps the published releases, newest first, without « Non publié »', () => {
    expect(releases.map((entry) => [entry.version, entry.date])).toEqual([
      ['0.2.0', '2026-10-24'],
      ['0.1.0', '2026-10-21'],
    ])
  })

  it('reads groups, wrapped lines, bold and nested items', () => {
    const [added, fixed] = releases[0]?.groups ?? []
    expect(added?.title).toBe('Ajouté')
    expect(added?.items[0]?.inlines).toEqual([
      { kind: 'text', text: 'Le ' },
      { kind: 'strong', text: 'carnet' },
      { kind: 'text', text: ' arrive : tes séances, jour par jour.' },
    ])
    expect(added?.items[1]?.children.map((child) => child.inlines[0])).toEqual([
      { kind: 'strong', text: 'Fréquentation' },
      { kind: 'strong', text: 'Bilan du topo' },
    ])
    expect(fixed?.items).toHaveLength(1)
  })

  it('leaves out a release without any item', () => {
    expect(parseChangelog('## 1.0.0 — 2026-11-01\n\n### Ajouté\n\n')).toEqual([])
  })

  it('reads the real CHANGELOG: every release has a date and something to say', () => {
    // Vitest runs from frontend/: the real CHANGELOG is one level up.
    const real = parseChangelog(readFileSync('../CHANGELOG.md', 'utf8'))
    expect(real.length).toBeGreaterThan(5)
    for (const entry of real) {
      expect(entry.date).toMatch(/^\d{4}-\d{2}-\d{2}$/)
      expect(entry.groups.every((group) => group.title !== '' && group.items.length > 0)).toBe(true)
    }
    const text = JSON.stringify(real)
    expect(text).not.toContain('**')
    expect(text).not.toContain('](')
  })
})

describe('parseInlines', () => {
  it('keeps https links and turns other links into text', () => {
    expect(parseInlines('voir [le site](https://example.org) ou [là](http://example.org)')).toEqual(
      [
        { kind: 'text', text: 'voir ' },
        { kind: 'link', text: 'le site', href: 'https://example.org' },
        { kind: 'text', text: ' ou là' },
      ],
    )
  })

  it('leaves a lone star alone', () => {
    expect(parseInlines('5* et **gras**')).toEqual([
      { kind: 'text', text: '5* et ' },
      { kind: 'strong', text: 'gras' },
    ])
  })
})

describe('compareVersions', () => {
  it('compares numerically, part by part', () => {
    expect(compareVersions('0.10.0', '0.9.3')).toBe(1)
    expect(compareVersions('1.0.0', '1.0.0')).toBe(0)
    expect(compareVersions('0.9.0', '0.9.1')).toBe(-1)
    expect(compareVersions('garbage', '0.0.1')).toBe(-1)
  })
})

describe('releasesToAnnounce', () => {
  const all = ['1.2.0', '1.1.0', '1.0.1', '1.0.0', '0.9.0'].map(release)
  const versions = (list: ChangelogRelease[]) => list.map((entry) => entry.version)

  it('presents only the running release when nothing was seen on this device', () => {
    expect(versions(releasesToAnnounce(all, '1.1.0', null))).toEqual(['1.1.0'])
    expect(versions(releasesToAnnounce(all, '1.1.0', 'n’importe quoi'))).toEqual(['1.1.0'])
  })

  it('presents what came since the last release seen, up to the running one', () => {
    expect(versions(releasesToAnnounce(all, '1.1.0', '1.0.0'))).toEqual(['1.1.0', '1.0.1'])
  })

  it('presents three releases at most, the newest', () => {
    expect(versions(releasesToAnnounce(all, '1.2.0', '0.1.0'))).toEqual(['1.2.0', '1.1.0', '1.0.1'])
  })

  it('presents nothing when up to date, after a rollback, or without notes', () => {
    expect(releasesToAnnounce(all, '1.1.0', '1.1.0')).toEqual([])
    expect(releasesToAnnounce(all, '1.0.0', '1.1.0')).toEqual([])
    expect(releasesToAnnounce(all, '1.3.0', null)).toEqual([])
  })
})
