# Plan de la phase 1 — Comptes et rôles

Écrit avant de coder (spec §0 : « passe en mode plan »). Branche : `feature/comptes-0.0.0`.
Les choix qui ne sont pas dans la spec sont tranchés dans `DECISIONS.md` (D-048 et suivantes).

## Ordre de travail

1. Backend complet (CI comme seul compilateur, voir D-030), contrat OpenAPI récupéré dans les journaux de la CI.
2. Frontend : client API (CSRF, 401), garde de navigation, écrans publics, onboarding, profil, gestion, PWA.
3. e2e (API simulée avec état), captures, revues sécurité et UX par sous-agents, corrections, PR.

## Backend

### Domaines et paquets

| Domaine | Contenu |
|---|---|
| `member` | Entité `Member` (table `member` : `user` est un mot réservé de PostgreSQL), rôles, statut, profil, avatar, `MediaFile` |
| `auth` | Connexion, déconnexion, état de session, inscription, codes d'inscription, code de premier démarrage, réinitialisation, appareils, limitation de débit, politique de mot de passe |
| `admin` | Gestion des membres (direction et admin), journal d'audit |
| `platform` | Sécurité (chaîne de filtres, hiérarchie des rôles, rechargement du membre à chaque requête), erreurs (nouveaux codes, 429) |

### Migrations Flyway

| Fichier | Contenu |
|---|---|
| `V2__create_member_and_media.sql` | `member` (pseudo unique insensible à la casse via index sur `lower(nickname)`, e-mail unique si renseigné), `media_file` |
| `V3__create_spring_session.sql` | Tables de Spring Session JDBC (schéma officiel PostgreSQL de la 4.1.1) |
| `V4__create_registration_and_reset.sql` | `registration_code`, `password_reset_token` (haché) |
| `V5__create_audit_log.sql` | `audit_log` (qui, quoi, cible, avant, après, quand) |

### Endpoints (`/api/v1`)

| Méthode et chemin | Accès | Rôle |
|---|---|---|
| `GET /auth/state` | public | Connecté ou non, membre courant ; pose le cookie CSRF |
| `POST /auth/login` | public, limité | Pseudo + mot de passe + « Rester connecté » |
| `POST /auth/logout` | connecté | Ferme la session de cet appareil |
| `POST /auth/registration-code/check` | public, limité | Vérifie un code d'inscription |
| `POST /auth/register` | public, limité | Crée le compte et connecte |
| `POST /auth/password-reset/check` | public, limité | Vérifie un lien de réinitialisation |
| `POST /auth/password-reset` | public, limité | Nouveau mot de passe, ferme toutes les sessions, connecte cet appareil |
| `GET /me` · `PUT /me/profile` | connecté | Mon profil complet, modification |
| `PUT /me/avatar` · `DELETE /me/avatar` | connecté | Photo de profil (multipart) |
| `PUT /me/password` | connecté | Change le mot de passe, ferme les autres sessions |
| `GET /me/devices` · `DELETE /me/devices/{id}` · `POST /me/devices/close-others` | connecté | Mes appareils |
| `GET /media/{id}` | connecté | Image (avatar) après contrôle d'accès |
| `GET /admin/members` · `GET /admin/members/{id}` | direction+ | Tableau et fiche membre (avec les actions permises) |
| `PUT /admin/members/{id}/role` | direction+ (matrice §5) | Changement de rôle |
| `PUT /admin/members/{id}/status` | direction+ | Désactiver / réactiver |
| `POST /admin/members/{id}/password-reset-link` | direction+ | Lien de réinitialisation 24 h |
| `GET /admin/audit` | direction+ | Journal d'audit (pagination par curseur) |
| `GET/POST /admin/registration-codes` · `PUT /admin/registration-codes/{id}` | admin | Codes d'inscription |

### Tests

- Unitaires : politique de mot de passe, format du pseudo, matrice des rôles (qui peut faire quoi sur qui), dernier admin,
  limiteur de débit (horloge fixe), normalisation et validité des codes, accord parental (< 15 ans, fuseau `Europe/Paris`),
  analyse du user-agent, traitement d'image (type réel, dimensions, ré-encodage sans EXIF).
- Intégration (Testcontainers) : chaque endpoint avec un test autorisé ET refusé par rôle (anonyme, grimpeur, ouvreur,
  direction, admin) ; inscription de bout en bout ; session valide après 30 jours d'inactivité et expirée après 181 jours ;
  désactivation qui ferme les sessions ; changement de rôle pris en compte à la requête suivante ; IDOR (fermer l'appareil
  d'un autre, lire la fiche admin) ; limitation de débit ; code de premier démarrage.

## Frontend

### Écrans

| Route | Écran |
|---|---|
| `/accueil` | Accueil non connecté : logo, accroche, « J'ai un code du club », « J'ai déjà un compte » |
| `/inscription` | Code du club, puis formulaire en une page |
| `/connexion` | Connexion (trousseau : `autocomplete` corrects) |
| `/nouveau-mot-de-passe#<jeton>` | Réinitialisation (jeton dans le fragment : jamais envoyé dans les journaux ni le Referer) |
| `/tes-donnees` | Page « Tes données » (publique) |
| `/premiers-pas/photo`, `/premiers-pas/infos`, `/premiers-pas/installer` | Onboarding |
| `/profil` | Profil + réglages |
| `/profil/modifier`, `/profil/mot-de-passe`, `/profil/appareils`, `/profil/installer` | Réglages |
| `/gestion`, `/gestion/membres`, `/gestion/membres/:id`, `/gestion/journal`, `/gestion/codes` | Espace gestion (navigation latérale ≥ 1024 px) |

### Transverse

- Client openapi-fetch : en-tête `X-XSRF-TOKEN` recopié du cookie sur les requêtes non-GET ; 401 → état de session invalidé.
- Garde de navigation : routes connectées par défaut, `meta.public` pour les autres, `meta.minRole` pour la gestion.
- Hors ligne : jamais de déconnexion parce que le réseau manque ; écran « hors ligne » drôle.
- PWA : manifeste, icônes (dont maskable), service worker `prompt`, bandeau « Une nouvelle version est arrivée ».
- Invitation à installer : Android (invite native), iOS (tutoriel), navigateurs intégrés, déjà installée, « Plus tard » 7 j × 3.
- Composants : champ texte, champ mot de passe, case à cocher, feuille de dialogue (Reka UI), confirmation, sélecteur de photo
  (recadrage carré et redimensionnement dans le navigateur).

### Tests

- Vitest : détection de plateforme et règles « Plus tard », redimensionnement d'avatar (calculs), validation des formulaires,
  middleware CSRF, garde de navigation, matrice d'affichage des actions de gestion.
- Playwright (API simulée avec état, D-040) : inscription → onboarding → invitation à installer → retour 30 jours plus tard
  sans reconnexion (horloge simulée) ; connexion / déconnexion ; mot de passe oublié ; gestion des membres en iPhone 13 et
  Desktop Chrome ; refus d'accès à la gestion pour un grimpeur.

## Reporté (lignes ajoutées à la roadmap)

- Voie préférée (onboarding et profil) : phase 3, quand les voies existent.
- Export JSON et suppression de compte (libre-service et admin) : phase 4, quand les séances existent (anonymisation
  « Ancien·ne membre »).
- Colonne « séances » du tableau des membres : phase 4.
