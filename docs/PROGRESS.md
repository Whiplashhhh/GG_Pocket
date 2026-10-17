# Journal des sessions

Entrée la plus récente en haut. Chaque entrée : date, branche, fait, reste, prochaine action exacte, points bloquants.

---

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
