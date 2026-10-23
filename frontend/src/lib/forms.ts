import { nextTick } from 'vue'

import { ApiError, type ErrorCode } from '@/api/errors'

/** Same rules as the backend (Nickname.PATTERN, PasswordPolicy), checked before sending. */
export const NICKNAME_PATTERN = /^[A-Za-z0-9._-]{3,20}$/
export const PASSWORD_MIN_LENGTH = 8
export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

/** Counted in characters (code points), like the backend: "é" is one. */
export function isPasswordLongEnough(password: string): boolean {
  return Array.from(password).length >= PASSWORD_MIN_LENGTH
}

/** i18n key of a message, with its parameters. */
export interface Message {
  key: string
  params?: Record<string, string | number>
}

export type FieldErrors<F extends string> = Partial<Record<F, Message>>

/** Business errors that belong to one field of a form. */
const FIELD_OF_CODE: Partial<Record<ErrorCode, string>> = {
  NICKNAME_TAKEN: 'nickname',
  EMAIL_TAKEN: 'email',
  PASSWORD_TOO_SHORT: 'password',
  PASSWORD_TOO_LONG: 'password',
  PASSWORD_TOO_COMMON: 'password',
  CURRENT_PASSWORD_WRONG: 'currentPassword',
  PARENTAL_CONSENT_REQUIRED: 'parentalConsent',
  REGISTRATION_CODE_INVALID: 'code',
}

/** Bean Validation constraints reported by the backend, as messages. */
const MESSAGE_OF_CONSTRAINT: Record<string, string> = {
  NotBlank: 'validation.required',
  NotNull: 'validation.required',
  Pattern: 'validation.nickname',
  Email: 'validation.email',
  Past: 'validation.dateInFuture',
  AssertTrue: 'validation.privacy',
  Min: 'validation.year',
  Max: 'validation.year',
  PlainText: 'validation.plainText',
}

/**
 * Splits a server error between the fields of a form and a general message. Field names follow
 * the API; `aliases` maps an API field to the form field that shows it (password → newPassword),
 * `codeFields` a business error to the field it belongs to in this form (ALREADY_EXISTS → name).
 */
export function serverFieldErrors<F extends string>(
  error: unknown,
  fields: readonly F[],
  aliases: Partial<Record<string, F>> = {},
  codeFields: Partial<Record<ErrorCode, F>> = {},
): { fields: FieldErrors<F>; general: Message | null } {
  if (!(error instanceof ApiError)) {
    return { fields: {}, general: { key: 'errors.UNKNOWN' } }
  }
  const result: FieldErrors<F> = {}
  const place = (field: string, message: Message) => {
    const target = aliases[field] ?? field
    if ((fields as readonly string[]).includes(target)) {
      result[target as F] = message
      return true
    }
    return false
  }
  if (error.code === 'VALIDATION_FAILED') {
    let placedAll = true
    for (const violation of error.fieldErrors) {
      placedAll =
        place(violation.field, {
          key: MESSAGE_OF_CONSTRAINT[violation.code] ?? 'validation.invalid',
        }) && placedAll
    }
    return { fields: result, general: placedAll ? null : { key: 'errors.VALIDATION_FAILED' } }
  }
  const field = codeFields[error.code] ?? FIELD_OF_CODE[error.code]
  if (field !== undefined && place(field, { key: `errors.${error.code}` })) {
    return { fields: result, general: null }
  }
  return { fields: result, general: { key: `errors.${error.code}` } }
}

/**
 * After a refused submit, put the focus on the first field to fix (screen readers read it).
 * A group of choices (colours, styles) marks itself with data-invalid and tabindex="-1".
 */
export function focusFirstInvalid(root: ParentNode = document): void {
  void nextTick(() =>
    root.querySelector<HTMLElement>('[aria-invalid="true"], [data-invalid="true"]')?.focus(),
  )
}
