# Décisions

Une entrée par décision : contexte, choix, alternatives, statut.
Statuts : **validée** (tranchée, à ne pas rediscuter sans raison nouvelle) ou **à valider** (choix par défaut appliqué, en attente de l'avis
de Willem ou du club). Les entrées ne sont jamais supprimées ; une décision remplacée est marquée « remplacée par D-xxx ».

Sommaire : choix par défaut du club (D-001 à D-014), versions (D-020), technique (D-030 et suivantes),
choix de la phase 1 (D-043 et suivantes ; D-048 à D-058 tranchés pendant la phase), environnements et pipeline
(D-059 à D-071, phase 2), correctifs de la 0.1.1 (D-072 et suivantes).

## Récapitulatif : tout ce qui reste à valider

Chaque ligne est appliquée par défaut ; rien n'attend une réponse pour avancer. Qui tranche : **club** (président,
direction) ou **Willem**.

| Décision | Question | Qui | Choix appliqué en attendant |
|---|---|---|---|
| D-001 | Barre d'onglets à 5 entrées ? | club | Actu, Topo, À faire, Planning, Podium ; profil via l'avatar |
| D-002 | Cotations avec les « + » ? | club | 4a à 7c avec les « + » (23 niveaux) |
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
| D-041 | Session conservée après installation sur iPhone ? | Willem + testeurs | À vérifier sur appareil réel (environnement dev, phase 2) |
| D-043 | Nom officiel de l'association et contact RGPD ? | club | « Gravelines Grimp », contact saisi par l'admin dans l'appli |
| D-044 | Durée de conservation des comptes inactifs ? | club | Suppression après 3 ans sans connexion |
| D-045 | Ce que la direction voit dans la fiche d'un membre ? | club | Tout sauf la date de naissance |
| D-046 | Format du pseudo ? | Willem | 3 à 20 caractères, `a-z 0-9 . _ -`, insensible à la casse |
| D-047 | Qui reçoit le code admin de premier démarrage ? | Willem | Willem, qui le transmet au président |
| D-049 | La direction gère grimpeurs et ouvreurs, l'admin tout le monde : ça convient ? | club | Oui (règle de sécurité) |
| D-059 | Nom de domaine du club ? | Willem | Adresses provisoires `sslip.io` tirées de l'IP du serveur |
| D-063 | Copie des sauvegardes hors du serveur ? | Willem | Aucune pour l'instant (sauvegardes sur le VPS seulement) |

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
- **Statut** : validée comme base de travail, à recontrôler sur appareil en phase 1.

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
  rechargement, pour éviter une boucle si le serveur est en panne ou le téléphone hors ligne.
- **Alternatives** : garder les fichiers des anciennes versions sur le serveur (images Docker plus lourdes, ménage à prévoir) ;
  sortir la version du fichier du Profil (ne règle que ce cas, n'importe quel écran modifié a le même problème).
- **Statut** : validée. Le diagnostic est déduit du code, sans accès aux outils de développement de l'iPhone : confirmation
  attendue sur le dev.
