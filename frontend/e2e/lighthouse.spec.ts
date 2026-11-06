import { mkdir, mkdtemp, readFile, writeFile } from 'node:fs/promises'
import { createServer, type IncomingMessage, type Server, type ServerResponse } from 'node:http'
import type { AddressInfo } from 'node:net'
import { tmpdir } from 'node:os'
import { extname, join, normalize } from 'node:path'
import { gzipSync } from 'node:zlib'

import { chromium, expect, test, type Route } from '@playwright/test'
import lighthouse from 'lighthouse'

import { FakeClub } from './fakeClub'
import { CAPTURES } from './scenarios'

/**
 * Lighthouse audit (spec §13: performance, accessibility and best practices ≥ 90 on mobile, on the
 * main screens): `pnpm build && pnpm lighthouse`. The production build is served the way Caddy
 * serves it (compressed, hashed assets cached for a year), the API answered by {@link FakeClub}
 * with the club of the screenshots. Reports: lighthouse/<screen>.{html,json} (git-ignored).
 */
const SCREENS = [
  'accueil',
  'connexion',
  'actu-fil',
  'topo',
  'a-faire',
  'planning',
  'carnet',
  'podium',
  'profil',
] as const
const CATEGORIES = ['performance', 'accessibility', 'best-practices'] as const
const MIN_SCORE = 0.9
const DIST = new URL('../dist/', import.meta.url).pathname
const REPORTS = new URL('../lighthouse/', import.meta.url).pathname
const DEBUGGING_PORT = 9333
const chromiumExecutable = process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH

const TYPES: Record<string, string> = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json',
  '.webmanifest': 'application/manifest+json',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon',
  '.woff2': 'font/woff2',
  '.txt': 'text/plain; charset=utf-8',
}
const COMPRESSED = /^(text\/|application\/(json|manifest)|image\/svg)/

async function serveFile(path: string, response: ServerResponse, request: IncomingMessage) {
  const safe = normalize(path).replace(/^(\.\.[/\\])+/, '')
  let file = join(DIST, safe)
  let body: Buffer
  try {
    body = await readFile(file)
  } catch {
    // The app's own routes: the single page answers them all.
    file = join(DIST, 'index.html')
    body = await readFile(file)
  }
  const type = TYPES[extname(file)] ?? 'application/octet-stream'
  const headers: Record<string, string> = {
    'content-type': type,
    'cache-control': file.includes('/assets/') ? 'public, max-age=31536000, immutable' : 'no-cache',
  }
  if (COMPRESSED.test(type) && (request.headers['accept-encoding'] ?? '').includes('gzip')) {
    body = gzipSync(body)
    headers['content-encoding'] = 'gzip'
    headers.vary = 'Accept-Encoding'
  }
  response.writeHead(200, headers).end(body)
}

/** The few parts of Playwright's Route that {@link FakeClub} uses, over a plain HTTP exchange. */
function routeOf(request: IncomingMessage, body: Buffer, response: ServerResponse): Route {
  const shim = {
    request: () => ({
      allHeaders: () => Promise.resolve(request.headers),
      method: () => request.method ?? 'GET',
      url: () => `http://localhost${request.url ?? '/'}`,
      postDataJSON: (): unknown => (body.length > 0 ? JSON.parse(body.toString()) : null),
    }),
    fulfill: (answer: {
      status?: number
      json?: unknown
      body?: string | Buffer
      contentType?: string
    }) => {
      const payload = answer.json === undefined ? answer.body : JSON.stringify(answer.json)
      const type = answer.json === undefined ? answer.contentType : 'application/json'
      response.writeHead(answer.status ?? 200, type ? { 'content-type': type } : {})
      response.end(payload)
      return Promise.resolve()
    },
    abort: () => {
      response.destroy()
      return Promise.resolve()
    },
  }
  return shim as unknown as Route
}

function serve(club: FakeClub): Promise<Server> {
  const server = createServer((request, response) => {
    const chunks: Buffer[] = []
    request.on('data', (chunk: Buffer) => chunks.push(chunk))
    request.on('end', () => {
      const path = new URL(request.url ?? '/', 'http://localhost').pathname
      const answer = path.startsWith('/api/')
        ? club.handle(routeOf(request, Buffer.concat(chunks), response))
        : serveFile(path, response, request)
      answer.catch(() => {
        response.destroy()
      })
    })
  })
  return new Promise((resolve) =>
    server.listen(0, '127.0.0.1', () => {
      resolve(server)
    }),
  )
}

for (const name of SCREENS) {
  test(`lighthouse ${name}`, async () => {
    const capture = CAPTURES.find((scenario) => scenario.name === name)
    if (capture === undefined) throw new Error(`No scenario named ${name}`)
    const club = new FakeClub()
    // Lighthouse's tab runs on the real clock: the club lives on the same day (no month ahead).
    club.now = Date.now()
    club.topo.quietLastChance = true
    if (capture.as !== null) club.signIn(club.addMember({ nickname: 'marie', role: capture.as }))
    capture.prepare?.(club)
    const server = await serve(club)
    const origin = `http://localhost:${String((server.address() as AddressInfo).port)}`

    // Lighthouse drives its own tab of this browser; the storage set here keeps the welcome
    // pop-ups (install, news) away, as they are for a member who has seen them.
    const browser = await chromium.launchPersistentContext(await mkdtemp(join(tmpdir(), 'lh-')), {
      args: [`--remote-debugging-port=${String(DEBUGGING_PORT)}`],
      // The full Chromium in its new headless mode, the one Lighthouse is made for.
      ...(chromiumExecutable ? { executablePath: chromiumExecutable } : { channel: 'chromium' }),
    })
    try {
      const page = await browser.newPage()
      await page.goto(`${origin}/tes-donnees`)
      await page.evaluate(
        (ids) => {
          localStorage.setItem(
            'gg-pocket:install-invite',
            JSON.stringify({ dismissals: 3, lastDismissedAt: Date.now() }),
          )
          for (const id of ids) localStorage.setItem(`gg-pocket:release-notes:${id}`, '999.0.0')
        },
        club.members.map((member) => member.id),
      )
      await page.close()

      const result = await lighthouse(`${origin}${capture.path}`, {
        port: DEBUGGING_PORT,
        output: ['html', 'json'],
        logLevel: 'error',
        onlyCategories: [...CATEGORIES],
        disableStorageReset: true,
      })
      if (result === undefined) throw new Error('Lighthouse returned nothing')
      await mkdir(REPORTS, { recursive: true })
      const [html, json] = result.report as [string, string]
      await writeFile(join(REPORTS, `${name}.html`), html)
      await writeFile(join(REPORTS, `${name}.json`), json)

      const scores = Object.fromEntries(
        CATEGORIES.map((category) => [category, result.lhr.categories[category].score ?? 0]),
      )
      console.log(name, JSON.stringify(scores))
      for (const category of CATEGORIES) {
        expect.soft(scores[category], `${name}: ${category}`).toBeGreaterThanOrEqual(MIN_SCORE)
      }
    } finally {
      await browser.close()
      server.close()
    }
  })
}
