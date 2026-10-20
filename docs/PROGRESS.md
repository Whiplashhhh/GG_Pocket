# Journal des sessions

Entrée la plus récente en haut. Chaque entrée : date, branche, fait, reste, prochaine action exacte, points bloquants.

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
- e2e contre la vraie stack (`frontend/e2e-stack/`, 5 tests, Chromium, via le frontal en HTTPS) : verts.
- `docs/DEPLOYMENT.md`, CLAUDE.md (commandes), ARCHITECTURE §6, ROADMAP, CHANGELOG.
- Label `deploy:dev` créé. Le jeton `gh` de Claude n'a pas les droits d'administration (environnements, secrets, règles) :
  tout est dans `setup-github.sh` pour Willem.

### Reste

- Faire passer le test sauvegarde/restauration en CI (dernier rouge du job « Docker stack »), puis ouvrir la PR.
- Willem : `setup-github.sh --host <ip>` puis `bootstrap.sh` sur le VPS (DEPLOYMENT.md §2-3).
- Premier déploiement dev (label `deploy:dev` sur la PR), vérification D-041 sur iPhone, fusion, release `0.1.0`,
  approbation, retour arrière testé (`release.yml` lancé à la main avec la version précédente… ou `gg-deploy rollback`).

### Prochaine action exacte

- Lire les annotations du job « Docker stack » (`gh api repos/Whiplashhhh/GG_Pocket/check-runs/<job>/annotations`), corriger
  `deploy/scripts/ci-restore-test.sh` ou `restore*.sh`, pousser jusqu'au vert.

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
