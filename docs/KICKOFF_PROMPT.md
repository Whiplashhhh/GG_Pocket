# GG Pocket — spécification complète et brief de développement

> Destinataire : Claude Code (Opus 5.5), travaillant de façon autonome dans ce dépôt, sur plusieurs sessions et plusieurs jours.
> Commanditaire : Willem, développeur du projet. Utilisateurs : les membres du club d'escalade Gravelines Grimp.
> Ce document est la source de vérité produit. `CLAUDE.md` est le manuel d'opération court relu à chaque session.

---

## 0. Ta mission et ta façon de travailler

Tu construis GG Pocket de zéro, dans un dépôt vide, en suivant la roadmap du §18 phase par phase.
Le projet ne tiendra pas en une session : c'est prévu et normal. Ce qui compte, c'est que chaque session
laisse le dépôt dans un état propre, testé, documenté, et que la suivante sache exactement où reprendre.

Ta première session (phase 0) commence par :

1. Lire ce document en entier, puis les images de `docs/references/` (logo du club, maquettes de la famille,
   captures de l'appli Oblyk qu'ils utilisaient avant et qu'il faut surpasser).
2. Créer `docs/ROADMAP.md` (la roadmap du §18 découpée en tâches à cocher), `docs/PROGRESS.md` (journal de sessions),
   `docs/DECISIONS.md` (décisions prises, une entrée par décision : contexte, choix, alternatives, statut « validée » ou « à valider »).
   Recopie dans `DECISIONS.md` tous les choix par défaut du §19 avec le statut « à valider ».
3. Vérifier sur le web les versions stables actuelles de chaque brique (Java LTS, Spring Boot, Vue, Vite, Tailwind,
   PostgreSQL, Node LTS, Caddy, plugins) et les épingler. Note les versions retenues dans `DECISIONS.md`.
4. Suivre la phase 0 du §18.

Niveau d'exigence attendu : celui d'une équipe senior qui livre un produit qu'elle va maintenir pendant des années.
Code lisible, nommage explicite, pas de code mort, pas de « TODO » silencieux (un TODO = une ligne dans ROADMAP.md),
pas de dépendance ajoutée sans raison. Préfère la simplicité robuste à l'astuce. Quand tu hésites entre deux approches,
prends celle qu'un développeur qui découvre le projet comprendra le plus vite (Willem relit tout, et quelqu'un d'autre
devra peut-être reprendre le projet un jour).

Avant chaque phase, passe en mode plan : liste les fichiers, les migrations, les endpoints, les écrans et les tests
que tu vas produire, vérifie la cohérence avec ce document, puis exécute. En fin de phase, fais une revue critique
de ton propre travail (sécurité, autorisations, cas limites, accessibilité, rendu visuel) et corrige avant d'ouvrir la PR.

---

## 1. Contexte

- Club : Gravelines Grimp (« GG »), club d'escalade associatif à Gravelines (Nord), mur de cordes en salle.
  Disciplines du club : escalade, canyoning, alpinisme (l'appli ne couvre que la grimpe en salle).
- Le club a essayé Oblyk (appli d'escalade généraliste). Échec d'adoption : il fallait 3 ou 4 clics pour atteindre
  l'espace du club, l'interface n'était pas intuitive. GG Pocket doit être l'inverse : dédiée au club, tout à un pouce.
- Gouvernance : un président (admin), une « direction » (famille du président et membres importants),
  des « responsables » qui ouvrent et démontent les voies (les ouvreurs), et les grimpeurs.
- Le président s'y connaît en informatique mais n'utilise pas git. Tout ce dont le club a besoin au quotidien (gestion des membres,
  des voies, du logo, lecture des notes de version) se fait dans l'appli, jamais dans le dépôt ni en ligne de commande.
  La doc technique s'adresse à un développeur ; un court `docs/GUIDE_CLUB.md`, sans aucun jargon, explique à la direction
  comment administrer le club depuis l'appli.

### Ce qu'il y a dans `docs/references/`

- `GG_logo.jpeg` : logo officiel sur fond blanc (le club l'utilise aussi beaucoup sur fond noir).
  Couleurs mesurées sur le fichier (à affiner toi-même) : orange ≈ `#F9561A`, vert olive/kaki ≈ `#C3C04C`, noir `#000000`.
- `exemple1.webp`, `exemple2.webp` : maquettes faites par la famille (onglet Actu vide, onglet Planning).
  À retenir : header avec avatar + pseudo + badge de rôle, barre d'onglets Actu / Topo GG / À faire / Planning / Podium / Profil,
  calendrier mensuel avec compteur de présences, bouton « + » pour enregistrer une séance. À faire mieux : le rendu
  actuel est générique (cartes blanches arrondies identiques), sans personnalité.
- `oblyk*.webp` : l'appli Oblyk sur le topo du club. À retenir : liste des voies avec photo ronde, nom, numéro de relais
  (= numéro de corde), pastilles de couleur des prises, cotation, compteurs (likes, commentaires, réalisations), tags de style
  (Conti, Rési, Physique, Complexe…), fiche voie (photo, ouvreur, date d'ouverture, secteur, styles), menu de tri
  (date d'ouverture, secteur, cotation, réalisations, likes, commentaires, voies démontées). À ne pas reproduire : la
  profondeur de navigation, la densité confuse, le topo 3D (hors périmètre).

---

## 2. Principes produit (à appliquer à chaque décision)

1. **Un pouce, deux secondes.** Les actions fréquentes (enregistrer une séance, voir le topo, voir ce qu'il reste à faire)
   sont accessibles depuis l'ouverture de l'appli en un geste.
2. **Zéro friction de connexion.** On se connecte une fois, puis on n'y pense plus pendant des mois (voir §6).
3. **Pensé pour la salle.** Réseau parfois mauvais, mains pleines de magnésie, téléphone posé au sol : grosses cibles,
   saisie rapide, brouillon local qui survit à une perte de réseau.
4. **Drôle mais clair.** Ton humoristique du vestiaire d'escalade (voir §11), jamais au prix de la compréhension.
5. **Neutre au lancement.** En prod, l'appli démarre vide (hors configuration du club : secteurs, cordes, cotations)
   pour que les vrais membres s'inscrivent et invitent les autres. Les données de démonstration n'existent qu'en dev.
6. **L'historique est sacré.** Démonter une voie ne fait rien disparaître des carnets, classements et badges.
7. **Installable.** PWA de qualité « appli native » : icône, splash, plein écran, mise à jour propre.

---

## 3. Glossaire (à utiliser dans le code et l'UI)

| Terme UI | Identifiant code | Sens |
|---|---|---|
| Voie | `Route` | Un itinéraire ouvert sur une corde, identifié par corde + couleur de prises + nom |
| Corde (n°) | `Cord` | Ligne d'assurage numérotée 1 à 29 ; Oblyk l'appelle « relais » |
| Secteur | `Sector` | Pan de mur regroupant des cordes consécutives |
| Cotation | `Grade` | Difficulté, de 4a à 7c (avec les « + », voir §19) |
| Ouvreur | `setter` | Responsable qui a ouvert la voie |
| Moulinette (« mouli ») | `TOP_ROPE` | Corde déjà en haut |
| En tête | `LEAD` | Le grimpeur mousquetonne en montant |
| Topée | `topped` | Le grimpeur a atteint le relais (des chutes ou repos sur corde sont possibles) |
| Enchaînée | `clean` | Topée sans aucune chute ni repos sur la corde |
| Séance | `Session` | Une venue à la salle, avec ses voies |
| Ascension / essai | `Ascent` | Une tentative d'une voie pendant une séance |
| Coup de cœur | `Favorite` | Voie chérie par un grimpeur (2 max) |
| Dernière chance | `LAST_CHANCE` | Voie annoncée comme bientôt démontée |
| Démontée | `DISMOUNTED` | Voie retirée du mur (suppression logique) |

Code (identifiants, commentaires) en anglais ; UI, docs et CHANGELOG en français.

---

## 4. Stack technique imposée

### Backend : `backend/`

- Java 25 LTS (repli possible sur 21 si une brique bloque, à noter dans DECISIONS), Maven, Spring Boot dernière version stable.
- Spring Web, Spring Security, Spring Session JDBC (sessions serveur en base), Spring Data JPA (Hibernate),
  Flyway, PostgreSQL, Bean Validation, springdoc-openapi, Actuator (health uniquement exposé).
- Traitement d'images côté serveur (redimensionnement, ré-encodage, suppression EXIF) avec une bibliothèque Java maintenue.
- Tests : JUnit 5, AssertJ, Spring Security Test, Testcontainers PostgreSQL. ArchUnit pour protéger le découpage en couches.
- Architecture : découpage par domaine (`auth`, `member`, `topo`, `session`, `social`, `ranking`, `badge`, `admin`, `club`),
  chaque domaine avec `api` (controllers + DTO), `application` (services, règles), `domain` (entités), `infra` (repos).
  Pas d'entité JPA exposée dans l'API : DTO explicites. Erreurs au format Problem Details (RFC 9457) avec un `code`
  métier stable que le front traduit en message (humoristique) via i18n.
- Fuseau horaire métier : `Europe/Paris` partout (jours, semaines ISO lundi-dimanche, mois, classements).

### Frontend : `frontend/`

- Vue 3 (Composition API, `<script setup>`), Vite, TypeScript `strict`, Vue Router, Pinia (état UI uniquement),
  TanStack Query pour Vue (état serveur, cache, invalidations), Tailwind CSS v4, VueUse, vue-i18n (fr uniquement),
  vite-plugin-pwa (Workbox). Composants headless accessibles (Reka UI) habillés par notre design system ; pas de kit visuel prêt-à-porter.
- Types API générés depuis l'OpenAPI du backend (`openapi-typescript` + client typé). La génération est une commande
  documentée et la CI échoue si les types commités sont désynchronisés.
- Polices auto-hébergées (`@fontsource` ou fichiers locaux) : aucun appel à Google Fonts ou autre CDN.
- Tests : Vitest + Vue Test Utils ; Playwright (projets « iPhone 13 » et « Desktop Chrome »).
- Budget : JS initial < 200 ko gzip ; routes de gestion chargées en lazy.

### Pourquoi pas autre chose (pour mémoire, ne pas rediscuter)

- Pas de Nuxt/SSR : aucun besoin de SEO, une SPA PWA est plus simple et évite la transmission des cookies de session côté serveur.
- Pas de JWT : sessions serveur + cookie HttpOnly, plus simples à révoquer et plus sûres dans un navigateur.
- Pas de Redis : Spring Session JDBC suffit largement pour un club ; un service de moins à opérer.

### Organisation du dépôt

```
/
├── CLAUDE.md
├── README.md                 # présentation, démarrage local en 5 minutes, liens docs
├── CHANGELOG.md              # notes de version, en français, ton du club (affichées dans l'appli)
├── CONTRIBUTING.md           # workflow git, conventions, comment proposer une modif
├── backend/
├── frontend/
├── deploy/
│   ├── compose.local.yml     # Postgres (+ mailpit si besoin) pour le dev local
│   ├── compose.yml           # stack applicative (base commune dev/prod)
│   ├── compose.dev.yml       # surcharges environnement dev
│   ├── compose.prod.yml      # surcharges environnement prod
│   ├── caddy/                # Caddyfile(s)
│   └── scripts/              # backup.sh, restore.sh, deploy.sh, rollback.sh
├── docs/
│   ├── KICKOFF_PROMPT.md     # ce document
│   ├── ROADMAP.md  PROGRESS.md  DECISIONS.md  DESIGN.md
│   ├── ARCHITECTURE.md       # schéma, domaines, flux d'auth, modèle de données (diagramme mermaid)
│   ├── DEPLOYMENT.md         # environnements, pipeline, release, rollback, sauvegardes, restauration
│   ├── GUIDE_CLUB.md         # mode d'emploi de l'administration depuis l'appli, pour la direction (zéro jargon)
│   └── references/           # images fournies
└── .github/workflows/        # ci.yml, deploy-dev.yml, release.yml
```

---

## 5. Rôles et permissions

Quatre rôles hiérarchiques ; chaque rôle a les droits des rôles inférieurs.

| Capacité | Grimpeur | Responsable | Direction | Admin |
|---|:-:|:-:|:-:|:-:|
| Profil, séances, amis, fil, commentaires, coups de cœur, à faire, podium, badges | ✅ | ✅ | ✅ | ✅ |
| Créer / modifier une voie, conseil d'ouvreur, photo de voie | | ✅ | ✅ | ✅ |
| Passer une voie en « dernière chance », la démonter, la remonter (annuler un démontage) | | ✅ | ✅ | ✅ |
| Stats de fréquentation des voies et bilans du topo | | ✅ | ✅ | ✅ |
| Supprimer le commentaire de quelqu'un d'autre (modération) | | | ✅ | ✅ |
| Liste des membres, désactiver un compte, générer un lien de réinitialisation de mot de passe | | | ✅ | ✅ |
| Donner / retirer le rôle Responsable | | | ✅ | ✅ |
| Donner / retirer les rôles Direction et Admin, supprimer définitivement un compte | | | | ✅ |
| Codes d'inscription, configuration du club (nom, logo, secteurs, cordes, couleurs de prises, cotations) | | | | ✅ |
| Journal d'audit | | | ✅ (lecture) | ✅ |

Règles : il doit toujours exister au moins un admin actif (le serveur refuse de retirer le dernier). Personne ne
modifie son propre rôle. Chaque changement de rôle, désactivation, démontage, suppression de commentaire par un tiers
et changement de configuration est tracé dans un journal d'audit (qui, quoi, quand, avant/après).

Le badge de rôle affiché sous le pseudo (cf. maquette « GRIMPEUR GG ») se nomme, avec humour :
Grimpeur → « Grimpeur GG », Responsable → « Ouvreur·se », Direction → « Direction (ça grimpe en haut) », Admin → « Le Président·e ».
Ces libellés sont dans l'i18n et modifiables facilement.

---

## 6. Authentification et sessions

Objectif : sécurité raisonnable, friction minimale. Une personne qui ouvre l'appli au moins une fois tous les six mois
ne doit jamais avoir à se reconnecter.

### Inscription

1. Écran d'accueil (non connecté) : logo du club, une phrase d'accroche, « J'ai un code du club » et « J'ai déjà un compte ».
2. Saisie du code d'inscription (fourni par le club, affiché à la salle). Codes gérés par l'admin : libellé, date d'expiration
   optionnelle, nombre d'utilisations max optionnel, actif/inactif, compteur d'utilisations. Tentatives de code limitées en débit.
3. Formulaire court en une page : prénom, nom (affiché aux membres), pseudo unique (identifiant de connexion),
   e-mail optionnel (sert uniquement à se reconnaître ; pas d'envoi de mail en v1), mot de passe (8 caractères minimum,
   refus des 1000 mots de passe les plus courants, pas d'autres règles pénibles), date de naissance optionnelle.
   Si la date de naissance indique moins de 15 ans : case obligatoire « Un parent ou responsable légal est d'accord ».
   Case obligatoire d'acceptation de la page « Tes données » (voir §14).
4. Connexion immédiate après inscription, puis enchaînement : photo de profil (facultative, passable),
   quelques infos (année d'arrivée au club, grimpe depuis, voie préférée, tout passable), puis invitation à installer l'appli (§12).

Les attributs `autocomplete` (`username`, `new-password`, `current-password`) sont corrects pour que le trousseau
iCloud, Google ou Bitwarden proposent d'enregistrer les identifiants (c'est la demande « enregistrer les codes de connexion »).

### Session longue

- Session serveur (Spring Session JDBC), cookie `HttpOnly`, `Secure`, `SameSite=Lax`, posé par le serveur
  (les cookies écrits en JavaScript sont limités à 7 jours par Safari : interdit ici).
- Durée d'inactivité 180 jours, glissante : chaque visite repousse l'échéance. Case « Rester connecté » cochée par défaut ;
  décochée, la session expire à la fermeture du navigateur.
- Écran « Mes appareils » dans le profil : liste des sessions actives (appareil approximatif, dernière activité), bouton
  pour en fermer une ou toutes les autres.
- iOS : l'appli installée sur l'écran d'accueil peut ne pas partager les cookies de Safari. Le parcours doit donc supposer
  qu'une reconnexion unique est possible juste après l'installation et la rendre indolore (trousseau, message drôle et clair).
  Vérifie le comportement actuel d'iOS sur le web et documente-le dans DECISIONS.
- CSRF : protection Spring Security adaptée aux SPA (jeton dans un cookie lisible par le front, renvoyé en en-tête).
- Même origine pour le front et l'API (`/api` derrière Caddy) : pas de CORS en prod.

### Mot de passe oublié (sans e-mail)

Pas de serveur mail en v1. Un membre de la direction ou l'admin génère depuis la fiche du membre un lien de réinitialisation
à usage unique valable 24 h, qu'il transmet par le canal de son choix (copier/partager). Le lien mène à un écran
« Nouveau mot de passe ». Toutes les autres sessions de l'utilisateur sont fermées après le changement.

### Sécurité de base

- Hash de mot de passe via `DelegatingPasswordEncoder` (bcrypt ou argon2), limitation de débit sur la connexion,
  l'inscription et la réinitialisation, messages d'erreur qui ne révèlent pas si un pseudo existe.
- Compte désactivé : sessions supprimées immédiatement, connexion refusée avec un message clair.
- Passkeys (WebAuthn, supporté nativement par les versions récentes de Spring Security) : phase ultérieure (§18), à concevoir
  dès maintenant pour qu'elles s'ajoutent sans refonte.

---

## 7. Modèle de domaine (base de travail, à affiner dans ARCHITECTURE.md)

Identifiants UUID. Horodatages `created_at` / `updated_at` partout. Suppression logique là où l'historique compte.

- **User** : pseudo (unique, insensible à la casse), prénom, nom, e-mail (nullable, unique), hash du mot de passe, rôle,
  statut (`ACTIVE`, `DISABLED`), avatar (référence fichier), date de naissance (nullable), afficher mon âge (bool, défaut faux),
  année d'arrivée au club (nullable), grimpe depuis (année, nullable), voie préférée (référence voie, nullable, peut pointer
  une voie démontée), bio courte (nullable, 160 caractères), apparaître dans les classements (bool, défaut vrai),
  dernière version de l'appli vue (pour les notes de version), consentements (date d'acceptation, accord parental le cas échéant).
- **RegistrationCode**, **PasswordResetToken** (stocké haché, usage unique, expiration), **AuditLog**.
- **ClubSettings** (une ligne) : nom affiché, logo version fond clair, logo version fond sombre, texte d'accueil.
- **Sector** : nom, ordre d'affichage, description courte optionnelle.
- **Cord** : numéro (unique), secteur, actif. Seed de configuration (identique en dev et en prod) :

  | Secteur | Cordes |
  |---|---|
  | Neutre 8 m | 1 à 3 |
  | Neutre 12 m | 4 à 6 |
  | Positif | 7 à 9 |
  | Négatif et ses 2 dièdres | 10 à 14 |
  | Grand dévers | 15 à 17 |
  | Cheminée | 18 à 19 |
  | Grand toit | 20 à 21 |
  | Sous toit | 22 à 24 |
  | Petit dévers | 25 à 27 |
  | Petit toit | 28 à 29 |

  L'admin peut renommer un secteur, ajouter une corde, déplacer une corde de secteur (sans casser l'historique :
  une ascension garde la voie, la voie garde sa corde, le secteur affiché est celui de la corde).
- **HoldColor** : couleurs de prises configurables (libellé + hex), seed : jaune, orange, rouge, rose, violet, bleu,
  bleu clair, vert, vert clair, noir, blanc, gris, marron, multicolore.
- **Grade** : liste ordonnée 4a, 4a+, 4b, 4b+, 4c, 4c+, 5a, 5a+, 5b, 5b+, 5c, 5c+, 6a, 6a+, 6b, 6b+, 6c, 6c+, 7a, 7a+, 7b, 7b+, 7c.
  Stockée avec un rang numérique pour trier et comparer.
- **Route** : nom, cotation, corde, couleur(s) de prises (1 ou 2), date d'ouverture, ouvreurs (utilisateurs, 1 ou plus,
  plus un champ texte libre pour un ouvreur sans compte), photo (optionnelle), styles (tags parmi une liste :
  Conti, Rési, Physique, Technique, Complexe, Dalle, Dévers, Réglettes, Bacs, Pinces, Plats, Dièdre, Toit, Morpho),
  conseil de l'ouvreur (texte, 500 caractères), statut (`ACTIVE`, `LAST_CHANCE`, `DISMOUNTED`), date prévue de démontage
  (si dernière chance), date de démontage, auteur.
- **RouteComment** : voie, auteur, texte, supprimé par (nullable).
- **Favorite** (coup de cœur) : utilisateur, voie, date. Contrainte métier : au plus 2 coups de cœur portant sur des voies
  non démontées par utilisateur. Un coup de cœur sur une voie démontée libère la place mais reste visible dans l'historique du profil.
- **RouteCheck** (« je sais la faire ») : utilisateur, voie, mode (`TOP_ROPE` ou `LEAD`), source (`MANUAL` ou `ASCENT`).
- **Session** : grimpeur, date (défaut aujourd'hui), ressenti global /10, commentaire, photo optionnelle,
  visibilité (amis, par défaut ; ou « juste moi »).
- **Ascent** : séance, voie, ordre, mode (`TOP_ROPE`/`LEAD`), topée (bool), enchaînée (bool, implique topée et zéro chute),
  nombre de chutes (entier ≥ 0), premier essai de ma vie sur cette voie (bool, prérempli automatiquement si aucune ascension
  antérieure n'existe), ressenti /10 (optionnel). La même voie peut apparaître plusieurs fois dans une séance.
- **Friendship** : demandeur, destinataire, statut (`PENDING`, `ACCEPTED`), dates. Relation symétrique une fois acceptée.
- **SessionReaction** (une réaction par personne et par séance, parmi un petit jeu fixe) et **SessionComment**.
- **LastChanceAck** : utilisateur, voie, date de prise de connaissance.
- **UserBadge** : utilisateur, code du badge, nombre d'obtentions, première et dernière date d'obtention.
- **MonthlyRankingSnapshot** : mois, type de classement, rang, utilisateur, valeur ; figé à la clôture du mois.
- **MediaFile** : chemin de stockage, type, dimensions, propriétaire, usage. Fichiers sur un volume Docker, servis par le
  backend après contrôle d'accès (jamais une URL publique devinable).

---

## 8. Écrans et fonctionnalités

### 8.1 Navigation

- Barre d'onglets en bas, **5 entrées maximum** (6 est trop serré à 360 px) : **Actu**, **Topo**, **À faire**, **Planning**, **Podium**.
- Le **Profil** s'ouvre en touchant l'avatar dans le header (comme sur la maquette).
- Bouton flottant « + » **Nouvelle séance**, visible sur Actu et Planning, à portée de pouce.
- Pour Responsable et au-dessus : une icône **Gestion** dans le header ouvre l'espace de gestion. Sur écran ≥ 1024 px,
  l'espace de gestion a une navigation latérale et des tableaux confortables.
- Header : logo du club (version clair/sombre selon le thème), avatar, pseudo, badge de rôle. Bandeau permanent
  « ENVIRONNEMENT DE DEV — branche x @ sha » en environnement dev.

Ce découpage diffère des 6 onglets de la maquette : c'est volontaire (choix par défaut à valider, §19).

### 8.2 Actu (fil d'actualité)

- En-tête du club : logo, nom, nombre de grimpeurs inscrits (« 87 grimpeurs accrochés à l'appli »). Toucher le nombre
  ouvre l'**annuaire** : recherche par nom ou pseudo, bouton « Demander en ami » / « En attente » / « Amis ».
- Demandes d'amis reçues : pastille sur l'avatar + section en haut du fil, accepter/refuser.
- Fil : mes séances et celles de mes amis, plus récentes d'abord, pagination infinie. Carte de séance façon Strava :
  avatar, nom, date relative, résumé chiffré (nombre de voies, topées, en tête, cotation max, ressenti global), photo si présente,
  liste compacte des voies (étiquette de voie, mode, topée/enchaînée, chutes), commentaire du grimpeur,
  réactions (jeu fixe de 4 : 🔥 💪 👏 🦍, une par personne, modifiable), commentaires (auteur ou modération peuvent supprimer).
  Les badges gagnés pendant la séance apparaissent dans la carte (« a débloqué : Le bébé mousqueton »).
- États vides drôles et utiles (« Le mur est calme… trop calme. Ajoute des amis ou enregistre ta première séance. »).

### 8.3 Nouvelle séance (le parcours le plus important de l'appli)

- Feuille plein écran, optimisée pour une saisie en 30 secondes à la salle.
- Date : aujourd'hui par défaut, modifiable vers le passé (pas de futur).
- Ajouter une voie : sélecteur rapide qui propose d'abord les voies récemment grimpées par moi, puis recherche par numéro de corde
  (taper « 12 » montre les voies de la corde 12), nom, couleur ou cotation. Chaque voie s'affiche avec son étiquette (§10).
- Pour chaque voie : mode (Mouli / Tête, gros boutons segmentés), Topée (oui/non), Enchaînée (oui/non, désactivé si non topée),
  chutes (stepper − / +, mis à 0 et verrouillé si enchaînée), premier essai (prérempli), ressenti /10 (curseur ou 10 pastilles, optionnel).
  Dupliquer une ligne en un geste (pour un deuxième essai). Réordonner, supprimer.
- Fin de séance : ressenti global /10, commentaire, photo optionnelle (compressée côté client avant envoi, puis traitée côté serveur),
  visibilité. Bouton « Enregistrer la séance ».
- Brouillon : la saisie en cours est sauvegardée localement (IndexedDB) à chaque modification et restaurée à la réouverture.
  Si le réseau tombe à l'envoi, la séance part en file d'attente et s'envoie au retour du réseau, avec un état clair
  (« En attente de réseau — elle partira toute seule »).
- Après enregistrement : animation discrète de validation, badges débloqués présentés un par un, retour au fil.
- Le propriétaire peut modifier ou supprimer sa séance (les classements des mois clos ne bougent pas, §9.3).

### 8.4 Topo

- Liste des voies actives et en dernière chance. Tri : cotation (défaut), date d'ouverture, secteur/corde, nombre de réalisations,
  nombre de coups de cœur, nombre de commentaires ; ordre croissant/décroissant. Filtres combinables : secteur, cotation min/max,
  couleur, style, « pas encore faite par moi ». Regroupement par secteur quand on trie par secteur.
- Ligne de liste : vignette photo, étiquette de voie (nom, cotation, couleur, corde), secteur, tags de style en icônes,
  compteurs, marque « faite en mouli / en tête » pour moi, marque « dernière chance ».
- Entrée « Voies démontées » (archives) : consultable, lecture seule, avec date de démontage.
- **Fiche voie** (feuille qui glisse du bas, plein écran possible) : photo, nom, cotation, corde et secteur, couleur(s), ouvreur(s),
  date d'ouverture, styles, **conseil de l'ouvreur** mis en valeur, statistiques (nombre d'ascensions enregistrées, nombre de topées,
  nombre de grimpeurs différents, part en tête), nombre de coups de cœur, commentaires (ajout, suppression des siens),
  mon historique sur cette voie, bouton coup de cœur, cases « je sais la faire en mouli / en tête ».
- Coups de cœur : si j'en ai déjà 2 actifs, toucher le cœur ouvre « Ton cœur est plein. Lequel tu lâches ? » avec mes 2 voies ;
  en choisir une la remplace.

### 8.5 À faire

- Deux onglets internes : **En mouli** et **En tête**. Chacun liste les voies actives que je ne « sais » pas encore faire dans ce mode.
- Une voie est « sue » dans un mode si je l'ai cochée manuellement OU si j'ai une ascension **enchaînée** dans ce mode
  (cochage automatique, source `ASCENT`). « Sue en tête » implique « sue en mouli ».
- Cocher fait disparaître la voie de la liste (avec annulation possible pendant quelques secondes). Un lien « Voir celles que je sais faire »
  permet de décocher.
- Progression en haut : « 34 / 58 voies en tête. Il en reste 24, courage. » Filtres secteur et cotation, tri par cotation.
- Les voies démontées n'apparaissent jamais ici.

### 8.6 Planning

- Calendrier mensuel (lundi en premier, contrairement à la maquette), navigation mois par mois, compteur de présences du mois.
- Un jour avec au moins une séance porte une coche ; toucher le jour ouvre le résumé des séances de ce jour.
- Sous le calendrier : petit bilan du mois (séances, voies topées, cotation max, ressenti moyen) et série en cours
  (« 3 semaines d'affilée avec 2 séances ou plus »).

### 8.7 Podium

- Deux classements du mois en cours, mis à jour en temps réel : **Le plus de séances** et **Le plus de voies** (définitions exactes §9.2).
- Top 3 mis en scène (podium visuel), puis le reste de la liste ; ma position toujours visible même hors du haut de liste.
- Sélecteur de mois pour l'historique des mois précédents (instantanés figés). Ex-aequo : même rang.
- Les personnes qui ont choisi de ne pas apparaître dans les classements sont exclues.

### 8.8 Profil

- Mon profil : avatar, prénom nom, pseudo, badge de rôle, badge d'ancienneté (§9.4), âge si je l'ai choisi, grimpe depuis,
  année d'arrivée au club, voie préférée, bio, coups de cœur, chiffres clés (séances, voies topées, en tête, cotation max),
  **bibliothèque de badges** (tous les badges, obtenus en couleur avec « ×N » pour les répétables, non obtenus en grisé avec
  la condition d'obtention), lien vers mon carnet complet.
- Profil d'un autre membre : mêmes infos publiques ; le carnet et les séances seulement si nous sommes amis.
- Réglages : modifier mes infos, photo, mot de passe, thème (auto/clair/sombre), apparaître dans les classements,
  mes appareils, installer l'appli, notes de version, « Tes données » (export JSON, suppression du compte), déconnexion.
- Pied de page : version de l'appli (« GG Pocket 2.6.0 »), lien vers les notes de version.

### 8.9 Dernière chance

- Un responsable passe une voie en « dernière chance » avec une date prévue de démontage optionnelle.
- À l'ouverture de l'appli, tout grimpeur connecté qui n'a pas encore pris connaissance d'une ou plusieurs voies en dernière chance
  voit une pop-up qui les liste (étiquette, date prévue, nombre de coups de cœur) avec le bouton **« J'accepte leurs destins »**.
  Le clic enregistre la prise de connaissance pour ces voies ; la pop-up ne revient que si de nouvelles voies passent en dernière chance.
- Les voies en dernière chance restent grimpables et visibles partout avec une marque dédiée.

### 8.10 Espace Gestion (Responsable et +)

- **Voies** : créer, modifier, dupliquer une voie (pratique quand on rouvre sur la même corde), photo, dernière chance, démonter,
  annuler un démontage. Démonter une voie qui a des coups de cœur affiche un avertissement explicite
  (« 4 grimpeurs l'ont en coup de cœur. Tu es sûr·e ? »).
- **Fréquentation** : pour un mois donné, nombre d'ascensions par voie (toutes tentatives, réussies ou non), nombre de grimpeurs
  différents, taux de réussite ; tri et export CSV.
- **Bilan du topo** : nombre total de voies actives, répartition par cotation (histogramme), par secteur (pan), par corde
  (grille des 29 cordes avec les voies dessus), voies les plus et les moins grimpées, ancienneté des voies (pour savoir quoi démonter),
  classement par coups de cœur.
- **Dernière chance** : vue de toutes les voies concernées, qui en a pris connaissance (nombre).

### 8.11 Espace Gestion (Direction et Admin)

- **Membres** : tableau recherchable et triable (nom, pseudo, rôle, statut, inscription, dernière activité, nombre de séances),
  fiche membre, changement de rôle (selon la matrice §5), désactivation, lien de réinitialisation du mot de passe.
- **Modération** : derniers commentaires, suppression.
- **Journal d'audit** (lecture).
- **Admin uniquement** : codes d'inscription ; configuration du club (nom, **logo fond clair et logo fond sombre** avec aperçu
  sur les deux fonds, secteurs, cordes, couleurs de prises) ; suppression définitive de compte.
- Le logo téléversé apparaît dans : le header, l'écran de connexion/inscription, l'en-tête du fil d'actu, la pop-up d'installation,
  l'écran de notes de version et, si faisable proprement, les icônes PWA (génération des tailles côté serveur et manifeste servi
  dynamiquement ; sinon, icônes générées au build depuis `docs/references/GG_logo.jpeg`, à noter dans DECISIONS).

### 8.12 Notes de version

- `CHANGELOG.md` (format Keep a Changelog, rédigé en français pour les membres, ton du club, sans jargon technique) est la seule source.
  Il est embarqué au build et affiché dans l'écran « Quoi de neuf ? ».
- Après une mise à jour, au premier lancement, une fenêtre présente les nouveautés de la version (une seule fois par version et par utilisateur).
- Mise à jour du service worker : bandeau « Une nouvelle version est arrivée. Recharger ? » ; jamais de rechargement surprise pendant une saisie.

---

## 9. Règles métier exactes

Toutes ces règles vivent dans des services backend purs, testés unitairement cas par cas. Le front n'en recalcule aucune.

### 9.1 Définitions

- Un **jour de grimpe** = une date (Europe/Paris) où l'utilisateur a au moins une séance. Deux séances le même jour = un seul jour de grimpe
  pour les compteurs hebdomadaires, mensuels et le classement des séances (évite de gonfler les chiffres en coupant une séance en deux).
- **Semaine** = semaine ISO (lundi → dimanche). **Mois** = mois calendaire.
- Une ascension **topée** compte comme une « voie topée ». Les compteurs cumulés (badges paliers) comptent les ascensions,
  pas les voies distinctes (sinon « 1000 voies topées » serait impossible dans une salle de ~60 voies).
- Démonter une voie ne modifie aucune ascension, aucun compteur, aucun classement, aucun badge.

### 9.2 Classements mensuels

- **Le plus de séances** : nombre de jours de grimpe dans le mois.
- **Le plus de voies** : nombre d'ascensions topées dans le mois (tous modes confondus, répétitions comprises).
- Les deux classements sont calculés en direct pour le mois en cours. Ex-aequo : même rang, puis ordre alphabétique d'affichage.
- Une séance peut être datée jusqu'à 7 jours dans le passé. Le mois précédent est donc affiché « provisoire » jusqu'au 8 du mois ;
  le 8 à 04:00 (Europe/Paris), une tâche planifiée fige le mois précédent dans `MonthlyRankingSnapshot` (tâche idempotente et rejouable).
- Une fois le mois figé, ses séances ne sont plus modifiables ni supprimables, et on ne peut plus créer de séance datée dans ce mois.

### 9.3 Modification des séances

Le propriétaire modifie ou supprime sa séance tant que son mois n'est pas figé. Toute création, modification ou suppression
relance le recalcul des « je sais la faire » automatiques et des badges de l'utilisateur (voir 9.5).

### 9.4 Badge d'ancienneté

Années au club = année courante − année d'arrivée. Affiché sur le profil et dans l'annuaire.

| Années | Badge |
|---|---|
| 0 | « Tout frais » (proposition, à valider) |
| 1 | Le bébé mousqueton |
| 2 à 4 | Le débutant professionnel |
| 5 à 9 | Le forceur |
| 10 à 14 | Le résident de la salle |
| 15 et + | Le GG fossile (il fait partie du mur) |

### 9.5 Catalogue des badges (trophées)

Le moteur de badges est **déterministe et recalculable** : à partir de l'historique complet d'un utilisateur, une fonction pure
produit l'ensemble des badges et leur nombre d'obtentions. Après chaque modification de séance, on recalcule et on enregistre la
différence (un badge dont la condition n'est plus remplie après une modification ou suppression est retiré). Chaque badge a un code
stable, un nom, une description d'obtention, une icône (dessinée pour l'appli, cohérente, pas d'emoji) et un indicateur « répétable ».
Les badges répétables affichent « ×N ». Écris un test par badge, avec les cas limites (seuil exact, seuil −1, fuseau horaire, fin de mois,
semaine à cheval sur deux mois).

Badges à paliers (obtenus une fois chacun) :

| Famille | Condition | Paliers |
|---|---|---|
| Voies topées | Nombre cumulé d'ascensions topées | 1, 5, 10, 20, 50, 100, 250, 500, 750, 1000 |
| Voies en tête | Nombre cumulé d'ascensions topées en tête | 1, 5, 10, 20, 50, 100, 250, 500, 750, 1000 |
| Première cotation | Première ascension topée (tout mode) d'une voie de cotation ≥ X | 5b, 5c, 6a, 6b, 6c, 7a, 7b, 7c |
| Séances réalisées | Nombre cumulé de jours de grimpe | 10, 25, 50, 75, 100, 500, 750, 1000 |

« Première cotation » : « 6a » est obtenu par une voie en 6a ou 6a+ ou plus dure ; topper un 7a d'emblée débloque d'un coup tous
les paliers inférieurs (c'est voulu et drôle).

Badges répétables :

| Code (indicatif) | Nom | Condition | Compté |
|---|---|---|---|
| `WEEK_2` / `WEEK_3` / `WEEK_4` | 2 / 3 / 4 séances dans la semaine | Semaine ISO avec ≥ 2 / ≥ 3 / ≥ 4 jours de grimpe | Une fois par semaine qualifiante (une semaine à 4 donne aussi 2 et 3) |
| `MONTH_COMPLETE` | Mois complet | Mois avec ≥ 10 jours de grimpe | Une fois par mois |
| `MONTH_12` | 12 séances dans le mois | Mois avec ≥ 12 jours de grimpe | Une fois par mois |
| `STREAK_3W` | 3 semaines d'affilée | 3 semaines ISO consécutives avec ≥ 2 jours de grimpe chacune | Une fois par bloc de 3 semaines, blocs sans chevauchement |
| `MONTH_NO_MISS` | Un mois sans rater une semaine | Mois dont chaque semaine ISO commençant dans ce mois a ≥ 1 jour de grimpe | Une fois par mois |
| `FULL_LEAD` | Séance full tête | Séance d'au moins 3 ascensions, toutes en tête | Une fois par séance |
| `PERFECT` | Séance parfaite | Séance d'au moins 3 ascensions, toutes enchaînées | Une fois par séance |
| `ZERO_REST` | Séance zéro repos | Séance avec ≥ 10 ascensions topées | Une fois par séance |
| `COMEBACK` | Le retour en force | Séance après ≥ 30 jours sans grimper (hors toute première séance) | Une fois par retour |
| `FLASH` | Le flash (nom à valider, §19) | Ascension enchaînée, marquée premier essai, sur une voie ouverte depuis ≤ 14 jours | Une fois par ascension |
| `RECORD` | La séance record | Séance dont le nombre d'ascensions topées dépasse strictement le précédent record, à partir de la 4e séance | Une fois par séance |

Les badges obtenus lors d'une séance sont rattachés à cette séance pour apparaître dans sa carte du fil.

---

## 10. Identité visuelle et design system

Avant d'écrire le moindre écran, crée `docs/DESIGN.md` en suivant cette démarche, puis implémente les tokens et les composants de base.

1. **Ancrage dans le sujet.** Le monde de la salle : prises en résine de toutes les couleurs, magnésie, cordes, numéros de corde peints,
   petites étiquettes de voie scotchées au pied du mur (nom + cotation écrits à la main, cf. photo Oblyk), le logo du club
   (silhouette orange, typo pinceau noire, sommet olive). C'est là que doit naître la personnalité, pas dans un kit générique.
2. **Couleurs imposées par le club** : blanc, orange, vert (+ noir du logo). Le mode sombre est un citoyen de première classe
   (le club utilise souvent le logo sur fond noir). Contraintes : l'orange du logo sur fond blanc n'atteint pas le contraste AA pour
   du petit texte ; prévois une déclinaison plus sombre pour le texte et garde l'orange vif pour les surfaces et grands éléments.
   Le vert olive du logo est peu lisible en petit : définis un vert de « réussite/présence » lisible dans les deux thèmes, apparenté au logo.
   Les couleurs de prises (HoldColor) sont un second système chromatique, réservé à l'identification des voies.
3. **Élément signature** : l'**étiquette de voie** (composant réutilisé partout : topo, saisie, fil, À faire, gestion). Elle évoque
   l'étiquette physique au pied du mur : pastille(s) de couleur des prises, numéro de corde bien lisible, nom, cotation. C'est l'endroit
   où tu dépenses ton audace visuelle ; le reste de l'interface reste calme et discipliné.
4. **Typographie** : une ou deux familles maximum, choisies pour ce projet (pas les polices par défaut que tu prendrais n'importe où),
   bonne lisibilité des chiffres (cotations, compteurs), accents français complets, auto-hébergées. Échelle typographique explicite.
5. **À éviter** (traits de design généré) : tout découper en cartes blanches arrondies identiques avec la même ombre grise (ce que fait
   la maquette), dégradés décoratifs, petites étiquettes en capitales espacées au-dessus de chaque titre, monospace pour les petites données,
   flèches « → » ajoutées aux boutons, animations d'entrée sur chaque bloc.
6. **Mouvement** : un seul moment orchestré et mémorable — l'enregistrement d'une séance et le déblocage de badges. Le reste :
   transitions sobres qui expliquent un changement d'état. `prefers-reduced-motion` respecté.
7. **Démarche** : écris le plan (palette en 4 à 6 hex nommés par thème, polices et rôles, principes de mise en page avec wireframes ASCII
   des écrans clés : Actu, Nouvelle séance, Topo, Fiche voie), relis-le en te demandant s'il ressemble à ce que tu produirais pour n'importe
   quelle appli sportive ; si oui, révise et note pourquoi. Puis construis, capture, critique, corrige.

Les maquettes de la famille donnent la structure et le contenu attendus, pas le style : fais mieux qu'elles.

---

## 11. Ton et écriture

- Tutoiement partout. Phrases courtes, verbes simples, casse de phrase (pas de Majuscules Partout).
- Humour de vestiaire d'escalade quand c'est possible : états vides, confirmations, chargements, erreurs, pop-ups, badges, libellés de rôle.
  Vocabulaire disponible : mouli, tête, vacher, zipper, avaler la corde, « Sec ! », « Du mou ! », « Bloque ! », relais, dégaine, magnésie,
  bac, réglette, pied-main, dévers, « ça force », « ça passe », « bras de T-Rex », « avant-bras en béton ».
- Jamais au détriment de la clarté : un bouton dit exactement ce qu'il fait (« Enregistrer la séance », pas « Envoyer du lourd ») ;
  une erreur dit ce qui s'est passé et quoi faire, l'humour vient en plus.
- Jamais moqueur envers un grimpeur sur son niveau, son âge ou son corps. Le club accueille des enfants et des débutants.
- Exemples de registre attendu :
  - Fil vide : « Le mur est calme… trop calme. Ajoute des amis ou enregistre ta première grimpe. »
  - Erreur réseau : « Ça a zippé. Pas de réseau pour l'instant ; ta séance est gardée au chaud et partira toute seule. »
  - Mauvais identifiants : « Ce pseudo et ce mot de passe ne s'assurent pas mutuellement. Réessaie. »
  - Déconnexion : « Redescendre » ; confirmation : « Tu redescends ? Ta session sera fermée sur cet appareil. »
  - Suppression : « Supprimer cette séance ? Elle disparaîtra de ton carnet et de ton fil. »
- Tous les textes sont dans `frontend/src/i18n/fr.json` (structuré par écran), relisibles d'un bloc par la famille.

---

## 12. PWA

- Manifeste complet : nom « GG Pocket », nom court, couleurs de thème clair/sombre, `display: standalone`, icônes y compris *maskable*,
  captures d'écran pour l'invite d'installation Android.
- Service worker (Workbox via vite-plugin-pwa, stratégie `prompt`) : précache de l'app shell ; lecture du topo, du profil et du planning
  en *network-first* avec repli sur le cache (consultable hors ligne) ; aucune mise en cache des réponses d'authentification ni des fichiers
  privés d'autres utilisateurs ; page hors ligne drôle.
- File d'attente hors ligne pour l'envoi des séances (§8.3), y compris la photo.
- **Invitation à installer** : après la première connexion (et après l'onboarding), une fenêtre explique que l'expérience est meilleure
  installée.
  - Android / Chrome / Edge : bouton « Installer » qui déclenche l'invite native.
  - iOS : tutoriel illustré pas à pas pour ajouter à l'écran d'accueil. Vérifie sur le web la procédure exacte des versions actuelles
    d'iOS/Safari (elle a changé récemment) et ce qu'il en est des autres navigateurs sur iOS ; garde les étapes dans l'i18n pour les ajuster facilement.
  - Navigateur intégré d'une appli (WhatsApp, Messenger, Instagram… les liens d'inscription circuleront par là) : détecter et expliquer
    comment ouvrir la page dans le vrai navigateur.
  - Déjà installée (mode standalone) : ne jamais afficher. « Plus tard » : réafficher après 7 jours, 3 fois maximum. Toujours accessible depuis le profil.
- Notifications push : hors périmètre v1 (§18, phase 9). Sur iOS elles n'existent que pour une PWA installée : c'est un argument de plus pour l'installation.

---

## 13. Qualité technique

- Backend : validations explicites sur chaque DTO, transactions au niveau service, index sur toutes les colonnes filtrées/triées,
  aucune requête N+1 sur les listes (fil, topo, podium) — vérifie-le dans les tests d'intégration en comptant les requêtes ou en lisant les plans.
  Pagination par curseur pour le fil. Logs structurés (JSON en prod), sans données personnelles ni mots de passe.
- Frontend : composants petits et nommés par leur rôle métier, logique dans des composables testables, pas de `any`,
  gestion systématique des états chargement / vide / erreur / hors ligne, squelettes plutôt que spinners plein écran.
- API : versionnée sous `/api/v1`, documentée (OpenAPI), codes HTTP corrects, erreurs Problem Details avec `code` métier.
- Accessibilité : navigation clavier complète sur desktop, rôles et labels ARIA corrects, contraste AA dans les deux thèmes, taille de texte
  respectant les réglages système, cibles tactiles ≥ 44 px.
- Performance : score Lighthouse ≥ 90 en performance, accessibilité, bonnes pratiques (mobile) sur les écrans principaux, vérifié en fin de phase 8.

---

## 14. Sécurité, vie privée et RGPD

- Toute l'appli est derrière connexion (hors accueil, inscription, connexion, réinitialisation, page « Tes données »). `noindex` partout.
- En-têtes de sécurité via Caddy : CSP stricte (pas d'inline script), HSTS, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`.
- Visibilité : profil public = visible des membres connectés ; carnet, séances et photos de séance = amis seulement (ou « juste moi »).
  Âge masqué par défaut. Aucune donnée n'est visible hors de l'appli.
- Mineurs : le club accueille probablement des enfants. Pour les moins de 15 ans, accord parental déclaré à l'inscription (§6).
  Pas de mise en avant particulière des mineurs, pas d'exposition hors membres.
- Photos : EXIF supprimés (dont la géolocalisation), ré-encodage, tailles limitées (10 Mo à l'envoi, redimensionnement serveur à 1600 px
  + vignette), fichiers servis uniquement après contrôle d'accès.
- Page « Tes données » (texte clair, sans jargon) : données collectées, pourquoi, qui les voit, durée de conservation, responsable (le club),
  comment exporter et supprimer. Export JSON de toutes mes données. Suppression de compte en libre-service (avec confirmation) :
  données personnelles et photos effacées ; séances et ascensions rattachées à un utilisateur anonyme « Ancien·ne membre » pour ne pas
  fausser les statistiques des voies ; idem dans les classements figés.
- Dépendances surveillées (Dependabot ou Renovate), scan de secrets (gitleaks) et de vulnérabilités d'images en CI.

---

## 15. Git, versions, CI/CD et environnements

### 15.1 Branches et versions

- `main` = la prod, ou ce qui part à la prochaine release. Protégée : PR obligatoire, CI verte obligatoire, pas de force-push.
- Une fonctionnalité : `feature/<slug>-<version-prod-de-base>` créée depuis `main` (ex. `feature/ecran-chat-2.5.3` quand la prod est en 2.5.3).
  Un correctif : `fix/<slug>-<version>`. Technique sans impact utilisateur : `chore/<slug>`. Le suffixe de version est informatif
  (git connaît la vraie base) ; si `main` avance pendant le développement, on rebase et on garde le nom.
- `prod/X.Y.Z` : une branche par version réellement déployée en prod, créée **uniquement** par le workflow de release, protégée en écriture.
  C'est le « dossier prod » où l'on retrouve chaque version jamais déployée ; elle sert aussi de base à un correctif d'urgence d'une ancienne version.
  Chaque release crée aussi le tag `vX.Y.Z` et une GitHub Release.
- SemVer strict : majeure = changement qui casse des usages ou refonte, mineure = nouvelle fonctionnalité, correctif = patch
  (depuis 2.5.3 : 2.5.4, 2.6.0 ou 3.0.0 ; jamais 2.6.4). Phase de construction en `0.x.y` ; première mise en service du club = `1.0.0`.
- Une seule source de version (fichier `VERSION` à la racine), propagée au build dans le backend (`/api/v1/meta` : version, environnement,
  commit) et le frontend (pied de page, notes de version).

### 15.2 Cycle d'une fonctionnalité

1. Branche `feature/...` depuis `main`.
2. Chaque push : `ci.yml`. Ouverture de la PR (gabarit : quoi, pourquoi, captures clair/sombre, comment tester, entrée CHANGELOG).
3. Déploiement sur l'environnement dev : workflow `deploy-dev.yml`, déclenché manuellement (choix de la branche) ou en posant le label
   `deploy:dev` sur la PR. Option « réinitialiser la base et rejouer le seed » (cochée par défaut).
4. Tests humains sur dev (Willem + 2 ou 3 testeurs).
5. Merge de la PR dans `main` (squash).
6. Release : workflow `prepare-release.yml` (déclenchement manuel, entrée : `patch` / `minor` / `major`) qui ouvre une PR « Release X.Y.Z »
   mettant à jour `VERSION` et déplaçant la section « Non publié » du CHANGELOG sous `X.Y.Z — date`. Refus si « Non publié » est vide.
7. Merge de la PR de release → `release.yml` : tag `vX.Y.Z`, branche `prod/X.Y.Z`, GitHub Release (notes = section du CHANGELOG),
   images `ghcr.io/<owner>/gg-pocket-api:X.Y.Z` et `gg-pocket-web:X.Y.Z`, puis job de déploiement prod protégé par l'environnement
   GitHub `production` (approbation manuelle requise de Willem).
8. Déploiement prod : sauvegarde de la base, `pull`, `up -d`, attente du healthcheck, test de fumée (`/api/v1/meta` renvoie la bonne version),
   rollback automatique vers la version précédente en cas d'échec, résumé dans le job.

Le passage par une PR de release respecte la protection de `main` (pas de commit direct du robot) et sert de dernier point de contrôle.

### 15.3 CI (`ci.yml`)

Sur chaque push et PR : backend (compilation, tests unitaires et d'intégration Testcontainers, ArchUnit), frontend (lint, format, typecheck,
Vitest, build), cohérence des types OpenAPI générés, Playwright e2e contre la stack Docker démarrée dans le job, build des images,
gitleaks, scan de vulnérabilités des images (échec sur critique). Caches Maven et pnpm. Temps cible < 12 minutes.

### 15.4 Environnements

| | Local | Dev | Prod |
|---|---|---|---|
| Accès | localhost | 2-3 personnes | membres du club |
| Données | seed | seed (réinitialisable) | réelles, vide au départ |
| Inscription | ouverte | désactivée : comptes de test et testeurs uniquement | par code du club |
| Particularités | hot reload | bandeau « DEV », `noindex`, secrets propres | sauvegardes, approbation manuelle |

- Dev et prod sont deux projets Compose séparés sur le même serveur par défaut (réseaux, volumes, bases, secrets distincts).
  Le dev ne peut atteindre ni la base ni les fichiers de prod.
- Protection du dev : appli entièrement fermée aux inconnus (pas d'inscription, comptes nominatifs) ; protection supplémentaire au niveau
  du proxy en option, désactivée par défaut (authentification basique : elle se comporte mal avec une PWA installée sur iOS).
  Pas de VPN ni de Tailscale. Documente les deux niveaux dans DEPLOYMENT.md. Les comptes de test du dev déployé ont des mots de passe
  forts fournis par variables d'environnement, et la connexion y est limitée en débit comme en prod.
- Migrations Flyway en mode « expand / contract » : la version N−1 de l'appli doit fonctionner sur le schéma N, pour que le rollback
  applicatif reste possible sans restaurer la base.

---

## 16. Docker et exploitation

- Images multi-étapes, utilisateur non root, tags immuables (version ou `dev-<sha>`), pas de `latest` en prod.
- Services : `caddy` (TLS automatique, sert le front statique, proxifie `/api`), `api`, `db` (PostgreSQL, volume nommé), `backup`.
  Si le serveur a déjà un reverse proxy (voir §20), adapte-toi plutôt que d'en ajouter un second sur les ports 80/443.
- Limites mémoire par conteneur et JVM réglée pour les conteneurs (`MaxRAMPercentage`). Budget cible : dev + prod tiennent dans 3 Go.
- Healthchecks Docker sur chaque service ; `restart: unless-stopped`.
- Premier démarrage de la prod (base vide) : le serveur crée un code d'inscription **admin** à usage unique valable 24 h et l'affiche
  une seule fois dans les logs. Aucun mot de passe par défaut, nulle part.
- Sauvegardes : `pg_dump` quotidien + archive des fichiers téléversés, rétention 14 jours quotidiens + 8 hebdomadaires, sauvegarde
  supplémentaire avant chaque déploiement prod. `restore.sh` documenté **et testé** en CI ou en local sur une base jetable.
- `DEPLOYMENT.md` explique pas à pas : préparer le serveur, utilisateur `deploy` dédié (droits limités à Docker Compose du projet),
  secrets GitHub requis, DNS, premier déploiement dev, première release, rollback manuel, restauration d'une sauvegarde.

---

## 17. Seed de démonstration (dev et local uniquement)

Garde-fous cumulés : le seeder n'existe que dans le profil Spring `dev` ; il exige `GG_ALLOW_DEMO_SEED=true` ; il refuse de s'exécuter si
la base porte le marqueur d'environnement `prod` ou contient des comptes non issus du seed. Il est idempotent et sait tout réinitialiser.

Contenu, assez riche pour juger l'appli « en vrai » :

- Configuration du club réelle (secteurs, 29 cordes, couleurs, cotations), logo de `docs/references/`.
- Comptes de test : un par rôle (`admin`, `direction`, `ouvreur`, `grimpeur`), mots de passe simples en local, fournis par variables
  d'environnement en dev déployé ; documentés dans le README (section dev).
- ~45 grimpeurs fictifs aux noms crédibles (aucune personne réelle), âges et anciennetés variés, quelques mineurs avec accord parental,
  un compte désactivé, un compte masqué des classements, des avatars générés (formes et initiales, pas de visages).
- ~60 voies réparties sur les 29 cordes, toutes les cotations de 4a à 7c avec une distribution réaliste (beaucoup de 5 et de 6),
  noms drôles dans l'esprit du club, styles, conseils d'ouvreur, photos de démonstration générées (motifs de mur et de prises, pas de photos réelles) ;
  5 voies démontées avec historique, 3 voies en dernière chance.
- 4 mois d'historique de séances aux profils variés (assidus, irréguliers, retour après absence, progression de cotation), amitiés,
  demandes en attente, réactions, commentaires, coups de cœur dont des cœurs pleins.
- Badges et classements figés produits **par le vrai moteur et la vraie tâche planifiée**, jamais insérés à la main.
- Cas limites présents exprès : ex-aequo au podium, semaine à cheval sur deux mois, séance à 10+ voies, séance parfaite.

---

## 18. Roadmap

Chaque phase = une ou plusieurs branches et PR. Ne commence pas une phase tant que la précédente n'est pas verte et documentée.
À la fin de chaque phase : revue critique (lance un sous-agent de revue sécurité/autorisations et un de revue UX sur les captures si possible),
corrections, mise à jour des docs, PR.

**Phase 0 — Fondations.** Structure du dépôt, docs (ROADMAP, PROGRESS, DECISIONS, ARCHITECTURE, CONTRIBUTING, README), squelette backend
(health, `/api/v1/meta`, Problem Details, Flyway, Testcontainers), squelette frontend (routing, i18n, TanStack Query, client API généré),
**DESIGN.md + tokens + composants de base + thèmes clair/sombre + étiquette de voie**, app shell avec les 5 onglets et écrans vides drôles,
`compose.local.yml`, `ci.yml`. Les commandes sont reportées dans CLAUDE.md.
*Critère :* `git clone` puis démarrage local documenté en moins de 5 minutes ; CI verte ; captures de l'app shell en clair et en sombre.

**Phase 1 — Comptes et rôles.** Inscription par code, connexion, session longue, mes appareils, onboarding, invitation à installer (§12),
profil de base, gestion des membres et des rôles, codes d'inscription, lien de réinitialisation, journal d'audit, code admin de premier démarrage,
page « Tes données » (contenu de base), seed des comptes.
*Critère :* tests d'autorisation pour chaque rôle sur chaque endpoint ; parcours e2e inscription → installation → retour 30 jours plus tard sans reconnexion (horloge simulée).

**Phase 2 — Environnements et pipeline.** Compose dev/prod, Caddy, `deploy-dev.yml`, `prepare-release.yml`, `release.yml`, sauvegardes
et restauration, DEPLOYMENT.md. Prépare tout, liste précisément ce qu'il te faut (§20), demande-le, puis fais le premier déploiement dev
en `0.x` avec Willem. *Critère :* une branche déployée sur dev en un clic ; une release de test `0.x` crée tag, branche `prod/0.x.y`,
GitHub Release, et se déploie après approbation ; un rollback testé.

**Phase 3 — Topo.** Configuration du club (secteurs, cordes, couleurs), gestion des voies, liste/tri/filtres, fiche voie, commentaires,
coups de cœur (limite de 2 et remplacement), démontage et archives, dernière chance et pop-up « J'accepte leurs destins ».

**Phase 4 — Séances.** Nouvelle séance complète avec brouillon et file d'attente hors ligne, photo, modification/suppression,
Planning, À faire (automatique + manuel), carnet personnel.

**Phase 5 — Social.** Annuaire, amis, fil d'actu, réactions, commentaires, modération.

**Phase 6 — Podium et statistiques.** Classements en direct, tâche de clôture mensuelle, historique, écrans Fréquentation et Bilan du topo, export CSV.

**Phase 7 — Badges.** Moteur recalculable, catalogue complet, icônes, bibliothèque de badges, badges dans le fil, ancienneté, moment de déblocage.

**Phase 8 — Finitions et lancement.** Logo téléversable et ses emplacements, icônes PWA, « Quoi de neuf ? », audits accessibilité et Lighthouse,
revue de sécurité complète, seed final, vérification qu'une prod vide démarre proprement. Release `1.0.0` déclenchée par Willem.

**Phase 9 — Après lancement (ne pas commencer sans feu vert).** Passkeys, notifications push (demandes d'amis, commentaires, dernière chance),
graphiques de progression personnelle, éventuel classement pondéré par la cotation, e-mail optionnel pour la réinitialisation.

---

## 19. Choix par défaut à faire valider par le club

Applique-les, recopie-les dans `DECISIONS.md` avec le statut « à valider », et ne bloque jamais dessus :

1. Barre d'onglets à 5 entrées, profil via l'avatar, gestion via une icône du header (la maquette en a 6).
2. Cotations avec les « + » de 4a à 7c, et badge « Première cotation » étendu à 7c.
3. Classement « voies » = ascensions topées du mois, répétitions comprises (alternative : voies distinctes).
4. Classement « séances » et badges de fréquence comptés en jours de grimpe (deux séances le même jour = une).
5. Badge « Mois complet » = 10 jours de grimpe dans le mois ; « 10 séances dans le mois » de la liste d'origine fusionné avec lui (doublon).
6. « Le flash » : le texte d'origine (« sans information préalable ») décrit en jargon l'**à vue** ; le flash, c'est réussir du premier
   coup *avec* des infos. Nom par défaut « Le flash », condition telle que §9.5 ; renommer en « L'à vue » si le club préfère être puriste.
7. Seuils inventés car non précisés : longue absence = 30 jours, voie « nouvelle » = 14 jours, séance « full tête » / « parfaite » = 3 voies minimum,
   record à partir de la 4e séance.
8. Badge d'ancienneté « Tout frais » pour la première année (le club n'a rien prévu sous 1 an).
9. Séances datables jusqu'à 7 jours dans le passé ; mois figé le 8 du mois suivant.
10. Coup de cœur sur une voie démontée : la place se libère.
11. Fil d'actu limité aux amis (pas de vue « tout le club ») et séances visibles des amis seulement.
12. Pas d'e-mail en v1 : réinitialisation du mot de passe par lien généré par la direction.
13. Couleur des prises et photo de voie ajoutées au modèle (absentes du cahier des charges, indispensables pour reconnaître une voie au mur).
14. Âge masqué par défaut ; accord parental déclaré sous 15 ans.

---

## 20. Ce que tu dois demander à Willem (ne devine pas)

- Hébergement : serveur cible pour dev et prod (VPS existant ?), accès SSH, système, RAM disponible, reverse proxy déjà en place ou non.
- Noms de domaine pour la prod et le dev, et qui gère le DNS.
- Dépôt GitHub (propriétaire, nom, public ou privé), droits pour créer les environnements, secrets et règles de protection.
- Liste des personnes qui auront accès au dev.
- Validation des choix du §19 quand il a la réponse du club (ne bloque pas en attendant).
