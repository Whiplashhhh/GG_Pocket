# GG Pocket — manuel d'opération pour Claude Code

Ce fichier est relu à chaque session : il reste court et opérationnel.
La spécification produit complète est dans `docs/KICKOFF_PROMPT.md` : c'est la source de vérité.
Tu ne la réécris pas. Si une règle te semble fausse, incomplète ou contradictoire, tu notes le point
dans `docs/DECISIONS.md` (statut « à valider »), tu appliques le choix par défaut indiqué, et tu continues.

## Le projet en bref

PWA mobile-first pour le club d'escalade Gravelines Grimp (GG) : topo des voies, carnet de séances,
fil d'actu entre amis, « à faire », planning, podium mensuel, badges, gestion des voies et des membres.
Un seul club, ~50 à 200 utilisateurs, enjeu de sécurité modéré mais données personnelles réelles (dont mineurs possibles).

## Rituel de début de session (obligatoire)

1. Lire la dernière entrée de `docs/PROGRESS.md`, puis `docs/ROADMAP.md`.
2. `git status`, `git branch --show-current`, `git log --oneline -10`. Si une branche de travail est en cours, la reprendre.
3. Écrire en 3 à 5 lignes ce que tu vas faire dans cette session, puis y aller.

## Rituel de fin de session ou de fin d'étape (obligatoire)

1. Tests, lint et typecheck verts (voir Definition of Done).
2. Ajouter une entrée en haut de `docs/PROGRESS.md` : date, branche, ce qui est fait, ce qui reste, prochaine action exacte, points bloquants.
3. Cocher `docs/ROADMAP.md`, compléter `docs/DECISIONS.md` si tu as tranché quelque chose.
4. Ajouter les changements visibles par les utilisateurs dans `CHANGELOG.md`, section « Non publié », en français, ton du club.
5. Commit, push, ouvrir ou mettre à jour la PR.

Une session peut s'arrêter à tout moment (contexte plein, quota) : ne laisse jamais le dépôt dans un état
où la prochaine session ne sait pas où elle en est. Commit souvent, par petites unités cohérentes.

## Stack (détails et justification dans la spec, §4)

- `backend/` : Java 25 LTS, Spring Boot (dernière version stable), Spring Security, Spring Session JDBC,
  Spring Data JPA, Flyway, PostgreSQL, springdoc-openapi, Testcontainers.
- `frontend/` : Vue 3 + Vite, TypeScript strict, Vue Router, Pinia, TanStack Query (Vue), Tailwind CSS v4,
  vite-plugin-pwa, vue-i18n (fr uniquement), types générés depuis l'OpenAPI, Vitest, Playwright.
- `deploy/` : Docker Compose (prod, dev, local), Caddy, scripts de sauvegarde/restauration.
- `.github/workflows/` : CI, déploiement dev, préparation de release, release prod.

Les commandes exactes (build, test, lint, lancement local, génération des types) sont listées ici
dès qu'elles existent. Tiens cette section à jour.

### Commandes

- Base locale : `docker compose -f deploy/compose.local.yml up -d` (PostgreSQL 18, `ggpocket`/`ggpocket`).
- Backend (`backend/`) :
  - lancer : `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` (ou `./mvnw spring-boot:test-run`, base Testcontainers) ;
  - tout vérifier : `./mvnw verify` (Spotless, unitaires `*Test`, intégration `*IT` Testcontainers, ArchUnit, contrat OpenAPI) ;
  - formater : `./mvnw spotless:apply` (google-java-format AOSP ; binaire natif possible : `google-java-format --aosp --skip-reflowing-long-strings --replace`, sans quoi il recoupe les longues chaînes autrement que Spotless) ;
  - mettre à jour le contrat : `./mvnw verify -Dgg.openapi.write=true` (écrit `frontend/src/api/openapi.json`).
- Frontend (`frontend/`, Node 24, pnpm 12) :
  - `pnpm install`, `pnpm dev` (port 5173, `/api` → 8080), galerie sur `/dev/galerie` ;
  - `pnpm lint`, `pnpm format:check` (ou `pnpm format`), `pnpm typecheck`, `pnpm test` ;
  - `pnpm api:types` après tout changement du contrat (la CI vérifie la synchro) ;
  - `pnpm build && pnpm e2e` (Playwright iPhone 13 + Desktop Chrome) ;
  - `pnpm screenshots` → `frontend/screenshots/{mobile,desktop}-{light,dark}/` (à regarder avant de dire « fini »).
- Environnement de Claude Code (sans Java, Docker ni accès aux navigateurs Playwright, voir DECISIONS D-030) :
  `export PATH=$HOME/.local/bin:$PATH PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=$HOME/.local/bin/gg-chromium`,
  e2e locaux limités au projet `--project="Desktop Chrome"` ; le backend est validé par la CI.
  Le bac à sable ne récupère pas les processus zombies (chaque lancement de Chromium en laisse, plafond 1024 PID/threads) :
  lancer Playwright avec `--workers=1` et `RAYON_NUM_THREADS=2 UV_THREADPOOL_SIZE=2`, et ne capturer que les écrans utiles
  (`pnpm screenshots --project=mobile-light --grep actu`). Compteur : `cat /sys/fs/cgroup/pids.current`.

<!-- COMMANDES : à compléter en phase 0 -->

## Git

- `main` = ce qui tourne en prod. Protégée : jamais de push direct après la phase 0, jamais de force-push.
- Branches : `feature/<slug>-<version-de-base>`, `fix/<slug>-<version-de-base>`, `chore/<slug>`.
  Exemple : `feature/ecran-chat-2.5.3` (créée depuis `main` quand la prod est en 2.5.3).
- `prod/X.Y.Z` : instantanés immuables créés uniquement par le workflow de release. Tu ne les crées jamais à la main.
- Commits : Conventional Commits en anglais (`feat(topo): ...`, `fix(auth): ...`). PR en français.
- Tu ne déclenches jamais une release prod. Tu peux déclencher un déploiement dev si Willem te l'a demandé.

## Definition of Done (pour chaque fonctionnalité)

- Règles métier couvertes par des tests unitaires backend ; endpoints couverts par des tests d'intégration
  (Testcontainers) incluant un test d'autorisation par rôle (accès autorisé ET refusé).
- Logique front non triviale testée (Vitest) ; parcours clés couverts en e2e Playwright (viewport iPhone + desktop pour les écrans de gestion).
- Lint, format, typecheck : zéro erreur, zéro warning ignoré sans justification.
- Testé visuellement en clair ET en sombre, à 360 px de large, et à 1280 px pour les écrans de gestion.
  Prends des captures avec Playwright et regarde-les avant de déclarer « fini ».
- Accessibilité : labels, focus visible, contraste AA, cibles tactiles ≥ 44 px, `prefers-reduced-motion` respecté.
- Textes UI dans les fichiers i18n, jamais en dur dans les composants.
- CHANGELOG « Non publié » mis à jour si visible par les utilisateurs. Migrations Flyway jamais modifiées une fois mergées.

## Sécurité (non négociable)

- Toute autorisation est vérifiée côté serveur (méthode ou service), jamais seulement en cachant un bouton.
- Pas d'IDOR : un grimpeur ne lit ni ne modifie ce qui ne lui est pas permis, tests à l'appui.
- Aucun secret dans le dépôt. `.env.example` documenté, vrais `.env` ignorés par git.
- Uploads : vérification du type réel, taille max, ré-encodage, suppression des métadonnées EXIF (dont GPS).
- Le seed de démo ne peut physiquement pas tourner en prod (garde-fous multiples, voir spec §17).

## UX et ton

- Mobile-first. Les écrans de gestion (responsable, direction, admin) sont aussi confortables sur PC.
- Ton humoristique « vestiaire de salle d'escalade » partout où c'est possible, jamais au détriment de la clarté :
  un message d'erreur dit toujours ce qui s'est passé et quoi faire. Voir spec §11.
- Pas de look « template SaaS » : suis `docs/DESIGN.md` (créé en phase 0).

## Quand t'arrêter pour demander

- Tu as besoin d'un accès, d'un secret, d'un nom de domaine, d'une info serveur : demande, ne devine pas.
- Une décision produit n'est couverte ni par la spec ni par un choix par défaut ET elle est coûteuse à défaire :
  demande. Sinon, choisis, note dans `docs/DECISIONS.md`, continue.
