import createClient from 'openapi-fetch'

import { csrfMiddleware } from './csrf'
import type { paths } from './schema'

/** Typed client generated from the backend contract (src/api/openapi.json). Same origin: /api. */
export const api = createClient<paths>({ baseUrl: '/', credentials: 'same-origin' })

api.use(csrfMiddleware())
