import {
  queryOptions,
  useMutation,
  useQuery,
  useQueryClient,
  type QueryClient,
} from '@tanstack/vue-query'
import { computed } from 'vue'

import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'
import { forgetOfflineReads } from '@/lib/offlineReads'

export type AuthState = components['schemas']['AuthState']
export type CurrentMember = components['schemas']['CurrentMember']
export type Role = CurrentMember['role']
export type LoginRequest = components['schemas']['LoginRequest']
export type RegisterRequest = components['schemas']['RegisterRequest']

export const AUTH_STATE_KEY = ['auth', 'state'] as const

export const SIGNED_OUT: AuthState = { authenticated: false }

/** Also sets the CSRF cookie that every write sends back (see csrf.ts). */
export const authStateQuery = queryOptions({
  queryKey: AUTH_STATE_KEY,
  queryFn: ({ signal }) => request(api.GET('/api/v1/auth/state', { signal })),
  staleTime: 5 * 60_000,
})

export function useAuthState() {
  return useQuery(authStateQuery)
}

/** The signed-in member, or undefined while unknown or signed out. */
export function useCurrentMember() {
  const { data } = useAuthState()
  return computed(() => data.value?.member)
}

/**
 * After a sign-in of any kind (login, registration, reset): forget everything cached for the
 * previous member, then remember the new one.
 */
function useSignedIn() {
  const queryClient = useQueryClient()
  return (member: CurrentMember) => {
    forgetMemberData(queryClient)
    queryClient.setQueryData(AUTH_STATE_KEY, { authenticated: true, member })
  }
}

/**
 * Drops every cached answer but the sign-in state itself, whose query the app shell is watching
 * (removing it would leave the shell looking at a query nobody updates any more).
 */
function forgetMemberData(queryClient: QueryClient) {
  queryClient.removeQueries({ predicate: (query) => query.queryKey[0] !== AUTH_STATE_KEY[0] })
  forgetOfflineReads()
}

export function useLogin() {
  const signedIn = useSignedIn()
  return useMutation({
    mutationFn: (body: LoginRequest) => request(api.POST('/api/v1/auth/login', { body })),
    onSuccess: signedIn,
  })
}

/**
 * Set while the member signs out on purpose, so the app shell sends them to the welcome screen
 * rather than announcing a lost session.
 */
export const signOut = { intentional: false }

export function useLogout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => requestNoContent(api.POST('/api/v1/auth/logout')),
    onSettled: () => {
      signOut.intentional = true
      forgetMemberData(queryClient)
      queryClient.setQueryData(AUTH_STATE_KEY, SIGNED_OUT)
    },
  })
}

export function useCheckRegistrationCode() {
  return useMutation({
    mutationFn: (code: string) =>
      requestNoContent(api.POST('/api/v1/auth/registration-code/check', { body: { code } })),
  })
}

export function useRegister() {
  const signedIn = useSignedIn()
  return useMutation({
    mutationFn: (body: RegisterRequest) => request(api.POST('/api/v1/auth/register', { body })),
    onSuccess: signedIn,
  })
}

export function usePasswordResetTarget(token: () => string) {
  return useQuery({
    queryKey: ['auth', 'password-reset', token] as const,
    queryFn: ({ signal }) =>
      request(api.POST('/api/v1/auth/password-reset/check', { body: { token: token() }, signal })),
    enabled: () => token() !== '',
    retry: false,
    staleTime: Infinity,
  })
}

export function useResetPassword() {
  const signedIn = useSignedIn()
  return useMutation({
    mutationFn: (body: { token: string; newPassword: string }) =>
      request(api.POST('/api/v1/auth/password-reset', { body })),
    onSuccess: signedIn,
  })
}
