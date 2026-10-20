import type { Middleware } from 'openapi-fetch'

/** Cookie set by the server (Spring Security, SPA mode) and header it expects back on writes. */
export const CSRF_COOKIE = 'XSRF-TOKEN'
export const CSRF_HEADER = 'X-XSRF-TOKEN'

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE'])

export function readCookie(cookies: string, name: string): string | undefined {
  for (const part of cookies.split(';')) {
    const [key, ...value] = part.trim().split('=')
    if (key === name) {
      return decodeURIComponent(value.join('='))
    }
  }
  return undefined
}

/**
 * Copies the CSRF cookie into the header on every write (spec §6: token in a cookie readable by
 * the front, sent back in a header). The cookie is set by GET /api/v1/auth/state at start-up.
 */
export function csrfMiddleware(readCookies: () => string = () => document.cookie): Middleware {
  return {
    onRequest({ request }) {
      if (SAFE_METHODS.has(request.method.toUpperCase())) {
        return undefined
      }
      const token = readCookie(readCookies(), CSRF_COOKIE)
      if (token !== undefined) {
        request.headers.set(CSRF_HEADER, token)
      }
      return request
    },
  }
}
