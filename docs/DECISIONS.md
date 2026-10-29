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
| D-001 | Barre d'onglets à 5 entrées ? | club | Actu, Topo, À faire, Planning, Podium ; profil via l'avatar |
| D-002 | Cotations avec les « + » ? | club | 4a- à 7c+ avec les « - » et les « + » (36 niveaux, révisé le 2026-10-23) |
| D-003 | Classement « voies » : ascensions ou voies distinctes ? | club | Ascensions topées, répétitions comprises |
| D-004 | Séances comptées en jours de grimpe ? | club | Oui : deux séances le même jour = une |
| D-005 | Seuil du badge « Mois complet » ? | club | 10 jours de grimpe |
| D-006 | Nom « Le flash » ou « L'à vue » ? | club | « Le flash » |
| D-007 | Seuils inventés (absence, voie nouvelle, full tête, record) ? | club | 30 j, 14 j, 3 voies, dès la 4e séance |
| D-008 | Badge « Tout frais » la première année ? | club | Oui |
| D-009 | Saisie 7 jours en arrière, mois figé le 8 ? | club | Oui |
| D-010 | Coup de cœur sur voie démontée : place libérée ? | club | Oui |
| D-011 | Fil limité aux amis ? | club | Oui |
| D-012 | Pas d'e-mail, lien de réinitialisation par la direction ? | club | Oui |
| D-013 | Couleur des prises et photo de voie ? | club | Ajoutées |
| D-014 | Âge masqué, accord parental sous 15 ans ? | club | Oui |
| D-037 | Polices et étiquette de voie : le rendu plaît-il ? | club | Archivo + Kalam, à juger sur l'environnement dev |
| D-039 | Logo vectoriel / haute définition / fond noir disponible ? | club | Détourage du JPEG fourni |
| D-043 | Nom officiel de l'association et contact RGPD ? | club | « Gravelines Grimp », contact saisi par l'admin dans l'appli |
| D-044 | Durée de conservation des comptes inactifs ? | club | Suppression après 3 ans sans connexion |
| D-045 | Ce que la direction voit dans la fiche d'un membre ? | club | Tout sauf la date de naissance |
| D-046 | Format du pseudo ? | Willem | 3 à 20 caractères, `a-z 0-9 . _ -`, insensible à la casse |
| D-047 | Qui reçoit le code admin de premier démarrage ? | Willem | Willem, qui le transmet au président |
| D-049 | La direction gère grimpeurs et ouvreurs, l'admin tout le monde : ça convient ? | club | Oui (règle de sécurité) |
| D-059 | Nom de domaine du club ? | Willem | Adresses provisoires `sslip.io` tirées de l'IP du serveur |
| D-063 | Copie des sauvegardes hors du serveur ? | Willem | Aucune pour l'instant (sauvegardes sur le VPS seulement) |
| D-087 | En attendant les amis, Actu montre mes séances ? | Willem | Oui, « Mes séances » en haut du fil |
| D-090 | « Réalisations » du topo = ascensions topées ? | club | Oui, par tout le monde, répétitions comprises |
| D-093 | Série du planning : semaines d'affilée à 2 séances ou plus ? | club | Oui, la semaine en cours ne casse pas la série |
| D-094 | Le carnet : chiffres, pyramide par cotation, toutes mes voies ? | Willem | Oui, depuis le Profil ; celui des amis en phase 5 |

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
- **Révision (2026-10-23, Willem)** : le club utilise aussi les « - ». La liste devient 4a-, 4a, 4a+, … 7c-, 7c, 7c+
  (36 cotations, « - » avant la cotation simple, « + » après). Le badge « Première cotation » va jusqu'à 7c+.
  Faite dans V7, pas encore en prod.
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
- **Statut** : à valider (rendu acceptable, mais une source vectorielle serait bien meilleure). La partie « header » est
  remplacée par D-072.

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
- **Statut** : à valider.

### D-044 — Durée de conservation
- **Contexte** : la spec demande d'afficher une durée de conservation sans la fixer.
- **Choix** : données conservées tant que le compte existe ; un compte sans aucune connexion depuis 3 ans est supprimé
  (données personnelles et photos effacées, séances rattachées à « Ancien·ne membre », comme pour une suppression volontaire).
  Journal d'audit conservé 3 ans.
- **Alternatives** : 2 ans ; pas de suppression automatique (déconseillé : contraire au principe de limitation de la conservation).
- **Statut** : à valider.

### D-045 — Données visibles par la direction dans la fiche d'un membre
- **Contexte** : la spec liste les colonnes du tableau des membres, pas le contenu de la fiche.
- **Choix** : prénom, nom, pseudo, rôle, statut, inscription, dernière activité, nombre de séances, e-mail s'il est renseigné.
  **Date de naissance masquée** : la direction voit seulement « moins de 15 ans, accord parental déclaré » quand c'est le cas
  (minimisation, mineurs possibles).
- **Statut** : à valider.

### D-046 — Format du pseudo
- **Contexte** : le pseudo est l'identifiant de connexion ; la spec dit seulement « unique, insensible à la casse ».
- **Choix** : 3 à 20 caractères parmi lettres sans accents, chiffres, `.`, `_`, `-` ; unicité insensible à la casse ; casse
  d'affichage conservée telle que saisie ; modifiable ensuite par le membre (l'ancien pseudo redevient libre).
- **Alternatives** : accepter les accents (risque de doublons visuels « Élodie » / « Elodie » et de saisie difficile sur certains claviers).
- **Statut** : à valider.

### D-047 — Code admin de premier démarrage
- **Contexte** : spec §16 : au premier démarrage de la prod, un code d'inscription admin à usage unique (24 h) s'affiche une fois dans les logs.
- **Choix** : Willem le récupère et le transmet au président, qui crée le premier compte admin. Willem peut garder son propre compte
  avec le rôle de son choix (créé ensuite par code d'inscription normal, rôle donné par l'admin).
- **Statut** : à valider (sert à rédiger `DEPLOYMENT.md` et `GUIDE_CLUB.md`).


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
- **Statut** : validée (règle de sécurité) ; à relire par le club pour le confort d'usage.

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
- **Statut** : à valider (club).

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
- **Statut** : à valider (club : seuil de 2 séances par semaine).

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
- **Statut** : à valider (choix par défaut appliqués : audit conservé, photos listées sans être incluses).

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
- **Statut** : à valider (découpage et choix par défaut : refus silencieux, annuaire sans pagination, chiffres clés plus tard).

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
- **Statut** : à valider (pas de réaction sur sa propre séance ; le grimpeur ne modère pas sous sa séance ; la direction
  lit les commentaires de séance pour modérer).

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
- **Statut** : à valider (le retrait du nom sans blocage ; la liste « Mes assurages » visible même pour une séance privée,
  réduite à qui et quand).

