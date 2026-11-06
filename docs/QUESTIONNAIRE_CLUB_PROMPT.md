# Prompt : questionnaire de validation pour le club

Ce fichier sert à faire valider par le club tous les choix encore « à valider » de `docs/DECISIONS.md`, et à récupérer
le logo définitif. État au 6 novembre 2026.

## Mode d'emploi (pour Willem)

1. Copier **tout ce qui se trouve entre les deux lignes `=====`** ci-dessous et le coller dans une conversation
   claude.ai. On peut joindre en plus quelques captures d'écran de l'appli (`docs/captures/`, ou celles de
   `pnpm screenshots`) : Claude les placera à côté des questions concernées.
2. Claude.ai produit un fichier Word (`.docx`) à remplir. Le relire, compléter la date limite et l'adresse de retour
   (les deux sont laissées à compléter), puis l'envoyer au bureau du club.
3. Le club coche, commente, colle son logo, et renvoie le document. Les fichiers du logo qu'on ne peut pas coller dans
   Word (SVG, PDF, AI, EPS) arrivent à part, par e-mail.
4. Déposer le document rempli et les fichiers du logo dans `docs/references/club/`, puis demander à Claude Code :
   « Applique les réponses du club : `docs/references/club/<fichier>` ». La marche à suivre de Claude Code est en bas de
   ce fichier.

Volontairement laissés de côté, parce qu'ils sont techniques et que c'est à toi de trancher :
- l'adresse du site (D-059) ;
- la copie des sauvegardes hors du serveur (D-063) ;
- le jeton `gh` et les environnements GitHub (D-112, point 2) ;
- le Caddy recompilé (D-102) ;
- « Quoi de neuf ? » (D-108) ;
- le carnet (D-094) ;
- D-087, devenu sans objet depuis l'arrivée du fil d'actu.

=====

Tu vas m'aider à préparer un questionnaire pour le bureau de mon club d'escalade.

## Contexte

Je suis Willem. Je développe bénévolement **GG Pocket**, une appli web pour téléphone (installable comme une appli)
destinée au club d'escalade **Gravelines Grimp (GG)**, à Gravelines. Elle compte de 50 à 200 membres, dont peut-être
des mineurs. On y trouve :
- le topo des voies de la salle ;
- un carnet de séances (on note les voies grimpées) ;
- un fil d'actu entre amis ;
- une liste « À faire » ;
- un planning de ses présences ;
- un podium mensuel ;
- des badges ;
- un espace de gestion pour les ouvreurs, la direction et le président.

L'appli est presque prête pour sa version 1.0. Pendant le développement, chaque fois qu'une question n'était pas
tranchée, j'ai appliqué un choix par défaut, en attendant l'avis du club. Il est temps de faire valider tous ces choix,
et de récupérer le logo définitif du club.

## Ce que tu dois produire

Un **document Word (.docx)** au format A4, que le bureau du club remplira directement. Il doit s'ouvrir sans problème
dans Word, LibreOffice et Google Docs. Le document contient :

1. **Une page de garde** :
   - le titre « GG Pocket — vos choix avant la version 1.0 » ;
   - une introduction de 5 à 8 lignes : pourquoi ce questionnaire, combien de temps il prend (20 à 30 minutes), et que
     sans réponse, **on garde le fonctionnement actuel** ;
   - le mode d'emploi : cocher en remplaçant ☐ par ☒ (ou en surlignant), écrire librement dans les zones prévues,
     coller le logo dans la partie dédiée ;
   - les champs à remplir « Rempli par », « Rôle au club » et « Date » ;
   - une date limite et une adresse de retour, sous forme de champs à compléter :
     `[date limite à compléter par Willem]` et `[adresse de retour à compléter par Willem]`.
2. **Les questions**, regroupées par thème dans l'ordre donné plus bas, avec un sommaire au début.
3. **La partie logo**, avec des cadres où coller les images.
4. **Une dernière page « Vos idées en vrac »**, avec une grande zone de texte libre.

### Présentation de chaque question

- **Un identifiant stable en tête** : `Q01`, `Q02`, etc., suivi de la référence technique en petit et en gris
  (« réf. D-003 »). Ne change jamais ces identifiants ni leur ordre : quand le document me reviendra, les réponses
  seront appliquées dans l'appli question par question, grâce à eux.
- **Un titre court.**
- **« Aujourd'hui dans l'appli »** : 2 à 4 phrases simples qui décrivent le fonctionnement actuel, avec un exemple
  concret quand ça aide (« Marie refait trois fois la même voie dans le mois : … »).
- **Les choix à cocher (☐).** Le premier est toujours « On garde comme ça ». Les options proposées sont celles que je
  donne ci-dessous, ni plus ni moins. Ajoute toujours « Autre (précisez) : » suivi d'une zone de texte.
- **Une zone « Commentaire »** de 2 à 3 lignes.
- **Les questions à plusieurs points** (marquées « sous-points ») : un petit tableau, avec une ligne par point et les
  colonnes « Aujourd'hui », « OK ? (☐ oui ☐ non) » et « Sinon, ce que vous voulez ».
- **Les questions prioritaires** (marquées ⭐ ci-dessous) : signale-les visuellement, et liste-les dans l'introduction,
  pour un lecteur pressé.

### Ton et règles d'écriture

- Écris en français, en vouvoyant le bureau (« vous »). Le ton est chaleureux, avec une pointe d'humour de vestiaire
  de salle d'escalade, mais la clarté passe toujours avant la blague.
- Pas de jargon technique : pas de « serveur », « base de données », « API », « RGPD » sans explication.
  Écris par exemple « vos données personnelles (ce que la loi appelle le RGPD) ».
- Les mots du club restent tels quels : voie, cotation, tête, moulinette (« mouli »), topée (arrivée en haut, avec ou
  sans chute), enchaînée (sans chute), ouvreur·se, assureur·se, corde, secteur.
- Ne pose aucune question en dehors de la liste, et ne tranche rien à la place du club. Tu peux reformuler pour rendre
  les questions plus claires, à condition de ne rien changer à leur sens ni aux valeurs données.
- Mise en page sobre et aérée, couleurs du club : orange, vert olive, noir et blanc. Laisse de la place pour écrire.

## Les questions

### A. Écrans et apparence

**Q01 — Les onglets** (réf. D-001)
Aujourd'hui : 5 onglets en bas de l'écran (Actu, Topo, À faire, Planning, Podium). Le profil s'ouvre en touchant sa
photo en haut à droite. La maquette d'origine avait 6 onglets, Profil compris, mais c'était trop serré sur un petit
téléphone.
Choix : on garde 5 onglets / on veut 6 onglets avec Profil, même si c'est serré.

**Q02 — Les polices** (réf. D-037)
Aujourd'hui : l'interface utilise une police droite et lisible (Archivo). Le nom des voies est écrit dans une police
« manuscrite » (Kalam), comme les étiquettes scotchées au pied du mur. Le mieux est d'en juger sur la version de test
de l'appli.
Choix : ça nous plaît / on veut changer (dites ce qui ne va pas : la police de l'interface, celle des voies, la taille…).

**Q03 — Les séances encadrées dans l'Actu** (réf. D-111)
Aujourd'hui : à votre demande, chaque séance du fil d'actu est dans un encadré. Le profil et le carnet gardent des
bandes pleine largeur, sans encadré.
Choix : on garde / des encadrés partout (profil et carnet aussi) / plus d'encadrés du tout.

### B. Cotations, podium et chiffres

**Q04 ⭐ — La liste des cotations** (réf. D-002)
Aujourd'hui : de 4a- à 7c+, avec les « - » et les « + » (4a-, 4a, 4a+, 4b-, … 7c, 7c+), soit 36 cotations.
Choix : on garde / sans les « - » / sans « - » ni « + » / il nous faut des cotations plus dures (jusqu'à : ___) /
il nous faut des cotations plus faciles (à partir de : ___).

**Q05 ⭐ — Le classement « Voies » du podium** (réf. D-003)
Aujourd'hui : chaque voie topée dans le mois compte, même si c'est la même voie refaite. Si Marie fait trois fois
« Bye Bye Morice », ça lui fait 3.
Choix : on garde / seules les voies différentes comptent (Marie aurait 1).

**Q06 — Le classement « Séances » et les badges de présence** (réf. D-004)
Aujourd'hui : on compte des **jours de grimpe**. Deux séances notées le même jour comptent pour un seul jour, pour
qu'on ne gonfle pas ses chiffres en coupant une séance en deux.
Choix : on garde / on compte chaque séance notée.

**Q07 — Le tri « les plus réalisées » du topo** (réf. D-090)
Aujourd'hui : c'est le nombre de fois où la voie a été topée, par tout le monde, répétitions comprises. La fiche
d'une voie montre aussi le nombre de grimpeur·ses différent·es et la part des passages en tête, sans aucun nom.
Choix : on garde / on trie par nombre de grimpeur·ses différent·es.

**Q08 — Les statistiques des ouvreur·ses et de la direction** (réf. D-104, sous-points)
- Le « taux de réussite » d'une voie est la part des tentatives **topées**, avec ou sans chute. Autre possibilité :
  ne compter que les tentatives **enchaînées**, sans chute.
- Le graphique des voies au mur regroupe 6a-, 6a et 6a+ dans une seule barre « 6a ». Autre possibilité : une barre par
  cotation.

**Q09 — Noter une séance après coup** (réf. D-009)
Aujourd'hui : on peut noter une séance jusqu'à 7 jours après. Le podium du mois précédent devient définitif le 8 du
mois à 4 h du matin. Après ça, les séances de ce mois ne bougent plus.
Choix : on garde / un autre délai : ___ jours.

**Q10 — Un classement « Assureur·se du mois » ?** (idée nouvelle, réf. D-101)
On peut déjà indiquer, voie par voie, qui nous assurait. Un troisième classement du podium pourrait récompenser
celui ou celle qui a le plus assuré dans le mois.
Choix : oui, on le veut / non merci / plus tard, après la version 1.0.

### C. Badges

**Q11 — Le badge « Mois complet »** (réf. D-005)
Aujourd'hui : 10 jours de grimpe dans le mois. Il existe aussi un badge « 12 séances dans le mois ».
Choix : on garde / un autre seuil : ___ jours.

**Q12 — « Le flash » ou « L'à vue » ?** (réf. D-006)
Le cahier des charges décrivait une voie réussie du premier coup « sans information préalable », ce qui est
techniquement une **à vue**. Le **flash**, c'est réussir du premier coup en ayant eu des infos (vu quelqu'un la faire,
eu la méthode). Le badge est obtenu en enchaînant, au premier essai, une voie ouverte depuis 14 jours au plus.
Choix : on garde « Le flash » / on renomme en « L'à vue » / un autre nom : ___.

**Q13 — Les seuils des badges** (réf. D-007, D-105, sous-points)
- « Le retour en force » : revenir après au moins **30 jours** sans grimper. Grimpé le 1er mars et revenu le 31 : oui ;
  revenu le 30 : non.
- « Le flash » : sur une voie ouverte depuis **14 jours** au plus, et **une seule fois par voie**.
- « Séance full tête » (tout en tête) et « Séance parfaite » (tout enchaîné) : au moins **3 voies** dans la séance.
- « La séance record » (plus de voies topées que toutes ses séances précédentes) : possible à partir de la
  **4e séance** seulement, pour que les toutes premières ne soient pas des records d'office.

**Q14 — Le badge d'ancienneté « Tout frais »** (réf. D-008)
Aujourd'hui : rien n'était prévu avant 1 an au club. Un badge « Tout frais » est donné l'année d'arrivée.
Choix : on garde / pas de badge d'ancienneté avant 1 an.

**Q15 ⭐ — Les badges « Première cotation »** (réf. D-111)
Aujourd'hui, suite à vos retours :
- chaque cotation se gagne séparément, de 5b à 7c ;
- le palier « 6b » vient d'une voie en 6b-, 6b ou 6b+ ;
- le niveau du badge affiché est la cotation la plus dure obtenue.

Exemple : un grimpeur qui tope une 7a sans jamais avoir fait de 5b a le niveau « 7a », et la 5b lui reste à gagner.

Choix : on garde / revenir à l'ancienne règle, où une 7a donnait d'un coup tous les paliers en dessous /
le « - » ne compte pas, une 6b- ne donne pas « 6b ».

**Q16 — Les couleurs des badges et les favoris** (réf. D-106, facultatif, sous-points)
- Les couleurs des niveaux reprennent celles des prises, du plus courant au plus rare : marron, vert clair, vert,
  bleu clair, bleu, violet, rose, rouge, orange, or.
- Les badges qu'on gagne plusieurs fois prennent la couleur suivante à ×1, ×2, ×3, ×5, ×10, ×15, ×25, ×50, ×75 et
  ×100 obtentions. Au-delà, ils restent or.
- Chacun peut afficher jusqu'à 3 badges favoris à côté de son pseudo.

### D. Séances, voies et « À faire »

**Q17 — Couleur des prises et photo de la voie** (réf. D-013)
Aujourd'hui : ajoutées au cahier des charges pour reconnaître une voie au mur. Chaque voie a 1 ou 2 couleurs de prises
et peut avoir une photo, facultative.
Choix : on garde / pas de photo de voie / autre.

**Q18 — Les coups de cœur** (réf. D-010)
Aujourd'hui : 2 coups de cœur au plus, sur des voies au mur. Quand une voie est démontée, la place se libère, et le
coup de cœur reste visible dans l'historique du profil.
Choix : on garde / un autre nombre de coups de cœur : ___ / autre.

**Q19 — La série du planning** (réf. D-093)
Aujourd'hui : la série compte les semaines d'affilée où l'on a grimpé **au moins 2 jours**. La semaine en cours ne
casse jamais la série avant dimanche.
Choix : on garde / 1 jour par semaine suffit / il en faut 3.

**Q20 — L'assureur·se** (réf. D-101, sous-points)
- On peut indiquer, voie par voie, qui nous assurait, mais seulement un membre de l'appli. Personne d'autre ne peut être nommé.
- La personne nommée peut retirer son nom à tout moment, sans avoir à se justifier.
- Dans « Mes assurages », elle voit qui elle a assuré, quel jour et combien de voies, même si la séance est privée,
  sans en voir le détail.
- Être nommé·e ne donne aucun accès à une séance privée.

### E. Amis et fil d'actu

**Q21 ⭐ — Un fil d'actu entre amis seulement** (réf. D-011)
Aujourd'hui : il n'y a pas de fil « tout le club ». On voit les séances de ses amis, et chaque séance est soit
« amis », soit « juste moi ». C'est aussi une protection pour les mineurs.
Choix : on garde / on veut aussi un fil « tout le club ».

**Q22 — Les amis et l'annuaire** (réf. D-098, sous-points)
- Une demande refusée l'est en silence : la personne n'est pas prévenue et peut redemander, 30 demandes par heure au
  plus.
- L'annuaire montre tous les membres validés.
- Le profil visible par les autres montre :
  - le pseudo, le rôle et la bio ;
  - les années au club, et l'âge seulement si la personne l'a choisi ;
  - la voie préférée et les coups de cœur ;
  - les badges.
- Les séances d'un membre ne sont visibles que par ses amis, et seulement celles marquées « amis ».

**Q23 — Réactions, commentaires et modération** (réf. D-100, sous-points)
- On ne peut pas réagir à sa propre séance (comme les « kudos » de Strava), mais on peut y commenter pour répondre.
- Le grimpeur d'une séance ne peut pas supprimer les commentaires des autres sous sa séance. Seuls l'auteur du
  commentaire et la direction le peuvent.
- Pour modérer, la direction voit le texte des commentaires de séance, qui l'a écrit et quel jour, mais pas la séance
  elle-même, qui reste aux amis.

### F. Comptes, mineurs et données personnelles

Signale en tête de partie que c'est le club, en tant qu'association, qui est responsable des données de ses membres.

**Q24 ⭐ — Infos à nous fournir** (réf. D-043)
Ici pas de cases à cocher : des champs à remplir.
- Nom officiel de l'association (tel qu'il figure dans les statuts) : ___
- Adresse e-mail pour les demandes des membres sur leurs données (consulter, corriger, supprimer) : ___
- Le club ou la fédération a-t-il déjà une politique de confidentialité ? ☐ non ☐ oui, lien ou document joint : ___

**Q25 ⭐ — Les comptes qui ne servent plus** (réf. D-044, D-112)
Aujourd'hui, la page « Tes données » de l'appli promet qu'un compte sans aucune connexion depuis **3 ans** est
supprimé : données personnelles et photos effacées, séances gardées sous le nom anonyme « Ancien·ne membre » pour les
statistiques des voies. Rien ne fait encore cette suppression automatiquement. Aucun compte ne peut être concerné
avant 2029, mais une suppression ne peut pas être annulée.
Choix : on garde, suppression automatique après 3 ans / après 2 ans / pas de suppression automatique, la direction
fait le ménage à la main, et on retire la promesse de la page.

**Q26 — Les copies de sauvegarde** (réf. D-112)
L'appli fait des copies de sécurité régulières de toutes les données. Certaines (celles faites avant une mise à jour,
et celles lancées à la main) n'ont pas de date limite. Un compte supprimé peut donc y rester plusieurs mois.
Choix : on garde ainsi / on limite la durée de ces copies à ___ mois (par exemple 3), et on l'écrit dans « Tes données ».

**Q27 — La suppression d'un compte** (réf. D-096, sous-points)
- Les séances sont gardées, sans commentaire ni photo, sous le nom anonyme « Ancien·ne membre », pour que les chiffres
  des voies ne changent pas.
- Photos, coups de cœur, commentaires et réactions sont effacés.
- Le journal interne de l'appli garde une ligne « compte supprimé » avec le pseudo, pendant 3 ans.
- « Télécharger mes données » donne un fichier avec tout le contenu du compte et la liste des photos, mais pas les
  photos elles-mêmes.

**Q28 ⭐ — L'âge et les moins de 15 ans** (réf. D-014)
Aujourd'hui : l'âge n'est pas affiché, sauf si la personne le choisit. Sous 15 ans, l'inscription demande de cocher
« Un parent ou responsable légal est d'accord ».
Choix : on garde / une autre limite d'âge : ___ ans / autre.

**Q29 ⭐ — Ce que la direction voit d'un membre** (réf. D-045)
Aujourd'hui, la fiche d'un membre montre à la direction :
- le prénom, le nom et le pseudo ;
- le rôle et le statut ;
- la date d'inscription et la dernière activité ;
- le nombre de séances ;
- l'e-mail, s'il est renseigné.

La date de naissance est cachée. La direction voit seulement « moins de 15 ans, accord parental déclaré » quand c'est
le cas.

Choix : on garde / la direction doit voir la date de naissance (expliquez pourquoi en commentaire) / autre.

**Q30 — Le pseudo** (réf. D-046)
Aujourd'hui : le pseudo sert à se connecter. Il fait de 3 à 20 caractères : lettres sans accents, chiffres, « . », « _ »
et « - ». « Marie » et « marie » sont le même pseudo, et on peut le changer ensuite.
Choix : on garde / accepter les accents (attention : « Élodie » et « Elodie » deviendraient deux comptes différents).

**Q31 — Mot de passe oublié** (réf. D-012)
Aujourd'hui : l'appli n'envoie pas d'e-mail. On demande à la direction, qui crée dans l'appli un lien valable 24 h,
utilisable une seule fois, et le transmet à la personne.
Choix : on garde / on voudrait un envoi automatique par e-mail (plus de travail, et un service d'envoi à mettre en place).

**Q32 ⭐ — Les nouveaux comptes et les « Touristes »** (réf. D-107, sous-points)
- Tout nouveau compte est « en attente » jusqu'à ce que la direction le valide. En attendant, il n'a que son carnet et
  reste invisible des autres : pas de fil, pas d'amis, pas de podium.
- À l'inscription, la personne déclare si elle est licenciée au club. Sinon, elle écrit en quelques mots qui elle est
  (« copain de Julie, je viens deux fois par an »).
- Ce petit texte ne sert qu'à décider : il est effacé une fois le compte validé ou refusé.
- Les grimpeurs non licenciés ont le rôle « Touriste », avec les mêmes possibilités qu'un grimpeur. Le nom du rôle
  vous convient-il ? Sinon, lequel : ___

### G. Gestion du club

**Q33 — Qui gère qui** (réf. D-049)
Aujourd'hui :
- la direction gère les grimpeur·ses, touristes compris, et les ouvreur·ses : donner ou retirer le rôle d'ouvreur·se,
  désactiver un compte, créer un lien de mot de passe ;
- le président (administrateur) gère tout le monde ;
- personne ne peut agir sur son propre compte, et il reste toujours au moins un administrateur.

Précise que c'est une règle de sécurité : la direction ne doit pas pouvoir prendre le compte du président. La
question porte sur le confort d'usage.
Choix : ça nous convient / il faudrait changer (précisez).

### H. Le logo ⭐ (réf. D-039)

Pas de cases à cocher : une demande claire et des cadres où coller.

Explique que l'appli utilise aujourd'hui un logo détouré à partir d'un petit fichier JPEG de 447 pixels sur fond
blanc, qui n'est pas net, et que le club utilise souvent son logo sur fond noir. Le logo apparaît en haut de l'appli,
sur l'écran de connexion, dans l'invitation à installer l'appli, et comme icône de l'appli sur l'écran d'accueil du
téléphone. Il reste la propriété du club.

Ce qu'on demande, du plus utile au moins utile :
1. **La version d'origine, en vectoriel** (fichier SVG, PDF, AI ou EPS), celle de l'imprimeur ou du graphiste qui
   l'a fait. À défaut, une image PNG d'au moins 1024 pixels de large, sur fond transparent.
2. **Une version pour fond clair et une version pour fond sombre ou noir.**
3. Si elle existe, **une version carrée et simplifiée** (le symbole seul, sans le texte), lisible en tout petit pour
   l'icône du téléphone.
4. **Les codes couleur officiels**, si le club a une charte graphique.

Prévois trois cadres « Collez ici le logo pour fond clair », « … pour fond sombre » et « … la version carrée ».
Le cadre « fond sombre » a un fond noir. Ajoute une ligne pour les codes couleur.

Ajoute en évidence : « Les fichiers SVG, PDF, AI ou EPS ne se collent pas dans Word. Envoyez-les en pièce jointe à
[adresse de retour à compléter par Willem], en même temps que ce document. »

Termine par :
- « Fichiers envoyés à part : ☐ oui ☐ non » ;
- « Nous autorisons l'utilisation du logo dans l'appli GG Pocket : ☐ oui ».

### I. Vos idées en vrac

Une grande zone libre pour tout ce qui n'a pas été demandé : ce qui manque, ce qui agace, ce qui plaît.

## Pour finir

Génère le fichier .docx. Ensuite, résume-moi en 5 lignes ce que contient le document et ce qu'il me reste à compléter
avant l'envoi.

=====

## Pour Claude Code : appliquer les réponses du club

Quand Willem fournit le document rempli (`.docx` ou export PDF) et les éventuels fichiers du logo :

1. **Lire le document.**
   - En `.docx` : `unzip -p <fichier>.docx word/document.xml`, puis enlever les balises. On peut aussi utiliser
     `pandoc -t plain` s'il est disponible.
   - Les images collées sont dans `word/media/`.
   - Une case est cochée si elle porte ☒, ☑, ✓, X ou si elle est surlignée. Dans le doute, demander à Willem plutôt
     que deviner.
2. **Relever chaque réponse** dans un tableau `Qxx → réf. D-xxx → réponse → commentaire`, puis traiter les
   questions dans l'ordre :
   - « On garde comme ça », ou pas de réponse : passer la décision en « validée (club, <date du document>) » dans
     `docs/DECISIONS.md`, et dans son récapitulatif en tête de fichier.
   - Un autre choix : nouvelle entrée `D-1xx` qui remplace l'ancienne (« remplacée par D-1xx »). Changer le code, les
     tests, les textes (`frontend/src/i18n/fr.json`) et la page « Tes données » si elle est concernée. Un changement
     de base passe par une **nouvelle** migration Flyway.
   - Un commentaire libre ou une idée (partie I) : l'ajouter à `docs/ROADMAP.md` (après la 1.0 sauf urgence). Ne
     rien construire sans l'accord de Willem.
3. **Logo** (D-039, ROADMAP phase 8) :
   - ranger les fichiers dans `docs/references/club/` ;
   - régénérer `frontend/src/assets/brand/` et les icônes PWA depuis la meilleure source ;
   - ajuster `docs/DESIGN.md` si les codes couleur officiels diffèrent ;
   - le téléversement du logo par l'admin dans l'appli (spec §8.11) reste la cible.
4. **Livraison** :
   - une branche `feature/decisions-club-<version>`, avec des commits par thème ;
   - une PR en français qui liste chaque Qxx et ce qui a changé ;
   - une entrée dans `CHANGELOG.md` (« Non publié ») pour tout changement visible ;
   - les captures relues en clair et en sombre.
