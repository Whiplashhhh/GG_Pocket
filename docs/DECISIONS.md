# Décisions

Une entrée par décision : contexte, choix, alternatives, statut.
Statuts : **validée** (tranchée, à ne pas rediscuter sans raison nouvelle) ou **à valider** (choix par défaut appliqué, en attente de l'avis
de Willem ou du club). Les entrées ne sont jamais supprimées ; une décision remplacée est marquée « remplacée par D-xxx ».

Sommaire : choix par défaut du club (D-001 à D-014), versions (D-020), technique (D-030 et suivantes),
choix de la phase 1 (D-043 et suivantes ; D-048 à D-058 tranchés pendant la phase), environnements et pipeline
(D-059 à D-071, phase 2), correctifs de la 0.1.1 (D-072, D-073), topo (D-074 à D-083, phase 3), séances (D-084 et suivantes, phase 4).

## Récapitulatif : tout ce qui reste à valider

Chaque ligne est appliquée par défaut ; rien n'attend une réponse pour avancer. Qui tranche : **club** (président,
direction) ou **Willem**.

| Décision | Question | Qui | Choix appliqué en attendant |
|---|---|---|---|
| D-059 | Nom de domaine du club ? | Willem | Adresses provisoires `sslip.io` tirées de l'IP du serveur |
| D-063 | Copie des sauvegardes hors du serveur ? | Willem | Aucune pour l'instant (sauvegardes sur le VPS seulement) |
| D-087 | En attendant les amis, Actu montre mes séances ? | Willem | Oui, « Mes séances » en haut du fil |
| D-094 | Le carnet : chiffres, pyramide par cotation, toutes mes voies ? | Willem | Oui, depuis le Profil ; celui des amis en phase 5 |
| D-108 | « Quoi de neuf ? » mémorisé par membre dans le navigateur (pas sur le serveur) | Willem | Oui : une fois par version, par membre et par appareil ; nouveaux comptes épargnés |
| D-112 | Jeton `gh` de l'agent (point 2 de la revue de sécurité) | Willem | À trancher (détails dans D-112) |

---

## Choix par défaut à faire valider par le club (spec §19)

### D-001 — Barre d'onglets à 5 entrées
- **Contexte** : la maquette de la famille a 6 onglets (Actu, Topo GG, À faire, Planning, Podium, Profil) ; 6 est trop serré à 360 px.
- **Choix** : 5 onglets (Actu, Topo, À faire, Planning, Podium). Le profil s'ouvre en touchant l'avatar du header, la gestion via une icône du header.
- **Alternatives** : 6 onglets ; menu « plus ».
- **Statut** : validée (club, questionnaire du 2026-11-07, Q01).

### D-002 — Cotations avec les « + » de 4a à 7c
- **Contexte** : le cahier des charges cite des cotations de 4a à 7c sans préciser les « + ».
- **Choix** : 23 cotations (4a, 4a+, … 7c), stockées avec un rang numérique. Badge « Première cotation » étendu jusqu'à 7c.
- **Révision (2026-10-23, Willem)** : le club utilise aussi les « - ». La liste devient 4a-, 4a, 4a+, … 7c-, 7c, 7c+
  (36 cotations, « - » avant la cotation simple, « + » après). Le badge « Première cotation » va jusqu'à 7c+.
  Faite dans V7, pas encore en prod.
- **Alternatives** : sans « + » (12 cotations).
- **Statut** : remplacée par D-115 (club, questionnaire du 2026-11-07, Q04).

### D-003 — Classement « voies » = ascensions topées du mois, répétitions comprises
- **Choix** : chaque ascension topée compte, même si c'est la même voie plusieurs fois.
- **Alternatives** : voies distinctes topées dans le mois.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q05).

### D-004 — Classement « séances » et badges de fréquence en jours de grimpe
- **Choix** : deux séances le même jour comptent pour un seul jour de grimpe (évite de gonfler les chiffres en coupant une séance en deux).
- **Alternatives** : compter les séances brutes.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q06).

### D-005 — « Mois complet » = 10 jours de grimpe
- **Choix** : badge « Mois complet » à 10 jours de grimpe dans le mois ; « 10 séances dans le mois » de la liste d'origine fusionné avec lui (doublon).
- **Statut** : remplacée par D-116 (club, questionnaire du 2026-11-07, Q11).

### D-006 — Nom du badge « Le flash »
- **Contexte** : le texte d'origine (« sans information préalable ») décrit l'**à vue** ; le flash, c'est réussir du premier coup *avec* des infos.
- **Choix** : nom « Le flash », condition du §9.5 (ascension enchaînée, premier essai, voie ouverte depuis ≤ 14 jours).
- **Alternatives** : renommer en « L'à vue » si le club préfère être puriste (changement d'un libellé i18n).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q12).

### D-007 — Seuils non précisés par le cahier des charges
- **Choix** : longue absence = 30 jours ; voie « nouvelle » = 14 jours ; séance « full tête » / « parfaite » = 3 voies minimum ;
  « séance record » à partir de la 4e séance.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q13), sauf « full tête » et « séance parfaite » : remplacé par D-116 (5 voies).

### D-008 — Badge d'ancienneté « Tout frais »
- **Contexte** : le club n'a rien prévu sous 1 an d'ancienneté.
- **Choix** : « Tout frais » pour l'année d'arrivée (0 an).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q14).

### D-009 — Séances datables 7 jours en arrière, mois figé le 8
- **Choix** : une séance peut être datée jusqu'à 7 jours dans le passé ; le mois précédent est figé le 8 à 04:00 (Europe/Paris).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q09).

### D-010 — Coup de cœur sur une voie démontée
- **Choix** : la place se libère (limite de 2 coups de cœur sur des voies non démontées) ; le coup de cœur reste visible dans l'historique du profil.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q18).

### D-011 — Fil d'actu limité aux amis
- **Choix** : pas de vue « tout le club » ; séances visibles des amis seulement (ou « juste moi »).
- **Statut** : remplacée par D-120 (club, questionnaire du 2026-11-07, Q21).

### D-012 — Pas d'e-mail en v1
- **Choix** : mot de passe oublié = lien de réinitialisation à usage unique (24 h) généré par la direction depuis la fiche du membre.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q31).

### D-013 — Couleur des prises et photo de voie ajoutées au modèle
- **Contexte** : absentes du cahier des charges, indispensables pour reconnaître une voie au mur.
- **Choix** : 1 ou 2 couleurs de prises par voie (liste configurable) et une photo optionnelle.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q17) ; affichage complété par D-122.

### D-014 — Âge masqué par défaut, accord parental sous 15 ans
- **Choix** : « afficher mon âge » désactivé par défaut ; case « Un parent ou responsable légal est d'accord » obligatoire sous 15 ans.
- **Statut** : remplacée par D-119 (club, questionnaire du 2026-11-07, Q28).

---

## Versions épinglées

### D-020 — Versions des briques (vérifiées le 2026-10-17)
- **Contexte** : spec §0.3. Vérification faite sur les releases GitHub officielles et le registre npm.
- **Choix** :

| Brique | Version | Remarque |
|---|---|---|
| Java (Temurin) | 25 LTS (25.0.4) | |
| Maven (wrapper) | 3.9.16 | 3.10.0 publiée le 2026-10-01 : on attend qu'elle mûrisse, la 3.9 reste maintenue |
| Spring Boot | 4.1.1 | Gère Spring Framework 7.0, Spring Security 7.1, Hibernate 7.4, Flyway 12.4, Testcontainers 2.0, Jackson 3.1 |
| springdoc-openapi | 3.1.1 | Branche 3.x = Spring Boot 4 |
| Spotless / google-java-format | 3.10.4 / 1.36.1 | gjf 1.37 a changé une API (`Style` devenu un record) que Spotless 3.10.4 utilise : on reste sur 1.36.1 tant que Spotless n'a pas suivi |
| ArchUnit | 1.5.1 | Artefact `archunit-junit6` (Boot 4.1 embarque JUnit Jupiter 6) |
| PostgreSQL | 18 (image `postgres:18-alpine`) | Dernière majeure stable |
| Node.js | 26 LTS (26.13, depuis le 2026-11-01) | Node 24 jusqu'au 2026-10-31 ; 26.13.0 « Lithium » est la première 26 LTS (2026-10-28). `@types/node` suit la majeure (26.6). Node 25+ n'embarque plus corepack : l'image web installe pnpm avec npm, à la version de `packageManager` |
| pnpm | 12.14.0 | |
| Vue | 3.5.44 | |
| Vite | 8.3 | |
| TypeScript | 6.0.3 | La 7.0 (compilateur natif) n'est pas encore supportée par typescript-eslint (`<6.1`) ni par openapi-typescript |
| Vue Router | 5.4 | |
| Pinia | 4.1 | |
| TanStack Query (Vue) | 5.104 | |
| Tailwind CSS | 4.3 | Plugin `@tailwindcss/vite` |
| vite-plugin-pwa | 2.0 | Installé en phase 1 (invitation à installer) |
| vue-i18n | 11.4 | |
| Reka UI | 2.11 | |
| VueUse | 15.0 | |
| openapi-typescript / openapi-fetch | 7.13 / 0.17 | |
| Vitest | 5.0 | |
| Playwright | 1.64 | |
| ESLint | 10 (flat config) | avec typescript-eslint 8.71, eslint-plugin-vue 10, eslint-plugin-vuejs-accessibility |
| Caddy | 2.11.7 | Phase 2 |
| gitleaks / Trivy | 8.30 / 0.76 | |

- **Statut** : validée. Les montées de version passent par Dependabot (phase 2) ou une PR `chore/`.

---

## Décisions techniques

### D-030 — Environnement de développement de Claude Code sans Java ni Docker
- **Contexte** : la machine où travaille Claude Code n'a ni JDK, ni Docker, et son réseau sortant ne joint que GitHub, npm et PyPI
  (Maven Central inaccessible).
- **Choix** : le frontend est construit, testé et capturé en local. Le backend est écrit en local, compilé et testé par la CI GitHub Actions
  (Testcontainers inclus). Les détails d'API de Spring Boot 4.1 ont été vérifiés dans le code source sur GitHub.
  Les captures d'écran locales utilisent un Chromium autonome via `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH` (option documentée dans le README).
- **Conséquence** : chaque modification backend est validée par un aller-retour CI ; c'est plus lent, pas moins sûr.
- **Statut** : validée (contrainte de l'environnement, sans effet sur le produit).

### D-031 — Nom de package Java : `fr.gravelinesgrimp.pocket`
- **Choix** : racine `fr.gravelinesgrimp.pocket`, un sous-package par domaine (`auth`, `member`, `topo`, `session`, `social`, `ranking`, `badge`,
  `admin`, `club`) avec `api` / `application` / `domain` / `infra`, plus `platform` pour le transverse technique (erreurs, sécurité, horloge,
  `/api/v1/meta`, garde-fou d'environnement).
- **Alternatives** : `club.gg.pocket` ; un package `common`. `platform` dit mieux que c'est de l'outillage et pas un fourre-tout métier.
- **Statut** : validée.

### D-032 — Source unique de version : fichier `VERSION`
- **Choix** : `VERSION` à la racine (actuellement `0.0.0` ; la première release de construction sera `0.1.0`).
  Backend : le fichier est copié dans le jar au build (`META-INF/gg-pocket/VERSION`) et exposé par `/api/v1/meta`. Commit, branche et
  environnement arrivent par variables d'environnement (`GG_GIT_COMMIT`, `GG_GIT_BRANCH`, `GG_ENVIRONMENT`), posées par les images Docker
  (phase 2). Frontend : `vite.config.ts` lit `VERSION` et l'injecte au build. La version Maven du projet reste figée à `0-SNAPSHOT`
  (purement technique, jamais affichée).
- **Alternatives** : `${revision}` Maven passé par la CI (deux sources à synchroniser) ; plugin git-commit-id (dépendance de plus).
- **Statut** : validée.

### D-033 — Contrat OpenAPI commité, types générés vérifiés en CI
- **Choix** : le contrat `frontend/src/api/openapi.json` est commité. Un test d'intégration backend (`OpenApiContractIT`) compare le contrat
  produit par springdoc au fichier commité et écrit toujours le contrat réel dans `backend/target/openapi.json` ; la commande
  `./mvnw verify -Dgg.openapi.write=true` (ou l'artefact CI) le met à jour. Côté front, `pnpm api:types` génère `src/api/schema.d.ts`
  avec openapi-typescript ; la CI régénère et échoue si le résultat diffère du fichier commité.
- **Alternatives** : générer les types depuis un backend lancé dans la CI à chaque build (plus lent, plus fragile).
- **Statut** : validée.

### D-034 — Spring Security dès la phase 0, en « tout fermé sauf liste blanche »
- **Contexte** : l'authentification arrive en phase 1, mais une API ouverte par défaut est un piège.
- **Choix** : la chaîne de sécurité refuse tout, sauf `/api/v1/meta`, `/actuator/health` et `/v3/api-docs` (cette dernière désactivée en prod).
  Les 401 et 403 passent par le même gestionnaire d'erreurs Problem Details que le reste.
- **Statut** : validée.

### D-035 — Marqueur d'environnement en base
- **Contexte** : spec §17 (le seed refuse une base marquée `prod`) et §15.4 (le dev ne doit jamais toucher la base de prod).
- **Choix** : table `instance_marker` (une ligne) remplie au premier démarrage avec `GG_ENVIRONMENT` (`local`, `dev`, `prod`).
  Au démarrage suivant, si la valeur ne correspond pas à l'environnement configuré, l'application refuse de démarrer.
  Une appli dev pointée par erreur sur la base de prod s'arrête donc immédiatement.
- **Statut** : validée.

### D-036 — Erreurs : Problem Details + `code` métier stable
- **Choix** : toute erreur API est un `application/problem+json` (RFC 9457) avec une propriété `code` (énumération `ErrorCode`, en
  MAJUSCULES_SNAKE, jamais renommée) et, pour les erreurs de validation, `errors` (liste champ + code). Le front traduit `code` via
  `errors.<CODE>` dans `fr.json`, avec repli sur un message générique.
- **Statut** : validée.

### D-037 — Polices : Archivo (interface) et Kalam (nom des voies)
- **Contexte** : spec §10.4. Comparaison de 7 manuscrites (Patrick Hand, Permanent Marker, Kalam, Gochi Hand, Caveat Brush, Shadows Into Light,
  Nanum Pen) rendues sur de vrais noms de voies.
- **Choix** : Archivo variable (graisse + largeur, chiffres tabulaires) pour toute l'interface ; Kalam 700 uniquement pour le nom des voies.
  Raisons détaillées dans `DESIGN.md` §3.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q02).

### D-038 — Icônes : Lucide pour l'interface, icônes métier dessinées
- **Choix** : `lucide-vue-next` (import icône par icône, tree-shaking) pour les icônes génériques ; icônes métier (onglets, prise, badges)
  dessinées dans la même grille 24 px / trait 2 px.
- **Alternatives** : tout dessiner (trop long, incohérent à terme) ; un autre jeu (Phosphor, Tabler : équivalents, Lucide est le plus répandu).
- **Statut** : validée.

### D-039 — Logo : versions clair/sombre détourées depuis le JPEG
- **Contexte** : seul `GG_logo.jpeg` (447 px, fond blanc) est disponible ; le texte noir disparaît sur fond sombre.
- **Choix** : versions détourées générées une fois (fond transparent ; encre noire passée en blanc magnésie pour le sombre),
  dans `frontend/src/assets/brand/`, affichées en grand sur l'Actu. Dans le header, le logo complet (et même un recadrage sur
  « Gravelines GRIMP ») est illisible à 44 px de haut : on y affiche un mot-symbole composé (le sommet olive du logo dessiné
  en SVG + « Gravelines Grimp » en Archivo, « Grimp » en orange brique). En phase 8, l'admin pourra téléverser ses logos.
- **À demander au club** : une version vectorielle (SVG, PDF, AI) ou en haute définition du logo, et la version « fond noir » qu'il utilise déjà.
- **Statut** : remplacée par D-123 (club, questionnaire du 2026-11-07, partie H). La partie « header » était déjà remplacée par D-072.

### D-040 — Ce qui est reporté hors de la phase 0
- **Choix** :
  - Service worker et manifeste PWA → phase 1, avec l'invitation à installer.
  - Dockerfiles, images, Caddy, scan Trivy et e2e contre la stack Docker → phase 2.
  - En phase 0, l'e2e Playwright tourne contre le build du front servi par `vite preview`, avec l'API simulée.
- **Raison** : la phase 0 de la spec liste `compose.local.yml` et `ci.yml` ; la phase 2 porte explicitement Compose dev/prod et Caddy.
- **Statut** : validée.

### D-041 — Comportement d'iOS pour la session et l'installation (recherche du 2026-10-17)
- **Contexte** : spec §6 et §12, vérification demandée sur le web. Recherche faite par extraits de recherche web ; les pages sources n'ont pas
  pu être ouvertes intégralement depuis l'environnement de travail. À recontrôler sur un vrai iPhone en phase 1.
- **Constats** :
  - Depuis iOS 17.2, l'ajout à l'écran d'accueil **copie une fois les cookies** de Safari vers la web app (pas le localStorage ni IndexedDB) ;
    ensuite, les deux stockages sont séparés. Notre cookie de session posé par le serveur devrait donc suivre, mais des pertes ont été signalées
    sur certaines versions : on garde un écran de reconnexion indolore (trousseau, message clair) comme filet.
  - Limites ITP de Safari : les cookies écrits en JavaScript sont plafonnés à 7 jours (interdit chez nous) ; les cookies serveur sont plafonnés
    à 7 jours seulement en cas de CNAME vers un tiers ou de réponse venant d'une IP « tierce ». Front et API sur la même origine derrière
    un seul Caddy : non concernés.
  - Le stockage écrit par JavaScript (IndexedDB, localStorage, caches du service worker) est effacé après 7 jours d'utilisation de Safari sans
    interaction avec le site ; une web app installée a son propre compteur. Conséquence : les **brouillons de séance** et la file d'attente
    hors ligne sont un confort, pas une garantie ; on les vide dès l'envoi et on ne compte jamais sur eux pour garder des données longtemps.
  - Installation iOS 26 (Safari) : bouton Partager (selon la disposition des onglets, via « ⋯ » puis « Partager »), « Sur l'écran d'accueil »
    (parfois sous « Afficher plus »), option « Ouvrir en tant qu'app web » activée par défaut, « Ajouter ». Chrome, Edge et Firefox sur iOS
    peuvent aussi ajouter à l'écran d'accueil (via Partager). Les libellés exacts restent dans l'i18n pour être ajustés après test.
  - Navigateurs intégrés : depuis iOS 17, SFSafariViewController permet l'ajout à l'écran d'accueil ; les navigateurs maison (Instagram,
    Facebook/Messenger, TikTok, et WhatsApp d'après son user-agent sans `Safari/`) ne le permettent pas. Détection par user-agent (`FBAN`,
    `FBAV`, `FB_IAB`, `Instagram`, `BytedanceWebview`, `WhatsApp/`), message expliquant « ⋯ → Ouvrir dans le navigateur ».
  - Android : `beforeinstallprompt` sur Chrome, Edge et Samsung Internet (pas Firefox). Manifeste requis (nom, icônes 192 et 512, `start_url`,
    `display`) ; Firefox Android propose aussi l'installation si le manifeste est valide.
  - Web Push sur iOS : toujours réservé aux web apps installées (phase 9).
- **Contrôle sur appareil (2026-10-21)** : sur l'iPhone de Willem (iOS 27), installer la PWA depuis Safari en étant connecté
  ouvre l'appli installée sans redemander les identifiants : le cookie de session est bien copié.
- **Statut** : validée (contrôlée sur appareil).

### D-042 — Branche de la phase 0
- **Choix** : `feature/fondations-0.0.0` (la prod n'existe pas encore ; `0.0.0` est la version de base). Fusion dans `main` par PR squash.
- **Statut** : validée.

---

## Choix de la phase 1 (comptes et rôles)

Points non couverts par la spec, relevés avant la phase 1. Appliqués par défaut, en attente de réponse.

### D-043 — Responsable du traitement et contact RGPD
- **Contexte** : la page « Tes données » (spec §14) doit nommer le responsable du traitement et dire comment exercer ses droits.
- **Choix** : responsable = l'association, nommée « Gravelines Grimp » tant que le nom officiel (statuts) n'est pas fourni ;
  adresse de contact saisie par l'admin dans la configuration du club (pas en dur dans le code). Si le club ou sa fédération
  a déjà une politique de confidentialité, la page s'y alignera.
- **À fournir** : nom officiel de l'association, adresse de contact pour les demandes, politique existante éventuelle.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q24) ; complétée par D-119 (pas de politique de confidentialité existante).

### D-044 — Durée de conservation
- **Contexte** : la spec demande d'afficher une durée de conservation sans la fixer.
- **Choix** : données conservées tant que le compte existe ; un compte sans aucune connexion depuis 3 ans est supprimé
  (données personnelles et photos effacées, séances rattachées à « Ancien·ne membre », comme pour une suppression volontaire).
  Journal d'audit conservé 3 ans.
- **Alternatives** : 2 ans ; pas de suppression automatique (déconseillé : contraire au principe de limitation de la conservation).
- **Statut** : remplacée par D-119 (club, questionnaire du 2026-11-07, Q25).

### D-045 — Données visibles par la direction dans la fiche d'un membre
- **Contexte** : la spec liste les colonnes du tableau des membres, pas le contenu de la fiche.
- **Choix** : prénom, nom, pseudo, rôle, statut, inscription, dernière activité, nombre de séances, e-mail s'il est renseigné.
  **Date de naissance masquée** : la direction voit seulement « moins de 15 ans, accord parental déclaré » quand c'est le cas
  (minimisation, mineurs possibles).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q29) ; complétée par D-119 (mention « mineur »).

### D-046 — Format du pseudo
- **Contexte** : le pseudo est l'identifiant de connexion ; la spec dit seulement « unique, insensible à la casse ».
- **Choix** : 3 à 20 caractères parmi lettres sans accents, chiffres, `.`, `_`, `-` ; unicité insensible à la casse ; casse
  d'affichage conservée telle que saisie ; modifiable ensuite par le membre (l'ancien pseudo redevient libre).
- **Alternatives** : accepter les accents (risque de doublons visuels « Élodie » / « Elodie » et de saisie difficile sur certains claviers).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q30).

### D-047 — Code admin de premier démarrage
- **Contexte** : spec §16 : au premier démarrage de la prod, un code d'inscription admin à usage unique (24 h) s'affiche une fois dans les logs.
- **Choix** : Willem le récupère et le transmet au président, qui crée le premier compte admin. Willem peut garder son propre compte
  avec le rôle de son choix (créé ensuite par code d'inscription normal, rôle donné par l'admin).
- **Ce qui s'est passé** (2026-10-31) : Willem a utilisé le code lui-même, son compte est donc le seul admin de la prod
  (badge « Le Président·e »). Passation décidée dans D-107 : le président crée son compte, Willem le passe Admin,
  puis le président passe Willem Touriste (Grimpeur en attendant que le rôle existe). Personne ne modifie son propre
  rôle et le dernier admin ne peut pas être retiré : l'ordre compte.
- **Statut** : tranchée par l'usage ; l'admin est réservé au président (D-107).


### D-048 — Session longue : cookie « Rester connecté » et durées
- **Contexte** : spec §6 (180 jours glissants, cookie posé par le serveur, « Rester connecté » coché par défaut).
- **Choix** :
  - Session Spring Session JDBC ; à chaque connexion, une **nouvelle** session (l'ancienne est invalidée : pas de fixation).
  - « Rester connecté » coché : inactivité maximale **180 jours**, glissante (Spring Session repousse l'échéance à chaque requête).
    Le cookie `GG_SESSION` reçoit un `Max-Age` lointain (mécanisme `REMEMBER_ME_LOGIN_ATTR` de Spring Session ; les navigateurs
    plafonnent à 400 jours) et il est **renvoyé tous les 7 jours** par `CurrentMemberFilter`, pour que son échéance côté navigateur
    glisse aussi : un membre qui revient au moins une fois tous les six mois ne se reconnecte jamais.
  - Décoché : cookie de session navigateur (disparaît à la fermeture) et inactivité maximale **12 heures** côté serveur.
  - Cookie `__Host-GG_SESSION`, `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/` : le préfixe `__Host-` interdit à un sous-domaine
    voisin (le futur dev) de le poser. Le jeton CSRF n'est lu que dans l'en-tête `X-XSRF-TOKEN`, jamais dans un paramètre de
    formulaire : un sous-domaine peut déposer un cookie, pas envoyer cet en-tête (pas de CORS). À la déconnexion,
    `Clear-Site-Data: "cache"` efface les photos gardées par le navigateur. Les sessions des autres appareils sont fermées
    **après** la validation d'un changement de mot de passe. Déclaré explicitement (`SessionCookieConfig`) : les propriétés
    `server.servlet.session.cookie.*` ne s'appliquent à Spring Session qu'avec un serveur embarqué, les tests auraient eu un autre cookie.
  - La session ne garde que l'identifiant du membre (`MemberPrincipal`, aussi clé de « Mes appareils ») ; rôle et statut sont relus
    en base à chaque requête (une petite requête) : un nouveau rôle s'applique tout de suite, un compte désactivé perd sa session.
- **Alternatives** : jeton « remember-me » séparé (deux mécanismes à sécuriser) ; durée fixe de 180 jours (pas glissante).
- **Statut** : validée.

### D-049 — Qui gère qui (matrice du §5 précisée)
- **Contexte** : la spec dit que la direction désactive les comptes et crée des liens de réinitialisation, sans dire pour qui.
  Une direction qui pourrait réinitialiser le mot de passe de l'admin prendrait son compte.
- **Choix** : personne n'agit sur son propre compte (rôle, désactivation, lien). La **direction** agit sur les grimpeurs et les
  ouvreurs seulement (rôle Ouvreur·se donné ou retiré, désactivation, lien de réinitialisation). L'**admin** agit sur tout le monde
  sauf lui-même et donne n'importe quel rôle. Il reste toujours un admin actif : vérifié sous verrou (`select … for update` des admins)
  au moment d'un changement de rôle ou d'une désactivation ; avec la règle « pas sur soi-même », ce cas ne peut aujourd'hui pas se
  produire par l'API, la vérification protège les futurs chemins (suppression de compte).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q33).

### D-050 — Limitation de débit en mémoire
- **Choix** : fenêtres glissantes en mémoire (une seule instance d'API ; un redémarrage remet les compteurs à zéro, acceptable).
  Connexion : 30 essais / 15 min par adresse (le Wi-Fi de la salle est partagé) ; 10 échecs / 15 min pour un pseudo **depuis une
  même adresse** (un inconnu qui tape de faux mots de passe ne bloque que lui-même, jamais le vrai membre) et 100 échecs / 15 min
  pour un pseudo toutes adresses confondues (seule une attaque très distribuée l'atteint) ;
  vérification de code d'inscription : 20 / 15 min par adresse ; inscription : 30 / heure par adresse ; réinitialisation :
  20 / 15 min par adresse ; changement de mot de passe : compté avec les échecs du pseudo depuis l'adresse ; photo de profil :
  10 envois / heure par membre. Réponse 429 `RATE_LIMITED` + `Retry-After`. Les adresses IPv6 comptent par réseau /64
  (une box en possède des milliards).
  L'adresse du client vient de `X-Forwarded-For` posé par Caddy, cru seulement depuis les réseaux privés
  (`server.forward-headers-strategy: native`).
- **Alternatives** : Bucket4j (une dépendance pour quelques compteurs) ; Redis (refusé par la spec).
- **Statut** : validée.

### D-051 — Codes d'inscription
- **Choix** : codes générés par le serveur, 8 caractères sans caractères ambigus (pas de 0/O ni 1/I), affichés `K7XM-P2QA`, saisis
  sans se soucier de la casse, des espaces ni des tirets. Un code est désactivé, jamais supprimé (son compteur reste). Un lien
  `/inscription?code=…` prérempli peut être copié depuis l'écran des codes. L'usage d'un code est pris sous verrou (`for update`) :
  sa dernière utilisation ne peut pas servir deux fois.
- **Code de premier démarrage** : tant qu'aucun membre n'existe, chaque démarrage désactive le code précédent et en crée un nouveau
  (rôle admin, usage unique, 24 h), affiché une seule fois dans les logs ; il n'apparaît jamais dans l'écran des codes.
- **Statut** : validée.

### D-052 — Lien de réinitialisation
- **Choix** : jeton aléatoire de 256 bits, seul son SHA-256 est en base, usage unique, 24 h ; un nouveau lien efface les précédents
  non utilisés. Le lien est `/nouveau-mot-de-passe#<jeton>` : le jeton est dans le **fragment**, jamais envoyé dans une requête de page,
  donc absent des journaux du serveur et du `Referer`. Après usage : toutes les sessions du membre sont fermées et cet appareil est
  connecté. L'écran rappelle le pseudo du membre (il l'a souvent oublié avec le mot de passe). Le jeton quitte la barre d'adresse et
  l'historique dès la lecture de la page.
  Un lien en attente est annulé quand le rôle ou le statut du membre change, ou qu'il change lui-même son mot de passe ; à l'usage,
  son auteur doit être encore actif et encore autorisé à gérer ce membre (sinon, un lien fait pour un grimpeur promu ensuite à la
  direction, ou fait par une direction rétrogradée depuis, ouvrirait un compte hors de portée). Revue de sécurité de fin de phase.
- **Statut** : validée.

### D-053 — Mots de passe
- **Choix** : bcrypt via `DelegatingPasswordEncoder` ; 8 caractères minimum (comptés en caractères, pas en octets) ; 72 octets
  maximum (au-delà, bcrypt tronquerait en silence) ; refus des 1000 mots de passe courants de 8 caractères ou plus (300 d'une liste
  française, 700 d'une liste internationale, SecLists, `backend/src/main/resources/auth/common-passwords.txt`).
  Les mauvais identifiants donnent la même réponse, que le pseudo existe ou non (comparaison à un hachage factice pour le même temps
  de réponse) ; « compte désactivé » n'est dit qu'à quelqu'un qui a donné le bon mot de passe.
- **Statut** : validée.

### D-054 — Photos de profil
- **Choix** : le navigateur recadre au carré, réduit à 1024 px et redessine la photo (ce qui applique l'orientation EXIF et lit le
  HEIC des iPhone) ; le serveur vérifie le type réel (octets JPEG, PNG ou WebP, jamais le nom ni le type annoncé), refuse au-delà de
  16 mégapixels ou 8192 px de côté **avant** de décoder, ne décode que le carré central en sautant des pixels (au plus deux fois la
  taille finale : une petite image très grande ne peut pas épuiser la mémoire), deux décodages à la fois au maximum, puis redessine
  en 512 × 512 et ré-encode en JPEG sans aucune métadonnée (EXIF, GPS).
  Décodage WebP par TwelveMonkeys 3.15.3. Fichiers sur le volume `GG_MEDIA_ROOT`, nom aléatoire, servis par `GET /api/v1/media/{id}`
  aux seuls membres connectés (`Cache-Control: private, immutable` : une nouvelle photo a un nouvel identifiant).
- **Limite connue** : une photo envoyée sans passer par l'appli garde son orientation brute (pas de lecture de l'EXIF côté serveur).
- **Statut** : validée.

### D-055 — Reporté hors de la phase 1
- **Choix** :
  - **Voie préférée** (onboarding et profil) : phase 3, quand les voies existent.
  - **Export JSON et suppression de compte** (libre-service et admin) : phase 4, quand les séances existent, pour faire
    l'anonymisation « Ancien·ne membre » en une fois. La page « Tes données » le dit et renvoie vers la direction d'ici là.
  - **Colonne « séances »** du tableau des membres : phase 4.
  - **Captures d'écran du manifeste** (invite d'installation enrichie d'Android) : phase 8, avec les écrans définitifs.
- **Statut** : validée.

### D-056 — Invitation à installer
- **Choix** : détection par fonctions pures (`lib/install.ts`, testées) : déjà installée (mode standalone) → jamais ; navigateur
  intégré (WhatsApp, Instagram, Messenger, TikTok, Snapchat, LinkedIn) → explication pour ouvrir le vrai navigateur et bouton
  « Copier le lien » ; iPhone et iPad (y compris iPadOS qui se présente comme un Mac) → les trois gestes du bouton Partager ;
  invite native reçue (`beforeinstallprompt`) → bouton « Installer » ; Safari sur Mac → Fichier, « Ajouter au Dock » ; sinon → menu du
  navigateur. Fermer l'invitation sans installer (« Plus tard », la croix ou en dehors) la reporte de 7 jours, 3 fois au maximum ;
  quitter l'onboarding sans installer compte comme un premier « Plus tard ». Historique gardé dans le `localStorage` de l'appareil
  (l'installation concerne un appareil, pas un compte). Toujours accessible depuis le profil.
- **Statut** : validée ; libellés iOS à recontrôler sur appareil réel (D-041).

### D-057 — Ce que la gestion montre aux ouvreurs
- **Contexte** : spec §8.1, l'icône Gestion est visible dès le rôle Responsable ; leurs écrans (voies) arrivent en phase 3.
- **Choix** : l'icône et l'espace existent dès maintenant pour eux, avec une section « Voies » qui annonce la suite ; les sections
  Membres et Journal sont réservées à la direction, Codes d'inscription à l'admin (vérifié par le routeur et, surtout, par le serveur).
- **Statut** : validée.

### D-058 — Données optionnelles absentes plutôt que `null`
- **Choix** : l'API omet les champs optionnels vides (`spring.jackson.default-property-inclusion: non_null`), ce que décrivent les
  types générés (`champ?: type`). En entrée, un booléen absent vaut `false` (Jackson 3 le refuse par défaut).
- **Statut** : validée.

### D-059 — Adresses provisoires en `sslip.io`
- **Contexte** : le club n'a pas encore de nom de domaine ; la PWA exige HTTPS (service worker, installation).
- **Choix** : `pocket.<ip-avec-tirets>.sslip.io` (prod) et `dev.pocket.<ip-avec-tirets>.sslip.io` (dev). sslip.io est un DNS public
  qui renvoie l'IP écrite dans le nom : Let's Encrypt délivre un vrai certificat sans rien configurer. Le jour du vrai domaine :
  deux variables du frontal et `GG_URL` dans GitHub (DEPLOYMENT.md §4) ; les applis installées devront être réinstallées.
- **Alternatives** : HTTP sur l'IP (pas de PWA) ; certificat auto-signé (alertes partout) ; acheter un domaine (décision du club).
- **Statut** : à valider (Willem, quand le domaine existe).

### D-060 — Un frontal Caddy partagé, un Caddy par environnement
- **Contexte** : dev et prod sur le même serveur, un seul couple de ports 80/443 (spec §16).
- **Choix** : projet `gg-pocket-edge` : Caddy en réseau hôte, seul sur 80/443, TLS, routage par nom vers `127.0.0.1:8180` (prod)
  et `:8181` (dev). Chaque environnement a son conteneur `web` (Caddy avec la PWA, les en-têtes de sécurité, `/api` vers l'API)
  publié sur la boucle locale seulement ; API et base sur des réseaux Docker `internal` (aucun accès Internet, rien de publié).
  Le réseau hôte donne au frontal les vraies adresses des clients, IPv6 comprise (le proxy de Docker les remplacerait par celle
  de sa passerelle, ce qui fausserait la limitation de débit). Le frontal n'a aucun proxy de confiance : il écrase tout
  `X-Forwarded-For` venu d'un client ; `web` et Tomcat font confiance aux réseaux privés.
- **Alternatives** : un Caddy unique qui sert les deux environnements (couple leurs versions) ; Traefik et ses étiquettes (plus
  de pièces mobiles).
- **Statut** : validée.

### D-061 — Images
- **Choix** : contexte de build à la racine (les deux lisent `VERSION`). API : Maven sur `eclipse-temurin:25-jdk-noble`, exécution
  sur `25-jre-noble`, couches Spring Boot, utilisateur 10001, `MaxRAMPercentage=75`, `SerialGC` (un processeur, petit tas),
  healthcheck sur `/actuator/health/readiness` écrit en bash (pas de curl à embarquer). Web : `node:24-alpine` puis
  `caddy:2.11-alpine`, utilisateur 10001, port 8080 ; le build échoue si `index.html` contient un script en ligne.
  Conteneurs en lecture seule quand c'est possible, `cap_drop: ALL`, `no-new-privileges`, journaux plafonnés à 30 Mo.
  Le binaire Caddy de l'image officielle porte une capacité fichier que le noyau refuse sans capacité : copié sans elle.
  Un service ponctuel `media-owner` donne le volume des photos à l'uid 10001 avant chaque démarrage de l'API (Docker ne
  recopie le propriétaire de l'image que si l'API monte le volume la première).
- **Statut** : validée.

### D-062 — Accès SSH de déploiement
- **Choix** : utilisateur `deploy` (groupe `docker`, sans mot de passe) ; chaque clé est épinglée à un environnement par une
  commande forcée (`gg-deploy dev|prod`, `restrict`) qui n'accepte que `deploy`, `rollback`, `status`, `login`, `edge` ;
  `authorized_keys` appartient à root. Les fichiers de déploiement voyagent avec la version (archive de `deploy/` sur l'entrée
  standard) et sont gardés par version pour les retours arrière.
- **Limite assumée** : le groupe `docker` équivaut à root, et un déploiement exécute les scripts de la branche envoyée : la
  clé dev donne donc, de fait, la main sur la prod. La protection réelle : seuls les jobs des environnements GitHub ont les
  clés, et **dev comme production exigent l'approbation de Willem** (revue sécurité de fin de phase). Les jetons envoyés au
  serveur ne peuvent que lire les images (`packages: read`), et chaque environnement a son propre `DOCKER_CONFIG`.
  Les journaux de l'API prod n'apparaissent jamais dans les jobs GitHub (dépôt public : code admin, données de membres).
- **Alternatives** : Docker sans root (complexe, peu documenté avec Compose et le réseau hôte) ; agent de déploiement qui tire
  depuis GitHub (un service de plus à maintenir).
- **Statut** : validée.

### D-063 — Sauvegardes
- **Choix** : service `backup` (image PostgreSQL 18, uid 70) dans la prod : chaque nuit à 3 h 30 (heure de Paris), un fichier
  `tar` = `pg_dump` au format custom + archive des photos ; 14 quotidiennes, la dominicale copiée en hebdomadaire (8 gardées),
  10 avant déploiement, 5 avant restauration, les manuelles jusqu'à suppression ; une sauvegarde n'en écrase jamais une autre
  (la CI a montré qu'une sauvegarde de sécurité prise dans la même seconde remplaçait celle qu'on restaurait). Le service passe `unhealthy` si la dernière quotidienne a plus de
  26 h. `restore.sh` fait une sauvegarde de l'état actuel, recrée la base,
  remplace les photos, redémarre ; il est joué à chaque CI sur une stack jetable (sonde en base et fichier).
- **À valider** : aucune copie hors du serveur n'est automatisée (option « Backup Storage » d'OVH ou `rsync` vers une autre
  machine, DEPLOYMENT.md §8).
- **Statut** : à valider (Willem).

### D-064 — Pas de repli sur les identifiants locaux hors machine locale
- **Contexte** : `application-dev.yml` a des valeurs par défaut publiques (`ggpocket`/`ggpocket`) pour la machine locale, et le dev
  déployé utilise aussi le profil `dev` (seed).
- **Choix** : un `BeanFactoryPostProcessor` (avant toute connexion, donc avant Flyway) refuse de démarrer si l'environnement n'est
  pas `local` et qu'une des variables `GG_DB_URL`, `GG_DB_USER`, `GG_DB_PASSWORD` manque, ou si le mot de passe est celui de la
  machine locale. Les fichiers Compose exigent aussi ces variables (`${…:?}`).
- **Statut** : validée.

### D-065 — Release : PR ouverte par le robot, CI relancée à la main
- **Contexte** : un push ou une PR faits avec le `GITHUB_TOKEN` ne déclenchent aucun workflow, or `main` exige la CI verte.
- **Choix** : `prepare-release.yml` pousse `release/X.Y.Z`, ouvre la PR puis lance `ci.yml` sur la branche (`workflow_dispatch`,
  ajouté à `ci.yml`) : les checks s'attachent au commit de la PR. `release.yml` se déclenche sur un changement de `VERSION` dans
  `main` ; lancé à la main avec un numéro, il redéploie une version existante (retour arrière tracé, même approbation).
  Le changelog est manipulé par `.github/scripts/changelog.py` (refus si « Non publié » est vide).
- **Alternatives** : une GitHub App ou un jeton personnel pour le robot (un secret de plus à faire tourner).
- **Statut** : validée.

### D-066 — Administration GitHub et images privées
- **Contexte** : le jeton de Claude Code n'a pas les droits d'administration du dépôt (environnements, secrets, règles : 403).
- **Choix** : `.github/scripts/setup-github.sh`, lancé une fois par Willem, crée environnements, clés (la clé privée va directement
  dans le secret, jamais sur disque), variables et règles (`main` exige aussi « Deploy config » et « Docker stack » ; `prod/*` et
  `v*` réservés au workflow). Les images ghcr restent privées : le serveur se connecte avec le jeton éphémère du job ; les
  images de la version en place et de la précédente restent sur le serveur pour un retour arrière sans téléchargement.
- **Statut** : validée.

### D-067 — e2e contre la vraie stack : Chromium seulement
- **Contexte** : les e2e de `frontend/e2e/` tournent contre une fausse API ; la spec demande aussi des e2e contre la stack Docker.
- **Choix** : `frontend/e2e-stack/` (en-têtes, CSP sans violation, cookie `__Host-` HttpOnly et Secure, connexion d'un compte de
  démo, cache des fichiers, internes de l'API invisibles), contre `http://localhost:8080` dans le job « Docker stack ».
  Chromium seulement : le cookie de session est `Secure` et seul Chromium l'accepte sur `http://localhost`. Les parcours
  détaillés restent dans les e2e à fausse API (iPhone + Desktop).
- **Statut** : validée.

### D-068 — Réinitialiser le dev
- **Choix** : l'option « réinitialiser » de `deploy-dev.yml` supprime les volumes du projet dev (base et photos) avant de démarrer ;
  le seed recrée tout au démarrage. Cochée par défaut, et toujours active sur un déploiement par label. Refusée en prod (script et porte).
- **Statut** : validée.

### D-069 — Limiteur de débit : plafond dur
- **Contexte** : revue sécurité de la phase 1 (L3) : au-delà de 10 000 clés, seules les entrées de plus d'une heure étaient purgées.
- **Choix** : plafond de 50 000 clés ; au-delà, les clés restées calmes le plus longtemps sont oubliées (instantané trié, pour ne pas
  trier une table que d'autres requêtes modifient). Oublier une clé ne fait que lui rendre ses essais : il faudrait plus de
  50 000 adresses récentes pour effacer celle d'un attaquant précis.
- **Statut** : validée.

### D-070 — Sécurité des dépendances et CSP
- **Choix** : Trivy (binaire vérifié par somme de contrôle, pas l'action) scanne les deux images ; échec sur une vulnérabilité
  critique corrigée, chaque trouvaille (haute ou critique) remonte en annotation. Quand Spring Boot tarde, la version corrigée est
  épinglée dans le pom (`tomcat.version`, `jackson-bom.version`, `jackson-2-bom.version`), à retirer dès que le parent la rattrape.
  Dependabot ouvre des PR groupées chaque lundi (Maven, npm, actions, images). CSP : aucun script en ligne ; les attributs
  `style` restent permis (`style-src-attr 'unsafe-inline'`), utilisés par Vue et Reka UI pour positionner les menus, sans
  exécution de code possible.
- **Statut** : validée.

### D-071 — Rôle de base de données de l'API sans superutilisateur
- **Contexte** : revue sécurité de fin de phase : l'API se connectait avec le superutilisateur créé par l'image PostgreSQL ; une
  injection SQL aurait pu exécuter des commandes (`COPY … TO PROGRAM`).
- **Choix** : à la création du volume, `deploy/db-init/10-app-role.sh` crée le rôle de l'API (`GG_DB_USER`), propriétaire de sa
  base, sans autre droit. Le superutilisateur `postgres` (`GG_DB_ADMIN_PASSWORD`) ne sert qu'aux sauvegardes et restaurations ;
  la restauration recrée la base au nom du rôle de l'API (`pg_restore --role`). Le seed de démonstration ne crée plus le code
  `DEMO2026` hors de la machine locale (le dev déployé reste fermé). Les clés IPv6 du limiteur étaient déjà regroupées par /64 ;
  la purge du limiteur redescend désormais à 80 % du plafond d'un coup.
- **Statut** : validée.

### D-072 — Logo du club dans l'en-tête, nom « GG Pocket »
- **Contexte** : après la mise en prod de la 0.1.0, Willem préfère le vrai logo, même petit, au sommet dessiné de D-039, et le
  nom de l'appli plutôt que celui du club ; le logo en grand de l'Actu faisait alors doublon.
- **Choix** : l'en-tête affiche le logo détouré (clair ou sombre) à 44 px de haut, puis « GG Pocket » à partir de 26rem de
  large (sous cette largeur, le pseudo et l'avatar prennent la place). Le logo est décoratif (`alt=""`) : le lien porte le nom
  « GG Pocket, retour à l'actu ». L'encart de l'Actu ne garde que sa phrase d'accroche. Les écrans d'accueil et de connexion
  (`DoorHeader`) ne changent pas.
- **Statut** : validée (Willem).

### D-073 — Recharger la page quand le code d'un écran a disparu
- **Contexte** : dans l'appli installée sur iPhone, toucher l'avatar ne faisait plus rien après la 0.1.0. L'écran Profil est le
  seul à contenir le numéro de version (`__APP_VERSION__`), donc le nom de son fichier change à chaque version. iOS garde l'appli
  en mémoire pendant des jours : l'ancien code tourne encore quand le nouveau service worker a déjà vidé l'ancien cache, et le
  serveur répond 404 sur l'ancien fichier. La navigation échouait sans aucun message, alors que les écrans inchangés marchaient.
- **Choix** : `router.onError` reconnaît l'échec de chargement d'un écran (messages de Chromium, WebKit, Firefox et Vite) et
  recharge la page visée (`src/lib/stale-chunk.ts`). Un deuxième échec dans les 30 secondes n'entraîne pas de nouveau
  rechargement, pour éviter une boucle si le serveur est en panne ou le téléphone hors ligne. Caddy ne met plus le cache d'un
  an (`immutable`) sur un fichier `/assets/` absent : un 404 part en `no-store`.
- **Limite du test** : WebKit garde en mémoire, pour tout l'onglet, l'échec d'un module à une URL donnée, et ne la redemande
  pas après le rechargement, même en `no-store`. L'e2e recharge le même build, donc le même nom de fichier : sur iPhone 13, il
  vérifie seulement que la page est bien rechargée, et que le profil s'affiche sur Chromium. En vrai, le nouvel `index.html`
  pointe vers un fichier au nouveau nom, que WebKit va chercher.
- **Alternatives** : garder les fichiers des anciennes versions sur le serveur (images Docker plus lourdes, ménage à prévoir) ;
  sortir la version du fichier du Profil (ne règle que ce cas, n'importe quel écran modifié a le même problème).
- **Retour du dev (2026-10-21)** : sur l'iPhone de Willem (PWA du dev), toucher l'avatar donne un écran noir. Le rechargement a
  lieu, mais le fichier du Profil échoue encore : ce n'est pas (seulement) une appli périmée, ce fichier ne se charge pas dans
  la PWA. L'écran noir vient de `App.vue`, qui n'affiche rien tant que la première navigation n'a pas abouti.
- **Suite** : au second échec, l'appli affiche `LoadFailureScreen` (« Réessayer », « Vider le cache et réessayer », qui
  désinscrit le service worker et vide ses caches sans déconnecter, et des détails à copier : version, mode, état du service
  worker, caches, scripts de `/assets/` redemandés qui ne répondent pas un JavaScript en 200). Les e2e bloquent le service
  worker (`serviceWorkers: 'block'`) : c'est le premier suspect, jamais testé.
- **Détails reçus (iPhone, iOS « 18_7 », Safari 27, PWA déjà installée)** : aucun service worker aux commandes ; 15 scripts sur
  16 répondent, `ProfileView-*.js` donne `Load failed` (erreur réseau, pas un code HTTP), même après « Vider le cache » et
  relance de l'appli. Le service worker est donc écarté. L'écran d'erreur redemande maintenant le fichier avec `?sonde=` :
  OK = adresse refusée par le téléphone ; échec = fichier qui casse en route. Bouton « Retour à l'actu » ajouté.
- **Sonde (2e envoi)** : `?sonde` échoue aussi (`TypeError: Load failed`), et Safari hors PWA échoue de même, Tailscale coupé
  ou non. Ce n'est donc ni l'adresse, ni la PWA, ni la taille (les deux scripts de plus de 120 ko passent) : c'est le contenu
  de ce fichier qui casse à la réception. La seule transformation qui dépend du contenu est la compression : Caddy envoyait du
  zstd, que Safari 27 accepte. Hypothèse : défaut du décodeur zstd de Safari 27 sur ce flux précis. Les e2e ne le voyaient pas
  (`vite preview` ne compresse pas en zstd, l'e2e de la stack tourne sous Chromium).
- **Essai gzip (réfuté)** : avec `encode gzip` seulement, l'iPhone de Willem (iOS 27.0) échoue toujours, même après un
  redémarrage, alors que des amis (dont un en iOS 27.0.1) installent la PWA du dev sans souci. Le zstd est rétabli.
- **Piste suivante** : un filtre propre à ce téléphone (bloqueur de contenu, protection contre le pistage, Temps d'écran,
  profil VPN ou DNS), qui juge sur l'URL : les deux adresses testées contiennent `ProfileView`. Les détails de l'écran
  d'erreur demandent maintenant des fichiers inexistants (`ProfileView-sonde.js`, `profile-sonde.js`, `Profil-sonde.js`,
  `Temoin-sonde.js`) : 404 = le serveur a répondu ; `Load failed` = refusé sur le téléphone avant d'arriver au serveur.
- **Cause trouvée (iOS 27.0.1, après mise à jour)** : `ProfileView-sonde.js`, `profile-sonde.js` et `Profil-sonde.js` →
  `Load failed` ; `Temoin-sonde.js` → 404. Le téléphone refuse, avant même de demander au serveur, toute requête de script ou
  `fetch()` dont l'URL contient « profil » (sans tenir compte des majuscules). Le réseau ne voit pas les chemins en HTTPS :
  c'est un filtre sur le téléphone (Safari et PWA), absent chez les amis de Willem.
- **Choix** : fichiers construits nommés par leur seule empreinte (`assets/[hash].js`, `vite.config.ts`), puisque les listes
  de blocage jugent les noms et pas le contenu. Les e2e reconnaissent le script du Profil à son contenu. L'écran d'erreur
  garde ses sondes de noms, pour reconnaître le cas chez un autre membre.
- **Vérifié sur l'iPhone de Willem (dev, 2026-10-21)** : l'avatar ouvre le profil, et « Modifier mon profil » enregistre
  (`PUT /api/v1/me/profile` passe : le filtre ne vise que les fichiers). Le filtre lui-même n'est pas identifié.
- **Statut** : validée.

### D-074 — Cotations en lecture seule
- **Contexte** : la spec range les cotations dans la configuration du club (§5), mais l'écran de configuration (§8.11) ne cite
  que secteurs, cordes et couleurs. Une cotation renommée ou retirée fausserait les tris, les filtres et, plus tard, le badge
  « Première cotation » et les classements.
- **Choix** : les 36 cotations (4a- → 7c+, D-002) sont créées par la migration, identiques en dev et en prod, avec un rang
  numérique ; l'appli les lit mais ne les modifie pas. En ajouter une (8a un jour ?) se fait par une migration.
- **Statut** : validée (Willem, 2026-10-23).

### D-075 — Coups de cœur : la limite vise les voies non démontées
- **Choix** : deux coups de cœur au plus sur des voies non démontées (D-010). Le troisième est refusé (`FAVORITES_FULL`), sauf
  remplacement explicite d'un des deux, fait dans la même transaction. La ligne du membre est verrouillée pendant le contrôle,
  pour que deux requêtes simultanées ne passent pas à 3. Remonter une voie démontée peut ramener quelqu'un à 3 : c'est toléré
  (on ne retire pas un cœur sans le dire), et il ne peut plus en ajouter tant qu'il en a 2 ou plus.
- **Statut** : validée (Willem, 2026-10-23).

### D-076 — Styles de voie
- **Choix** : liste fixe dans le code (`RouteStyle`) : Conti, Rési, Physique, Technique, Complexe, Dalle, Dévers, Réglettes,
  Bacs, Pinces, Plats, Dièdre, Toit, Morpho ; cinq au plus par voie, pour que les icônes tiennent dans une ligne du topo.
  Les libellés sont dans `fr.json`, donc renommables sans toucher aux données.
- **Statut** : validée (Willem, 2026-10-23).

### D-077 — Ouvreurs d'une voie
- **Choix** : un ou plusieurs comptes de rôle Responsable et plus (actifs), et/ou un nom libre pour un ouvreur sans compte
  (invité, ancien membre). Au moins l'un des deux. La date d'ouverture ne peut pas être dans le futur.
- **Statut** : validée (Willem, 2026-10-23).

### D-078 — Le topo trie et filtre dans le navigateur
- **Contexte** : une salle porte une soixantaine de voies actives.
- **Choix** : `GET /api/v1/routes` renvoie toutes les voies actives et en dernière chance avec leurs compteurs (sans requête
  par voie), et le navigateur trie, filtre et regroupe (`lib/`, testé). Changer de tri est instantané, même avec un réseau de
  salle. Tri et filtres vivent dans l'URL : un lien partagé ou un retour arrière les retrouve. Les archives sont chargées à
  part, à la demande.
- **Alternative** : tri et filtres côté serveur avec pagination, utile au-delà de quelques centaines de voies : pas le cas.
- **Statut** : validée.

### D-079 — Photo de voie
- **Choix** : même chaîne de sécurité que la photo de profil (D-054 : type réel, plafonds avant décodage, deux décodages à la
  fois, ré-encodage JPEG sans métadonnées) mais sans recadrage : la photo est réduite pour tenir dans 1600 px. Le navigateur la
  réduit avant l'envoi. Servie aux seuls membres connectés.
- **Statut** : validée.

### D-080 — Statistiques d'ascensions de la fiche voie en phase 4
- **Contexte** : la fiche voie doit montrer le nombre d'ascensions, de topées, de grimpeurs et la part en tête, ainsi que
  « mon historique » sur la voie, et le topo doit pouvoir trier par réalisations. Tout cela se calcule sur les ascensions,
  qui arrivent avec les séances.
- **Choix** : en phase 3, la fiche n'affiche rien pour ces chiffres (pas de faux zéros) et le tri par réalisations n'est pas
  proposé. Ils arrivent avec la phase 4 (ligne dans la ROADMAP).
- **Statut** : validée.

### D-081 — Une voie démontée est en lecture seule
- **Choix** : sur une voie démontée, on ne modifie rien (fiche, photo, nouveaux commentaires, coups de cœur, « je sais la
  faire », dernière chance) : refus `ROUTE_DISMOUNTED`. Seul le remontage la rend de nouveau modifiable. Exception : un
  commentaire peut toujours être supprimé (par son auteur, ou par la direction pour modérer). Un coup de cœur posé avant
  le démontage reste donc dans l'historique du profil (D-010), sans compter dans la limite de deux.
- **Statut** : validée.

### D-082 — Pop-up « dernière chance » : « Me le redire plus tard »
- **Contexte** : la spec prévoit un seul bouton, « J'accepte leurs destins », qui enregistre la prise de connaissance.
- **Choix** : un second bouton, « Me le redire plus tard », ferme la pop-up sans rien enregistrer : elle revient à la
  prochaine ouverture de l'appli. La pop-up n'apparaît jamais pendant la saisie d'une séance, ni par-dessus l'invitation à
  installer l'appli.
- **Statut** : validée (Willem, 2026-10-23).

### D-083 — Revue visuelle de la phase 3 : table de gestion compacte, tampon au-dessus de l'étiquette
- **Contexte** : à 1280 px, la table des voies débordait (huit colonnes dans ~860 px) ; le tampon « Démontée » posé sur
  l'étiquette cachait les noms courts (DESIGN §5 : le nom reste lisible).
- **Choix** : la table perd la colonne « Cotation » (déjà sur l'étiquette) et la colonne « Ouverture » (reprise sous le
  statut, en date courte) ; cœurs et commentaires ont une icône pour en-tête. Le tampon « Démontée » se pose au-dessus de
  l'étiquette, à la manière du ruban « Dernière chance ». Les tris du topo s'appellent « Secteur » et « Ouverture » (le
  libellé long était coupé à 360 px). Les textes ont une espace insécable avant `? ! : ;` et les dates écrivent « 1er ».
  On garde deux façons de montrer une sélection dans le formulaire de voie : coche pour les couleurs (choix exclusif),
  aplat plein pour les styles et les ouvreur·ses (choix multiples).
- **Statut** : validée.

### D-084 — Mois figé calculé d'après la date, avant la clôture de la phase 6
- **Contexte** : la spec fige un mois le 8 du mois suivant à 04:00 (D-009), par une tâche qui écrit les classements
  (phase 6). Les séances (phase 4) doivent déjà refuser les changements d'un mois figé.
- **Choix** : `SessionCalendar.isFrozen` calcule la règle sur l'horloge (Europe/Paris) : un mois est figé dès le 8 suivant
  à 04:00, que la tâche ait tourné ou non. La tâche de la phase 6 figera les classements au même instant, sans changer
  cette règle. Création : aujourd'hui à 7 jours en arrière (`SESSION_TOO_OLD`), jamais demain (`DATE_IN_THE_FUTURE`),
  jamais dans un mois figé (`MONTH_FROZEN`). Modification et suppression : mois de la date actuelle non figé ; une
  nouvelle date suit les règles d'une création (une séance peut donc garder son jour même au-delà de 7 jours).
- **Statut** : validée (application directe de D-009).

### D-085 — Envoi idempotent des séances (`clientRef`)
- **Contexte** : la file hors ligne renvoie une séance si la réponse s'est perdue ; il ne faut pas de doublon.
- **Choix** : l'appli tire un UUID par nouvelle séance (`clientRef`, unique par membre). Un second `POST` avec la même
  référence renvoie la séance déjà enregistrée (200) sans rien changer. La photo part ensuite (`PUT …/photo`), et repart
  avec sa séance si elle s'est perdue. Les écritures de séances verrouillent la ligne du membre (une à la fois).
- **Alternative** : en-tête `Idempotency-Key` générique : plus lourd pour un seul usage.
- **Statut** : validée.

### D-086 — « Je sais la faire » automatique et décochage
- **Choix** : une ascension enchaînée coche la voie dans son mode (source `ASCENT`) ; plus d'ascension enchaînée, la
  coche `ASCENT` part ; une coche manuelle (`MANUAL`) n'est jamais touchée par les séances. Décocher à la main une voie
  enchaînée dans ce mode est refusé (`CHECK_FROM_ASCENT`, 409) : la coche reviendrait à la séance suivante ; on modifie la
  séance à la place. « Sue en tête implique sue en mouli » reste une règle d'affichage (À faire, PR 4.2).
- **Statut** : validée.

### D-087 — Actu montre mes séances en attendant le fil
- **Contexte** : le fil des amis arrive en phase 5 ; il faut d'ici là retrouver, modifier et supprimer ses séances.
- **Choix** : Actu liste « Mes séances » (pagination par curseur, carte façon Strava) ; une séance a sa page
  (`/seances/:id`) avec Modifier et Supprimer. Lecture réservée au propriétaire (404 pour tout autre, direction comprise)
  tant que les amis n'existent pas ; la photo de séance suit la même règle (`PrivateMediaAccess`).
- **Statut** : à valider (Willem).

### D-088 — Ressenti en curseur, pas en dix pastilles
- **Contexte** : la spec laisse le choix « curseur ou 10 pastilles » ; dix cibles de 44 px ne tiennent pas en 360 px.
- **Choix** : un curseur 0 à 10, où 0 veut dire « pas noté » (le ressenti reste facultatif), pour chaque voie et pour la
  séance. Une nouvelle ligne est « topée » par défaut, dans le mode de la ligne d'avant.
- **Statut** : validée.

### D-089 — Voie démontée dans une séance notée après coup
- **Choix** : une séance peut être notée jusqu'à 7 jours après ; une voie démontée y est acceptée si elle a été démontée
  le jour de la séance ou après, refusée sinon (`ROUTE_DISMOUNTED`). Le sélecteur rapide ne propose que les voies au mur.
  Une séance compte 1 à 50 ascensions, 0 à 99 chutes par ascension.
- **Statut** : validée.

### D-090 — « Réalisations » = ascensions topées
- **Contexte** : la spec prévoit un tri du topo « nombre de réalisations » sans le définir.
- **Choix** : le nombre d'ascensions topées par tout le monde, répétitions comprises (comme les compteurs du §9.1). La
  fiche voie montre aussi ascensions, topées, grimpeur·ses différent·es et part en tête : des agrégats sans nom, qui
  comptent aussi les séances « juste moi ».
- **Statut** : validée (club, questionnaire du 2026-11-07, Q07).

### D-091 — Mises à jour de l'appli installée : chercher plus souvent, dire pourquoi ça bloque
- **Contexte** : sur l'iPhone de Willem, la PWA du dev affichait encore « GG Pocket 0.1.1 » avec un serveur en 0.2.0, et
  aucun bandeau « Nouvelle version » ne venait, même après avoir fermé l'appli. Réinstallée en 0.2.0, le Profil ne
  s'ouvre plus : `/assets/DaqITAgE.js` → `Load failed` (même avec `?sonde`), alors que son nom ne contient pas « profil »
  (D-073), et « aucun » service worker aux commandes malgré un cache de pré-chargement présent. Lecture : une nouvelle
  version ne s'installe que si **tous** ses fichiers arrivent ; un seul fichier refusé par le téléphone et l'installation
  échoue sans un mot, l'ancienne version reste (ou aucune ne prend la main).
- **Choix** :
  - l'appli cherche une mise à jour au retour au premier plan et toutes les heures, plus seulement au démarrage complet
    (`lib/appUpdate.ts`, branché dans `UpdateBanner`) ;
  - Profil → « Chercher une mise à jour » dit en clair : à jour, nouvelle version prête, ou échec avec la liste des
    fichiers refusés (à copier) ;
  - nouvelle sonde : `/sonde/temoin.js?f=<fichier>` (Caddy du conteneur web) sert les mêmes octets sous un nom neutre.
    Refusé là aussi : le téléphone juge le **contenu** ; accepté : il juge le **nom**. L'écran d'erreur et le bouton la
    lancent pour chaque script refusé, et listent tous les fichiers de l'appli que le téléphone refuse.
- **Cause trouvée (2026-10-25)** : l'extension Safari « Cyberfiltre » (sept bloqueurs de contenu « Phishing1 » à
  « Phishing7 », dans *Réglages → Apps → Safari → Extensions*). Une fois coupée, le Profil s'ouvre et les mises à jour
  arrivent. Un bloqueur de contenu WebKit ne juge que l'adresse (motifs sur l'URL, le domaine, le type de ressource),
  jamais le contenu : la règle fautive vise donc l'adresse du fichier, peut-être le domaine de test `*.sslip.io`
  (souvent listé par les filtres anti-hameçonnage) associé au chemin. Le nom d'un fichier change à chaque version :
  une version peut passer, la suivante non.
- **Suite** : savoir si la prod (vrai domaine) est touchée avant de changer quoi que ce soit. Si un membre tombe sur le
  même écran, l'écran d'erreur et le bouton « Chercher une mise à jour » listent les fichiers refusés ; la réponse à
  donner est de couper Cyberfiltre (ou de l'autoriser pour le site). Si la prod est touchée, nommer les fichiers par
  un motif plus sage, avec la sonde pour le vérifier.
- **Statut** : cause trouvée ; contournement côté appli à décider si la prod est touchée.

### D-092 — « À faire » : l'état de chaque voie vient du serveur
- **Contexte** : la spec (§8.5) veut deux listes (mouli, tête), « sue en tête implique sue en mouli », et la possibilité
  de décocher ; le front ne recalcule aucune règle (§9).
- **Choix** : `GET /api/v1/me/todo` renvoie chaque voie au mur avec son état par mode : `TODO`, `CHECKED` (à la main),
  `FROM_ASCENT` (une ascension enchaînée dans ce mode), `FROM_LEAD` (mouli seulement : sue en tête), et les compteurs.
  Seule une coche `CHECKED` se décoche depuis l'écran ; les autres disent quoi faire (modifier la séance, décocher en
  tête). Cocher et annuler passent par les points d'accès de la fiche voie. Filtres secteur et famille de cotation
  (« les 6a » = 6a-, 6a, 6a+) et tri : présentation seulement, dans l'URL.
- **Statut** : validée (application de la spec).

### D-093 — Planning : bilan du mois et série en cours
- **Contexte** : la spec (§8.6) cite « séances, voies topées, cotation max, ressenti moyen » et une série « 3 semaines
  d'affilée avec 2 séances ou plus », sans définir les cas limites.
- **Choix** : présences = jours de grimpe (§9.1) ; « Séances » du bilan = séances réellement notées ; voies topées =
  ascensions topées, répétitions comprises ; cotation max = la plus dure topée ; ressenti moyen = moyenne des ressentis
  de séance renseignés, à une décimale. Série = semaines ISO consécutives à au moins 2 jours de grimpe, en remontant
  depuis la semaine en cours ; la semaine en cours compte dès qu'elle a ses 2 jours et ne casse jamais la série avant
  dimanche. Pas de mois futur ni avant 2000-01 (400). Un jour de la semaine écoulée sans séance propose d'en noter une
  (`/seance/nouvelle?jour=…`).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q19) ; affichage complété par D-122.

### D-094 — Le carnet personnel
- **Contexte** : la roadmap prévoit un « carnet personnel » et le profil un « lien vers mon carnet complet » (§8.8),
  sans en décrire le contenu.
- **Choix** : `/carnet` (depuis le Profil) : chiffres (séances, voies différentes, ascensions, topées, topées en tête,
  enchaînées, cotation max, max en tête), pyramide par cotation (voies différentes topées, comptées dans le mode de
  leur meilleure ascension) et la liste de toutes les voies grimpées, démontées comprises, avec la meilleure réalisation
  (enchaînée en tête > topée en tête > enchaînée en mouli > topée en mouli > pas encore topée). Mon carnet seulement en
  phase 4 ; celui d'un ami arrivera avec les amis (phase 5).
- **Statut** : à valider (Willem).

### D-095 — Lecture hors ligne du topo, du profil, du planning et de l'À faire
- **Choix** : le service worker lit `routes`, `routes/{id}`, `topo/config`, `me`, `me/favorites`, `me/todo` et
  `me/planning` en *network-first* (5 s), garde la dernière réponse 200 dans le cache `gg-offline-reads` (60 entrées,
  14 jours) et la sert sans réseau (§12). Ces réponses sont celles du membre connecté : le cache est vidé à chaque
  connexion et déconnexion. Jamais les fichiers (`/media`), l'authentification ni les séances.
- **Statut** : validée (application de la spec).


### D-096 — Suppression de compte et « Ancien·ne membre »
- **Choix** :
  - Un seul membre anonyme « Ancien·ne membre », créé par la migration V10 (UUID fixe `…a5c1`), désactivé, au pseudo
    impossible à saisir et au mot de passe inconnu. Il n'apparaît ni dans le tableau des membres, ni dans les
    classements (`appears_in_rankings = false`), et ne compte pas comme un membre (premier démarrage, garde-fou du seed).
  - Ce qui lui est transmis : les séances et leurs passages (pour les statistiques des voies), sans commentaire ni photo,
    en visibilité « juste moi » ; le crédit d'ouvreur·se des voies ouvertes. Les classements figés suivront en phase 6.
  - Ce qui est effacé : la ligne du membre, son avatar et ses photos de séance (fichiers compris), ses coups de cœur,
    ses « je sais la faire » (même ceux venus d'une séance), ses commentaires de voies, ses accusés « dernière chance »,
    ses liens de réinitialisation et toutes ses sessions. Les photos de voies restent au club.
  - Libre-service derrière le mot de passe (compté comme un échec de connexion s'il est faux) ; côté gestion,
    l'admin seulement, jamais sur son propre compte. Le dernier admin actif ne peut pas partir.
  - Le journal d'audit garde une ligne `MEMBER_DELETED` avec le pseudo (intérêt légitime, durée du journal : 3 ans).
  - Export : un fichier JSON (`GET /api/v1/me/export`) avec le profil, les séances, les coches, les coups de cœur,
    les commentaires, les accusés, les voies ouvertes, les appareils et la liste des photos (pas les images elles-mêmes).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q27).

### D-097 — La cotation avant la corde sur l'étiquette de voie
- **Contexte** : demande du club (Willem, 2026-10-27) : la corde était l'info la plus voyante de l'étiquette, alors que
  les grimpeurs cherchent d'abord la cotation. La spec (§10, élément signature) voulait un « numéro de corde bien lisible ».
- **Choix** : la cotation prend le bloc encre à gauche (Archivo 800 largeur 75, largeur minimale fixe pour aligner les
  noms d'une liste) ; la corde passe à droite, plus petite et en encre à 75 %, sous un petit « corde » pour qu'un numéro
  seul ne passe pas pour une cotation. Le libellé accessible suit l'ordre visuel : « 6b, Bye Bye Morice, couleur jaune
  et bleu, corde 12 ». Le ruban « Dernière chance » et le tampon « Démontée » se placent à gauche de la corde.
- **Statut** : validé (demande du club).

### D-098 — Phase 5 : amis, annuaire et lecture des séances d'un ami
- **Contexte** : phase 5 (spec §7, §8.2, §8.8, §14). Willem a demandé (2026-10-27) s'il fallait une base graphe type
  Neo4j : non, les amitiés sont directes (pas d'amis d'amis), ~200 membres, une table PostgreSQL et un index suffisent ;
  une deuxième base coûterait sauvegardes, cohérence et tests sans rien apporter.
- **Choix** :
  - Deux PR, sur le modèle de la phase 4 : 5.1 « Amis » (annuaire, amitiés, profils, séances d'un ami), 5.2 « Fil
    d'actu » (fil, réactions, commentaires, modération).
  - Table `friendship` : une ligne par paire quel que soit le sens (index unique sur `least/greatest`), `PENDING` puis
    `ACCEPTED`. Refuser, annuler ou retirer supprime la ligne (pas de statut « refusé » : on peut redemander, la
    limite de 30 demandes par heure suffit contre l'insistance). Demander quelqu'un qui m'a déjà demandé accepte.
  - Tout passe par mon côté (`PUT/DELETE /me/friends/{memberId}`) : pas d'identifiant d'amitié à deviner.
  - Annuaire : tous les membres actifs, envoyés d'un bloc (≤ 200) et filtrés côté client comme le tableau des membres ;
    le compteur du fil (« 87 grimpeurs accrochés ») compte les membres actifs, moi compris. Un membre désactivé ou
    « Ancien·ne membre » n'a ni ligne ni profil (404) ; un ami désactivé disparaît de mes listes et ses séances se ferment.
  - Profil public : identité, rôle, bio, années, âge seulement s'il l'a choisi, voie préférée, coups de cœur. Les
    chiffres clés viendront avec les badges (phase 7).
  - Séances d'un autre : lisibles par ses amis acceptés si la séance est « amis » ; sinon 404, comme une séance qui
    n'existe pas (une demande en attente n'ouvre rien, un rôle non plus). Même règle pour la photo et le carnet d'un ami
    (calculé sur ses séances « amis » seulement). `Session` expose désormais `memberId`.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q22).

### D-099 — Retours du club sur la PR 5.1 : demandes d'amis, accueil, déconnexion
- **Contexte** : Willem a essayé la PR 5.1 sur le dev (2026-10-27). La spec (§8.2) plaçait les demandes reçues en
  section en haut du fil ; elles y prenaient trop de place.
- **Choix** :
  - Les demandes d'amis quittent le fil. Elles vivent dans le profil, sous un bandeau « N demandes d'ami », replié par
    défaut (même pour une seule), qui déplie la liste complète. La pastille sur l'avatar reste et mène au profil.
  - Le bouton de déconnexion devient « Se déconnecter » (« Redescendre » était moins clair) ; la confirmation suit.
  - L'écran d'accueil propose « Se connecter » (bouton principal) puis « Rejoindre le club » ; le lien de l'écran de
    connexion devient « Pas encore de compte ? Rejoindre le club ».
- **Statut** : validé (demande du club).

### D-100 — Phase 5.2 : fil d'actu, réactions, commentaires de séance, modération
- **Contexte** : PR 5.2 (spec §7, §8.2, §8.11, §5, §14). La spec fixe le jeu de 4 réactions et « auteur ou modération
  peuvent supprimer » ; le reste est à trancher.
- **Choix** :
  - Tout vit dans le module `session` (tables `session_reaction`, `session_comment`, V12) : la lecture d'une séance y
    est déjà décidée (`SessionAudience`), et le module `social` ne dépend pas de `session`.
  - Réagir ou commenter demande de pouvoir lire la séance (amis acceptés et actifs, séance « amis ») ; sinon 404, comme
    une séance qui n'existe pas. **Pas de réaction sur sa propre séance** (comme les kudos de Strava) ; commenter la
    sienne, oui (pour répondre).
  - Une réaction par personne et par séance (index unique, écriture par `insert … on conflict`) ; changer = remplacer,
    retoucher la même = la reprendre (côté appli). L'appli bouge les compteurs tout de suite, puis prend ceux du serveur.
  - Toute séance renvoyée par l'API porte désormais `author`, `reactions` (les 4 compteurs dans l'ordre fixe, et la
    mienne) et `commentCount` : la même carte sert au fil, au profil d'un ami et à la fiche séance.
  - Fil : mes séances + les séances « amis » de mes amis acceptés dont le compte est actif, triées comme le carnet
    (jour, puis enregistrement), curseur opaque. Les identifiants d'amis (≤ 200) sont passés à la requête.
  - Commentaires : 500 caractères, 40 par heure ; supprimer = masquer (comme les commentaires de voie). L'auteur supprime
    les siens même s'il ne voit plus la séance (ses mots restent les siens) ; le grimpeur de la séance ne supprime pas
    ceux des autres (la spec dit « auteur ou modération ») ; en dessous de la direction, le commentaire d'un autre
    répond 404.
  - Modération : `GET /admin/moderation/comments` (direction et admin) fusionne commentaires de voie et de séance,
    plus récents d'abord, par pages. Pour une séance, la direction voit qui, quel jour et le texte, **pas la séance**
    (elle reste aux amis). Une suppression par un tiers va au journal (`SESSION_COMMENT_DELETED`).
  - Suppression de compte : en plus de ses propres réactions et commentaires (cascade), ceux des autres sur ses
    séances partent, puisque plus personne ne voit ces séances. L'export contient mes réactions et mes commentaires
    de séance.
  - Badges dans la carte (« a débloqué… ») : avec la phase 7.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q23).

### D-101 — PR 5.3 : l'assureur, voie par voie
- **Contexte** : demande de Willem (2026-10-29), hors spec : choisir son assureur en saisissant une séance. Willem a
  tranché : facultatif, **un assureur par voie**, choisi dans **tout le club, amis en tête**, **membres seulement**
  (pas de texte libre : pas de nom stocké pour quelqu'un qui n'a rien accepté).
- **Choix** :
  - `ascent.belayer_id` (V13, `on delete set null`), facultatif. Le serveur refuse (400 `VALIDATION_FAILED`) le grimpeur
    lui-même et tout membre inconnu, désactivé ou « Ancien·ne membre ».
  - Saisie : une nouvelle voie reprend l'assureur de la voie d'avant (souvent le même binôme toute la soirée), dupliquer
    le garde ; le sélecteur montre mes amis, puis le reste de l'annuaire, avec une recherche.
  - Affichage : « À l'assurage : jules et tom » sur la carte, et voie par voie sur la fiche. Un assureur désactivé
    n'apparaît plus (comme partout).
  - **Être nommé ne donne aucun droit sur la séance** : une séance privée reste privée. L'assureur voit, dans Profil >
    Mes assurages, qui grimpait, quel jour et combien de voies il a assurées ; il n'ouvre la séance que s'il la voit
    déjà (ami, séance « amis »). Il peut **retirer son nom** (`DELETE /sessions/{id}/belayer`), même un mois figé : ça
    ne change rien aux chiffres du grimpeur. Le grimpeur peut le renommer ensuite (pas de liste noire, à revoir si abus).
  - Suppression de compte : un assureur supprimé disparaît des voies (FK) ; les séances cédées à « Ancien·ne membre »
    perdent leurs assureurs. Export : l'assureur de mes voies (pseudo) et mes assurages (grimpeur, jour, nombre de voies).
  - Idée pour la phase 6, non engagée : un classement « assureur du mois ».
- **Statut** : règles validées (club, questionnaire du 2026-11-07, Q10 : pas de classement des assureur·ses ; Q20) ; saisie remplacée par D-118.


### D-102 — Caddy de l'image web recompilé avec un Go corrigé
- **Contexte** : depuis le 2026-10-29, Trivy bloque l'image web : le binaire de `caddy:2.11-alpine` (2.11.7, la
  dernière) est compilé avec Go 1.26.8, dont la bibliothèque standard a une faille critique corrigée. Aucune image
  officielle corrigée n'existe encore.
- **Choix** : `frontend/Dockerfile` recompile Caddy 2.11.7 (même version, sans plugin) avec `xcaddy` sur
  `golang:1.27-alpine`, remplace le binaire de l'image officielle et applique `apk upgrade`. On revient au binaire
  officiel dès qu'il est corrigé. Le proxy d'entrée (`deploy/edge`, image officielle, non scanné par la CI) a la même
  faille : à mettre à jour sur le serveur quand l'image officielle sera corrigée.
- **Statut** : à valider.

### D-103 — PR 6.1 : le podium, la clôture mensuelle et les mois figés
- **Contexte** : phase 6 (spec §8.7, §9.2, D-003, D-004, D-009, D-084). La spec fixe les deux classements, l'ex-aequo,
  la clôture du 8 à 04:00 et l'entité `MonthlyRankingSnapshot` ; le reste est à trancher.
- **Choix** :
  - Module `ranking` (V14) : `monthly_ranking_closure` (une ligne par mois figé, clé primaire = le mois : c'est elle
    qui rend la tâche idempotente, même lancée deux fois en même temps) et `monthly_ranking_snapshot` (mois, type,
    rang, membre, valeur).
  - **Rangs** : rang olympique (1, 1, 3), puis ordre alphabétique des pseudos (collation française, sans casse :
    « abel » avant « Zazie »). Ne sont classés que les membres actifs qui apparaissent dans les classements, avec au
    moins 1. Les séances « juste moi » **comptent** (le podium montre des totaux, jamais le détail) : dit dans
    « Tes données ».
  - **Tâche** : tous les jours à 04:00 (Europe/Paris), elle fige chaque mois échu (D-084) pas encore figé, depuis le
    mois de la première séance du club. Le 8, c'est le mois précédent ; les autres jours, elle rattrape une nuit où
    le serveur était arrêté. Entre-temps, un mois échu non figé est compté en direct (ses séances ne bougent plus,
    les chiffres sont les mêmes). Le seed de démo appelle la même clôture (spec §17).
  - **API** : `GET /rankings?month=YYYY-MM` (les deux classements, statut `LIVE` / `PROVISIONAL` / `FROZEN`, date de
    gel, mes lignes, si j'apparais) et `GET /rankings/months`, pour tout membre connecté.
  - **Mois figé** : les rangs restent ceux du jour de la clôture, lignes comprises. Un membre désactivé ou qui s'est
    retiré des classements depuis **garde sa ligne, au nom d'« Ancien·ne membre »** (sans photo ni lien de profil) :
    personne ne bouge et le classement ne montre pas de trou (choix de Willem du 2026-10-30, à la place des
    lignes masquées). Un compte supprimé cède ses rangs à « Ancien·ne membre », comme ses séances (D-096).
    L'export contient mes rangs figés.
  - **Écran** : sélecteur de mois par flèches (comme le Planning), borné au premier mois ; Séances / Voies dans
    l'adresse (`?classement=voies`) ; top 3 sur des marches (hauteur selon le rang, donc les ex æquo à la même
    hauteur), puis la liste ; quand ma ligne sort de l'écran, une barre « Ta place » au-dessus des onglets.
  - « Assureur du mois » (idée de D-101) : non fait, à demander au club.
- **Statut** : validée par Willem le 2026-10-30 (les séances « juste moi » comptent ; rangs figés anonymisés plutôt que masqués).

### D-104 — PR 6.2 : Fréquentation et Bilan du topo
- **Contexte** : spec §8.10 (Responsable et +). La spec ne dit pas ce qu'est un « taux de réussite », ni quelles voies
  lister pour un mois, ni comment compter « les plus et les moins grimpées ».
- **Choix** :
  - **Module `stats`** (backend), `GET /api/v1/stats/attendance?month=YYYY-MM` et `GET /api/v1/stats/routes`, réservés
    au rôle Ouvreur et au-dessus (`@PreAuthorize`, matrice d'autorisations). Seuls des totaux sortent, jamais qui a
    grimpé : les séances « juste moi » comptent donc aussi.
  - **Taux de réussite** = part des ascensions **topées** (avec ou sans chute), arrondie ; absent sans ascension. Même
    définition du succès que le podium (D-003). Les enchaînées ont leur propre colonne.
  - **Voies d'un mois** : celles au mur pendant le mois (ouvertes au plus tard le dernier jour, pas démontées avant le
    premier), grimpées ou non (pour voir celles que personne ne touche), plus celles grimpées dans le mois même si elles
    sont démontées depuis. Tri par défaut : les plus grimpées.
  - **Mois proposés** : ceux du podium (`/rankings/months`, du premier mois grimpé à aujourd'hui), jamais un mois futur.
  - **Export CSV** généré dans le navigateur à partir des lignes affichées, dans l'ordre du tri : séparateur `;`, BOM
    UTF-8, fins de ligne CRLF (ce qu'attendent Excel et LibreOffice en français) ; une cellule qui commence par `=`, `+`,
    `-` ou `@` est préfixée d'une apostrophe (pas de formule exécutée par le tableur). Fichier
    `gg-pocket-frequentation-AAAA-MM.csv`.
  - **Bilan du topo** (voies au mur aujourd'hui, dernière chance comprise) : histogramme par cotation **de base**
    (6a-, 6a, 6a+ → « 6a »), de la plus facile à la plus dure présente, trous compris ; répartition par secteur ;
    grille des cordes actives (une corde libre est en pointillés) ; les 5 plus et les 5 moins grimpées depuis leur
    ouverture, toutes tentatives comprises (à égalité, la plus ancienne d'abord) ; les plus anciennes (8, puis « Tout
    afficher ») ; les 5 premiers coups de cœur. Les agrégats sont calculés côté front (`lib/stats.ts`, testés), le
    backend ne donne que les voies et leurs compteurs.
- **Statut** : remplacée par D-117 (club, questionnaire du 2026-11-07, Q08).

### D-105 — PR 7.1 : le moteur de badges
- **Contexte** : spec §9.4, §9.5. Plusieurs conditions laissent un choix : les cotations en « - » (D-002), le décompte
  des 30 jours d'absence, le flash répété sur une même voie, la séance à laquelle rattacher un badge de semaine ou de mois.
- **Choix** :
  - **Moteur pur** (`badge/domain/BadgeEngine`) : l'historique complet est rejoué de la plus ancienne séance à la plus
    récente (jour, puis heure d'enregistrement) ; chaque obtention a une clé stable (vide pour un palier ; la semaine,
    le mois, la séance, le jour ou la voie pour un répétable) et va à la séance qui a rempli la condition. Une obtention
    ne dépend que des séances d'avant : modifier une séance ne touche jamais les badges des mois figés.
  - **Enregistrement** : une ligne par obtention (`badge_award`, V15). Après chaque création, modification ou suppression
    de séance, la différence est écrite : ajoutées (renvoyées dans `unlockedBadges` pour le moment de déblocage),
    retirées, ou déplacées vers une autre séance (même clé : pas de nouveau déblocage). Nombre, première et dernière
    date se lisent sur ces lignes. Renvoyer une séance déjà reçue (même `clientRef`) ne débloque rien.
  - **Première cotation** : « 6a » demande 6a, 6a+ ou plus dur ; un **6a- ne compte pas** (lecture littérale de « ≥ X »).
  - **Jours de grimpe** (D-004) pour les semaines, les mois, les paliers « séances réalisées » et le retour en force ;
    le badge va à la première séance du jour. Les badges « séance » (full tête, parfaite, zéro repos, record) restent
    par séance : deux séances le même jour ne s'additionnent pas.
  - **Le retour en force** : au moins **30 jours entre deux jours de grimpe** (grimpé le 1er mars, revenu le 31 : oui ;
    le 30 : non). Une fois par jour de retour.
  - **Le flash** : une fois **par voie** (on ne flashe une voie qu'une fois, même si « premier essai » est recoché plus
    tard) ; la voie doit avoir été ouverte au plus 14 jours avant (le jour d'ouverture compte).
  - **Un mois sans rater une semaine** : les semaines qui commencent (lundi) dans le mois ; la dernière peut finir le mois
    suivant, le badge tombe alors à la première séance de cette semaine-là.
  - **3 semaines d'affilée** : blocs de 3 semaines consécutives à 2 jours ou plus, sans chevauchement (6 semaines = ×2).
  - **La séance record** : à partir de la 4e séance (séances, pas jours), strictement plus de voies topées que toutes les
    séances d'avant.
  - **Ancienneté** : calculée côté serveur (année en cours à Gravelines − année d'arrivée), absente sans année d'arrivée,
    dans le profil et les cartes de l'annuaire et des amis. Une année d'arrivée future compte comme « Tout frais ».
  - **Visibilité** : la bibliothèque de badges fait partie du profil public (tout grimpeur la lit, membre actif seulement) ;
    les badges d'une séance suivent la visibilité de la séance (fil). Toutes les séances comptent, « juste moi » comprises.
  - **Suppression de compte** : les badges partent avec le compte (clé étrangère), les séances passent à « Ancien·ne
    membre » sans badge.
- **Statut** : validée (club, questionnaire du 2026-11-07, Q13).

### D-106 — PR 7.2 : badges à niveaux, rareté et favoris
- **Contexte** : retour de Willem sur la première bibliothèque (paliers alignés 1, 5, 10…, noms et conditions partout) :
  trop de place sur le profil, réglages trop loin. Demandé : profil replié (compteur, 3 derniers, tout le reste
  dépliable), chaque badge une seule fois, détails dans une fenêtre au toucher, « Niveau N » / « Niveau max » sans dire
  combien il y en a, barre de progression cumulée (6/10, pas de remise à zéro), couleur = niveau identique pour tous les
  badges, « ×N » pour les répétables, 2 ou 3 badges favoris à côté du nom (fil et profil).
- **Choix** :
  - **17 badges** (`Badge`) : les quatre familles à paliers (un badge, son niveau = paliers obtenus) et les treize
    répétables (niveau = nombre d'obtentions). « X sur 17 débloqués ». Les codes de palier (`BadgeCode`) ne changent pas.
  - **Échelle de rareté** en couleurs de prises du club : 1 marron (bronze), 2 vert clair, 3 vert, 4 bleu clair, 5 bleu,
    6 violet, 7 rose, 8 rouge, 9 orange, 10 or (avec un reflet). Le gris reste réservé aux badges pas encore obtenus.
    Un niveau 3 a la même couleur partout, que la famille ait 8 ou 10 niveaux.
  - **Répétables** : même échelle selon le nombre d'obtentions (×1, ×2, ×3, ×5, ×10, ×15, ×25, ×50, ×75, ×100) ;
    au-delà de ×100 la couleur reste or et seul « ×N » grimpe (réponse à « que faire après la dernière couleur ? »).
  - **Barre de progression** : voies topées, en tête et jours de grimpe, comptés depuis zéro sur tout l'historique
    (`progress` de `GET /me/badges`, absent de la bibliothèque des autres : leurs totaux ne regardent qu'eux et leurs amis).
    « Première cotation » n'a pas de barre (pas de compte, juste « prochain niveau : une voie en 6b ou plus dure »).
  - **Favoris** : 3 au maximum, obtenus, dans l'ordre choisi (`PUT /me/badges/favorites`, table `favorite_badge`, V16),
    choisis depuis la fenêtre d'un badge. Affichés au niveau actuel à côté du nom (profils, `Session.authorBadges` dans
    le fil) ; un favori perdu depuis (séance supprimée) n'est plus affiché mais reste choisi s'il revient.
- **Statut** : échelle de couleurs et paliers validés (club, questionnaire du 2026-11-07, Q16) ; favoris remplacés par D-116 (2 au maximum).

### D-107 — Comptes « Touriste » et validation des nouveaux comptes par la direction
- **Contexte** : Willem n'est ni président ni licencié, et ne veut pas de droits qui ne correspondent pas à son rôle
  dans le club : l'admin est réservé au président. Le club accueille aussi des grimpeurs occasionnels non licenciés
  qui veulent garder une trace de leurs séances. Un compte admin « invisible » a été écarté : un compte qui voit les
  données de tout le monde (dont des mineurs) doit rester visible de la direction.
- **Choix** (validés par Willem le 2026-10-31) :
  - **Nouveau rôle `TOURIST`** (« Touriste »), sous Grimpeur, **avec les mêmes droits** : carnet, à faire, fil, amis,
    podium, badges. La seule différence avec Grimpeur est la licence au club.
  - **Validation** : tout nouveau compte naît Touriste et **en attente** (indicateur de validation distinct du rôle,
    car un touriste validé et un nouveau venu n'ont pas le même statut). Les codes d'inscription du club donnent
    Touriste au lieu de Grimpeur. Le profil affiche un message : la direction doit valider le compte. Les comptes
    existant avant la migration sont considérés comme validés.
  - **Déclaration à l'inscription** : la personne choisit « Je suis licencié·e au club » ou « Je ne suis pas
    licencié·e ». Dans le second cas, une zone de texte **obligatoire** apparaît : qui elle est par rapport au club
    (ex. « copain de Julie, je viens deux fois par an »), 10 à 300 caractères, contrôlés côté serveur. C'est une simple
    déclaration, pas une preuve : le compte reste en attente dans les deux cas. La direction voit la déclaration et le
    texte dans la liste des comptes en attente ; le rôle proposé par défaut à la validation suit la déclaration
    (licencié·e → Grimpeur, sinon Touriste), mais la direction choisit librement.
  - **Le texte de présentation** n'est visible que de la direction et du président, et il est **effacé une fois la
    décision prise** (validation ou suppression) : il ne sert qu'à décider (minimisation des données). La décision
    reste dans le journal d'audit.
  - **Compte en attente = carnet personnel seulement** : topo, séances, à faire ; invisible des autres (pas de fil, pas
    d'amis, pas de recherche, pas de podium) tant qu'il n'est pas validé. Protège les membres, dont des mineurs, d'un
    inconnu qui aurait le code affiché à la salle.
  - **La direction (et le président)** valide : licencié → Grimpeur, occasionnel → Touriste validé ; elle peut ensuite
    passer un compte de Touriste à Grimpeur et inversement, comme pour le rôle Responsable. Liste « comptes en attente »
    dans l'espace Gestion.
  - **Refus** : la direction peut supprimer définitivement un compte **jamais validé** (exception à « suppression =
    admin », il n'y a presque pas de données). Tracé dans le journal d'audit.
  - Toute la logique est vérifiée côté serveur (en attente ⇒ refus des endpoints sociaux, IDOR testés).
- **Par défaut, à revoir si besoin** : pas de purge automatique des comptes en attente en v1 ; les rôles Responsable
  et au-dessus restent réservés aux licenciés de fait (pas de contrôle technique de la licence).
- **Mise en œuvre** (choix par défaut, faciles à revoir) :
  - Rôle `TOURIST` sous `CLIMBER` dans la hiérarchie ; toutes les méthodes « membre » passent de `hasRole('CLIMBER')`
    à `hasRole('TOURIST')`. La validation est une autorité à part (`VALIDATED`, ajoutée par `CurrentMemberFilter`),
    exigée par `@ValidatedMember` : annuaire, profils et badges des autres, demande d'ami, podium, ajout d'un
    commentaire de voie. Colonnes `validated_at`, `declared_licensed`, `join_note` (V17).
  - Un compte en attente garde son fil (il n'y voit que ses séances : il n'a pas d'amis), lit les commentaires des
    voies sans en écrire, et ne peut ni nommer un assureur ni être nommé (ça le rendrait visible).
  - Les autres ne le voient nulle part : absent de l'annuaire, profil et badges en 404, demande d'ami refusée,
    exclu des scores du podium (ses séances comptent dès la validation, pour les mois pas encore figés).
  - Pas de changement de rôle avant validation (`MEMBER_AWAITING_VALIDATION`) : on valide d'abord, on promeut
    ensuite. Audit : `MEMBER_VALIDATED` (après = rôle choisi) et `MEMBER_REFUSED`.
  - Le code de premier démarrage crée toujours un admin validé d'office. Seed : comptes `touriste` (validé) et
    `nouveau` (en attente, non licencié).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q32).

### D-108 — « Quoi de neuf ? » : notes de version embarquées et fenêtre après mise à jour
- **Contexte** : spec §8.12. `CHANGELOG.md` est la seule source, embarquée au build ; une fenêtre présente les
  nouveautés « une seule fois par version et par utilisateur ».
- **Choix** :
  - **Build** : un petit plugin Vite (`vite.config.ts`) lit `../CHANGELOG.md` et le transforme en données
    (`virtual:changelog`, parseur `src/lib/changelog.ts`), chargées dans un morceau à part. L'appli n'affiche jamais de
    Markdown brut (pas de `v-html`) : seuls les titres de version, les groupes, les listes imbriquées, le gras et les
    liens `https` sont compris. « Non publié » n'est jamais montré. Le Dockerfile du web copie `CHANGELOG.md`.
  - **Mémoire** : la dernière version vue est gardée **dans le navigateur, par membre**
    (`localStorage`, clé `gg-pocket:release-notes:<id du membre>`). Pas de colonne ni d'endpoint côté serveur :
    rien de sensible, et un changement d'appareil ne coûte qu'une fenêtre de plus. Stockage bloqué (navigation
    privée) : au plus une fois par visite.
  - **Contenu** : les versions publiées depuis la dernière vue, jusqu'à celle qui tourne, trois au plus ; jamais rien
    vu sur cet appareil : seulement la version en cours. Une version sans note ou un retour en arrière : rien.
  - **Nouveaux comptes** : l'inscription marque la version en cours comme vue (rien à rattraper).
  - **Une fenêtre à la fois** : pas pendant la saisie d'une séance (`/seance…`), pas dans la même visite que
    l'invitation à installer (elle attend le lancement suivant), et la fenêtre « dernière chance » attend sa fermeture.
  - **Écran** « Quoi de neuf ? » dans le profil (`/profil/nouveautes`) : les trois dernières versions, les plus
    anciennes sur demande. Les groupes Keep a Changelog sont renommés pour les grimpeurs : Ajouté → « Nouveau »,
    Modifié → « Ça change », Corrigé → « Réparé », Supprimé → « Retiré ».
- **Alternative écartée pour l'instant** : mémoriser la version vue côté serveur (colonne sur le membre, `PUT /me/…`),
  pour ne la montrer qu'une fois tous appareils confondus. À reprendre si les grimpeurs s'en plaignent.
- **Reste pour la phase « logo »** : le logo téléversé dans l'en-tête de l'écran de notes (spec §8.11).
- **Statut** : à valider (Willem), appliqué par défaut.

### D-109 — Guide du club : un fichier Markdown, pas de site de documentation
- **Contexte** : spec §1 et §4, un court `docs/GUIDE_CLUB.md` sans jargon pour la direction. Un site (MkDocs ou son
  successeur Zensical) a été envisagé : recherche, lecture confortable sur téléphone, captures Playwright réutilisables.
- **Choix** (Willem, 2026-11-02) : on reste sur le fichier prévu par la spec. Le dépôt est public : le guide se lit
  sur GitHub sans compte.
- **À reprendre plus tard** si le guide grossit ou s'il faut une aide pour les grimpeurs : un site généré depuis le
  même Markdown, publié sur GitHub Pages ou servi par Caddy sous `/guide/`, sans réécrire le contenu.
- **Statut** : validée ; complétée par D-110 (le même fichier, lisible dans l'appli).

### D-110 — Le guide du club dans l'espace gestion
- **Contexte** : Willem (2026-11-02) veut que la direction lise le guide sans quitter l'appli. Un lien vers GitHub a
  été écarté : on sort de l'appli, l'interface de GitHub déroute, et il faut du réseau.
- **Choix** : `docs/GUIDE_CLUB.md` reste la seule source. Au build, markdown-it (dépendance de développement,
  absente de l'appli) le change en HTML dans le module `virtual:club-guide` (`src/lib/guide.ts`, même plugin Vite que
  le CHANGELOG) : HTML brut refusé (`html: false`), titre du document retiré (l'écran a le sien), ancres sur les
  titres pour un sommaire, tableaux qui défilent s'ils débordent, typographie française (apostrophes courbes, espaces
  insécables). L'écran `/gestion/guide` (« Guide du club ») est ouvert dès Ouvreur·se, comme l'espace gestion ; il
  affiche ce HTML tel quel (`v-html`), sûr puisqu'il vient du dépôt et jamais des membres.
- **Conséquences** : le guide décrit toujours la version installée et se lit hors connexion. Le Dockerfile du web
  copie `docs/GUIDE_CLUB.md`. La police de l'appli n'a pas de flèches : « › » à la place de « → » (un test le vérifie
  dans le guide et le CHANGELOG).
- **Statut** : validée par Willem (version intégrée choisie) ; détails de mise en œuvre par défaut.

### D-111 — Retours du club (2026-11-03) : niveaux des badges, cotations, fil, voie au hasard
- **Contexte** : quatre retours du club relayés par Willem. (1) Voir la liste des niveaux d'un badge, ceux franchis et
  ceux à venir, dans la fenêtre du badge, en gardant la barre de progression. (2) Les badges « Première cotation »
  doivent être indépendants : une 6b ne doit plus donner tout ce qu'il y a en dessous (D-105 lisait « ≥ X »).
  (3) Mieux séparer les séances dans Actu (un encadré par séance). (4) Un bouton « au hasard » dans le topo.
- **Choix** :
  - **Liste des niveaux** dans la fenêtre d'un badge à niveaux (`BadgeDialog`) : chaque niveau avec sa médaille, sa
    condition courte, sa date une fois obtenu, le prochain surligné (`aria-current="step"`). La barre et « Prochain
    niveau » restent au-dessus. Remplace le « sans dire combien il y en a » de D-106 pour la fenêtre (la vignette
    garde « Niveau N » / « Niveau max »).
  - **Cotations une à une** (`BadgeEngine.firstGrade`) : le palier « 6b » ne vient que d'une voie en **6b-, 6b ou
    6b+** (le « - » compte maintenant, contrairement à D-105 : sinon une 6b- ne donnerait plus rien). En dessous de
    5b, rien ; une 7c+ donne « 7c ».
  - **Niveau d'un badge à paliers = palier le plus haut obtenu**, pas le nombre de paliers (serveur `ShownBadge`,
    front `badgeStates`) : identique pour les voies et les jours (paliers toujours consécutifs) ; pour les
    cotations, la couleur suit la plus dure. Une 7a sans 5b au carnet = niveau 6. Les cotations plus faciles
    manquantes s'affichent « Pas encore, celle-là » ; au niveau max avec des trous, un message dédié.
  - **Moment de déblocage et carte de séance** : pour les cotations, on affiche les cotations gagnées (« 5c, 6a »)
    au lieu d'un niveau (une 5c après une 6b ne fait pas « baisser » le niveau).
  - **Recalcul au démarrage** (`BadgeRecount`, après le seed) : tous les grimpeurs ayant une séance sont rejoués,
    chacun dans sa transaction, ligne membre verrouillée. Retire les paliers donnés par l'ancienne règle sans
    attendre une nouvelle séance ; sans changement de règle, la différence est vide et rien n'est écrit. Un échec
    est journalisé et n'empêche pas le démarrage.
  - **Fil d'actu** : séances encadrées (filet, fond « surface », coins de 4 px, 12 px entre elles) dans Actu
    uniquement (`SessionCard framed`). Exception assumée à DESIGN §4 « pas de cartes », à la demande du club ; le
    profil et le carnet gardent les blocs pleine largeur.
  - **Voie au hasard** : bouton dans la barre d'outils du topo, tirage parmi les voies **affichées** (filtres
    compris), jamais deux fois la même d'affilée quand il y a le choix (`pickRandomRoute`). Fenêtre « Et pourquoi
    pas celle-là ? » avec « Une autre ! » et « Voir la voie » (fiche de la voie, le filtre du topo conservé).
- **Statut** : validée (club, questionnaire du 2026-11-07, Q03 et Q15).

### D-112 — Revue de sécurité complète (phase 8, 2026-11-04)
- **Méthode** : quatre relectures indépendantes par sous-agents (authentification et sessions ; autorisations et IDOR sur
  tous les endpoints ; fichiers et données personnelles ; infra, CI, chaîne d'approvisionnement et front), chaque constat
  revérifié dans le code avant correction ; `pnpm audit`. Aucun point critique.
- **Corrigé** :
  - *Haut* — **Sauvegardes en échec dès la première photo** : Java écrit ses fichiers temporaires en `rw-------`, le
    conteneur `backup` (uid 70) ne pouvait pas les lire, `tar` échouait et toute la sauvegarde avec (base comprise), y
    compris celle d'avant déploiement prod. Photos écrites en `rw-r-----`, `backup` dans le groupe 10001, `media-owner`
    remet les fichiers existants en ordre ; la CI écrit sa sonde avec ces droits.
  - *Haut* — **Proxy d'entrée sur un Caddy vulnérable, jamais renouvelé** (D-102) : il tourne désormais sur l'image web de
    la release (Caddy recompilé, scanné par Trivy), épinglée par `scripts/pin-edge-image.sh` dans `release.yml` ; la CI
    démarre le proxy avec cette image.
  - *Moyen* — **Mois figé** : on ne pouvait plus retirer une photo, passer une séance en « juste moi » ni effacer son mot.
    Nouvel endpoint `PUT /sessions/{id}/privacy` et suppression de photo permis en tout mois (rien de cela ne compte dans
    les chiffres) ; boutons sous une séance figée.
  - *Moyen* — **Texte des commentaires supprimés conservé** (base et sauvegardes) : effacé à la suppression (V18 pour
    l'existant). Le journal garde le texte modéré pour la direction, mais l'oublie (libellé « Ancien·ne membre », texte
    effacé) quand l'auteur supprime son compte.
  - *Moyen* — **Rétention du journal** promise (3 ans) : tâche quotidienne `AuditRetentionJob`.
  - *Moyen* — **Images PostgreSQL jamais mises à jour** : `deploy.sh` les retélécharge à chaque déploiement.
  - *Moyen* — **Actions GitHub épinglées par étiquette** dans des workflows qui publient les images : épinglées par
    empreinte de commit (Dependabot suit) ; téléchargement de gitleaks vérifié par somme de contrôle.
  - *Bas* — profil modifiable sans limite (un compte en attente pouvait tester quels e-mails ou pseudos sont au club) :
    20 par heure. Mauvais mot de passe actuel (changement, suppression de compte) compté aussi dans la limite globale du
    pseudo. Commentaires et « Tes assurages » d'un membre désactivé : masqués / « Ancien·ne membre ». Dates des badges
    d'un autre membre ramenées au mois (les badges comptent les séances « juste moi »). Photos de séance gardées un jour
    au plus dans le cache du navigateur (un an pour avatars et photos de voies). Fichier manquant : 404 au lieu de 500.
    401 : le cache hors ligne du membre est vidé comme à la déconnexion. Commentaire trompeur de `gg-deploy`.
- **Assumé, sans changement** : session non renouvelée au changement de mot de passe (cookie `__Host-`, `HttpOnly`) ;
  verrouillage global d'un pseudo atteignable avec beaucoup d'adresses IPv6 (D-050, les sessions ouvertes ne sont pas
  touchées) ; `deploy-dev` envoie les scripts de la branche déployée (nécessaire pour tester un changement de
  déploiement en dev ; l'approbation de l'environnement reste la barrière, D-062) ; alertes `pnpm audit` limitées aux
  outils de build et de test (rien dans l'appli livrée).
- **Reste à faire (suivi)** : export « Mes données » sans les badges ni les fichiers photo ; pas de vignettes (la spec en
  prévoit) ; fichiers orphelins jamais nettoyés si la suppression échoue après validation ; empreinte `sha512` pour
  `packageManager`.
- **À trancher par Willem** :
  1. *Comptes inactifs depuis 3 ans* : « Tes données » promet leur suppression, rien ne la fait. Aucun compte ne peut
     l'atteindre avant 2029, mais c'est irréversible : faut-il l'automatiser (et à partir de quelle « dernière
     connexion ») ou retirer la promesse ?
  2. *Jeton `gh` de Claude Code* : c'est celui de Willem ; s'il peut approuver un déploiement en attente, la barrière
     de D-062 tient à ce jeton. Recommandé : un jeton à portée fine sans droit Actions/Deployments en écriture, et
     `prevent_self_review` sur les environnements. L'environnement `production` accepte aussi les étiquettes `v*` :
     garder `main` seul si elles ne servent pas.
  3. *Sauvegardes* : celles d'avant déploiement (10 gardées quel que soit leur âge) et les manuelles (jamais effacées)
     peuvent garder des comptes supprimés pendant des mois ; plafonner leur âge et le dire dans « Tes données » ?
- **Statut** : corrections faites ; point 1 tranché par D-119 (club, questionnaire du 2026-11-07, Q25), point 3 : on garde comme ça (Q26) ; point 2 à trancher par Willem.

### D-113 — Licence MIT
- **Contexte** : le dépôt n'avait pas de licence, donc aucun droit de réutilisation explicite.
- **Choix** : licence MIT, au nom de Willem Vanbaelinghem (`LICENSE` à la racine), déclarée aussi dans
  `frontend/package.json` (`"license": "MIT"`) et `backend/pom.xml` (`<licenses>`). La licence couvre le code, pas les
  données des membres ni les photos qu'ils envoient.
- **Statut** : validée par Willem (2026-11-04).

### D-114 — Audits accessibilité et Lighthouse (phase 8, 2026-11-05)
- **Contexte** : spec §13, « navigation clavier, ARIA, contraste AA dans les deux thèmes, cibles ≥ 44 px » et « Lighthouse
  ≥ 90 en performance, accessibilité, bonnes pratiques (mobile) sur les écrans principaux », à vérifier en fin de phase 8.
- **Outillage, gardé dans la CI** (job E2E) :
  - `e2e/a11y.spec.ts` passe axe-core (`@axe-core/playwright`, règles WCAG 2.2 A et AA + bonnes pratiques d'axe) sur
    chaque écran de la revue visuelle, en clair et en sombre (scénarios partagés avec les captures : `e2e/scenarios.ts`),
    écrans de téléphone sur l'iPhone 13, écrans de gestion aussi sur Chrome bureau ; plus un parcours clavier du topo.
  - `pnpm lighthouse` (`e2e/lighthouse.spec.ts`) : Lighthouse 13 en mobile (throttling simulé) sur 9 écrans
    (accueil, connexion, actu, topo, à faire, planning, carnet, podium, profil), seuil 90 par catégorie. Le build est
    servi comme par Caddy (gzip, assets en cache un an) et l'API par la fausse API des e2e (`FakeClub.handle`, rendue
    publique). La pile Docker réelle n'est pas mesurée (pas de compte de démo connecté côté Lighthouse).
- **Constats et corrections** :
  - *Repères* : le bandeau d'environnement passe dans un `<aside>` nommé, le bouton « Nouvelle séance » dans `<main>`.
  - *Nom visible dans le nom accessible* (WCAG 2.5.3, RGAA 6.1.5) : axe compare le texte affiché, découpé en morceaux
    collés dans les étiquettes de voie (« 6aBye Bye Moricecorde12 »), au nom donné par `aria-label`/`aria-labelledby` ;
    il n'y a jamais accord. Règle retenue : un lien ou un bouton qui contient une étiquette de voie, un badge ou un
    avatar est **nommé par son contenu** (le texte `sr-only` de l'étiquette, les visuels en `aria-hidden`), sans
    `aria-label` ni `aria-labelledby`. La ligne du topo devient une carte dont seul le lien de l'étiquette est focusable,
    étendu à toute la ligne par un `::after` qui porte aussi l'anneau de focus ; le reste reste sa description. Lien du
    profil dans l'en-tête : « Mon profil marie Grimpeur GG ». Boutons d'amis : « Demander en ami : jules », « Retirer de
    mes amis : tom ». Le 1er du mois s'affiche « 1er » dans le calendrier.
  - *ARIA* : `aria-controls` du calendrier seulement quand le panneau du jour existe ; un `h2` caché dans le sélecteur
    de voies de la saisie (ordre des titres) ; la zone défilante de la fiche voie est focusable (voie démontée, rien
    d'autre à atteindre au clavier).
  - *Contraste* (sombre) : nom et corde d'une voie démontée à 70 % d'encre au lieu de 60 % (4,4:1 → 6,0:1 sur le papier
    sombre). Toujours délavés à côté des 100 % / 75 % d'une voie au mur.
  - *Décalages de mise en page* (CLS) : la police de texte (Archivo, latin) est préchargée dans `index.html` (petit
    plugin Vite) ; le profil attend ses blocs (voie préférée, coups de cœur, badges) derrière le squelette au lieu de
    les empiler un à un — en chargement seulement, une requête en pause hors ligne ne cache pas les réglages ; logo de
    la porte d'entrée à son vrai ratio.
- **Résultats** (2026-11-05, local) : axe sans violation sur les 80 scénarios × 2 thèmes ; Lighthouse 99 à 100 dans les trois
  catégories sur les 9 écrans (avant : connexion 88 et profil 76 en performance, 96 en bonnes pratiques sur trois
  écrans à cause d'un ratio d'image et d'erreurs propres au banc).
- **Hors axe, déjà en place** : cibles ≥ 44 px (axe `target-size` vérifie 24 px, la règle du projet reste 44),
  `prefers-reduced-motion`, tailles en `rem` (réglages de taille du système). Le lecteur d'écran réel (VoiceOver, TalkBack)
  n'a pas été testé : à faire sur téléphone avant la 1.0.0.
- **Statut** : appliquée.

---

## Réponses du club au questionnaire (2026-11-07)

Document rempli par le bureau : `docs/GG-Pocket_questionnaire_reponses.docx` (questions Q01 à Q33, logo, idées).
Ambiguïtés tranchées par Willem le 2026-11-07. Les décisions gardées telles quelles sont marquées « validée » plus haut.

### D-115 — Cotations : « + » à partir de 5b, « - » à partir de 6b (remplace D-002)
- **Contexte** (Q04) : « Les + n'arrivent qu'à partir de 5b donc en dessous pas besoin de +. Et les moins uniquement à
  partir de 6b. »
- **Choix** : 25 cotations : 4a, 4b, 4c, 5a, 5b, 5b+, 5c, 5c+, 6a, 6a+, 6b-, 6b, 6b+, 6c-, 6c, 6c+, 7a-, 7a, 7a+,
  7b-, 7b, 7b+, 7c-, 7c, 7c+. Les 11 cotations retirées (4a- à 5a+, 5b-, 5c-, 6a-) ne sont plus proposées ; les voies,
  « À faire » et données existantes qui les portent passent à la cotation sans signe (4a+ → 4a, 6a- → 6a), confirmé par
  Willem. Nouvelle migration Flyway (rien n'est supprimé de la table `grade`, voir D-074).
- **Détail d'implémentation** : `V19__retire_unused_grades.sql` ajoute `grade.retired` (booléen), marque les 11
  cotations et fait pointer les voies vers la cotation sans signe (« À faire », séances et badges suivent la voie ;
  aucune autre table ne référence `grade`). Les rangs ne changent pas (4a reste 2, 7c+ reste 36) : seul l'ordre
  compte. `ClubLayout.sortedGrades()` écarte les cotations retirées, donc `GET /api/v1/topo/config` n'en sert plus
  que 25 (contrat inchangé) ; `RouteService` refuse une cotation retirée (`CLUB_ITEM_INACTIVE`), comme une corde
  inactive. `GradeScale` (badges) lit toujours les anciens codes. Seed de démo et fausse API e2e passent aux 25.
- **Statut** : validée (club et Willem).

### D-116 — Badges : « Mois complet » sur les séances libres, 5 voies, 2 favoris (remplace D-005, D-007 en partie, D-106 en partie)
- **Contexte** (Q11, Q13, Q16).
- **Choix** :
  - *Mois complet* : obtenu en grimpant **chaque jour de séance libre du mois** : les mardis, jeudis et dimanches, moins
    les **jours de fermeture** que la direction saisit dans l'appli (vacances, jours fériés, été). Un mois sans aucun jour
    de séance libre ne donne pas le badge.
  - *Séance full tête* et *Séance parfaite* : au moins **5 voies** dans la séance (au lieu de 3).
  - *Favoris* : **2** au maximum (au lieu de 3). Un membre qui en avait 3 garde les 2 premiers.
- **Détail d'implémentation** (5 voies et favoris faits ; « Mois complet » et jours de fermeture à part) : seuils
  de `BadgeCode.FULL_LEAD` et `PERFECT` à 5 (les badges déjà gagnés sont recomptés au démarrage par `BadgeRecount`) ;
  `FavoriteBadge.MAX` = 2 (d'où `@Size(max = 2)` et `maxItems: 2` dans le contrat), `MAX_FAVORITES` = 2 dans
  `BadgeDialog.vue` ; `V20__keep_two_favorite_badges.sql` supprime le 3e favori selon `slot`.
- **Détail d'implémentation** (« Mois complet » et jours de fermeture) :
  - table `closed_day` (`V21__create_closed_day.sql`) : une ligne par date fermée (`closed_on` unique), libellé
    facultatif de 60 caractères. Gérée par la direction et la présidence (`hasRole('BOARD')`, sous
    `/api/v1/admin/closed-days` : `GET` liste, `POST {from, to?, label?}` ferme un jour ou une plage de 366 jours
    au plus, entre l'an 2000 et deux ans après cette année, un jour déjà fermé prend le nouveau libellé ;
    `DELETE ?from&to` rouvre, sans erreur s'il n'y a rien à rouvrir). Journal : `CLUB_DAYS_CLOSED` /
    `CLUB_DAYS_REOPENED`, cible `CLOSED_DAY`, libellé « 2026-12-21 / 2027-01-04 ».
  - Les membres voient les fermetures du mois dans leur planning (`closedDays` de `GET /me/planning`), pas de liste
    complète hors gestion. Écran `/gestion/fermetures` (section à part, car `/gestion/club` est réservé à la
    présidence) ; les jours consécutifs de même libellé s'affichent en une période, rouverte d'un coup.
  - Moteur : `BadgeEngine.awards(history, closedDays)` reçoit les fermetures de tout le club ; `FreeSessions` dit
    les jours de séance libre (mardi, jeudi, dimanche, hors fermeture). Le badge tombe sur la séance qui grimpe le
    dernier jour de séance libre manquant du mois, donc parfois avant la fin du mois. Le seuil de
    `BadgeCode.MONTH_COMPLETE` (1) ne sert plus au calcul. Médaille : plus de chiffre « 10 ».
  - Recalcul : au démarrage (`BadgeRecount`) et, à chaque fermeture ou réouverture, dans la même transaction,
    pour les grimpeurs ayant une séance dans les mois touchés (`BadgeAwards.refreshMonthsOf`, verrous des membres
    pris dans l'ordre des identifiants).
- **Statut** : validée (club) ; jours de fermeture validés par Willem.

### D-117 — Statistiques de gestion : taux de réussite sans chute, une barre par cotation (remplace D-104 en partie)
- **Contexte** (Q08) : case « oui » cochée mais « Ne compter que les tentatives enchaînées » écrit ; le texte fait foi
  (Willem).
- **Choix** : taux de réussite = tentatives enchaînées (sans chute) / tentatives. L'histogramme des voies au mur a une
  barre par cotation (plus de regroupement 6a-/6a/6a+).
- **Détail d'implémentation** : `Attendance.successPercent()` divise `cleanCount` (ascensions `clean`, déjà
  comptées par `AttendanceRepository`) par le nombre de tentatives ; contrat inchangé hormis la description de
  `successPercent`. Écran Fréquentation : aide et ligne mobile parlent d'« enchaînées ». `gradeDistribution`
  (`src/lib/stats.ts`) fait une barre par cotation servie par la config (25 au plus), toujours bornée de la plus
  facile à la plus dure au mur ; les barres horizontales restent lisibles à 360 px et 1280 px.
- **Statut** : validée (club et Willem).

### D-118 — Assureur·se : un champ pour la séance, voie par voie sur demande (complète D-101)
- **Contexte** (Q10, Q20) : « Il est très rare qu'une personne ait plus qu'1 assureur dans la séance. » Willem : un champ
  pour toute la séance, et une case « Plusieurs assureur·ses » qui ouvre le champ voie par voie.
- **Choix** : le modèle ne change pas (`ascent.belayer_id`, une valeur par voie) ; le formulaire de séance propose un seul
  champ qui s'applique à toutes les voies. Cocher « Plusieurs assureur·ses » ouvre le champ sur chaque voie. À l'édition,
  la case est cochée d'office si les voies n'ont pas toutes le même assureur·se. Les règles de D-101 (membre de l'appli
  seulement, retrait libre du nom, « Mes assurages ») ne changent pas.
- **Statut** : validée (club et Willem).

### D-119 — Données personnelles : contact, comptes inactifs, date de naissance, mention « mineur » (remplace D-014, D-044 ; complète D-043, D-045)
- **Contexte** (Q24, Q25, Q28, Q29) et choix de Willem.
- **Choix** :
  - *Responsable et contact* : « Gravelines Grimp », `contact@gravelinesgrimp.org`. Le club n'a pas de politique de
    confidentialité : la page « Tes données » en tient lieu.
  - *Comptes inactifs* : sans connexion depuis **1 an**, le compte est **désactivé** (invisible des autres, connexion
    refusée avec un message qui renvoie vers la direction, qui peut le réactiver) ; sans connexion depuis **2 ans**, il
    est **supprimé** comme une suppression volontaire (D-096). Tâche planifiée quotidienne. « Tes données » le dit.
  - *Date de naissance* : **obligatoire** à l'inscription ; un membre existant sans date la saisit à sa prochaine
    connexion.
  - *Mention « mineur »* : visible **seulement de la direction et de l'admin** (fiche du membre, file des comptes en
    attente) ; les autres membres ne la voient jamais. L'accord parental sous 15 ans ne change pas.
  - *Âge sur le profil* : option « Afficher mon âge », décochée par défaut, proposée **seulement aux majeur·es** ; pour un
    mineur l'option n'apparaît pas et son âge n'est jamais montré, quel que soit le réglage enregistré (la règle est
    appliquée par le serveur au moment de servir le profil).
- **Statut** : validée (club et Willem).

### D-120 — Fil « tout le club », à double sens (remplace D-011)
- **Contexte** (Q21) : « On peut rajouter un fil “tout le club” en plus du “amis”, mais il est flouté : si quelqu'un veut
  voir ce fil, il doit accepter un réglage qui le prévient que s'il veut voir, il doit être vu aussi (un peu comme
  BeReal). » Willem : pour la 1.0, mineurs compris ; troisième visibilité de séance.
- **Choix** :
  - Une séance est « Juste moi », « Mes amis » ou **« Tout le club »**.
  - Réglage de profil **« Je participe au fil du club »**, désactivé par défaut. Tant qu'il est désactivé, l'onglet
    « Club » d'Actu montre des cartes floutées et une explication avec le bouton pour l'activer ; la visibilité
    « Tout le club » n'est pas proposée.
  - Une fois activé, on voit les séances « Tout le club » des autres participant·es. Le désactiver retire aussitôt ses
    séances du fil du club (elles restent visibles des amis) et floute de nouveau le fil.
  - Les comptes en attente ne participent pas (D-107). Réactions et commentaires : mêmes règles que le fil des amis.
- **Statut** : validée (club et Willem) ; détails d'interface à valider sur l'environnement dev.

### D-121 — Voie « à plus de 50 % » : 0,5 au classement « Voies »
- **Contexte** : demande du club du 2026-11-07 : une case « plus de 50 % » pour une voie pas finie mais grimpée au-delà de
  la moitié, qui compterait 0,5 au classement.
- **Choix** (par défaut, Willem à valider) : nouvelle issue de voie « Plus de la moitié », exclusive de « topée ». Elle
  compte **0,5** au classement « Voies » du podium et rien d'autre : ni réalisation du topo, ni badge, ni taux de réussite,
  ni « À faire » coché. Le classement affiche les demis (« 12,5 »).
- **Statut** : à valider (Willem).

### D-122 — Affichage : pastille bicolore, miniature ronde, série en flammes (complète D-013, D-093)
- **Contexte** (Q17, Q19).
- **Choix** : une voie à deux couleurs a **une** pastille coupée en deux moitiés ; la miniature de la photo d'une voie est
  ronde ; la série du planning s'affiche « 5 🔥 ».
- **Statut** : validée (club).

### D-123 — Logo du club (remplace D-039)
- **Contexte** (partie H) : logo pour fond clair (PNG 801 px sur fond blanc) et pour fond noir (PNG 2048 px) collés dans
  le document ; le fichier `Logo_GravelinesGrimp_3-couleurs.svg` est en fait une image PNG de 3775 × 3719 px (plus son
  masque de transparence) enveloppée dans du SVG, pas un vrai vectoriel. Pas de version carrée ni de charte. Utilisation
  dans l'appli autorisée.
- **Choix** :
  - sources rangées dans `docs/references/club/` (le SVG et les deux images du questionnaire) ;
  - `logo-light.webp` et `logo-dark.webp` (512 px) régénérés depuis le SVG : la version sombre remplace l'encre du texte
    (`#231F20`) par le papier du thème sombre, comme la version fond noir du club ;
  - icônes PWA, Apple et favicon : le logo complet sur le fond magnésie (zone sûre respectée pour l'icône « maskable »),
    faute de version carrée simplifiée ;
  - couleurs officielles relevées dans l'image : orange `#F26522` et vert `#C8DC5D` remplacent `#F9561A` et `#C3C04C`
    (`tokens.css`, `DESIGN.md`) ; contrastes AA conservés (encre sur orange 5,8:1, sur vert 12,1:1).
- **Statut** : appliquée en attendant une vraie source vectorielle et une version carrée (à redemander au club).
