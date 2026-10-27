# Roadmap

Découpage du §18 de la spec en tâches à cocher. Une phase ne commence que lorsque la précédente est verte et documentée.
Un « TODO » dans le code n'existe pas : il devient une ligne ici.

Légende : `[x]` fait · `[ ]` à faire · `[~]` commencé.

## Phase 0 — Fondations

- [x] Docs de pilotage : ROADMAP, PROGRESS, DECISIONS (choix du §19 recopiés, versions épinglées)
- [x] DESIGN.md (palette, typographie, mise en page, wireframes, étiquette de voie, relecture « générique ? »)
- [x] README (démarrage local en 5 minutes), CONTRIBUTING, ARCHITECTURE (domaines, couches, flux, modèle cible en mermaid)
- [x] `VERSION`, `CHANGELOG.md` (Keep a Changelog, section « Non publié »), `.gitignore`, `.gitattributes`, `.editorconfig`, `.env.example`
- [x] Backend : projet Maven + wrapper, Spring Boot 4.1, découpage par domaine (`platform` d'abord)
- [x] Backend : `/api/v1/meta` (version, environnement, commit, branche)
- [x] Backend : Problem Details avec `code` métier (validation, 404, 405, 401, 403, 500)
- [x] Backend : Spring Security « tout fermé sauf liste blanche »
- [x] Backend : Flyway + migration V1 (`instance_marker`) + garde-fou d'environnement au démarrage
- [x] Backend : horloge injectable (`Clock`) ; la zone `Europe/Paris` arrive avec la première règle calendaire (phase 1)
- [x] Backend : tests unitaires, tests d'intégration Testcontainers, ArchUnit (couches et entités non exposées)
- [x] Backend : contrat OpenAPI exporté et comparé au fichier commité
- [x] Frontend : Vite + Vue 3 + TS strict, ESLint/Prettier, Vitest, Playwright (iPhone 13 + Desktop Chrome)
- [x] Frontend : vue-i18n (`fr.json` structuré par écran), Vue Router, Pinia, TanStack Query
- [x] Frontend : client API typé généré depuis l'OpenAPI (`pnpm api:types`) + vérification de synchro
- [x] Frontend : tokens, thèmes clair/sombre/auto, polices auto-hébergées
- [x] Frontend : composants de base (bouton, bouton icône, avatar, badge de rôle, état vide, contrôle segmenté, squelette, grille d'inserts)
- [x] Frontend : étiquette de voie + prise dessinée
- [x] Frontend : app shell (header, 5 onglets, FAB « Nouvelle séance », bandeau DEV, pied de version) et écrans vides drôles
- [x] Frontend : galerie des composants `/dev/galerie` (dev uniquement)
- [x] `deploy/compose.local.yml` (PostgreSQL)
- [x] `.github/workflows/ci.yml` : backend, frontend, synchro des types, e2e, gitleaks
- [x] Captures clair/sombre 360 px et 1280 px, relues et critiquées
- [x] Revue critique de fin de phase (sécurité + UX), corrections
- [x] CI verte (backend : 20 tests unitaires + 10 d'intégration ; frontend : 49 Vitest + e2e iPhone 13 et Desktop Chrome)
- [x] PR « Phase 0 — Fondations » fusionnée

## Phase 1 — Comptes et rôles

- [x] Modèle `User`, rôles hiérarchiques, statut ; migrations
- [x] Codes d'inscription (CRUD admin, expiration, plafond d'utilisations, compteur) + limitation de débit sur la saisie du code
- [x] Inscription (formulaire une page, mots de passe courants refusés, accord parental < 15 ans, consentement « Tes données »)
- [x] Connexion / déconnexion, Spring Session JDBC, cookie HttpOnly Secure SameSite=Lax, 180 jours glissants, « Rester connecté »
- [x] CSRF adapté SPA : côté serveur déjà en place (`csrf.spa()`) ; côté front, middleware openapi-fetch qui recopie le cookie `XSRF-TOKEN` dans `X-XSRF-TOKEN` sur les requêtes non-GET
- [x] Limitation de débit connexion / inscription / réinitialisation ; messages qui ne révèlent pas l'existence d'un pseudo
- [x] Mes appareils (liste des sessions, fermer une ou toutes les autres)
- [x] Onboarding (photo, infos passables) ; upload d'avatar sécurisé (type réel, taille, ré-encodage, EXIF supprimé)
- [x] PWA : manifeste, service worker (stratégie `prompt`), page hors ligne
- [x] Invitation à installer (Android natif, tutoriel iOS, navigateurs intégrés, déjà installée, « Plus tard » 7 jours × 3)
- [x] Profil de base + réglages (thème, mot de passe, classements)
- [x] Gestion des membres (tableau, fiche, rôles selon matrice §5, désactivation, dernier admin protégé, pas de modification de son propre rôle)
- [x] Lien de réinitialisation 24 h usage unique (haché), fermeture des autres sessions
- [x] Journal d'audit (écriture + lecture direction/admin)
- [x] Code admin de premier démarrage (usage unique, 24 h, affiché une fois dans les logs)
- [x] Page « Tes données » (contenu de base)
- [x] Seed des comptes (profil `dev`, `GG_ALLOW_DEMO_SEED`, refus si marqueur `prod` ou comptes non seedés)
- [x] Tests d'autorisation pour chaque rôle sur chaque endpoint ; e2e inscription → installation → retour 30 jours plus tard (horloge simulée)
- [x] Revues sécurité et UX de fin de phase, corrections
- [x] PR « Phase 1 — Comptes et rôles » relue et fusionnée par Willem
- [x] Contrôle sur un vrai iPhone de la copie des cookies à l'installation (D-041) : session gardée (iOS 27, 2026-10-21)

## Phase 2 — Environnements et pipeline

- [x] Limiteur de débit : plafond dur du nombre de clés suivies (revue sécurité phase 1, L3)
- [x] Dockerfiles backend et frontend (multi-étapes, non root, version et commit injectés)
- [x] `compose.yml`, `compose.dev.yml`, `compose.prod.yml`, Caddy (TLS, en-têtes de sécurité, CSP stricte, `/api` proxifié)
- [x] Limites mémoire, `MaxRAMPercentage`, healthchecks, `restart: unless-stopped`
- [x] Dev déployé : aucun repli sur les identifiants locaux `ggpocket` (variables `GG_DB_*` obligatoires hors machine locale)
- [x] Port 8080 de l'API jamais publié ; Caddy ne relaie pas un `X-Forwarded-For` venu du client (`server.forward-headers-strategy: native` fait confiance aux réseaux privés)
- [x] `deploy-dev.yml` (manuel ou label `deploy:dev`, option reset + seed)
- [x] `prepare-release.yml`, `release.yml` (tag, branche `prod/X.Y.Z`, GitHub Release, images ghcr, déploiement approuvé)
- [x] Scripts `backup.sh`, `restore.sh` (testé), `deploy.sh`, `rollback.sh` ; rétention 14 j + 8 semaines
- [x] CI : build des images, scan Trivy, e2e contre la stack Docker
- [x] Dependabot ou Renovate
- [x] DEPLOYMENT.md
- [x] Demander à Willem les infos du §20 (serveur, domaines, DNS, accès dev)
- [x] Willem : `.github/scripts/setup-github.sh --host <ip>` puis `bootstrap.sh` sur le VPS (DEPLOYMENT.md §2-3)
- [x] Premier déploiement dev, release de test `0.1.0`, rollback testé (0.1.1 → 0.1.0 → 0.1.1, le 2026-10-21)
- [x] Protection de branche `main` (PR + CI obligatoires) : règle en place, « Deploy config » et « Docker stack » ajoutés par `setup-github.sh`

## Phase 3 — Topo

- [x] Voie préférée dans l'onboarding et le profil (D-055)
- [x] Configuration du club : secteurs, cordes, couleurs de prises, cotations (seed de configuration identique dev/prod)
- [x] Gestion des voies (créer, modifier, dupliquer, photo, styles, conseil d'ouvreur)
- [x] Liste du topo : tris, filtres combinables, regroupement par secteur
- [x] Fiche voie (statistiques d'ascensions et « mon historique » en phase 4, D-080)
- [x] Commentaires (ajout, suppression des siens, modération)
- [x] Coups de cœur (limite de 2, « Ton cœur est plein »)
- [x] « Je sais la faire » manuel (mouli / tête)
- [x] Démontage, remontage, archives ; avertissement si coups de cœur
- [x] Dernière chance + pop-up « J'accepte leurs destins »
- [x] Captures 360 / 1280 px, clair et sombre, regardées et corrigées (revue UX, D-083)

## Phase 4 — Séances

Découpée en trois PR successives (décidé avec Willem le 2026-10-23), chacune fusionnée et testable sur le dev avant la
suivante. Branches : `feature/seances-<version>`, `feature/planning-<version>`, `feature/comptes-<version>`, chacune
créée depuis `main` à jour.

### PR 4.1 — Séances (`feature/seances-0.2.0`)

- [x] Nouvelle séance (sélecteur rapide, lignes d'ascension, duplication, réordonner)
- [x] Brouillon IndexedDB + file d'attente hors ligne (photo comprise)
- [x] Photo de séance (compression client, traitement serveur)
- [x] Modification / suppression (mois non figé)
- [x] « Je sais la faire » automatique (source `ASCENT`)
- [x] Fiche voie : statistiques d'ascensions et « mon historique » ; tri du topo par réalisations (D-080)

### PR 4.2 — Planning, À faire, Carnet (`feature/planning-0.3.0`)

- [x] Planning (calendrier lundi en premier, bilan du mois, série en cours)
- [x] À faire (mouli / tête, progression, annulation)
- [x] Carnet personnel
- [x] Lecture hors ligne du topo, du profil, du planning et de l'À faire (D-095)

### PR 4.3 — Comptes et données personnelles

- [x] Export JSON de mes données, suppression de compte (libre-service et admin) avec anonymisation « Ancien·ne membre » (D-055)
- [x] Suppression de compte : supprimer soi-même le fichier et la ligne de l'avatar (`media_file.owner_id` passe à null) ;
      créditer « Ancien·ne membre » sur les voies ouvertes (`route_setter` est en `on delete cascade`)
- [x] Colonne « séances » du tableau des membres (D-055)

## Phase 5 — Social

- [ ] Annuaire (recherche, demandes d'amis)
- [ ] Amitiés (demande, acceptation, refus, pastille)
- [ ] Fil d'actu (pagination par curseur, cartes de séance)
- [ ] Réactions (4, une par personne)
- [ ] Commentaires de séance + modération

## Phase 6 — Podium et statistiques

- [ ] Classements en direct (séances, voies), ex-aequo
- [ ] Tâche de clôture mensuelle (le 8 à 04:00, idempotente) + verrouillage des séances du mois figé
- [ ] Historique des mois
- [ ] Fréquentation (export CSV), Bilan du topo

## Phase 7 — Badges

- [ ] Moteur pur et recalculable, catalogue complet, un test par badge
- [ ] Icônes de badges dessinées
- [ ] Bibliothèque de badges, badges dans le fil, ancienneté
- [ ] Moment de déblocage

## Phase 8 — Finitions et lancement

- [ ] Logo téléversable (clair/sombre) et ses emplacements ; icônes PWA ; captures d'écran du manifeste (D-055)
- [ ] « Quoi de neuf ? » (CHANGELOG embarqué, fenêtre une fois par version)
- [ ] Audits accessibilité et Lighthouse ≥ 90
- [ ] Revue de sécurité complète
- [ ] Seed final, démarrage d'une prod vide vérifié
- [ ] `docs/GUIDE_CLUB.md`
- [ ] Release `1.0.0` (déclenchée par Willem)

## Phase 9 — Après lancement (pas sans feu vert)

- [ ] Passkeys, notifications push, graphiques de progression, classement pondéré, e-mail optionnel

## Maintenance (hors phases)

- [ ] Passer à Node 26 LTS après le 2026-10-28 (D-020)
- [ ] Passer à TypeScript 7 quand typescript-eslint et openapi-typescript le supportent (D-020)
- [ ] Obtenir du club un logo vectoriel ou haute définition et sa version fond noir (D-039)
