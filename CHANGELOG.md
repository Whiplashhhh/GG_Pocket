# Notes de version

Toutes les nouveautés de GG Pocket, racontées pour les grimpeurs du club.
Le format suit [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) et les versions suivent [SemVer](https://semver.org/lang/fr/).

## Non publié

### Ajouté

- Le podium ouvre ! Deux classements du mois, en direct : **Séances** (tes jours de grimpe, deux séances le même
  jour comptent pour un) et **Voies** (tes voies topées, répétitions comprises). Le top 3 monte sur les marches,
  les ex æquo partagent la même, et ta place reste affichée en bas de l’écran même quand tu es loin dans la liste.
- Remonte le temps avec les flèches : le mois dernier reste « provisoire » jusqu’au 8 à 4 h (les retardataires
  ont 7 jours pour noter leurs séances), puis il est figé pour de bon.
  Les places d’un mois figé ne bougent plus : qui quitte le club ou les classements ensuite garde sa ligne, au nom
  d’« Ancien·ne membre ».
- Pas envie d’y apparaître ? Décoche « Apparaître dans les classements du podium » dans ton profil : tu grimpes
  incognito.

### Modifié

- « Tes données » et l’export de tes données parlent aussi du podium.

## 0.8.0 — 2026-10-30

### Ajouté

- L’assureur : en saisissant ta séance, note qui tenait la corde, voie par voie (tes amis en tête, mais tout le club
  est là). La voie suivante garde le même assureur, pas besoin de le rechoisir à chaque fois. Ça s’affiche sur ta
  séance : « À l’assurage : jules et tom ».
- Profil > Mes assurages : les séances où quelqu’un t’a noté à l’assurage. Pas d’accord ? Retire ton nom en un tap.

### Modifié

- « Tes données » et l’export de tes données parlent aussi de tes assurages.

## 0.7.0 — 2026-10-28

### Ajouté

- Le fil d’actu s’anime : tes séances et celles de tes amis (celles qu’ils partagent), les plus récentes d’abord,
  avec la tête et le pseudo de qui a grimpé.
- Les réactions : 🔥 💪 👏 🦍 sous chaque séance d’un ami. Une seule par personne, tu peux changer d’avis (ou la
  reprendre) d’un tap. Sous tes séances, tu vois ce que les copains t’ont laissé.
- Les commentaires de séance : un bravo, une question sur la méthode, une vanne gentille. Tu supprimes les tiens
  quand tu veux.
- Pour la direction : Gestion > Modération, les derniers commentaires du club (voies et séances) en un seul endroit,
  à supprimer s’il le faut. Chaque suppression laisse une trace au journal.

### Modifié

- « Tes données » et l’export de tes données parlent maintenant de tes amis, de tes réactions et de tes commentaires.

## 0.6.0 — 2026-10-28

### Ajouté

- L’annuaire du club : touche « 87 grimpeurs accrochés à l’appli » en haut de l’Actu, cherche un prénom, un nom ou un
  pseudo, et demande les copains de cordée en ami. Une pastille sur ton avatar te prévient des demandes reçues :
  elles t’attendent dans ton profil, sous un bandeau à déplier. Accepter, refuser, à toi de voir.
- Le profil des autres grimpeurs : bio, années de grimpe, voie préférée, coups de cœur. Entre amis, tu vois aussi
  leurs séances (photos comprises) et leur carnet ; les séances « juste moi » restent à leur propriétaire.
- « Mes amis » dans ton profil, pour retrouver ta bande (et s’en séparer, sans que personne ne soit prévenu).

### Modifié

- L’écran d’accueil va droit au but : « Se connecter » ou « Rejoindre le club ».
- « Redescendre » devient « Se déconnecter » : plus clair, même si on perd en poésie.

## 0.5.1 — 2026-10-27

### Modifié

- L’étiquette des voies met enfin la cotation en gros, dans le bloc noir à gauche : c’est ce qu’on cherche en premier
  au pied du mur. Le numéro de corde passe à droite, en plus discret (il est toujours là pour trouver la voie).

## 0.5.0 — 2026-10-27

### Ajouté

- Mes données et mon compte, dans ton profil : télécharge en un fichier tout ce que l’appli garde sur toi (profil,
  séances, voies cochées, coups de cœur, commentaires, appareils). Et si tu raccroches les chaussons pour de bon, tu
  peux supprimer ton compte toi-même, mot de passe à l’appui. Ton profil, tes photos et tes commentaires
  disparaissent ; tes séances restent pour les stats des voies, au nom d’« Ancien·ne membre », et plus personne ne les voit.
- Gestion : le tableau des membres compte les séances de chacun (et se trie avec), et le Président·e peut supprimer
  définitivement un compte depuis la fiche membre.

### Modifié

- La page « Tes données » explique comment exporter tes données et supprimer ton compte, sans passer par la direction.

## 0.4.0 — 2026-10-26

### Ajouté

- À faire : en mouli ou en tête, la liste des voies au mur que tu ne sais pas encore faire, avec ta progression
  (« 34 / 58 voies en tête. Il en reste 24, courage. »), un filtre secteur et cotation, et le tri par cotation. Coche une
  voie, elle file ; tu as cinq secondes pour « Annuler ». « Voir celles que je sais faire » pour décocher. Une voie sue en
  tête l'est aussi en mouli, et une voie enchaînée en séance se coche toute seule.
- Planning : ton mois à la salle, lundi en premier. Chaque jour grimpé porte sa coche, touche-le pour retrouver ses
  séances. Dessous, le bilan du mois (séances, voies topées, cotation max, ressenti moyen) et ta série en cours
  (semaines d'affilée à 2 séances ou plus). Un jour de la semaine passée sans séance ? « Noter une séance ce jour-là ».
- Mon carnet (depuis le Profil) : tes chiffres, ta pyramide par cotation et toutes les voies que tu as grimpées, avec
  ta meilleure réalisation sur chacune. Les voies démontées y restent : l'histoire est sacrée.
- Le topo, ton profil, ton planning et ton À faire se consultent sans réseau (la dernière version vue).

### Corrigé

- Nouvelle séance : une voie déjà ajoutée se voit dans la liste (« ✓ Dans la séance », sur fond teinté). Le « + »
  reste là pour noter un deuxième passage, et un « − » juste à côté reprend le dernier ajouté : un doigt qui a glissé
  se rattrape sans descendre jusqu'à la ligne.

## 0.3.0 — 2026-10-25

### Ajouté

- Nouvelle séance : le gros bouton orange ouvre la feuille de saisie. Tes voies récentes d'abord, sinon tape « 12 » pour
  la corde 12 (ou un nom, une couleur, une cotation). Pour chaque voie : mouli ou tête, topée, enchaînée, chutes,
  premier essai (déjà coché si c'est vrai), ressenti. Duplique une ligne pour un deuxième essai, réordonne, retire.
  Fin de séance : ressenti global, commentaire, photo et « qui la voit ». Une séance se note jusqu'à 7 jours en arrière.
- Ta saisie est gardée sur le téléphone à chaque geste : ferme la feuille, elle t'attend. Pas de réseau au fond de la
  salle ? La séance attend et part toute seule au retour du réseau, photo comprise.
- Actu montre tes séances (en attendant tes amis) ; chaque séance a sa page pour la modifier ou la supprimer, tant que
  son mois n'est pas clos.
- Une voie enchaînée en séance se coche toute seule en « Je sais la faire ».
- L'appli installée cherche ses mises à jour quand tu reviens dessus, plus seulement au démarrage. Et si elle traîne,
  Profil → « Chercher une mise à jour » te dit où ça coince.
- La fiche voie affiche ses chiffres (ascensions, topées, grimpeur·ses, part en tête) et ton historique sur la voie ;
  le topo se trie par réalisations.

## 0.2.0 — 2026-10-24

### Ajouté

- Le topo est au mur : toutes les voies du club, avec leur étiquette, leur secteur, leurs styles, leurs coups de cœur et
  leurs commentaires. Les cotations vont de 4a- à 7c+, avec les « - » et les « + » du club. Trie par cotation, date
  d'ouverture, secteur, cœurs ou commentaires, et combine les filtres
  (secteur, cotation, couleur, style, « pas encore faite par moi »). Un lien partagé garde tes tris et tes filtres.
- La fiche voie glisse du bas : photo, ouvreur·ses, date d'ouverture, styles et surtout le conseil de l'ouvreur·se.
  Commente (et supprime tes commentaires), coche « Je sais la faire » en mouli ou en tête.
- Coups de cœur : deux voies au mur, pas une de plus. Quand ton cœur est plein, l'appli te demande laquelle tu lâches.
- Dernière chance : quand une voie va être démontée, une fenêtre te prévient à l'ouverture de l'appli. « J'accepte leurs
  destins » et elle ne revient que pour les prochaines.
- Les voies démontées partent aux archives, consultables, avec leur date de démontage. Rien ne disparaît de l'historique.
- Ta voie préférée s'affiche sur ton profil, avec tes coups de cœur : choisis-la pendant tes premiers pas ou plus tard.
- Pour les ouvreur·ses : créer, modifier et dupliquer une voie (pratique quand on rouvre sur la même corde), avec photo,
  styles et conseil ; annoncer la dernière chance, démonter (l'appli prévient si des grimpeurs l'ont en coup de cœur) et
  remonter une voie démontée par erreur. Une vue montre qui a pris connaissance de chaque dernière chance.
- Pour le Président·e : la configuration du club (secteurs, cordes, couleurs de prises), où l'on renomme, déplace et
  désactive sans jamais casser l'historique.

## 0.1.1 — 2026-10-21

### Modifié

- Le vrai logo du club s'accroche en haut de chaque page, à côté de « GG Pocket ». L'Actu n'a plus besoin de le répéter.

### Corrigé

- Sur certains iPhone, toucher ton avatar ne menait nulle part : un filtre du téléphone refusait tout fichier dont le nom
  contenait « profil ». Les fichiers de l'appli ont maintenant des noms qui ne disent rien à personne.
- Si un écran refuse quand même de se charger, l'appli recharge la page toute seule. S'il résiste encore, elle te le dit au
  lieu de rester muette, avec un retour à l'actu et un bouton pour vider son cache sans te déconnecter.

## 0.1.0 — 2026-10-21

### Ajouté

- Premières prises posées sur le mur : l'appli a son squelette, ses cinq onglets (Actu, Topo, À faire, Planning, Podium)
  et un thème clair « magnésie » ou sombre « salle fermée », au choix ou selon ton téléphone.
- L'étiquette de voie est née : numéro de corde, couleur des prises, nom écrit au feutre et cotation, comme au pied du mur.
- On s'encorde : inscription avec le code du club affiché à la salle, en une seule page, puis connexion qui tient des mois.
  Coche « Rester connecté·e » et l'appli ne te redemandera rien tant que tu passes au moins une fois tous les six mois.
- Premiers pas guidés après l'inscription : une photo si tu veux, quelques infos, et l'appli sur ton écran d'accueil
  (bouton « Installer » sur Android, mode d'emploi en trois gestes sur iPhone).
- Ton profil : photo, bio, année d'arrivée au club, depuis quand tu grimpes, âge affiché ou non, classements ou incognito.
- « Mes appareils » : vois où ta session est ouverte et ferme celles que tu ne reconnais pas.
- Mot de passe oublié ? Pas d'e-mail chez nous : la direction te crée un lien valable 24 heures pour en choisir un nouveau.
- Pour la direction et le Président·e : un espace gestion avec la liste des membres, les rôles (Ouvreur·se, Direction…),
  la désactivation d'un compte, le journal de qui a fait quoi, et les codes d'inscription à afficher à la salle.
- La page « Tes données » explique ce que l'appli garde sur toi, pourquoi, et qui le voit.
- Quand une nouvelle version arrive, un bandeau te propose de recharger : jamais de rechargement surprise en pleine saisie.
- Un mur d'essai en ligne pour les testeurs, peuplé de grimpeurs imaginaires : on y casse tout avant que le club n'y pose les mains.
