import { describe, expect, it } from 'vitest'
import type { RouteMeta } from 'vue-router'

import type { AuthState } from '@/api/auth'

import { decideNavigation, safeReturnPath } from './guard'

const SIGNED_OUT: AuthState = { authenticated: false }
const signedInAs = (role: 'CLIMBER' | 'SETTER' | 'BOARD' | 'ADMIN'): AuthState => ({
  authenticated: true,
  member: { id: '1', nickname: 'marie', firstName: 'Marie', lastName: 'H', role },
})
const to = (fullPath: string, meta: RouteMeta = {}) => ({ fullPath, meta })

describe('decideNavigation', () => {
  it('sends visitors to the welcome screen, remembering where they wanted to go', () => {
    expect(decideNavigation(to('/actu'), SIGNED_OUT)).toBe('/accueil')
    expect(decideNavigation(to('/profil/appareils'), SIGNED_OUT)).toEqual({
      path: '/accueil',
      query: { retour: '/profil/appareils' },
    })
  })

  it('lets everyone see public pages', () => {
    expect(decideNavigation(to('/tes-donnees', { public: true }), SIGNED_OUT)).toBe(true)
    expect(decideNavigation(to('/tes-donnees', { public: true }), signedInAs('CLIMBER'))).toBe(true)
  })

  it('takes signed-in members away from the sign-in screens', () => {
    expect(
      decideNavigation(to('/connexion', { public: true, guestOnly: true }), signedInAs('CLIMBER')),
    ).toBe('/actu')
  })

  it('keeps the management space for the right roles', () => {
    const members = to('/gestion/membres', { minRole: 'BOARD' })
    expect(decideNavigation(members, signedInAs('SETTER'))).toBe('/actu')
    expect(decideNavigation(members, signedInAs('BOARD'))).toBe(true)
    expect(decideNavigation(members, signedInAs('ADMIN'))).toBe(true)
  })

  it('does not sign anyone out because the network is down', () => {
    expect(decideNavigation(to('/actu'), 'unknown')).toBe(true)
  })
})

describe('safeReturnPath', () => {
  it('only goes back inside the app', () => {
    expect(safeReturnPath('/profil')).toBe('/profil')
    expect(safeReturnPath('//evil.example')).toBe('/actu')
    expect(safeReturnPath('https://evil.example')).toBe('/actu')
    expect(safeReturnPath(undefined)).toBe('/actu')
  })
})
