import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'

import { AUDIT_KEY } from './admin'
import { MY_BADGES_KEY } from './badges'
import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { MY_PLANNING_KEY } from './sessions'

/** The days the gym is closed (D-116): the board and the admin set them. */
export type ClosedDay = components['schemas']['ClosedDay']
export type ClosedDaysSettings = components['schemas']['ClosedDaysSettings']

export const CLOSED_DAYS_KEY = ['admin', 'closed-days'] as const

export function useClosedDays() {
  return useQuery({
    queryKey: CLOSED_DAYS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/admin/closed-days', { signal })),
  })
}

/** Closed or reopened days show on the plannings and may move a "Mois complet" badge. */
function useClosedDaysChanged() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: CLOSED_DAYS_KEY })
    void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
    void queryClient.invalidateQueries({ queryKey: MY_PLANNING_KEY })
    void queryClient.invalidateQueries({ queryKey: MY_BADGES_KEY })
  }
}

export function useCloseDays() {
  const queryClient = useQueryClient()
  const changed = useClosedDaysChanged()
  return useMutation({
    mutationFn: (settings: ClosedDaysSettings) =>
      request(api.POST('/api/v1/admin/closed-days', { body: settings })),
    onSuccess: (all) => {
      queryClient.setQueryData(CLOSED_DAYS_KEY, all)
      changed()
    },
  })
}

export function useReopenDays() {
  const changed = useClosedDaysChanged()
  return useMutation({
    mutationFn: (range: { from: string; to: string }) =>
      requestNoContent(api.DELETE('/api/v1/admin/closed-days', { params: { query: range } })),
    onSuccess: changed,
  })
}
