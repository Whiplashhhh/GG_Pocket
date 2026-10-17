import { describe, expect, it } from 'vitest'

import { ApiError, errorMessageKey, isRetryable } from './errors'

describe('ApiError.fromResponse', () => {
  it('keeps the business code and field errors of a Problem Details body', () => {
    const error = ApiError.fromResponse(
      { status: 400, code: 'VALIDATION_FAILED', errors: [{ field: 'nickname', code: 'NotBlank' }] },
      400,
    )
    expect(error.code).toBe('VALIDATION_FAILED')
    expect(error.status).toBe(400)
    expect(error.fieldErrors).toEqual([{ field: 'nickname', code: 'NotBlank' }])
  })

  it('falls back to INTERNAL_ERROR for a 5xx without a known code', () => {
    expect(ApiError.fromResponse('<html>Bad gateway</html>', 502).code).toBe('INTERNAL_ERROR')
  })

  it('falls back to UNKNOWN for a 4xx with an unknown code', () => {
    expect(ApiError.fromResponse({ code: 'SOMETHING_NEW' }, 418).code).toBe('UNKNOWN')
  })
})

describe('errorMessageKey', () => {
  it('points to the translation of the code', () => {
    expect(errorMessageKey(new ApiError('ACCESS_DENIED', { status: 403 }))).toBe(
      'errors.ACCESS_DENIED',
    )
  })

  it('has a generic message for anything else', () => {
    expect(errorMessageKey(new TypeError('boom'))).toBe('errors.UNKNOWN')
  })
})

describe('isRetryable', () => {
  it('retries network failures and server errors only', () => {
    expect(isRetryable(new ApiError('NETWORK'))).toBe(true)
    expect(isRetryable(new ApiError('INTERNAL_ERROR', { status: 503 }))).toBe(true)
    expect(isRetryable(new ApiError('ACCESS_DENIED', { status: 403 }))).toBe(false)
    expect(isRetryable(new Error('boom'))).toBe(false)
  })
})
