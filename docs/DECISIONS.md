# Décisions

Une entrée par décision : contexte, choix, alternatives, statut.
Statuts : **validée** (tranchée, à ne pas rediscuter sans raison nouvelle) ou **à valider** (choix par défaut appliqué, en attente de l'avis
de Willem ou du club). Les entrées ne sont jamais supprimées ; une décision remplacée est marquée « remplacée par D-xxx ».

Sommaire : choix par défaut du club (D-001 à D-014), versions (D-020), technique (D-030 et suivantes).

---

## Choix par défaut à faire valider par le club (spec §19)

### D-001 — Barre d'onglets à 5 entrées
- **Contexte** : la maquette de la famille a 6 onglets (Actu, Topo GG, À faire, Planning, Podium, Profil) ; 6 est trop serré à 360 px.
- **Choix** : 5 onglets (Actu, Topo, À faire, Planning, Podium). Le profil s'ouvre en touchant l'avatar du header, la gestion via une icône du header.
- **Alternatives** : 6 onglets ; menu « plus ».
- **Statut** : à valider.

### D-002 — Cotations avec les « + » de 4a à 7c
- **Contexte** : le cahier des charges cite des cotations de 4a à 7c sans préciser les « + ».
- **Choix** : 23 cotations (4a, 4a+, … 7c), stockées avec un rang numérique. Badge « Première cotation » étendu jusqu'à 7c.
- **Alternatives** : sans « + » (12 cotations).
- **Statut** : à valider.

### D-003 — Classement « voies » = ascensions topées du mois, répétitions comprises
- **Choix** : chaque ascension topée compte, même si c'est la même voie plusieurs fois.
- **Alternatives** : voies distinctes topées dans le mois.
- **Statut** : à valider.

### D-004 — Classement « séances » et badges de fréquence en jours de grimpe
- **Choix** : deux séances le même jour comptent pour un seul jour de grimpe (évite de gonfler les chiffres en coupant une séance en deux).
- **Alternatives** : compter les séances brutes.
- **Statut** : à valider.

### D-005 — « Mois complet » = 10 jours de grimpe
- **Choix** : badge « Mois complet » à 10 jours de grimpe dans le mois ; « 10 séances dans le mois » de la liste d'origine fusionné avec lui (doublon).
- **Statut** : à valider.

### D-006 — Nom du badge « Le flash »
- **Contexte** : le texte d'origine (« sans information préalable ») décrit l'**à vue** ; le flash, c'est réussir du premier coup *avec* des infos.
- **Choix** : nom « Le flash », condition du §9.5 (ascension enchaînée, premier essai, voie ouverte depuis ≤ 14 jours).
- **Alternatives** : renommer en « L'à vue » si le club préfère être puriste (changement d'un libellé i18n).
- **Statut** : à valider.

### D-007 — Seuils non précisés par le cahier des charges
- **Choix** : longue absence = 30 jours ; voie « nouvelle » = 14 jours ; séance « full tête » / « parfaite » = 3 voies minimum ;
  « séance record » à partir de la 4e séance.
- **Statut** : à valider.

### D-008 — Badge d'ancienneté « Tout frais »
- **Contexte** : le club n'a rien prévu sous 1 an d'ancienneté.
- **Choix** : « Tout frais » pour l'année d'arrivée (0 an).
- **Statut** : à valider.

### D-009 — Séances datables 7 jours en arrière, mois figé le 8
- **Choix** : une séance peut être datée jusqu'à 7 jours dans le passé ; le mois précédent est figé le 8 à 04:00 (Europe/Paris).
- **Statut** : à valider.

### D-010 — Coup de cœur sur une voie démontée
- **Choix** : la place se libère (limite de 2 coups de cœur sur des voies non démontées) ; le coup de cœur reste visible dans l'historique du profil.
- **Statut** : à valider.

### D-011 — Fil d'actu limité aux amis
- **Choix** : pas de vue « tout le club » ; séances visibles des amis seulement (ou « juste moi »).
- **Statut** : à valider.

### D-012 — Pas d'e-mail en v1
- **Choix** : mot de passe oublié = lien de réinitialisation à usage unique (24 h) généré par la direction depuis la fiche du membre.
- **Statut** : à valider.

### D-013 — Couleur des prises et photo de voie ajoutées au modèle
- **Contexte** : absentes du cahier des charges, indispensables pour reconnaître une voie au mur.
- **Choix** : 1 ou 2 couleurs de prises par voie (liste configurable) et une photo optionnelle.
- **Statut** : à valider.

### D-014 — Âge masqué par défaut, accord parental sous 15 ans
- **Choix** : « afficher mon âge » désactivé par défaut ; case « Un parent ou responsable légal est d'accord » obligatoire sous 15 ans.
- **Statut** : à valider.

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
| Node.js | 24 LTS (24.22) | Node 26 passe LTS le 2026-10-28 : montée de version prévue dans la roadmap |
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
- **Statut** : validée (côté technique) ; le rendu est à juger par le club sur l'environnement dev.

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
- **Statut** : à valider (rendu acceptable, mais une source vectorielle serait bien meilleure).

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
- **Statut** : validée comme base de travail, à recontrôler sur appareil en phase 1.

### D-042 — Branche de la phase 0
- **Choix** : `feature/fondations-0.0.0` (la prod n'existe pas encore ; `0.0.0` est la version de base). Fusion dans `main` par PR squash.
- **Statut** : validée.
