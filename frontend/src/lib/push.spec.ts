import { describe, expect, it } from 'vitest'

import { applicationServerKey, pushSupport, type PushFacts } from './push'

const ready: PushFacts = {
  hasPushApi: true,
  permission: 'default',
  ios: false,
  standalone: false,
  serverKey: 'BP4z',
}

describe('pushSupport', () => {
  it('is ready on Android or a computer, installed or not', () => {
    expect(pushSupport(ready)).toBe('ready')
    expect(pushSupport({ ...ready, permission: 'granted' })).toBe('ready')
  })

  it('asks to install the app first on an iPhone', () => {
    expect(pushSupport({ ...ready, ios: true, hasPushApi: false })).toBe('needsInstall')
    expect(pushSupport({ ...ready, ios: true, standalone: true })).toBe('ready')
  })

  it('says when the browser was told no, or cannot', () => {
    expect(pushSupport({ ...ready, permission: 'denied' })).toBe('denied')
    expect(pushSupport({ ...ready, hasPushApi: false })).toBe('unsupported')
    expect(pushSupport({ ...ready, serverKey: undefined })).toBe('unsupported')
  })
})

describe('applicationServerKey', () => {
  it('decodes base64url without padding', () => {
    expect([...applicationServerKey('BP4z_-8')]).toEqual([4, 254, 51, 255, 239])
  })
})
