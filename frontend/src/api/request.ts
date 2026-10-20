import { ApiError } from './errors'

interface ClientResult<T> {
  data?: T
  error?: unknown
  response: Response
}

/**
 * Awaits an openapi-fetch call and returns its data, or throws an {@link ApiError} that the UI
 * can translate. For endpoints that return a body (not 204).
 */
export async function request<T>(call: Promise<ClientResult<T>>): Promise<T> {
  let result: ClientResult<T>
  try {
    result = await call
  } catch (cause) {
    throw new ApiError('NETWORK', { cause })
  }
  if (result.error !== undefined || !result.response.ok) {
    throw ApiError.fromResponse(result.error, result.response.status)
  }
  if (result.data === undefined) {
    throw new ApiError('UNKNOWN', { status: result.response.status })
  }
  return result.data
}

/** Same as {@link request}, for endpoints that answer 204 No Content. */
export async function requestNoContent(call: Promise<ClientResult<unknown>>): Promise<void> {
  let result: ClientResult<unknown>
  try {
    result = await call
  } catch (cause) {
    throw new ApiError('NETWORK', { cause })
  }
  if (result.error !== undefined || !result.response.ok) {
    throw ApiError.fromResponse(result.error, result.response.status)
  }
}
