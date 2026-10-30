import { useQuery } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { request } from './request'
import type { components } from './schema'

export type Ranking = components['schemas']['Ranking']
export type RankingBoard = components['schemas']['RankingBoard']
export type RankingLine = components['schemas']['RankingLine']
export type RankingMonth = components['schemas']['RankingMonth']
export type RankingStatus = Ranking['status']

/** Every month of the podium lives under it: [...RANKINGS_KEY, '2026-10']. */
export const RANKINGS_KEY = ['rankings'] as const
const RANKING_MONTHS_KEY = [...RANKINGS_KEY, 'months'] as const

/** One month of the podium (spec §8.7): both rankings and my lines. */
export function useRanking(month: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => [...RANKINGS_KEY, toValue(month)] as const),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/rankings', { params: { query: { month: toValue(month) } }, signal }),
      ),
    placeholderData: (previous) => previous,
  })
}

/** The months that have rankings, this one first. */
export function useRankingMonths() {
  return useQuery({
    queryKey: RANKING_MONTHS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/rankings/months', { signal })),
  })
}
