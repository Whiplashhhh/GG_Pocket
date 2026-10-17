import createClient from 'openapi-fetch'

import type { paths } from './schema'

/** Typed client generated from the backend contract (src/api/openapi.json). Same origin: /api. */
export const api = createClient<paths>({ baseUrl: '/', credentials: 'same-origin' })
