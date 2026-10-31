# Journal des sessions

Entrée la plus récente en haut. Chaque entrée : date, branche, fait, reste, prochaine action exacte, points bloquants.

---

## 2026-10-31 — Comptes « Touriste » et validation par la direction (D-107)

**Branche** : `feature/comptes-touristes-0.9.0` (depuis `chore/comptes-touristes-decision`, PR #47 ; #46 y est incluse).

### Fait

- Backend : rôle `TOURIST`, V17 (`validated_at`, `declared_licensed`, `join_note`, codes du club → `TOURIST`),
  `LicenceDeclaration`, `Member.awaitingValidation` / `validate`, autorité `VALIDATED` + `@ValidatedMember`
  (annuaire, profils, badges des autres, demande d'ami, podium, commentaire de voie), comptes en attente exclus de
  l'annuaire, du podium et des assureurs, `PUT /admin/members/{id}/validation`, refus = `DELETE` par la direction
  d'un compte jamais validé (`MEMBER_REFUSED`). Seed : `touriste`, `nouveau`.
- Tests : unitaires (déclaration, validation, gouvernance), IT (inscription, gestion, matrice d'autorisations avec
  compte en attente, annuaire, podium, assureurs, seed). CI backend verte sur le premier commit.
- Front : question de licence + petit mot à l'inscription (`LicenceQuestion`), encart d'attente (profil, fil,
  podium, assureur, commentaires de voie), garde `meta.validated`, section « En attente de validation » et bloc de
  validation dans la gestion, i18n, faux backend, e2e `validation.spec.ts`, captures relues (clair, sombre, 360 et
  1280 px).
- Contrat OpenAPI écrit à la main : le test de contrat de la CI l'a confirmé identique.

### Reste

- Relecture et fusion de #47 (Willem), qui ferme #46.
- Hors code : passation de l'admin en prod (le président crée son compte, Willem le passe Admin, le président passe
  Willem Touriste).

### Prochaine action

Vérifier la CI du dernier commit de #47, puis reprendre la phase 8 (logo téléversable).

### Points bloquants

Aucun.

---

## 2026-10-31 — Décision : comptes « Touriste » et validation par la direction

**Branche** : `chore/comptes-touristes-decision` (depuis `main` en 0.9.0, docs seulement).

### Fait

- D-107 : rôle Touriste (mêmes droits que Grimpeur, distinction = licence), nouveaux comptes Touriste en attente de
  validation par la direction, compte en attente limité au carnet personnel, refus = suppression d'un compte jamais validé.
- D-047 mise à jour : Willem a utilisé le code admin de premier démarrage ; passation de l'admin au président prévue.
- ROADMAP phase 8 : ligne « Comptes Touriste » ajoutée.

### Reste

- Implémenter D-107 (`feature/comptes-touristes-0.9.0`) : migration Flyway (rôle, indicateur de validation, comptes
  existants validés), déclaration licencié·e ou non à l'inscription (texte obligatoire si non, effacé après décision),
  codes d'inscription → Touriste, règles serveur des comptes en attente, liste « en attente » en
  Gestion, message de profil, i18n, tests (unitaires, IT par rôle, e2e).
- Hors code : passation de l'admin en prod (le président crée son compte, Willem le passe Admin, le président passe
  Willem Grimpeur puis Touriste).

### Prochaine action

Créer `feature/comptes-touristes-0.9.0` depuis `main` et commencer par la migration et `MemberGovernance`.

### Points bloquants

Aucun.

---

## 2026-10-31 — Phase 7, PR 7.3 : badges dans le fil et moment de déblocage

**Branche** : `feature/badges-unlock-0.8.0` (depuis `feature/badges-library-0.8.0`, #43 pas encore fusionnée).

### Fait

- `lib/badges.ts` : `levelOfCode`, `unlocks` (paliers d'une famille regroupés au plus haut, répétable gagné deux fois
  dans la séance = ×2).
- Carte de séance : bloc `SessionUnlocks` (« Tu as débloqué » / « tom.bloc a débloqué », médaille au niveau, nom,
  niveau ou ×N) depuis `Session.badges`.
- `SessionSavedMoment` : tampon, puis chaque badge de `unlockedBadges` en grand (rebond, statique avec
  `prefers-reduced-motion`), « Badge suivant » / « Continuer » focalisés ; sans badge ou hors ligne, départ automatique
  comme avant. L'éditeur passe la destination au moment (`@done`).
- Fakes : `FakeSession.badges`, `club.climbs.nextUnlocks`. Vitest `SessionSavedMoment.spec.ts`, `unlocks` ; e2e
  « une séance enregistrée présente ses badges… » ; captures `seance-deblocage`, `actu-fil` relues (clair, sombre, 360 px).
- ROADMAP phase 7 entièrement cochée, DESIGN §8, CHANGELOG.

### Reste

- Relecture et fusion de #43 puis de cette PR (Willem). D-105 (club) et D-106 (Willem) à valider.
- Phase 8 : finitions et lancement.

### Prochaine action

Après fusion : phase 8, premier item de la ROADMAP (logo téléversable, icônes PWA, captures du manifeste).

### Points bloquants

- Aucun.

---

## 2026-10-31 — Phase 7, PR 7.2 : badges à niveaux, favoris et ancienneté

**Branche** : `feature/badges-library-0.8.0` (depuis `main` 0.8.0 + #42). PR #43.

### Fait

- Icônes dessinées `components/icons/BadgeIcon.vue` (grille Lucide) et médaille `BadgeMedal` (la prise de `HoldSwatch`).
- Refonte après retour de Willem (D-106) : 17 badges (`Badge` côté serveur, `badgeStates` côté appli), couleur = niveau
  sur une échelle de rareté (marron → or), répétables colorés selon ×N ; profil replié (« X sur 17 », 3 derniers, grille
  dépliable sans noms) ; fenêtre d'un badge (`BadgeDialog` : niveau / niveau max / ×N, description, prochain niveau,
  barre cumulée pour moi seulement) ; favoris (3 max) à côté du nom sur les profils et dans le fil.
- Backend : `Badge`, `ShownBadge`, `BadgeProgress`, `FavoriteBadge` (V16), `BadgeShowcase`, `PUT /me/badges/favorites`,
  `progress` et `favorites` dans la bibliothèque, `Session.authorBadges` ; tests unitaires + `BadgeIT` + matrice.
- Ancienneté (`SeniorityBadge`) sur les profils, dans l'annuaire à la place de l'année d'arrivée.
- Fakes e2e (bibliothèques, favoris, progression posés par les tests), Vitest `lib/badges.spec.ts`, e2e `badges.spec.ts`,
  captures `galerie`, `profil-badges(-tous)`, `badge-detail`, `actu-fil` relues en clair et sombre à 360 px.

### Reste

- PR 7.3 (front) : badges débloqués dans la carte du fil (« a débloqué : … », `Session.badges`), moment de déblocage
  après enregistrement (`unlockedBadges`, un par un, `prefers-reduced-motion`), cocher la ROADMAP.

### Prochaine action

Après fusion de la PR 7.2 : branche `feature/badges-unlock-0.8.0`, lire `components/session/SessionCard.vue` et le
flux d'enregistrement ; réutiliser `BadgeToken` (taille 96, `badgeOf` + niveau du palier) et `useBadgeText`.

### Points bloquants

- Aucun. D-105 à valider par le club, D-106 (couleurs, paliers des répétables) par Willem.

---

## 2026-10-30 — Phase 7, PR 7.1 : le moteur de badges (backend)

**Branche** : `feature/badges-0.8.0` (depuis `main` 0.8.0 + #39, #40, #41).

### Fait

- Module `badge` : catalogue `BadgeCode` (4 familles à paliers + 13 répétables, D-004 à D-006), `GradeScale`
  (comparaison des cotations sans base), moteur pur `BadgeEngine` (historique rejoué, clé stable par obtention, séance de
  rattachement), `AwardDiff`, `Seniority` (§9.4, D-008, année à Gravelines). Tests : `BadgeEngineTest` (une classe par
  badge, seuil exact / seuil −1, semaine à cheval, fin de mois, ordre de l'historique, causalité), `GradeScaleTest`,
  `SeniorityTest` (dont le Nouvel An UTC/Gravelines), `AwardDiffTest`, `BadgeCodeTest`.
- V15 `badge_award` ; `BadgeAwards.refresh` appelé par `SessionService` après création, modification et suppression
  (badge retiré si la condition ne tient plus, déplacé si une autre séance la remplit) ; seed de démo par le vrai moteur.
- API : `GET /me/badges`, `GET /members/{id}/badges` (bibliothèque complète, ×N, dates) ; `Session.badges` (rattachés)
  et `Session.unlockedBadges` (juste après un enregistrement) ; `seniority` dans le profil et les cartes de membre.
  `BadgeIT` + matrice d'autorisations. Contrat OpenAPI et types front régénérés. D-105.

### Reste

- Relecture et fusion de la PR 7.1 par Willem ; D-105 à valider par le club.
- PR 7.2 (front) : icônes SVG dessinées, bibliothèque de badges (mon profil, profil d'un autre), ancienneté profil +
  annuaire. PR 7.3 (front) : badges dans la carte du fil, moment de déblocage (reduced-motion).

### Prochaine action

- Si la CI est rouge : lire le journal du job Backend (`gh run view <run> --job <id> --log`, une fois le run fini).
  Sinon ouvrir la PR 7.1, puis enchaîner la 7.2 sur une branche depuis celle-ci ou depuis `main` après fusion.

---

## 2026-10-30 — Phase 6, PR 6.2 : Fréquentation et Bilan du topo

**Branche** : `feature/stats-0.8.0` (depuis `main` 0.8.0 + #39 et #40, D-104).

### Fait

- Backend : module `stats` (`Attendance` + taux de réussite, `AttendanceRepository` sur les ascensions,
  `StatsService` Ouvreur+, `GET /stats/attendance?month=`, `GET /stats/routes`), requête « voies au mur pendant le
  mois » dans `RouteRepository`. Tests : `AttendanceTest`, `StatsIT` (mois vierge, voies non grimpées, démontées
  exclues du bilan, 403 grimpeur, mois futur/mal formé), matrice d'autorisations.
- Front : `AttendanceView` (mois, totaux, tri dans l'adresse `?tri=`, table ≥ 1024 px, liste sur mobile, export CSV),
  `TopoReviewView` (cotations regroupées, secteurs, grille des cordes, plus/moins grimpées, anciennes, coups de cœur),
  `api/stats.ts`, `lib/stats.ts` + Vitest (tri, CSV et injection de formules, agrégats, ancienneté).
  e2e `stats.spec.ts` (Desktop Chrome en local), captures 360/1280 clair/sombre regardées (débordement mobile de la
  grille et « % » orphelin corrigés).

### Reste

- CI verte sur la branche (le contrat OpenAPI écrit à la main est identique à celui généré par la CI), PR à ouvrir
  puis relecture et fusion par Willem.
- D-104 à valider (taux de réussite = topées / ascensions ; histogramme par cotation de base).

### Prochaine action

- Phase 7 (badges) après fusion de la 6.2 : branche `feature/badges-<version>` depuis `main`.

---

## 2026-10-30 — Phase 6, PR 6.1 : le podium

**Branche** : `feature/podium-0.8.0` (depuis `main` 0.8.0, D-103).

### Fait

- Backend : V14 (`monthly_ranking_closure`, `monthly_ranking_snapshot`), module `ranking` (`Ranking` pur, `LiveRankings`,
  `RankingClosure` idempotente, `MonthlyClosureJob` à 04:00 Europe/Paris, `@EnableScheduling`), `GET /rankings`,
  `GET /rankings/months`. Suppression de compte (rangs → « Ancien·ne membre »), export (`frozenRanks`), seed via la
  vraie clôture. Tests : `RankingTest`, `RankingStatusTest`, `RankingIT` (mois « vierge » pour la base partagée),
  matrice d'autorisations.
- Front : `PodiumView` (marches, liste, barre « Ta place », mois, Séances/Voies), `api/rankings.ts`, `lib/podium.ts`
  + Vitest, invalidation après séance et profil, textes « Tes données ». e2e `podium.spec.ts` (Desktop Chrome),
  captures 360 clair/sombre regardées (contraste des marches corrigé).

### Reste

- CI verte ; `main` (correctif #40) fusionnée dans la branche ; relecture et fusion par Willem.
- PR 6.2 : Fréquentation (CSV) et Bilan du topo.
- D-103 validée par Willem : séances « juste moi » comptées, rangs figés gardés au nom d'« Ancien·ne membre ».

### Prochaine action

- PR 6.2 (Fréquentation + CSV, Bilan du topo), branche `feature/stats-<version>` depuis `main` après fusion de 6.1.

---

## 2026-10-30 — Correctif : « Voie introuvable » dans un brouillon de séance (signalé par Willem sur dev)

**Branche** : `fix/brouillon-voie-0.8.0` (depuis `main` 0.8.0, indépendante de la PR #39 du podium).

### Fait
- Cause : le brouillon (IndexedDB) garde l'identifiant des voies ; une voie démontée depuis, ou un topo de démo réinitialisé
  (dev : nouveaux identifiants à chaque déploiement), laissait une ligne « Voie introuvable » que le serveur refuserait.
  `withoutGoneRoutes` (lib/sessionForm.ts) retire ces lignes à la restauration, une fois les voies du mur chargées, avec un message.
- Brouillons rangés par membre (`draftKey(memberId, …)`) ; file d'attente hors ligne marquée par membre (`memberId`, `belongsTo`) :
  seule celle du membre connecté s'affiche et part. Les éléments déjà en file sans propriétaire partent comme avant.
- Tests : Vitest (sessionForm, outbox), e2e « drops from a kept draft the route dismounted since ».

### Reste / prochaine action
- Willem relit et fusionne ; puis reprise de la phase 6 (PR #39, puis PR 6.2).

### Points bloquants
- Aucun.

---

## 2026-10-29 — PR 5.3 : l'assureur (demande de Willem)

**Branche** : `feature/assureur-0.7.0` (D-101).

### Fait

- Backend : V13 (`ascent.belayer_id`), assureur facultatif par voie (membre actif, pas soi-même, sinon 400),
  affiché sur toute séance ; `GET /me/belays` et `DELETE /sessions/{id}/belayer` (`BelayService`) ; suppression de
  compte, export, seed. IT : `BelayIT`, `DemoSeederIT`, matrice d'autorisations. CI backend verte.
- Front : choix de l'assureur par voie (`BelayerPicker`, amis en tête, hérité de la voie d'avant), « À l'assurage »
  sur la carte et la fiche, Profil > Mes assurages (`BelaysView`). Vitest (`belayers`, `sessionForm`), e2e
  `belays.spec.ts` (72 verts en Desktop Chrome), captures 360 clair/sombre regardées.

### Reste

- CI complète de la PR, essai sur le dev si Willem le demande, fusion. D-101 à valider.
- Le job « Docker stack » échoue (sur `main` aussi depuis la 0.7.0) : Trivy trouve une faille critique corrigée dans la
  bibliothèque Go de l'image `caddy:2.11-alpine`. Hors de cette PR : monter l'image Caddy dès qu'une version corrigée
  existe (PR `chore/`).

### Prochaine action

Attendre Willem (essai dev, fusion), puis phase 6 (podium), ou la PR `chore/` pour l'image Caddy s'il le demande.

---

## 2026-10-28 — Phase 5, PR 5.2 : le fil d'actu

**Branche** : `feature/fil-0.6.0` (PR 5.2, voir D-100).

### Fait

- Backend : V12 (`session_reaction`, `session_comment`), `SessionReactionService` (upsert, pas sur sa propre séance),
  `SessionCommentService` (auteur ou direction, audit `SESSION_COMMENT_DELETED`), `SessionService.feed`
  (`GET /feed`), `ModerationService` (`GET /admin/moderation/comments`, voies + séances fusionnées par curseur).
  Toute séance porte `author`, `reactions`, `commentCount`. Suppression de compte (réactions/commentaires des autres
  sur mes séances), export, seed (`DemoFeedSeeder`). IT : `SessionReactionIT`, `SessionCommentIT`, `FeedIT`,
  `ModerationIT`, `AccountIT`, `DemoSeederIT`, matrice d'autorisations. CI backend verte.
- Front : Actu sur le fil (`api/feed.ts`), carte avec auteur, réactions (optimistes, `lib/reactions.ts` + Vitest),
  nombre de commentaires ; commentaires sur la fiche séance (`CommentThread`, partagé avec les voies) ;
  Gestion > Modération. Textes « Tes données ». e2e Desktop Chrome (70 verts), captures 360 clair/sombre et 1280
  (modération) regardées.

### Reste

- CI complète sur le dernier commit, essai sur le dev (label `deploy:dev`) si Willem le demande, fusion.
- D-100, D-098, D-096, D-093, D-094, D-087, D-090 à valider ; PR #17 en suspens.

### Prochaine action

- Après fusion de la PR 5.2 : phase 6 (Podium), branche `feature/<slug>-<version>` depuis `main`.

---

## 2026-10-27 — Phase 5, PR 5.1 : les amis

**Branche** : `feature/amis-0.5.1` (PR 5.1, voir D-098).

### Fait

- Question de Willem : pas de base graphe (Neo4j) ; amitiés directes, table PostgreSQL (D-098).
- Backend : domaine `social` (V11 `friendship`, une ligne par paire), `FriendshipService` (demander/accepter,
  refuser/annuler/retirer, tout depuis mon côté), annuaire et profils publics (`DirectoryService`, membres actifs
  seulement), `SessionAudience` (séance, photo, liste et carnet d'un ami : amis acceptés, visibilité « amis »,
  404 sinon). `Session.memberId` exposé. Export et seed (grimpeur : 2 amis + 1 demande). IT : `FriendshipIT`,
  `DirectoryIT`, `FriendSessionsIT`, matrice d'autorisations, `AccountIT`, `DemoSeederIT`.
- Front : `/annuaire` (recherche, « Mes amis »), `/membres/:id`, `/membres/:id/carnet` (LogbookView réutilisée),
  demandes en haut de l'Actu et du profil, pastille sur l'avatar, compteur du club dans l'Actu, séance d'un ami en
  lecture seule. Vitest (`lib/friendship`), e2e `social.spec.ts` (Desktop Chrome), captures 360 px clair/sombre
  regardées (débordement d'un pseudo très long corrigé).
- Déployée sur le dev. Retours de Willem appliqués (D-099) : demandes d'amis retirées du fil, bandeau repliable dans
  le profil ; « Se déconnecter » ; accueil « Se connecter » / « Rejoindre le club ».

### Reste

- PR 5.2 : fil d'actu (amis), réactions, commentaires de séance, modération.
- D-098, D-096, D-093, D-094, D-087, D-090 à valider ; PR #17 en suspens.

### Prochaine action

- Faire essayer la PR 5.1 sur le dev (label `deploy:dev`) ; après fusion, créer `feature/fil-<version>` depuis `main`.

---

## 2026-10-27 — La cotation avant la corde sur l'étiquette

**Branche** : `feature/cotation-avant-corde-0.5.0` (PR #30, en prod en 0.5.1).

### Fait

- Demande du club : `RouteLabel` met la cotation dans le bloc encre à gauche et la corde à droite, plus petite, sous un
  petit « corde » (D-097). Libellé accessible dans le nouvel ordre ; ruban « Dernière chance » et tampon « Démontée »
  décalés à gauche de la corde. DESIGN.md §5 et maquettes mises à jour. Vitest, e2e Desktop Chrome, captures 360 px
  clair/sombre (topo, fiche, fil, archives) et 1280 px (gestion des voies) regardées.

- Déployée sur le dev (label `deploy:dev`), essayée et validée par le club. Un test de `e2e-stack/` cherchait encore
  « Corde 12, … » (job « Docker stack » rouge au premier run) : corrigé avant la fusion.
- PR #30 fusionnée, **release 0.5.1 en prod** (PR #31).

### Reste

- D-096, D-093, D-094, D-087, D-090 à valider ; PR #17 en suspens.

### Prochaine action

- Phase 5 (Social) : relire `docs/ROADMAP.md`, créer `feature/<slug>-0.5.1` depuis `main`.

---

## 2026-10-26 — PR 4.3 : comptes et données personnelles

**Branche** : `feature/comptes-0.4.0` (PR #25).

### Fait

- Backend : migration V10 « Ancien·ne membre » (D-096) ; `AccountDeletionService` (libre-service derrière le mot de
  passe, admin seulement côté gestion, dernier admin protégé) qui transmet séances et voies ouvertes, efface avatar et
  photos de séance (fichiers compris) ; export JSON `GET /api/v1/me/export` ; `sessionCount` dans le tableau des membres
  et `canDelete` sur la fiche. `countMembers()` ignore l'ancien membre (premier démarrage, seed). `AccountIT` +
  matrice d'autorisation.
- Front : page « Mes données et mon compte » (`/profil/compte`), colonne « Séances » triable, suppression depuis la fiche
  (admin), journal (« X a supprimé son compte »), textes de « Tes données ». Vitest, e2e Desktop Chrome, captures
  360 px et 1280 px clair/sombre regardées.

### Reste

- CI verte sur le run de la PR (204b91d). Le run « push » du même commit a vu son job e2e dépasser 15 min et
  être coupé (aucun test en échec) : à relancer si la protection de branche l'exige.
- D-096 à valider par Willem (audit conservé avec le pseudo, photos listées mais pas incluses dans l'export).
- Puis Willem fusionne #25 et lance la release (0.5.0). Ensuite : phase 5 (Social).
- D-093, D-094, D-087, D-090 à valider ; PR #17 en suspens.

---

## 2026-10-26 — PR 4.2 : retours de Willem sur le dev

**Branche** : `feature/planning-0.3.0` (PR #23).

### Fait

- Déployée sur le dev (label `deploy:dev`), essayée par Willem.
- Nouvelle séance, sélecteur de voies : une voie déjà dans la séance est marquée (« ✓ Dans la séance », fond teinté,
  « + » en contour pour un passage de plus) et un « − » reprend son dernier passage (`removeLastOf`, testé). e2e des
  séances complété, captures 360 px clair/sombre regardées.

### Reste

- Willem fusionne #23, puis lance « Prepare release » (`minor` → 0.4.0) et approuve la release prod. Claude ne
  déclenche jamais la release.
- Ensuite, dans une nouvelle session : PR 4.3 (Comptes et données personnelles), branche `feature/comptes-0.4.0`
  depuis `main` à jour.
- D-093, D-094, D-087, D-090 à valider ; PR #17 en suspens.

---

## 2026-10-25 — PR 4.2 Planning, À faire, Carnet

**Branche** : `feature/planning-0.3.0` (depuis `main` = 0.3.0, PR 4.1 fusionnée et publiée).

### Fait

- Backend, sans migration : `GET /me/todo` (topo, `TodoStates` : état par mode `TODO` / `CHECKED` / `FROM_ASCENT` /
  `FROM_LEAD`, D-092), `GET /me/planning?month=` (session : jours, `MonthTotals`, `Streak`, `PlanningMonth`, D-093),
  `GET /me/logbook` (session : `Logbook`, `AscentBest`, D-094). Unitaires + `TodoIT`, `PlanningIT`, `LogbookIT`
  (calculs et isolement entre membres), matrice d'autorisations, `SessionFixtures` pour les IT. CI backend : 440 tests
  verts au premier passage, contrat régénéré depuis le journal.
- Front : `/a-faire` (onglets, progression, filtres dans l'URL, cocher + « Annuler » 5 s, celles que je sais faire,
  fiche voie par-dessus), `/planning` (calendrier lundi en premier, jour → séances, « Noter une séance ce jour-là »,
  bilan, série), `/carnet` (chiffres, pyramide, toutes mes voies) avec lien depuis le Profil. Lecture hors ligne
  network-first, cache vidé à la connexion et à la déconnexion (D-095).
- Vitest 224, e2e Desktop Chrome 62 (dont 8 nouveaux), captures 360 px clair/sombre regardées et corrigées
  (listes tronquées, chevron, mois vide allégé, « Démontée » en double).

### Reste

- CI de la PR, déploiement dev (label `deploy:dev`) et essai par Willem ; fusion ; puis PR 4.3 (Comptes et données
  personnelles) sur une nouvelle branche depuis `main`.
- D-093 (seuil de la série) et D-094 (contenu du carnet) à valider ; D-087, D-090 toujours à valider ; PR #17 en suspens.

### Prochaine action exacte

- `gh pr checks <n°>` sur la PR 4.2 ; si vert, demander à Willem s'il veut le déploiement dev pour tester À faire,
  Planning et Carnet sur téléphone (dont le mode avion pour la lecture hors ligne).

### Points bloquants

- Aucun.

---

## 2026-10-24 — PR 4.1 Séances

**Branche** : `feature/seances-0.2.0` (depuis `main` = 0.2.0).

### Fait

- Backend : V9 (`climbing_session`, `ascent`, `media_file.usage` + `SESSION_PHOTO`), paquet `session` : création
  idempotente par `clientRef` (D-085), lecture, modification, suppression, photo, mes séances par curseur, mes voies
  grimpées. Mois figé et fenêtre de 7 jours côté serveur (D-084), voie démontée acceptée si démontée après la séance
  (D-089), « je sais la faire » automatique + `CHECK_FROM_ASCENT` (D-086), statistiques de la fiche voie et `toppedCount`
  (via `RouteAscentStats`, D-090), photo de séance réservée au propriétaire (`PrivateMediaAccess`, D-087).
  Seed : `DemoSessionSeeder` (10 semaines de séances pour grimpeur, direction, ouvreur). CI backend verte (SessionIT 13,
  matrice 295), contrat régénéré depuis le journal.
- Front : feuille de saisie (`/seance/nouvelle`, `/seances/:id/modifier`), brouillon IndexedDB, file hors ligne avec
  photo (bandeau dans l'app), page d'une séance, Actu « Mes séances », fiche voie « Au compteur » + « Mon historique »,
  tri « Réalisations ». Vitest 192, e2e Desktop Chrome 54 (dont 7 séances), captures 360 px clair/sombre regardées
  (saisie, Actu, détail, attente, fiche voie).
- PWA (D-091) : recherche de mise à jour au retour au premier plan et toutes les heures, bouton Profil « Chercher une
  mise à jour » avec la liste des fichiers refusés, sonde `/sonde/temoin.js` (Caddy). Cause du Profil bloqué sur
  l'iPhone de Willem : l'extension Safari « Cyberfiltre » (bloqueurs « Phishing1-7 ») ; coupée, tout passe.

### Reste

- CI de la PR, déploiement dev (label `deploy:dev`) et essai par Willem ; fusion ; puis PR 4.2 (Planning, À faire,
  Carnet) sur une nouvelle branche depuis `main`.
- D-087 et D-090 à valider ; PR #17 (graphify) toujours en suspens.

### Prochaine action exacte

- `gh pr checks <n°>` sur la PR 4.1 ; si vert, demander à Willem d'approuver le déploiement dev et de tester la saisie
  sur téléphone (dont le mode avion).

### Points bloquants

- Aucun.

---

## 2026-10-23 — Phase 3 publiée (0.2.0), phase 4 découpée en trois PR

**Branche** : `feature/seances-0.2.0` (depuis `main` = 0.2.0), créée pour démarrer la phase 4.

### Fait

- PR #18 (`mvnw.cmd`) et #19 (Phase 3 — Topo) fusionnées par Willem ; release `0.2.0` publiée et déployée en prod
  (tag `v0.2.0`, déploiement prod vert). D-074 à D-077 et D-082 validées par Willem.
- Phase 4 découpée en trois PR successives (voir `docs/ROADMAP.md`) : 4.1 Séances, 4.2 Planning / À faire / Carnet,
  4.3 Comptes et données personnelles.

### Reste

- Toute la phase 4, en commençant par la PR 4.1.
- PR #17 (graphe graphify) : en conflit, périmée (instantané 0.1.1) et sa décision porte le numéro D-074, déjà pris.
  Question posée à Willem : la fermer, ou la refaire sur le code à jour (décision renumérotée). Ne pas la fusionner telle quelle.
- 24 décisions encore « à valider » dans le récapitulatif de `docs/DECISIONS.md` (surtout côté club).

### Prochaine action exacte

- PR 4.1 sur `feature/seances-0.2.0` : relire la spec (`docs/KICKOFF_PROMPT.md`, sections séances, ascensions,
  mois figé, hors ligne) et `docs/DESIGN.md` (écran de saisie), puis concevoir le modèle (séance, ascensions, photo ;
  migration V9) et le contrat API avant le code.

### Points bloquants

- Aucun.

---

## 2026-10-23 — Phase 3 (Topo) : cotations avec les « - »

**Branche** : `feature/topo-0.1.1`, PR #19 (label `deploy:dev` posé : chaque push redéploie le dev après approbation).

### Fait

- Willem : le club utilise aussi les « - ». Cotations 4a- → 7c+ (36, « - » < simple < « + ») dans V7 (pas encore sur
  `main`, donc modifiable ; le dev est réinitialisé à chaque déploiement), seed (chaque cotation au moins une fois),
  `ClubAdminIT`, données des e2e (une voie en 6c-), texte de la configuration du club, D-002/D-074 révisées, CHANGELOG.
- Local : lint, format, typecheck, Vitest 169, e2e Desktop Chrome 47 verts ; capture vérifiée (« 6c- » entre 6b et 7a).

### Reste

- CI de la PR, approbation du déploiement dev par Willem, vérification sur le dev ; fusion ; release `0.2.0`.

### Prochaine action exacte

- `gh pr checks 19` ; si vert, Willem approuve le déploiement dev et teste.

### Points bloquants

- Aucun.

---

## 2026-10-21 — Phase 3 (Topo) : revue visuelle faite, phase terminée

**Branche** : `feature/topo-0.1.1`, PR #19 « Phase 3 — Topo ». PR #18 à part (`mvnw.cmd`).

### Fait

- Conteneur redémarré : captures 360/1280 px clair/sombre du topo, de la fiche, des archives, de la pop-up, de la
  gestion des voies et de la configuration du club ; revue UX (sous-agent) puis corrections (D-083) :
  table de gestion qui tenait en 1365 px (colonnes Cotation et Ouverture retirées, dates courtes, icônes) ; dialogues
  desktop coupés (`sm:bottom-auto`) ; tampon « Démontée » au-dessus de l'étiquette ; prise noire visible en sombre ;
  tris « Secteur » / « Ouverture » ; points « · » jamais orphelins (utilitaire `dot-list`) ; secteur non répété en tri par
  secteur ; menu de gestion actif sur les sous-pages ; « 1er » dans les dates ; espaces insécables avant `? ! : ;` ;
  « Prises : jaune et bleu » ; avertissements reka-ui (description de dialogue) ; captures sans animation, en fr-FR.
- Lint, format, typecheck, Vitest (169), e2e Desktop Chrome (47) verts en local.

### Reste

- Willem : valider D-074..D-077, D-082 ; fusionner la PR #19 ; release `0.2.0`.
- Les champs date des captures restent en mm/jj/aaaa ici : le Chromium du bac à sable n'a que la locale en-US.

### Prochaine action exacte

- Vérifier la CI de la PR #19 (iPhone 13 + Desktop), puis attaquer la phase 4 (`docs/ROADMAP.md`) après la fusion.

### Points bloquants

- Aucun.

---

## 2026-10-21 — Phase 3 (Topo) : code complet, CI verte, captures à faire

**Branche** : `feature/topo-0.1.1` (depuis `main` = 0.1.1). PR « Phase 3 — Topo ». PR #18 à part (`mvnw.cmd`).

### Fait

- Backend : paquets `club` (secteurs, cordes, couleurs, cotations ; V7 avec la configuration réelle, identique dev/prod ;
  configuration par l'admin, journalisée) et `topo` (voies, ouvreurs, styles, photo, statuts ; commentaires et modération ;
  coups de cœur limités à 2 avec remplacement ; « je sais la faire » ; dernière chance et prises de connaissance ; voie
  préférée ; V8). Seed de démo : 60 voies, 5 démontées, 3 en dernière chance, commentaires, cœurs pleins. Tous les points
  d'accès dans `AuthorizationMatrixIT`. Compilé et vert en CI dès le premier passage (sauf le contrat, écrit à la main).
- Front : topo (tri, filtres combinables dans l'URL, regroupement par secteur), archives, fiche voie en feuille, cœurs
  (« Ton cœur est plein »), coches, commentaires, pop-up dernière chance, voie préférée (premiers pas + profil),
  gestion des voies (création, modification, duplication, photo, dernière chance, démontage, remontage), vue dernière
  chance (avec changement de date), configuration du club (admin). e2e : `topo.spec.ts`, `topo-management.spec.ts`,
  94 tests verts en CI (iPhone 13 + Desktop Chrome) ; e2e de la vraie stack : connexion, pop-up, topo réel.
- Revue sécurité (sous-agent) : aucun point critique ni haut ; 2 moyens et 6 bas corrigés (photo de voie qui survit au
  compte, sous-échantillonnage arrondi au-dessus, 409 `CONCURRENT_CHANGE`, verrous sur les prises de connaissance,
  envoi par lots de 50, 10 ouvreurs max, `@PlainText` contre les caractères de contrôle, modération sur voie démontée).
- Décisions D-074 à D-082. ARCHITECTURE, CHANGELOG « Non publié », CLAUDE.md (contrat depuis le journal CI, `reap`).

### Reste

- **Captures non prises** : le bac à sable a 950 processus zombies (PID 1 = `sleep`, qui ne les ramasse pas) et
  ~1000/1024 PID occupés ; Chromium ne démarre pas. Il faut redémarrer le conteneur (ou fermer les vieilles sessions
  Claude Code) puis : `pnpm build`, `pnpm screenshots --grep "topo|gestion"` en clair et sombre, regarder 360 et 1280 px,
  corriger, et faire la revue UX de fin de phase.
- Ensuite : fusion de la PR par Willem, release `0.2.0` (minor : nouvelles fonctionnalités).

### Prochaine action exacte

- Après redémarrage du conteneur : `cd frontend && pnpm install && pnpm build && pnpm screenshots --project=mobile-light`
  (puis `mobile-dark`, `desktop-light`, `desktop-dark`), regarder `frontend/screenshots/` pour le topo, la fiche voie, la
  pop-up, la gestion des voies, le formulaire, la configuration du club ; corriger ; cocher la dernière ligne de la phase 3.

### Points bloquants

- Ressources du bac à sable (voir « Reste »).

---

## 2026-10-21 — Phase 2 terminée : 0.1.1 en prod, retour arrière testé

**Branche** : `chore/release-rollback-doc` (depuis `main`).

### Fait

- PR #13 fusionnée, release **0.1.1** en prod. Retour arrière testé par Willem : 0.1.1 → 0.1.0 (« Serveur 0.1.0 » dans le
  profil) → 0.1.1. Premier essai refusé : lancé depuis la branche `prod/0.1.0`, que l'environnement `production` refuse.
  `DEPLOYMENT.md` §7 le précise maintenant, et `release.yml` échoue d'emblée avec un message clair (job « Started from main »).
- D-041 contrôlée sur appareil : la session suit à l'installation de la PWA depuis Safari (iOS 27).
- Dependabot #6 et #7 fusionnées par Willem (partiront dans la prochaine release).
- ROADMAP : phases 1 et 2 entièrement cochées.

### Reste

- `backend/mvnw.cmd` (venu de Dependabot #6) est en LF alors que `.gitattributes` veut CRLF : git le voit modifié après
  checkout. À renormaliser (`git add --renormalize backend/mvnw.cmd`) dans une PR à part.

### Prochaine action exacte

- Phase 3 (Topo), nouvelle session : branche `feature/topo-0.1.1` depuis `main`, en commençant par la configuration du club
  (secteurs, cordes, couleurs de prises, cotations ; ROADMAP phase 3).

### Points bloquants

- Aucun.

---

## 2026-10-21 — Correctif iPhone et logo du club (vers 0.1.1)

**Branche** : `fix/iphone-profil-0.1.0` (depuis `main` = 0.1.0), PR #13.

### Fait

- Bug iPhone : un filtre du téléphone de Willem (Safari et PWA) refusait tout fichier dont l'URL contient « profil », avant
  même le serveur. Trouvé grâce aux sondes de l'écran d'erreur, après deux fausses pistes (appli périmée, zstd) : D-073.
  Correctif : fichiers construits nommés par leur seule empreinte (`vite.config.ts`). Vérifié par Willem sur le dev : le profil
  s'ouvre, l'enregistrement du profil passe.
- Au passage : `router.onError` recharge la page quand le code d'un écran manque ; au second échec, `LoadFailureScreen`
  (réessayer, vider le cache, retour à l'actu, détails copiables) au lieu d'un écran vide ; Caddy met un 404 de `/assets/` en
  `no-store`.
- En-tête : logo du club (clair/sombre) + « GG Pocket » ; logo retiré de l'encart Actu (D-072).
- CI verte (e2e iPhone 13 + Desktop Chrome, stack Docker). CHANGELOG « Non publié » à jour.

### Reste

- Captures non prises ici (bac à sable saturé de processus) : rendu du logo à confirmer par Willem.
- Fusion de la PR #13, release 0.1.1, test du retour arrière (dernier point de la phase 2).

### Prochaine action exacte

- Willem : fusionner la PR #13, Actions → « Prepare release » en `patch` (0.1.1), fusionner la PR de release, approuver la
  prod ; puis Actions → Release → Run workflow avec `0.1.0`, vérifier la version dans le profil, et redéployer 0.1.1.

### Points bloquants

- Aucun.

---

## 2026-10-21 — Fin de phase 2 : prod 0.1.0 en ligne

**Branche** : `fix/iphone-profil-0.1.0` (depuis `main` = 0.1.0).

### Fait

- PR #4 (phase 2) fusionnée, puis #9 (pipeline : PR de release, Dependabot sur les LTS, clés gardées par `setup-github.sh`) et
  #11 (test e2e de version lisant `VERSION`). Dependabot #5 et #8 (Node 26) fermées.
- Willem : `setup-github.sh` + `bootstrap.sh` sur le VPS OVH (51.68.44.88). Dev déployé depuis la PR (label), puis release
  **0.1.0** en prod (https://pocket.51-68-44-88.sslip.io) après régénération des clés prod (`--new-keys`).
- Corrigé en route : sauvegarde de sécurité de `restore.sh` qui pouvait écraser la sauvegarde restaurée (dossier `pre-restore/`).

### Reste

- Bug iPhone (PWA seulement) : l'avatar de l'en-tête n'ouvre pas le profil. Logo du club dans l'en-tête, nom « GG Pocket »,
  logo retiré de l'encart Actu. → release 0.1.1, qui sert aussi au test de retour arrière (dernier point de la phase 2).

### Prochaine action exacte

- Nouvelle session : lire `.relais/HANDOFF.md` (local, non versionné) et suivre « Prochaine étape ».

### Points bloquants

- Aucun.

---

## 2026-10-20 — Phase 2 (environnements et pipeline)

**Branche** : `chore/environnements` (depuis `main` après fusion de la phase 1), poussée.

### Fait

- Réponses de Willem (§20) : VPS OVH Ubuntu 8 Go avec Docker, rien d'autre dessus, pas encore de domaine → `sslip.io` (D-059).
- Plan : `docs/plans/phase-2.md`. Décisions D-059 à D-070.
- Backend : plafond dur du limiteur (50 000 clés, D-069), garde-fou « pas d'identifiants locaux hors local » avant toute
  connexion (D-064), Tomcat 11.0.25 et Jackson 3.1.7 / 2.21.7 épinglés (CVE critiques et hautes, D-070).
- Images `backend/Dockerfile`, `frontend/Dockerfile` (non root, couches, healthchecks, version et commit injectés) ;
  `deploy/compose{,.dev,.prod,.ci}.yml` ; Caddy `web` (CSP stricte, HSTS, en-têtes, `/api`) et frontal `deploy/edge/` (D-060).
- Scripts `deploy/scripts/` : `deploy.sh` (sauvegarde prod, healthchecks, fumée, retour arrière auto), `rollback.sh`,
  `backup.sh` + `backup-loop.sh`, `restore.sh` + `restore-data.sh`, `backup-now.sh`, `ci-restore-test.sh` ;
  serveur : `server/bootstrap.sh`, porte SSH `server/gg-deploy` (D-062).
- Workflows : `ci.yml` (+ jobs « Deploy config » et « Docker stack » : images, Trivy, frontal en HTTPS, e2e réels,
  sauvegarde/restauration ; + `workflow_dispatch`), `deploy-dev.yml`, `prepare-release.yml`, `release.yml`,
  `dependabot.yml`, `.github/scripts/changelog.py`, `.github/scripts/setup-github.sh` (D-065, D-066).
- e2e contre la vraie stack (`frontend/e2e-stack/`, 5 tests, Chromium, via le frontal en HTTPS) et test
  sauvegarde → dégâts → restauration : verts. Trouvé en route : volume des photos à root (service `media-owner` ajouté).
- `docs/DEPLOYMENT.md`, CLAUDE.md (commandes), ARCHITECTURE §6, ROADMAP, CHANGELOG.
- Revue de sécurité de fin de phase (sous-agent) : 2 hautes, 4 moyennes, 3 basses, toutes traitées (journaux de l'API prod
  hors des jobs publics, approbation exigée aussi sur `dev`, retour arrière sans téléchargement, pas de code d'inscription
  sur le dev déployé, jetons en lecture seule et `DOCKER_CONFIG` par environnement, rôle de base sans superutilisateur,
  surveillance des sauvegardes, purge du limiteur par lots) ; le point IPv6 était déjà couvert (/64). D-062, D-063, D-071.
- Label `deploy:dev` créé. Le jeton `gh` de Claude n'a pas les droits d'administration (environnements, secrets, règles) :
  tout est dans `setup-github.sh` pour Willem.

### Reste

- Willem : `setup-github.sh --host <ip>` puis `bootstrap.sh` sur le VPS (DEPLOYMENT.md §2-3).
- Premier déploiement dev (label `deploy:dev` sur la PR), vérification D-041 sur iPhone, fusion, release `0.1.0`,
  approbation, retour arrière testé (`release.yml` lancé à la main avec la version précédente… ou `gg-deploy rollback`).

### Prochaine action exacte

- Attendre que Willem ait lancé `setup-github.sh` et `bootstrap.sh`, puis poser le label `deploy:dev` sur la PR et suivre
  le déploiement (`gh run list --workflow deploy-dev.yml`).

### Points bloquants

- Accès d'administration GitHub et adresse IP du serveur : côté Willem (voir « Reste »).
- Bac à sable : journaux bruts de la CI injoignables (annotations à la place), réseau GitHub intermittent.

---

## 2026-10-20 — Phase 1 (comptes et rôles)

**Branche** : `feature/comptes-0.0.0` (depuis `main` après fusion de la phase 0), poussée ; PR #3 « Phase 1 — Comptes et rôles », CI verte (backend, frontend, e2e iPhone 13 + Desktop Chrome, gitleaks).

### Fait

- Plan de phase : `docs/plans/phase-1.md` (fichiers, migrations, endpoints, écrans, tests).
- Backend (domaines `member`, `auth`, `admin`, `seed`) : migrations V2 à V6 ; inscription par code (vérification, verrou sur
  l'usage), connexion, session Spring Session JDBC 180 jours glissants / 12 h sans « Rester connecté » (cookie
  `__Host-GG_SESSION`, renvoyé tous les 7 jours), rôle et statut relus à chaque requête, « Mes appareils », changement de mot
  de passe, lien de réinitialisation (haché, 24 h, fragment d'URL), limitation de débit, code admin de premier démarrage,
  photo de profil nettoyée, gestion des membres (matrice D-049), journal d'audit, codes d'inscription, seed des comptes de
  démonstration avec ses garde-fous (§17).
- Frontend : accueil, connexion, inscription, nouveau mot de passe, « Tes données », premiers pas (photo, infos, installation),
  profil et réglages, appareils, espace gestion (navigation latérale ≥ 1024 px), PWA (manifeste, icônes, service worker
  `prompt`, bandeau de mise à jour), invitation à installer (Android, iOS, navigateurs intégrés, 7 jours × 3), CSRF en en-tête,
  401 → connexion, hors ligne sans déconnexion.
- Tests : backend 152+ (matrice d'autorisation : 18 endpoints × 5 profils), Vitest 97, e2e 25 par navigateur contre une
  fausse API avec état (`e2e/fakeClub.ts`), dont inscription → premiers pas → installation → retour 30 jours plus tard.
- Revues de fin de phase par sous-agents : sécurité (3 points moyens, 6 mineurs, tous corrigés sauf la limite d'entrées du
  limiteur, notée) et UX (corrections appliquées, voir le commit `fix: apply the end-of-phase security and UX reviews`).
- Captures relues en 360 px clair/sombre et 1280 px ; sélection dans `docs/captures/phase-1/`.
- Docs : DECISIONS D-048 à D-058, ARCHITECTURE, ROADMAP, CHANGELOG, README (comptes de test, premier démarrage).

### Reste

- Relecture et fusion de la PR par Willem (squash).
- Points de la revue UX non retenus : journal d'audit avec nom complet (le journal ne garde que le pseudo du moment, à revoir
  si la direction le demande), format des champs date selon l'appareil (à vérifier sur téléphone réel).
- Contrôle sur un vrai iPhone de la session après installation (D-041), sur l'environnement dev (phase 2).

### Prochaine action exacte

- Après fusion : `git checkout main && git pull`, brancher `chore/environnements` pour la phase 2, mode plan d'abord, puis
  demander à Willem les infos du §20 (serveur, domaines, DNS, accès dev) dès que la liste est prête.

### Points bloquants

- Aucun pour la phase 1.
- Bac à sable : réseau vers GitHub intermittent (relancer `git push`/`gh` en boucle) ; plafond de 1024 PID (~820 utilisés en fin
  de session à cause des zombies de Chromium) : limiter les lancements Playwright.

---

## 2026-10-18 — Phase 0 (fondations), session 3

**Branche** : `feature/fondations-0.0.0`, poussée ; PR ouverte vers `main`.

### Fait

- Jeton GitHub mis à jour par Willem : branche poussée.
- CI : frontend, e2e (iPhone 13/WebKit et Desktop Chrome) et gitleaks verts du premier coup. Le backend est passé au vert
  après 5 corrections, toutes liées à l'outillage de format et jamais au code applicatif :
  - google-java-format 1.37 incompatible avec Spotless 3.10.4 (`Style` devenu un record) : épinglé en 1.36.1 (D-020) ;
  - étape `removeUnusedImports` retirée (redondante) ;
  - ordre des imports AOSP (`reorderImports`) pour coller au binaire local ;
  - commentaire XML invalide ;
  - `.mvn/jvm.config` (exports `jdk.compiler`).
- Backend vert : 20 tests unitaires + 10 tests d'intégration Testcontainers, ArchUnit, contrat OpenAPI identique au fichier
  écrit à la main (vérifié par `OpenApiContractIT`), zéro avertissement Maven.
- Captures de la PR dans `docs/captures/phase-0/` (360 px, clair et sombre).

### Reste

- Relecture et fusion de la PR par Willem (squash). Ensuite, activer la protection de `main` (prévu en phase 2, mais faisable
  tout de suite dans les réglages GitHub : PR obligatoire, CI obligatoire, pas de force-push).

### Prochaine action exacte

- Une fois la PR fusionnée : `git checkout main && git pull`, puis brancher `feature/comptes-0.0.0` pour la phase 1,
  en commençant par le mode plan (fichiers, migrations, endpoints, écrans, tests) comme le demande la spec §0.

### Points bloquants

- Aucun pour la phase 0.
- Réseau du bac à sable instable vers GitHub (coupures de quelques secondes) et artefacts de la CI non téléchargeables
  (stockage Azure inaccessible) : relancer les commandes `gh`/`git` en boucle ; lire les journaux avec `gh run view --log`.


## 2026-10-17 — Phase 0 (fondations), session 2

**Branche** : `feature/fondations-0.0.0` (commits locaux, **toujours pas poussée** : jeton GitHub en lecture seule).

### Fait

- Revue UX (sous-agent, sur les captures) appliquée : mot-symbole lisible dans le header (le logo complet restait illisible
  à 44 px, D-039), étiquette de voie (tampon « Démontée » qui laisse le nom lisible, coin orange pour la dernière chance en
  petit format, séparateur de cotation), états vides en bande pleine largeur, focus déplacé sur le contenu à chaque navigation,
  titres d'écran en `text-xl`, contrôle segmenté 48 px disponible, apostrophes typographiques, textes revus.
- Revue sécurité/backend (sous-agent, API vérifiées dans les sources GitHub) appliquée :
  - `-Xlint:-path`, sans quoi la compilation des tests aurait échoué (Class-Path de jaxb-runtime) ;
  - en prod, `GG_ENVIRONMENT` est obligatoire et le profil `prod` refuse un environnement autre que `PROD` (testé) ;
  - le message d'une `BusinessException` ne part plus au client ;
  - repli `sendError` si le rendu d'une erreur 401/403 échoue ;
  - `/error` autorisé.
- La revue confirme comme probablement conformes : le contrat OpenAPI écrit à la main, les signatures Spring 7 / Jackson 3 /
  ArchUnit / Testcontainers, les versions des actions de la CI.
- Frontend : lint, format, typecheck, 49 tests Vitest, build et 9 e2e Desktop Chrome verts.

### Reste pour clore la phase 0

- Pousser, faire passer la CI (backend jamais compilé), ouvrir la PR avec les captures.

### Prochaine action exacte

1. `git push -u origin feature/fondations-0.0.0`, puis `gh run watch`.
2. Corriger le backend jusqu'au vert. Si `OpenApiContractIT` échoue : `gh run download <id> -n openapi-contract -D /tmp/contract`,
   `cp /tmp/contract/openapi.json frontend/src/api/openapi.json && (cd frontend && pnpm api:types)`.
3. Ouvrir la PR « Phase 0 — Fondations » (gabarit `.github/pull_request_template.md`, captures de `frontend/screenshots/`).

### Points bloquants

- **Push impossible** : il faut un jeton fine-grained sur `Whiplashhhh/GG_Pocket` avec Contents, Workflows et Pull requests
  en lecture/écriture, et Actions en lecture.
- Bac à sable : les processus zombies de Chromium ne sont jamais récupérés (plafond 1024 PID/threads, ~500 déjà pris).
  Playwright doit tourner avec `--workers=1` (voir CLAUDE.md, section Commandes).

---

## 2026-10-17 — Phase 0 (fondations), session 1

**Branche** : `feature/fondations-0.0.0` (commits locaux, **pas encore poussée** : voir blocage).

### Fait

- Lecture de la spec, de CLAUDE.md et des références (logo, maquettes, Oblyk).
- Docs : ROADMAP, DECISIONS (14 choix du §19 « à valider », versions épinglées D-020, décisions techniques D-030 à D-042,
  dont le comportement d'iOS D-041), DESIGN (palette contrôlée AA, Archivo + Kalam, wireframes, étiquette de voie,
  relecture « générique ? » et itérations après captures), ARCHITECTURE, README, CONTRIBUTING, gabarit de PR, CHANGELOG.
- Backend (écrit, **jamais compilé** : pas de JDK ni de Maven Central ici) : Spring Boot 4.1.1, `platform`
  (`/api/v1/meta`, Problem Details + `ErrorCode`, sécurité fermée par défaut + CSRF SPA, marqueur d'environnement en base
  avec migration V1, horloge injectable), tests unitaires, `@WebMvcTest`, intégration Testcontainers, ArchUnit, contrat OpenAPI.
- Frontend (vérifié en local) : Vue 3.5 / Vite 8 / TS 6 strict, Tailwind 4, vue-i18n précompilé (pas d'`eval`),
  TanStack Query, Pinia (thème), client typé openapi-fetch ; tokens clair/sombre/auto ; composants de base ; étiquette de voie
  et prise dessinée ; app shell (header, 5 onglets, FAB, bandeau DEV), écrans vides, profil avec choix du thème, 404,
  galerie `/dev/galerie`. Lint, format, typecheck, 50 tests Vitest, build (≈ 95 ko gzip de JS initial) et 9 tests e2e
  (projet Desktop Chrome) verts. Captures 360/1280 clair/sombre relues et corrigées.
- `deploy/compose.local.yml`, `.env.example`, `.github/workflows/ci.yml` (backend, frontend, synchro des types, e2e, gitleaks).

### Reste pour clore la phase 0

- Pousser la branche, faire passer la CI (backend jamais compilé : s'attendre à quelques allers-retours).
- Remplacer `frontend/src/api/openapi.json` (écrit à la main au plus près de la sortie de springdoc) par l'artefact
  `openapi-contract` de la CI, puis `pnpm api:types`.
- Corrections issues des revues sécurité et UX (en cours en fin de session).
- Ouvrir la PR « Phase 0 — Fondations » avec les captures.

### Prochaine action exacte

1. `git push -u origin feature/fondations-0.0.0`, puis `gh run watch` sur le workflow CI.
2. Corriger le backend jusqu'au vert ; récupérer le contrat : `gh run download <id> -n openapi-contract -D /tmp/contract`
   puis `cp /tmp/contract/openapi.json frontend/src/api/openapi.json && (cd frontend && pnpm api:types)`.
3. Ouvrir la PR.

### Points bloquants

- **Le jeton GitHub de l'environnement est en lecture seule** (push refusé en 403). Il faut un jeton fine-grained sur
  `Whiplashhhh/GG_Pocket` avec Contents (lecture/écriture), Workflows (lecture/écriture), Pull requests (lecture/écriture),
  Actions (lecture). Demandé à Willem.
- Outils locaux installés hors dépôt dans `~/.local` (Node 24, pnpm, google-java-format, Chromium autonome) : à réinstaller
  si l'environnement est réinitialisé (procédure dans DECISIONS D-030 et CLAUDE.md).
