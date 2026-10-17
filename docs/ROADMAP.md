# Roadmap

Découpage du §18 de la spec en tâches à cocher. Une phase ne commence que lorsque la précédente est verte et documentée.
Un « TODO » dans le code n'existe pas : il devient une ligne ici.

Légende : `[x]` fait · `[ ]` à faire · `[~]` commencé.

## Phase 0 — Fondations

- [x] Docs de pilotage : ROADMAP, PROGRESS, DECISIONS (choix du §19 recopiés, versions épinglées)
- [x] DESIGN.md (palette, typographie, mise en page, wireframes, étiquette de voie, relecture « générique ? »)
- [ ] README (démarrage local en 5 minutes), CONTRIBUTING, ARCHITECTURE (domaines, couches, flux, modèle cible en mermaid)
- [ ] `VERSION`, `CHANGELOG.md` (Keep a Changelog, section « Non publié »), `.gitignore`, `.gitattributes`, `.editorconfig`, `.env.example`
- [ ] Backend : projet Maven + wrapper, Spring Boot 4.1, découpage par domaine (`platform` d'abord)
- [ ] Backend : `/api/v1/meta` (version, environnement, commit, branche)
- [ ] Backend : Problem Details avec `code` métier (validation, 404, 405, 401, 403, 500)
- [ ] Backend : Spring Security « tout fermé sauf liste blanche »
- [ ] Backend : Flyway + migration V1 (`instance_marker`) + garde-fou d'environnement au démarrage
- [ ] Backend : horloge métier `Europe/Paris` injectable
- [ ] Backend : tests unitaires, tests d'intégration Testcontainers, ArchUnit (couches et entités non exposées)
- [ ] Backend : contrat OpenAPI exporté et comparé au fichier commité
- [ ] Frontend : Vite + Vue 3 + TS strict, ESLint/Prettier, Vitest, Playwright (iPhone 13 + Desktop Chrome)
- [ ] Frontend : vue-i18n (`fr.json` structuré par écran), Vue Router, Pinia, TanStack Query
- [ ] Frontend : client API typé généré depuis l'OpenAPI (`pnpm api:types`) + vérification de synchro
- [ ] Frontend : tokens, thèmes clair/sombre/auto, polices auto-hébergées
- [ ] Frontend : composants de base (bouton, bouton icône, avatar, badge de rôle, état vide, contrôle segmenté, squelette, grille d'inserts)
- [ ] Frontend : étiquette de voie + prise dessinée
- [ ] Frontend : app shell (header, 5 onglets, FAB « Nouvelle séance », bandeau DEV, pied de version) et écrans vides drôles
- [ ] Frontend : galerie des composants `/dev/galerie` (dev uniquement)
- [ ] `deploy/compose.local.yml` (PostgreSQL)
- [ ] `.github/workflows/ci.yml` : backend, frontend, synchro des types, e2e, gitleaks
- [ ] Captures clair/sombre 360 px et 1280 px, relues et critiquées
- [ ] Revue critique de fin de phase (sécurité + UX), corrections
- [ ] PR « Phase 0 — Fondations », CI verte

## Phase 1 — Comptes et rôles

- [ ] Modèle `User`, rôles hiérarchiques, statut ; migrations
- [ ] Codes d'inscription (CRUD admin, expiration, plafond d'utilisations, compteur) + limitation de débit sur la saisie du code
- [ ] Inscription (formulaire une page, mots de passe courants refusés, accord parental < 15 ans, consentement « Tes données »)
- [ ] Connexion / déconnexion, Spring Session JDBC, cookie HttpOnly Secure SameSite=Lax, 180 jours glissants, « Rester connecté »
- [ ] CSRF adapté SPA (cookie lisible + en-tête)
- [ ] Limitation de débit connexion / inscription / réinitialisation ; messages qui ne révèlent pas l'existence d'un pseudo
- [ ] Mes appareils (liste des sessions, fermer une ou toutes les autres)
- [ ] Onboarding (photo, infos passables) ; upload d'avatar sécurisé (type réel, taille, ré-encodage, EXIF supprimé)
- [ ] PWA : manifeste, service worker (stratégie `prompt`), page hors ligne
- [ ] Invitation à installer (Android natif, tutoriel iOS, navigateurs intégrés, déjà installée, « Plus tard » 7 jours × 3)
- [ ] Profil de base + réglages (thème, mot de passe, classements)
- [ ] Gestion des membres (tableau, fiche, rôles selon matrice §5, désactivation, dernier admin protégé, pas de modification de son propre rôle)
- [ ] Lien de réinitialisation 24 h usage unique (haché), fermeture des autres sessions
- [ ] Journal d'audit (écriture + lecture direction/admin)
- [ ] Code admin de premier démarrage (usage unique, 24 h, affiché une fois dans les logs)
- [ ] Page « Tes données » (contenu de base)
- [ ] Seed des comptes (profil `dev`, `GG_ALLOW_DEMO_SEED`, refus si marqueur `prod` ou comptes non seedés)
- [ ] Tests d'autorisation pour chaque rôle sur chaque endpoint ; e2e inscription → installation → retour 30 jours plus tard (horloge simulée)
- [ ] Contrôle sur un vrai iPhone de la copie des cookies à l'installation (D-041)

## Phase 2 — Environnements et pipeline

- [ ] Dockerfiles backend et frontend (multi-étapes, non root, version et commit injectés)
- [ ] `compose.yml`, `compose.dev.yml`, `compose.prod.yml`, Caddy (TLS, en-têtes de sécurité, CSP stricte, `/api` proxifié)
- [ ] Limites mémoire, `MaxRAMPercentage`, healthchecks, `restart: unless-stopped`
- [ ] `deploy-dev.yml` (manuel ou label `deploy:dev`, option reset + seed)
- [ ] `prepare-release.yml`, `release.yml` (tag, branche `prod/X.Y.Z`, GitHub Release, images ghcr, déploiement approuvé)
- [ ] Scripts `backup.sh`, `restore.sh` (testé), `deploy.sh`, `rollback.sh` ; rétention 14 j + 8 semaines
- [ ] CI : build des images, scan Trivy, e2e contre la stack Docker
- [ ] Dependabot ou Renovate
- [ ] DEPLOYMENT.md
- [ ] Demander à Willem les infos du §20 (serveur, domaines, DNS, accès dev)
- [ ] Premier déploiement dev, release de test `0.x`, rollback testé
- [ ] Protection de branche `main` (PR + CI obligatoires)

## Phase 3 — Topo

- [ ] Configuration du club : secteurs, cordes, couleurs de prises, cotations (seed de configuration identique dev/prod)
- [ ] Gestion des voies (créer, modifier, dupliquer, photo, styles, conseil d'ouvreur)
- [ ] Liste du topo : tris, filtres combinables, regroupement par secteur
- [ ] Fiche voie, statistiques, mon historique
- [ ] Commentaires (ajout, suppression des siens, modération)
- [ ] Coups de cœur (limite de 2, « Ton cœur est plein »)
- [ ] « Je sais la faire » manuel (mouli / tête)
- [ ] Démontage, remontage, archives ; avertissement si coups de cœur
- [ ] Dernière chance + pop-up « J'accepte leurs destins »

## Phase 4 — Séances

- [ ] Nouvelle séance (sélecteur rapide, lignes d'ascension, duplication, réordonner)
- [ ] Brouillon IndexedDB + file d'attente hors ligne (photo comprise)
- [ ] Photo de séance (compression client, traitement serveur)
- [ ] Modification / suppression (mois non figé)
- [ ] « Je sais la faire » automatique (source `ASCENT`)
- [ ] Planning (calendrier lundi en premier, bilan du mois, série en cours)
- [ ] À faire (mouli / tête, progression, annulation)
- [ ] Carnet personnel

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

- [ ] Logo téléversable (clair/sombre) et ses emplacements ; icônes PWA
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
