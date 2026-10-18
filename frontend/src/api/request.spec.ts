import { describe, expect, it } from 'vitest'

import { ApiError } from './errors'
import { request } from './request'

const response = (status: number) => new Response(null, { status })

describe('request', () => {
  it('returns the data of a successful call', async () => {
    await expect(
      request(Promise.resolve({ data: { ok: true }, response: response(200) })),
    ).resolves.toEqual({
      ok: true,
    })
  })

  it('turns a Problem Details answer into an ApiError', async () => {
    const call = Promise.resolve({ error: { code: 'NOT_FOUND' }, response: response(404) })
    await expect(request(call)).rejects.toMatchObject({ code: 'NOT_FOUND', status: 404 })
  })

  it('reports a network failure as NETWORK', async () => {
    const call = Promise.reject(new TypeError('Failed to fetch'))
    const error: unknown = await request(call).catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect((error as ApiError).code).toBe('NETWORK')
  })
})
