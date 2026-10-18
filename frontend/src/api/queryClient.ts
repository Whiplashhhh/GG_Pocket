import { QueryClient } from '@tanstack/vue-query'

import { isRetryable } from './errors'

const MAX_RETRIES = 2

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        // Only retry what can succeed later; a 403 stays a 403.
        retry: (failureCount, error) => failureCount < MAX_RETRIES && isRetryable(error),
      },
    },
  })
}
