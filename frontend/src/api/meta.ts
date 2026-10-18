import { useQuery } from '@tanstack/vue-query'

import { api } from './client'
import { request } from './request'
import type { components } from './schema'

export type Meta = components['schemas']['Meta']

export function useMetaQuery() {
  return useQuery({
    queryKey: ['meta'],
    queryFn: ({ signal }) => request(api.GET('/api/v1/meta', { signal })),
    staleTime: 5 * 60_000,
  })
}
