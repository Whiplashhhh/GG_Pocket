import { readFileSync } from 'node:fs'

import { describe, expect, it } from 'vitest'

import { frenchSpacing, renderGuide, slugify } from './guide'

describe('slugify', () => {
  it('keeps letters and digits, without accents', () => {
    expect(slugify('8. Démarrage et passation')).toBe('8-demarrage-et-passation')
    expect(slugify('Qui peut faire quoi ?')).toBe('qui-peut-faire-quoi')
  })
})

describe('frenchSpacing', () => {
  it('ties « », :, ;, ? and ! to their word', () => {
    expect(frenchSpacing('« Touriste » : oui ; non ? si !')).toBe(
      '«\u00a0Touriste\u00a0»\u00a0: oui\u00a0; non\u00a0? si\u00a0!',
    )
  })
})

describe('renderGuide', () => {
  const sample = renderGuide(`# Guide

Intro.

## 1. Les rôles

| Rôle | Droits |
|---|---|
| **Ouvreur·se** | Les voies |

### Détails

<script>alert(1)</script>

Voir [le site](https://example.org).

## 1. Les rôles
`)

  it('leaves out the document title: the screen has its own', () => {
    expect(sample.html).not.toContain('<h1')
    expect(sample.html).not.toContain('Guide</')
  })

  it('lists the sections with unique anchors', () => {
    expect(sample.sections).toEqual([
      { id: '1-les-roles', title: '1. Les rôles' },
      { id: '1-les-roles-2', title: '1. Les rôles' },
    ])
    expect(sample.html).toContain('<h2 id="1-les-roles" tabindex="-1">')
    expect(sample.html).toContain('<h3 id="details" tabindex="-1">')
  })

  it('wraps tables so they scroll on a phone', () => {
    expect(sample.html).toContain('<div class="guide-table"><table>')
    expect(sample.html).toContain('<strong>Ouvreur·se</strong>')
  })

  it('never lets raw HTML through', () => {
    expect(sample.html).not.toContain('<script>')
    expect(sample.html).toContain('&lt;script&gt;')
  })

  it('opens outside links in a new tab', () => {
    expect(sample.html).toContain(
      '<a href="https://example.org" target="_blank" rel="noopener noreferrer">',
    )
  })

  it('renders the real guide: sections, no title, no raw HTML', () => {
    // Vitest runs from frontend/: the guide is in the repository's docs/.
    const real = renderGuide(readFileSync('../docs/GUIDE_CLUB.md', 'utf8'))
    expect(real.sections.length).toBeGreaterThanOrEqual(5)
    expect(real.sections.every((section) => section.id !== '' && section.title !== '')).toBe(true)
    expect(real.html).not.toMatch(/<h1|<script|<iframe/)
    expect(real.html).toContain('Qui peut faire quoi')
    expect(real.html).not.toContain("'")
  })

  it('uses no arrow: the app font has none (› instead)', () => {
    for (const file of ['../docs/GUIDE_CLUB.md', '../CHANGELOG.md']) {
      expect(readFileSync(file, 'utf8'), file).not.toMatch(/[←→↑↓⇒]/)
    }
  })
})
