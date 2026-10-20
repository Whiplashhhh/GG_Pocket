# Plan de la phase 2 — Environnements et pipeline

Écrit avant de coder. Branche : `chore/environnements`. Choix hors spec : `DECISIONS.md` D-059 à D-069.

## Contexte serveur (réponses de Willem, §20)

- VPS OVH, Ubuntu, 8 Go de RAM, Docker installé, accès SSH, rien d'autre ne tourne dessus (ports 80/443 libres).
- Pas encore de nom de domaine : noms provisoires `sslip.io` dérivés de l'IP (D-059), remplacés par une variable le jour venu.
- Dépôt public `Whiplashhhh/GG_Pocket` ; Claude crée environnements, règles et secrets avec `gh`.

## Topologie sur le serveur (D-060)

```
Internet ──80/443──► edge (Caddy, réseau hôte, TLS Let's Encrypt, projet gg-pocket-edge)
                        ├─ pocket.<ip>.sslip.io     ──► 127.0.0.1:8180  web (prod)
                        └─ dev.pocket.<ip>.sslip.io ──► 127.0.0.1:8181  web (dev)

Projet gg-pocket-prod (idem gg-pocket-dev, réseaux et volumes à lui) :
  web (Caddy, statique + en-têtes + /api) ──réseau app──► api (Spring) ──réseau data (interne)──► db (PostgreSQL 18)
                                                                        backup (prod seulement) ──► db, médias
```

- L'API (8080) et la base ne sont jamais publiées ; `web` n'est publié que sur la boucle locale.
- `X-Forwarded-For` : le frontal le réécrit avec l'adresse réelle (aucun proxy de confiance), `web` fait confiance aux réseaux
  privés et le transmet, Tomcat (`native`) aussi. Le frontal en réseau hôte voit les vraies adresses IPv4 et IPv6.
- Budget mémoire : prod 768 + 512 + 64 + 128 Mo, dev 640 + 384 + 64 Mo, frontal 128 Mo ≈ 2,7 Go.

## Fichiers

| Fichier | Rôle |
|---|---|
| `.dockerignore` | Contexte de build = racine (les deux builds lisent `VERSION`) |
| `backend/Dockerfile` | Maven (JDK 25) → JRE 25, couches Spring Boot, utilisateur 10001, healthcheck sans curl |
| `frontend/Dockerfile` | Node 24 + pnpm → Caddy, utilisateur 10001, port 8080 |
| `deploy/web/Caddyfile` | Statique, cache, CSP stricte, HSTS, en-têtes, `noindex`, `/api` → `api:8080` |
| `deploy/edge/Caddyfile`, `deploy/edge/compose.yml` | Frontal partagé, TLS, option d'authentification basique du dev |
| `deploy/compose.yml` | Base commune : web, api, db, backup |
| `deploy/compose.dev.yml`, `deploy/compose.prod.yml` | Surcharges : profils Spring, limites, ports, seed, sauvegardes |
| `deploy/compose.ci.yml` | Stack de CI : images construites dans le job, seed, `http://localhost:8080` |
| `deploy/env/*.env.example` | Variables documentées de chaque projet |
| `deploy/scripts/backup.sh`, `restore.sh` | `pg_dump` + médias ; rétention 14 quotidiennes + 8 hebdomadaires ; restauration testée en CI |
| `deploy/scripts/backup-loop.sh` | Boucle du service `backup` (tous les jours à 3 h 30, heure de Paris) |
| `deploy/scripts/deploy.sh`, `rollback.sh` | Sauvegarde (prod), `pull`, `up -d`, attente du healthcheck, fumée `/api/v1/meta`, retour arrière auto |
| `deploy/scripts/server/bootstrap.sh` | À lancer une fois en root : utilisateur `deploy`, dossiers, `.env` aux secrets aléatoires, pare-feu, frontal |
| `deploy/scripts/server/gg-deploy` | Commande forcée SSH de `deploy` : n'accepte que `deploy`, `rollback`, `status`, `backup` (D-062) |
| `.github/workflows/ci.yml` | + job « Stack Docker » : images, Trivy, e2e réels, sauvegarde/restauration ; + `workflow_dispatch` |
| `.github/workflows/deploy-dev.yml` | Manuel (branche, reset) ou label `deploy:dev` ; images `dev-<sha>` |
| `.github/workflows/prepare-release.yml` | PR « Release X.Y.Z » (VERSION + CHANGELOG), CI lancée par dispatch (D-065) |
| `.github/workflows/release.yml` | Tag, `prod/X.Y.Z`, GitHub Release, images `X.Y.Z`, déploiement approuvé (`production`) |
| `.github/dependabot.yml` | Maven, npm, actions, images Docker, groupés, hebdomadaire |
| `frontend/playwright.stack.config.ts`, `frontend/e2e-stack/` | e2e contre la vraie stack (Chromium) |
| `docs/DEPLOYMENT.md` | Pas à pas : serveur, secrets, DNS, premier déploiement, release, rollback, restauration |

## Backend

- Limiteur : plafond dur de 50 000 clés, les plus calmes oubliées d'abord (D-069).
- Garde-fou : hors `local`, `GG_DB_URL`, `GG_DB_USER`, `GG_DB_PASSWORD` obligatoires et mot de passe local refusé, vérifié avant
  toute connexion (D-064).

## Ordre de travail

1. Backend (limiteur, garde-fou) — CI.
2. Dockerfiles, Caddyfiles, compose, scripts — job « Stack Docker » jusqu'au vert (CI seul juge, D-030).
3. e2e de la stack, sauvegarde/restauration en CI, Trivy.
4. Workflows dev et release, DEPLOYMENT.md.
5. GitHub : environnements `dev` et `production`, clé SSH de déploiement (secret), règles `main` et `prod/*`.
6. Willem lance `bootstrap.sh` sur le VPS ; premier déploiement dev ; release `0.1.0` ; rollback testé.
