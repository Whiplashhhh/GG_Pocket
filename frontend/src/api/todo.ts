import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/vue-query'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { ARCHIVE_KEY, MY_TODO_KEY, ROUTES_KEY, type ClimbMode } from './topo'

export type Todo = components['schemas']['Todo']
export type TodoRoute = components['schemas']['TodoRoute']
export type TodoState = TodoRoute['lead']

/** "À faire" (spec §8.5): every route on the wall and where I stand on it, per mode. */
export function useMyTodo() {
  return useQuery({
    queryKey: MY_TODO_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/todo', { signal })),
  })
}

const STATE_FIELD = { TOP_ROPE: 'topRope', LEAD: 'lead' } as const satisfies Record<
  ClimbMode,
  keyof TodoRoute
>

/**
 * Shows the tick at once: the route leaves (or comes back to) the list of that mode. What else it
 * changes (lead also means top rope, the counters) comes back with the server's answer.
 */
function patchState(queryClient: QueryClient, id: string, mode: ClimbMode, state: TodoState) {
  const previous = queryClient.getQueryData<Todo>(MY_TODO_KEY)
  queryClient.setQueryData<Todo>(MY_TODO_KEY, (todo) =>
    todo
      ? {
          ...todo,
          routes: todo.routes.map((item) =>
            item.route.id === id ? { ...item, [STATE_FIELD[mode]]: state } : item,
          ),
        }
      : todo,
  )
  return () => {
    queryClient.setQueryData(MY_TODO_KEY, previous)
  }
}

/** Ticks ("je sais la faire") or unticks a route in one mode, from the "à faire" list. */
export function useTodoCheck() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, mode, checked }: { id: string; mode: ClimbMode; checked: boolean }) => {
      const params = { params: { path: { id, mode } } }
      return requestNoContent(
        checked
          ? api.PUT('/api/v1/routes/{id}/checks/{mode}', params)
          : api.DELETE('/api/v1/routes/{id}/checks/{mode}', params),
      )
    },
    onMutate: ({ id, mode, checked }) =>
      patchState(queryClient, id, mode, checked ? 'CHECKED' : 'TODO'),
    onError: (_error, _variables, undo) => {
      undo?.()
    },
    onSettled: () => {
      void queryClient.invalidateQueries({ queryKey: MY_TODO_KEY })
      void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
      void queryClient.invalidateQueries({ queryKey: ARCHIVE_KEY })
    },
  })
}
