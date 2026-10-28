import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'

import { api } from './client'
import { FEED_KEY } from './sessions'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { ROUTES_KEY } from './topo'

export type MemberSummary = components['schemas']['MemberSummary']
export type MemberDetail = components['schemas']['MemberDetail']
export type MemberStatus = MemberSummary['status']
export type AuditEntry = components['schemas']['AuditEntry']
export type ModeratedComment = components['schemas']['ModeratedComment']
export type RegistrationCode = components['schemas']['RegistrationCode']
export type RegistrationCodeSettings = components['schemas']['RegistrationCodeSettings']
type Role = MemberSummary['role']

const MEMBERS_KEY = ['admin', 'members'] as const
/** The audit log: refreshed after every audited action (roles, codes, dismounts, club config). */
export const AUDIT_KEY = ['admin', 'audit'] as const
export const MODERATION_KEY = ['admin', 'moderation'] as const
const CODES_KEY = ['admin', 'registration-codes'] as const

export const AUDIT_PAGE_SIZE = 50

export function useMembers() {
  return useQuery({
    queryKey: MEMBERS_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/admin/members', { signal })),
  })
}

export function useMember(id: () => string) {
  return useQuery({
    queryKey: [...MEMBERS_KEY, id] as const,
    queryFn: ({ signal }) =>
      request(api.GET('/api/v1/admin/members/{id}', { params: { path: { id: id() } }, signal })),
  })
}

/** A member changed: refresh their sheet, the table and the audit log. */
function useMemberChanged() {
  const queryClient = useQueryClient()
  return (detail: MemberDetail) => {
    queryClient.setQueryData([...MEMBERS_KEY, detail.member.id], detail)
    void queryClient.invalidateQueries({ queryKey: MEMBERS_KEY, exact: true })
    void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
  }
}

export function useChangeRole() {
  const changed = useMemberChanged()
  return useMutation({
    mutationFn: ({ id, role }: { id: string; role: Role }) =>
      request(
        api.PUT('/api/v1/admin/members/{id}/role', {
          params: { path: { id } },
          body: { role },
        }),
      ),
    onSuccess: changed,
  })
}

export function useChangeStatus() {
  const changed = useMemberChanged()
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: MemberStatus }) =>
      request(
        api.PUT('/api/v1/admin/members/{id}/status', {
          params: { path: { id } },
          body: { status },
        }),
      ),
    onSuccess: changed,
  })
}

/** For good (admin): the sheet goes, the table and the audit log follow. */
export function useDeleteMember() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      requestNoContent(api.DELETE('/api/v1/admin/members/{id}', { params: { path: { id } } })),
    onSuccess: (_, id) => {
      queryClient.removeQueries({ queryKey: [...MEMBERS_KEY, id], exact: true })
      void queryClient.invalidateQueries({ queryKey: MEMBERS_KEY, exact: true })
      void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
    },
  })
}

export function useCreateResetLink() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) =>
      request(
        api.POST('/api/v1/admin/members/{id}/password-reset-link', { params: { path: { id } } }),
      ),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: AUDIT_KEY }),
  })
}

export function useAuditLog() {
  return useInfiniteQuery({
    queryKey: AUDIT_KEY,
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/admin/audit', {
          params: { query: { cursor: pageParam, size: AUDIT_PAGE_SIZE } },
          signal,
        }),
      ),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (page) => page.nextCursor,
  })
}

export function useRegistrationCodes() {
  return useQuery({
    queryKey: CODES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/admin/registration-codes', { signal })),
  })
}

export function useCreateRegistrationCode() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: RegistrationCodeSettings) =>
      request(api.POST('/api/v1/admin/registration-codes', { body })),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: CODES_KEY })
      void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
    },
  })
}

export function useUpdateRegistrationCode() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, settings }: { id: string; settings: RegistrationCodeSettings }) =>
      request(
        api.PUT('/api/v1/admin/registration-codes/{id}', {
          params: { path: { id } },
          body: settings,
        }),
      ),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: CODES_KEY })
      void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
    },
  })
}

const MODERATION_PAGE_SIZE = 30

/** Spec §8.11: the latest comments on routes and sessions, newest first (board and admin). */
export function useModeratedComments() {
  return useInfiniteQuery({
    queryKey: MODERATION_KEY,
    queryFn: ({ pageParam, signal }) =>
      request(
        api.GET('/api/v1/admin/moderation/comments', {
          params: { query: { cursor: pageParam, size: MODERATION_PAGE_SIZE } },
          signal,
        }),
      ),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (page) => page.nextCursor,
  })
}

/** Deletes a comment of either kind; the server writes the audit log. */
export function useModerateComment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (comment: ModeratedComment) => {
      const params = { params: { path: { id: comment.id } } }
      return requestNoContent(
        comment.kind === 'ROUTE'
          ? api.DELETE('/api/v1/route-comments/{id}', params)
          : api.DELETE('/api/v1/session-comments/{id}', params),
      )
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: MODERATION_KEY })
      void queryClient.invalidateQueries({ queryKey: AUDIT_KEY })
      // Wherever the comment showed: route sheets and their counts, sessions and theirs.
      void queryClient.invalidateQueries({ queryKey: ROUTES_KEY })
      void queryClient.invalidateQueries({ queryKey: ['sessions'] })
      void queryClient.invalidateQueries({ queryKey: FEED_KEY })
    },
  })
}
