import type { MyProfile, ProfileUpdate } from '@/api/me'

import type { FieldErrors } from './forms'

export const FIRST_YEAR = 1950

/** "2019" → 2019, "" → undefined; anything else is an error for the field. */
export function parseYear(value: string, currentYear: number): number | undefined | 'invalid' {
  const trimmed = value.trim()
  if (trimmed === '') {
    return undefined
  }
  if (!/^\d{4}$/.test(trimmed)) {
    return 'invalid'
  }
  const year = Number(trimmed)
  return year < FIRST_YEAR || year > currentYear ? 'invalid' : year
}

/** The optional details, as typed in the form. */
export interface ProfileDetails {
  clubArrivalYear: string
  climbingSinceYear: string
  bio: string
}

export interface ProfileForm {
  nickname: string
  firstName: string
  lastName: string
  email: string
  birthDate: string
  parentalConsent: boolean
  showAge: boolean
  appearsInRankings: boolean
  clubArrivalYear: string
  climbingSinceYear: string
  bio: string
}

export function toForm(profile: MyProfile): ProfileForm {
  return {
    nickname: profile.nickname,
    firstName: profile.firstName,
    lastName: profile.lastName,
    email: profile.email ?? '',
    birthDate: profile.birthDate ?? '',
    parentalConsent: profile.parentalConsent,
    showAge: profile.showAge,
    appearsInRankings: profile.appearsInRankings,
    clubArrivalYear: profile.clubArrivalYear?.toString() ?? '',
    climbingSinceYear: profile.climbingSinceYear?.toString() ?? '',
    bio: profile.bio ?? '',
  }
}

/** The update to send, or the errors of the fields that cannot be sent as typed. */
export function toUpdate(
  form: ProfileForm,
  currentYear: number,
): { update: ProfileUpdate } | { errors: FieldErrors<keyof ProfileForm> } {
  const errors: FieldErrors<keyof ProfileForm> = {}
  const clubArrivalYear = parseYear(form.clubArrivalYear, currentYear)
  const climbingSinceYear = parseYear(form.climbingSinceYear, currentYear)
  if (clubArrivalYear === 'invalid') errors.clubArrivalYear = { key: 'validation.year' }
  if (climbingSinceYear === 'invalid') errors.climbingSinceYear = { key: 'validation.year' }
  if (clubArrivalYear === 'invalid' || climbingSinceYear === 'invalid') {
    return { errors }
  }
  const optional = (value: string) => (value.trim() === '' ? undefined : value.trim())
  return {
    update: {
      nickname: form.nickname.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: optional(form.email),
      birthDate: optional(form.birthDate),
      parentalConsent: form.parentalConsent,
      showAge: form.showAge,
      appearsInRankings: form.appearsInRankings,
      clubArrivalYear,
      climbingSinceYear,
      bio: optional(form.bio),
    },
  }
}
