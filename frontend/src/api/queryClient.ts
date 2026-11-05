import { MutationCache, QueryCache, QueryClient } from '@tanstack/vue-query'

import { forgetOfflineReads } from '@/lib/offlineReads'

import { AUTH_STATE_KEY, SIGNED_OUT } from './auth'
import { ApiError, isRetryable } from './errors'

const MAX_RETRIES = 2

export function createQueryClient(): QueryClient {
  // A 401 anywhere means the session is gone (expired, closed from another device, account
  // disabled): the app shell sees the signed-out state and sends the member to the sign-in screen.
  const onError = (error: unknown) => {
    if (error instanceof ApiError && error.code === 'UNAUTHENTICATED') {
      // Like a sign-out: nothing of the previous member stays on the device, the offline copies
      // of their profile, planning and "à faire" included (security review, D-112).
      client.removeQueries({ predicate: (query) => query.queryKey[0] !== AUTH_STATE_KEY[0] })
      forgetOfflineReads()
      client.setQueryData(AUTH_STATE_KEY, SIGNED_OUT)
    }
  }
  const client: QueryClient = new QueryClient({
    queryCache: new QueryCache({ onError }),
    mutationCache: new MutationCache({ onError }),
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        // Only retry what can succeed later; a 403 stays a 403.
        retry: (failureCount, error) => failureCount < MAX_RETRIES && isRetryable(error),
      },
    },
  })
  return client
}

/** The one client of the app, shared with the router guard. */
export const queryClient = createQueryClient()
