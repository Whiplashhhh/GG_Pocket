import { describe, expect, it } from 'vitest'

import { CSRF_HEADER, csrfMiddleware, readCookie } from './csrf'

async function headerAfter(method: string, cookies: string): Promise<string | null> {
  const middleware = csrfMiddleware(() => cookies)
  const request = new Request('http://localhost/api/v1/auth/login', { method })
  const result = await middleware.onRequest?.({
    request,
    schemaPath: '/api/v1/auth/login',
    params: {},
    options: {} as never,
    id: '1',
  })
  return (result instanceof Request ? result : request).headers.get(CSRF_HEADER)
}

describe('readCookie', () => {
  it('finds a cookie among others and decodes it', () => {
    expect(readCookie('a=1; XSRF-TOKEN=ab%3Dcd; b=2', 'XSRF-TOKEN')).toBe('ab=cd')
    expect(readCookie('a=1', 'XSRF-TOKEN')).toBeUndefined()
  })
})

describe('csrfMiddleware', () => {
  it('sends the token back on writes', async () => {
    expect(await headerAfter('POST', 'XSRF-TOKEN=jeton')).toBe('jeton')
    expect(await headerAfter('DELETE', 'XSRF-TOKEN=jeton')).toBe('jeton')
  })

  it('leaves reads alone', async () => {
    expect(await headerAfter('GET', 'XSRF-TOKEN=jeton')).toBeNull()
  })

  it('sends nothing when there is no cookie yet', async () => {
    expect(await headerAfter('POST', '')).toBeNull()
  })
})
