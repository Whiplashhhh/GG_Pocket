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
  lastChancePopup?: boolean
  prepare?: (club: FakeClub) => void
  act?: (page: Page, club: FakeClub) => Promise<void>
}

/** Marie's last sessions: one yesterday with a comment, one last week in top rope only. */
const marieClimbs = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  club.climbs.add(
    marie,
    1,
    [
      { route: 'Bye Bye Morice', mode: 'LEAD', clean: true },
      { route: 'Réglisse', mode: 'LEAD' },
      { route: 'Tonton Talon', mode: 'TOP_ROPE', topped: false },
      { route: 'Bye Bye Morice', mode: 'TOP_ROPE', clean: true },
      { route: 'Iron Man', mode: 'TOP_ROPE' },
      { route: 'Sec !', mode: 'TOP_ROPE', clean: true },
    ],
    { comment: 'Les avant-bras en béton, le moral au sommet.', feeling: 8 },
  )
  club.climbs.add(marie, 6, [{ route: 'P’tite Line' }, { route: 'Sec !', clean: true }], {
    visibility: 'PRIVATE',
  })
}

const addToSession = async (page: Page, search: string, name: string) => {
  await page.getByRole('searchbox', { name: 'Ajouter une voie' }).fill(search)
  await page.getByRole('button', { name: `Ajouter ${name}` }).click()
}

/** Marie's own marks on the topo: favourites (one dismounted since), routes she can climb. */
const marieOnTheWall = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  club.topo.favorite(marie, 'Schtroumpfs')
  club.topo.favorite(marie, 'Bye Bye Morice')
  club.topo.favorite(marie, 'Réglisse')
  club.topo.check(marie, 'Bye Bye Morice', 'LEAD')
  club.topo.check(marie, 'Iron Man', 'TOP_ROPE')
  marie.favoriteRouteId = club.topo.route('Tonton Talon').id
}

/** A month of Marie at the gym, a route dismounted since in her history, a few marks. */
const marieSeason = (club: FakeClub) => {
  marieClimbs(club)
  marieOnTheWall(club)
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  club.climbs.add(marie, 2, [{ route: 'Iron Man', mode: 'LEAD' }, { route: 'Du mou !' }], {
    feeling: 6,
  })
  club.climbs.add(marie, 9, [{ route: 'Avant-bras en béton', mode: 'LEAD', clean: true }], {
    feeling: 9,
  })
  club.climbs.add(marie, 13, [{ route: 'Pied-main party' }, { route: 'Réglisse', clean: true }])
  club.climbs.add(marie, 15, [{ route: 'Sec !', mode: 'LEAD', clean: true }], { feeling: 7 })
  club.climbs.add(marie, 40, [{ route: 'Schtroumpfs', mode: 'LEAD' }], { frozen: true })
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

/** Openers with an account, Sam credited on Bye Bye Morice; a cord and a colour put away. */
const settersAndAWornWall = (club: FakeClub) => {
  const sam = club.addMember({
    nickname: 'sam',
    firstName: 'Sam',
    lastName: 'Ouvre',
    role: 'SETTER',
  })
  club.addMember({
    nickname: 'tonton.talon',
    firstName: 'Tonton',
    lastName: 'Talon',
    role: 'BOARD',
  })
  club.topo.route('Bye Bye Morice').setterIds = [sam.id]
  const cord = club.topo.config.sectors
    .flatMap((sector) => sector.cords)
    .find((candidate) => candidate.number === 7)
  if (cord) cord.active = false
  const grey = club.topo.config.holdColors.find((color) => color.label === 'gris')
  if (grey) grey.active = false
}

/** Large screens act from the table, phones from the actions sheet of the route. */
const routeActionOf = async (page: Page, name: string, word: string, label: string) => {
  if ((page.viewportSize()?.width ?? 0) >= 1024) {
    await page.getByLabel(label, { exact: true }).click()
  } else {
    await page
      .getByRole('main')
      .getByRole('button', { name: new RegExp(name) })
      .click()
    await page.getByRole('dialog').getByText(word, { exact: true }).click()
  }
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
  { name: 'premiers-pas-voie', path: '/premiers-pas/voie', as: 'CLIMBER' },
  { name: 'premiers-pas-installer', path: '/premiers-pas/installer', as: 'CLIMBER' },
  { name: 'actu-invitation', path: '/actu', as: 'CLIMBER', installInvite: true },
  { name: 'profil', path: '/profil', as: 'BOARD', prepare: marieOnTheWall },
  { name: 'actu-seances', path: '/actu', as: 'CLIMBER', prepare: marieClimbs },
  { name: 'a-faire', path: '/a-faire', as: 'CLIMBER', prepare: marieSeason },
  {
    name: 'a-faire-sues',
    path: '/a-faire',
    as: 'CLIMBER',
    prepare: marieSeason,
    act: async (page) => {
      await page.getByRole('button', { name: /Voir celles que je sais faire/ }).click()
    },
  },
  {
    name: 'planning',
    path: '/planning',
    as: 'CLIMBER',
    prepare: marieSeason,
    act: async (page) => {
      await page.getByRole('button', { name: '19 octobre 2026, grimpé' }).click()
    },
  },
  { name: 'planning-vide', path: '/planning', as: 'CLIMBER' },
  { name: 'carnet', path: '/carnet', as: 'CLIMBER', prepare: marieSeason },
  {
    name: 'seance-saisie',
    path: '/seance/nouvelle',
    as: 'CLIMBER',
    prepare: marieClimbs,
    act: async (page) => {
      await addToSession(page, '12', 'Bye Bye Morice')
      await addToSession(page, 'reglisse', 'Réglisse')
      const second = page
        .getByRole('listitem')
        .filter({ has: page.getByLabel('Actions sur la voie n° 2') })
      await second
        .getByRole('radiogroup', { name: 'Mode' })
        .getByRole('radio', { name: 'Tête' })
        .click()
      await second
        .getByRole('radiogroup', { name: 'Topée' })
        .getByRole('radio', { name: 'Non' })
        .click()
      await second.getByRole('button', { name: 'Une chute de plus' }).click()
      await page.getByRole('searchbox', { name: 'Ajouter une voie' }).fill('')
      await page.getByLabel('Commentaire').fill('Grosse séance de conti.')
    },
  },
  {
    name: 'seance-detail',
    path: '/actu',
    as: 'CLIMBER',
    prepare: marieClimbs,
    act: async (page) => {
      await page.getByRole('link', { name: 'Séance d’hier' }).click()
      await page.getByRole('heading', { level: 1, name: 'Ma séance' }).waitFor()
    },
  },
  {
    name: 'seance-en-attente',
    path: '/seance/nouvelle',
    as: 'CLIMBER',
    act: async (page, club) => {
      await addToSession(page, 'reglisse', 'Réglisse')
      club.climbs.offline = true
      await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
      await page.getByText('1 séance en attente de réseau').waitFor()
    },
  },
  { name: 'topo', path: '/topo', as: 'CLIMBER', prepare: marieOnTheWall },
  { name: 'topo-secteurs', path: '/topo?tri=secteur', as: 'CLIMBER', prepare: marieOnTheWall },
  {
    name: 'topo-filtres',
    path: '/topo',
    as: 'CLIMBER',
    act: async (page) => {
      await page.getByRole('button', { name: 'Filtres' }).click()
      await page.getByRole('button', { name: 'Grand dévers' }).click()
      await page.getByRole('button', { name: 'Dévers', exact: true }).click()
    },
  },
  {
    name: 'topo-fiche',
    path: '/topo',
    as: 'CLIMBER',
    prepare: (club) => {
      marieOnTheWall(club)
      marieClimbs(club)
    },
    act: async (page) => {
      await page.getByRole('link', { name: /Bye Bye Morice/ }).click()
      await page.getByRole('dialog').getByText('Le conseil de l’ouvreur').waitFor()
    },
  },
  {
    name: 'topo-fiche-compteur',
    path: '/topo',
    as: 'CLIMBER',
    prepare: marieClimbs,
    act: async (page) => {
      await page.getByRole('link', { name: /Bye Bye Morice/ }).click()
      const counter = page.getByRole('dialog').getByRole('heading', { name: 'Au compteur' })
      await counter.scrollIntoViewIfNeeded()
    },
  },
  {
    name: 'topo-fiche-photo',
    path: '/topo',
    as: 'CLIMBER',
    act: async (page) => {
      await page.getByRole('link', { name: /OH purée/ }).click()
      await page.getByRole('dialog').getByText('Commentaires (0)').waitFor()
    },
  },
  {
    name: 'topo-coeur-plein',
    path: '/topo',
    as: 'CLIMBER',
    prepare: (club) => {
      const marie = club.members.find((member) => member.nickname === 'marie')
      if (marie === undefined) return
      club.topo.favorite(marie, 'Réglisse')
      club.topo.favorite(marie, 'Iron Man')
    },
    act: async (page) => {
      await page.getByRole('link', { name: /Du mou/ }).click()
      await page.getByRole('button', { name: 'Coup de cœur' }).click()
      await page.getByRole('dialog', { name: /Ton cœur est plein/ }).waitFor()
    },
  },
  {
    name: 'topo-archives',
    path: '/topo/archives',
    as: 'CLIMBER',
  },
  {
    name: 'topo-fiche-demontee',
    path: '/topo/archives',
    as: 'CLIMBER',
    act: async (page) => {
      await page.getByRole('link', { name: /Schtroumpfs/ }).click()
      await page
        .getByRole('dialog')
        .getByText(/^Démontée le/)
        .waitFor()
    },
  },
  { name: 'topo-derniere-chance', path: '/actu', as: 'CLIMBER', lastChancePopup: true },
  { name: 'profil-modifier', path: '/profil/modifier', as: 'CLIMBER', prepare: marieOnTheWall },
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
    name: 'gestion-voies',
    path: '/gestion/voies',
    as: 'SETTER',
    desktop: true,
    prepare: settersAndAWornWall,
  },
  {
    name: 'gestion-voies-archives',
    path: '/gestion/voies?vue=archives',
    as: 'SETTER',
    desktop: true,
  },
  {
    name: 'gestion-voies-actions',
    path: '/gestion/voies',
    as: 'SETTER',
    desktop: true,
    act: async (page) => {
      if ((page.viewportSize()?.width ?? 0) < 1024) {
        await page
          .getByRole('main')
          .getByRole('button', { name: /Bye Bye Morice/ })
          .click()
        await page.getByRole('dialog', { name: 'Actions pour Bye Bye Morice' }).waitFor()
      }
    },
  },
  {
    name: 'gestion-voie-demonter',
    path: '/gestion/voies',
    as: 'SETTER',
    desktop: true,
    act: async (page) => {
      await routeActionOf(page, 'Bye Bye Morice', 'Démonter', 'Démonter Bye Bye Morice')
      await page.getByRole('alertdialog').waitFor()
    },
  },
  {
    name: 'gestion-voie-derniere-chance',
    path: '/gestion/voies',
    as: 'SETTER',
    desktop: true,
    act: async (page) => {
      await routeActionOf(page, 'Réglisse', 'Dernière chance', 'Passer Réglisse en dernière chance')
      await page.getByRole('dialog', { name: /Dernière chance pour/ }).waitFor()
    },
  },
  {
    name: 'gestion-voie-nouvelle',
    path: '/gestion/voies/nouvelle',
    as: 'SETTER',
    desktop: true,
    prepare: settersAndAWornWall,
    act: async (page) => {
      await page.getByLabel('Nom de la voie').fill('Avale la corde')
      await page.getByLabel('Cotation').selectOption({ label: '5c' })
      await page.getByLabel('Corde', { exact: true }).selectOption({ label: 'Corde 4' })
      await page
        .getByRole('group', { name: 'Couleur des prises' })
        .getByText('vert', { exact: true })
        .click()
      await page.getByRole('button', { name: 'Dalle', exact: true }).click()
    },
  },
  {
    name: 'gestion-voie-erreurs',
    path: '/gestion/voies/nouvelle',
    as: 'SETTER',
    desktop: true,
    act: async (page) => {
      await page.getByRole('button', { name: 'Enregistrer la voie' }).click()
      await page.getByText('Choisis une cotation.').waitFor()
    },
  },
  {
    name: 'gestion-voie-modifier',
    path: '/gestion/voies',
    as: 'SETTER',
    desktop: true,
    prepare: settersAndAWornWall,
    act: async (page, club) => {
      await page.goto(`/gestion/voies/${club.topo.route('Bye Bye Morice').id}/modifier`)
      await page.getByLabel('Nom de la voie').waitFor()
    },
  },
  {
    name: 'gestion-derniere-chance',
    path: '/gestion/derniere-chance',
    as: 'SETTER',
    desktop: true,
  },
  {
    name: 'gestion-club',
    path: '/gestion/club',
    as: 'ADMIN',
    desktop: true,
    prepare: settersAndAWornWall,
  },
  {
    name: 'gestion-club-couleur',
    path: '/gestion/club',
    as: 'ADMIN',
    desktop: true,
    act: async (page) => {
      await page.getByRole('button', { name: 'Modifier la couleur bleu clair' }).click()
      await page.getByRole('dialog', { name: 'Couleur bleu clair' }).waitFor()
    },
  },
  {
    name: 'gestion-club-corde',
    path: '/gestion/club',
    as: 'ADMIN',
    desktop: true,
    act: async (page) => {
      await page.getByRole('button', { name: 'Modifier la corde 12' }).click()
      await page.getByRole('dialog', { name: 'Corde 12' }).waitFor()
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
    test.use({
      signedInAs: capture.as,
      installInvite: capture.installInvite ?? false,
      lastChancePopup: capture.lastChancePopup ?? false,
    })

    test(`capture ${capture.name}`, async ({ page, club, isMobile }, testInfo) => {
      test.skip(!isMobile && capture.desktop !== true, 'Phone screens are reviewed at 360 px')
      // The phone and the fake server agree on the day (the session sheet reads it).
      await page.clock.setSystemTime(new Date(club.now))
      capture.prepare?.(club)
      await page.goto(capture.path)
      await expect(page.locator('h1').first()).toBeVisible()
      await capture.act?.(page, club)
      await page.evaluate(() => document.fonts.ready)
      await page.screenshot({
        path: `screenshots/${testInfo.project.name}/${capture.name}.png`,
        fullPage: true,
        // Finished transitions only: a chip caught mid-fade reads grey on grey.
        animations: 'disabled',
      })
    })
  })
}
