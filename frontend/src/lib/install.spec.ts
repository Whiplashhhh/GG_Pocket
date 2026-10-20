import { describe, expect, it } from 'vitest'

import {
  dismissed,
  installMethod,
  NO_INVITE_HISTORY,
  parseInviteHistory,
  shouldInviteAutomatically,
  type BrowserFacts,
} from './install'

const UA = {
  iphoneSafari:
    'Mozilla/5.0 (iPhone; CPU iPhone OS 26_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Mobile/15E148 Safari/604.1',
  iphoneChrome:
    'Mozilla/5.0 (iPhone; CPU iPhone OS 26_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/141.0 Mobile/15E148 Safari/604.1',
  ipadAsMac:
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15',
  macChrome:
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36',
  androidChrome:
    'Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Mobile Safari/537.36',
  androidFirefox: 'Mozilla/5.0 (Android 15; Mobile; rv:144.0) Gecko/144.0 Firefox/144.0',
  instagram:
    'Mozilla/5.0 (iPhone; CPU iPhone OS 26_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Instagram 400.0.0.0',
  whatsapp: 'WhatsApp/2.25.20 i',
  messenger:
    'Mozilla/5.0 (Linux; Android 15; Pixel 9; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/141.0 Mobile Safari/537.36 [FB_IAB/MESSENGER;FBAV/500.0]',
}

function facts(userAgent: string, overrides: Partial<BrowserFacts> = {}): BrowserFacts {
  return { userAgent, standalone: false, canPrompt: false, maxTouchPoints: 0, ...overrides }
}

describe('installMethod', () => {
  it('never invites an app that is already installed', () => {
    expect(installMethod(facts(UA.androidChrome, { standalone: true, canPrompt: true }))).toBe(
      'installed',
    )
  })

  it('sends in-app browsers to the real browser first', () => {
    expect(installMethod(facts(UA.instagram))).toBe('in-app-browser')
    expect(installMethod(facts(UA.whatsapp))).toBe('in-app-browser')
    expect(installMethod(facts(UA.messenger, { canPrompt: true }))).toBe('in-app-browser')
  })

  it('shows the Share steps on iPhone, whatever the browser, and on iPad seen as a Mac', () => {
    expect(installMethod(facts(UA.iphoneSafari))).toBe('ios')
    expect(installMethod(facts(UA.iphoneChrome))).toBe('ios')
    expect(installMethod(facts(UA.ipadAsMac, { maxTouchPoints: 5 }))).toBe('ios')
  })

  it('uses the native prompt when the browser offered one', () => {
    expect(installMethod(facts(UA.androidChrome, { canPrompt: true }))).toBe('prompt')
    expect(installMethod(facts(UA.macChrome, { canPrompt: true }))).toBe('prompt')
  })

  it('explains the browser menu otherwise', () => {
    expect(installMethod(facts(UA.ipadAsMac))).toBe('mac-safari')
    expect(installMethod(facts(UA.androidFirefox))).toBe('menu')
    expect(installMethod(facts(UA.macChrome))).toBe('menu')
  })
})

describe('shouldInviteAutomatically', () => {
  const DAY = 24 * 60 * 60 * 1000
  const now = Date.UTC(2026, 9, 20)

  it('invites the first time', () => {
    expect(shouldInviteAutomatically('ios', NO_INVITE_HISTORY, now)).toBe(true)
  })

  it('waits 7 days after "Plus tard"', () => {
    const later = dismissed(NO_INVITE_HISTORY, now)
    expect(shouldInviteAutomatically('ios', later, now + 6 * DAY)).toBe(false)
    expect(shouldInviteAutomatically('ios', later, now + 7 * DAY)).toBe(true)
  })

  it('stops after 3 "Plus tard"', () => {
    let history = NO_INVITE_HISTORY
    for (let i = 0; i < 3; i++) {
      history = dismissed(history, now)
    }
    expect(shouldInviteAutomatically('prompt', history, now + 365 * DAY)).toBe(false)
  })

  it('never invites inside the installed app', () => {
    expect(shouldInviteAutomatically('installed', NO_INVITE_HISTORY, now)).toBe(false)
  })
})

describe('parseInviteHistory', () => {
  it('reads what was stored', () => {
    expect(parseInviteHistory('{"dismissals":2,"lastDismissedAt":1000}')).toEqual({
      dismissals: 2,
      lastDismissedAt: 1000,
    })
  })

  it('starts over on anything else', () => {
    expect(parseInviteHistory(null)).toEqual(NO_INVITE_HISTORY)
    expect(parseInviteHistory('pas du json')).toEqual(NO_INVITE_HISTORY)
    expect(parseInviteHistory('{"dismissals":"deux"}')).toEqual(NO_INVITE_HISTORY)
  })
})
