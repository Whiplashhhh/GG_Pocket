import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'

import { AUTH_STATE_KEY, type AuthState } from './auth'
import { api } from './client'
import { request, requestNoContent } from './request'
import type { components } from './schema'

export type MyProfile = components['schemas']['MyProfile']
export type ProfileUpdate = components['schemas']['ProfileUpdate']
export type Device = components['schemas']['Device']

export const MY_PROFILE_KEY = ['me'] as const
export const MY_DEVICES_KEY = ['me', 'devices'] as const

export function useMyProfile() {
  return useQuery({
    queryKey: MY_PROFILE_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me', { signal })),
  })
}

/** The header shows the name and photo too: keep the signed-in member in step. */
function useProfileSaved() {
  const queryClient = useQueryClient()
  return (profile: MyProfile) => {
    queryClient.setQueryData(MY_PROFILE_KEY, profile)
    queryClient.setQueryData<AuthState>(AUTH_STATE_KEY, (state) =>
      state?.member
        ? {
            ...state,
            member: {
              ...state.member,
              nickname: profile.nickname,
              firstName: profile.firstName,
              lastName: profile.lastName,
              avatarUrl: profile.avatarUrl,
            },
          }
        : state,
    )
  }
}

export function useUpdateProfile() {
  const saved = useProfileSaved()
  return useMutation({
    mutationFn: (body: ProfileUpdate) => request(api.PUT('/api/v1/me/profile', { body })),
    onSuccess: saved,
  })
}

export function useUploadAvatar() {
  const saved = useProfileSaved()
  return useMutation({
    mutationFn: (photo: Blob) =>
      request(
        api.PUT('/api/v1/me/avatar', {
          // The contract types binary parts as strings; the browser sends the Blob itself.
          body: { file: photo as unknown as string },
          bodySerializer: () => {
            const form = new FormData()
            form.append('file', photo, 'photo.jpg')
            return form
          },
        }),
      ),
    onSuccess: saved,
  })
}

export function useRemoveAvatar() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => requestNoContent(api.DELETE('/api/v1/me/avatar')),
    onSuccess: () => {
      queryClient.setQueryData<MyProfile>(MY_PROFILE_KEY, (profile) =>
        profile ? { ...profile, avatarUrl: undefined } : profile,
      )
      queryClient.setQueryData<AuthState>(AUTH_STATE_KEY, (state) =>
        state?.member ? { ...state, member: { ...state.member, avatarUrl: undefined } } : state,
      )
    },
  })
}

export function useChangePassword() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: { currentPassword: string; newPassword: string }) =>
      requestNoContent(api.PUT('/api/v1/me/password', { body })),
    // The other sessions were closed by the server.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: MY_DEVICES_KEY }),
  })
}

export function useMyDevices() {
  return useQuery({
    queryKey: MY_DEVICES_KEY,
    queryFn: ({ signal }) => request(api.GET('/api/v1/me/devices', { signal })),
  })
}

export function useCloseDevice() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (deviceId: string) =>
      requestNoContent(
        api.DELETE('/api/v1/me/devices/{deviceId}', { params: { path: { deviceId } } }),
      ),
    onSettled: () => queryClient.invalidateQueries({ queryKey: MY_DEVICES_KEY }),
  })
}

export function useCloseOtherDevices() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => requestNoContent(api.POST('/api/v1/me/devices/close-others')),
    onSettled: () => queryClient.invalidateQueries({ queryKey: MY_DEVICES_KEY }),
  })
}
