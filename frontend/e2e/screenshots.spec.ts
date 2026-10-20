import type { Page } from '@playwright/test'

import type { FakeClub } from './fakeClub'
import { expect, test } from './fixtures'

type Role = 'CLIMBER' | 'SETTER' | 'BOARD' | 'ADMIN'

interface Capture {
  name: string
  path: string
  as: Role | null
  /** Management screens are also captured at 1280 px. */
  desktop?: boolean
  installInvite?: boolean
  prepare?: (club: FakeClub) => void
  act?: (page: Page) => Promise<void>
}

const someMembers = (club: FakeClub) => {
  club.addMember({
    nickname: 'tonton.talon',
    firstName: 'Tonton',
    lastName: 'Talon',
    role: 'SETTER',
    lastSeenAt: '2026-10-19T18:00:00Z',
    clubArrivalYear: 2018,
  })
  club.addMember({ nickname: 'jo', firstName: 'Joséphine', lastName: 'Bleau', status: 'DISABLED' })
  club.addMember({
    nickname: 'lea.d',
    firstName: 'Léa',
    lastName: 'Dupont',
    role: 'BOARD',
    email: 'lea@example.org',
    lastSeenAt: '2026-10-20T07:30:00Z',
  })
}

/** Screens captured for visual review (the gallery only exists on the dev server). */
const CAPTURES: Capture[] = [
  { name: 'accueil', path: '/accueil', as: null },
  { name: 'connexion', path: '/connexion?fin=1', as: null },
  { name: 'inscription-code', path: '/inscription', as: null },
  {
    name: 'inscription-fiche',
    path: '/inscription?code=K7XM-P2QA',
    as: null,
    prepare: (club) => {
      club.addCode('K7XM-P2QA')
    },
    act: async (page) => {
      await page.getByRole('button', { name: 'Vérifier le code' }).click()
      await page.getByLabel('Date de naissance').fill('2016-05-04')
      await page.getByRole('button', { name: 'Créer mon compte' }).click()
    },
  },
  { name: 'tes-donnees', path: '/tes-donnees', as: null },
  {
    name: 'nouveau-mot-de-passe',
    path: '/nouveau-mot-de-passe#jeton',
    as: null,
    prepare: (club) => {
      const member = club.addMember({ nickname: 'tonton.talon', firstName: 'Tonton' })
      club.resetTokens.set('jeton', member.id)
    },
  },
  { name: 'premiers-pas-photo', path: '/premiers-pas/photo', as: 'CLIMBER' },
  { name: 'premiers-pas-infos', path: '/premiers-pas/infos', as: 'CLIMBER' },
  { name: 'premiers-pas-installer', path: '/premiers-pas/installer', as: 'CLIMBER' },
  { name: 'actu-invitation', path: '/actu', as: 'CLIMBER', installInvite: true },
  { name: 'profil', path: '/profil', as: 'BOARD' },
  { name: 'profil-modifier', path: '/profil/modifier', as: 'CLIMBER' },
  { name: 'appareils', path: '/profil/appareils', as: 'CLIMBER' },
  { name: 'gestion', path: '/gestion', as: 'ADMIN', desktop: true },
  { name: 'membres', path: '/gestion/membres', as: 'BOARD', desktop: true, prepare: someMembers },
  {
    name: 'fiche-membre',
    path: '/gestion/membres',
    as: 'BOARD',
    desktop: true,
    prepare: someMembers,
    act: async (page) => {
      await page.getByRole('link', { name: /Talon/ }).first().click()
      await page.getByRole('button', { name: 'Créer un lien de réinitialisation' }).click()
    },
  },
  {
    name: 'journal',
    path: '/gestion/membres',
    as: 'ADMIN',
    desktop: true,
    prepare: someMembers,
    act: async (page) => {
      await page.getByRole('link', { name: /Talon/ }).first().click()
      await page.getByLabel('Choisir le rôle').selectOption({ label: 'Direction' })
      await page.getByRole('button', { name: 'Changer le rôle' }).click()
      await page.getByText('Rôle mis à jour.').waitFor()
      await page.goto('/gestion/journal')
    },
  },
  {
    name: 'codes',
    path: '/gestion/codes',
    as: 'ADMIN',
    desktop: true,
    prepare: (club) => {
      club.addCode('K7XM-P2QA', 'Affiche de la salle')
      club.addCode('ZT4R-9HQW', 'Forum des assos')
      const exhausted = club.codes.at(-1)
      if (exhausted) Object.assign(exhausted, { state: 'EXHAUSTED', useCount: 12, maxUses: 12 })
    },
  },
]

test.use({ meta: { environment: 'dev', branch: 'feature/comptes-0.0.0', commit: 'e0d3317abc' } })

for (const capture of CAPTURES) {
  test.describe(capture.name, () => {
    test.use({ signedInAs: capture.as, installInvite: capture.installInvite ?? false })

    test(`capture ${capture.name}`, async ({ page, club, isMobile }, testInfo) => {
      test.skip(!isMobile && capture.desktop !== true, 'Phone screens are reviewed at 360 px')
      capture.prepare?.(club)
      await page.goto(capture.path)
      await expect(page.locator('h1').first()).toBeVisible()
      await capture.act?.(page)
      await page.evaluate(() => document.fonts.ready)
      await page.screenshot({
        path: `screenshots/${testInfo.project.name}/${capture.name}.png`,
        fullPage: true,
      })
    })
  })
}
