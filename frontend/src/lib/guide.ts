import MarkdownIt from 'markdown-it'

/**
 * The club guide (docs/GUIDE_CLUB.md, DECISIONS D-110), turned into HTML at build time only
 * (vite.config.ts, module `virtual:club-guide`): markdown-it never reaches the app. The source is
 * the repository, not the members, and raw HTML in it is refused anyway (`html: false`).
 */
export interface GuideSection {
  id: string
  title: string
}

export interface ClubGuide {
  html: string
  /** The `##` headings, for the table of contents. */
  sections: GuideSection[]
}

/** "8. Démarrage et passation" → "8-demarrage-et-passation". */
export function slugify(text: string): string {
  return text
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
}

/** French typography: no « nor : left alone at the start of a line. */
export function frenchSpacing(text: string): string {
  return text.replace(/« /g, '«\u00a0').replace(/ ([»:;?!])/g, '\u00a0$1')
}

export function renderGuide(markdown: string): ClubGuide {
  // The typographer turns straight apostrophes into curly ones, like everywhere else in the app.
  const md = new MarkdownIt({ html: false, linkify: false, typographer: true })
  // A wide table scrolls by itself on a phone instead of stretching the page.
  md.renderer.rules.table_open = () => '<div class="guide-table"><table>\n'
  md.renderer.rules.table_close = () => '</table></div>\n'

  // The screen has its own title: the document's `#` heading is left out.
  const tokens = md.parse(markdown.replace(/^# .*\r?\n/, ''), {})
  const sections: GuideSection[] = []
  const used = new Set<string>()
  tokens.forEach((token, index) => {
    if (token.type === 'heading_open') {
      const inline = tokens[index + 1]
      const title = (inline?.children ?? [])
        .filter((child) => child.type === 'text' || child.type === 'code_inline')
        .map((child) => child.content)
        .join('')
      let id = slugify(title) || 'section'
      for (let suffix = 2; used.has(id); suffix++) id = `${slugify(title)}-${String(suffix)}`
      used.add(id)
      token.attrSet('id', id)
      token.attrSet('tabindex', '-1')
      if (token.tag === 'h2') sections.push({ id, title })
    }
    if (token.type === 'inline') {
      for (const child of token.children ?? []) {
        if (child.type === 'text') child.content = frenchSpacing(child.content)
        const href = child.type === 'link_open' ? child.attrGet('href') : null
        if (typeof href === 'string' && /^https?:/.test(href)) {
          child.attrSet('target', '_blank')
          child.attrSet('rel', 'noopener noreferrer')
        }
      }
    }
  })
  return { html: md.renderer.render(tokens, md.options, {}), sections }
}
