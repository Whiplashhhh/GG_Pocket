/** Stable error codes sent by the backend (ErrorCode.java), plus client-side ones. */
export const SERVER_ERROR_CODES = [
  'VALIDATION_FAILED',
  'MALFORMED_REQUEST',
  'UNAUTHENTICATED',
  'ACCESS_DENIED',
  'CSRF_TOKEN_INVALID',
  'NOT_FOUND',
  'METHOD_NOT_ALLOWED',
  'UNSUPPORTED_MEDIA_TYPE',
  'REQUEST_REJECTED',
  'PAYLOAD_TOO_LARGE',
  'INTERNAL_ERROR',
  'RATE_LIMITED',
  'CONCURRENT_CHANGE',
  'BAD_CREDENTIALS',
  'ACCOUNT_DISABLED',
  'REGISTRATION_CODE_INVALID',
  'NICKNAME_TAKEN',
  'EMAIL_TAKEN',
  'PASSWORD_TOO_SHORT',
  'PASSWORD_TOO_LONG',
  'PASSWORD_TOO_COMMON',
  'CURRENT_PASSWORD_WRONG',
  'PARENTAL_CONSENT_REQUIRED',
  'YEAR_IN_THE_FUTURE',
  'PASSWORD_RESET_LINK_INVALID',
  'IMAGE_UNSUPPORTED',
  'IMAGE_TOO_LARGE',
  'SELF_MANAGEMENT_FORBIDDEN',
  'LAST_ADMIN',
  'FAVORITES_FULL',
  'ROUTE_DISMOUNTED',
  'ROUTE_STATUS_CONFLICT',
  'ALREADY_EXISTS',
  'CLUB_ITEM_INACTIVE',
  'INVALID_SETTERS',
  'DATE_IN_THE_FUTURE',
  'DATE_IN_THE_PAST',
  'SESSION_TOO_OLD',
  'MONTH_FROZEN',
  'INVALID_ASCENT',
  'CHECK_FROM_ASCENT',
] as const

export type ServerErrorCode = (typeof SERVER_ERROR_CODES)[number]
export type ErrorCode = ServerErrorCode | 'NETWORK' | 'UNKNOWN'

export interface FieldViolation {
  field: string
  code: string
}

/** Problem Details (RFC 9457) as produced by the backend's ApiExceptionHandler. */
export interface ProblemDetails {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  code?: string
  errors?: FieldViolation[]
}

function isServerErrorCode(value: unknown): value is ServerErrorCode {
  return SERVER_ERROR_CODES.includes(value as ServerErrorCode)
}

function isProblemDetails(value: unknown): value is ProblemDetails {
  return typeof value === 'object' && value !== null
}

export class ApiError extends Error {
  readonly code: ErrorCode
  readonly status: number | undefined
  readonly fieldErrors: FieldViolation[]

  constructor(
    code: ErrorCode,
    options: { status?: number; fieldErrors?: FieldViolation[]; cause?: unknown } = {},
  ) {
    super(
      `API error ${code}${options.status === undefined ? '' : ` (${String(options.status)})`}`,
      {
        cause: options.cause,
      },
    )
    this.name = 'ApiError'
    this.code = code
    this.status = options.status
    this.fieldErrors = options.fieldErrors ?? []
  }

  /** Builds an error from whatever the server answered, even if it is not Problem Details. */
  static fromResponse(body: unknown, status: number): ApiError {
    if (isProblemDetails(body) && isServerErrorCode(body.code)) {
      return new ApiError(body.code, { status, fieldErrors: body.errors ?? [] })
    }
    return new ApiError(status >= 500 ? 'INTERNAL_ERROR' : 'UNKNOWN', { status })
  }
}

/** i18n key of the message to show for an error (see errors.* in fr.json). */
export function errorMessageKey(error: unknown): `errors.${ErrorCode}` {
  if (error instanceof ApiError) {
    return `errors.${error.code}`
  }
  return 'errors.UNKNOWN'
}

/** Whether retrying the same request may succeed (network hiccup, server error). */
export function isRetryable(error: unknown): boolean {
  return (
    error instanceof ApiError &&
    (error.code === 'NETWORK' || (error.status !== undefined && error.status >= 500))
  )
}
