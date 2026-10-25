import { describe, expect, it } from 'vitest'

import { OFFLINE_READS } from './offlineReads'

const ORIGIN = 'https://pocket.example'

describe('offline reads', () => {
  it.each([
    '/api/v1/routes',
    '/api/v1/routes/0b7c6a52-1c1d-4f6e-9a65-2d5d1a1b2c3d',
    '/api/v1/topo/config',
    '/api/v1/me',
    '/api/v1/me/favorites',
    '/api/v1/me/todo',
    '/api/v1/me/planning?month=2026-10',
  ])('keeps %s', (path) => {
    expect(OFFLINE_READS.test(`${ORIGIN}${path}`)).toBe(true)
  })

  it.each([
    '/api/v1/auth/state',
    '/api/v1/media/0b7c6a52-1c1d-4f6e-9a65-2d5d1a1b2c3d',
    '/api/v1/me/devices',
    '/api/v1/me/sessions',
    '/api/v1/routes/0b7c6a52-1c1d-4f6e-9a65-2d5d1a1b2c3d/comments',
    '/api/v1/routes/archive',
    '/api/v1/admin/members',
  ])('never keeps %s', (path) => {
    expect(OFFLINE_READS.test(`${ORIGIN}${path}`)).toBe(false)
  })
})
