import { describe, expect, it } from 'vitest'

import { ApiError } from '@/api/errors'

import { serverFieldErrors } from './forms'

const FIELDS = ['nickname', 'email', 'password', 'privacyAccepted'] as const

describe('serverFieldErrors', () => {
  it('puts business errors on their field', () => {
    const result = serverFieldErrors(new ApiError('NICKNAME_TAKEN', { status: 409 }), FIELDS)
    expect(result).toEqual({
      fields: { nickname: { key: 'errors.NICKNAME_TAKEN' } },
      general: null,
    })
  })

  it('translates validation errors field by field', () => {
    const error = new ApiError('VALIDATION_FAILED', {
      status: 400,
      fieldErrors: [
        { field: 'email', code: 'Email' },
        { field: 'privacyAccepted', code: 'AssertTrue' },
      ],
    })
    expect(serverFieldErrors(error, FIELDS).fields).toEqual({
      email: { key: 'validation.email' },
      privacyAccepted: { key: 'validation.privacy' },
    })
  })

  it('follows aliases when the form names a field differently', () => {
    const result = serverFieldErrors(
      new ApiError('PASSWORD_TOO_COMMON', { status: 400 }),
      ['newPassword'] as const,
      { password: 'newPassword' },
    )
    expect(result.fields).toEqual({ newPassword: { key: 'errors.PASSWORD_TOO_COMMON' } })
  })

  it('keeps a general message for what has no field', () => {
    expect(serverFieldErrors(new ApiError('RATE_LIMITED', { status: 429 }), FIELDS)).toEqual({
      fields: {},
      general: { key: 'errors.RATE_LIMITED' },
    })
    expect(serverFieldErrors(new TypeError('boom'), FIELDS).general).toEqual({
      key: 'errors.UNKNOWN',
    })
  })
})
