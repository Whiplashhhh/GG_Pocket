import { describe, expect, it } from 'vitest'

import { applicationServerKey, pushSupport, sameServerKey, type PushFacts } from './push'

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

describe('sameServerKey', () => {
  const key =
    'BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8'

  it('recognises the key a subscription was made with', () => {
    expect(sameServerKey(applicationServerKey(key).buffer, key)).toBe(true)
  })

  it('tells a subscription made with other keys, or none known', () => {
    const other = applicationServerKey(key)
    other[10] = (other[10] ?? 0) ^ 1
    expect(sameServerKey(other.buffer, key)).toBe(false)
    expect(sameServerKey(new ArrayBuffer(3), key)).toBe(false)
    expect(sameServerKey(null, key)).toBe(false)
  })
})
