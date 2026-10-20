import { MutationCache, QueryCache, QueryClient } from '@tanstack/vue-query'

import { AUTH_STATE_KEY, SIGNED_OUT } from './auth'
import { ApiError, isRetryable } from './errors'

const MAX_RETRIES = 2

export function createQueryClient(): QueryClient {
  // A 401 anywhere means the session is gone (expired, closed from another device, account
  // disabled): the app shell sees the signed-out state and sends the member to the sign-in screen.
  const onError = (error: unknown) => {
    if (error instanceof ApiError && error.code === 'UNAUTHENTICATED') {
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
