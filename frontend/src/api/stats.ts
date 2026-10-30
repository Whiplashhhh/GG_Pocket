import { useQuery } from '@tanstack/vue-query'
import { computed, toValue, type MaybeRefOrGetter } from 'vue'

import { api } from './client'
import { request } from './request'
import type { components } from './schema'

export type Attendance = components['schemas']['Attendance']
export type RouteAttendance = components['schemas']['RouteAttendance']
export type MonthAttendance = components['schemas']['MonthAttendance']

const STATS_KEY = ['stats'] as const

/** The ascents of a month, route by route (spec §8.10, setter+). */
export function useMonthAttendance(month: MaybeRefOrGetter<string>) {
  return useQuery({
    queryKey: computed(() => [...STATS_KEY, 'attendance', toValue(month)] as const),
    queryFn: ({ signal }) =>
      request(
        api.GET('/api/v1/stats/attendance', {
          params: { query: { month: toValue(month) } },
          signal,
        }),
      ),
    placeholderData: (previous) => previous,
  })
}

/** The routes on the wall, with their ascents since they opened (the topo overview). */
export function useRouteAttendance() {
  return useQuery({
    queryKey: [...STATS_KEY, 'routes'] as const,
    queryFn: ({ signal }) => request(api.GET('/api/v1/stats/routes', { signal })),
  })
}
