# GG Pocket

L'appli du club d'escalade **Gravelines Grimp** : topo des voies, carnet de séances, fil d'actu entre amis, « à faire »,
planning, podium mensuel et badges. Une PWA mobile d'abord, pensée pour la salle : tout à portée de pouce.

> État : phase 1 (comptes et rôles). Voir [`docs/ROADMAP.md`](docs/ROADMAP.md) et [`docs/PROGRESS.md`](docs/PROGRESS.md).

## Documentation

| Document | Pour quoi |
|---|---|
| [`docs/KICKOFF_PROMPT.md`](docs/KICKOFF_PROMPT.md) | Spécification produit complète (source de vérité) |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Domaines, couches, flux, modèle de données |
| [`docs/DESIGN.md`](docs/DESIGN.md) | Identité visuelle et design system |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | Décisions prises et choix à valider par le club |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) · [`docs/PROGRESS.md`](docs/PROGRESS.md) | Où on en est |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Workflow git, conventions, proposer une modification |
| [`CHANGELOG.md`](CHANGELOG.md) | Notes de version (affichées dans l'appli) |

## Démarrer en local (5 minutes)

### Prérequis

- **Java 25** (Temurin conseillé), **Node.js 24** (voir `.nvmrc`), **pnpm 12** (`corepack enable` suffit),
  **Docker** (base de données locale et tests d'intégration).

### 1. La base de données

```sh
docker compose -f deploy/compose.local.yml up -d
```

PostgreSQL 18 écoute sur `127.0.0.1:5432` (base, utilisateur et mot de passe : `ggpocket`, valables en local uniquement).

### 2. Le backend (API)

```sh
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

L'API répond sur <http://localhost:8080> : <http://localhost:8080/api/v1/meta> doit renvoyer la version.

Variante sans docker compose : `./mvnw spring-boot:test-run` démarre l'API avec une base jetable dans Docker (Testcontainers).

### 3. Le frontend

```sh
cd frontend
pnpm install
pnpm dev
```

L'appli s'ouvre sur <http://localhost:5173> ; les appels `/api` sont relayés vers le backend.
Utilise Chrome ou Firefox en local : le cookie de session est `Secure` (préfixe `__Host-`), ce que ces navigateurs
acceptent sur `http://localhost`, contrairement à Safari.
La galerie des composants est sur <http://localhost:5173/dev/galerie> (jamais présente dans un build de prod).

## Commandes utiles

| Où | Commande | Effet |
|---|---|---|
| `backend/` | `./mvnw verify` | Compilation, format (Spotless), tests unitaires, tests d'intégration (Testcontainers), ArchUnit, contrat OpenAPI |
| `backend/` | `./mvnw spotless:apply` | Formate le code Java (google-java-format, style AOSP) |
| `backend/` | `./mvnw verify -Dgg.openapi.write=true` | Met à jour le contrat `frontend/src/api/openapi.json` depuis le code |
| `frontend/` | `pnpm api:types` | Régénère les types TypeScript de l'API depuis le contrat |
| `frontend/` | `pnpm lint` · `pnpm format:check` · `pnpm typecheck` | Qualité (zéro avertissement toléré) |
| `frontend/` | `pnpm test` | Tests unitaires (Vitest) |
| `frontend/` | `pnpm build && pnpm e2e` | Tests de bout en bout (Playwright, iPhone 13 + Desktop Chrome) |
| `frontend/` | `pnpm screenshots` | Captures clair/sombre en 360 et 1280 px dans `frontend/screenshots/` |

Playwright télécharge ses navigateurs avec `pnpm exec playwright install`. Sur une machine où c'est impossible,
`PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/chemin/vers/chromium` fait tourner les projets Chromium avec un navigateur déjà installé.

## Environnements

| | Local | Dev | Prod |
|---|---|---|---|
| Profil Spring | `dev` | `dev` | `prod` |
| `GG_ENVIRONMENT` | `local` | `dev` | `prod` |

Les variables d'environnement sont documentées dans [`.env.example`](.env.example). Les environnements dev et prod
(Docker Compose, Caddy, pipeline de release) arrivent en phase 2 et seront décrits dans `docs/DEPLOYMENT.md`.

### Comptes de test (local et dev uniquement)

Avec `GG_ALLOW_DEMO_SEED=true` et le profil `dev`, le backend crée au démarrage un compte par rôle et un code d'inscription :

| Pseudo | Rôle | Mot de passe en local |
|---|---|---|
| `admin` | Le Président·e (admin) | `grimpe-en-local` |
| `direction` | Direction | `grimpe-en-local` |
| `ouvreur` | Ouvreur·se | `grimpe-en-local` |
| `grimpeur` | Grimpeur GG | `grimpe-en-local` |

Code d'inscription de démonstration : `DEMO-2026`. Sur le dev déployé, le mot de passe vient de `GG_SEED_PASSWORD`
(obligatoire, aucun mot de passe faible par défaut). Le seed refuse de tourner sur une base marquée `prod` ou qui contient
des comptes qu'il n'a pas créés (spec §17).

```sh
GG_ALLOW_DEMO_SEED=true ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Premier démarrage sans seed

Une base sans aucun membre reçoit un **code d'inscription admin** à usage unique, valable 24 heures, affiché une seule fois
dans les logs du backend (`Single-use admin registration code…`). Il sert à créer le premier compte admin depuis l'écran
« J'ai un code du club ». Tant que personne ne s'est inscrit, chaque redémarrage en affiche un nouveau.
