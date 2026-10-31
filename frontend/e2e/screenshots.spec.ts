import type { Page } from '@playwright/test'

import type { FakeClub, FakeMember } from './fakeClub'
import { expect, test } from './fixtures'

type Role = 'TOURIST' | 'CLIMBER' | 'SETTER' | 'BOARD' | 'ADMIN'

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

/** Tom's badges, earned over a few seasons (the fake has no engine: they are set by hand). */
const tomBadges = (club: FakeClub, tom: FakeMember) => {
  for (const code of ['TOPPED_1', 'TOPPED_5', 'TOPPED_10', 'TOPPED_20', 'TOPPED_50'] as const) {
    club.social.award(tom, code, '2024-03-12')
  }
  for (const code of ['LEAD_1', 'LEAD_5', 'LEAD_10', 'GRADE_5B', 'GRADE_5C', 'GRADE_6A'] as const) {
    club.social.award(tom, code, '2024-05-02')
  }
  club.social.award(tom, 'DAYS_10', '2024-04-20')
  club.social.award(tom, 'DAYS_25', '2024-09-14')
  club.social.award(tom, 'WEEK_2', '2024-03-18', 14, '2026-10-13')
  club.social.award(tom, 'FLASH', '2025-01-08', 3, '2026-10-17')
  club.social.favorites.set(tom.id, ['FLASH', 'TOPPED'])
}

/** Marie's badges: a good start, one repeatable earned many times, plenty left to go for. */
const marieBadges = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  marie.clubArrivalYear = 2025
  for (const code of ['TOPPED_1', 'TOPPED_5', 'TOPPED_10', 'LEAD_1', 'LEAD_5'] as const) {
    club.social.award(marie, code, '2025-10-02')
  }
  for (const code of ['GRADE_5B', 'GRADE_5C', 'GRADE_6A', 'GRADE_6B', 'DAYS_10'] as const) {
    club.social.award(marie, code, '2026-02-11')
  }
  club.social.award(marie, 'WEEK_2', '2025-10-06', 6, '2026-10-13')
  club.social.award(marie, 'WEEK_3', '2026-10-13')
  club.social.award(marie, 'PERFECT', '2026-03-04', 2, '2026-09-30')
  club.social.award(marie, 'COMEBACK', '2026-09-01')
  club.social.favorites.set(marie.id, ['FIRST_GRADE', 'WEEK_2'])
  club.social.progress.set(marie.id, { topped: 14, lead: 6, climbingDays: 13 })
}

/** Marie's friends: Tom and Léa, a request from Jules, one sent to Sam, and strangers. */
const marieFriends = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  const tom = club.addMember({
    nickname: 'tom.bloc',
    firstName: 'Tom',
    lastName: 'Bloc',
    clubArrivalYear: 2018,
    climbingSinceYear: 2012,
    bio: 'Toujours fourré dans le grand dévers. Ne prête pas sa magnésie.',
  })
  const lea = club.addMember({ nickname: 'lea.dyno', firstName: 'Léa', lastName: 'Dupont' })
  const jules = club.addMember({
    nickname: 'jules',
    firstName: 'Jules',
    lastName: 'Martin',
    clubArrivalYear: 2024,
  })
  const sam = club.addMember({ nickname: 'sam', firstName: 'Samia', lastName: 'Benali' })
  club.addMember({
    nickname: 'bernard.leclercq.du.grand.devers',
    firstName: 'Bernard-Emmanuel',
    lastName: 'Leclercq-Vandenberghe',
    clubArrivalYear: 2003,
  })
  club.social.link(tom, marie)
  club.social.link(marie, lea)
  club.social.link(jules, marie, false)
  club.social.link(marie, sam, false)
  club.topo.favorite(tom, 'Iron Man')
  tomBadges(club, tom)
  club.climbs.add(
    tom,
    1,
    [
      { route: 'Iron Man', mode: 'LEAD', clean: true },
      { route: 'Réglisse', mode: 'LEAD' },
      { route: 'Tonton Talon', mode: 'TOP_ROPE', topped: false },
    ],
    {
      comment: 'Enfin enchaînée, Iron Man.',
      feeling: 9,
      badges: ['TOPPED_50', 'LEAD_10', 'GRADE_6A', 'FLASH'],
    },
  )
  club.climbs.add(tom, 4, [{ route: 'Sec !', clean: true }])
}

/** Marie's friends at it: their sessions in her feed, reactions and comments both ways. */
const feedAlive = (club: FakeClub) => {
  marieClimbs(club)
  marieFriends(club)
  const marie = club.members.find((member) => member.nickname === 'marie')
  const tom = club.members.find((member) => member.nickname === 'tom.bloc')
  const lea = club.members.find((member) => member.nickname === 'lea.dyno')
  if (marie === undefined || tom === undefined || lea === undefined) return
  const latest = (member: FakeMember) =>
    club.climbs.sessions
      .filter((session) => session.memberId === member.id && session.visibility === 'FRIENDS')
      .sort((a, b) => b.climbedOn.localeCompare(a.climbedOn))
      .at(0)
  const hers = latest(marie)
  const his = latest(tom)
  if (hers === undefined || his === undefined) return
  club.climbs.react(tom, hers, 'FIRE')
  club.climbs.react(lea, hers, 'GORILLA')
  club.climbs.comment(tom, hers, 'Les avant-bras en béton ? Les miens sont en guimauve.', 90)
  club.climbs.react(lea, his, 'CLAP')
  club.climbs.react(marie, his, 'STRONG')
  club.climbs.comment(lea, his, 'Enfin ! Tu me montres la méthode du crux jeudi ?', 120)
  club.climbs.comment(tom, his, 'Jeudi 19 h, ramène de la magnésie.', 60)
  club.climbs.add(lea, 0, [{ route: 'Réglisse', mode: 'LEAD', clean: true }], { feeling: 7 })
  // Who held the rope: marie for tom, tom then léa for marie (D-101).
  for (const ascent of his.ascents) ascent.belayerId = marie.id
  hers.ascents.forEach((ascent, index) => {
    ascent.belayerId = index === 0 ? tom.id : lea.id
  })
  const sam = club.members.find((member) => member.nickname === 'sam')
  if (sam !== undefined) {
    club.climbs.add(sam, 4, [{ route: 'Bye Bye Morice', belayerId: marie.id }])
  }
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
/** The club's October: a tie at the top, a long list, Marie far enough down for her bar. */
/** A month of the club on the wall: popular routes, ignored ones, falls and clean ascents. */
const clubAttendance = (club: FakeClub) => {
  const climbers = ['lea.dyno', 'Tom', 'jules', 'nina', 'paulo', 'zoe'].map((nickname) =>
    club.addMember({ nickname }),
  )
  const plan: [string, number, number][] = [
    ['Bye Bye Morice', 14, 9],
    ['Iron Man', 9, 8],
    ['Réglisse', 7, 3],
    ['OH purée !', 6, 2],
    ['Pied-main party', 4, 4],
    ['Avant-bras en béton', 3, 0],
    ['Sec !', 2, 2],
  ]
  for (const [route, ascents, topped] of plan) {
    for (let index = 0; index < ascents; index++) {
      const climber = climbers[index % climbers.length]
      club.climbs.add(climber, 1 + (index % 15), [
        { route, topped: index < topped, clean: index < topped && index % 3 === 0 },
      ])
    }
  }
}

const clubPodium = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie === undefined) return
  const climbers: [string, string, string, number, number][] = [
    ['lea.dyno', 'Léa', 'Dupont', 9, 2],
    ['Tom', 'Tom', 'Bloc', 9, 3],
    ['jules', 'Jules', 'Martin', 8, 2],
    ['bernard-emmanuel', 'Bernard-Emmanuel', 'Delacroix-Vandenberghe', 6, 4],
    ['nina', 'Nina', 'Roche', 6, 1],
    ['paulo', 'Paulo', 'Prise', 5, 2],
    ['zoe', 'Zoé', 'Arête', 4, 2],
    ['kiki', 'Kiki', 'Dalle', 4, 1],
  ]
  for (const [nickname, firstName, lastName, days, topped] of climbers) {
    const member = club.addMember({ nickname, firstName, lastName })
    for (let day = 1; day <= days; day++) {
      club.climbs.add(
        member,
        day * 2,
        Array.from({ length: topped }, () => ({ route: 'Bye Bye Morice' })),
      )
    }
  }
  for (const day of [1, 4, 6]) {
    club.climbs.add(marie, day, [{ route: 'Réglisse' }, { route: 'Iron Man' }])
  }
}

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
  club.addMember({
    nickname: 'alix',
    firstName: 'Alix',
    lastName: 'Moreau',
    role: 'TOURIST',
    validated: false,
    declaredLicensed: false,
    joinNote: 'Collègue de Lou au boulot, je viens grimper un jeudi sur deux avec elle.',
    registeredAt: '2026-10-19T20:15:00Z',
  })
}

/** Marie has just joined: her account awaits the board (D-107). */
const marieAwaits = (club: FakeClub) => {
  const marie = club.members.find((member) => member.nickname === 'marie')
  if (marie) marie.validated = false
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
  { name: 'galerie', path: '/dev/galerie', as: null },
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
  {
    name: 'inscription-touriste',
    path: '/inscription?code=K7XM-P2QA',
    as: null,
    prepare: (club) => {
      club.addCode('K7XM-P2QA')
    },
    act: async (page) => {
      await page.getByRole('button', { name: 'Vérifier le code' }).click()
      await page.getByRole('radio', { name: /Non, je passe de temps en temps/ }).check()
      await page
        .getByLabel('Qui es-tu pour le club ?')
        .fill('Copine de Julie, je viens deux fois par an quand je passe dans le Nord.')
      await page.getByLabel('Qui es-tu pour le club ?').scrollIntoViewIfNeeded()
    },
  },
  { name: 'profil-en-attente', path: '/profil', as: 'TOURIST', prepare: marieAwaits },
  { name: 'podium-en-attente', path: '/podium', as: 'TOURIST', prepare: marieAwaits },
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
  {
    name: 'seance-deblocage',
    path: '/seance/nouvelle',
    as: 'CLIMBER',
    prepare: (club) => {
      club.climbs.nextUnlocks = ['GRADE_5B', 'GRADE_5C', 'GRADE_6A', 'FLASH']
    },
    act: async (page) => {
      await addToSession(page, '12', 'Bye Bye Morice')
      await page.getByRole('button', { name: 'Enregistrer la séance' }).click()
      await page.getByText('Badge débloqué ! · 1 sur 2').waitFor()
    },
  },
  {
    name: 'profil-badges',
    path: '/profil',
    as: 'CLIMBER',
    prepare: marieBadges,
    act: async (page) => {
      await page.getByText('8 sur 17 débloqués').waitFor()
    },
  },
  {
    name: 'profil-badges-tous',
    path: '/profil',
    as: 'CLIMBER',
    prepare: marieBadges,
    act: async (page) => {
      await page.getByRole('button', { name: 'Voir tous les badges' }).click()
    },
  },
  {
    name: 'badge-detail',
    path: '/profil',
    as: 'CLIMBER',
    prepare: marieBadges,
    act: async (page) => {
      await page.getByRole('button', { name: 'Voir tous les badges' }).click()
      await page.getByRole('button', { name: 'Voies topées, niveau 3' }).click()
      await page.getByRole('dialog', { name: 'Voies topées' }).waitFor()
    },
  },
  { name: 'actu-seances', path: '/actu', as: 'CLIMBER', prepare: marieClimbs },
  { name: 'actu-fil', path: '/actu', as: 'CLIMBER', prepare: feedAlive },
  {
    name: 'seance-ami',
    path: '/actu',
    as: 'CLIMBER',
    prepare: feedAlive,
    act: async (page) => {
      await page.getByRole('link', { name: 'Séance d’hier de tom.bloc' }).click()
      await page.getByRole('heading', { name: 'Commentaires (2)' }).waitFor()
    },
  },
  {
    name: 'mes-assurages',
    path: '/profil/assurages',
    as: 'CLIMBER',
    prepare: feedAlive,
  },
  {
    name: 'moderation',
    path: '/gestion/moderation',
    as: 'BOARD',
    desktop: true,
    prepare: feedAlive,
  },
  {
    name: 'profil-demandes',
    path: '/profil',
    as: 'CLIMBER',
    prepare: (club) => {
      marieOnTheWall(club)
      marieFriends(club)
      const marie = club.members.find((member) => member.nickname === 'marie')
      const lea = club.addMember({ nickname: 'zoe', firstName: 'Zoé', lastName: 'Lambert' })
      if (marie) club.social.link(lea, marie, false)
    },
    act: async (page) => {
      await page.getByRole('button', { name: '2 demandes d’ami' }).click()
    },
  },
  { name: 'annuaire', path: '/annuaire', as: 'CLIMBER', prepare: marieFriends },
  {
    name: 'membre-ami',
    path: '/annuaire',
    as: 'CLIMBER',
    prepare: marieFriends,
    act: async (page) => {
      await page.getByRole('link', { name: /Tom Bloc/ }).click()
      await page.getByRole('heading', { level: 2, name: 'Tom Bloc' }).waitFor()
    },
  },
  {
    name: 'membre-inconnu',
    path: '/annuaire',
    as: 'CLIMBER',
    prepare: marieFriends,
    act: async (page) => {
      await page.getByRole('link', { name: /Bernard-Emmanuel/ }).click()
      await page.getByRole('heading', { level: 2, name: /Bernard-Emmanuel/ }).waitFor()
    },
  },
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
  { name: 'podium', path: '/podium', as: 'CLIMBER', prepare: clubPodium },
  {
    name: 'podium-voies',
    path: '/podium?classement=voies',
    as: 'CLIMBER',
    prepare: clubPodium,
    act: async (page) => {
      await page.getByRole('list', { name: 'Les trois premiers' }).waitFor()
    },
  },
  { name: 'podium-vide', path: '/podium', as: 'CLIMBER' },
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
    name: 'seance-assureur',
    path: '/seance/nouvelle',
    as: 'CLIMBER',
    prepare: (club) => {
      marieClimbs(club)
      marieFriends(club)
    },
    act: async (page) => {
      await addToSession(page, '12', 'Bye Bye Morice')
      await page.getByRole('button', { name: /^À l’assurage sur la voie n° 1/ }).click()
      await page.getByRole('dialog', { name: 'Qui t’assurait ?' }).waitFor()
    },
  },
  {
    name: 'seance-detail',
    path: '/actu',
    as: 'CLIMBER',
    prepare: marieClimbs,
    act: async (page) => {
      await page.getByRole('link', { name: /Séance d’hier/ }).click()
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
  {
    name: 'compte',
    path: '/profil/compte',
    as: 'CLIMBER',
    act: async (page) => {
      await page.getByRole('button', { name: 'Supprimer mon compte' }).click()
    },
  },
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
    name: 'membre-a-valider',
    path: '/gestion/membres',
    as: 'BOARD',
    desktop: true,
    prepare: someMembers,
    act: async (page) => {
      await page
        .getByRole('region', { name: /En attente de validation/ })
        .getByRole('link', { name: /Alix Moreau/ })
        .click()
      await page.getByRole('heading', { name: 'Nouveau compte à valider' }).waitFor()
    },
  },
  {
    name: 'fiche-membre-admin',
    path: '/gestion/membres',
    as: 'ADMIN',
    desktop: true,
    prepare: someMembers,
    act: async (page) => {
      await page.getByRole('link', { name: /Talon/ }).first().click()
      await page.getByRole('button', { name: 'Supprimer définitivement le compte' }).click()
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
    name: 'gestion-frequentation',
    path: '/gestion/frequentation',
    as: 'SETTER',
    desktop: true,
    prepare: clubAttendance,
  },
  {
    name: 'gestion-bilan-du-topo',
    path: '/gestion/bilan-du-topo',
    as: 'SETTER',
    desktop: true,
    prepare: clubAttendance,
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
