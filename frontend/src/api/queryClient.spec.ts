import { afterEach, describe, expect, it, vi } from 'vitest'

import * as offlineReads from '@/lib/offlineReads'

import { AUTH_STATE_KEY, SIGNED_OUT } from './auth'
import { ApiError } from './errors'
import { createQueryClient } from './queryClient'

describe('a 401 anywhere', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('signs out and leaves nothing of the previous member behind (D-112)', async () => {
    const forget = vi.spyOn(offlineReads, 'forgetOfflineReads').mockImplementation(() => undefined)
    const client = createQueryClient()
    client.setQueryData(['me', 'planning'], { weeks: [] })

    await client
      .query({
        queryKey: ['me', 'todo'],
        queryFn: () => Promise.reject(new ApiError('UNAUTHENTICATED', { status: 401 })),
        retry: false,
      })
      .catch(() => undefined)

    expect(client.getQueryData(AUTH_STATE_KEY)).toEqual(SIGNED_OUT)
    expect(client.getQueryData(['me', 'planning'])).toBeUndefined()
    expect(forget).toHaveBeenCalled()
  })
})
